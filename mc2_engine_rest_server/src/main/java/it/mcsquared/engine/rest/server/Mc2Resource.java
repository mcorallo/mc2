package it.mcsquared.engine.rest.server;

import it.mcsquared.engine.Mc2Engine;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import com.google.gson.JsonObject;

@Produces(MediaType.APPLICATION_JSON)
public class Mc2Resource {

	protected static Mc2Engine engine;

	public static void init(Mc2Engine engine) {
		Mc2Resource.engine = engine;
	}

	@GET
	@Path(value = "/echo/{message}")
	public JsonObject echo(@PathParam("message") String message) throws UnsupportedEncodingException {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("message", URLDecoder.decode(message, "UTF-8"));

		return jsonObject;
	}

	protected String getHeader(HttpHeaders httpHeaders, String name) {
		List<String> headers = httpHeaders.getRequestHeader(name);
		return headers != null && !headers.isEmpty() ? headers.get(0) : null;
	}

	protected ResponseBuilder getNoCacheResponseBuilder(Status status) {
		CacheControl cc = new CacheControl();
		cc.setNoCache(true);
		cc.setMaxAge(-1);
		cc.setMustRevalidate(true);

		return Response.status(status).cacheControl(cc);
	}
}
