package com.couchbase.integrations.sg_restapi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import junit.framework.Assert;

import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.JSONObject;
import org.apache.wink.json4j.OrderedJSONObject;

import com.couchbase.integrations.LiteTestCase;
import com.couchbase.integrations.rest.RestClient;

public class BaseApiTest extends LiteTestCase {

	protected List<String> addDocs(RestClient rc, int numDocs)
			throws JSONException {
		List<String> ids = new ArrayList<String>();
		for (int i = 0; i < numDocs; i++) {
			HashMap<String, String> objToPost = new HashMap<String, String>();
			objToPost.put("doc" + (i + 1), "value" + (i + 1));
			JSONObject jsonData = new JSONObject(objToPost);
			Response response = rc.sendPost(DEFAULT_SG_BUCKET + "/", jsonData,
					null);
			OrderedJSONObject jsonResp = response
					.readEntity(OrderedJSONObject.class);
			ids.add(jsonResp.getString("id"));

		}
		return ids;
	}

	protected List<String> addDocsWithAttachment(RestClient rc, int numDocs)
			throws JSONException {
		List<String> ids = new ArrayList<String>();
		for (int i = 0; i < numDocs; i++) {
			HashMap<String, Object> objToPost = new HashMap<String, Object>();
			objToPost.put("_id", "attachment_doc" + (i + 1));
			String attachmentItem = "{\"foo.txt\":{\"content_type\":\"text\\/plain\",\"data\": \"VGhpcyBpcyBhIGJhc2U2NCBlbmNvZGVkIHRleHQ=\"}}";
			objToPost.put("_attachments", new JSONObject(attachmentItem));
			JSONObject jsonData = new JSONObject(objToPost);

			Response response = rc.sendPost(DEFAULT_SG_BUCKET + "/", jsonData,
					null);
			OrderedJSONObject jsonResp = response
					.readEntity(OrderedJSONObject.class);
			ids.add(jsonResp.getString("id"));

		}
		return ids;
	}

	protected List<String> addDocsWithMultipleAttachment(RestClient rc,
			int numDocs) throws JSONException {
		List<String> ids = new ArrayList<String>();
		for (int i = 0; i < numDocs; i++) {
			HashMap<String, Object> objToPost = new HashMap<String, Object>();
			objToPost.put("_id", "attachment_doc" + (i + 1));
			String attachmentItem = "{\"foo.txt\":{\"content_type\":\"text\\/plain\",\"data\": \"VGhpcyBpcyBhIGJhc2U2NCBlbmNvZGVkIHRleHQ=\"},\"bar.txt\":{\"content_type\":\"text\\/plain\",\"data\": \"VGhpcyBpcyBhIGJhc2U2NCBlbmNvZGVkIHRleHQ=\"}}";
			objToPost.put("_attachments", new JSONObject(attachmentItem));
			JSONObject jsonData = new JSONObject(objToPost);

			Response response = rc.sendPost(DEFAULT_SG_BUCKET + "/", jsonData,
					null);
			OrderedJSONObject jsonResp = response
					.readEntity(OrderedJSONObject.class);
			ids.add(jsonResp.getString("id"));

		}
		return ids;
	}

	protected List<String> addBulkDocs(RestClient rc, int numDocs)
			throws JSONException {
		List<String> ids = new ArrayList<String>();

		List<JSONObject> docsToPost = new ArrayList<JSONObject>();
		for (int i = 0; i < numDocs; i++) {
			HashMap<String, String> objToPost = new HashMap<String, String>();
			objToPost.put("doc" + (i + 1), "value" + (i + 1));
			docsToPost.add(new JSONObject(objToPost));
		}

		HashMap<String, List<JSONObject>> objToPost = new HashMap<String, List<JSONObject>>();
		objToPost.put("docs", docsToPost);
		JSONObject jsonData = new JSONObject(objToPost);
		Response response = rc.sendPost(DEFAULT_SG_BUCKET + "/_bulk_docs",
				jsonData, null);
		JSONObject[] jsonResp = response.readEntity(JSONObject[].class);
		for (JSONObject jsonObject : jsonResp) {
			ids.add(jsonObject.getString("id"));
		}
		return ids;
	}

