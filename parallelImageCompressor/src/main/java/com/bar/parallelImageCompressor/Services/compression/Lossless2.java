package com.bar.parallelImageCompressor.Services.compression;

import org.yaml.snakeyaml.util.ArrayUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Lossless2 {
	public static void main(String[] args) {
		String strLine = null;
		try {
			// Opening the file that is the first
			// command line parameter
			FileInputStream fstream = new FileInputStream(args[0]);
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

			//Read File Line By Line
			while ((strLine = br.readLine()) != null) {
				// Print the content on the console
				System.out.println(strLine);
				List<Integer> compressed = compress(strLine, Double.parseDouble(args[1]), args[0]);
				//compressed contains the encoded output symbols
				System.out.println(compressed);
			}

		} catch (Exception e) {//Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
	}

	/**
	 * Compress a string to a list of output symbols.
	 */
	public static List<Integer> compress(String uncompressed, double max_table_size, String filename) throws IOException {
		// Build the dictionary.
		int dictSize = 256;

		Map<String, Integer> dictionary = new HashMap<String, Integer>();

		for (int i = 0; i < 256; i++)
			dictionary.put("" + (char) i, i); //storing all the primary ASCII characters in the dictionary.

		String string = "";

		List<Integer> result = new ArrayList<>();

		String binarystring = ""; //binary string will be used to store the binary representation of encoded out symbols.

		for (char symbol : uncompressed.toCharArray()) {
			String new_string = string + symbol;

			if (dictionary.containsKey(new_string))
				string = new_string;
				//if table contains the key then it will store
				// the new string as current string and then append it with the next symbol.


			else {
				if (dictSize < Math.pow(2, max_table_size)) { // Checking whether the table is full or not.

					result.add(dictionary.get(string)); //Storing the string to the dictionary

					binarystring = binarystring + intToString(dictionary.get(string), 8) + "\t";// appending the binary result to the previous results.

					// Add new_string to the dictionary.
					dictionary.put(new_string, dictSize++);
					string = "" + symbol;


				} else {
					System.out.println("Table reached max size");
				}
			}
		}

		// Output the code for string.
		if (!string.equals("")) {
			result.add(dictionary.get(string));
			binarystring = binarystring + intToString(dictionary.get(string), 8);
		}

		String[] name = filename.split(".txt");
		/* Storing the encoded output to the file */
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(name[0] + ".lzw"), "UTF-16BE"))) {
			writer.write(binarystring); //Storing the output in 16bit format.

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}


	/**
	 * This function is used to convert given integer to its binary representation in the specified group size.
	 */
	public static String intToString(int number, int groupSize) {
		StringBuilder result = new StringBuilder();

		for (int i = 15; i >= 0; i--) {
			int mask = 1 << i;
			result.append((number & mask) != 0 ? "1" : "0");

			if (i % groupSize == 0)
				result.append(" ");
		}
		result.replace(result.length() - 1, result.length(), "");

		return result.toString();
	}

	public static BufferedImage ConvertToBufferedImage(List<Integer> compressed) throws IOException {

		Byte[] bytes = compressed.stream().map(Integer::byteValue).toArray(Byte[]::new);
		byte[] bi = new byte[bytes.length];
		for (int i = 0; i < bytes.length; i++) {
			bi[i] = bytes[i];
		}
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		ObjectOutputStream o = new ObjectOutputStream(b);
		o.writeObject(compressed);
		InputStream is = new ByteArrayInputStream(bi);
		return ImageIO.read(is);
	}




}