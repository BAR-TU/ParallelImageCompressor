package com.example.lossytestjavafx.Graph;

import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

import java.awt.*;

public class Graph extends ImageView {
    public WritableImage image;
    private PixelWriter pixelWriter;

    private int width;
    private int height;

    public Graph() {
    }

    public Graph(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void setPixel(int x, int y, int color) {
        pixelWriter.setArgb(x, y, color);
    }

    public void ready() {
                image = new WritableImage(width, height);
                setImage(image);

                pixelWriter = image.getPixelWriter();
                createPanel();
    }

    private void createPanel() {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                setPixel(i, j, Color.WHITE.getRGB());
            }
        }
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void clear() {
        createPanel();
    }

    public void setImage(WritableImage image) {
        this.image = image;
    }
}