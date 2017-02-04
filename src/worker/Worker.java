package worker;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.log4j.PropertyConfigurator;
import random_forest.Tree;
import util.TheSocket;
import util.XML;

public class Worker {
	public static List<Tree> treeList;
	public static boolean[] treeCond;
	private final int treeNum;
	private final String ip;
	private final int port;
	public static TrainData trainData;
	public Worker() {
		trainData = new TrainData();
		PropertyConfigurator.configure("conf/log4j.properties");
		XML xml = new XML();
		Map<String, String> masterConf = xml.masterConf();
		ip = masterConf.get("ip");
		port = Integer.parseInt(masterConf.get("port"));
		Map<String, String> workerConf = xml.workerConf();
		treeNum = Integer.parseInt(workerConf.get("treeNum"));
		treeList = new ArrayList<Tree>();
		for (int i = 0; i < treeNum; ++i) {
			treeList.add(null);
		}
		treeCond = new boolean[treeNum];
	}

	public void run() throws InterruptedException {
		try {
			Socket socket;
			socket = new Socket(ip, port);
			TheSocket theSocket = new TheSocket(socket);
			while (true) {
				String cmd = theSocket.read();
				if (cmd.equals("train")) {
					train();
					theSocket.write("ok");
				} else if (cmd.equals("run")) {
					theSocket.write("finish");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void train() throws InterruptedException {
		for(int i=0; i<treeNum; ++i) {
			new Thread(new Train(i)).start();
		}
		boolean finish = false;
		while(!finish) {
			finish = true;
			for(int i=0; i<treeNum; ++i) {
				if(!treeCond[i]) {
					finish = false;
				}
			}
			Thread.sleep(50);
		}
		treeCond = new boolean[treeNum];
	}
	
	private void judge() {
		
	}
	
	public static void main(String[] args) throws InterruptedException {
		new Worker().run();
	}
}
