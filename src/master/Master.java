package master;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.PropertyConfigurator;

import util.TheSocket;
import util.ThreadPool;
import util.XML;

public class Master {
	private final int workersNum;// 记录worker集群的健康状态
	public static boolean workerCrash;
	private ServerSocket serverSocket;
	public static List<TheSocket> socketList;
	public static List<String> ackList;

	public Master() {
		PropertyConfigurator.configure("conf/log4j.properties");
		workerCrash = false;
		socketList = new ArrayList<TheSocket>();
		XML xml = new XML();
		Map<String, String> workerConf = xml.workerConf();
		workersNum = Integer.parseInt(workerConf.get("num"));

		ackList = new ArrayList<String>();
		for (int i = 0; i < workersNum; ++i) {
			ackList.add(null);
		}

		Map<String, String> masterConf = xml.masterConf();
		int port = Integer.parseInt(masterConf.get("port"));
		String ip = masterConf.get("ip");
		try {
			serverSocket = new ServerSocket(port, 50, InetAddress.getByName(ip));
			int counter = 0;
			while (counter < workersNum) {
				Socket socket = serverSocket.accept();
				socketList.add(new TheSocket(socket));
				++counter;
				Thread.sleep(10);
			}
			System.out.println("workers are all online");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void start_training() throws InterruptedException {
		for (int i = 0; i < ackList.size() && !workerCrash; ++i) {
			ackList.set(i, null);
			if (socketList.get(i) != null)
				ThreadPool.get_instance().add_tasks(new Send_msg_wait_ack(socketList.get(i), "train", i));
		}

		boolean finish = false;
		while (!finish) {
			if (workerCrash) {
				finish = true;
			} else {
				finish = true;
				for (String ack : ackList) {
					if (ack == null) {
						finish = false;
					}
				}
			}
			Thread.sleep(1000);
		}
		if (!workerCrash)
			System.out.println("train finished!");
		else
			System.out.println("train fail, worker crash!");
	}

	public void start_running() throws InterruptedException {
		for (int i = 0; i < ackList.size(); ++i) {
			ackList.set(i, null);
			ThreadPool.get_instance().add_tasks(new Send_msg_wait_ack(socketList.get(i), "run", i));
		}

		boolean finish = false;
		while (!finish) {
			if (workerCrash) {
				finish = true;
			} else {
				finish = true;
				for (String ack : ackList) {
					if (ack == null) {
						finish = false;
					}
				}
			}
			Thread.sleep(1000);
		}
		if (!workerCrash)
			System.out.println("run finished!");
		else
			System.out.println("run fail, worker crash!");
	}
	
	public static void main(String[] args) throws InterruptedException {
		Master master = new Master();
		master.start_training();
		master.start_training();
	}
}
