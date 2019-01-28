package com.keqiang.table.util;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 异步线程控制类
 *
 * @author 汪高皖
 */
@SuppressWarnings("ALL")
public class AsyncExecutor {
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int MAX_CACHE_POOL_SIZE = 2 * CPU_COUNT + 1;
    
    private static class Inner {
        private static final AsyncExecutor INSTANCE = new AsyncExecutor();
    }
    
    /**
     * 根据线程池类型获取不同类型的线程池，如果指定类型不对，则返回null
     *
     * @return 返回Executor对象
     */
    public static AsyncExecutor getInstance() {
        return Inner.INSTANCE;
    }
    
    private ExecutorService mExecutor;
    
    private AsyncExecutor() {
        mExecutor = new ThreadPoolExecutor(
            0,
            MAX_CACHE_POOL_SIZE,
            60L, TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>(),
            new DefaultThreadFactory("table-lite"),
            new ThreadPoolExecutor.CallerRunsPolicy());
    }
    
    public void execute(Runnable runnable) {
        if (isShutdown()) {
            mExecutor = new ThreadPoolExecutor(
                0,
                MAX_CACHE_POOL_SIZE,
                60L, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(),
                new DefaultThreadFactory("table-lite"),
                new ThreadPoolExecutor.CallerRunsPolicy());
        }
        
        mExecutor.execute(runnable);
    }
    
    public void shutdown() {
        mExecutor.shutdown();
    }
    
    public List<Runnable> shutdownNow() {
        return mExecutor.shutdownNow();
    }
    
    public boolean isShutdown() {
        return mExecutor.isShutdown();
    }
    
    private static class DefaultThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;
        
        DefaultThreadFactory(String poolName) {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                Thread.currentThread().getThreadGroup();
            namePrefix = poolName + " pool-" +
                poolNumber.getAndIncrement() +
                "-thread-";
        }
        
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                namePrefix + threadNumber.getAndIncrement(),
                0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }
}


