package com.bar.parallelImageCompressor.Classes;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.RecursiveTask;
import java.util.function.Supplier;

public class Consumer implements Supplier<String> {
    BufferedImage[] imgs;

    public Consumer(BufferedImage[] imgs) {this.imgs = imgs; }

    @Override
    public String get() {
        try {
            processing(imgs);
        } catch (IOException | InterruptedException e) {
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

    private void processing(BufferedImage[] imgs) throws IOException, InterruptedException {
        for (int i = 0; i < imgs.length; i++)
        {
            File outputFile = new File("img" + Thread.currentThread().getName() + "-" + i + ".jpg");
            ImageIO.write(imgs[i], "jpg", outputFile);
            Thread.sleep(1000);
        }
    }
}
