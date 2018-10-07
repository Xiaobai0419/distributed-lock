package com.xiaobai.distributed.utils;

import java.util.concurrent.*;

public class ThreadPoolUtil {

    public static ThreadPoolExecutor createThreadPool() {

        return new ThreadPoolExecutor(20,50,100L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(50),Executors.defaultThreadFactory(),new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public static ThreadPoolExecutor createThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {

        return new ThreadPoolExecutor(corePoolSize,maximumPoolSize,keepAliveTime, TimeUnit.MILLISECONDS,
                workQueue,handler);
    }

    public static ThreadPoolExecutor createThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory,RejectedExecutionHandler handler) {

        return new ThreadPoolExecutor(corePoolSize,maximumPoolSize,keepAliveTime, TimeUnit.MILLISECONDS,
                workQueue,threadFactory,handler);
    }
}
