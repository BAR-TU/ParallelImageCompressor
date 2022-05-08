package com.bar.parallelImageCompressor.Classes.HuffmanCoding;

/**
 * @author Hamza Akbar
 */
public class PixelColor {
    int [] rgba;
    String oldBits;
    String newBits;
    String hexa;
    long frequency;
    
    public void increaseFrequency(){
        this.frequency++;
    }
    
    public PixelColor(int red, int green, int blue, int alpha, String oldBits, String hexa){
        this.rgba = new int[4];
        rgba[0] = red;
        rgba[1] = green;
        rgba[2] = blue;
        rgba[3] = alpha;
        this.oldBits = oldBits;
        this.hexa = hexa;
        this.frequency = 1;
    }
    
    public PixelColor(int red, int green, int blue, int alpha, String oldBits, String hexa, long freq){
        this.rgba = new int[4];
        rgba[0] = red;
        rgba[1] = green;
        rgba[2] = blue;
        rgba[3] = alpha;
        this.oldBits = oldBits;
        this.hexa = hexa;
        this.frequency = freq;
    }
    
    public PixelColor(long freq){
        this.rgba = null;
        this.oldBits = "";
        this.newBits = "";
        this.hexa = "";
        this.frequency = freq;
    }
    
    public long getFrequency() {
        return frequency;
    }
    
    public int[] getRgba() {
        return rgba;
    }

    public String getOldBits() {
        return oldBits;
    }

    public String getNewBits() {
        return newBits;
    }

    public void setFrequency(long frequency) {
        this.frequency = frequency;
    }

    public String getHexa() {
        return hexa;
    }

    public void setHexa(String hexa) {
        this.hexa = hexa;
    }

    public void setOldBits(String oldBits) {
        this.oldBits = oldBits;
    }

    public void setNewBits(String newBits) {
        this.newBits = newBits;
    } 
    
}