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
//				System.out.println("Waiting for a knock...");
				Socket clientSocket = welcomeSocket.accept();
//				System.out.println("Knock has been heard!...");
				BrowserReader browser = new BrowserReader(clientSocket);
				new Thread(browser).start();
			}
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}
}