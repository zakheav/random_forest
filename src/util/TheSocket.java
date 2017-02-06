package util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import org.apache.log4j.Logger;

public class TheSocket {
	public Socket socket;
	private Logger log = Logger.getLogger(TheSocket.class);
	private byte[] bagCache = new byte[1024 * 10];
	private int payloadLength = -1;
	private int bagWriteIdx = 0;
	
	public TheSocket(Socket socket) {
		this.socket = socket;
	}

	private byte[] wrap(byte[] payload) {// 为发送的数据包增加长度头
		byte[] bag = new byte[payload.length + 4];
		byte[] payloadLength = Integer_byte_transform.intToByteArray(payload.length);
		bag[0] = payloadLength[0];
		bag[1] = payloadLength[1];
		bag[2] = payloadLength[2];
		bag[3] = payloadLength[3];
		for (int i = 0; i < payload.length; ++i) {
			bag[i + 4] = payload[i];
		}
		return bag;
	}
	
	public String read() throws IOException {
		String info = "";
		
		byte[] buffer = new byte[1024 * 10];
		InputStream input = socket.getInputStream();
		int length = input.read(buffer);
		
		for (int bufferIdx = 0; bufferIdx < length; ++bufferIdx){
			if (bagWriteIdx < 4) {// 包头没有接受完全
				bagCache[bagWriteIdx++] = buffer[bufferIdx];
			} else {// 包头接受完了，已经知道包有多长
				if (payloadLength == -1) {
					byte[] lengthByte = new byte[4];
					lengthByte[0] = bagCache[0];
					lengthByte[1] = bagCache[1];
					lengthByte[2] = bagCache[2];
					lengthByte[3] = bagCache[3];
					payloadLength = Integer_byte_transform.byteArrayToInt(lengthByte);
				}
				if (bagWriteIdx < payloadLength + 4) {
					bagCache[bagWriteIdx++] = buffer[bufferIdx];
				}
				if(bagWriteIdx == payloadLength + 4) {
					// this bag receive complete
					StringBuffer massageBuffer = new StringBuffer(1024 * 10);
					for (int i = 4; i < payloadLength + 4; ++i) {
						massageBuffer.append((char) bagCache[i]);
					}
					info = massageBuffer.toString();
					// prepare to read next bag
					payloadLength = -1;
					bagWriteIdx = 0;
				}
			}
		}
		
		return info;
	}

	public void write(String info) throws IOException {
		byte[] msg = wrap(info.getBytes());
		try {
			OutputStream output = socket.getOutputStream();
			output.write(msg);
			output.flush();
		} catch (IOException e) {
			log.error("socket error");
			throw e;
		}
	}
}
