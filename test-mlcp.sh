#!/bin/sh

. settings.sh
$MLCP import -host $HOST -port $XDBCPORT -username admin -password admin -mode local -thread_count 10 -batch_size 500 -input_file_path ./docs/ -input_file_type documents -fastload true -output_uri_prefix /mlcp/

exit 0
