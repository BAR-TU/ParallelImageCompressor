package com.bar.parallelImageCompressor.Services;

import com.bar.parallelImageCompressor.Classes.Producer;
import com.bar.parallelImageCompressor.Classes.SubImage;
import com.bar.parallelImageCompressor.Controllers.Compressor;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.*;
import java.util.concurrent.*;

import static com.bar.parallelImageCompressor.Controllers.Compressor.nameNumber;

@Component
public class Parallelization implements Runnable {

    public BufferedImage compressedImage;
    static Collection<Producer> taskss = new ArrayList<>();
    static int imagesForSubtask = 1;

    public Parallelization() {

    }

    public  Boolean Start() throws IOException {
        int coresToUse = Runtime.getRuntime().availableProcessors() - 1;
        ForkJoinPool pool = new ForkJoinPool(coresToUse);

        SubImage[] imgs = processIntoChunks(coresToUse);
        System.out.println("Sub-images created");

        int border = (int)Math.ceil(Double.parseDouble(String.valueOf(imgs.length)) / Double.parseDouble(String.valueOf(coresToUse)));

        createTasks(imgs, border);

        compressImages(pool);

        saveImage();
        System.out.println("Compressed image saved");
    }

    private void saveImage() throws IOException {
        File outputFile = new File("img" + nameNumber.getAndAdd(1) + ".jpg");
        ImageIO.write(compressedImage, "jpg", outputFile);
    }

    private void createTasks(SubImage[] imgs, int border) {
        if (imgs.length > border) {
            createTasks(Arrays.copyOfRange(imgs, border, imgs.length), border);
        }
        if (border > imgs.length) {
            taskss.add(new Producer(Arrays.copyOfRange(imgs, 0, imgs.length), imagesForSubtask, compressedImage));
            return;
        }

        taskss.add(new Producer(Arrays.copyOfRange(imgs, 0, border), imagesForSubtask, compressedImage));
    }

    private void compressImages(ForkJoinPool pool) {
        System.out.println("Compressing...");
        try {
            List<Future<Void>> futures = new ArrayList<>();
            for (Producer p : taskss) {
                futures.add(pool.submit(p));
            }
            System.out.println("Started all tasks");
            for (Future<Void> future : futures) {
                future.get(60, TimeUnit.SECONDS);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (RejectedExecutionException e) {
            e.printStackTrace();
            if (e.getCause() == null)
                throw new AssertionError();
            throw new AssertionError(e.getCause());
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            throw new RuntimeException(e);
        } finally {
            pool.shutdown();
        }
    }

    SubImage[] processIntoChunks(int cores) throws IOException {
        System.setProperty("http.agent", "Chrome");

        URL url = new URL(Objects.requireNonNull(Compressor.imagesToProcessQueue.poll()));
        InputStream is = url.openStream();
        BufferedImage image = ImageIO.read(is);
        compressedImage = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());

        long numOfChunks;
        for (int i = 1; ;i++) {
            double currPower = Math.pow(4.0, Double.parseDouble(String.valueOf(i)));
            if (currPower > cores) {
                double prevPower = Math.pow(4.0, Double.parseDouble(String.valueOf(i - 1)));
                numOfChunks = Integer.parseInt(String.valueOf(Math.round(currPower)));
                break;
            }
        }

        SubImage[] imgs = new SubImage[numOfChunks];

        return divideToSubImages(image, imgs);
    }

    private SubImage[] divideToSubImages(BufferedImage image, SubImage[] imgs) {
        int lengthSqrt = Integer.parseInt(String.valueOf(Math.round(Math.sqrt(Double.parseDouble(String.valueOf(imgs.length))))));
        int subimage_Width = image.getWidth() / lengthSqrt;
        int subimage_Height = image.getHeight() / lengthSqrt;

        int current_img = 0;


        for (int i = 0; i < lengthSqrt; i++)
        {
            for (int j = 0; j < lengthSqrt; j++)
            {

                BufferedImage img = new BufferedImage(subimage_Width, subimage_Height, image.getType());

                Graphics2D img_creator = img.createGraphics();

                int src_first_x = subimage_Width * j;
                int src_first_y = subimage_Height * i;

                int src_second_x = subimage_Width * j + subimage_Width;
                int src_second_y = subimage_Height * i + subimage_Height;

                img_creator.drawImage(image, 0, 0, subimage_Width, subimage_Height, src_first_x, src_first_y, src_second_x, src_second_y, null);

                imgs[current_img] = new SubImage(src_first_x, src_first_y, src_second_x, src_second_y, img);
                current_img++;
            }
        }

        return imgs;
    }

    @Override
    public void run() {
        try {
            Start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