	protected String getRevById(RestClient rc, String docId)
			throws JSONException {

		Response response = rc.getClientResponse(DEFAULT_SG_BUCKET + "/"
				+ docId, null);
		JSONObject jsonResp = response.readEntity(JSONObject.class);
		return jsonResp.getString("_rev");
	}

	protected void checkUnauthorized401(Response response) {
		assertEquals(401, response.getStatus());
		assertEquals(Response.Status.UNAUTHORIZED.toString(), response
				.getStatusInfo().toString());
		MultivaluedMap<String, String> headers = response.getStringHeaders();
		Set<String> expectedHeadersKeys = new HashSet<String>(Arrays.asList(
				"Date", "Content-Length", "Www-Authenticate", "Content-Type",
				"Server"));
		assertEquals("Found headers: " + headers, expectedHeadersKeys,
				headers.keySet());
		assertEquals("Found headers: " + headers, MediaType.APPLICATION_JSON,
				headers.get("Content-Type").get(0));
		assertTrue("Found headers: " + headers,
				Integer.valueOf(headers.get("Content-Length").get(0)) > 0);
		assertTrue("Found headers: " + headers, headers.get("Server").get(0)
				.startsWith("Couchbase Sync Gateway"));
		assertTrue(response.getDate().compareTo(new Date()) < 0);
		assertEquals("Found headers: " + headers,
				"Basic realm=\"Couchbase Sync Gateway\"",
				headers.get("Www-Authenticate").get(0));
		assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
		Assert.assertTrue(response.getLength() > 0);
		OrderedJSONObject json = response.readEntity(OrderedJSONObject.class);
		String expected = "{\"error\":\"Unauthorized\",\"reason\":\"Login required\"}";
		assertEquals("Found in response:" + json, json.toString(), expected);

	}

	protected void checkForbidden403(Response response) {
		assertEquals(403, response.getStatus());
		assertEquals(Response.Status.FORBIDDEN.toString(), response
				.getStatusInfo().toString());
		MultivaluedMap<String, String> headers = response.getStringHeaders();
		Set<String> expectedHeadersKeys = new HashSet<String>(Arrays.asList(
				"Date", "Content-Length", "Content-Type", "Server"));
		assertEquals("Found headers: " + headers, expectedHeadersKeys,
				headers.keySet());
		assertEquals("Found headers: " + headers, MediaType.APPLICATION_JSON,
				headers.get("Content-Type").get(0));
		assertTrue("Found headers: " + headers,
				Integer.valueOf(headers.get("Content-Length").get(0)) > 0);
		assertTrue("Found headers: " + headers, headers.get("Server").get(0)
				.startsWith("Couchbase Sync Gateway"));
		assertTrue(response.getDate().compareTo(new Date()) < 0);
		assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
		Assert.assertTrue(response.getLength() > 0);
		OrderedJSONObject json = response.readEntity(OrderedJSONObject.class);
		String expected = "{\"error\":\"Forbidden\",\"reason\":\"Creating a DB over the public API is unsupported\"}";
		assertEquals("Found in response:" + json, json.toString(), expected);

	}

	protected void checkBadRequest400(Response response) {
		String expectedRespString = "{\"error\":\"Bad Request\",\"reason\":\"invalid database name \\\"SYNC_GATEWAY\\\"\"}";
		checkBadRequest400(response, expectedRespString);
	}

