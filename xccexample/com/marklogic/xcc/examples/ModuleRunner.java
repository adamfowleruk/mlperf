/*
 * Copyright (c) 2003-2012 MarkLogic Corporation. All rights reserved.
 */
package com.marklogic.xcc.examples;

import java.net.URI;

import com.marklogic.xcc.ContentSource;
import com.marklogic.xcc.ContentSourceFactory;
import com.marklogic.xcc.ModuleInvoke;
import com.marklogic.xcc.Request;
import com.marklogic.xcc.RequestOptions;
import com.marklogic.xcc.ResultSequence;
import com.marklogic.xcc.Session;
import com.marklogic.xcc.exceptions.RequestException;
import com.marklogic.xcc.exceptions.XccConfigException;

/**
 * <p>
 * This is a very simple class that will invoke a named XQuery module on the server and return the
 * result.
 * </p>
 * <p>
 * Click here for the <a href="doc-files/ModuleRunner.java.txt"> source code for this class</a>
 * </p>
 * <p>
 * The main() method looks for two command-line args, a URL for the server (
 * {@link ContentSourceFactory#newContentSource(java.net.URI)}) and theURI of a module to invoke.
 * This URI should be relative to the "Library" path set in the XDBC server configuration for the
 * content server you're connection to (arg #1). This program invokes the named module and then
 * prints the result sequence to stdout, one item per line.
 * </p>
 * <p>
 * The class has methods the could be used to set request options and to obtain the results as an
 * array of Strings or as a real {@link ResultSequence}.
 * </p>
 * <p>
 * If you want to set external variables for a request, you can call {@link #getRequest()} to obtain
 * a reference to the internal {@link Request} object and set the variable values on it before
 * calling {@link #invoke(String)}.
 * </p>
 * <p>
 * ToDo: Accept variable names and values on the command line.
 * </p>
 */
public class ModuleRunner {
    private final Session session;
    private final ModuleInvoke request;
    private RequestOptions options;

    /**
     * Construct an instance that will invoke modules on the server represented by the given URI.
     * Note that the URI will not be validated at this time.
     * 
     * @param serverUri
     *            A URI that specifies a server per (
     *            {@link ContentSourceFactory#newContentSource(java.net.URI)}).
     * @throws XccConfigException
     *             If the URI is not a valid XCC server URL.
     */
    public ModuleRunner(URI serverUri) throws XccConfigException {
        ContentSource cs = ContentSourceFactory.newContentSource(serverUri);

        session = cs.newSession();
        request = session.newModuleInvoke(null);
    }

    /**
     * Invoke the module with the given URI and return the resulting {@link ResultSequence}.
     * 
     * @param moduleUri
     * @return An instance {@link ResultSequence}, possibly with size zero.
     * @throws RequestException
     *             If an unrecoverable error occurs when submitting or evaluating the request.
     */
    public ResultSequence invoke(String moduleUri) throws RequestException {
        request.setModuleUri(moduleUri);
        request.setOptions(options);

        return session.submitRequest(request);
    }

    public String[] invokeToStringArray(String moduleUri) throws RequestException {
        ResultSequence rs = invoke(moduleUri);

        return rs.asStrings();
    }

    public String invokeToSingleString(String moduleUri, String separator) throws RequestException {
        ResultSequence rs = invoke(moduleUri);
        String str = rs.asString(separator);

        rs.close();

        return str;
    }

    /**
     * Returns the {@link com.marklogic.xcc.Request} object used internally to submit requests. This
     * object can be used to set external variables that will be bound to the query when submitted.
     * You should not set your own {@link RequestOptions} object, use
     * {@link #setRequestOptions(com.marklogic.xcc.RequestOptions)} instead.
     * 
     * @return An instance of {@link com.marklogic.xcc.Request}.
     */
    public Request getRequest() {
        return request;
    }

    /**
     * Set (or clear) the {@link RequestOptions} instance to associate with submitted queries.
     * 
     * @param options
     *            An instance of {@link RequestOptions} or null.
     */
    public void setRequestOptions(RequestOptions options) {
        this.options = options;
    }

    // ----------------------------------------------------------

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            usage();
            return;
        }

        URI serverUri = new URI(args[0]);
        String moduleUri = args[1];

        ModuleRunner runner = new ModuleRunner(serverUri);

        String result = runner.invokeToSingleString(moduleUri, System.getProperty("line.separator"));

        System.out.println(result);
    }

    private static void usage() {
        System.err.println("usage: serveruri docuri [-o outfilename]");
    }
}
