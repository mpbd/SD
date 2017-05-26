package utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import com.google.gson.Gson;

import api.Endpoint;

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

	public List<Endpoint> listValues(String path) {
		try {
			List<Endpoint> res = new ArrayList<Endpoint>();
			for (String child : getZooKeeper().getChildren(path, false)) {
				String nodePath = path + "/" + child;
				byte[] data = getZooKeeper().getData(nodePath, false, new Stat());
				Endpoint endp = new Gson().fromJson(data.toString(), Endpoint.class);
				res.add(endp);
				//System.err.println("---> " + new String(data));
			}
			return res;
		} catch (Exception x) {
			x.printStackTrace();
		}
		return Collections.emptyList();
	}
	
	public boolean contains(String path){
		Stat stat = null;
		
		try {
			stat =  getZooKeeper().exists(path, false);
		} catch (KeeperException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		return (stat != null);
		
	}
}
