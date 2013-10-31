import java.nio.ByteBuffer;


public class HttpRequest {
	// private class variables
	private final int INVALID_INDEX = -1; 
	private String request;
	
	private enum HeaderField {
		HOST_HEADER("Host"),
		CONNECTION_HEADER("Connection"),
		X_FORWARDED_FOR_HEADER("X-Forwarded-For"),
		PROXY_CONNECTION_HEADER("Proxy-Connection");
		
		private String value;
		
		HeaderField(String value) {
			this.value = value;
		}
		
		public String getHeaderString() {
			return value;
		}
	}
	
	public HttpRequest(ByteBuffer byteStream, String ipAddress) {
		// read in byte buffer as String
		byteStream.flip();
		StringBuilder builder = new StringBuilder();
		while(byteStream.hasRemaining()) {
			byte nextByte = byteStream.get();
			builder.append((char)nextByte);
		}
		byteStream.flip();
		byteStream.clear();
		request = builder.toString();
		// append new header fields
		addHeaderField(HeaderField.CONNECTION_HEADER, "closed");
		addHeaderField(HeaderField.X_FORWARDED_FOR_HEADER, ipAddress);
		removeHeaderField(HeaderField.PROXY_CONNECTION_HEADER);
//		addVia();
	}
	
	public String getHostname() {
		String hostHeaderString = HeaderField.HOST_HEADER.getHeaderString();
		int start = request.indexOf(hostHeaderString);
		if(start == INVALID_INDEX) {
			System.err.println("Could not process hostname");
			System.exit(1);
		}
		start += hostHeaderString.length() + ": ".length();
		int end = start;
		while(request.charAt(end) != '\r') {
			end++;
		}
		return request.substring(start, end);
	}
	
	public byte[] toByteArray() {
		return request.getBytes();
	}
	
	@Override
	public String toString() {
		return request;
	}
	
	private void addHeaderField(HeaderField header, String value) {
		// find the start of the header field
		StringBuilder builder = new StringBuilder();
		int start = getStartOfHeaderField(header);
		if(start == INVALID_INDEX) { // does not contain header field
			start = getNewHeaderFieldIndex();
			builder.append(request.substring(0, start));
			builder.append(header.getHeaderString() + ": " + value + "\r\n");
			builder.append(request.substring(start));
		} else { // contains header field so replace current value
			start += header.getHeaderString().length() + ": ".length();
			// find end of header field
			int end = start;
			while(request.charAt(end) != '\r') {
				end++;
			}
			builder.append(request.substring(0, start));
			builder.append(value);
			builder.append(request.substring(end));
		}
		request = builder.toString();
	}
	
	private void removeHeaderField(HeaderField header) {
		int start = getStartOfHeaderField(header);
		if(start != INVALID_INDEX) {
			int end = start;
			while(request.charAt(end) != '\n') {
				end++;
			}
			request = request.substring(0, start) + request.substring(end + 1);
		}	
	}
	
	private int getNewHeaderFieldIndex() {
		// find host header field
		int index = getStartOfHeaderField(HeaderField.HOST_HEADER);
		if(index != INVALID_INDEX) {
			// find index of end of line
			while(request.charAt(index) != '\n') {
				index++;
			}
		} else {
			System.err.println("Could not process hostname");
			System.exit(1);
		}
		return index + 1;
	}
	
	private int getStartOfHeaderField(HeaderField header) {
		int index = 0;
		while(true) {
			index = request.indexOf(header.getHeaderString(), index);
			if(index == INVALID_INDEX || request.charAt(index - 1) == '\n') {
				return index;
			} else {
				index++;
			}
		}
	}
	
}
