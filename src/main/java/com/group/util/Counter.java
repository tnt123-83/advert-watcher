package com.group.util;

public class Counter {
    private int count = 0;

    public Counter() {
    }

    public Counter(int i) {
        this.count = i;
    }

    public void countDown(){
        count--;
    }
    public void countUp(){
        count++;
    }

    public int getCount() {
        return count;
    }
}