
public class HttpRequest {
	private String request;
	
	public HttpRequest(byte[] byteStream, String ipAddress) {
		request = new String(byteStream);
		addForwardForX(ipAddress);
		addVia();
	}
	
	public String getHostname() {
		int startingIndex = request.indexOf("Host:") + 6;
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
