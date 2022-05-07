package com.bar.parallelImageCompressor.Services.compression;

import com.googlecode.pngtastic.core.PngImage;
import com.googlecode.pngtastic.core.PngOptimizer;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.io.*;
import java.util.*;

public class Lossless {

		public static HashMap<String, Integer> dictionary = new HashMap<>();
		public static int dictSize = 256;
		public static String P = "";
		public static String filename = "";
		public static String BP = "";
		public static byte inputByte;
		public static byte[] buffer = new byte[3];
		public static boolean isLeft = true;

		public static void main(String[] args) {
			System.out.println("*****LZW Image Compression*****");
			System.out.println("Enter image to compress with extension: ");
			Scanner sc = new Scanner(System.in);
			filename = sc.next();
			try {
				File file = new File(filename);
				compress();
				String[] getFileNameWOExtn = filename.split("\\.");
				System.out.println("Compression complete!Check file "+getFileNameWOExtn[0].concat(".lzw")+"!");
			}
			catch(IOException ie) {
				System.out.println("File "+filename+" not found!");
			}
		}

		public static void compress() throws IOException {

			int i;
			int byteToInt;
			char c;

			// Character dictionary
			for(i = 0; i < 256; i++) {
				dictionary.put(Character.toString((char)i), i);
			}

			// Read input file and output file
			try(RandomAccessFile inputFile = new RandomAccessFile(filename, "r")) {
				String[] getFileNameWOExtn = filename.split("\\.");
				try(RandomAccessFile outputFile = new RandomAccessFile(getFileNameWOExtn[0].concat(".lzw"), "rw")) {

					try {
						// Read first byte to initialize P
						inputByte = inputFile.readByte();
						byteToInt = new Byte(inputByte).intValue();

						if(byteToInt < 0) {
							byteToInt += 256;
						}
						c = (char) byteToInt;
						P = "" + c;

						while(true) {
							inputByte = inputFile.readByte();
							byteToInt = new Byte(inputByte).intValue();

							if(byteToInt < 0) {
								byteToInt += 256;
							}
							c = (char) byteToInt;

							// if P+c is present in dictionary
							if(dictionary.containsKey(P + c)) {
								P = P + c;
							} else {
								BP = convertTo12Bit(dictionary.get(P));
								if(isLeft) {
									buffer[0] = (byte) Integer.parseInt(BP.substring(0, 8), 2);
									buffer[1] = (byte) Integer.parseInt(BP.substring(8, 12) + "0000", 2);
								} else {
									buffer[1] += (byte) Integer.parseInt(BP.substring(0, 4), 2);
									buffer[2] = (byte) Integer.parseInt(BP.substring(4, 12), 2);
									for(i = 0; i < buffer.length; i++) {
										outputFile.writeByte(buffer[i]);
										buffer[i] = 0;
									}
								}
								isLeft = !isLeft;
								if(dictSize < 4096) {
									dictionary.put(P + c, dictSize++);
								}

								P = "" + c;
							}
						}

					} catch(IOException ie) {
						BP = convertTo12Bit(dictionary.get(P));
						if(isLeft) {
							buffer[0] = (byte) Integer.parseInt(BP.substring(0, 8), 2);
							buffer[1] = (byte) Integer.parseInt(BP.substring(8, 12) + "0000", 2);
							outputFile.writeByte(buffer[0]);
							outputFile.writeByte(buffer[1]);
						} else {
							buffer[1] += (byte) Integer.parseInt(BP.substring(0, 4), 2);
							buffer[2] = (byte) Integer.parseInt(BP.substring(4, 12), 2);
							for(i = 0; i < buffer.length; i++) {
								outputFile.writeByte(buffer[i]);
								buffer[i] = 0;
							}
						}
						inputFile.close();
						outputFile.close();
					}
				}
			}

		}

		public static String convertTo12Bit(int i) {
			String to12Bit = Integer.toBinaryString(i);
			while (to12Bit.length() < 12) to12Bit = "0" + to12Bit;
			return to12Bit;
		}


		// ----- Lossless


	private byte[] Lossless(MultipartFile multipartFile) throws IOException {
		String imageExtention = multipartFile.getContentType().substring(multipartFile.getContentType().indexOf('/') + 1);
		if(!StringUtils.isEmpty(imageExtention)){
			if(imageExtention.equals("jpeg")){
				//JPEG, JPG
				BufferedImage image = ImageIO.read(new ByteArrayInputStream(multipartFile.getBytes()));
				ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
				ImageWriteParam param = writer.getDefaultWriteParam();
				param.setCompressionMode(ImageWriteParam.MODE_DEFAULT);

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
				writer.setOutput(ios);
				writer.write(null, new IIOImage(image, null, null), param);

				return baos.toByteArray();
			} else if(imageExtention.equals("png")){
				//PNG
				PngImage image = new PngImage(new ByteArrayInputStream(multipartFile.getBytes()));
				PngOptimizer pngOptimizer = new PngOptimizer();
				PngImage optimizedImage = pngOptimizer.optimize(image);

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				optimizedImage.writeDataOutputStream(baos);

				return baos.toByteArray();
			}
		}
		return multipartFile.getBytes();
	}
}
