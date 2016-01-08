package it.mcsquared.engine.rest.client;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * Handles the data to be sent to a REST service, helping some phases of the http request building process:
 * <ul>
 * <li>url composition with path parameters</li>
 * <li>addition of query parameters</li>
 * <li>setting of body entity or post parameters</li>
 * <li>authentication</li>
 * <li>headers</li>
 * </ul>
 *
 * This class is provided with a {@link RestRequestBuilder} builder class that simplifies the request composition.
 * 
 * @author mcorallo
 *
 */
public class RestRequest {
	private static Logger logger = LoggerFactory.getLogger(RestRequest.class);

	public enum HttpMethod {
		GET, POST, PUT, DELETE
	}

	private Integer id;
	private HttpMethod method;
	private String baseUrl;
	private List<String> pathSegments = new ArrayList<String>();
	private Map<String, String> queryParameters = new LinkedHashMap<String, String>();
	private Object body;
	private String mediaType;

	private Map<String, String> headers;
	private Map<String, Object> postParameters;
	private List<Cookie> cookies;

	public RestRequest() {
	}

	private RestRequest(RestRequestBuilder builder) {
		this.baseUrl = builder.baseUrl;
		this.method = builder.method;
		this.pathSegments = builder.pathSegments;
		this.queryParameters = builder.queryParameters;
		this.body = builder.body;
		this.headers = builder.headers;
		this.postParameters = builder.postParameters;
		this.cookies = builder.cookies;
		this.mediaType = builder.mediaType;
	}

	/**
	 * @return The String representing this HTTP request
	 */
	public String getRequestUrl() {
		try {
			StringBuilder sb = new StringBuilder();
			if (baseUrl.endsWith("/")) {
				sb.append(baseUrl.substring(0, baseUrl.length() - 1));
			} else {
				sb.append(baseUrl);
			}

			for (String p : pathSegments) {
				sb.append("/").append(URLEncoder.encode(p, "UTF-8"));
			}

			if (!queryParameters.isEmpty()) {
				boolean first = true;
				String name, value;
				for (Entry<String, String> q : queryParameters.entrySet()) {
					if (first) {
						sb.append("?");
						first = false;
					} else {
						sb.append("&");
					}
					name = URLEncoder.encode(q.getKey(), "UTF-8");
					value = q.getValue() != null ? URLEncoder.encode(q.getValue(), "UTF-8") : null;
					sb.append(name).append("=").append(value);
				}
			}
			return sb.toString();
		} catch (UnsupportedEncodingException e) {
			throw new ConnectionServiceRequestException(e);
		}
	}

	/**
	 * @return The {@link HttpRequestBase} instance to be used in an {@link HttpClient} reqeust.
	 */
	public HttpRequestBase getHttpRequest() {
		Gson gson = new Gson();
		HttpRequestBase request = null;
		try {
			if (method == HttpMethod.GET) {
				request = new HttpGet(getRequestUrl());
				if (body != null) {
					throw new ConnectionServiceRequestException("An entity can't be assigned to e GET request");
				}
			} else {
				StringEntity entity = null;
				if (body != null) {
					String serializedEntity = gson.toJson(body);
					logger.debug("serialized entity: {} ", serializedEntity);
					entity = new StringEntity(serializedEntity);
				} else if (postParameters != null && !postParameters.isEmpty()) {
					entity = new UrlEncodedFormEntity(createNameValueList());
				}
				if (entity != null && mediaType != null && !mediaType.isEmpty()) {
					entity.setContentType(mediaType);
				}
				if (method == HttpMethod.POST) {
					request = new HttpPost(getRequestUrl());
					if (entity != null) {
						((HttpPost) request).setEntity(entity);
					}
				} else if (method == HttpMethod.PUT) {
					request = new HttpPut(getRequestUrl());
					if (entity != null) {
						((HttpPut) request).setEntity(entity);
					}
				} else if (method == HttpMethod.DELETE) {
					request = new HttpDelete(getRequestUrl());
					if (entity != null) {
						throw new ConnectionServiceRequestException("An entity can't be assigned to e DELETE request");
					}
				}
			}

			if (!headers.isEmpty()) {
				for (Entry<String, String> e : headers.entrySet()) {
					request.addHeader(e.getKey(), e.getValue());
				}
			}

		} catch (UnsupportedEncodingException e) {
			throw new ConnectionServiceRequestException(e);
		}
		return request;
	}

