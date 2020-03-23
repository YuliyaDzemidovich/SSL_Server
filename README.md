# SSL Server
SSL client-server model based on javax.net.ssl package

Task:
Create server-client model. 
Client sends request to server to search files with given extensions from the given directory.
As a response server sends a list of files found.
Provide the possibility of multiple clients.

Solution:
1. Server-client socket model, HTTPSClient example as well.
2. For certificate creation Java KeyStore is used.
3. Protocol TSLv1 is used, can be changed to any SSL/TSL version within one line.
4. Server logging is enabled using Log4j library.
