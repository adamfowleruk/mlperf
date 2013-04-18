package com.marklogic.adamfowler.performance;

import com.marklogic.client.example.batch.BatchManager;
import com.marklogic.client.io.Format;
import com.marklogic.client.io.StringHandle;

public class BatchWrapper extends Thread {
  String[] xml = null;
  BatchManager manager = null;
  String uriBase = null;
  int loop = 0;
  int split = 100;
  boolean done = false;
  
  public BatchWrapper(BatchManager manager,String[] xml,String uriBase,int loop) {
    this.xml = xml;
    this.manager = manager;
    this.loop = loop;
    this.uriBase = uriBase;
    this.done = false;
  }
  
  public void run() {
    try {
      BatchManager.BatchRequest request = null;
      //System.out.println("loop=" + loop + " xml length: " + xml.length);
      for (int f = 0;f < xml.length;f++) {
        //System.out.println("loop=" + loop + " f=" + f);
        if (0 == (f % split) || 0 == f ) {
          //System.out.println("Creating split request at f=" + f);
          request = manager.newBatchRequest();
          //System.out.println("Got request object f=" + f);
        }
        //System.out.println("loop=" + loop + " Adding file " + f);
        request.withWrite(uriBase + loop + "/" + f + ".xml",new StringHandle().withFormat(Format.XML).with(xml[f]));
        //System.out.println("Added file f=" + f);
        if (((split - 1) == (f % split)) || (f == (xml.length - 1))) {
          //System.out.println("Commiting split at f=" + f);
          // add content in a single hit
          BatchManager.BatchResponse response = manager.apply(request);
          if (response.getSuccess()) {
            //System.out.println("  SUCCESS " + loop + " f=" + f);
          } else {
            //System.out.println("  FAILURE " + loop + " f=" + f);
          }
        }
        //System.out.println("loop=" + loop + " f is now: " + f);
      }
    } catch (Exception e) {
      e.printStackTrace(System.out);
    }
    this.done = true;
  }
  
  public boolean done() {
    return done;
  }
}
