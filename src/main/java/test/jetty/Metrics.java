package test.jetty;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Metrics {

    AtomicInteger activeRequestCounter = new AtomicInteger();

    Object latencySync = new Object();
    int latencyStorageCapacity = 10000;
    long[] latencyStorage = new long[10000];
    int latencyCounter = 0;

    AtomicLong lastUpdateTimer = new AtomicLong();
    Timer displayTimer = new Timer();
    TimerTask timerTask = new TimerTask() {

        @Override
        public void run() {
            displayUpdate();
        }
    };

    public void start() {
        long now = System.nanoTime();
        lastUpdateTimer.set(now);
        displayTimer.schedule(timerTask, 1000, 1000);
    }

    public void stop() {
        displayTimer.cancel();
    }

    public void incrementActiveRequests() {
        activeRequestCounter.getAndIncrement();
    }

    public void decrementActiveRequests() {
        activeRequestCounter.getAndDecrement();
    }

    public void recordLatency(long latencyInNanos) {
        synchronized (latencySync) {
            if (latencyCounter >= latencyStorageCapacity) {
                latencyStorageCapacity *= 2;
                latencyStorage = Arrays.copyOf(latencyStorage, latencyStorageCapacity);
            }
            latencyStorage[latencyCounter] = latencyInNanos;
            latencyCounter++;
        }
    }

    private void displayUpdate() {
        long now = System.nanoTime();
        long lastUpdateTime = lastUpdateTimer.getAndSet(now);
        long activeRequests = activeRequestCounter.get();

        long[] latencies;
        int latencyCount;
        synchronized (latencySync) {
            latencyCount = latencyCounter;
            latencyCounter = 0;

            latencies = latencyStorage;
            latencyStorage = new long[latencyStorageCapacity];
        }
        Arrays.sort(latencies, 0, latencyCount);

        double timeInSeconds = (double) (now - lastUpdateTime) * 1e-9;

        double latencyP50 = latencyCount == 0 ? 0 : (double) latencies[latencyCount / 2] * 1e-6;
        double latencyP90 = latencyCount == 0 ? 0 : (double) latencies[latencyCount * 9 / 10] * 1e-6;
        double responseRate = timeInSeconds == 0 ? 0 : (double) latencyCount / timeInSeconds;
        System.out
                .printf(
                        "Active Requests: %d, Response rate: %.0f/s, Latency: P50 %.3fms P90 %.3fms\n",
                        activeRequests,
                        responseRate,
                        latencyP50,
                        latencyP90);
    }
}
