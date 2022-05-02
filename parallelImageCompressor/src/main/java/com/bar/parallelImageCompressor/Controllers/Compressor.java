package com.bar.parallelImageCompressor.Controllers;

import com.bar.parallelImageCompressor.Services.Lossy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/compress")
public class Compressor {

    @Autowired
    Lossy lossyService;

    public static ConcurrentLinkedQueue<String> urlQueue = new ConcurrentLinkedQueue<>();

    public static AtomicInteger nameNumber = new AtomicInteger();

    @GetMapping
    public void startCompression() throws InterruptedException, IOException, ExecutionException {
        urlQueue.add("https://www.educative.io/api/edpresso/shot/5120209133764608/image/5075298506244096/test.jpg");
        urlQueue.add("https://s1.cdn.autoevolution.com/images/news/transformed-bmw-e92-335i-sounds-good-video-57366_1.png");
        CompletableFuture<Void> future1 = CompletableFuture.runAsync(new Lossy());
        CompletableFuture<Void> future2 = CompletableFuture.runAsync(new Lossy());
        Collection<CompletableFuture<Void>> worker = new ArrayList<>();
        worker.add(future1);
        worker.add(future2);
//        CompletableFuture.supplyAsync(() -> "").thenCompose(f -> future1).thenCompose(f -> future2);
//                CompletableFuture<Void> allFutures = future1.thenCompose(f -> future2);
//                allFutures.get();
//                System.out.println("Exiting...");

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(worker.toArray(new CompletableFuture[worker.size()]));
        allFutures.get();
        System.out.println("Exiting...");
    }

    public static void  compressJpegImage(File originalImage, File compressedImage, float compressionQuality) throws IOException {
        //        File originalImage = new File("C:\\Users\\a.dachkinova\\Desktop\\four.jpg");
//        File compressedImage = new File("C:\\Users\\a.dachkinova\\Desktop\\compressedImage.jpg");
//        try {
//            compressJpegImage(originalImage, compressedImage, 0.7f);
//            System.out.println("Done!");
//        }
//        catch(IOException e){
//            System.out.println(e.getMessage());
//        }
        RenderedImage image = ImageIO.read(originalImage);
        ImageWriter jpegWriter = ImageIO.getImageWritersByFormatName("jpg").next();
        ImageWriteParam jpegWriteParam = jpegWriter.getDefaultWriteParam();
        jpegWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        jpegWriteParam.setCompressionQuality(compressionQuality);

        try(ImageOutputStream outputStream = ImageIO.createImageOutputStream(compressedImage)) {
            jpegWriter.setOutput(outputStream);
            IIOImage outputImage = new IIOImage(image, null, null);
            jpegWriter.write(null, outputImage, jpegWriteParam);
        }

        jpegWriter.dispose();
    }
}
