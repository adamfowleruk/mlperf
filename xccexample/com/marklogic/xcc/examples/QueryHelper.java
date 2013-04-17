/*
 * Copyright (c) 2003-2012 MarkLogic Corporation. All rights reserved.
 */
package com.marklogic.xcc.examples;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.NumberFormat;

/**
 * <p>
 * This is a staitc helper class with some common code routines used by the examples in this
 * package.
 * </p>
 * <p>
 * Click here for the <a href="doc-files/QueryHelper.java.txt"> source code for this class</a>
 * </p>
 */
public class QueryHelper {
    static final int MILLIS = 1000;
    static final int SECONDS = MILLIS;
    static final int MINUTES = 60 * SECONDS;
    static final int HOURS = 60 * MINUTES;

    private QueryHelper() {
        // this is a helper class, cannot be instantiated
    }

    static long bytesPerSecond(long totalByteCount, long elapsed) {
        return (long)(((double)totalByteCount / (double)elapsed) * MILLIS);
    }

    static String formatInteger(long n) {
        NumberFormat formatter = NumberFormat.getIntegerInstance();

        return formatter.format(n);
    }

    static String formatTime(long millis) {
        StringBuffer sb = new StringBuffer();
        long n = millis / HOURS;

        if (n != 0) {
            sb.append(n).append("h");
        }

        n = (millis % HOURS) / MINUTES;

        if ((n) != 0) {
            sb.append(n).append("m");
        }

        sb.append((millis % MINUTES) / SECONDS).append(".");

        n = millis % MILLIS;

        if (n < 100)
            sb.append("0");
        if (n < 10)
            sb.append("0");

        sb.append(n).append("s");

        return (sb.toString());
    }

    static String loadQueryFromFile(String queryFile) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(queryFile));
        StringBuffer sb = new StringBuffer();
        String line;

        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }

        reader.close();

        return (sb.toString());
    }
}
