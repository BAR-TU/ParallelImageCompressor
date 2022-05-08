package com.bar.parallelImageCompressor.Classes.HuffmanCoding;

import com.bar.parallelImageCompressor.Controllers.Compressor;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.Callable;

public class HuffmanCoding implements Callable<BufferedImage> {
    Node root;
    ArrayList<PixelColor> colors;
    ArrayList<PixelColor> secondaryColorsArr;
    MinHeap mh;
    BufferedImage imageToCompress;
    
    public HuffmanCoding(BufferedImage imageToCompress){
        this.colors = new ArrayList<PixelColor>();
        secondaryColorsArr = new ArrayList<PixelColor>();
        root = null;
        this.imageToCompress = imageToCompress;
    }
    
    public void getColorsFromImage(String imageFile){
        File file = new File(imageFile + ".bmp");
        try{
            FileWriter writer = new FileWriter(imageFile + "pixel_values.txt");
            BufferedImage img = ImageIO.read(file);
            for (int y = 0; y < img.getHeight(); y++) {
                for (int x = 0; x < img.getWidth(); x++) {
                    int pixel = img.getRGB(x,y);
                    Color color = new Color(pixel, true);
                    int red = color.getRed();
                    int green = color.getGreen();
                    int blue = color.getBlue();
                    int alpha = color.getAlpha(); 
                    String hexa = toHexa(red, green, blue, alpha);
                    String binary = "" + toBinary(red) + toBinary(green) + toBinary(blue) + toBinary(alpha);
                    int frequency = 1;
                    
                    secondaryColorsArr.add(new PixelColor(red, green, blue,alpha, binary, hexa));
                    for(int z = 0; z < colors.size(); z++){
                        if(colors.get(z).hexa.equals(hexa)){
                            frequency += colors.get(z).getFrequency();
                            colors.remove(z);
                            z--;
                        }
                    }
                    colors.add(new PixelColor(red, green, blue, alpha, binary, hexa, frequency));

                    writer.append(binary);
                    writer.flush();
                }
                writer.append("\n");
            }
            mh = new MinHeap(colors.size()); // Creating MinHeap with size of Colors ArrayList
            
            quickSort(colors, 0, colors.size() - 1);
            writer.close();
            for(int i = 0; i < colors.size(); i++){
                mh.insert(new Node(colors.get(i)));
            }
            mh.buildHeap(); // Building MinHeap
        }
        catch(Exception e){
            System.out.println(e);
        }
    }
    
    public String toHexa(int red, int green, int blue, int alpha){
        String hexa = "#";
        hexa += Integer.toHexString(red);
        hexa += Integer.toHexString(green);
        hexa += Integer.toHexString(blue);
        hexa += Integer.toHexString(alpha);
        return hexa;
    }
    
    public String toBinary(int num){
        String bits = "";
        for(; num > 0; num = num / 2){
            bits = num % 2 + bits;
        }
        for(int i = 8 - bits.length(); i > 0; i--){
            bits = "0" +  bits;
        }
        return bits;
    }
  
    public void quickSort(ArrayList<PixelColor> list, int low, int high){
        if(low < high){
            int partitionIndex = partition(low, high, list);
            quickSort(list, low, partitionIndex - 1);
            quickSort(list, partitionIndex + 1, high);
        }
        return;
    }
    
    public void swap(int index1, int index2, ArrayList<PixelColor> param){
        if(index1 < param.size() && index2 < param.size()){
            PixelColor temp = param.get(index1);
            param.set(index1, param.get(index2));
            param.set(index2, temp);
        }
    }
    
    public int partition(int low, int high, ArrayList<PixelColor> param){
        PixelColor pivot = param.get(low + ((high - low)/ 2));
        swap(low + ((high - low)/ 2), high, param);
        int i = low - 1; int j = low;
        for(; j < high; j++){
            if(param.get(j).getFrequency() < pivot.getFrequency()){
                swap(++i, j, param);
            }
        }
        swap(++i, high, param);
        return i;
    }
    
    public Node formHuffmanTree(){
        Node dummyRoot = null;
        while(mh.arr[1] != null){
            Node minOne = mh.deleteRoot();
            Node minTwo = mh.deleteRoot();
            minOne.setCode(0);
            minTwo.setCode(1);
            dummyRoot = new Node(minOne.getColor().getFrequency() + minTwo.getColor().getFrequency());
            dummyRoot.left = minOne;
            dummyRoot.right = minTwo;
            mh.insert(dummyRoot);
            mh.heapify();
        }
        return mh.arr[0];
    }
    
    public void encode(Node root, String bits){
        if(root != null){
            encode(root.left, "" + bits + "" + root.getCode());
            if(root.left == null && root.right == null){
                root.getColor().setNewBits(("" + bits + "" + root.getCode()).substring(2));
                System.out.println("Encoded Bits of " + root.getColor().getHexa() + " "
                                           + root.getColor().frequency +  ": " + root.getColor().getNewBits());
            }
            encode(root.right, bits + "" + root.getCode());
        }
    }
    
    public void setEncoding(){
        for(int i = 0; i < colors.size(); i++){
            for(int j = 0; j < secondaryColorsArr.size(); j++){
                if(colors.get(i).getHexa().equals(secondaryColorsArr.get(j).getHexa())){
                    secondaryColorsArr.get(j).setNewBits(colors.get(i).getNewBits());
                }
            }
        }
    }

    public void generateImage(String imageFile){
        File file = new File(imageFile + ".bmp");
        File newImageFile = new File(imageFile + "regenerated.png");
        try{
            BufferedImage img = ImageIO.read(file);
            BufferedImage regenImage = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
            for (int y = 0, z = 0; y < img.getHeight(); y++)
            {
                for (int x = 0; x < img.getWidth(); x++)
                {
                    String bits = secondaryColorsArr.get(z++).getOldBits();
                    int red = Integer.parseInt(bits.substring(0, 8), 2);
                    int green = Integer.parseInt(bits.substring(8, 16), 2);
                    int blue = Integer.parseInt(bits.substring(16, 24), 2);
                    int alpha = Integer.parseInt(bits.substring(24, 32), 2);
                    int pixel = (alpha << 24) | (red << 16) | (green << 8) | blue;
                    regenImage.setRGB(x, y, pixel);
                }
            }
            ImageIO.write(regenImage, "png", newImageFile);
        }
        catch(Exception e){
            System.out.println(e);
        }  
    }
    
    public BufferedImage controller() throws IOException {
        String pathname = "tmpimg" + Compressor.subImgsNameNumber.getAndAdd(1);
        File file = new File(pathname  + ".bmp");
        try {
            ImageIO.write(imageToCompress, "bmp", file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        getColorsFromImage(pathname);
        root = formHuffmanTree();
        encode(root, "");
        setEncoding();
        generateImage(pathname);
        File outputFile = new File(pathname + "regenerated.png");
        return  ImageIO.read(outputFile);
    }

    @Override
    public BufferedImage call() throws Exception {
        return controller();
    }
}

