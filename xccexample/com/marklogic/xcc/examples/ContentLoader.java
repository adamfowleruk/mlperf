/*
 * Copyright (c) 2003-2012 MarkLogic Corporation. All rights reserved.
 */
package com.marklogic.xcc.examples;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import com.marklogic.xcc.Content;
import com.marklogic.xcc.ContentCreateOptions;
import com.marklogic.xcc.ContentFactory;
import com.marklogic.xcc.ContentSource;
import com.marklogic.xcc.ContentSourceFactory;
import com.marklogic.xcc.Session;
import com.marklogic.xcc.exceptions.RequestException;
import com.marklogic.xcc.exceptions.XccConfigException;

/**
 * <p>
 * This program accepts a server URI (in the format expected by
 * {@link ContentSourceFactory#newContentSource(java.net.URI)}) and one or more file pathnames of
 * documents to load.
 * </p>
 * <p>
 * Click here for the <a href="doc-files/ContentLoader.java.txt"> source code for this class</a>
 * </p>
 * <p>
 * The main() method in this example leaves the {@link ContentCreateOptions} value set to null which
 * will apply defaults when documents are loaded. This means that document type (XML, text() or
 * binary()) may be determined by URI extention. If you instantiate this class from another class,
 * you can set explicit options via {@link #setOptions(com.marklogic.xcc.ContentCreateOptions)}
 * before calling {@link #load(java.io.File[])}.
 * </p>
 * <p>
 * The main() method uses the absolute pathname of each file as its URI for loading. There is also a
 * method provided which takes parallel arrays of URI strings and File objects.
 * </p>
 */
public class ContentLoader {
    private final Session session;
    private ContentCreateOptions options = null;

    /**
     * Construct an instance that may be used to insert content.
     * 
     * @param serverUri
     *            A URI identifying a {@link ContentSource}, in the format expected by
     *            {@link ContentSourceFactory#newContentSource(java.net.URI)}.
     * @throws XccConfigException
     *             Thrown if a {@link Session} cannot be created. This usually indicates that the
     *             host/port or user credentials are incorrect.
     */
    public ContentLoader(URI serverUri) throws XccConfigException {
        ContentSource cs = ContentSourceFactory.newContentSource(serverUri);

        session = cs.newSession();
    }

    /**
     * Load the provided {@link File}s, using the provided URIs, into the content server.
     * 
     * @param uris
     *            An array of URIs (identifiers) that correspond to the {@link File} instances given
     *            in the "files" parameter.
     * @param files
     *            An array of {@link File} objects representing disk files to be loaded. The
     *            {@link ContentCreateOptions} object set with
     *            {@link #setOptions(com.marklogic.xcc.ContentCreateOptions)}, if any, will be
     *            applied to all documents when they are loaded.
     * @throws RequestException
     *             If there is an unrecoverable problem with sending the data to the server. If this
     *             exception is thrown, none of the documents will have been committed to the
     *             contentbase.
     */
    public void load(String[] uris, File[] files) throws RequestException {
        Content[] contents = new Content[files.length];

        for (int i = 0; i < files.length; i++) {
            contents[i] = ContentFactory.newContent(uris[i], files[i], options);
        }

        session.insertContent(contents);
    }

    /**
     * Load the provided {@link File}s into the contentbase, using the absolute pathname of each
     * {@link File} as the document URI.
     * 
     * @param files
     *            An array of {@link File} objects representing disk files to be loaded. The
     *            {@link ContentCreateOptions} object set with
     *            {@link #setOptions(com.marklogic.xcc.ContentCreateOptions)}, if any, will be
     *            applied to all documents when they are loaded.
     * @throws RequestException
     *             If there is an unrecoverable problem with sending the data to the server. If this
     *             exception is thrown, none of the documents will have been committed to the
     *             contentbase.
     */
    public void load(File[] files) throws RequestException {
        String[] uris = new String[files.length];

        for (int i = 0; i < files.length; i++) {
            uris[i] = files[i].getAbsolutePath();
        }

        load(uris, files);
    }

    /**
     * Set (or clear) an instance of {@link ContentCreateOptions} which defines creation options to
     * apply to each document loaded. This is null (defaults) unless explictly set.
     * 
     * @param options
     */
    public void setOptions(ContentCreateOptions options) {
        this.options = options;
    }

    // -------------------------------------------------------

    /**
     * Command-line main() module to run this content loader.
     * 
     * @param args
     *            A URI [arg 0] identifying the server/port/user/db where the content should be
     *            inserted and one or more [args 1-n] giving file pathnames of documents to load.
     * @throws URISyntaxException
     *             If there is a problems interpreting the URI.
     * @throws XccConfigException
     *             If a {@link Session} cannot be created.
     * @throws RequestException
     *             If the content cannot be inserted in the contentbase.
     */
    public static void main(String[] args) throws URISyntaxException, XccConfigException, RequestException {
        if (args.length < 2) {
            usage();
            return;
        }

        URI serverUri = new URI(args[0]);
        File[] files = new File[args.length - 1];
        long totalByteCount = 0;

        for (int i = 0; i < files.length; i++) {
            files[i] = new File(args[i + 1]);
            totalByteCount += files[i].length();
        }

        ContentLoader loader = new ContentLoader(serverUri);

        long start = System.currentTimeMillis();

        loader.load(files);

        long elapsed = System.currentTimeMillis() - start;

        System.out.println("Loaded " + files.length + " files (" + QueryHelper.formatInteger(totalByteCount)
                + " bytes) in " + QueryHelper.formatTime(elapsed) + " ("
                + QueryHelper.formatInteger(QueryHelper.bytesPerSecond(totalByteCount, elapsed)) + " bytes/second)");
    }

    // ------------------------------------------------------

    private static void usage() {
        System.err.println("usage: serveruri docpath ...");
    }
}
