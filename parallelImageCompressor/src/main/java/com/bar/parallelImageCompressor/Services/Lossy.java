package com.bar.parallelImageCompressor.Services;

import com.bar.parallelImageCompressor.Classes.Producer;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.*;

@Component
public class Lossy {
    public static void Start() throws IOException {
        int coresToUse = Runtime.getRuntime().availableProcessors() - 1;
        ForkJoinPool pool = new ForkJoinPool(coresToUse);

        BufferedImage[] imgs = processIntoChunks();
        int threshold = (int)Math.ceil(Double.parseDouble(String.valueOf(imgs.length)) / Double.parseDouble(String.valueOf(coresToUse)));
        Producer tasks = new Producer(imgs, threshold);

        compressImages(pool, tasks);

        pool.shutdown();
//        BlockingQueue<String> blockingQueue = new ArrayBlockingQueue<>(threadPool.getPoolSize());

//        Producer producer = new Producer(blockingQueue);
//        Consumer consumer1 = new Consumer(blockingQueue);
//        Consumer consumer2 = new Consumer(blockingQueue);

//        Thread producerThread = new Thread(producer);
//        CompletableFuture<String> processImage1 = CompletableFuture.supplyAsync(consumer1);
//        CompletableFuture<String> processImage2 = CompletableFuture.supplyAsync(consumer2);

//        ExecutorService executor = Executors.newWorkStealingPool();
        ////        Future<String> consumerThread1 = executor.submit(consumer1);
////        Future<String> consumerThread2 = executor.submit(consumer2);
//        List<Callable<String>> callables = new ArrayList<>();
//        callables.add(consumer1);
//        callables.add(consumer2);

//        executor.invokeAll(callables)
//                .stream()
//                .map(future -> {
//                    try {
//                        return future.get();
//                    }
//                    catch (Exception e) {
//                        throw new IllegalStateException(e);
//                    }
//                })
//                .forEach(System.out::println);

//        producerThread.start();

    }

    private static void compressImages(ForkJoinPool pool, Producer tasks) {
        Future<Void> res = pool.submit(tasks);
        System.out.println("Processing...");
        try {
            res.get(60, TimeUnit.SECONDS);
        } catch (InterruptedException | TimeoutException e) {
            res.cancel(true);
        } catch (ExecutionException e) {
            if (e.getCause() == null)
                throw new AssertionError();
            throw new AssertionError(e.getCause());
        }
        System.out.println("Sub-images have been created.");
    }

    static BufferedImage[] processIntoChunks() throws IOException {
        System.setProperty("http.agent", "Chrome");

        URL url = new URL("https://www.educative.io/api/edpresso/shot/5120209133764608/image/5075298506244096/test.jpg");
        InputStream is = url.openStream();
        BufferedImage image = ImageIO.read(is);

        int rows = 4;
        int columns = 4;
        while ((image.getHeight() / rows) > 100 || (image.getHeight() / columns) > 100) {
            rows *= 2;
            columns *= 2;
        }

        BufferedImage[] imgs = new BufferedImage[rows * columns];

        return divideToSubImages(image, imgs, rows, columns);
    }

    private static BufferedImage[] divideToSubImages(BufferedImage image, BufferedImage[] imgs, int rows, int columns) {
        int subimage_Width = image.getWidth() / columns;
        int subimage_Height = image.getHeight() / rows;

        int current_img = 0;

        for (int i = 0; i < rows; i++)
        {
            for (int j = 0; j < columns; j++)
            {
                imgs[current_img] = new BufferedImage(subimage_Width, subimage_Height, image.getType());

                Graphics2D img_creator = imgs[current_img].createGraphics();

                int src_first_x = subimage_Width * j;
                int src_first_y = subimage_Height * i;

                int dst_corner_x = subimage_Width * j + subimage_Width;
                int dst_corner_y = subimage_Height * i + subimage_Height;

                img_creator.drawImage(image, 0, 0, subimage_Width, subimage_Height, src_first_x, src_first_y, dst_corner_x, dst_corner_y, null);
                current_img++;
            }
        }

        return imgs;
    }
}
