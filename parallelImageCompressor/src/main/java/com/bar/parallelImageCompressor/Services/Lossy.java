package com.bar.parallelImageCompressor.Services;

import com.bar.parallelImageCompressor.Classes.Producer;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

@Component
public class Lossy {
    static Collection<Producer> taskss = new ArrayList<>();
    static int imagesForSubtask = 1;

    public static int name = 0;
    public static void Start() throws IOException {
        int coresToUse = Runtime.getRuntime().availableProcessors() - 1;
        ForkJoinPool pool = new ForkJoinPool(coresToUse);

        BufferedImage[] imgs = processIntoChunks();

        int border = (int)Math.ceil(Double.parseDouble(String.valueOf(imgs.length)) / Double.parseDouble(String.valueOf(coresToUse)));

        createTasks(imgs, border);

        compressImages(pool);

        pool.shutdown();
    }

    private static void createTasks(BufferedImage[] imgs, int border) {
        if (imgs.length > border) {
            createTasks(Arrays.copyOfRange(imgs, border, imgs.length), border);
        }
        if (border > imgs.length) {
            taskss.add(new Producer(Arrays.copyOfRange(imgs, 0, imgs.length), imagesForSubtask));
            return;
        }

        taskss.add(new Producer(Arrays.copyOfRange(imgs, 0, border), imagesForSubtask));
    }

    private static void compressImages(ForkJoinPool pool) {
        System.out.println("Processing...");
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
        System.out.println("Sub-images created.");
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
