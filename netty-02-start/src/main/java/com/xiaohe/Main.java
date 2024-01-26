package com.xiaohe;

public class Main {
    private static int i = 0;
    private static Object lock = new Object();

    public static void main(String[] args) {
        new Thread(() -> {
            while (i < 100) {
                synchronized (lock) {
                    lock.notifyAll();
                    if (i < 100) {
                        System.out.println(Thread.currentThread().getName() + " " + (i++));
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    lock.notifyAll();

                }
            }

        }, "线程1").start();

        new Thread(() -> {
            while (i < 100) {
                synchronized (lock) {
                    lock.notifyAll();
                    if (i < 100) {
                        System.out.println(Thread.currentThread().getName() + " " + (i++));
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    lock.notifyAll();
                }
            }
        }, "线程2").start();
    }
}