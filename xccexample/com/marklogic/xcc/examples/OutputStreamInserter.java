/*
 * Copyright (c) 2003-2012 MarkLogic Corporation. All rights reserved.
 */
package com.marklogic.xcc.examples;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

import com.marklogic.xcc.ContentCreateOptions;
import com.marklogic.xcc.ContentSource;
import com.marklogic.xcc.ContentSourceFactory;
import com.marklogic.xcc.Session;

/**
 * <p>
 * This class illustrates a sample usage of the {@link OutputStreamContent} class.
 * </p>
 * <p>
 * Click here for the <a href="doc-files/OutputStreamInserter.java.txt"> source code for this
 * class</a>
 * </p>
 * <p>
 * A simple inner class is defined here which generates some content programmatically and writes it
 * to an {@link OutputStream}. It makes use of {@link OutputStreamContent} to obtain an
 * {@link OutputStream} instance tied to a {@link com.marklogic.xcc.Content} object.
 * </p>
 * <p>
 * The way this works is that {@link OutputStreamContent} creates a pipe (double-ended stream). A
 * thread is spawned which writes data to the sink end of the pipe. The {@link OutputStreamContent}
 * object is passed to {@link Session#insertContent(com.marklogic.xcc.Content)} which will
 * ultimately read data from the source end of the pipe. It's therefore important that the writer
 * thread be started before doing the insert.
 * </p>
 * <p>
 * To make use of {@link OutputStreamContent}, you simply need to implement the standard
 * {@link Runnable} interface and put your data transfer logic in the {@link Runnable#run()} method.
 * </p>
 * <p>
 * See the <a href="doc-files/OutputStreamInserter.java.txt">source code</a> for the main() method.
 * </p>
 * 
 * @see OutputStreamContent
 */
public class OutputStreamInserter {
    private OutputStreamInserter() {
    }

    /**
     * Looks for a server URI and a document URI on the command line, then spawns a thread to
     * generate and insert some content there.
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

        OutputStreamContent content = new OutputStreamContent(docUri, options);
        OutputStream out = content.getOutputStream();
        Runnable producer = new DocBuilder(out, 50);

        Thread thread = spawnThread(producer);

        session.insertContent(content);
        session.close();

        thread.join(); // not strictly necessary, waits for thread to finish
    }

    /**
     * A simple dummy content generator which writes to an {@link OutputStream}. This is a
     * brain-dead example that simply generates dummy content. A real implementation might be
     * fetching rows from an SQL database or some other transient source. You should only use this
     * approach if you don't have a better option. If the data can be written to a temp file or
     * provided as a standard {@link java.io.InputStream} then one of the provided implementations
     * in {@link com.marklogic.xcc.ContentFactory} should be used. Note also that inserting content
     * this way is not eligible for automatic error recovery and you can get in trouble if you don't
     * manage threads properly.
     */
    public static class DocBuilder implements Runnable {
        private final OutputStream out;
        private final int lines;

        /**
         * Initialize this instance.
         * 
         * @param out
         *            An {@link OutputStream} to which content should be written.
         * @param lines
         *            How many lines to generate.
         */
        public DocBuilder(OutputStream out, int lines) {
            this.out = out;
            this.lines = lines;
        }

        /**
         * Entry point when the new {@link Thread} starts. This is where your real business logic
         * would go, ulitmately writing data to the {@link OutputStream}.
         */
        public void run() {
            StringBuffer sb = new StringBuffer();

            try {
                out.write("<file>\n".getBytes());

                for (int i = 0; i < lines; i++) {
                    sb.setLength(0);
                    sb.append("<line index=\"");
                    sb.append(i);
                    sb.append("\">xxxxxxxxxxxxxxxxx</line>\n");
                    out.write(sb.toString().getBytes());
                }

                out.write("</file>\n".getBytes());
                out.flush();
            } catch (IOException e) {
                //noinspection CallToPrintStackTrace
                e.printStackTrace(); // you should never smother exceptions...
            } finally {
                try {
                    out.close();
                } catch (IOException e) { /* ignore */
                }
            }
        }
    }

    // ------------------------------------------------------

    /**
     * Create and start a new {@link Thread} for the given {@link Runnable}. The run() method of
     * this object will be invoked in a new thread and should write the desired content to the
     * {@link OutputStream}.
     * 
     * @param runnable
     *            An instance of {@link Runnable}.
     * @return A reference to the newly created {@link Thread}. The new thread may or may not have
     *         begun executing upon return from this method.
     */
    public static Thread spawnThread(Runnable runnable) {
        Thread thread = new Thread(runnable);

        thread.start();

        return thread;
    }

    /**
     * What were those args again?
     */
    private static void usage() {
        //noinspection UseOfSystemOutOrSystemErr
        System.err.println("usage: serveruri docuri");
    }
}
