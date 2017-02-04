package util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import org.apache.log4j.Logger;

public class TheSocket {
	public Socket socket;
	private Logger log = Logger.getLogger(TheSocket.class);

	public TheSocket(Socket socket) {
		this.socket = socket;
	}

	public String read() throws IOException {
		String info = "";
		byte[] buffer = new byte[1024];
		
		try {
			int n = 0;
			InputStream input = socket.getInputStream();
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			n = input.read(buffer);
			stream.write(buffer, 0, n);
			info = new String(stream.toByteArray());
		} catch (IOException e) {
			log.error(TheSocket.class, e);
			throw e;
		}

		return info;
	}

	public void write(String info) throws IOException {
		byte[] msg = info.getBytes();
		try {
			OutputStream output = socket.getOutputStream();
			output.write(msg);
			output.flush();
		} catch (IOException e) {
			log.error(TheSocket.class, e);
			throw e;
		}
	}
}
