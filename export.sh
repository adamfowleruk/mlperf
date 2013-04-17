#!/bin/sh

. settings.sh

$MLCP export -host $HOST -port 8056 -username admin -password admin -mode local -thread_count 10 -output_file_path ./docs/ -collection_filter prescriptions -output_type document
echo "Done."

