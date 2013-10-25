import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;


public class BrowserReader implements Runnable {
	private final int EOF = -1;
	private final int HTTP_PORT = 80;
	private final int BUFFER_SIZE = 8*1024;
	
	private Socket browserSocket;
	private Socket webSocket;
	
	public BrowserReader(Socket browserSocket) {
		this.browserSocket = browserSocket;
		this.webSocket = null;
	}
	
	@Override
	public void run() {
		System.out.println("New thread created!");
		InetAddress ip = browserSocket.getInetAddress();
		System.out.println("IP: " + ip.getHostAddress() + " port: " + browserSocket.getPort());
		try {
			// create input stream from browser
			InputStream input = browserSocket.getInputStream();
//			ByteBuffer inputBuffer = ByteBuffer.allocate(BUFFER_SIZE);
//			byte[] inputBuffer = new byte[input.available()];
			// create output stream to the web
			String host = "stackoverflow.com";
			webSocket = new Socket(host, HTTP_PORT);
			OutputStream output = webSocket.getOutputStream();
			WebReader webReader = new WebReader(browserSocket, webSocket);
			new Thread(webReader).start();
			// transfer data
			int readVal;
			while((readVal = input.read()) != EOF) {
				System.out.print((char)readVal);
				output.write(readVal);
//				inputBuffer.put((byte)readVal);
			}
//			System.out.println("End of File reached");
//			String host = findHostname(inputBuffer);
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
		System.out.println("BrowserReader is finished");
	}
//
//	private String findHostname(ByteBuffer inputBuffer) {
//		System.out.print(inputBuffer.toString());
//		return "";
//	}
//
//	private void printBuffer(byte[] buffer) {
//		int byteCount = 0;
//		boolean newLineFlag = false;
//		BYTE_LOOP:
//		for(byte b : buffer) {
//			if(b != '\0') { //not the end
//				System.out.print((char)b);
//				byteCount++;
//				if(b == '\n') {
//					if(newLineFlag) {
//						break BYTE_LOOP;
//					}
//					newLineFlag = true;
//				} else {
//					newLineFlag = false;
//				}
//			} else {
//				break BYTE_LOOP;
//			}
//		}
//		System.out.println("Byte Count: " + byteCount);
//	}
//	
	
}
