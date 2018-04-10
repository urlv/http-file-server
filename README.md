# http-file-server
Java asynchronous http file server

Efficient. Secured. Robust. Asynchronous. Single Threaded. Easy to use.

### Example
Running a server on port `8823` And makes the files in `C:\\data\\images` folder accessible
```java
HttpFileServer server = new HttpFileServer(8823, "C:\\data\\images");
server.start(); // it's async, so it running the server and continues to the next line
System.out.println("server is up");
```

Now open a browser and go to `http://127.0.0.1:8823/yourface.jpg` (or any other file found there including subfolders)<br>
BOOM! The requested file is shown to you.

use `server.close()` to close the server.


#### Good Luck ;
