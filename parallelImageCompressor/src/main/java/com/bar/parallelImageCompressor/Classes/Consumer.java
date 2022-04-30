package com.bar.parallelImageCompressor.Classes;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.BlockingQueue;

public class Consumer implements Runnable {
    BlockingQueue<String> blockingQueue = null;

    public Consumer(BlockingQueue<String> queue) {this.blockingQueue = queue; }

    @Override
    public void run() {
        while(true) {
            try {
                Instant start = Instant.now();
                String block = this.blockingQueue.take();
                //
                Instant end = Instant.now();
                Duration timeElapsed = Duration.between(start, end);
                if (timeElapsed.toSeconds() >= 1) {
                    break;
                }
                System.out.println(block);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
