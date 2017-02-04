package master;

import java.io.IOException;

import util.TheSocket;

public class Send_msg_wait_ack implements Runnable {
	private TheSocket socket;
	private String msg;
	private int no;
	public Send_msg_wait_ack(TheSocket socket, String msg, int no) {
		this.socket = socket;
		this.msg = msg;
		this.no = no;
	}

	@Override
	public void run() {
		try{
			socket.write(msg);
			String ack = socket.read();
			if(!ack.isEmpty())
				Master.ackList.set(no, ack);
		} catch(IOException e) {
			Master.workerCrash = true;
			Master.socketList.set(no, null);
		}
	}
}
