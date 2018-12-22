package com.github.urlv.utils;

public class IntWrapper {
    private int value;

    public int get() {
        return this.value;
    }

    public IntWrapper set(int value) {
        this.value = value;
        return this;
    }

    public IntWrapper add(int value) {
        this.value += value;
        return this;
    }
}
