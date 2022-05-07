package com.bar.parallelImageCompressor.Services.compression;

import org.springframework.web.multipart.MultipartFile;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;

public class Lossy {
	static final int WIDTH = 512;
	static final int HEIGHT = 512;

		private byte[] Lossy(String[] args) throws IOException {
			//Fix readimage
			String fileName = args[0];
			int n = Integer.parseInt(args[1]);
			int m = n/4096;
			// Read in RGB file
			BufferedImage image = readImage(fileName);
			int[] ycbcr = convertToYCbCr(image);
			// Extract Y from YCbCr
			int[] y = new int[WIDTH*HEIGHT];
			for (int i = 0; i < WIDTH*HEIGHT; i++) {
				y[i] = ycbcr[i];
			}
			// Extract Cb from YCbCr
			int[] cb = new int[WIDTH*HEIGHT];
			int offset = WIDTH*HEIGHT;
			for(int i = 0; i < WIDTH*HEIGHT; i++){
				cb[i] = ycbcr[i + offset];
			}
			// Extract Cr from YCbCr
			int[] cr = new int[WIDTH*HEIGHT];
			offset = 2*WIDTH*HEIGHT;
			for(int i = 0; i < WIDTH*HEIGHT; i++){
				cr[i] = ycbcr[i + offset];
			}
			// Perform progressive analysis and finish program if specified in arguments
			JFrame frame = new JFrame();
			if (n == -1) {
				for (int i = 1; i <= 64; i++) {
					System.out.println("Iteration: " + i);
					displayAnalysis(y, cb, cr, i*4096, frame);
				}
				System.out.println("Finished!");
			} else {
				displayAnalysis(y, cb, cr, n, frame);
			}
			return fileName.getBytes(); //
		}
	public static BufferedImage readImage (String fileName) {
		BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		try {
			File file = new File(fileName);
			InputStream is = new FileInputStream(file);
			// Get length of file and create byte array
			long len = file.length();
			byte[] bytes = new byte[(int)len];
			// Read all bytes from image file into byte array0
			int offset = 0;
			int numRead = 0;
			while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
				offset += numRead;
			}
			// Set image contents
			int ind = 0;
			for(int y = 0; y < HEIGHT; y++){
				for(int x = 0; x < WIDTH; x++){
					byte r = bytes[ind];
					byte g = bytes[ind+HEIGHT*WIDTH];
					byte b = bytes[ind+HEIGHT*WIDTH*2];
					// set the RGB value for a specific pixel
					int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
					img.setRGB(x,y,pix);
					ind++;
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return img;
	}

	public static int[] convertToYCbCr (BufferedImage image) {
		int index = 0;
		int[] ycbcr = new int[3*WIDTH*HEIGHT];
		for (int j = 0; j < HEIGHT; j++) {
			for (int i = 0; i < WIDTH; i++) {
				Color pixel = new Color(image.getRGB(i, j));
				int y = (int)(0.299*pixel.getRed() + 0.587*pixel.getGreen() + 0.114*pixel.getBlue());
				int cb = (int)(-0.159*pixel.getRed() - 0.332*pixel.getGreen() + 0.050*pixel.getBlue());
				int cr = (int)(0.500*pixel.getRed() - 0.419*pixel.getGreen() - 0.081*pixel.getBlue());
				ycbcr[index] = y;
				ycbcr[index+WIDTH*HEIGHT] = cb;
				ycbcr[index+2*WIDTH*HEIGHT] = cr;
				index ++;
			}
		}
		return ycbcr;
	}

	public static BufferedImage convertToRGB (int[] YCbCr) {
		BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		int index = 0;
		for (int y = 0; y < HEIGHT; y++) {
			for (int x = 0; x < WIDTH; x++) {
				int r = (int)(0.871*YCbCr[index] - 0.233*YCbCr[index+WIDTH*HEIGHT] + 1.405*YCbCr[index+2*WIDTH*HEIGHT]);
				int g = (int)(0.221*YCbCr[index] - 1.752*YCbCr[index+WIDTH*HEIGHT] - 0.689*YCbCr[index+2*WIDTH*HEIGHT]);
				int b = (int)(4.236*YCbCr[index] + 7.626*YCbCr[index+WIDTH*HEIGHT] - 0.108*YCbCr[index+2*WIDTH*HEIGHT]);
				// System.out.println("r:" + r + " g:" + g + " b:" + b);
				r = Math.max(0, Math.min(r, 255));
				g = Math.max(0, Math.min(g, 255));
				b = Math.max(0, Math.min(b, 255));
				Color color = new Color(r, g, b);
				image.setRGB(x, y, color.getRGB());
				index ++;
			}
		}
		return image;
	}

	public static ArrayList<double[][]> encodeDCT(ArrayList<int[][]> blocks) {
		ArrayList<double[][]> dct = new ArrayList<double[][]>();
		// prepopulate list of cosine values
		double[][] cos = new double[8][8];
		for (int u = 0; u < 8; u++) {
			for (int x = 0; x < 8; x++) {
				cos[u][x] = Math.cos((2*x+1)*u*Math.PI/16);
			}
		}
		for (int[][] block: blocks) {
			dct.add(forwardDCT(block, cos));
		}
		return dct;
	}

	public static double[][] forwardDCT(int[][] block, double[][] cos) {
		double[][] dct = new double[8][8];
		// loop for u
		for (int u = 0; u < 8; u++) {
			double cu = 1;
			if (u == 0)
				cu = 1/Math.sqrt(2);
			// loop for v
			for (int v = 0; v < 8; v++) {
				double cv = 1;
				if (v == 0)
					cv = 1/Math.sqrt(2);
				// keep track of sum
				double sum = 0;
				// get sum for all x and y
				for (int x = 0; x < 8; x++) {
					for (int y = 0; y < 8; y++) {
						sum += block[y][x] * cos[u][x] * cos[v][y];
					}
				}
				// get value for F(u, v)
				dct[v][u] = (0.25 * cu * cv * sum);
			}
		}
		return dct;
	}

	public static ArrayList<int[][]> decodeDCT(ArrayList<double[][]> blocks, int m) {
		// use coefficient to select first m values in zig zag
		for (double[][] block : blocks) {
			int count = 0;
			// zig zag through upper left half
			for (int j = 0; j < 8; j++) {
				int x = 0;
				int y = j;
				while(x < 8 && y >= 0) {
					if (count == m) {
						block[y][x] = 0;
					} else {
						count ++;
					}
					x ++;
					y --;
				}
			}
			// zig zag through lower right half
			for (int i = 1; i < 8; i++) {
				int x = i;
				int y = 7;
				while(x < 8 && y >= 0) {
					if (count == m) {
						block[y][x] = 0;
					} else {
						count ++;
					}
					x ++;
					y --;
				}
			}
		}
		// prepopulate list of cosine values
		double[][] cos = new double[8][8];
		for (int u = 0; u < 8; u++) {
			for (int x = 0; x < 8; x++) {
				cos[u][x] = Math.cos((2*x+1)*u*Math.PI/16);
			}
		}
		// run IDCT on each block
		ArrayList<int[][]> idct = new ArrayList<int[][]>();
		for (double[][] block : blocks) {
			idct.add(inverseDCT(block, cos));
		}
		return idct;
	}

	public static int[][] inverseDCT(double[][] block, double[][] cos) {
		int original[][] = new int[8][8];
		// loop for x
		for (int x = 0; x < 8; x++) {
			// loop for y
			for (int y = 0; y < 8; y++) {
				// keep track of sum
				double sum = 0;
				// get sum for all u and v
				for (int u = 0; u < 8; u++) {
					double cu = 1;
					if (u == 0) {
						cu = 1/Math.sqrt(2);
					}
					for (int v = 0; v < 8; v++) {
						double cv = 1;
						if (v == 0) {
							cv = 1/Math.sqrt(2);
						}
						sum += cu * cv * block[v][u] * cos[u][x] * cos[v][y];
					}
				}
				// get value for F(u, v)
				original[y][x] = (int)(0.25 * sum);
			}
		}
		return original;
	}

	public static ArrayList<int[][]> getBlocks (int[] array) {
		ArrayList<int[][]> blocks = new ArrayList<int[][]>();
		// get all blocks through the entire height
		int index = 0;
		int xOffset = 0;
		int yOffset = 0;
		while(index < array.length) {
			// create 8x8 block
			int[][] block = new int[8][8];
			for (int j = 0; j < 8; j++) {
				for (int i = 0; i < 8; i++) {
					block[j][i] = array[i + xOffset + (j+yOffset)*WIDTH];
					index ++;
				}
			}
			xOffset += 8;
			if (xOffset == WIDTH) {
				xOffset = 0;
				yOffset += 8;
			}
			blocks.add(block);
		}
		return blocks;
	}

	public static int[] combineBlocks(ArrayList<int[][]> blocks) {
		int[] array = new int[blocks.size()*64];
		int xOffset = 0;
		int yOffset = 0;
		for (int[][] block : blocks) {
			for (int j = 0; j < 8; j++) {
				for (int i = 0; i < 8; i++) {
					array[i + xOffset + (j+yOffset)*WIDTH] = block[j][i];
				}
			}
			xOffset += 8;
			if (xOffset == WIDTH) {
				xOffset = 0;
				yOffset += 8;
			}
		}
		return array;
	}

	public static void displayAnalysis(int[] y, int[] cb, int[] cr, int n, JFrame frame) {
		// perform analysis of DCT vs DWT
		int m = n/4096;
		// get 8x8 blocks for DCT
		final ArrayList<int[][]> yBlocks = getBlocks(y);
		final ArrayList<int[][]> cbBlocks = getBlocks(cb);
		final ArrayList<int[][]> crBlocks = getBlocks(cr);
		// perform DCT for each channel
		final ArrayList<double[][]> yDCT = encodeDCT(yBlocks);
		final ArrayList<double[][]> cbDCT = encodeDCT(cbBlocks);
		final ArrayList<double[][]> crDCT = encodeDCT(crBlocks);
		// inverse DCT
		ArrayList<int[][]> yIDCT = decodeDCT(yDCT, m);
		ArrayList<int[][]> cbIDCT = decodeDCT(cbDCT, m);
		ArrayList<int[][]> crIDCT = decodeDCT(crDCT, m);
		int[] newY = combineBlocks(yIDCT);
		int[] newCb = combineBlocks(cbIDCT);
		int[] newCr = combineBlocks(crIDCT);
		int[] idctYCbCr = new int[3*WIDTH*HEIGHT];
		int index = 0;
		for (int i = 0; i < WIDTH*HEIGHT; i++) {
			idctYCbCr[index] = newY[i];
			index ++;
		}
		for (int i = 0; i < WIDTH*HEIGHT; i++) {
			idctYCbCr[index] = newCb[i];
			index ++;
		}
		for (int i = 0; i < WIDTH*HEIGHT; i++) {
			idctYCbCr[index] = newCr[i];
			index ++;
		}
		// display results
		BufferedImage decodedImageDCT = convertToRGB(idctYCbCr);
		// Display decoded image into a jframe
		JLabel dctLabel = new JLabel(new ImageIcon(decodedImageDCT));
		JPanel panel = new JPanel();
		panel.add("DCT", new JScrollPane(dctLabel));
		frame.getContentPane().add(panel);
		frame.pack();
		frame.setVisible(true);
		try {
			Thread.sleep(1200);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}
