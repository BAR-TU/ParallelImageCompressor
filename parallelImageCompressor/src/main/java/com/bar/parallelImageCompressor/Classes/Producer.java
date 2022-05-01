package com.bar.parallelImageCompressor.Classes;

import com.bar.parallelImageCompressor.Services.Lossy;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

public class Producer extends RecursiveAction {
    BufferedImage[] imgs;
    int threshold;

    public Producer(BufferedImage[] imgs, int threshold) {
        this.imgs = imgs;
        this.threshold = threshold;
    }

    @Override
    protected void compute() {
        if (imgs.length > threshold) {
            ForkJoinTask.invokeAll(createSubtasks());
        } else {
            try {
                processing(imgs);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }

    private void processing(BufferedImage[] imgs) throws IOException, InterruptedException {
        for (int i = 0; i < imgs.length; i++)
        {
            File outputFile = new File("img" + Instant.now().getEpochSecond() + Thread.currentThread().getName() + "-" + Lossy.name + ".jpg");
            ImageIO.write(imgs[i], "jpg", outputFile);
            Lossy.name++;
        }
    }
    private Collection<Producer> createSubtasks() {
        List<Producer> dividedTasks = new ArrayList<>();
        dividedTasks.add(new Producer(Arrays.copyOfRange(imgs, 0, threshold), threshold));
        dividedTasks.add(new Producer(
                Arrays.copyOfRange(imgs, threshold, imgs.length), threshold));
        return dividedTasks;
    }
}
