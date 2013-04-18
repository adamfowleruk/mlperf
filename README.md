
## README

Here is how to install the tests.

1. Edit settings.sh to reflect your MarkLogic server configuration
2. Install Batch extension in to your REST server. Do this by executing ./install-batch.sh once only
3. Copy the docs/0001 folder 1000 times (for 500 000 documents to be ingested) or however many you need. Replace the content if needs be. The Java samples use just the contents of the 0001 folder LOOPS times(from settings.sh), whereas MLCP requires there be LOOPS number of folders. They must all be subfolders of docs/
4. Execute ./doit.sh - this will run tests.sh and log all output to test.log

If you have XML with potentially faulty characters or sequences, run prepxml.sh passing in the appropriate folder. You only need do this once.

Any questions, please email me at adam.fowler@marklogic.com
 
