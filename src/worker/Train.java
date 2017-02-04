package worker;

import random_forest.Tree;

public class Train implements Runnable {
	private int no;
	public Train(int no) {
		this.no = no;
	}
	@Override
	public void run() {// 生成一棵决策树
		// 生成决策树的过程
		// 获取数据
		Tree tree = new Tree(Worker.trainData.get_subData());
		Worker.treeList.set(no, tree);
		Worker.treeCond[no] = true;
	}
}
