package com.xiaobai.zkclient;

import java.util.List;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;

/**
 * Hello world!
 *
 */
public class ZClient 
{
	
	private void client() {
		ZkClient client = new ZkClient("127.0.0.1:2181", 5000);
		String path = "/zk-book/cl";
		client.createPersistent(path, true);
	}
	
	private void listener() throws InterruptedException {
		ZkClient client = new ZkClient("127.0.0.1:2181", 5000);
		String path = "/zk-book11";
		client.subscribeChildChanges(path, new IZkChildListener() {
			
			public void handleChildChange(String parentPath, List<String> currentChildren) throws Exception {
				System.out.println(parentPath + "'s children changed,current children:" + currentChildren);
			}
		});
		
		//节点是否存在
		System.out.println("Node Exists:" + client.exists(path));
		
		//节点不存在也可通知：
		client.createPersistent(path);
		Thread.sleep(1000);
		
		System.out.println("Node Exists:" + client.exists(path));
		
		//获取子节点
		System.out.println(client.getChildren(path));
		
		client.createPersistent(path + "/cl");
		Thread.sleep(1000);
		
		client.delete(path + "/cl");
		Thread.sleep(1000);
		
		client.delete(path);
		Thread.sleep(Integer.MAX_VALUE);
	}
	
	private void read() throws InterruptedException {
		ZkClient client = new ZkClient("127.0.0.1:2181", 5000);
		String path = "/zk-book222";
		//节点是否存在
		System.out.println("Node Exists:" + client.exists(path));
		client.createEphemeral(path, "123");//数据为123
		System.out.println("Node Exists:" + client.exists(path));
		client.subscribeDataChanges(path, new IZkDataListener() {
			
			public void handleDataDeleted(String dataPath) throws Exception {
				System.out.println("Node" + dataPath + " deleted");
			}
			
			public void handleDataChange(String dataPath, Object data) throws Exception {
				System.out.println("Node" + dataPath + " changed,new data:" + data);
			}
		});
		
		System.out.println(client.readData(path));//获取节点数据
		
		client.writeData(path, "456");//写数据
		Thread.sleep(1000);
		
		client.delete(path);
		Thread.sleep(Integer.MAX_VALUE);
	}
	
    public static void main( String[] args ) throws InterruptedException
    {
    	ZClient cl = new ZClient();
//    	cl.client();
//    	cl.listener();
    	cl.read();
        System.out.println( "Hello World!" );
    }
}
