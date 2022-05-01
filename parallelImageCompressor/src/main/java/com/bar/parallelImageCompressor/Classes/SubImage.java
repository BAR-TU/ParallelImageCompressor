package com.bar.parallelImageCompressor.Classes;

import java.awt.image.BufferedImage;
import java.nio.Buffer;

public class SubImage {
   private int src_first_x;
   private int src_first_y;
   private int src_second_x;
   private int src_second_y;

   BufferedImage image;

    public SubImage(int src_first_x, int src_first_y, int src_second_x, int src_second_y, BufferedImage image) {
        this.src_first_x = src_first_x;
        this.src_first_y = src_first_y;
        this.src_second_x = src_second_x;
        this.src_second_y = src_second_y;
        this.image = image;
    }

    public int getSrc_first_x() {
        return src_first_x;
    }

    public void setSrc_first_x(int src_first_x) {
        this.src_first_x = src_first_x;
    }

    public int getSrc_first_y() {
        return src_first_y;
    }

    public void setSrc_first_y(int src_first_y) {
        this.src_first_y = src_first_y;
    }

    public int getSrc_second_x() {
        return src_second_x;
    }

    public void setSrc_second_x(int src_second_x) {
        this.src_second_x = src_second_x;
    }

    public int getSrc_second_y() {
        return src_second_y;
    }

    public void setSrc_second_y(int src_second_y) {
        this.src_second_y = src_second_y;
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }
}