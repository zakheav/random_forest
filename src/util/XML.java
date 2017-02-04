package util;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class XML {
	public Map<String, String> workerConf() {
		Map<String, String> r = new HashMap<String, String>();
		SAXReader reader = new SAXReader();
		try {
			Document document = reader.read(new File("conf/conf.xml"));
			Element root = document.getRootElement();
			Element worker = root.element("worker");
			String workerNum = worker.element("num").getText();
			String treeNum = worker.element("treeNum").getText();
			r.put("num", workerNum);
			r.put("treeNum", treeNum);
			return r;
		} catch (DocumentException e) {
			e.printStackTrace();
			return null;
		}
	}

	public Map<String, String> masterConf() {
		Map<String, String> r = new HashMap<String, String>();
		SAXReader reader = new SAXReader();
		try {
			Document document = reader.read(new File("conf/conf.xml"));
			Element root = document.getRootElement();
			Element master = root.element("master");
			String ip = master.element("ip").getText();
			String port = master.element("port").getText();
			r.put("ip", ip);
			r.put("port", port);
			return r;
		} catch (DocumentException e) {
			e.printStackTrace();
			return null;
		}
	}
}
