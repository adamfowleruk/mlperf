#!/bin/sh
. settings.sh
java -XX:MaxPermSize=128m -Xms512m -Xmx1024m -cp ./example:./bin:$LIBRARIES com.marklogic.client.example.batch.BatchManagerExample 

