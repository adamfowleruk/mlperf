package com.marklogic.adamfowler.performance;

import java.io.File;
import java.io.FileFilter;


public class FolderFilter implements FileFilter {

  public boolean accept(File arg0) {
    return !arg0.isDirectory() && !(arg0.getName().endsWith(".DS_Store"));
  }
  
}