	protected void checkBadRequest400(Response response,
			String expectedRespString) {
		assertEquals(400, response.getStatus());
		Assert.assertEquals(Response.Status.BAD_REQUEST.toString(), response
				.getStatusInfo().toString());
		MultivaluedMap<String, String> headers = response.getStringHeaders();
		Set<String> expectedHeadersKeys = new HashSet<String>(Arrays.asList(
				"Date", "Content-Length", "Content-Type", "Server"));
		assertEquals("Found headers: " + headers, expectedHeadersKeys,
				headers.keySet());
		assertEquals("Found headers: " + headers, MediaType.APPLICATION_JSON,
				headers.get("Content-Type").get(0));
		assertTrue("Found headers: " + headers,
				Integer.valueOf(headers.get("Content-Length").get(0)) > 0);
		assertTrue("Found headers: " + headers, headers.get("Server").get(0)
				.startsWith("Couchbase Sync Gateway"));
		assertTrue(response.getDate().compareTo(new Date()) < 0);
		assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
		Assert.assertTrue(response.getLength() > 0);
		OrderedJSONObject json = response.readEntity(OrderedJSONObject.class);
		assertEquals("Found in response:" + json, json.toString(),
				expectedRespString);

	}

	protected void checkNonExistDB404(Response response) {
		assertEquals(404, response.getStatus());
		Assert.assertEquals(Response.Status.NOT_FOUND.toString(), response
				.getStatusInfo().toString());
		MultivaluedMap<String, String> headers = response.getStringHeaders();
		Set<String> expectedHeadersKeys = new HashSet<String>(Arrays.asList(
				"Date", "Content-Length", "Content-Type", "Server"));
		assertEquals("Found headers: " + headers, expectedHeadersKeys,
				headers.keySet());
		assertEquals("Found headers: " + headers, MediaType.APPLICATION_JSON,
				headers.get("Content-Type").get(0));
		assertTrue("Found headers: " + headers,
				Integer.valueOf(headers.get("Content-Length").get(0)) > 0);
		assertTrue("Found headers: " + headers, headers.get("Server").get(0)
				.startsWith("Couchbase Sync Gateway"));
		assertTrue(response.getDate().compareTo(new Date()) < 0);
		assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
		Assert.assertTrue(response.getLength() > 0);
		OrderedJSONObject json = response.readEntity(OrderedJSONObject.class);
		String expected = "{\"error\":\"not_found\",\"reason\":\"no such database \\\"sync_gateway1\\\"\"}";
		assertEquals("Found in response:" + json, json.toString(), expected);

	}

	// https://github.com/couchbase/sync_gateway/issues/741
	protected void checkMethodNotAllowed405(Response response) {
		assertEquals(405, response.getStatus());
		Assert.assertEquals(Response.Status.METHOD_NOT_ALLOWED.toString(),
				response.getStatusInfo().toString());
		MultivaluedMap<String, String> headers = response.getStringHeaders();
		Set<String> expectedHeadersKeys = new HashSet<String>(Arrays.asList(
				"Date", "Content-Length", "Allow", "Content-Type"));
		assertEquals("Found headers: " + headers, expectedHeadersKeys,
				headers.keySet());
		assertEquals("Found headers: " + headers, "GET, HEAD, PUT, DELETE",
				headers.get("Allow").get(0));
		assertTrue("Found headers: " + headers,
				Integer.valueOf(headers.get("Content-Length").get(0)) > 0);
		assertTrue("Found headers: " + headers, headers.get("Server").get(0)
				.startsWith("Couchbase Sync Gateway"));
		assertTrue(response.getDate().compareTo(new Date()) < 0);
		assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
		Assert.assertTrue(response.getLength() > 0);
		OrderedJSONObject json = response.readEntity(OrderedJSONObject.class);
		String expected = "{\"error\":\"Method Not Allowed\",\"reason\":\"\"}";
		assertEquals("Found in response:" + json, json.toString(), expected);

	}

