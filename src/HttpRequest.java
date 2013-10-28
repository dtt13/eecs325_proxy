import java.nio.ByteBuffer;


public class HttpRequest {
	private final int INVALID_INDEX = -1; 
	private String request;
	
	public HttpRequest(ByteBuffer byteStream, String ipAddress) {
		// read in byte buffer as String
		byteStream.flip();
		StringBuilder builder = new StringBuilder();
		while(byteStream.hasRemaining()) {
			byte nextByte = byteStream.get();
			builder.append((char)nextByte);
//			System.out.print((char)nextByte);
//			System.out.flush();
		}
		byteStream.flip();
		byteStream.clear();
		request = builder.toString();
		// append new header fields
		addForwardForX(ipAddress);
		addVia();
	}
	
	public String getHostname() {
		int startingIndex = request.indexOf("Host:") + 6;
		if(startingIndex == INVALID_INDEX) {
			System.err.println("Could not process hostname");
			System.exit(1);
		}
		int endingIndex = startingIndex;
		while(request.charAt(endingIndex) != '\r') {
			endingIndex++;
		}
		return request.substring(startingIndex, endingIndex);
	}
	
	public byte[] toByteArray() {
		return request.getBytes();
	}
	
	@Override
	public String toString() {
		return request;
	}
	
	private void addForwardForX(String ipAddress) {
		
	}
	
	private void addVia() {
		
	}
	
}
