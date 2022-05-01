package com.bar.parallelImageCompressor.Classes;

import com.bar.parallelImageCompressor.Services.Lossy;

import java.awt.*;
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
    SubImage[] imgs;
    int threshold;

    public Producer(SubImage[] imgs, int threshold) {
        this.imgs = imgs;
        this.threshold = threshold;
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
            Graphics2D writeToImage = Lossy.compressedImage.createGraphics();
            writeToImage.drawImage(imgs[i].getImage(), imgs[i].getSrc_first_x(), imgs[i].getSrc_first_y(),
                    imgs[i].getSrc_second_x(), imgs[i].getSrc_second_y(), 0, 0, imgs[i].getImage().getWidth(),
                    imgs[i].getImage().getHeight(), null);
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
