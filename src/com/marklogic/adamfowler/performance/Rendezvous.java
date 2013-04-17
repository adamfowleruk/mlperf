package com.marklogic.adamfowler.performance;

public class Rendezvous {
  int count = 0;
  int total = 100;
  
  public Rendezvous(int total) {
    this.total = total;
  }
  
  public synchronized void increment() {
    count++;
  }
  
  public synchronized boolean done() {
    return (count == total);
  }
  
}
