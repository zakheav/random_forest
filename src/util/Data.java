package util;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import com.csvreader.CsvReader;

public class Data {
	public List<String[]> data = new ArrayList<String[]>();

	public Data(String path) {
		data = new ArrayList<String[]>();
		try {
			CsvReader r = new CsvReader(path, ',', Charset.forName("GBK"));
			r.readRecord();
			while (r.readRecord()) {
				String[] row = r.getRawRecord().toString().split(",");
				data.add(row);
			}
			r.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
		System.out.println(data.size()+" 条数据已经加入内存");
	}

	public List<String[]> get_data() {
		return this.data;
	}
	
	public List<String[]> get_subData() {
		List<String[]> result = new ArrayList<String[]>();
		while(result.isEmpty()) {
			for(int i=0; i<data.size(); ++i) {
				if(Math.random() >= 0.2) {
					result.add(data.get(i));
				}
			}
		}
		return result;
	}
}