	protected void checkUnsuportedMediaType415(Response response) {
		Assert.assertEquals(415, response.getStatus());
		Assert.assertEquals(Response.Status.UNSUPPORTED_MEDIA_TYPE.toString(),
				response.getStatusInfo().toString());
		MultivaluedMap<String, String> headers = response.getStringHeaders();
		Set<String> expectedHeadersKeys = new HashSet<String>(Arrays.asList(
				"Date", "Content-Length", "Content-Type", "Server"));
		Assert.assertEquals("Found headers: " + headers, expectedHeadersKeys,
				headers.keySet());
		Assert.assertEquals("Found headers: " + headers,
				MediaType.APPLICATION_JSON, headers.get("Content-Type").get(0));
		Assert.assertTrue("Found headers: " + headers,
				Integer.valueOf(headers.get("Content-Length").get(0)) > 0);
		Assert.assertTrue("Found headers: " + headers, headers.get("Server")
				.get(0).startsWith("Couchbase Sync Gateway"));
		assertTrue(response.getDate().compareTo(new Date()) < 0);

		Assert.assertEquals(MediaType.APPLICATION_JSON_TYPE,
				response.getMediaType());
		Assert.assertTrue(response.getLength() > 0);
		OrderedJSONObject json = response.readEntity(OrderedJSONObject.class);
		String expected = "{\"error\":\"Unsupported Media Type\",\"reason\":\"Invalid content type text\\/plain\"}";
		assertEquals("Found in response:" + json, json.toString(), expected);
	}

	protected void checkDocumentExistConflict409(Response response) {
		Assert.assertEquals(409, response.getStatus());
		Assert.assertEquals(Response.Status.CONFLICT.toString(), response
				.getStatusInfo().toString());
		MultivaluedMap<String, String> headers = response.getStringHeaders();
		Set<String> expectedHeadersKeys = new HashSet<String>(Arrays.asList(
				"Date", "Content-Length", "Content-Type", "Server"));
		Assert.assertEquals("Found headers: " + headers, expectedHeadersKeys,
				headers.keySet());
		Assert.assertEquals("Found headers: " + headers,
				MediaType.APPLICATION_JSON, headers.get("Content-Type").get(0));
		Assert.assertTrue("Found headers: " + headers,
				Integer.valueOf(headers.get("Content-Length").get(0)) > 0);
		Assert.assertTrue("Found headers: " + headers, headers.get("Server")
				.get(0).startsWith("Couchbase Sync Gateway"));
		assertTrue(response.getDate().compareTo(new Date()) < 0);

		Assert.assertEquals(MediaType.APPLICATION_JSON_TYPE,
				response.getMediaType());
		Assert.assertTrue(response.getLength() > 0);
		OrderedJSONObject json = response.readEntity(OrderedJSONObject.class);
		String regex = "{\"error\":\"conflict\",\"reason\":\"Document exists\"}";
		assertEquals("Found in response:" + json, json.toString(), regex);

	}

	protected void checkDocumentRevisionConflict409(Response response) {
		Assert.assertEquals(409, response.getStatus());
		Assert.assertEquals(Response.Status.CONFLICT.toString(), response
				.getStatusInfo().toString());
		MultivaluedMap<String, String> headers = response.getStringHeaders();
		Set<String> expectedHeadersKeys = new HashSet<String>(Arrays.asList(
				"Date", "Content-Length", "Content-Type", "Server"));
		Assert.assertEquals("Found headers: " + headers, expectedHeadersKeys,
				headers.keySet());
		Assert.assertEquals("Found headers: " + headers,
				MediaType.APPLICATION_JSON, headers.get("Content-Type").get(0));
		Assert.assertTrue("Found headers: " + headers,
				Integer.valueOf(headers.get("Content-Length").get(0)) > 0);
		Assert.assertTrue("Found headers: " + headers, headers.get("Server")
				.get(0).startsWith("Couchbase Sync Gateway"));
		assertTrue(response.getDate().compareTo(new Date()) < 0);

		Assert.assertEquals(MediaType.APPLICATION_JSON_TYPE,
				response.getMediaType());
		Assert.assertTrue(response.getLength() > 0);
		OrderedJSONObject json = response.readEntity(OrderedJSONObject.class);
		String regex = "{\"error\":\"conflict\",\"reason\":\"Document revision conflict\"}";
		assertEquals("Found in response:" + json, json.toString(), regex);
	}

}
