package com.bar.parallelImageCompressor.Classes;

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
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            Future<BufferedImage> future = executorService.submit(new HelloApplication(imgs[i].getImage()));
            executorService.shutdown();
            BufferedImage compressimg = future.get();
            synchronized (this) {
                saveSubImage(compressimg, imgs[i]);
            }
//            Thread.sleep(1000);
        }
    }

    private void saveSubImage(BufferedImage compressimg, SubImage preCompressedImage) {
        Graphics2D writeToImage = compressedImage.createGraphics();
        writeToImage.drawImage(compressimg, preCompressedImage.getSrc_first_x(), preCompressedImage.getSrc_first_y(),
                preCompressedImage.getSrc_second_x(), preCompressedImage.getSrc_second_y(), 0, 0,
                preCompressedImage.getImage().getWidth(), preCompressedImage.getImage().getHeight(), null);
        writeToImage.dispose();
    }
    private Collection<Producer> createSubtasks() {
        List<Producer> dividedTasks = new ArrayList<>();
        dividedTasks.add(new Producer(Arrays.copyOfRange(imgs, 0, threshold), threshold, compressedImage));
        dividedTasks.add(new Producer(
                Arrays.copyOfRange(imgs, threshold, imgs.length), threshold, compressedImage));
        return dividedTasks;
    }
}
