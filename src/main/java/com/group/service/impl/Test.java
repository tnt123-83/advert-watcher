package com.group.service.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {
    public static void main(String[] args) {

        String type = "$";
        System.out.println("1000$ ".matches("\\$"));
        System.out.println("1000$ ".split("\\" + type).length);
        String a = "7 200 $";
        Pattern p = Pattern.compile("\\D*(\\d+[\\d|\\s]*\\d*\\$?)\\D*");

        Matcher m = p.matcher(a);
        //a="jjj";
        m.find();
        String number = m.group(1);
        System.out.println(m.groupCount());
        System.out.println(number);
//        final CountDownLatch latch = new CountDownLatch(1);
//        //Boolean isTaskFinished = false;
//        final ArrayDeque<Integer> queue = new ArrayDeque<>();
//        final Random random = new Random();
//        Thread task1 = new Thread("Task1") {
//            @Override
//            public void run() {
//                for (int i = 0; i < 100; i++) {
//                    try {
//                        synchronized (queue) {
//                            queue.addLast(i);
//                            System.out.println("Added " + i);
//                        }
//                        Thread.sleep(random.nextInt(1000));
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//                latch.countDown();
//            }
//        };
//        Thread task2 = new Thread("UIHandler") {
//            @Override
//            public void run() {
//                while (latch.getCount() > 0 || !queue.isEmpty()) {
//                    try {
//                        synchronized (queue) {
//                            while (!queue.isEmpty()) System.out.println("Processed item " + queue.poll());
//                        }
//                        Thread.sleep(random.nextInt(2000));
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                //while (!queue.isEmpty()) System.out.println("Processed item " + queue.poll());
//            }
//        };
//        task1.start();
//        task2.start();
    }
}
