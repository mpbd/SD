package utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import com.google.gson.Gson;

public class Zookeeper {

	private ZooKeeper zookeeper;
	private static final int TIMEOUT = 5000;
	private CountDownLatch connectedSignal = new CountDownLatch(1);

	public Zookeeper(String servers) throws Exception {
		this.connect(servers, TIMEOUT);
	}

	private ZooKeeper getZooKeeper() {
		if (zookeeper == null || !zookeeper.getState().equals(ZooKeeper.States.CONNECTED)) {
			throw new IllegalStateException("ZooKeeper is not connected.");
		}
		return zookeeper;
	}


	private void connect(String host, int timeout) throws IOException, InterruptedException {
		zookeeper = new ZooKeeper(host, timeout, new Watcher() {
			@Override
			public void process(WatchedEvent event) {
				if (event.getState().equals(Watcher.Event.KeeperState.SyncConnected)) {
					connectedSignal.countDown();
				}
			}

		});
		connectedSignal.await();
	}
	
	public void saveValue(String path, Object value) {
		try {
			int i = path.lastIndexOf('/');
			String parent = i < 0 ? path : path.substring(0, i);

			Stat stat = getZooKeeper().exists(parent, false);
			if (stat == null)
				getZooKeeper().create(parent, ".root".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

			byte[] data = new Gson().toJson(value).getBytes();

			stat = getZooKeeper().exists(path, false);
			if (stat == null)
				getZooKeeper().create(path, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			else
				getZooKeeper().setData(path, data, -1);
			
		} catch (Exception x) {
			x.printStackTrace();
		}
	}

	public <T> List<T> listValues(String path) {
		try {
			List<T> res = new ArrayList<T>();
			for (String child : getZooKeeper().getChildren(path, false)) {
				String nodePath = path + "/" + child;
				byte[] data = getZooKeeper().getData(nodePath, false, new Stat());
				System.err.println(new String(data));
			}
			return res;
		} catch (Exception x) {
			x.printStackTrace();
		}
		return Collections.emptyList();
	}
	
	
	public static void main(String[] args ) throws Exception {
		
		Zookeeper zk = new Zookeeper("zoo1,zoo2,zoo3");
		
		String root = "/";
		
		for( String node : Arrays.asList("node1", "node2", "node3", "node4"))
			zk.saveValue(root + "/" + node, "valueOf-" + node);
		
		zk.listValues( root );
	}
}
