package worker;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.log4j.PropertyConfigurator;
import random_forest.Tree;
import util.Data;
import util.JSON;
import util.TheSocket;
import util.XML;

public class Worker {
	public static List<Tree> treeList;
	public static boolean[] treeCond;
	public static String[] treeResult;
	private final int treeNum;
	private final String ip;
	private final int port;
	public static Data trainData;

	public Worker() {
		trainData = new Data("data//train.csv");
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
				String cmdString = theSocket.read();
				List<Object> cmd = cmdParser(cmdString);
				String cmdType = (String) cmd.get(0);
				if (cmdType.equals("train")) {
					train();
					theSocket.write("ok");
				} else if (cmdType.equals("run")) {

					List<String[]> samples = new ArrayList<String[]>();
					for (int i = 1; i < cmd.size(); ++i) {
						samples.add((String[]) cmd.get(i));
					}
					List<String[]> results = judge(samples);
					theSocket.write(ackBuilder(results));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void train() throws InterruptedException {
		treeCond = new boolean[treeNum];
		for (int i = 0; i < treeNum; ++i) {
			new Thread(new Train(i)).start();
		}
		boolean finish = false;
		while (!finish) {
			finish = true;
			for (int i = 0; i < treeNum; ++i) {
				if (!treeCond[i]) {
					finish = false;
				}
			}
			Thread.sleep(50);
		}
	}

	private List<String[]> judge(List<String[]> samples) throws InterruptedException {

		List<String[]> ack = new ArrayList<String[]>();
		for (String[] sample : samples) {
			treeCond = new boolean[treeNum];
			treeResult = new String[treeNum];
			for (int i = 0; i < treeNum; ++i) {
				new Thread(new Judge(i, sample)).start();
			}
			boolean finish = false;
			while (!finish) {
				finish = true;
				for (int i = 0; i < treeNum; ++i) {
					if (!treeCond[i]) {
						finish = false;
					}
				}
				Thread.sleep(50);
			}
			ack.add(treeResult);
		}
		return ack;
	}

	private List<Object> cmdParser(String cmd) {
		List<Object> temp = JSON.JSONToArray(cmd);
		List<Object> result = new ArrayList<Object>();
		for (int i = 0; i < temp.size(); ++i) {
			if (i == 0) {
				result.add(temp.get(i).toString());
			} else {
				String[] sample = temp.get(i).toString().split(",");
				result.add(sample);
			}
		}
		return result;
	}

	private String ackBuilder(List<String[]> ack) {
		List<Object> temp = new ArrayList<Object>();
		for (String[] result : ack) {
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < result.length; ++i) {
				if (i == result.length - 1) {
					sb.append(result[i]);
				} else {
					sb.append(result[i] + ",");
				}
			}
			temp.add(sb.toString());
		}
		return JSON.ArrayToJSON(temp);
	}

	public static void main(String[] args) throws InterruptedException {
		new Worker().run();
	}
}
