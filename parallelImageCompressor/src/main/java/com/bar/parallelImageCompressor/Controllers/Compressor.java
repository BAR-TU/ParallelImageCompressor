package com.bar.parallelImageCompressor.Controllers;

import com.bar.parallelImageCompressor.Services.Parallelization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/compress")
public class Compressor {

    @Autowired
    Parallelization parallelizationService;

    public static ConcurrentLinkedQueue<String> urlQueue = new ConcurrentLinkedQueue<>();

    public static AtomicInteger nameNumber = new AtomicInteger();

    @GetMapping("/lossy")
    public void startCompressionLossy() throws InterruptedException, IOException, ExecutionException {
        parallelCompression("lossy");

        //saveImagesMethod in ImageUtils

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
