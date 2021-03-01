package com.utils;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class Demo {
    static volatile boolean runState = true;
    static BlockingQueue<Integer> Q = new LinkedBlockingQueue<>();
    static volatile boolean inReactor = false;

    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(() -> {
            while (true) {
                inReactor = true;
                try {
                    Integer taskId;
                    if (!(runState && (taskId = Q.poll(5, TimeUnit.SECONDS)) != null)) {
                        inReactor = false;
                        break;
                    }
                    System.out.println("执行任务:" + taskId);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });


        Thread t2 = new Thread(() -> {
            try {
                //2秒后关停
                Thread.sleep(2000);
                runState = false;

                //5秒后又开启
                Thread.sleep(5000);
                runState = true;


            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        t1.start();
        t2.start();

    }


}
