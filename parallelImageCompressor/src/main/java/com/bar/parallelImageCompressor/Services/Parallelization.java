package com.bar.parallelImageCompressor.Services;

import com.bar.parallelImageCompressor.Classes.Producer;
import com.bar.parallelImageCompressor.Classes.SubImage;
import com.bar.parallelImageCompressor.Controllers.Compressor;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.concurrent.*;

import static com.bar.parallelImageCompressor.Controllers.Compressor.finalImgNameNumber;

@Component
public class Parallelization implements Runnable {

    public BufferedImage compressedImage;

    public String compressedImageType;
    Collection<Producer> taskss = Collections.synchronizedList(new ArrayList<>());
    static int imagesForSubtask = 1;
    public String flag;

    public Parallelization(String flag) {
        this.flag = flag;
        this.taskss = Collections.synchronizedList(new ArrayList<>());
    }

    public Parallelization() {

    }

    public void Start() throws IOException {
        int coresToUse = Runtime.getRuntime().availableProcessors() - 1;
        ForkJoinPool pool = new ForkJoinPool(coresToUse);
        System.out.println(coresToUse);
        SubImage[] imgs = processIntoChunks(coresToUse);
        System.out.println("Sub-images created");

        int border = (int)Math.ceil(Double.parseDouble(String.valueOf(imgs.length)) / Double.parseDouble(String.valueOf(coresToUse)));

        createTasks(imgs, border);

        compressImages(pool);

        saveImage();
        System.out.println("Compressed image saved");
    }

    private void saveImage() throws IOException {
        String format = "";
        if ("lossy".equals(flag))
            format = "jpg";
        else
            format = "png";

        File outputFile = new File("img" + finalImgNameNumber.getAndAdd(1) + "." + format);
        ImageIO.write(compressedImage, format, outputFile);
    }

    private void createTasks(SubImage[] imgs, int border) {
        if (imgs.length > border) {
            createTasks(Arrays.copyOfRange(imgs, border, imgs.length), border);
        }
        if (border > imgs.length) {
            taskss.add(new Producer(Arrays.copyOfRange(imgs, 0, imgs.length), imagesForSubtask, compressedImage, flag));
            return;
        }

        taskss.add(new Producer(Arrays.copyOfRange(imgs, 0, border), imagesForSubtask, compressedImage, flag));
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
                future.get(500, TimeUnit.SECONDS);
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
        FileInputStream file = new FileInputStream(Objects.requireNonNull(Compressor.imagesToProcessQueue.poll()));
        BufferedImage image = ImageIO.read(file);
        compressedImage = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());

        long numOfChunks;
        for (int i = 1; ;i++) {
            double currPower = Math.pow(4.0, Double.parseDouble(String.valueOf(i)));
            if(currPower > cores) {
                double prevPower = Math.pow(4.0, Double.parseDouble(String.valueOf(i - 1)));
                long prevPowerDiff = Math.round(Double.valueOf(cores) - prevPower);
                long currPowerDiff = Math.round(currPower - Double.valueOf(cores));
                if (prevPowerDiff > currPowerDiff) {
                    numOfChunks = Math.round(currPower);
                    break;
                }
                numOfChunks = Math.round(prevPower);
                break;
            }
        }

        SubImage[] imgs = new SubImage[Integer.parseInt(String.valueOf(numOfChunks))];

        file.close();
        return divideToSubImages(image, imgs);
    }

    private BufferedImage checkForSize(BufferedImage image) {
        int imgWidth = image.getWidth();
        int imgHeight = image.getHeight();

        int correctWidth = 0;
        int correctHeight = 0;
        if (image.getWidth() % 8 != 0) {
            for (int k = image.getWidth() + 1; ; k++) {
                if (k % 8 == 0) {
                    correctWidth = k;
                    break;
                }
            }
        }
        if (image.getHeight() % 8 != 0) {
            for (int k = image.getHeight() + 1; ; k++) {
                if (k % 8 == 0) {
                    correctHeight = k;
                    break;
                }
            }
        }

        if (correctWidth != 0)
            imgWidth = correctWidth;
        if (correctHeight != 0)
            imgHeight = correctHeight;



        BufferedImage img = new BufferedImage(imgWidth, imgHeight, image.getType());

        Graphics2D graphics = img.createGraphics();
        graphics.drawImage(image, 0, 0, imgWidth, imgHeight, null);
        graphics.dispose();

        return img;
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
                img = checkForSize(img);

                Graphics2D img_creator = img.createGraphics();

                int src_first_x = img.getWidth() * j;
                int src_first_y = img.getHeight() * i;

                int src_second_x = img.getWidth() * j + img.getWidth();
                int src_second_y = img.getHeight() * i + img.getHeight();

                img_creator.drawImage(image, 0, 0, img.getWidth(), img.getHeight(), src_first_x, src_first_y, src_second_x, src_second_y, null);

                imgs[current_img] = new SubImage(src_first_x, src_first_y, src_second_x, src_second_y, img);
                current_img++;
                img_creator.dispose();
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
