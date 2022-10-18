package com.artemchep.basics_multithreading.cipher;

import android.util.Log;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;

public class CypherManager<T extends Runnable> {
    private final Queue<T> tasksQueue = new LinkedList<>();
    private volatile boolean isRunning = true;
    public void start() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isRunning) {
                    T task = getTask();
                    if (task != null) {
                        task.run();
                    }
                }
            }
        });
        thread.start();
    }

    private synchronized T getTask() {
        while (tasksQueue.isEmpty() && isRunning) {
            try {
                Log.d("TAGGGGG", "run: I'm empty");
                wait();
            } catch (Exception e) {
                Log.d("Exception", "Got exception: " + e);
            }
        }
        return tasksQueue.poll();
    }

    public synchronized void postTask(T task) {
        boolean res = tasksQueue.offer(task);
        Log.d("TAGGGGG", "postTask: " + res);
        Log.d("TAGGGGG", "postTask: " + Thread.currentThread().getState().name());
        notify();
    }

    public synchronized void stop() {
        isRunning = false;
        notify();
    }
}
