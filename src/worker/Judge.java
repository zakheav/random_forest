package worker;

public class Judge implements Runnable {
	private int no;
	private String[] sample;

	public Judge(int no, String[] sample) {
		this.no = no;
		this.sample = sample;
	}

	@Override
	public void run() {
		String r = Worker.treeList.get(no).judge(sample);
		Worker.treeResult[no] = r;
		Worker.treeCond[no] = true;
	}

}
