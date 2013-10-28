import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class BrowserListener {
	private static int portNumber = 5000;
	
	public static void main(String[] args) {
		try {
			// create a welcome socket for all TCP connections
			System.out.println("Creating welcome socket at port " + portNumber + "...");
			ServerSocket welcomeSocket = new ServerSocket(portNumber);
			// accept each request as it comes and spawn a new thread
			while(true) {
				Socket clientSocket = welcomeSocket.accept();
				BrowserReader browser = new BrowserReader(clientSocket);
				new Thread(browser).start();
			}
		} catch (IOException e) {
			System.err.println("Error with welcome socket");
			System.err.println(e.getMessage());
		}
	}
}
