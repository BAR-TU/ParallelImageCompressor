package com.bar.parallelImageCompressor.Classes;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

public class Producer extends RecursiveAction {
    SubImage[] imgs;
    int threshold;

    BufferedImage compressedImage;

    public Producer(SubImage[] imgs, int threshold, BufferedImage compressedImage) {
        this.imgs = imgs;
        this.threshold = threshold;
        this.compressedImage = compressedImage;
    }

    @Override
    protected void compute() {
        if (imgs.length > threshold) {
            ForkJoinTask.invokeAll(createSubtasks());
        } else {
            try {
                synchronized (this) {
                    processing(imgs);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }

    private void processing(SubImage[] imgs) throws IOException, InterruptedException {
        for (int i = 0; i < imgs.length; i++)
        {

            Graphics2D writeToImage = compressedImage.createGraphics();
            writeToImage.drawImage(imgs[i].getImage(), imgs[i].getSrc_first_x(), imgs[i].getSrc_first_y(),
                    imgs[i].getSrc_second_x(), imgs[i].getSrc_second_y(), 0, 0, imgs[i].getImage().getWidth(),
                    imgs[i].getImage().getHeight(), null);
//            Thread.sleep(1000);
        }
    }
    private Collection<Producer> createSubtasks() {
        List<Producer> dividedTasks = new ArrayList<>();
        dividedTasks.add(new Producer(Arrays.copyOfRange(imgs, 0, threshold), threshold, compressedImage));
        dividedTasks.add(new Producer(
                Arrays.copyOfRange(imgs, threshold, imgs.length), threshold, compressedImage));
        return dividedTasks;
    }
}
