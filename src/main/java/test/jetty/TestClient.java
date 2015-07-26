package test.jetty;

import java.util.concurrent.Semaphore;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.eclipse.jetty.http2.client.http.HttpClientTransportOverHTTP2;

public class TestClient {
    static boolean failed = false;

    public static void main(String[] args) throws Exception {
        boolean http2 = Boolean.parseBoolean(System.getProperty("http2", "false"));
        String targetUri = System.getProperty("targetUri", "http://localhost:8080");

        HttpClient client;
        if (http2) {
            System.out.println("Client using HTTP/2 protocol");
            HTTP2Client http2Client = new HTTP2Client();
            HttpClientTransportOverHTTP2 clientTransport = new HttpClientTransportOverHTTP2(http2Client);
            client = new HttpClient(clientTransport, null);
        } else {
            System.out.println("Client using HTTP/1.1 protocol");
            client = new HttpClient();
        }

        client.start();

        Metrics metrics = new Metrics();
        metrics.start();

        // Limits the number of concurrent requests
        Semaphore throttle = new Semaphore(200);

        while (!failed) {
            throttle.acquire();
            long start = System.nanoTime();
            metrics.incrementActiveRequests();
            client.newRequest(targetUri)
                    .send(new BufferingResponseListener() {

                        @Override
                        public void onComplete(Result result) {
                            try {
                                if (result.isFailed()) {
                                    throw result.getFailure();
                                }

                                String content = getContentAsString();
                                assert (content.equals("<h1>Hello World</h1>"));

                                long end = System.nanoTime();
                                metrics.recordLatency(end - start);
                                metrics.decrementActiveRequests();

                                throttle.release();
                            } catch (Throwable e) {
                                e.printStackTrace();
                            }
                        }
                    });
        }

    }
}
