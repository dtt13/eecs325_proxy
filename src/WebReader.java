import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;


public class WebReader implements Runnable {
	private final int EOF = -1;
	
	private Socket browserSocket;
	private Socket webSocket;
	
	public WebReader(Socket browserSocket, Socket webSocket) {
		this.browserSocket = browserSocket;
		this.webSocket = webSocket;
	}
	
	@Override
	public void run() {
		try {
			// create input and output streams
			InputStream input = webSocket.getInputStream();
			OutputStream output = browserSocket.getOutputStream();
			// transfer data
			int readVal;
			while((readVal = input.read()) != EOF) {
//				System.out.print((char)readVal);
				output.write(readVal);
			}
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
		System.out.println("WebReader is finished");
	}

}
