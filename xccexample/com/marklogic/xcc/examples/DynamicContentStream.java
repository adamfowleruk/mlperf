/*
 * Copyright (c) 2003-2012 MarkLogic Corporation. All rights reserved.
 */
package com.marklogic.xcc.examples;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import com.marklogic.xcc.Content;
import com.marklogic.xcc.ContentCreateOptions;
import com.marklogic.xcc.ContentFactory;
import com.marklogic.xcc.ContentSource;
import com.marklogic.xcc.ContentSourceFactory;
import com.marklogic.xcc.Session;

/**
 * <p>
 * This class, which extends {@link InputStream}, is an example of producing dynamic, on-the-fly
 * content that can be used with
 * {@link ContentFactory#newUnBufferedContent(String, java.io.InputStream, com.marklogic.xcc.ContentCreateOptions)}
 * .
 * </p>
 * <p>
 * Click here for the <a href="doc-files/DynamicContentStream.java.txt"> source code for this
 * class</a>
 * </p>
 * <p>
 * Like {@link OutputStreamInserter}, this is an example of inserting dynamically generated or
 * transformed on-the-fly content. The {@link OutputStreamInserter} example uses
 * {@link OutputStreamContent} and spawns a thread which then loops and writes to an
 * {@link java.io.OutputStream}.
 * </p>
 * <p>
 * By contrast, this example illustrates how to insert dynamic content without creating a new
 * thread. Here, a custom {@link InputStream} is passed to the standard factory
 * {@link ContentFactory#newUnBufferedContent(String, java.io.InputStream, com.marklogic.xcc.ContentCreateOptions)}
 * . When {@link Session#insertContent(com.marklogic.xcc.Content)} is invoked, it will repeatedly
 * call back into this class to obtain chunks of data to send to the server.
 * </p>
 * <p>
 * If your data generation can be expressed this way, multiple calls to obtain the data in chunks,
 * it is preferrable to spawning a new thread.
 * </p>
 * <p>
 * Note that if your content is potentially large, you should use the above unbuffered factory
 * method. If you call
 * {@link ContentFactory#newContent(String, java.io.InputStream, com.marklogic.xcc.ContentCreateOptions)}
 * , XCC will first consume and buffer the entire stream before attempting the insert. If the
 * content is unbuffered, then automatic error recovery cannot be performed.
 * </p>
 */
public class DynamicContentStream extends InputStream {
    private int position = -1;
    private int limit = -1;
    private byte[] buffer = new byte[1024];

    // -----------------------------------------------------
    // Methods overridden from InputStream

    /**
     * Read a single byte. This method passes through to the byte-array read().
     * 
     * @return A byte value, as an integer, or -1 on EOF.
     * @throws IOException
     *             If there is a problem.
     */
    @Override
    public int read() throws IOException {
        byte[] temp = new byte[1];
        int rc = read(temp, 0, 1);

        return (rc == -1) ? -1 : (int)temp[0];
    }

    /**
     * Read "len" bytes into "userBuffer", starting at "off". This method copies bytes from an
     * internal holding buffer out to the provided buffer. On underflow, when the internal buffer is
     * exhausted, the fillBuffer() method is called to obtain more data. Your logic, which may be
     * fetching data from an SQL source or a web service and transforming it on the fly, would
     * placed in fillBUffer().
     * 
     * @param userBuffer
     *            The buffer to place data in.
     * @param off
     *            Index into userBuffer where data should be placed.
     * @param len
     *            The maximum number of bytes to transfer.
     * @return The number of bytes actually transferred, or -1 on EOS.
     * @throws IOException
     *             Will not happen in this sample implementation but could conceivably be thrown by
     *             fillBUffer().
     */
    @Override
    public int read(byte userBuffer[], int off, int len) throws IOException {
        if (position == limit) {
            limit = fillBuffer(buffer);

            if (limit == -1) {
                return -1;
            }

            position = 0;
        }

        int copyLen = Math.min(limit - position, len);

        System.arraycopy(buffer, position, userBuffer, off, copyLen);

        position += copyLen;

        return copyLen;
    }

    // -----------------------------------------------------

    private final int NEW = 0;
    private final int RUNNING = 1;
    private final int FINISHING = 2;
    private final int DONE = 3;
    private final int lines;
    private int state = NEW;
    private int currentLine = 0;

    /**
     * Constructor for the example, which takes the number of lines to generate in the dummy
     * document.
     * 
     * @param lines
     *            Number of lines to put in the programmatically generated document.
     */
    public DynamicContentStream(int lines) {
        this.lines = lines;
    }

    // -----------------------------------------------------

    /**
     * This method will be called repeatedly as XCC reads data from the {@link InputStream} (this
     * object). In a real app, this method would do something more useful, such as reading data from
     * some other source and transforming it. This dummy implementation uses a simple state machine
     * to produce the beginning of a document, then the variable length body, then the end and
     * finally indicates EOS.
     * 
     * @param buffer
     *            The byte array to which data should be copied.
     * @return The number of bytes transferred, or -1 if there are no more bytes available.
     */
    public int fillBuffer(byte[] buffer) {
        if (state == DONE)
            return -1;

        if (state == NEW) {
            state = RUNNING;
            return copyStringBytes("<file>\n", buffer, 0);
        }

        if (state == FINISHING) {
            state = DONE;
            return copyStringBytes("</file>\n", buffer, 0);
        }

        int count = 0;

        count += copyStringBytes("\t<line index=\"" + currentLine + "\">", buffer, count);

        for (int i = 0; i < 26; i++) {
            buffer[count] = (byte)('a' + ((i + currentLine) % 26));
            count++;
        }

        count += copyStringBytes("</line>\n", buffer, count);

        currentLine++;

        if (currentLine == lines) {
            state = FINISHING;
        }

        return count;
    }

    /**
     * Given a {@link String}, extract the bytes (using default encoding) and copy them to the given
     * byte array, staring at the given offset. No length check is done to confirm that there is
     * sufficient space.
     * 
     * @param str
     *            The String.
     * @param buffer
     *            The byte array.
     * @param offset
     *            The offset into the array.
     * @return The number of bytes copied.
     */
    private int copyStringBytes(String str, byte[] buffer, int offset) {
        byte[] bytes = str.getBytes();

        System.arraycopy(bytes, 0, buffer, offset, bytes.length);

        return bytes.length;
    }

    // -------------------------------------------------------
    // -------------------------------------------------------

    /**
     * Example invocation that uses an instance of this class to insert dynamically-generated
     * content, without spawning a separate thread.
     * 
     * @param args
     *            Server URI: xcc://user:password@host:port/contentbase, Doc URI: any valid URI
     *            string to assign to the new content.
     * @throws Exception
     *             If anything bad happens.
     */
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            usage();
            return;
        }

        URI serverUri = new URI(args[0]);
        String docUri = args[1];
        ContentSource cs = ContentSourceFactory.newContentSource(serverUri);
        Session session = cs.newSession();
        ContentCreateOptions options = ContentCreateOptions.newXmlInstance();

        InputStream inputStream = new DynamicContentStream(30);
        Content content = ContentFactory.newUnBufferedContent(docUri, inputStream, options);

        session.insertContent(content);
        session.close();
    }

    /**
     * What were those args again?
     */
    private static void usage() {
        //noinspection UseOfSystemOutOrSystemErr
        System.err.println("usage: serveruri docuri");
    }

}
