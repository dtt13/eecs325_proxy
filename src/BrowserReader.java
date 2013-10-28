import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;


public class BrowserReader implements Runnable {
	private final int EOF = -1;
	private final int HTTP_PORT = 80;
	private final int BUFFER_SIZE = 1024;
	
	private Socket browserSocket;
//	private Socket webSocket;
	private RequestState requestState;
	
	private enum RequestState {
		CR1,LF1,CR2, WAIT
	};
	
	public BrowserReader(Socket browserSocket) {
		this.browserSocket = browserSocket;
//		this.webSocket = null;
		this.requestState = RequestState.WAIT;
	}
	
	@Override
	public void run() {
		System.out.println("New thread created!");
		InetAddress ip = browserSocket.getInetAddress();
		System.out.println("IP: " + ip.getHostAddress() + " port: " + browserSocket.getPort());
		try {
			// create input stream from browser
			InputStream input = browserSocket.getInputStream();
			ByteBuffer inputBuffer = ByteBuffer.allocate(BUFFER_SIZE);
			// transfer data
			int readVal;
			while((readVal = input.read()) != EOF) {
//				System.out.print((char)readVal);
				inputBuffer.put((byte)readVal);
				if(isEndOfRequest((char)readVal)) {
					HttpRequest hRequest = new HttpRequest(inputBuffer.array(), ip.getHostAddress());
					System.out.print(hRequest);
					System.out.flush();
					inputBuffer.clear();
					// create output stream to the web
					Socket webSocket = new Socket(hRequest.getHostname(), HTTP_PORT);
					WebReader webReader = new WebReader(browserSocket, webSocket);
					new Thread(webReader).start();
					OutputStream output = webSocket.getOutputStream();
					output.write(hRequest.toByteArray());
				}
			}
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
		System.out.println("BrowserReader is finished");
	}
	
	/**
	 * Determines whether the end of a HTTP request has been reached by identifying
	 * a double CRLF.
	 * 
	 * @param nextChar - the next character from the input stream
	 * @return true if end of request; false otherwise
	 */
	private boolean isEndOfRequest(char nextChar) {
		switch(requestState) {
		case WAIT:
			if(nextChar == '\r') {
				requestState = RequestState.CR1;
			}
			break;
		case CR1:
			if(nextChar == '\n') {
				requestState = RequestState.LF1;
			} else {
				requestState = RequestState.WAIT;
			}
			break;
		case LF1:
			if(nextChar == '\r') {
				requestState = RequestState.CR2;
			} else {
				requestState = RequestState.WAIT;
			}
			break;
		case CR2:
			requestState = RequestState.WAIT;
			if(nextChar == '\n') {
				return true;
			}
			break;
		}
		return false;
	}
}
