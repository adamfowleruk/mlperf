/*
 * Copyright (c) 2003-2012 MarkLogic Corporation. All rights reserved.
 */
package com.marklogic.xcc.examples;

import java.io.FileInputStream;
import java.net.URI;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import com.marklogic.xcc.ContentSource;
import com.marklogic.xcc.ContentSourceFactory;
import com.marklogic.xcc.Request;
import com.marklogic.xcc.ResultSequence;
import com.marklogic.xcc.SecurityOptions;
import com.marklogic.xcc.Session;

/**
 * <p>
 * A version of the Hello World example that connects to a server using SSL/TLS.
 * </p>
 * <p>
 * This example can load a specified key store of trusted signing authorities, use the Java default 
 * key store, or use a stub that accepts any server certificate.  It can also load client certificates 
 * from a specified key store, or connect without a certificate. 
 * </p>
 * <p>
 * In part to demonstrate the available configuration options, connections are forced to use TLS with 
 * strong ciphers using 256 bit keys, as might be desirable with highly sensitive data being transmitted
 * over public networks.
 * </p>
 * <p>
 * Click here for the <a href="doc-files/HelloSecureWorld.java.txt"> source code for this class</a>.
 * </p>
 */
public class HelloSecureWorld {
    public static void main(String[] args) throws Exception {
        if ((args.length < 2) || (args.length > 4)) {
            System.err.println("usage: xccs://user:password@host:port/contentbase trustJKS|DEFAULT|ANY [clientJKS [passphrase]]");
            System.err.println();
            System.err.println("trustJKS may be one of:");
            System.err.println("    path    to use Java Key Store containing trusted signing authorities.");
            System.err.println("    DEFAULT to use Java default cacerts file.");
            System.err.println("    ANY     to accept any server certificate.");
            System.err.println();
            System.err.println("clientJKS is optional path to Java Key Store containing client certificates.");
            System.err.println();
            System.err.println("passphrase is optional passphrase for client key store.");
            return;
        }

        // Create a URI object from the supplied argument
        URI uri = new URI(args[0]);

        String trustJKS = args[1];

        TrustManager[] trustManagers;

        if (trustJKS.equals("DEFAULT")) {
            // Use Java defaults.
            trustManagers = null;
        } else if (trustJKS.equals("ANY")) {
            // Trust anyone.
            trustManagers = new TrustManager[] { new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] x509Certificates, String s)
                        throws CertificateException {
                    // nothing to do
                }

                public void checkServerTrusted(X509Certificate[] x509Certificates, String s)
                        throws CertificateException {
                    // nothing to do
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            } };
        } else {
            // Load key store with trusted signing authorities.
            KeyStore trustedKeyStore = KeyStore.getInstance("JKS");
            trustedKeyStore.load(new FileInputStream(args[1]), null);

            // Build trust manager to validate server certificates using the specified key store.
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            trustManagerFactory.init(trustedKeyStore);
            trustManagers = trustManagerFactory.getTrustManagers();
        }

        String clientJKS = (args.length > 2) ? args[2] : null;
        
        String passphrase = (args.length > 3) ? args[3] : "";

        KeyManager[] keyManagers;
        
        if (clientJKS == null) {
            // Don't use client certificate.
            keyManagers = null;
        }
        else {
            // Load key store with client certificates.
            KeyStore clientKeyStore = KeyStore.getInstance("JKS");
            clientKeyStore.load(new FileInputStream(clientJKS), null);

            // Get key manager to provide client credentials.
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(clientKeyStore, passphrase.toCharArray());
            keyManagers = keyManagerFactory.getKeyManagers();
        }

        // Get an SSLContext that supports the desired protocol; SSLv3 or TLSv1.
        SSLContext sslContext = SSLContext.getInstance("TLSv1");

        // Initialize the SSL context with key and trust managers.
        sslContext.init(keyManagers, trustManagers, null);

        // Create a security options object for use by the secure content source.
        SecurityOptions securityOptions = new SecurityOptions(sslContext);

        // Limit acceptable protocols; SSLv3 and/or TLSv1 (optional)
        securityOptions.setEnabledProtocols(new String[] { "TLSv1" });

        // Limit acceptable cipher suites. (optional)
        // See ciphers man page or TLS 1.0 / SSL 3.0 specifications.
        securityOptions.setEnabledCipherSuites(new String[] { "TLS_DHE_RSA_WITH_AES_256_CBC_SHA",
                "TLS_DHE_DSS_WITH_AES_256_CBC_SHA", "TLS_RSA_WITH_AES_256_CBC_SHA" });

        // Obtain a ContentSource object for the server at the URI.
        // ContentSources can create many Sessions.  ContentSources are
        // tightly bound to a host/port, but user, password and contentbase,
        // if provided, are defaults and may be overridden for each Session.
        ContentSource contentSource = ContentSourceFactory.newContentSource(uri, securityOptions);

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
