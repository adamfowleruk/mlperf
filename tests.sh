#!/bin/sh

. settings.sh

echo "Performing XCC tests"
date
java -cp ./bin:$LIBRARIES com.marklogic.adamfowler.performance.XCCBatch xdbc://admin:admin@$HOST:$XDBCPORT/ $DIR/0001 $LOOPS
date
echo "Completed XCC tests"

sleep 240

echo "Performing REST individual tests"
date
java -XX:MaxPermSize=128m -Xms512m -Xmx1024m -cp ./bin:$LIBRARIES com.marklogic.adamfowler.performance.RESTThreaded $HOST $RESTPORT $DIR/0001 $LOOPS
date
echo "Completed REST INDIVIDUAL tests"

sleep 240

echo "Performing REST Batch tests"
date
java -Xms1024m -Xmx3072m -cp ./bin:$LIBRARIES com.marklogic.adamfowler.performance.RESTBatch $HOST $RESTPORT $DIR/0001 $LOOPS
date
echo "Completed REST Batch tests"

sleep 240

echo "Performing MLCP batch test"
date
./test-mlcp.sh
date
echo "Completed MLCP batch test"

exit 0
