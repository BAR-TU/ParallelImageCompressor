package com.bar.parallelImageCompressor.Classes;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.RecursiveTask;
import java.util.function.Supplier;

public class Consumer extends RecursiveTask<String> {
    BlockingQueue<String> blockingQueue = null;

    public Consumer(BlockingQueue<String> queue) {this.blockingQueue = queue; }

    @Override
    protected String compute() {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
//        try {
//            Instant start = Instant.now();
//            String block = this.blockingQueue.take();
//            //
//            Instant end = Instant.now();
//            Duration timeElapsed = Duration.between(start, end);
//            if (timeElapsed.toSeconds() >= 1) {
//                return  block;
//            }
//            System.out.println(block);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        return "0";
    }
}