	public HttpMethod getMethod() {
		return method;
	}

	public Map<String, String> getQueryParameters() {
		return queryParameters;
	}

	public Object getBody() {
		return body;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	private List<NameValuePair> createNameValueList() {
		List<NameValuePair> result = null;
		if (postParameters != null && !postParameters.isEmpty()) {
			result = new ArrayList<NameValuePair>();
			for (Map.Entry<String, Object> param : postParameters.entrySet()) {
				for (String value : (String[]) param.getValue()) {
					if (logger.isDebugEnabled()) {
						logger.debug("Param key: " + param.getKey() + " Param Value: " + value);
					}
					result.add(new BasicNameValuePair(param.getKey(), value));
				}
			}
		}
		return result;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RestRequest [method=");
		builder.append(method);
		builder.append(", baseUrl=");
		builder.append(baseUrl);
		builder.append(", pathSegments=");
		builder.append(pathSegments);
		builder.append(", queryParameters=");
		builder.append(queryParameters);
		builder.append(", body=");
		builder.append(body);
		builder.append(", headers=");
		builder.append(headers);
		builder.append(", id=");
		builder.append(id);
		builder.append("]");
		return builder.toString();
	}

	public List<Cookie> getCookies() {
		return cookies;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public static class RestRequestBuilder {
		private HttpMethod method;
		private String baseUrl;
		private List<String> pathSegments = new ArrayList<String>();
		private Map<String, String> queryParameters = new LinkedHashMap<String, String>();
		private Object body;
		private String mediaType;

		private Map<String, String> headers = new LinkedHashMap<String, String>();
		private Map<String, Object> postParameters = new LinkedHashMap<String, Object>();
		private List<Cookie> cookies = new ArrayList<Cookie>();

		public RestRequestBuilder(HttpMethod method, String baseUrl) {
			this.method = method;
			this.baseUrl = baseUrl;
		}

		public RestRequestBuilder pathSegment(String pathSegment) {
			if (pathSegment == null || pathSegment.isEmpty()) {
				throw new ConnectionServiceRequestException("path segments can't be null");
			}
			pathSegments.add(pathSegment);
			return this;
		}

		public RestRequestBuilder queryParameter(String name, String value) {
			if (name == null || name.isEmpty()) {
				throw new ConnectionServiceRequestException("query parameter names can't be null");
			}
			queryParameters.put(name, value);
			return this;
		}

		public RestRequestBuilder body(Object body) {
			this.body = body;
			return this;
		}

		public RestRequestBuilder mediaType(String mediaType) {
			this.mediaType = mediaType;
			return this;
		}

		public RestRequestBuilder header(String key, String value) {
			this.headers.put(key, value);
			return this;
		}

		public RestRequestBuilder postParameter(String key, Object value) {
			this.postParameters.put(key, value);
			return this;
		}

		public RestRequestBuilder cookie(String key, String value) {
			BasicClientCookie c = new BasicClientCookie(key, value);
			// FIXME [mc2] rendere dinamico
			c.setDomain("localhost");
			c.setPath("/base-system-generator/");
			this.cookies.add(c);
			return this;
		}

		public RestRequest build() {
			if (method == null) {
				throw new ConnectionServiceRequestException("http method can't be null");
			}
			if (baseUrl == null || baseUrl.isEmpty()) {
				throw new ConnectionServiceRequestException("baseUrl can't be null");
			}
			return new RestRequest(this);
		}

	}

	public static class ConnectionServiceRequestException extends RuntimeException {

		private static final long serialVersionUID = 5036249479847463115L;

		public ConnectionServiceRequestException(String message) {
			super(message);
		}

		public ConnectionServiceRequestException(Throwable throwable) {
			super(throwable);
		}

	}
}
