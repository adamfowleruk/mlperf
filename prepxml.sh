#!/bin/sh

java -XX:MaxPermSize=128m -Xms512m -Xmx1024m -cp ./bin:$LIBRARIES com.marklogic.adamfowler.performance.ReplaceXMLChars ./docs

