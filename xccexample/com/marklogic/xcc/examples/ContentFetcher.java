/*
 * Copyright (c) 2003-2012 MarkLogic Corporation. All rights reserved.
 */
package com.marklogic.xcc.examples;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

import com.marklogic.xcc.ContentSource;
import com.marklogic.xcc.ContentSourceFactory;
import com.marklogic.xcc.Request;
import com.marklogic.xcc.RequestOptions;
import com.marklogic.xcc.ResultItem;
import com.marklogic.xcc.ResultSequence;
import com.marklogic.xcc.Session;
import com.marklogic.xcc.exceptions.RequestException;
import com.marklogic.xcc.exceptions.XccConfigException;

/**
 * <p>
 * This class fetches documents from the conentbase and writes their serialized contents to a
 * provided {@link OutputStream}.
 * </p>
 * <p>
 * Click here for the <a href="doc-files/ContentFetcher.java.txt"> source code for this class</a>
 * </p>
 */
public class ContentFetcher {
    private final Session session;
    private RequestOptions options = null;

    /**
     * Construct an instance that may be used to fetch documents.
     * 
     * @param serverUri
     *            A URI identifying a {@link ContentSource}, in the format expected by
     *            {@link ContentSourceFactory#newContentSource(java.net.URI)}.
     * @throws XccConfigException
     *             Thrown if a {@link Session} cannot be created. This usually indicates that the
     *             host/port or user credentials are incorrect.
     */
    public ContentFetcher(URI serverUri) throws XccConfigException {
        ContentSource cs = ContentSourceFactory.newContentSource(serverUri);

        session = cs.newSession();

        options = new RequestOptions();

        options.setCacheResult(false); // stream by default
    }

    /**
     * Fetch the document with the given URI and write its serialized form to the given
     * {@link OutputStream}
     * 
     * @param docUri
     *            The URI (name) of the document.
     * @param outStream
     *            An open {@link OutputStream} open to write the result to. This stream will be
     *            flushed before return, but not closed.
     * @throws RequestException
     *             If there is a problem issuing the request to the server.
     * @throws IOException
     *             If there is a problem writing to the {@link OutputStream}.
     * @throws IllegalArgumentException
     *             If the given URI does not exist on the server.
     */
    private void fetch(String docUri, OutputStream outStream) throws RequestException, IOException {
        Request request = session.newAdhocQuery("doc (\"" + docUri + "\")", options);
        ResultSequence rs = session.submitRequest(request);
        ResultItem item = rs.next();

        if (item == null) {
            throw new IllegalArgumentException("No document found with URI '" + docUri + "'");
        }

        item.writeTo(outStream);
    }

    /**
     * <p>
     * Set (or clear) the {@link RequestOptions} instance to associate with submitted queries.
     * </p>
     * <p>
     * Note: It's a good idea to set CachedResult=false. Since the data is being written straight
     * out to an {@link OutputStream} there is no need to buffer the document first. Streaming will
     * also accommodate arbitrarily large documents without running out of memory. Setting an
     * options value of null will use defaults, which includes cached result mode.
     * </p>
     * 
     * @param options
     *            An instance of {@link RequestOptions} or null.
     */
    public void setRequestOptions(RequestOptions options) {
        this.options = options;
    }

    // ----------------------------------------------------------------

    /**
     * Command-line main() method to fetch a document.
     * 
     * @param args
     *            Arg 1: A server URL as per
     *            {@link ContentSourceFactory#newContentSource(java.net.URI)}. Arg 2: A document
     *            URI. Optional Args 3&4: "-o outputfilename"
     * @throws Exception
     *             No exceptions are handled, anything that goes wrong will spew out a stack trace
     *             and exit.
     */
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            usage();
            return;
        }

        URI serverUri = new URI(args[0]);
        String docUri = args[1];
        OutputStream outStream;

        if (args.length == 4) {
            if (args[2].equals("-o")) {
                outStream = new BufferedOutputStream(new FileOutputStream(args[3]));
            } else {
                usage();
                return;
            }
        } else {
            outStream = System.out;
        }

        ContentFetcher fetcher = new ContentFetcher(serverUri);
        long start = System.currentTimeMillis();

        fetcher.fetch(docUri, outStream);

        if (outStream != System.out) {
            outStream.close();
        }

        System.err.println("Fetched " + docUri + " in " + formatTime(System.currentTimeMillis() - start));
    }

    // -----------------------------------------------------------------

    private static void usage() {
        System.err.println("usage: serveruri docuri [-o outfilename]");
    }

    private static final int MILLIS = 1000;
    private static final int SECONDS = MILLIS;
    private static final int MINUTES = 60 * SECONDS;
    private static final int HOURS = 60 * MINUTES;

    private static String formatTime(long millis) {
        StringBuffer sb = new StringBuffer();
        long n = millis / HOURS;

        if (n != 0) {
            sb.append(n).append("h");
        }

        n = (millis % HOURS) / MINUTES;

        if ((n) != 0) {
            sb.append(n).append("m");
        }

        sb.append((millis % MINUTES) / SECONDS).append(".");

        n = millis % MILLIS;

        if (n < 100)
            sb.append("0");
        if (n < 10)
            sb.append("0");

        sb.append(n).append("s");

        return (sb.toString());
    }
}
