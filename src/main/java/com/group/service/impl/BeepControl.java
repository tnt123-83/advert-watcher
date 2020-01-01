package com.group.service.impl;

import javafx.concurrent.Task;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import static java.util.concurrent.TimeUnit.*;

public class BeepControl {
    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1);

    public void beepForAnHour() {
        final Task<Void> beeper = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                System.out.println("beep");
                return null;
            }

        };
        final ScheduledFuture<?> beeperHandle =
                scheduler.scheduleAtFixedRate(beeper, 0, 3, SECONDS);
    }

    public static void main(String[] args) {
        System.out.println("beep");

        new BeepControl().beepForAnHour();
    }
}