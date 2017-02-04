package random_forest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class TreeNode {
	public boolean leaf;
	public int classificationFeatureIdx;
	public Map<String, TreeNode> children;
	public String label;// 叶子节点存储标签类型
}

public class Tree {

	private TreeNode root;
	
	public Tree(List<String[]> trainData) {
		root = build_node(trainData);
	}

	private double get_empiricalEntropy(List<String[]> dataSet) {// 计算信息熵
		int dataNum = dataSet.size();
		Map<String, Integer> labelMap = new HashMap<String, Integer>();// 记录每个类的数目
		for (int i = 0; i < dataNum; ++i) {
			String label = (dataSet.get(i))[0];
			if (labelMap.containsKey(label)) {
				int oldNum = labelMap.get(label);
				labelMap.put(label, oldNum + 1);
			} else {
				labelMap.put(label, 1);
			}
		}
		double shannonEntropy = 0.0;// 信息熵
		for (String label : labelMap.keySet()) {
			double prob = (double) (labelMap.get(label)) / dataNum;
			shannonEntropy -= prob * (Math.log(prob) / Math.log(2.0));
		}

		return shannonEntropy;
	}

	private Map<String, Object> get_informationGain(List<String[]> dataSet, int featureIdx) {// 计算信息增益，dataSet的划分结果
		double empiricalEnptopy = get_empiricalEntropy(dataSet);
		Map<String, List<String[]>> subDatasetSet = new HashMap<String, List<String[]>>();// 根据指定的特征对dataSet进行划分
		for (String[] data : dataSet) {
			String labelValue = data[featureIdx];
			if (subDatasetSet.containsKey(labelValue)) {
				subDatasetSet.get(labelValue).add(data);
			} else {
				subDatasetSet.put(labelValue, new ArrayList<String[]>());
				subDatasetSet.get(labelValue).add(data);
			}
		}

		double empiricalConditionalEntropy = 0.0;// 条件经验熵
		for (String labelValue : subDatasetSet.keySet()) {
			double prob = (double) subDatasetSet.get(labelValue).size() / dataSet.size();
			empiricalConditionalEntropy += prob * get_empiricalEntropy(subDatasetSet.get(labelValue));
		}

		Map<String, Object> result = new HashMap<String, Object>();
		result.put("informationGain", empiricalEnptopy - empiricalConditionalEntropy);
		result.put("subDatasetSet", subDatasetSet);
		return result;
	}

	private String get_majorityLabel(List<String[]> dataSet) {
		Map<String, Integer> labelMap = new HashMap<String, Integer>();// 记录每个类的数目
		for (int i = 0; i < dataSet.size(); ++i) {
			String label = (dataSet.get(i))[0];
			if (labelMap.containsKey(label)) {
				int oldNum = labelMap.get(label);
				labelMap.put(label, oldNum + 1);
			} else {
				labelMap.put(label, 1);
			}
		}
		int max = 0;
		String majorityLabel = "";
		for (String label : labelMap.keySet()) {
			if (max < labelMap.get(label)) {
				max = labelMap.get(label);
				majorityLabel = label;
			}
		}
		return majorityLabel;
	}
	
	private void build_node(List<String[]> dataSet, TreeNode node, Set<Integer> featureIdxSet) {
		if(featureIdxSet.isEmpty()) {
			node.leaf = true;
			node.classificationFeatureIdx = -1;
			node.children = null;
			node.label = get_majorityLabel(dataSet);
		} else {
			double biggestInfoGain = 0.0;// 最大信息增益
			Map<String, List<String[]>> bestDatasetDivide = null;// 最佳的数据集划分
			int bestFeatureIdx = -1;
			for(int featureIdx : featureIdxSet) {// 寻找最佳的划分属性
				Map<String, Object> result = get_informationGain(dataSet, featureIdx);
				double infoGain = (Double)result.get("informationGain");
				@SuppressWarnings("unchecked")
				Map<String, List<String[]>> datasetDivide = (Map<String, List<String[]>>)result.get("subDatasetSet");
				if(infoGain > biggestInfoGain) {
					bestFeatureIdx = featureIdx;
					biggestInfoGain = infoGain;
					bestDatasetDivide = datasetDivide;
				}
			}
			if(biggestInfoGain < 0.01) {
				node.leaf = true;
				node.classificationFeatureIdx = -1;
				node.children = null;
				node.label = get_majorityLabel(dataSet);
			} else {
				featureIdxSet.remove(bestFeatureIdx);// 删除已选择的特征
				node.leaf = false;
				node.classificationFeatureIdx = bestFeatureIdx;
				node.children = new HashMap<String, TreeNode>();
				node.label = get_majorityLabel(dataSet);
				for(String label : bestDatasetDivide.keySet()) {
					node.children.put(label, new TreeNode());
					build_node(bestDatasetDivide.get(label), node.children.get(label), featureIdxSet);// 构建子节点
				}
			}
		}
	}
	
	private TreeNode build_node(List<String[]> dataSet) {
		root = new TreeNode();
		Set<Integer> featureIdxSet = new HashSet<Integer>();
		while(featureIdxSet.isEmpty()) {
			for(int i=1; i<dataSet.get(0).length; ++i) {// 随机找一些特征
				if(Math.random() > 0.6) {
					featureIdxSet.add(i);
				}
			}
		}
		build_node(dataSet, root, featureIdxSet);
		return root;
	}
	
	public String judge(String[] sample) {
		TreeNode node = root;
		boolean finish = false;
		String result = "";
		while(!finish) {
			if(node.leaf) {
				finish = true;
				result = node.label;
			} else {
				int classificationFeatureIdx = node.classificationFeatureIdx;
				String featureValue = sample[classificationFeatureIdx];
				if(node.children.containsKey(featureValue)) {
					node = node.children.get(featureValue);
				} else {
					finish = true;
					result = node.label;
				}
			}
		}
		return result;
	}
}
