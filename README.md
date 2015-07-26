Jetty Tests
============

Jetty was one of the first Java HTTP servers that I could find to have support for HTTP/2 protocol. I'm anxious to see how much a single multiplexed TCP socket can improve the performance of RESTful APIs so I created a test application to compare HTTP/1.1 and HTTP/2.

This project comes in two parts:
* Server
* Client

The server is fully embedded and contains a single Hello World handler. It's designed to be a "simple as possible" implementation with only as much configuration as is needed to get both HTTP/1.1 and HTTP/2 working.

To run the server using gradle, run:

```
./gradlew server
```

The client is also designed to have a minimum configuration that would allow either HTTP/1.1 or HTTP/2 operation and still be readable (ie. no configuration file magic)

The client runs a continuous request loop, sending up to 200 concurrent requests and measuring the response latency for each request. The results are aggregated per second and displayed on standard out.

To run the client in HTTP/1.1 mode, run:
```
./gradlew client -DtargetUri="http://your-server-ip:8080"
```

For HTTP/2 mode, run:
```
./gradlew client -DtargetUri="http://your-server-ip:8080" -Dhttp2=true
```
