import java.nio.ByteBuffer;

/**
 * Represents a HTTP request message; targeted for proxy applications by altering
 * and adding proxy-specific header fields to the HTTP request.
 * 
 * @author Derrick Tilsner dtt13
 */
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
	
	/**
	 * Constructor of the HttpRequest class that reads a single request from
	 * the byte buffer and adds the appropriate header fields for a proxy
	 * application.
	 * 
	 * @param byteBuffer - byte buffer containing a single HTTP request
	 * @param ipAddress - IP address of the browser's host
	 */
	public HttpRequest(ByteBuffer byteBuffer, String ipAddress) {
		// read in byte buffer as String
		byteBuffer.flip();
		StringBuilder builder = new StringBuilder();
		while(byteBuffer.hasRemaining()) {
			byte nextByte = byteBuffer.get();
			builder.append((char)nextByte);
		}
		byteBuffer.flip();
		byteBuffer.clear();
		request = builder.toString();
		// append new header fields
		addHeaderField(HeaderField.CONNECTION_HEADER, "closed");
		addHeaderField(HeaderField.X_FORWARDED_FOR_HEADER, ipAddress);
		removeHeaderField(HeaderField.PROXY_CONNECTION_HEADER);
//		addVia();
	}
	
	/**
	 * Finds the hostname corresponding to the HTTP request.
	 * 
	 * @return a String containing the value of the Host header field
	 */
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
	
	/**
	 * Adds a header field to the HTTP request with a specified value. If the
	 * header field is already in use, the value is replaced.
	 * 
	 * @param header - the header field
	 * @param value - the value corresponding to the header field
	 */
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
	
	/**
	 * Removes a header field from the HTTP request if it exists.
	 * 
	 * @param header - the header field to be removed
	 */
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
	
	/**
	 * Finds a place to insert another header field. The index returned
	 * will be the field directly following the Host header field.
	 * 
	 * @return the index of new header field
	 */
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
	
	/**
	 * Finds a header field in the request message if it exists.
	 * 
	 * @param header - the header field to find
	 * @return the starting index of the header field; -1 if the field doesn't
	 * exist
	 */
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
