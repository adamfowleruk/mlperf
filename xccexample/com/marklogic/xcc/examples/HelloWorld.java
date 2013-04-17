/*
 * Copyright (c) 2003-2012 MarkLogic Corporation. All rights reserved.
 */
package com.marklogic.xcc.examples;

import java.net.URI;

import com.marklogic.xcc.ContentSource;
import com.marklogic.xcc.ContentSourceFactory;
import com.marklogic.xcc.Request;
import com.marklogic.xcc.ResultSequence;
import com.marklogic.xcc.Session;

/**
 * <p>
 * The obligatory Hello World example.
 * </p>
 * <p>
 * Click here for the <a href="doc-files/HelloWorld.java.txt"> source code for this class</a>.
 * </p>
 */
public class HelloWorld {
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("usage: xcc://user:password@host:port/contentbase");
            return;
        }

        // Create a URI object from the supplied argument
        URI uri = new URI(args[0]);

        // Obtain a ContentSource object for the server at the URI.
        // ContentSources can create many Sessions.  ContentSources are
        // tightly bound to a host/port, but user, password and contentbase,
        // if provided, are defaults and may be overridden for each Session.
        ContentSource contentSource = ContentSourceFactory.newContentSource(uri);

        // Create a Session, which encapsulates host, port, user and
        // password, and an optional contentbase id.  If Contentbase is
        // not specified, the default configured on the server for the
        // host/port will be used.
        // Sessions represent a dialog with a contentbase and may hold
        // state related to that dialog.  A Session is also the factory
        // for Request objects.  Sessions are lightweight and relatively
        // cheap to create -- don't bother pooling them, they do not
        // represent connections.
        Session session = contentSource.newSession();

        // Create an ad-hoc Request, which contains XQuery code to be
        // evaluated.  Requests are mutable and may be re-used repeatedly
        // and in any order.
        Request request = session.newAdhocQuery("\"Hello World\"");

        // Submit the Request and return a new ResultSequence object.
        // By default, the result will be cached and need not be closed.
        ResultSequence rs = session.submitRequest(request);

        // Print the String representation of the ResultSequence.
        // In this case, there is only one item in the sequence.
        // Not that "asString()" is different than "toString()".
        // The asString() method returns the value of the object
        // after converting it to String form.  But toString()
        // returns a descriptive String that summarizes the state
        // of an object.
        System.out.println(rs.asString());

        // All done
        session.close();
    }
}
