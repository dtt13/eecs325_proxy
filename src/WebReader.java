import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Creates a pipeline from the web server's output stream to the browser's
 * input stream. Transfers data byte-by-byte.
 *  
 * @author Derrick Tilsner dtt13
 */
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
				output.write(readVal);
			}
			webSocket.close();
		} catch (IOException e) {
			// browser closed the connection prematurely
			// no action needs to be taken
		}
	}

}
