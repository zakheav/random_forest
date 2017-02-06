package master;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.PropertyConfigurator;

import util.Data;
import util.JSON;
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
		String cmd = cmdBuilder("train", null);
		for (int i = 0; i < ackList.size() && !workerCrash; ++i) {
			ackList.set(i, null);
			if (socketList.get(i) != null)
				ThreadPool.get_instance().add_tasks(new Send_msg_wait_ack(socketList.get(i), cmd, i));
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

	public List<String> start_running(List<String[]> samples) throws InterruptedException {
		List<String> results = new ArrayList<String>();
		String cmd = cmdBuilder("run", samples);
		for (int i = 0; i < ackList.size(); ++i) {
			ackList.set(i, null);
			ThreadPool.get_instance().add_tasks(new Send_msg_wait_ack(socketList.get(i), cmd, i));
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
		if (!workerCrash) {
			List<List<String[]>> temp = new ArrayList<List<String[]>>();
			for (String ack : ackList) {
				temp.add(ackParser(ack));
			}
			int resultsNum = temp.get(0).size();
			for (List<String[]> workerResult : temp) {
				if (workerResult.size() != resultsNum) {
					workerCrash = true;
				}
			}
			if (!workerCrash) {
				for (int i = 0; i < resultsNum; ++i) {
					Map<String, Integer> resultsCounter = new HashMap<String, Integer>();
					for (int j = 0; j < workersNum; ++j) {
						for (String r : temp.get(j).get(i)) {
							if (resultsCounter.containsKey(r)) {
								int oldNum = resultsCounter.get(r);
								resultsCounter.put(r, oldNum + 1);
							} else {
								resultsCounter.put(r, 1);
							}
						}
					}
					int maxNum = 0;
					String result = "";
					for (String key : resultsCounter.keySet()) {
						int counter = resultsCounter.get(key);
						if (counter > maxNum) {
							maxNum = counter;
							result = key;
						}
					}
					results.add(result);
				}
			} else {
				System.out.println("run fail, worker crash 1!");
			}
		} else
			System.out.println("run fail, worker crash 2!");
		return results;
	}

	private String cmdBuilder(String type, List<String[]> samples) {
		List<Object> temp = new ArrayList<Object>();
		temp.add(type);
		if (type.equals("run")) {
			for (String[] sample : samples) {
				StringBuffer sb = new StringBuffer();
				for (int i = 0; i < sample.length; ++i) {
					if (i == sample.length - 1) {
						sb.append(sample[i]);
					} else {
						sb.append(sample[i] + ",");
					}
				}
				temp.add(sb.toString());
			}
		}
		return JSON.ArrayToJSON(temp);
	}

	private List<String[]> ackParser(String ack) {
		List<Object> temp = JSON.JSONToArray(ack);
		List<String[]> result = new ArrayList<String[]>();
		for (int i = 0; i < temp.size(); ++i) {
			result.add(temp.get(i).toString().split(","));
		}
		return result;
	}

	public static void main(String[] args) throws InterruptedException {
		Master master = new Master();
		master.start_training();

		// 得到需要分析的样本列表
		Data data = new Data("data//samples.csv");
		List<String[]> samples = data.get_data();
		List<String> results = master.start_running(samples);
		for (String r : results) {
			System.out.println(r);
		}
	}
}
