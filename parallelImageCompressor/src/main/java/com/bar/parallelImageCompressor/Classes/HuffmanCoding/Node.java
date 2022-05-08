package com.bar.parallelImageCompressor.Classes.HuffmanCoding;

/**
 * @author Hamza Akbar
 */
public class Node {
    private PixelColor color;
    Node left;
    Node right;
    int code;
    
    public Node(PixelColor px){
        this.color = px;
        this.left = null;
        this.right = null;
        this.code = -1;
    }
    
    public Node(long freq){
        this.color = new PixelColor(freq);
        this.left = null;
        this.right = null;
        this.code = -1;
    }
   
    public PixelColor getColor(){
        return this.color;
    }

    public void setColor(int freq){
        color.setFrequency(freq);
    }

    public void setCode(int code){
        this.code = code;
    }

    public int getCode() {
        return code;
    }
   
   
}
