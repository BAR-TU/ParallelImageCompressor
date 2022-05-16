package com.bar.parallelImageCompressor.Controllers;

import com.bar.parallelImageCompressor.Classes.SubImage;
import com.bar.parallelImageCompressor.Services.Parallelization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
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

	public static ConcurrentLinkedQueue<String> imagesToProcessQueue = new ConcurrentLinkedQueue<>();

	public static AtomicInteger finalImgNameNumber = new AtomicInteger();
	public static AtomicInteger subImgsNameNumber = new AtomicInteger();

	@PostMapping(value = "/lossy", consumes = "multipart/form-data")
	public void startCompressionLossy(@RequestParam("images") MultipartFile[] images) throws InterruptedException, ExecutionException {
		saveToDisk(images);

		parallelCompression("lossy");

		System.out.println("Compression finished successfully.");
	}

	@PostMapping(value = "/lossless", consumes = "multipart/form-data")
	public void startCompressionLossless(@RequestParam("images") MultipartFile[] images) throws InterruptedException, ExecutionException {
		saveToDisk(images);

		parallelCompression("lossless");

		System.out.println("Compression finished successfully.");
	}

	private void saveToDisk(MultipartFile[] images) {
		Arrays.stream(images)
				.forEach(img -> {
					try {
						uploadToLocalFileSystem(img);
					} catch(IOException e) {
						throw new RuntimeException(e);
					}
				});
	}

	private void uploadToLocalFileSystem(@RequestParam("img") MultipartFile img) throws IOException {
		String tmpdir = Files.createTempDirectory("tmp").toFile().getAbsolutePath();

		String fileName = StringUtils.cleanPath(img.getOriginalFilename());
		Path path = Paths.get(tmpdir + "\\" + fileName);

		int extDotIndex = img.getOriginalFilename().lastIndexOf(".");

		String type = img.getOriginalFilename().substring(extDotIndex + 1);

		try {
			if("png".equals(type) || "jpeg".equals(type) || "jpg".equals(type)) {
				Files.copy(img.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				imagesToProcessQueue.add(path.toAbsolutePath().toString());
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	public void parallelCompression(String flag) throws InterruptedException, ExecutionException {
		Collection<CompletableFuture<Void>> worker = new ArrayList<>();
		for(int i = 0; i < imagesToProcessQueue.size(); i++) {
			CompletableFuture<Void> future = CompletableFuture.runAsync(new Parallelization(flag));
			worker.add(future);
		}

		for(CompletableFuture<Void> voidCompletableFuture : worker) {
			voidCompletableFuture.get();
		}

		//CompletableFuture<Void> allFutures = CompletableFuture.allOf(worker.toArray(new CompletableFuture[worker.size()]));
		// allFutures.get();
	}

	public static synchronized void saveSubImage(BufferedImage compressImg, SubImage preCompressedImage, BufferedImage compressedImage) {
		Graphics2D writeToImage = compressedImage.createGraphics();
		writeToImage.drawImage(compressImg, preCompressedImage.getSrc_first_x(), preCompressedImage.getSrc_first_y(),
							   preCompressedImage.getSrc_second_x(), preCompressedImage.getSrc_second_y(), 0, 0,
							   preCompressedImage.getImage().getWidth(), preCompressedImage.getImage().getHeight(), null);
		writeToImage.dispose();
	}

}
