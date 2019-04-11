package com.su.wx.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadUtil {

    private static ThreadUtil instance;

    ExecutorService executorService;

    private ThreadUtil(){
        executorService=Executors.newFixedThreadPool(10);
    }

    public static ThreadUtil getInstance(){
        if(instance==null){
            synchronized (ThreadUtil.class){
                if(instance==null){
                    instance=new ThreadUtil();
                }
            }
        }
        return instance;
    }

    public void execute(Runnable runnable){
        executorService.execute(runnable);
    }
}
