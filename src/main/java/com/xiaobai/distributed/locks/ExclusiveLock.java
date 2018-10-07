package com.xiaobai.distributed.locks;

import com.xiaobai.distributed.affair.Affair;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class ExclusiveLock {

    private static AtomicLong num = new AtomicLong(0l);
    private ZkClient zkClient;
    private Affair affair;//封装每个锁用户各自的事务，在锁逻辑中执行，因为这个锁是逻辑上的，与Java真正的锁不同！！
    private static final Logger log = LoggerFactory.getLogger(ExclusiveLock.class);

    public void build(ZkClient client,Affair af) {
        this.zkClient = client;
        this.affair = af;
    }

    /**
     * parentPath:"/exclusive_lock"，需要是事先创建、已存在的永久节点（临时节点下不能再创建临时节点），监听的是它的子节点列表变化事件
     * path:"/exclusive_lock/lock"，永久节点下全路径的临时节点，就是我们的分布式锁
     * @param parentPath
     * @param path
     * @return
     */
    public boolean lock(final String parentPath, final String path) {
        if(zkClient == null || affair == null) {
            log.error("Lock Not Builded With A Solo ZkClient And A Solo Affair!");
            return false;
        }
        try {
            log.info(Thread.currentThread().getName() + "----------------------------------------------------------->>tryLock...");
            //创建临时节点（全路径，且不支持递归，需要父节点已经创建好，且父节点为永久节点；不要与节点赋值Data方法弄混！）
            zkClient.createEphemeral(path);//事实上，不可能有线程并发执行下面的代码，因为这一句只有一个线程会成功，向下执行；况且这里是模拟分布式多节点，实际代码部署在多机器，彼此是不可能有线程安全关系的！分布式锁本身就是在非本机多线程环境下解决共享资源安全问题！
//            Thread.sleep(1000);//ZK Server端异步过程，所以休息一秒保证节点创建完成
            log.info(Thread.currentThread().getName() + "======================>>>>>>>----------------------------------------->" + num.incrementAndGet());
            log.info(Thread.currentThread().getName() + "----->Path " + path + " created!");
            log.info(Thread.currentThread().getName() + ":I've got the fucking lock!!!!!!!!!!!!!Congratulations!!!!!!!!!!!!");
            //执行事务逻辑
            affair.execute();
            Thread.sleep(2000);
            //释放锁
            unlock(path);//删除的是这个临时节点，也就是锁，注意一定不要递归删除，也不要删除其父节点（永久节点，被监听节点）！！
            zkClient.unsubscribeAll();
            zkClient.close();//及时关闭客户端！！--临时节点自动删除
            return true;
        }catch (Exception e) {
            System.out.println(Thread.currentThread().getName() + "======================>>Reach Here:error------>" + e.getMessage());
            zkClient.unsubscribeAll();//先取消所有此前订阅！！
            //失败，则注册监听--经测试，回调方法完成前，这里的执行线程应持续不退出（调用了长久睡眠），原理待查（让线程中的ZkClient存活？保持连接，服务端对客户端的异步远程调用？因为一切变动在服务端，且一定是异步，但具体采用什么线程调用不得而知，远程调用原理是本地线程执行本地代码，执行线程已经睡眠，应为ZkClient线程执行回调，该线程可以是守护线程，随执行线程退出而退出）
            /**
             * 已通过线程打印证实上述部分猜测：
             * 这里的测试程序，只有最初获取到锁并执行其事务的那一个线程是我们自己创建的，成功获取锁并执行事务的打印如下：
             * pool-1-thread-13======================>>>>>>>----------------------------------------->1
             * 其他均是我们的线程注册了监听，打印获取锁失败如下：
             * pool-1-thread-19======================>>Reach Here:error------>org.apache.zookeeper.KeeperException$NodeExistsException: KeeperErrorCode = NodeExists for /exclusive_lock/lock
             * 这以后再也没出现过我们自己的线程：
             * 此后在各自执行线程不退出（长久睡眠）的情况下，由各自独立的ZkClient线程（大概为守护线程）再次尝试获取锁：
             * ZkClient-EventThread-41-127.0.0.1:2181----------------------------------------------------------->>tryLock...
             * 获取锁成功的：
             * ZkClient-EventThread-34-127.0.0.1:2181======================>>>>>>>----------------------------------------->3
             * 获取失败的：
             * ZkClient-EventThread-43-127.0.0.1:2181======================>>Reach Here:error------>org.apache.zookeeper.KeeperException$NodeExistsException: KeeperErrorCode = NodeExists for /exclusive_lock/lock
             * 此后全是ZkClient线程逐一获取锁，执行事务成功，再也没有出现我们的线程，直到全部结束！！
             */
            //注意！！这里监听的是锁的父节点（必须是一个事先已存在的永久节点）的孩子列表！！
            zkClient.subscribeChildChanges(parentPath, new IZkChildListener() {

                public void handleChildChange(String parentPath, List<String> currentChildren) throws Exception {
                    System.out.println(Thread.currentThread().getName() + "======================>>" + parentPath + "'s children changed,current children:" + currentChildren);
                    lock(parentPath,path);//递归调用，继续尝试获取锁，利用监听达到异步循环获取效果--只要没有获取锁执行事务，就不能关闭此客户端！！
                }
            });
            return false;
        }
    }

    private void unlock(String path) {
        if(zkClient == null || affair == null) {
            log.error("Lock Not Builded With A Solo ZkClient And A Solo Affair!");
            return;
        }
        try {
            boolean un = zkClient.delete(path);//删除节点--造成无法创建递归路径
//            Thread.sleep(1000);
            if (un){
                log.info(Thread.currentThread().getName() + ":Lock Deleted By Me!!!!!!!!!!");
            }
        }catch (Exception e){
            e.printStackTrace();
            log.error("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~============"+ e.getMessage());
        }
    }

}
