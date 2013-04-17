package com.marklogic.adamfowler.performance;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.document.XMLDocumentManager;
import com.marklogic.client.io.Format;
import com.marklogic.client.io.StringHandle;

public class UploadWrapper extends Thread {
  DatabaseClient client;
  XMLDocumentManager docMgr;
  String uri;
  String xmlContent;
  Rendezvous rv;
  
  public UploadWrapper(DatabaseClient client,XMLDocumentManager docMgr,String uri,String xmlContent,Rendezvous rv) {
    this.client = client;
    this.docMgr = docMgr;
    this.uri = uri;
    this.xmlContent = xmlContent;
    this.rv = rv;
  }
  
  public void run() {
    // do request in separate thread
    docMgr.write(uri,new StringHandle().withFormat(Format.XML).with(xmlContent));
    rv.increment();
  }
}
