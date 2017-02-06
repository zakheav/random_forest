package util;

import java.util.ArrayList;
import java.util.List;
import net.sf.json.JSONArray;

public class JSON {
	public static String ArrayToJSON(List<Object> list) {
		JSONArray jsonArray = JSONArray.fromObject(list);
		return jsonArray.toString();
	}

	public static List<Object> JSONToArray(String json) {
		List<Object> result = new ArrayList<Object>();
		JSONArray jsonArray = JSONArray.fromObject(json);
		for (Object object : jsonArray) {
			result.add(object);
		}
		return result;
	}

	public static void main(String[] args) {
		List<Object> list = new ArrayList<Object>();
		list.add("hahaha");
		System.out.println(JSON.ArrayToJSON(list));
	}
}
