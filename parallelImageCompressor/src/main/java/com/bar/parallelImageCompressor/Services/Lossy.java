package com.bar.parallelImageCompressor.Services;

import com.bar.parallelImageCompressor.Classes.Consumer;
import com.bar.parallelImageCompressor.Classes.Producer;
import org.springframework.stereotype.Component;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@Component
public class Lossy {
    public static void Start() {
//        List<Integer> listOfNumbers = Arrays.asList(1, 2, 3, 4);
//        listOfNumbers.parallelStream().forEach(number ->
//                System.out.println(number + " " + Thread.currentThread().getName())
//        );

        BlockingQueue<String> blockingQueue = new ArrayBlockingQueue<>(5);

        Producer producer = new Producer(blockingQueue);
        Consumer consumer1 = new Consumer(blockingQueue);
        Consumer consumer2 = new Consumer(blockingQueue);

        Thread producerThread = new Thread(producer);
        Thread consumerThread1 = new Thread(consumer1);
        Thread consumerThread2 = new Thread(consumer2);
        producerThread.start();
        consumerThread1.start();
        consumerThread2.start();

    }
}
