package com.xiaobai.distributed.locks;

import com.xiaobai.distributed.affair.impl.MainAffair;
import com.xiaobai.distributed.affair.impl.SubAffair;
import com.xiaobai.distributed.utils.ThreadPoolUtil;
import org.I0Itec.zkclient.ZkClient;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

public class TestLocks {

    private String parentPath = "/exclusive_lock";//需要是事先创建、已存在的永久节点（临时节点下不能再创建临时节点）
    private String path = "/exclusive_lock/lock";//锁是同一个，但ZkClient不能使用一个，是每个线程一个客户端（ZK客户端不同，ZK服务端相同），争夺（同一个服务端）同一个锁！！
    private String zkServerIp = "127.0.0.1:2181";//服务端要相同！！
    private static final Logger log = LoggerFactory.getLogger(TestLocks.class);

    @Test
    public void testExclusiveLock() throws InterruptedException {
        ExclusiveLock lock = new ExclusiveLock();//创建一个锁，用于特定资源
        //主线程绑定客户端--重要！！不同的线程客户端不同，争夺同一把锁！！
        lock.build(new ZkClient(zkServerIp, 5000),new MainAffair());//新建客户端！！（每个线程只新建一次客户端，执行完事务才关闭，否则异步轮询获取锁）服务端和Path相同！！
        Runnable runnable = new Runnable() {
            public void run() {
                //注意这里一定要创建各自新的Lock,绑定自己的客户端！！只要保证是锁住同一个服务端同一个路径就行！这与Java的锁不同！！
                ExclusiveLock lock = new ExclusiveLock();//创建一个锁，用于特定资源
                lock.build(new ZkClient(zkServerIp, 5000),new SubAffair());//新建客户端！！服务端和Path相同！！
                lock.lock(parentPath,path);//锁中执行事务、执行成功释放锁、没获取到锁则异步获取锁、直到执行成功释放锁
                //这句是要保证每个子线程不退出，才能顺利执行完回调逻辑，最终每个线程（模拟分布式各个节点）获取锁执行完其模拟的分布式事务
                try {
                    Thread.sleep(Integer.MAX_VALUE);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        ThreadPoolExecutor executor = ThreadPoolUtil.createThreadPool(20,50,100l,new ArrayBlockingQueue<Runnable>(50),new ThreadPoolExecutor.CallerRunsPolicy());
        for(int j=0;j<20;j++) {
            executor.submit(runnable);
//            Thread thread = new Thread(runnable);
//            thread.start();
        }
        lock.lock(parentPath,path);//锁中执行事务、执行成功释放锁、没获取到锁则异步获取锁、直到执行成功释放锁
//        Thread.sleep(Integer.MAX_VALUE);//JUnit测试程序需要这里保证@Test测试主线程不退出，才能执行完全部线程的回调，main函数则不需要如此！！
    }

    public static void main(String[] args) throws InterruptedException {
        TestLocks test = new TestLocks();
        test.testExclusiveLock();
    }
}
