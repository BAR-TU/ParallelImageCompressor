package com.bar.parallelImageCompressor.Classes;

import java.util.concurrent.BlockingQueue;

public class Producer implements Runnable {
    BlockingQueue<String> blockingQueue = null;

    public Producer(BlockingQueue<String> queue) { this.blockingQueue = queue; }

    @Override
    public void run() {
        for(int i = 0; i < 1000; i++) {
            try {
                this.blockingQueue.put(String.valueOf(i));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
