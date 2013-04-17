package com.marklogic.adamfowler.performance;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class ReplaceXMLChars {

  /**
   * @param args
   */
  public static void main(String[] args) {
    try {
      File dir = new File(args[0]);
      File[] files = dir.listFiles(new FolderFilter());
      String pattern = "[^" 
          + "\u0001-\uD7FF" 
          + "\uE000-\uFFFD"
          + "\ud800\udc00-\udbff\udfff" 
          + "]+";
      
      String entire;
      StringBuffer sb;
      BufferedReader r;
      FileWriter writer;
      char[] buffer = new char[1024];
      int len;
      for (int i = 0;i < files.length;i++) {
        System.out.println("Processing " + files[i].getName());
        sb = new StringBuffer();
        r = new BufferedReader(new FileReader(files[i]));
        while (-1 != (len = r.read(buffer))) {
          sb.append(buffer, 0, len);
        }
        entire = sb.toString();

        entire = entire.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>","");
        entire = stripNonValidXMLCharacters(entire);
        //entire = entire.replace("&","&amp;");
        //entire = entire.replace("%","");

        writer = new FileWriter(files[i]);
        writer.write(entire);
        writer.flush();
        writer.close();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static String stripNonValidXMLCharacters(String in) {
    StringBuffer out = new StringBuffer(); // Used to hold the output.
    char current; // Used to reference the current character.

    if (in == null || ("".equals(in))) return ""; // vacancy test.
    for (int i = 0; i < in.length(); i++) {
        current = in.charAt(i); // NOTE: No IndexOutOfBoundsException caught here; it should not happen.
        if ((current == 0x9) ||
            (current == 0xA) ||
            (current == 0xD) ||
            ((current >= 0x20) && (current <= 0xD7FF)) ||
            ((current >= 0xE000) && (current <= 0xFFFD)) ||
            ((current >= 0x10000) && (current <= 0x10FFFF)))
            out.append(current);
    }
    return out.toString();
}  
}
