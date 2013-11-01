import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

/**
 * Reads requests coming from the browser and creates new sockets to the
 * web server to handle these requests.
 * 
 * @author Derrick Tilsner dtt13
 */
public class BrowserReader implements Runnable {
	private final int EOF = -1;
	private final int HTTP_PORT = 80;
	private final int BUFFER_SIZE = 16*1024;
	
	private Socket browserSocket;
	private RequestState requestState;
	
	private enum RequestState {
		CR1, LF1, CR2, WAIT
	};
	
	public BrowserReader(Socket browserSocket) {
		this.browserSocket = browserSocket;
		this.requestState = RequestState.WAIT;
	}
	
	@Override
	public void run() {
		InetAddress clientIP = browserSocket.getInetAddress();
		System.out.println("Request from " + clientIP.getHostAddress() + " on port " + browserSocket.getPort());
		ByteBuffer inputBuffer = null;
		try {
			// create input stream from browser
			InputStream input = browserSocket.getInputStream();
			inputBuffer = ByteBuffer.allocate(BUFFER_SIZE);
			// transfer data
			int readVal;
			while((readVal = input.read()) != EOF) {
				inputBuffer.put((byte)readVal);
				if(isEndOfRequest((char)readVal)) {
					HttpRequest hRequest = new HttpRequest(inputBuffer, clientIP.getHostAddress());
					inputBuffer.clear();
					System.out.print(hRequest);
					System.out.flush();
					// create output stream to the web
					Socket webSocket = new Socket(hRequest.getHostname(), HTTP_PORT);
					WebReader webReader = new WebReader(browserSocket, webSocket);
					new Thread(webReader).start();
					OutputStream output = webSocket.getOutputStream();
					output.write(hRequest.toByteArray());
				}
			}
			browserSocket.close();
		} catch (IOException e) {
			// browser initiated connection reset
			// no action needs to be taken
		} catch (BufferOverflowException e) {
			System.err.println("The HTTP request buffer overflowed");
			System.err.flush();
		}
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
