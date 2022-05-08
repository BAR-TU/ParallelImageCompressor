package com.bar.parallelImageCompressor.Classes;

import com.bar.parallelImageCompressor.Classes.HuffmanCoding.HuffmanCoding;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.concurrent.*;

public class Producer extends RecursiveAction {
    SubImage[] imgs;
    int threshold;
    String flag;

    BufferedImage compressedImage;

    public Producer(SubImage[] imgs, int threshold, BufferedImage compressedImage, String flag) {
        this.imgs = imgs;
        this.threshold = threshold;
        this.compressedImage = compressedImage;
        this.flag = flag;
    }

    @Override
    protected void compute() {
        if (imgs.length > threshold) {
            ForkJoinTask.invokeAll(createSubtasks());
        } else {
            try {
                processing(imgs);
            } catch (IOException | InterruptedException | ExecutionException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }

    private void processing(SubImage[] imgs) throws IOException, InterruptedException, ExecutionException {


        for (int i = 0; i < imgs.length; i++)
        {
            if("lossy".equals(flag)) {
                ExecutorService executorService = Executors.newFixedThreadPool(imgs.length);
                Future<BufferedImage> future = executorService.submit(new LossyCompression(imgs[i].getImage()));
                BufferedImage compressImg = future.get();
                executorService.shutdown();
                synchronized (this) {
                    saveSubImage(compressImg, imgs[i]);
                }

            } else if("lossless".equals(flag)) {
                ExecutorService executorService = Executors.newFixedThreadPool(imgs.length);
                Future<BufferedImage> future = executorService.submit(new HuffmanCoding(imgs[i].getImage()));
                BufferedImage compressImg = future.get();
                executorService.shutdown();
                synchronized (this) {
                    saveSubImage(compressImg, imgs[i]);
                }
            }
//            Thread.sleep(1000);
        }

    }

    private void saveSubImage(BufferedImage compressImg, SubImage preCompressedImage) {
        Graphics2D writeToImage = compressedImage.createGraphics();
        writeToImage.drawImage(compressImg, preCompressedImage.getSrc_first_x(), preCompressedImage.getSrc_first_y(),
                preCompressedImage.getSrc_second_x(), preCompressedImage.getSrc_second_y(), 0, 0,
                preCompressedImage.getImage().getWidth(), preCompressedImage.getImage().getHeight(), null);
        writeToImage.dispose();
    }
    private Collection<Producer> createSubtasks() {
        List<Producer> dividedTasks = new ArrayList<>();
        dividedTasks.add(new Producer(Arrays.copyOfRange(imgs, 0, threshold), threshold, compressedImage, flag));
        dividedTasks.add(new Producer(
                Arrays.copyOfRange(imgs, threshold, imgs.length), threshold, compressedImage, flag));
        return dividedTasks;
    }
}
