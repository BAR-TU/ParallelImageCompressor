package com.bar.parallelImageCompressor.Classes.HuffmanCoding;

public class MinHeap {
    
    public Node[] arr;
    public int lastIndex;
    
    public MinHeap(int size){
        this.arr = new Node[size];
        lastIndex = 0;
    }

    public void insert(Node node) {
        if (lastIndex < arr.length) {
            arr[lastIndex++] = node;
//            lastIndex++;
        }
    }

    public Node deleteRoot() {
        int index = 0;
        while(index < arr.length && arr[index] != null) {
            index++;
        }
        Node temporary = arr[0];
        swap(0, --index);
        arr[index] = null;
        lastIndex--;
        heapify();
        return temporary;
    }

    public void buildHeap() {
        int parent;
        for(int index = 1; index < arr.length; index++) {
            parent = (index - 1) / 2;
            int child = index;
            while(child != 0) {
                if (arr[parent] != null && arr[child] != null) {
                    if (arr[parent].getColor().getFrequency() > arr[child].getColor().getFrequency()) {
                        Node temporary = arr[parent];
                        arr[parent] = arr[child];
                        arr[child] = temporary;
                    }
                }
                child = parent;
                parent = (parent - 1) / 2;
            }
        }
    }

    public void heapify() {
        int parent = 0;
        int leftChild = 1;
        int rightChild = 2;
        while(leftChild < lastIndex && rightChild < lastIndex) {
            if (arr[leftChild].getColor().getFrequency() < arr[rightChild].getColor().getFrequency()) {
                if (arr[parent].getColor().getFrequency() > arr[leftChild].getColor().getFrequency()) {
                    swap(parent, leftChild);
                }
                parent = leftChild;
            } else {
                if (arr[parent].getColor().getFrequency() > arr[rightChild].getColor().getFrequency()) {
                    swap(parent, rightChild);
                }
                parent = rightChild;
            }
            leftChild = parent * 2 + 1;
            rightChild = parent * 2 + 2;
            if (leftChild < lastIndex && rightChild > lastIndex && arr[leftChild].getColor().getFrequency() < arr[lastIndex].getColor().getFrequency()) {
                swap(leftChild, parent);
            }
        }
    }

    public void swap(int i, int j) {
        Node temporary = arr[i];
        arr[i] = arr[j];
        arr[j] = temporary;
    }

    public void print() {
        System.out.println("Start: ");
        for (int index = 0; index < arr.length; index++) {
            if (arr[index] != null) {
                System.out.println(arr[index]);
            }
        }
        System.out.println("End");
        System.out.println("------------");
    }
    
    
}

