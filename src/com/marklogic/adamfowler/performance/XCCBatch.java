package com.marklogic.adamfowler.performance;

import java.io.File;
import java.net.URI;

import com.marklogic.xcc.Content;
import com.marklogic.xcc.ContentCreateOptions;
import com.marklogic.xcc.ContentFactory;
import com.marklogic.xcc.ContentSource;
import com.marklogic.xcc.ContentSourceFactory;
import com.marklogic.xcc.Session;

public class XCCBatch {
  /**
   * 0 - XCC Server URI
   * 1 - folderPath
   * 2 - times repeat
   * 
   * @param args
   */
  public static void main(String args[]) {
    for (int i = 0;i < args.length;i++) {
      System.out.println("ARG " + i + ":" + args[i].toString());
    }
    try {
      URI serverUri = new URI(args[0]);
      
      // load all files in a directory
      File dir = new File(args[1]);
      File files[] = dir.listFiles(new FolderFilter());
      
      // TODO hold file content in memory between adds (faster, less disc io)

      ContentCreateOptions options = null;
      ContentSource cs = ContentSourceFactory.newContentSource(serverUri);
      Session session = cs.newSession();

      Content[] contents = new Content[files.length];
      
      System.out.println("Using XCC to add " + files.length + " files " + args[2] + " times.");

      String uriBase = "/performance/xcc/";
      String uris[] = new String[files.length];
      int loops = Integer.parseInt(args[2]);
      for (int i = 0;i < loops;i++) {
        System.out.println("Entering loop " + i + " of " + args[2]);
        // build URIs
        for (int f = 0;f < uris.length;f++) {
          uris[f] = uriBase + i + "/" + f;
          contents[f] = ContentFactory.newContent(uris[f], files[f], options);
        }
        
        // add content in a single hit
        session.insertContent(contents);
      }

      System.out.println("Done.");
      System.exit(0);
    } catch (Exception e) {
      e.printStackTrace(System.out);
      System.exit(-1);
    }
  }
  
}
