package com.bar.parallelImageCompressor.Services;

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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Compression {
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
