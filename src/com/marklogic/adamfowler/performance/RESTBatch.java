package com.marklogic.adamfowler.performance;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.client.DatabaseClientFactory.Authentication;
import com.marklogic.client.example.batch.BatchManager;
import com.marklogic.client.io.Format;
import com.marklogic.client.io.StringHandle;

public class RESTBatch {
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

      DatabaseClient[] clients = new DatabaseClient[10];
      BatchManager[] managers = new BatchManager[10];
      for (int i = 0;i < 10;i++) {
        clients[i] = DatabaseClientFactory.newClient(args[0], Integer.parseInt(args[1]), "admin", "admin", Authentication.DIGEST);
        managers[i] = new BatchManager(clients[i]); // both parallelised with 10 connections - should use multiple cores on server side
      }
      // create the batch manager
      //BatchManager batchMgr = new BatchManager(client);
      
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
      
      System.out.println("Using Erik's REST Batch Extension to add " + files.length + " files " + args[3] + " times.");

      String uriBase = "/performance/restbatch/";
      int loops = Integer.parseInt(args[3]);
      int choice;
      BatchManager.BatchRequest request;
      for (int i = 0;i < loops;i++) {
        System.out.println("Entering loop " + i + " of " + args[3]);
        // build URIs

        choice = (int)Math.floor(Math.random() * 10);
        request = managers[choice].newBatchRequest();
        for (int f = 0;f < files.length;f++) {
          //System.out.println("Adding file " + files[f]);
          request.withWrite(uriBase + i + "/" + f + ".xml",new StringHandle().withFormat(Format.XML).with(xml[f]));
        }
        
        // add content in a single hit
        BatchManager.BatchResponse response = managers[choice].apply(request);
        if (response.getSuccess()) {
          System.out.println("  SUCCESS");
        } else {
          System.out.println("  FAILURE");
        }

      }

      client.release();
      
      System.out.println("Done.");
      System.exit(0);
    } catch (Exception e) {
      e.printStackTrace(System.out);
      System.exit(-1);
    }
  }

}
