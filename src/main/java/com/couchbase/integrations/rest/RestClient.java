package com.couchbase.integrations.rest;

import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.wink.json4j.JSONObject;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;

public class RestClient {
	private Client client;
	private String baseURL;

	// private String user;
	// private String password;

	public RestClient(String baseURL) {
		client = ClientBuilder.newClient();
		this.baseURL = baseURL;

	}

	public RestClient(String baseURL, String user, String password) {
		client = ClientBuilder.newClient();
		// client.property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND,
		// false);
		// client.property(JSONConfiguration.FEATURE_POJO_MAPPING,
		// Boolean.TRUE);
		client.register(HttpAuthenticationFeature.basic(user, password));
		this.baseURL = baseURL;

	}

	public String getResponse() {
		return getResponse(null);
	}

	public Response getClientResponse() {
		return getClientResponse(null, null);
	}

	public String getResponse(String path) {
		try {
			WebTarget webTarget = client.target(baseURL);
			if (path != null) {
				webTarget = webTarget.path(path);
			}
			Response response = webTarget.request().accept("application/json")
					.get(Response.class);

			if (response.getStatus() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ response.getStatus());
			}

			String result = response.readEntity(String.class);
			return result;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public Response getClientResponse(String path,
			Map<String, String> queryParams) {
		try {
			WebTarget webTarget = client.target(baseURL);
			if (path != null) {
				webTarget = webTarget.path(path);
			}
			if (queryParams != null) {
				for (String key : queryParams.keySet()) {
					webTarget = webTarget.queryParam(key, queryParams.get(key));
				}

			}

			Response response = webTarget.request(MediaType.APPLICATION_JSON)
					.accept("application/json")
					// .type(MediaType.APPLICATION_JSON)
					.get(Response.class);

			return response;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public Response sendPost(String path, Object formData,
			Map<String, String> queryParams) {
		WebTarget webTarget = client.target(baseURL);
		if (path != null) {
			webTarget = webTarget.path(path);
		}
		if (queryParams != null) {
			for (String key : queryParams.keySet()) {
				webTarget = webTarget.queryParam(key, queryParams.get(key));
			}
		}

		Response response = null;

		if (formData instanceof JSONObject) {
			// String input = "{\"qty\":100,\"name\":\"iPad 4\"}";
			response = webTarget
					.request()
					.accept(MediaType.APPLICATION_FORM_URLENCODED)
					.accept(MediaType.APPLICATION_JSON_TYPE)
					.accept(MediaType.APPLICATION_JSON)
					.accept(MediaType.MULTIPART_FORM_DATA_TYPE)
					.method("POST",
							Entity.entity(formData, MediaType.APPLICATION_JSON),
							Response.class);

			// .post(Entity.json(formData), Response.class);
		} else if (formData instanceof String) {
			response = webTarget.request("*/*")
			// .accept(MediaType.APPLICATION_FORM_URLENCODED)
			// .accept(MediaType.APPLICATION_JSON_TYPE)
					.post(Entity.text(formData), Response.class);

		} else if ((formData instanceof Form)) {
			response = webTarget.request()
					.accept(MediaType.APPLICATION_FORM_URLENCODED)
					.accept(MediaType.APPLICATION_JSON_TYPE)
					.post(Entity.form((Form) formData), Response.class);
		} else {
			throw new IllegalAccessError(
					"Post data should be String, FormData or JSONObject instance");
		}

		return response;
	}

	// File f = ...
	// webTarget.request().post(Entity.entity(f, MediaType.TEXT_PLAIN_TYPE));

	public Response sendPut(String path, Object formData,
			Map<String, String> queryParams) {
		return sendPut(path, formData, queryParams, null);
	}

	public Response sendPut(String path, Object formData,
			Map<String, String> queryParams,
			MultivaluedMap<String, Object> headers) {
		if (formData == null) {
			// workaround for:
			// java.lang.IllegalStateException: Entity must not be null for http
			// method PUT
			client.property(
					ClientProperties.SUPPRESS_HTTP_COMPLIANCE_VALIDATION, true);
		}

		WebTarget webTarget = client.target(baseURL);
		if (path != null) {
			webTarget = webTarget.path(path);
		}
		if (queryParams != null) {
			for (String key : queryParams.keySet()) {
				webTarget = webTarget.queryParam(key, queryParams.get(key));
			}
		}
		Response response = null;

		if (formData instanceof JSONObject) {

			Builder builder = webTarget.request()
					.accept(MediaType.APPLICATION_FORM_URLENCODED)
					.accept(MediaType.APPLICATION_JSON_TYPE)
					.accept(MediaType.APPLICATION_JSON)
					.accept(MediaType.MULTIPART_FORM_DATA_TYPE);

			if (headers != null) {
				builder = builder.headers(headers);
			}
			response = builder.method("PUT",
					Entity.entity(formData, MediaType.APPLICATION_JSON),
					Response.class);

			// .post(Entity.json(formData), Response.class);
		} else if (formData instanceof String) {
			Builder builder = webTarget.request("*/*");
			if (headers != null) {
				builder = builder.headers(headers);
			}
			response = builder
			// .accept(MediaType.APPLICATION_FORM_URLENCODED)
			// .accept(MediaType.APPLICATION_JSON_TYPE)
					.put(Entity.text(formData), Response.class);

		} else if ((formData instanceof Form)) {
			response = webTarget.request()
					.accept(MediaType.APPLICATION_FORM_URLENCODED)
					.accept(MediaType.APPLICATION_JSON_TYPE)
					.put(Entity.form((Form) formData), Response.class);
		} else if (formData == null) {
			response = webTarget.request()
					.accept(MediaType.APPLICATION_FORM_URLENCODED)
					.accept(MediaType.APPLICATION_JSON_TYPE)
					.put(null, Response.class);
		} else {

			throw new IllegalAccessError(
					"Post data should be String, FormData or JSONObject instance");
		}

		return response;
	}

	public Response delete(String path, Map<String, String> queryParams,
			MultivaluedMap<String, Object> headers) {
		WebTarget webTarget = client.target(baseURL);
		if (path != null) {
			webTarget = webTarget.path(path);
		}
		if (queryParams != null) {
			for (String key : queryParams.keySet()) {
				webTarget = webTarget.queryParam(key, queryParams.get(key));
			}
		}

		Response response = null;
		if (headers != null) {
			response = webTarget.request().headers(headers)
					.delete(Response.class);
		} else {
			response = webTarget.request().delete(Response.class);
		}

		return response;

	}
}
