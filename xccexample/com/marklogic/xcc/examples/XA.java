/*
 * Copyright (c) 2003-2012 MarkLogic Corporation. All rights reserved.
 */
package com.marklogic.xcc.examples;

import java.net.URI;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;

import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.arjuna.recovery.RecoveryModule;
import com.arjuna.ats.jta.recovery.XAResourceRecoveryHelper;
import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;

import com.marklogic.xcc.AdhocQuery;
import com.marklogic.xcc.ContentSource;
import com.marklogic.xcc.ContentSourceFactory;
import com.marklogic.xcc.ResultSequence;
import com.marklogic.xcc.Session;

public class XA {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("usage: xcc://user1:password1@host1:port1/contentbase1 xcc://user2:password2@host2:port2/contentbase2");
            System.err.println();
            System.err.println("Specify two MarkLogic clusters to participate in the distributed transaction.");
            System.err.println("hint: You can try out XA transactions across two databases in the same MarkLogic cluster");
            System.err.println("      by specifying the same host with different contentbases in the two URLs.");
            return;
        }

        final ContentSource cs1 = ContentSourceFactory.newContentSource(new URI(args[0]));
        final ContentSource cs2 = ContentSourceFactory.newContentSource(new URI(args[1]));

        // JBoss specific recovery setup
        RecoveryManager rm = RecoveryManager.manager();
        for(RecoveryModule mod : rm.getModules()) {
            if(mod instanceof XARecoveryModule) {
                ((XARecoveryModule)mod).addXAResourceRecoveryHelper(
                    new XAResourceRecoveryHelper() {
                        public boolean initialise(String p) throws Exception { return true; }
                        public XAResource[] getXAResources() throws Exception {
                            return new XAResource[] {
                                cs1.newSession().getXAResource(),
                                cs2.newSession().getXAResource()
                            };
                        }
                    });
                break;
            }
        }

        // Force recovery
        rm.scan();

        TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

        Session session1 = cs1.newSession();
        Session session2 = cs2.newSession();
        try {
            // Begin the distributed transaction
            tm.begin();

            // Enlist the MarkLogic XAResource instances
            tm.getTransaction().enlistResource(session1.getXAResource());
            tm.getTransaction().enlistResource(session2.getXAResource());

            // Perform work on the Session instances
            AdhocQuery req = session1.newAdhocQuery(
                "xquery version '1.0-ml';\n" +
                "xdmp:host-status(xdmp:host())//*:transaction\n" +
                "  [*:transaction-id eq xdmp:transaction()],\n" +
                "xdmp:document-insert('query1', <query1/>)"
                );
            ResultSequence rs = session1.submitRequest(req);
            System.out.println(rs.asString(System.getProperty("line.separator")));
            rs.close();

            req = session2.newAdhocQuery(
                "xquery version '1.0-ml';\n" +
                "xdmp:host-status(xdmp:host())//*:transaction\n" +
                "  [*:transaction-id eq xdmp:transaction()],\n" +
                "xdmp:document-insert('query2', <query2/>)"
                );
            rs = session2.submitRequest(req);
            System.out.println(rs.asString(System.getProperty("line.separator")));
            rs.close();

            // Commit the distributed transaction
            tm.commit();
        } catch(Exception e) {
            e.printStackTrace();
            if(tm.getTransaction() != null) tm.rollback();
        } finally {
            session1.close();
            session2.close();
        }
        // JBoss specific recovery termination
        rm.terminate();
    }
}
