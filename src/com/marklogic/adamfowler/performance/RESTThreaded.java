package com.marklogic.adamfowler.performance;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.client.DatabaseClientFactory.Authentication;
import com.marklogic.client.document.XMLDocumentManager;

public class RESTThreaded {
  /**
   * 0 - hostname
   * 1 - port
   * 2 - folderPath
   * 3 - times repeat
   * 
   * @param args
   */
  public static void main(String args[]) {
    for (int i = 0;i < args.length;i++) {
      System.out.println("ARG " + i + ":" + args[i].toString());
    }
    try {
      
      // load all files in a directory
      File dir = new File(args[2]);
      File files[] = dir.listFiles(new FolderFilter());
      
      // hold file content in memory between adds (faster, less disc io)
      
      DatabaseClient client = DatabaseClientFactory.newClient(args[0], Integer.parseInt(args[1]), "admin", "admin", Authentication.DIGEST);
      /*
      DatabaseClient[] clients = new DatabaseClient[10];
      XMLDocumentManager[] managers = new XMLDocumentManager[10];
      for (int i = 0;i < 10;i++) {
        clients[i] = DatabaseClientFactory.newClient(args[0], Integer.parseInt(args[1]), "admin", "admin", Authentication.DIGEST);
        managers[i] = clients[i].newXMLDocumentManager(); // both parallelised with 10 connections - should use multiple cores on server side
      }
      */
      XMLDocumentManager manager = client.newXMLDocumentManager();
      
      String xml[] = new String[files.length];
      StringBuffer sb;
      char[] buffer = new char[1024];
      int len;
      BufferedReader r;
      for (int i = 0;i < files.length;i++) {
        sb = new StringBuffer();
        r = new BufferedReader(new FileReader(files[i]));
        while (-1 != (len = r.read(buffer))) {
          sb.append(buffer, 0, len);
        }
        xml[i] = sb.toString();
        r.close();
      }
      
      //XMLDocumentManager docMgr = client.newXMLDocumentManager();
      
      System.out.println("Using REST /v1/documents in parallel to add " + files.length + " files " + args[3] + " times.");

      String uriBase = "/performance/restfast/";
      int loops = Integer.parseInt(args[3]);
      Rendezvous rv;
      //int choice;
      UploadWrapper wrapper;
      for (int i = 0;i < loops;i++) {
        rv = new Rendezvous(files.length);
        System.out.println("Entering loop " + i + " of " + args[3]);
        // build URIs
        for (int f = 0;f < files.length;f++) {
          //choice = (int)Math.floor(Math.random() * 10.0);
          //System.out.println("Using client " + choice);
          //wrapper = new UploadWrapper(clients[choice],managers[choice],uriBase + i + "/" + f,xml[f],rv); // random client connection
          wrapper = new UploadWrapper(client,manager,uriBase + i + "/" + f,xml[f],rv); // random client connection
          wrapper.start(); // add content individually, in parallel on client via threads
        }
        
        // wait for rendezvous
        while (!rv.done()) {
          Thread.sleep(500);
        }
      }
      
      //client.release(); // let client die when JVM exits as we're not monitoring thread state
      
      System.out.println("Done.");
      System.exit(0);
    } catch (Exception e) {
      e.printStackTrace(System.out);
      System.exit(-1);
    }
  }

}
