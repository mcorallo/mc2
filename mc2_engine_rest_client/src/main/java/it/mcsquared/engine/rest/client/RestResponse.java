package it.mcsquared.engine.rest.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the data returned by an HTTP request, collecting them in a simple object with helper methods.
 * 
 * @author mcorallo
 *
 */
public class RestResponse {

	private static Logger logger = LoggerFactory.getLogger(RestResponse.class);

	public static final int SC_OK = Response.Status.OK.getStatusCode();

	private String serializedEntity;
	private int statusCode;
	private Map<String, String> headers = new HashMap<String, String>();

	public RestResponse(HttpResponse response) {
		try {
			if (response == null) {
				throw new ConnectionServiceResponseException("the response object can't be null");
			}
			this.statusCode = response.getStatusLine().getStatusCode();
			InputStream inputStream = null;
			try {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					inputStream = entity.getContent();
					String result = IOUtils.toString(inputStream, "UTF-8");
					this.serializedEntity = result;
				}
			} finally {
				if (response.getEntity() != null) {
					EntityUtils.consume(response.getEntity());
				}
				if (inputStream != null) {
					inputStream.close();
				}
			}

			Header[] resHeaders = response.getAllHeaders();
			for (Header h : resHeaders) {
				headers.put(h.getName(), h.getValue());
			}

			logger.debug("RestResponse: {} - {}", statusCode, this.serializedEntity);
		} catch (IllegalStateException e) {
			throw new ConnectionServiceResponseException(e);
		} catch (IOException e) {
			throw new ConnectionServiceResponseException(e);
		}
	}

	public boolean isOk() {
		return this.statusCode == SC_OK;
	}

	public String getSerializedEntity() {
		return serializedEntity;
	}

	public int getStatusCode() {
		return statusCode;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RestResponse [serializedEntity=");
		builder.append(serializedEntity);
		builder.append(", statusCode=");
		builder.append(statusCode);
		builder.append("]");
		return builder.toString();
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	public static class ConnectionServiceResponseException extends RuntimeException {

		private static final long serialVersionUID = 8290363283494326394L;

		public ConnectionServiceResponseException(String message) {
			super(message);
		}

		public ConnectionServiceResponseException(Throwable throwable) {
			super(throwable);
		}

	}
}
