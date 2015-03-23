package com.couchbase.integrations.sg_restapi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import jersey.repackaged.com.google.common.collect.ImmutableMap;
import junit.framework.Assert;

import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.JSONObject;
import org.apache.wink.json4j.OrderedJSONObject;

import com.couchbase.integrations.helpers.SGHelper;
import com.couchbase.integrations.rest.RestClient;
import com.couchbase.integrations.shell.RemoteExecutor;
import com.couchbase.lite.CouchbaseLiteException;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

public class DocumentAPI extends BaseApiTest {

	// POST /{db} Creates a new document in the database
	public void postDoc() throws JSchException, IOException, SftpException,
			CouchbaseLiteException, JSONException {
		RemoteExecutor reSG = new RemoteExecutor(sgmaster);

		SGHelper.killAllSG(reSG);
		SGHelper.runSG(reSG, SG_PATH, null);
		List<String> urls = Arrays.asList(sgmaster.buildSgUrl());
		for (String url : urls) {
			RestClient rc = new RestClient(url);
			HashMap<String, String> objToPost = new HashMap<String, String>();
			objToPost.put("doc", "value");
			JSONObject jsonData = new JSONObject(objToPost);
			Response response = rc.sendPost(DEFAULT_SG_BUCKET + "/", jsonData,
					null);
			checkUnauthorized401(response);
		}
	}

	// POST /{db} Creates a new document in the database
	public void postDocAdmin() throws JSchException, IOException,
			SftpException, CouchbaseLiteException, JSONException {
		RemoteExecutor reSG = new RemoteExecutor(sgmaster);

		SGHelper.killAllSG(reSG);
		SGHelper.runSG(reSG, SG_PATH, null);
		List<JSONObject> docsToPost = new ArrayList<JSONObject>();
		HashMap<String, String> objToPost = new HashMap<String, String>();
		objToPost.put("doc", "value");
		docsToPost.add(new JSONObject(objToPost));

		HashMap<String, Object> objToPostAtt = new HashMap<String, Object>();
		objToPostAtt.put("_id", "attachment_doc1");
		String attachmentItem = "{\"foo.txt\":{\"content_type\":\"text\\/plain\",\"data\": \"VGhpcyBpcyBhIGJhc2U2NCBlbmNvZGVkIHRleHQ=\"}}";
		objToPostAtt.put("_attachments", new JSONObject(attachmentItem));
		docsToPost.add(new JSONObject(objToPostAtt));

		HashMap<String, Object> objToPostMultAtt = new HashMap<String, Object>();
		objToPostMultAtt.put("_id", "attachment_doc2");
		String attachmentMultItem = "{\"foo.txt\":{\"content_type\":\"text\\/plain\",\"data\": \"VGhpcyBpcyBhIGJhc2U2NCBlbmNvZGVkIHRleHQ=\"},\"bar.txt\":{\"content_type\":\"text\\\\/plain\",\"data\": \"VGhpcyBpcyBhIGJhc2U2NCBlbmNvZGVkIHRleHQ=\"}}";
		objToPostMultAtt
				.put("_attachments", new JSONObject(attachmentMultItem));
		docsToPost.add(new JSONObject(objToPostMultAtt));

		for (JSONObject jsonOject : docsToPost) {
			RestClient rc = new RestClient(sgmaster.buildSgAdminUrl());
			Response response = rc.sendPost(DEFAULT_SG_BUCKET + "/", jsonOject,
					null);
			Assert.assertEquals(200, response.getStatus());
			Assert.assertEquals(Response.Status.OK.toString(), response
					.getStatusInfo().toString());
			MultivaluedMap<String, String> headers = response
					.getStringHeaders();
			Set<String> expectedHeadersKeys = new HashSet<String>(
					Arrays.asList("Date", "Content-Length", "Location", "Etag",
							"Content-Type", "Server"));
			Assert.assertEquals("Found headers: " + headers,
					expectedHeadersKeys, headers.keySet());
			Assert.assertTrue("Found empty location:" + response.getLocation(),
					response.getLocation().toString().length() > 0);
			Assert.assertTrue("Found entity tag:" + headers.get("Etag"),
					headers.get("Etag").get(0).startsWith("1-"));
			Assert.assertEquals("Found headers: " + headers,
					MediaType.APPLICATION_JSON, headers.get("Content-Type")
							.get(0));
			Assert.assertTrue("Found headers: " + headers,
					Integer.valueOf(headers.get("Content-Length").get(0)) > 0);
			Assert.assertTrue("Found headers: " + headers, headers
					.get("Server").get(0).startsWith("Couchbase Sync Gateway"));
			assertTrue(response.getDate().compareTo(new Date()) < 0);

			Assert.assertEquals(MediaType.APPLICATION_JSON_TYPE,
					response.getMediaType());
			Assert.assertTrue(response.getLength() > 0);
			OrderedJSONObject json = response
					.readEntity(OrderedJSONObject.class);
			String regex = "\\{\"id\":\"(.*)\",\"ok\":true,\"rev\":\"1-(.*)\"\\}";
			assertTrue("Found in response:" + json,
					json.toString().matches(regex));
		}
	}

	public void postDocWithInvalidDataAttachmentAdmin500()
			throws JSchException, IOException, SftpException,
			CouchbaseLiteException, JSONException {
		RemoteExecutor reSG = new RemoteExecutor(sgmaster);

		SGHelper.killAllSG(reSG);
		SGHelper.runSG(reSG, SG_PATH, null);
		List<JSONObject> docsToPost = new ArrayList<JSONObject>();
		HashMap<String, String> objToPost = new HashMap<String, String>();

		HashMap<String, Object> objToPostAtt = new HashMap<String, Object>();
		objToPost.put("_id", "attachment_doc");
		String attachmentItem = "{\"foo.txt\":{\"content_type\":\"text\\/plain\",\"data\": \"VGhpcyBpcyBhIGJhc2U2NCBlbmNvZGVkIHRleHQ==\"}}";
		objToPostAtt.put("_attachments", new JSONObject(attachmentItem));
		docsToPost.add(new JSONObject(objToPostAtt));

		HashMap<String, Object> objToPostMultAtt = new HashMap<String, Object>();
		objToPostMultAtt.put("_id", "attachment_doc");
		String attachmentMultItem = "{\"foo.txt\":{\"content_type\":\"text\\/plain\",\"data\": \"VGhpcyBpcyBhIGJhc2U2NCBlbmNvZGVkIHRleHQ==\"},\"bar.txt\":{\"content_type\":\"text\\\\/plain\",\"data\": \"VGhpcyBpcyBhIGJhc2U2NCBlbmNvZGVkIHRleHQ=\"}}";
		objToPostMultAtt
				.put("_attachments", new JSONObject(attachmentMultItem));
		docsToPost.add(new JSONObject(objToPostMultAtt));

		for (JSONObject jsonOject : docsToPost) {
			RestClient rc = new RestClient(sgmaster.buildSgAdminUrl());
			Response response = rc.sendPost(DEFAULT_SG_BUCKET + "/", jsonOject,
					null);
			Assert.assertEquals(500, response.getStatus());
			Assert.assertEquals(Response.Status.INTERNAL_SERVER_ERROR
					.toString(), response.getStatusInfo().toString());
			MultivaluedMap<String, String> headers = response
					.getStringHeaders();
			Set<String> expectedHeadersKeys = new HashSet<String>(
					Arrays.asList("Date", "Content-Length", "Content-Type",
							"Server"));
			Assert.assertEquals("Found headers: " + headers,
					expectedHeadersKeys, headers.keySet());
			Assert.assertEquals("Found headers: " + headers,
					MediaType.APPLICATION_JSON, headers.get("Content-Type")
							.get(0));
			Assert.assertTrue("Found headers: " + headers,
					Integer.valueOf(headers.get("Content-Length").get(0)) > 0);
			Assert.assertTrue("Found headers: " + headers, headers
					.get("Server").get(0).startsWith("Couchbase Sync Gateway"));
			assertTrue(response.getDate().compareTo(new Date()) < 0);

			Assert.assertEquals(MediaType.APPLICATION_JSON_TYPE,
					response.getMediaType());
			Assert.assertTrue(response.getLength() > 0);
			OrderedJSONObject json = response
					.readEntity(OrderedJSONObject.class);
			String regex = "{\"error\":\"Internal Server Error\",\"reason\":\"Internal error: illegal base64 data at input byte 40\"}";
			assertEquals("Found in response:" + json, json.toString(), regex);

		}
	}

	// TODO how to create with text/plain?

	// POST /{db} Creates a new document in the database
	public void postTextDocAdmin() throws JSchException, IOException,
			SftpException, CouchbaseLiteException, JSONException {
		RemoteExecutor reSG = new RemoteExecutor(sgmaster);

		SGHelper.killAllSG(reSG);
		SGHelper.runSG(reSG, SG_PATH, null);
		List<String> urls = Arrays.asList(sgmaster.buildSgAdminUrl());
		for (String url : urls) {
			RestClient rc = new RestClient(url);
			HashMap<String, String> objToPost = new HashMap<String, String>();
			objToPost.put("doc", "value");
			JSONObject jsonData = new JSONObject(objToPost);
			Response response = rc.sendPost(DEFAULT_SG_BUCKET + "/",
					jsonData.toString(), null);
			checkUnsuportedMediaType415(response);

		}
	}

	public void postDocNonExistDB() throws JSchException, IOException,
			SftpException, CouchbaseLiteException, JSONException {
		RemoteExecutor reSG = new RemoteExecutor(sgmaster);

		SGHelper.killAllSG(reSG);
		SGHelper.runSG(reSG, SG_PATH, null);
		List<String> urls = Arrays.asList(sgmaster.buildSgAdminUrl());
		for (String url : urls) {
			RestClient rc = new RestClient(url);
			HashMap<String, String> objToPost = new HashMap<String, String>();
			objToPost.put("doc", "value");
			JSONObject jsonData = new JSONObject(objToPost);
			Response response = rc.sendPost(DEFAULT_SG_BUCKET + "/", jsonData,
					null);
			checkBadRequest400(response);
		}
	}

	public void postDocOnDoc() throws JSchException, IOException,
			SftpException, CouchbaseLiteException, JSONException {
		RemoteExecutor reSG = new RemoteExecutor(sgmaster);

		SGHelper.killAllSG(reSG);
		SGHelper.runSG(reSG, SG_PATH, null);
		List<String> urls = Arrays.asList(sgmaster.buildSgAdminUrl());
		for (String url : urls) {
			RestClient rc = new RestClient(url);
			HashMap<String, String> objToPost = new HashMap<String, String>();
			objToPost.put("doc", "value");
			JSONObject jsonData = new JSONObject(objToPost);
			Response response = rc.sendPost(DEFAULT_SG_BUCKET + "1/doc",
					jsonData, null);
			checkMethodNotAllowed405(response);
		}
	}

	public void putDocument() throws JSchException, IOException, SftpException,
			CouchbaseLiteException, JSONException {
		RemoteExecutor reSG = new RemoteExecutor(sgmaster);

		SGHelper.killAllSG(reSG);
		SGHelper.runSG(reSG, SG_PATH, null);
		List<String> urls = Arrays.asList(sgmaster.buildSgUrl());
		for (String url : urls) {
			RestClient rc = new RestClient(url);
			HashMap<String, String> objToPost = new HashMap<String, String>();
			objToPost.put("doc", "value");
			JSONObject jsonData = new JSONObject(objToPost);
			Response response = rc.sendPut(DEFAULT_SG_BUCKET + "/doc",
					jsonData, null);
			checkUnauthorized401(response);
		}

	}

	public void putDocumentAdmin() throws JSchException, IOException,
			SftpException, CouchbaseLiteException, JSONException {
		RemoteExecutor reSG = new RemoteExecutor(sgmaster);

		SGHelper.killAllSG(reSG);
		SGHelper.runSG(reSG, SG_PATH, null);
		List<String> urls = Arrays.asList(sgmaster.buildSgAdminUrl());
		for (String url : urls) {
			RestClient rc = new RestClient(url);
			HashMap<String, String> objToPost = new HashMap<String, String>();
			objToPost.put("doc", "value");
			JSONObject jsonData = new JSONObject(objToPost);
			Response response = rc.sendPut(DEFAULT_SG_BUCKET + "/doc",
					jsonData, null);
			Assert.assertEquals(201, response.getStatus());
			Assert.assertEquals(Response.Status.CREATED.toString(), response
					.getStatusInfo().toString());
			MultivaluedMap<String, String> headers = response
					.getStringHeaders();
			Set<String> expectedHeadersKeys = new HashSet<String>(
					Arrays.asList("Date", "Content-Length", "Etag",
							"Content-Type", "Server"));
			Assert.assertEquals("Found headers: " + headers,
					expectedHeadersKeys, headers.keySet());
			Assert.assertTrue("Found entity tag:" + headers.get("Etag"),
					headers.get("Etag").get(0).startsWith("1-"));
			Assert.assertEquals("Found headers: " + headers,
					MediaType.APPLICATION_JSON, headers.get("Content-Type")
							.get(0));
			Assert.assertTrue("Found headers: " + headers,
					Integer.valueOf(headers.get("Content-Length").get(0)) > 0);
			Assert.assertTrue("Found headers: " + headers, headers
					.get("Server").get(0).startsWith("Couchbase Sync Gateway"));
			assertTrue(response.getDate().compareTo(new Date()) < 0);

			Assert.assertEquals(MediaType.APPLICATION_JSON_TYPE,
					response.getMediaType());
			Assert.assertTrue(response.getLength() > 0);
			OrderedJSONObject json = response
					.readEntity(OrderedJSONObject.class);
			String regex = "\\{\"id\":\"(.*)\",\"ok\":true,\"rev\":\"1-(.*)\"\\}";
			assertTrue("Found in response:" + json,
					json.toString().matches(regex));
		}
	}

	public void putExistingDocumentConflictAdmin() throws JSchException,
			IOException, SftpException, CouchbaseLiteException, JSONException {
		RemoteExecutor reSG = new RemoteExecutor(sgmaster);

		SGHelper.killAllSG(reSG);
		SGHelper.runSG(reSG, SG_PATH, null);
		List<ImmutableMap<String, String>> queryParams = Arrays.asList(null,
				ImmutableMap.<String, String> builder()
						.put("new_edits", "true").build());
		for (ImmutableMap<String, String> queryParam : queryParams) {
			RestClient rc = new RestClient(sgmaster.buildSgAdminUrl());
			HashMap<String, String> objToPost = new HashMap<String, String>();
			objToPost.put("doc", "value");
			JSONObject jsonData = new JSONObject(objToPost);
			Response response = rc.sendPut(DEFAULT_SG_BUCKET + "/doc",
					jsonData, null);
			response = rc.sendPut(DEFAULT_SG_BUCKET + "/doc", jsonData,
					queryParam);
			checkDocumentExistConflict409(response);
		}
	}

	public void putExistingDocumentIfMatchAdmin() throws JSchException,
			IOException, SftpException, CouchbaseLiteException, JSONException {
		RemoteExecutor reSG = new RemoteExecutor(sgmaster);

		SGHelper.killAllSG(reSG);
		SGHelper.runSG(reSG, SG_PATH, null);
		List<ImmutableMap<String, String>> queryParams = Arrays.asList(null,
				ImmutableMap.<String, String> builder()
						.put("new_edits", "true").build());
		for (ImmutableMap<String, String> queryParam : queryParams) {
			RestClient rc = new RestClient(sgmaster.buildSgAdminUrl());
			HashMap<String, String> objToPost = new HashMap<String, String>();
			objToPost.put("doc", "value");
			JSONObject jsonData = new JSONObject(objToPost);
			Response response = rc.sendPut(DEFAULT_SG_BUCKET + "/doc",
					jsonData, null);
			response = rc.sendPut(DEFAULT_SG_BUCKET + "/doc", jsonData,
					queryParam);
			Assert.assertEquals(409, response.getStatus());
			checkDocumentExistConflict409(response);
		}
	}

	public void putExistingDocumentWithNewEditFalseAdmin()
			throws JSchException, IOException, SftpException,
			CouchbaseLiteException, JSONException {
		RemoteExecutor reSG = new RemoteExecutor(sgmaster);

		SGHelper.killAllSG(reSG);
		SGHelper.runSG(reSG, SG_PATH, null);
		List<ImmutableMap<String, String>> queryParams = Arrays
				.asList(ImmutableMap.<String, String> builder()
						.put("new_edits", "false").build());
		for (ImmutableMap<String, String> queryParam : queryParams) {
			RestClient rc = new RestClient(sgmaster.buildSgAdminUrl());
			HashMap<String, String> objToPost = new HashMap<String, String>();
			objToPost.put("doc", "value");
			JSONObject jsonData = new JSONObject(objToPost);
			Response response = rc.sendPut(DEFAULT_SG_BUCKET + "/doc",
					jsonData, null);
			response = rc.sendPut(DEFAULT_SG_BUCKET + "/doc", jsonData,
					queryParam);
			Assert.assertEquals(400, response.getStatus());
			Assert.assertEquals(Response.Status.BAD_REQUEST.toString(),
					response.getStatusInfo().toString());
			MultivaluedMap<String, String> headers = response
					.getStringHeaders();
			Set<String> expectedHeadersKeys = new HashSet<String>(
					Arrays.asList("Date", "Content-Length", "Content-Type",
							"Server"));
			Assert.assertEquals("Found headers: " + headers,
					expectedHeadersKeys, headers.keySet());
			Assert.assertEquals("Found headers: " + headers,
					MediaType.APPLICATION_JSON, headers.get("Content-Type")
							.get(0));
			Assert.assertTrue("Found headers: " + headers,
					Integer.valueOf(headers.get("Content-Length").get(0)) > 0);
			Assert.assertTrue("Found headers: " + headers, headers
					.get("Server").get(0).startsWith("Couchbase Sync Gateway"));
			assertTrue(response.getDate().compareTo(new Date()) < 0);

			Assert.assertEquals(MediaType.APPLICATION_JSON_TYPE,
					response.getMediaType());
			Assert.assertTrue(response.getLength() > 0);
			OrderedJSONObject json = response
					.readEntity(OrderedJSONObject.class);
			String regex = "{\"error\":\"Bad Request\",\"reason\":\"Bad _revisions\"}";
			assertEquals("Found in response:" + json, json.toString(), regex);
		}
	}

	public void putUpdatedExistingDocumentWithNewEditFalseAdmin()
			throws JSchException, IOException, SftpException,
			CouchbaseLiteException, JSONException {
		RemoteExecutor reSG = new RemoteExecutor(sgmaster);

		SGHelper.killAllSG(reSG);
		SGHelper.runSG(reSG, SG_PATH, null);
		List<ImmutableMap<String, String>> queryParams = Arrays
				.asList(ImmutableMap.<String, String> builder()
						.put("new_edits", "false").build());
		for (ImmutableMap<String, String> queryParam : queryParams) {
			RestClient rc = new RestClient(sgmaster.buildSgAdminUrl());
			HashMap<String, String> objToPost = new HashMap<String, String>();
			objToPost.put("doc", "value");
			JSONObject jsonData = new JSONObject(objToPost);
			Response response = rc.sendPut(DEFAULT_SG_BUCKET + "/doc",
					jsonData, null);
			String rev = getRevById(rc, "doc");
			response = rc.sendPut(DEFAULT_SG_BUCKET + "/doc/" + rev, jsonData,
					queryParam);
			checkDocumentExistConflict409(response);
		}
	}

	// https://github.com/couchbase/sync_gateway/issues/744
	public void putNonExistDocumentByRevAdmin() throws JSchException,
			IOException, SftpException, CouchbaseLiteException, JSONException {
		RemoteExecutor reSG = new RemoteExecutor(sgmaster);

		SGHelper.killAllSG(reSG);
		SGHelper.runSG(reSG, SG_PATH, null);
		List<String> urls = Arrays.asList(sgmaster.buildSgAdminUrl());
		for (String url : urls) {
			RestClient rc = new RestClient(url);
			List<String> ids = addDocs(rc, 1);
			String rev = getRevById(rc, ids.get(0));
			ImmutableMap<String, String> queryParam = ImmutableMap
					.<String, String> builder().put("rev", rev).build();
			HashMap<String, String> objToPost = new HashMap<String, String>();
			objToPost.put("doc", "value_NEW");
			JSONObject jsonData = new JSONObject(objToPost);
			Response response = rc.sendPut(DEFAULT_SG_BUCKET + "/doc",
					jsonData, queryParam);
			Assert.assertEquals(201, response.getStatus());
			Assert.assertEquals(Response.Status.CREATED.toString(), response
					.getStatusInfo().toString());
			MultivaluedMap<String, String> headers = response
					.getStringHeaders();
			Set<String> expectedHeadersKeys = new HashSet<String>(
					Arrays.asList("Date", "Content-Length", "Etag",
							"Content-Type", "Server"));
			Assert.assertEquals("Found headers: " + headers,
					expectedHeadersKeys, headers.keySet());
			Assert.assertTrue("Found entity tag:" + headers.get("Etag"),
					headers.get("Etag").get(0).startsWith("1-"));
			Assert.assertEquals("Found headers: " + headers,
					MediaType.APPLICATION_JSON, headers.get("Content-Type")
							.get(0));
			Assert.assertTrue("Found headers: " + headers,
					Integer.valueOf(headers.get("Content-Length").get(0)) > 0);
			Assert.assertTrue("Found headers: " + headers, headers
					.get("Server").get(0).startsWith("Couchbase Sync Gateway"));
			assertTrue(response.getDate().compareTo(new Date()) < 0);

			Assert.assertEquals(MediaType.APPLICATION_JSON_TYPE,
					response.getMediaType());
			Assert.assertTrue(response.getLength() > 0);
			OrderedJSONObject json = response
					.readEntity(OrderedJSONObject.class);
			String regex = "\\{\"id\":\"(.*)\",\"ok\":true,\"rev\":\"1-(.*)\"\\}";
			assertTrue("Found in response:" + json,
					json.toString().matches(regex));

		}
	}

	public void putDocumentByRevAdmin() throws JSchException, IOException,
			SftpException, CouchbaseLiteException, JSONException {
		RemoteExecutor reSG = new RemoteExecutor(sgmaster);

		SGHelper.killAllSG(reSG);
		SGHelper.runSG(reSG, SG_PATH, null);
		List<String> urls = Arrays.asList(sgmaster.buildSgAdminUrl());
		for (String url : urls) {
			RestClient rc = new RestClient(url);
			List<String> ids = addDocs(rc, 1);
			String rev = getRevById(rc, ids.get(0));
			ImmutableMap<String, String> queryParam = ImmutableMap
					.<String, String> builder().put("rev", rev).build();
			HashMap<String, String> objToPost = new HashMap<String, String>();
			objToPost.put("doc", "value_NEW");
			JSONObject jsonData = new JSONObject(objToPost);
			Response response = rc.sendPut(
					DEFAULT_SG_BUCKET + "/" + ids.get(0), jsonData, queryParam);
			Assert.assertEquals(201, response.getStatus());
			Assert.assertEquals(Response.Status.CREATED.toString(), response
					.getStatusInfo().toString());
			MultivaluedMap<String, String> headers = response
					.getStringHeaders();
			Set<String> expectedHeadersKeys = new HashSet<String>(
					Arrays.asList("Date", "Content-Length", "Etag",
							"Content-Type", "Server"));
			Assert.assertEquals("Found headers: " + headers,
					expectedHeadersKeys, headers.keySet());
			Assert.assertTrue("Found entity tag:" + headers.get("Etag"),
					headers.get("Etag").get(0).startsWith("2-"));
			Assert.assertEquals("Found headers: " + headers,
					MediaType.APPLICATION_JSON, headers.get("Content-Type")
							.get(0));
			Assert.assertTrue("Found headers: " + headers,
					Integer.valueOf(headers.get("Content-Length").get(0)) > 0);
			Assert.assertTrue("Found headers: " + headers, headers
					.get("Server").get(0).startsWith("Couchbase Sync Gateway"));
			assertTrue(response.getDate().compareTo(new Date()) < 0);

			Assert.assertEquals(MediaType.APPLICATION_JSON_TYPE,
					response.getMediaType());
			Assert.assertTrue(response.getLength() > 0);
			OrderedJSONObject json = response
					.readEntity(OrderedJSONObject.class);
			String regex = "\\{\"id\":\"(.*)\",\"ok\":true,\"rev\":\"2-(.*)\"\\}";
			assertTrue("Found in response:" + json,
					json.toString().matches(regex));

		}
	}

	public void putDocumentWithIfMatchRevNewEditTrueAdmin()
			throws JSchException, IOException, SftpException,
			CouchbaseLiteException, JSONException {
		RemoteExecutor reSG = new RemoteExecutor(sgmaster);

		SGHelper.killAllSG(reSG);
		SGHelper.runSG(reSG, SG_PATH, null);
		List<ImmutableMap<String, String>> queryParams = Arrays.asList(null,
				ImmutableMap.<String, String> builder()
						.put("new_edits", "true").build());

		for (ImmutableMap<String, String> queryParam : queryParams) {
			RestClient rc = new RestClient(sgmaster.buildSgAdminUrl());
			List<String> ids = addDocs(rc, 1);
			String rev = getRevById(rc, ids.get(0));
			@SuppressWarnings({ "rawtypes", "unchecked" })
			MultivaluedMap<String, Object> headersPut = new MultivaluedHashMap();
			headersPut.add("If-Match", rev);
			HashMap<String, String> objToPost = new HashMap<String, String>();
			objToPost.put("doc", "value_NEW");
			JSONObject jsonData = new JSONObject(objToPost);
			Response response = rc.sendPut(
					DEFAULT_SG_BUCKET + "/" + ids.get(0), jsonData, queryParam,
					headersPut);
			Assert.assertEquals(201, response.getStatus());
			Assert.assertEquals(Response.Status.CREATED.toString(), response
					.getStatusInfo().toString());
			MultivaluedMap<String, String> headers = response
					.getStringHeaders();
			Set<String> expectedHeadersKeys = new HashSet<String>(
					Arrays.asList("Date", "Content-Length", "Etag",
							"Content-Type", "Server"));
			Assert.assertEquals("Found headers: " + headers,
					expectedHeadersKeys, headers.keySet());
			Assert.assertTrue("Found entity tag:" + headers.get("Etag"),
					headers.get("Etag").get(0).startsWith("2-"));
			Assert.assertEquals("Found headers: " + headers,
					MediaType.APPLICATION_JSON, headers.get("Content-Type")
							.get(0));
			Assert.assertTrue("Found headers: " + headers,
					Integer.valueOf(headers.get("Content-Length").get(0)) > 0);
			Assert.assertTrue("Found headers: " + headers, headers
					.get("Server").get(0).startsWith("Couchbase Sync Gateway"));
			assertTrue(response.getDate().compareTo(new Date()) < 0);

			Assert.assertEquals(MediaType.APPLICATION_JSON_TYPE,
					response.getMediaType());
			Assert.assertTrue(response.getLength() > 0);
			OrderedJSONObject json = response
					.readEntity(OrderedJSONObject.class);
			String regex = "\\{\"id\":\"(.*)\",\"ok\":true,\"rev\":\"2-(.*)\"\\}";
			assertTrue("Found in response:" + json,
					json.toString().matches(regex));

		}
	}

	public void putDocumentWithIfMatchRevNewEditFalseAdmin()
			throws JSchException, IOException, SftpException,
			CouchbaseLiteException, JSONException {
		RemoteExecutor reSG = new RemoteExecutor(sgmaster);

		SGHelper.killAllSG(reSG);
		SGHelper.runSG(reSG, SG_PATH, null);
		List<ImmutableMap<String, String>> queryParams = Arrays
				.asList(ImmutableMap.<String, String> builder()
						.put("new_edits", "false").build());

		for (ImmutableMap<String, String> queryParam : queryParams) {
			RestClient rc = new RestClient(sgmaster.buildSgAdminUrl());
			List<String> ids = addDocs(rc, 1);
			String rev = getRevById(rc, ids.get(0));
			@SuppressWarnings({ "rawtypes", "unchecked" })
			MultivaluedMap<String, Object> headersPut = new MultivaluedHashMap();
			headersPut.add("If-Match", rev);
			HashMap<String, String> objToPost = new HashMap<String, String>();
			objToPost.put("doc", "value_NEW");
			JSONObject jsonData = new JSONObject(objToPost);
			Response response = rc.sendPut(
					DEFAULT_SG_BUCKET + "/" + ids.get(0), jsonData, queryParam,
					headersPut);
			Assert.assertEquals(400, response.getStatus());
			Assert.assertEquals(Response.Status.BAD_REQUEST.toString(),
					response.getStatusInfo().toString());
			MultivaluedMap<String, String> headers = response
					.getStringHeaders();
			Set<String> expectedHeadersKeys = new HashSet<String>(
					Arrays.asList("Date", "Content-Length", "Content-Type",
							"Server"));
			Assert.assertEquals("Found headers: " + headers,
					expectedHeadersKeys, headers.keySet());
			Assert.assertEquals("Found headers: " + headers,
					MediaType.APPLICATION_JSON, headers.get("Content-Type")
							.get(0));
			Assert.assertTrue("Found headers: " + headers,
					Integer.valueOf(headers.get("Content-Length").get(0)) > 0);
			Assert.assertTrue("Found headers: " + headers, headers
					.get("Server").get(0).startsWith("Couchbase Sync Gateway"));
			assertTrue(response.getDate().compareTo(new Date()) < 0);

			Assert.assertEquals(MediaType.APPLICATION_JSON_TYPE,
					response.getMediaType());
			Assert.assertTrue(response.getLength() > 0);
			OrderedJSONObject json = response
					.readEntity(OrderedJSONObject.class);
			String regex = "{\"error\":\"Bad Request\",\"reason\":\"Bad _revisions\"}";
			assertEquals("Found in response:" + json, json.toString(), regex);

		}
	}

	public void putDocumentWithIfMatchInvalidRevAdmin() throws JSchException,
			IOException, SftpException, CouchbaseLiteException, JSONException {
		RemoteExecutor reSG = new RemoteExecutor(sgmaster);

		SGHelper.killAllSG(reSG);
		SGHelper.runSG(reSG, SG_PATH, null);
		List<String> urls = Arrays.asList(sgmaster.buildSgAdminUrl());
		for (String url : urls) {
			RestClient rc = new RestClient(url);
			List<String> ids = addDocs(rc, 1);
			String rev = getRevById(rc, ids.get(0));
			@SuppressWarnings({ "rawtypes", "unchecked" })
			MultivaluedMap<String, Object> headersPut = new MultivaluedHashMap();
			headersPut.add("If-Match", rev + "1");// added wrong rev
			HashMap<String, String> objToPost = new HashMap<String, String>();
			objToPost.put("doc", "value_NEW");
			JSONObject jsonData = new JSONObject(objToPost);
			Response response = rc.sendPut(
					DEFAULT_SG_BUCKET + "/" + ids.get(0), jsonData, null,
					headersPut);
			checkDocumentRevisionConflict409(response);
		}
	}

	public void putDocNonExistDB() throws JSchException, IOException,
			SftpException, CouchbaseLiteException, JSONException {
		RemoteExecutor reSG = new RemoteExecutor(sgmaster);

		SGHelper.killAllSG(reSG);
		SGHelper.runSG(reSG, SG_PATH, null);
		List<String> urls = Arrays.asList(sgmaster.buildSgAdminUrl());
		for (String url : urls) {
			RestClient rc = new RestClient(url);
			HashMap<String, String> objToPost = new HashMap<String, String>();
			objToPost.put("doc", "value");
			JSONObject jsonData = new JSONObject(objToPost);
			Response response = rc.sendPut(DEFAULT_SG_BUCKET + "1/doc",
					jsonData, null);
			checkNonExistDB404(response);
		}
	}

	public void getDocsAdmin() throws JSchException, IOException,
			SftpException, CouchbaseLiteException, JSONException {
		RemoteExecutor reSG = new RemoteExecutor(sgmaster);

		SGHelper.killAllSG(reSG);
		SGHelper.runSG(reSG, SG_PATH, null);
		RestClient rc = new RestClient(sgmaster.buildSgAdminUrl());
		HashMap<String, String> objToPost = new HashMap<String, String>();
		objToPost.put("_id", "my_id");
		JSONObject jsonData = new JSONObject(objToPost);
		rc.sendPost(DEFAULT_SG_BUCKET + "/", jsonData, null);

		List<ImmutableMap<String, String>> queryParams = Arrays.asList(
				null,
				ImmutableMap.<String, String> builder()
						.put("attachments", "true").build(), ImmutableMap
						.<String, String> builder().put("attachments", "false")
						.build(),
				ImmutableMap.<String, String> builder().put("revs", "true")
						.build(),
				ImmutableMap.<String, String> builder().put("revs", "false")
						.build(),
				ImmutableMap.<String, String> builder().put("atts_since", "1")
						.build(),
				ImmutableMap.<String, String> builder().put("atts_since", "2")
						.build(),
				ImmutableMap.<String, String> builder().put("atts_since", "0")
						.build());
		for (ImmutableMap<String, String> queryParam : queryParams) {
			Response response = rc.getClientResponse(DEFAULT_SG_BUCKET
					+ "/my_id", queryParam);
			Assert.assertEquals(200, response.getStatus());
			Assert.assertEquals(Response.Status.OK.toString(), response
					.getStatusInfo().toString());
			MultivaluedMap<String, String> headers = response
					.getStringHeaders();
			Set<String> expectedHeadersKeys = new HashSet<String>(
					Arrays.asList("Date", "Content-Length", "Etag",
							"Content-Type", "Server"));
			Assert.assertEquals("Found headers: " + headers,
					expectedHeadersKeys, headers.keySet());
			Assert.assertTrue("Found entity tag:" + headers.get("Etag"),
					headers.get("Etag").get(0).startsWith("1-"));
			Assert.assertEquals("Found headers: " + headers,
					MediaType.APPLICATION_JSON, headers.get("Content-Type")
							.get(0));
			Assert.assertTrue("Found headers: " + headers,
					Integer.valueOf(headers.get("Content-Length").get(0)) > 0);
			Assert.assertTrue("Found headers: " + headers, headers
					.get("Server").get(0).startsWith("Couchbase Sync Gateway"));
			assertTrue(response.getDate().compareTo(new Date()) < 0);

			Assert.assertEquals(MediaType.APPLICATION_JSON_TYPE,
					response.getMediaType());
			Assert.assertTrue(response.getLength() > 0);
			String json, regex;
			if (queryParam != null
					&& queryParam.equals(ImmutableMap
							.<String, String> builder().put("revs", "true")
							.build())) {
				json = response.readEntity(String.class);
				regex = "\\{\"_id\":\"my_id\",\"_rev\":\"1-(.*)\",\"_revisions\":\\{\"ids\":\\[\"(.*)\"\\],\"start\":1\\}\\}";
			} else {
				OrderedJSONObject jsonObj = response
						.readEntity(OrderedJSONObject.class);
				json = jsonObj.toString();
				regex = "\\{\"_id\":\"my_id\",\"_rev\":\"1-(.*)\"\\}";

			}
			assertTrue("Found in response:" + json, json.matches(regex));

		}
	}

	public void getDocsInvalidOpen_revsAdmin() throws JSchException,
			IOException, SftpException, CouchbaseLiteException, JSONException {
		RemoteExecutor reSG = new RemoteExecutor(sgmaster);

		SGHelper.killAllSG(reSG);
		SGHelper.runSG(reSG, SG_PATH, null);
		RestClient rc = new RestClient(sgmaster.buildSgAdminUrl());
		HashMap<String, String> objToPost = new HashMap<String, String>();
		objToPost.put("_id", "my_id");
		JSONObject jsonData = new JSONObject(objToPost);
		rc.sendPost(DEFAULT_SG_BUCKET + "/", jsonData, null);

		List<ImmutableMap<String, String>> queryParams = Arrays
				.asList(ImmutableMap.<String, String> builder()
						.put("open_revs", "1-342342").build()

				);
		for (ImmutableMap<String, String> queryParam : queryParams) {
			Response response = rc.getClientResponse(DEFAULT_SG_BUCKET
					+ "/my_id", queryParam);
			checkBadRequest400(response,
					"{\"error\":\"Bad Request\",\"reason\":\"bad open_revs\"}");

		}
	}

	public void getDocsOpen_revsAdmin() throws JSchException, IOException,
			SftpException, CouchbaseLiteException, JSONException {
		RemoteExecutor reSG = new RemoteExecutor(sgmaster);

		SGHelper.killAllSG(reSG);
		SGHelper.runSG(reSG, SG_PATH, null);
		RestClient rc = new RestClient(sgmaster.buildSgAdminUrl());
		HashMap<String, String> objToPost = new HashMap<String, String>();
		objToPost.put("_id", "my_id");
		JSONObject jsonData = new JSONObject(objToPost);
		Response response = rc
				.sendPost(DEFAULT_SG_BUCKET + "/", jsonData, null);
		OrderedJSONObject jsonDocCreated = response
				.readEntity(OrderedJSONObject.class);
		String rev = jsonDocCreated.getString("rev");
		List<ImmutableMap<String, String>> queryParams = Arrays
				.asList(ImmutableMap.<String, String> builder()
						.put("open_revs", "[\"" + rev + "\"]").build()

				);
		for (ImmutableMap<String, String> queryParam : queryParams) {
			response = rc.getClientResponse(DEFAULT_SG_BUCKET + "/my_id",
					queryParam);
			Assert.assertEquals(200, response.getStatus());
			Assert.assertEquals(Response.Status.OK.toString(), response
					.getStatusInfo().toString());
			MultivaluedMap<String, String> headers = response
					.getStringHeaders();
			Set<String> expectedHeadersKeys = new HashSet<String>(
					Arrays.asList("Date", "Content-Length", "Content-Type",
							"Server"));
			Assert.assertEquals("Found headers: " + headers,
					expectedHeadersKeys, headers.keySet());
			Assert.assertEquals("Found headers: " + headers,
					MediaType.APPLICATION_JSON, headers.get("Content-Type")
							.get(0));
			Assert.assertTrue("Found headers: " + headers,
					Integer.valueOf(headers.get("Content-Length").get(0)) > 0);
			Assert.assertTrue("Found headers: " + headers, headers
					.get("Server").get(0).startsWith("Couchbase Sync Gateway"));
			assertTrue(response.getDate().compareTo(new Date()) < 0);

			Assert.assertEquals(MediaType.APPLICATION_JSON_TYPE,
					response.getMediaType());
			Assert.assertTrue(response.getLength() > 0);
			String json = response.readEntity(String.class);
			String regex = "\\[\\{\"ok\":\\{\"_id\":\"my_id\",\"_rev\":\"1\\-(.*)\"\\}\\}\\]";
			assertTrue("Found in response:" + json,
					json.replaceAll("(\\t|\\r?\\n)+", "").matches(regex));
		}
	}

	public void getDocsWithAttachmentAdmin() throws JSchException, IOException,
			SftpException, CouchbaseLiteException, JSONException {
		RemoteExecutor reSG = new RemoteExecutor(sgmaster);

		SGHelper.killAllSG(reSG);
		SGHelper.runSG(reSG, SG_PATH, null);
		RestClient rc = new RestClient(sgmaster.buildSgAdminUrl());

		List<String> ids = addDocsWithAttachment(rc, 1);
		List<ImmutableMap<String, String>> queryParams = Arrays.asList(
				null,
				ImmutableMap.<String, String> builder()
						.put("attachments", "true").build(), ImmutableMap
						.<String, String> builder().put("attachments", "false")
						.build(),
				ImmutableMap.<String, String> builder().put("revs", "true")
						.build(),
				ImmutableMap.<String, String> builder().put("revs", "false")
						.build(),
				ImmutableMap.<String, String> builder().put("atts_since", "1")
						.build(),
				ImmutableMap.<String, String> builder().put("atts_since", "2")
						.build(),
				ImmutableMap.<String, String> builder().put("atts_since", "0")
						.build());
		for (ImmutableMap<String, String> queryParam : queryParams) {
			Response response = rc.getClientResponse(DEFAULT_SG_BUCKET + "/"
					+ ids.get(0), queryParam);
			Assert.assertEquals(200, response.getStatus());
			Assert.assertEquals(Response.Status.OK.toString(), response
					.getStatusInfo().toString());
			MultivaluedMap<String, String> headers = response
					.getStringHeaders();
			Set<String> expectedHeadersKeys = new HashSet<String>(
					Arrays.asList("Date", "Content-Length", "Etag",
							"Content-Type", "Server"));
			Assert.assertEquals("Found headers: " + headers,
					expectedHeadersKeys, headers.keySet());
			Assert.assertTrue("Found entity tag:" + headers.get("Etag"),
					headers.get("Etag").get(0).startsWith("1-"));
			Assert.assertEquals("Found headers: " + headers,
					MediaType.APPLICATION_JSON, headers.get("Content-Type")
							.get(0));
			Assert.assertTrue("Found headers: " + headers,
					Integer.valueOf(headers.get("Content-Length").get(0)) > 0);
			Assert.assertTrue("Found headers: " + headers, headers
					.get("Server").get(0).startsWith("Couchbase Sync Gateway"));
			assertTrue(response.getDate().compareTo(new Date()) < 0);

			Assert.assertEquals(MediaType.APPLICATION_JSON_TYPE,
					response.getMediaType());
			Assert.assertTrue(response.getLength() > 0);
			String regex;
			String json = response.readEntity(String.class);
			System.out.println(json);
			if (queryParam != null
					&& queryParam.equals(ImmutableMap
							.<String, String> builder().put("revs", "true")
							.build())) {
				regex = "\\{\"_attachments\":\\{\"foo.txt\":\\{\"content_type\":\"text/plain\",\"digest\":\"sha1-ortavo\\+7s1JYc91U8\\/4oa9lKJFg=\",\"length\":29,\"revpos\":1,\"stub\":true\\}\\},\"_id\":\"attachment_doc1\",\"_rev\":\"1-(.*)\",\"_revisions\":\\{\"ids\":\\[\"f20345c5e2f45f6f5ab3d828b7bd32a6\"\\],\"start\":1\\}\\}";
			} else if (queryParam != null
					&& queryParam.equals(ImmutableMap
							.<String, String> builder()
							.put("attachments", "true").build())) {
				regex = "\\{\"_attachments\":\\{\"foo.txt\":\\{\"content_type\":\"text/plain\",\"data\":\"VGhpcyBpcyBhIGJhc2U2NCBlbmNvZGVkIHRleHQ=\",\"digest\":\"sha1-ortavo\\+7s1JYc91U8\\/4oa9lKJFg=\",\"length\":29,\"revpos\":1\\}\\},\"_id\":\"attachment_doc1\",\"_rev\":\"1-f20345c5e2f45f6f5ab3d828b7bd32a6\"}";
			} else {
				regex = "\\{\"_attachments\":\\{\"foo.txt\":\\{\"content_type\":\"text/plain\",\"digest\":\"sha1-ortavo\\+7s1JYc91U8\\/4oa9lKJFg=\",\"length\":29,\"revpos\":1,\"stub\":true\\}\\},\"_id\":\"attachment_doc1\",\"_rev\":\"1-(.*)\"\\}";
			}
			assertTrue("Found in response:" + json, json.matches(regex));

		}
	}

	public void getDocsWithAttachment() throws JSchException, IOException,
			SftpException, CouchbaseLiteException, JSONException {
		RemoteExecutor reSG = new RemoteExecutor(sgmaster);

		SGHelper.killAllSG(reSG);
		SGHelper.runSG(reSG, SG_PATH, null);
		RestClient rc = new RestClient(sgmaster.buildSgAdminUrl());

		List<String> ids = addDocsWithAttachment(rc, 1);
		List<ImmutableMap<String, String>> queryParams = Arrays.asList(
				null,
				ImmutableMap.<String, String> builder()
						.put("attachments", "true").build(), ImmutableMap
						.<String, String> builder().put("attachments", "false")
						.build(),
				ImmutableMap.<String, String> builder().put("revs", "true")
						.build(),
				ImmutableMap.<String, String> builder().put("revs", "false")
						.build(),
				ImmutableMap.<String, String> builder().put("atts_since", "1")
						.build(),
				ImmutableMap.<String, String> builder().put("atts_since", "2")
						.build(),
				ImmutableMap.<String, String> builder().put("atts_since", "0")
						.build());
		rc = new RestClient(sgmaster.buildSgUrl());
		for (ImmutableMap<String, String> queryParam : queryParams) {
			Response response = rc.getClientResponse(DEFAULT_SG_BUCKET + "/"
					+ ids.get(0), queryParam);
			checkUnauthorized401(response);
		}
	}

	public void deleteDocsAdmin() throws JSchException, IOException,
			SftpException, CouchbaseLiteException, JSONException {
		RemoteExecutor reSG = new RemoteExecutor(sgmaster);

		SGHelper.killAllSG(reSG);
		SGHelper.runSG(reSG, SG_PATH, null);
		RestClient rc = new RestClient(sgmaster.buildSgAdminUrl());
		HashMap<String, String> objToPost = new HashMap<String, String>();
		objToPost.put("_id", "my_id");
		JSONObject jsonData = new JSONObject(objToPost);

		Response response = rc
				.sendPost(DEFAULT_SG_BUCKET + "/", jsonData, null);
		OrderedJSONObject jsonDocCreated = response
				.readEntity(OrderedJSONObject.class);
		String rev = jsonDocCreated.getString("rev");
		List<ImmutableMap<String, String>> queryParams = Arrays
				.asList(ImmutableMap.<String, String> builder().put("rev", rev)
						.build()

				);
		for (ImmutableMap<String, String> queryParam : queryParams) {
			response = rc
					.delete(DEFAULT_SG_BUCKET + "/my_id", queryParam, null);
			Assert.assertEquals(200, response.getStatus());
			Assert.assertEquals(Response.Status.OK.toString(), response
					.getStatusInfo().toString());
			MultivaluedMap<String, String> headers = response
					.getStringHeaders();
			Set<String> expectedHeadersKeys = new HashSet<String>(
					Arrays.asList("Date", "Content-Length", "Content-Type",
							"Server"));
			Assert.assertEquals("Found headers: " + headers,
					expectedHeadersKeys, headers.keySet());
			Assert.assertEquals("Found headers: " + headers,
					MediaType.APPLICATION_JSON, headers.get("Content-Type")
							.get(0));
			Assert.assertTrue("Found headers: " + headers,
					Integer.valueOf(headers.get("Content-Length").get(0)) > 0);
			Assert.assertTrue("Found headers: " + headers, headers
					.get("Server").get(0).startsWith("Couchbase Sync Gateway"));
			assertTrue(response.getDate().compareTo(new Date()) < 0);

			Assert.assertEquals(MediaType.APPLICATION_JSON_TYPE,
					response.getMediaType());
			Assert.assertTrue(response.getLength() > 0);
			String json = response.readEntity(String.class);
			String regex = "\\{\"id\":\"my_id\",\"ok\":true,\"rev\":\"2-(.*)\"\\}";
			assertTrue("Found in response:" + json, json.matches(regex));
		}

	}

	public void deleteDocWitoutRevAdmin() throws JSchException, IOException,
			SftpException, CouchbaseLiteException, JSONException {
		RemoteExecutor reSG = new RemoteExecutor(sgmaster);

		SGHelper.killAllSG(reSG);
		SGHelper.runSG(reSG, SG_PATH, null);
		RestClient rc = new RestClient(sgmaster.buildSgAdminUrl());
		HashMap<String, String> objToPost = new HashMap<String, String>();
		objToPost.put("_id", "my_id");
		JSONObject jsonData = new JSONObject(objToPost);
		Response response = rc
				.sendPost(DEFAULT_SG_BUCKET + "/", jsonData, null);

		response = rc.delete(DEFAULT_SG_BUCKET + "/my_id", null, null);
		checkDocumentExistConflict409(response);
	}

	public void deleteDocsRevsAdmin() throws JSchException, IOException,
			SftpException, CouchbaseLiteException, JSONException { // aaaaaaaaaaaaaaaaaaaaa
		RemoteExecutor reSG = new RemoteExecutor(sgmaster);

		SGHelper.killAllSG(reSG);
		SGHelper.runSG(reSG, SG_PATH, null);
		RestClient rc = new RestClient(sgmaster.buildSgAdminUrl());
		HashMap<String, String> objToPost = new HashMap<String, String>();
		objToPost.put("_id", "my_id");
		JSONObject jsonData = new JSONObject(objToPost);
		Response response = rc
				.sendPost(DEFAULT_SG_BUCKET + "/", jsonData, null);
		OrderedJSONObject jsonDocCreated = response
				.readEntity(OrderedJSONObject.class);
		String rev = jsonDocCreated.getString("rev");
		@SuppressWarnings({ "rawtypes", "unchecked" })
		MultivaluedMap<String, Object> headersPut = new MultivaluedHashMap();
		headersPut.add("If-Match", rev);

		response = rc.delete(DEFAULT_SG_BUCKET + "/my_id", null, headersPut);
		Assert.assertEquals(200, response.getStatus());
		Assert.assertEquals(Response.Status.OK.toString(), response
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
		String json = response.readEntity(String.class);
		String regex = "\\{\"id\":\"my_id\",\"ok\":true,\"rev\":\"2-(.*)\"\\}";
		assertTrue("Found in response:" + json, json.matches(regex));

	}

	public void deleteDocsWithAttachmentAdmin() throws JSchException,
			IOException, SftpException, CouchbaseLiteException, JSONException {
		RemoteExecutor reSG = new RemoteExecutor(sgmaster);

		SGHelper.killAllSG(reSG);
		SGHelper.runSG(reSG, SG_PATH, null);
		RestClient rc = new RestClient(sgmaster.buildSgAdminUrl());

		List<String> ids = addDocsWithAttachment(rc, 1);
		String rev = getRevById(rc, ids.get(0));

		List<ImmutableMap<String, String>> queryParams = Arrays
				.asList(ImmutableMap.<String, String> builder().put("rev", rev)
						.build()

				);
		for (ImmutableMap<String, String> queryParam : queryParams) {

			Response response = rc.delete(DEFAULT_SG_BUCKET + "/" + ids.get(0),
					queryParam, null);
			Assert.assertEquals(200, response.getStatus());
			Assert.assertEquals(Response.Status.OK.toString(), response
					.getStatusInfo().toString());
			MultivaluedMap<String, String> headers = response
					.getStringHeaders();
			Set<String> expectedHeadersKeys = new HashSet<String>(
					Arrays.asList("Date", "Content-Length", "Content-Type",
							"Server"));
			Assert.assertEquals("Found headers: " + headers,
					expectedHeadersKeys, headers.keySet());
			Assert.assertEquals("Found headers: " + headers,
					MediaType.APPLICATION_JSON, headers.get("Content-Type")
							.get(0));
			Assert.assertTrue("Found headers: " + headers,
					Integer.valueOf(headers.get("Content-Length").get(0)) > 0);
			Assert.assertTrue("Found headers: " + headers, headers
					.get("Server").get(0).startsWith("Couchbase Sync Gateway"));
			assertTrue(response.getDate().compareTo(new Date()) < 0);

			Assert.assertEquals(MediaType.APPLICATION_JSON_TYPE,
					response.getMediaType());
			Assert.assertTrue(response.getLength() > 0);
			String json = response.readEntity(String.class);
			String regex = "\\{\"id\":\"attachment_doc1\",\"ok\":true,\"rev\":\"2-(.*)\"\\}";
			assertTrue("Found in response:" + json, json.matches(regex));
		}
	}

	public void deleteDocsWithAttachmentTwiceAdmin() throws JSchException,
			IOException, SftpException, CouchbaseLiteException, JSONException {
		RemoteExecutor reSG = new RemoteExecutor(sgmaster);

		SGHelper.killAllSG(reSG);
		SGHelper.runSG(reSG, SG_PATH, null);
		RestClient rc = new RestClient(sgmaster.buildSgAdminUrl());

		List<String> ids = addDocsWithAttachment(rc, 1);
		String rev = getRevById(rc, ids.get(0));

		List<ImmutableMap<String, String>> queryParams = Arrays
				.asList(ImmutableMap.<String, String> builder().put("rev", rev)
						.build()

				);
		for (ImmutableMap<String, String> queryParam : queryParams) {

			Response response = rc.delete(DEFAULT_SG_BUCKET + "/" + ids.get(0),
					queryParam, null);
			Assert.assertEquals(200, response.getStatus());
			response = rc.delete(DEFAULT_SG_BUCKET + "/" + ids.get(0),
					queryParam, null);
			checkDocumentRevisionConflict409(response);
		}
	}

	public void deleteDocsWithAttachment() throws JSchException, IOException,
			SftpException, CouchbaseLiteException, JSONException {
		RemoteExecutor reSG = new RemoteExecutor(sgmaster);

		SGHelper.killAllSG(reSG);
		SGHelper.runSG(reSG, SG_PATH, null);
		RestClient rc = new RestClient(sgmaster.buildSgAdminUrl());

		List<String> ids = addDocsWithAttachment(rc, 1);
		String rev = getRevById(rc, ids.get(0));

		List<ImmutableMap<String, String>> queryParams = Arrays
				.asList(ImmutableMap.<String, String> builder().put("rev", rev)
						.build()

				);
		rc = new RestClient(sgmaster.buildSgUrl());
		for (ImmutableMap<String, String> queryParam : queryParams) {

			Response response = rc.delete(DEFAULT_SG_BUCKET + "/" + ids.get(0),
					queryParam, null);
			checkUnauthorized401(response);
		}
	}

	public void putDocAtachmentAdmin() throws JSchException, IOException,
			SftpException, CouchbaseLiteException, JSONException {
		RemoteExecutor reSG = new RemoteExecutor(sgmaster);

		SGHelper.killAllSG(reSG);
		SGHelper.runSG(reSG, SG_PATH, null);
		List<JSONObject> docsToPost = new ArrayList<JSONObject>();

		HashMap<String, Object> objToPostAtt = new HashMap<String, Object>();
		objToPostAtt.put("_id", "attachment_doc1");
		String attachmentItem = "{\"foo.txt\":{\"content_type\":\"text\\/plain\",\"data\": \"VGhpcyBpcyBhIGJhc2U2NCBlbmNvZGVkIHRleHQ=\"}}";
		objToPostAtt.put("_attachments", new JSONObject(attachmentItem));
		docsToPost.add(new JSONObject(objToPostAtt));

		HashMap<String, Object> objToPostMultAtt = new HashMap<String, Object>();
		objToPostMultAtt.put("_id", "attachment_doc2");
		String attachmentMultItem = "{\"foo.txt\":{\"content_type\":\"text\\/plain\",\"data\": \"VGhpcyBpcyBhIGJhc2U2NCBlbmNvZGVkIHRleHQ=\"},\"bar.txt\":{\"content_type\":\"text\\/plain\",\"data\": \"VGhpcyBpcyBhIGJhc2U2NCBlbmNvZGVkIHRleHQ=\"}}";
		objToPostMultAtt
				.put("_attachments", new JSONObject(attachmentMultItem));
		docsToPost.add(new JSONObject(objToPostMultAtt));

		for (JSONObject jsonOject : docsToPost) {
			RestClient rc = new RestClient(sgmaster.buildSgAdminUrl());

			Response response = rc.sendPost(DEFAULT_SG_BUCKET + "/", jsonOject,
					null);
			Assert.assertEquals(200, response.getStatus());
			String rev = getRevById(rc, jsonOject.getString("_id"));
			ImmutableMap<String, String> queryParam = ImmutableMap
					.<String, String> builder().put("rev", rev).build();

			attachmentItem = "{\"foo.txt\":{\"content_type\":\"text\\/plain\",\"data\": \"AGhpcyBpcyBhIGJhc2U2NCBlbmNvZGVkIHRleHQ=\"}}";
			response = rc.sendPut(
					DEFAULT_SG_BUCKET + "/" + jsonOject.getString("_id")
							+ "/foo.txt", attachmentItem, queryParam);
			Assert.assertEquals(201, response.getStatus());
			Assert.assertEquals(Response.Status.CREATED.toString(), response
					.getStatusInfo().toString());
			MultivaluedMap<String, String> headers = response
					.getStringHeaders();
			Set<String> expectedHeadersKeys = new HashSet<String>(
					Arrays.asList("Date", "Content-Length", "Etag",
							"Content-Type", "Server"));
			Assert.assertEquals("Found headers: " + headers,
					expectedHeadersKeys, headers.keySet());
			Assert.assertTrue("Found entity tag:" + headers.get("Etag"),
					headers.get("Etag").get(0).startsWith("2-"));
			Assert.assertEquals("Found headers: " + headers,
					MediaType.APPLICATION_JSON, headers.get("Content-Type")
							.get(0));
			Assert.assertTrue("Found headers: " + headers,
					Integer.valueOf(headers.get("Content-Length").get(0)) > 0);
			Assert.assertTrue("Found headers: " + headers, headers
					.get("Server").get(0).startsWith("Couchbase Sync Gateway"));
			assertTrue(response.getDate().compareTo(new Date()) < 0);

			Assert.assertEquals(MediaType.APPLICATION_JSON_TYPE,
					response.getMediaType());
			Assert.assertTrue(response.getLength() > 0);
			OrderedJSONObject json = response
					.readEntity(OrderedJSONObject.class);
			String regex = "\\{\"id\":\"(.*)\",\"ok\":true,\"rev\":\"2-(.*)\"\\}";
			assertTrue("Found in response:" + json,
					json.toString().matches(regex));
		}

	}

	public void putDocAtachmentIfMatchAdmin() throws JSchException,
			IOException, SftpException, CouchbaseLiteException, JSONException {
		RemoteExecutor reSG = new RemoteExecutor(sgmaster);

		SGHelper.killAllSG(reSG);
		SGHelper.runSG(reSG, SG_PATH, null);
		List<JSONObject> docsToPost = new ArrayList<JSONObject>();

		HashMap<String, Object> objToPostAtt = new HashMap<String, Object>();
		objToPostAtt.put("_id", "attachment_doc1");
		String attachmentItem = "{\"foo.txt\":{\"content_type\":\"text\\/plain\",\"data\": \"VGhpcyBpcyBhIGJhc2U2NCBlbmNvZGVkIHRleHQ=\"}}";
		objToPostAtt.put("_attachments", new JSONObject(attachmentItem));
		docsToPost.add(new JSONObject(objToPostAtt));

		HashMap<String, Object> objToPostMultAtt = new HashMap<String, Object>();
		objToPostMultAtt.put("_id", "attachment_doc2");
		String attachmentMultItem = "{\"foo.txt\":{\"content_type\":\"text\\/plain\",\"data\": \"VGhpcyBpcyBhIGJhc2U2NCBlbmNvZGVkIHRleHQ=\"},\"bar.txt\":{\"content_type\":\"text\\\\/plain\",\"data\": \"VGhpcyBpcyBhIGJhc2U2NCBlbmNvZGVkIHRleHQ=\"}}";
		objToPostMultAtt
				.put("_attachments", new JSONObject(attachmentMultItem));
		docsToPost.add(new JSONObject(objToPostMultAtt));

		for (JSONObject jsonOject : docsToPost) {
			RestClient rc = new RestClient(sgmaster.buildSgAdminUrl());

			Response response = rc.sendPost(DEFAULT_SG_BUCKET + "/", jsonOject,
					null);
			Assert.assertEquals(200, response.getStatus());
			String rev = getRevById(rc, jsonOject.getString("_id"));

			@SuppressWarnings({ "rawtypes", "unchecked" })
			MultivaluedMap<String, Object> headersPut = new MultivaluedHashMap();
			headersPut.add("If-Match", rev);

			attachmentItem = "{\"foo.txt\":{\"content_type\":\"text\\/plain\",\"data\": \"AGhpcyBpcyBhIGJhc2U2NCBlbmNvZGVkIHRleHQ=\"}}";
			response = rc.sendPut(
					DEFAULT_SG_BUCKET + "/" + jsonOject.getString("_id")
							+ "/foo.txt", attachmentItem, null, headersPut);
			Assert.assertEquals(201, response.getStatus());
			Assert.assertEquals(Response.Status.CREATED.toString(), response
					.getStatusInfo().toString());
			MultivaluedMap<String, String> headers = response
					.getStringHeaders();
			Set<String> expectedHeadersKeys = new HashSet<String>(
					Arrays.asList("Date", "Content-Length", "Etag",
							"Content-Type", "Server"));
			Assert.assertEquals("Found headers: " + headers,
					expectedHeadersKeys, headers.keySet());
			Assert.assertTrue("Found entity tag:" + headers.get("Etag"),
					headers.get("Etag").get(0).startsWith("2-"));
			Assert.assertEquals("Found headers: " + headers,
					MediaType.APPLICATION_JSON, headers.get("Content-Type")
							.get(0));
			Assert.assertTrue("Found headers: " + headers,
					Integer.valueOf(headers.get("Content-Length").get(0)) > 0);
			Assert.assertTrue("Found headers: " + headers, headers
					.get("Server").get(0).startsWith("Couchbase Sync Gateway"));
			assertTrue(response.getDate().compareTo(new Date()) < 0);

			Assert.assertEquals(MediaType.APPLICATION_JSON_TYPE,
					response.getMediaType());
			Assert.assertTrue(response.getLength() > 0);
			OrderedJSONObject json = response
					.readEntity(OrderedJSONObject.class);
			String regex = "\\{\"id\":\"(.*)\",\"ok\":true,\"rev\":\"2-(.*)\"\\}";
			assertTrue("Found in response:" + json,
					json.toString().matches(regex));
		}

	}

	public void putDocAtachmentWithoutRevAdmin() throws JSchException,
			IOException, SftpException, CouchbaseLiteException, JSONException {
		RemoteExecutor reSG = new RemoteExecutor(sgmaster);

		SGHelper.killAllSG(reSG);
		SGHelper.runSG(reSG, SG_PATH, null);
		List<JSONObject> docsToPost = new ArrayList<JSONObject>();

		HashMap<String, Object> objToPostAtt = new HashMap<String, Object>();
		objToPostAtt.put("_id", "attachment_doc1");
		String attachmentItem = "{\"foo.txt\":{\"content_type\":\"text\\/plain\",\"data\": \"VGhpcyBpcyBhIGJhc2U2NCBlbmNvZGVkIHRleHQ=\"}}";
		objToPostAtt.put("_attachments", new JSONObject(attachmentItem));
		docsToPost.add(new JSONObject(objToPostAtt));

		HashMap<String, Object> objToPostMultAtt = new HashMap<String, Object>();
		objToPostMultAtt.put("_id", "attachment_doc2");
		String attachmentMultItem = "{\"foo.txt\":{\"content_type\":\"text\\/plain\",\"data\": \"VGhpcyBpcyBhIGJhc2U2NCBlbmNvZGVkIHRleHQ=\"},\"bar.txt\":{\"content_type\":\"text\\\\/plain\",\"data\": \"VGhpcyBpcyBhIGJhc2U2NCBlbmNvZGVkIHRleHQ=\"}}";
		objToPostMultAtt
				.put("_attachments", new JSONObject(attachmentMultItem));
		docsToPost.add(new JSONObject(objToPostMultAtt));

		for (JSONObject jsonOject : docsToPost) {
			RestClient rc = new RestClient(sgmaster.buildSgAdminUrl());

			Response response = rc.sendPost(DEFAULT_SG_BUCKET + "/", jsonOject,
					null);
			Assert.assertEquals(200, response.getStatus());

			attachmentItem = "{\"foo.txt\":{\"content_type\":\"text\\/plain\",\"data\": \"VGhpcyBpcyBhIGJhc2U2NCBlbmNvZGVkIHRleHQ=\"}}";
			response = rc.sendPut(
					DEFAULT_SG_BUCKET + "/" + jsonOject.getString("_id")
							+ "/foo.txt", attachmentItem, null);
			checkDocumentExistConflict409(response);

		}
	}

	public void putDocAtachment() throws JSchException, IOException,
			SftpException, CouchbaseLiteException, JSONException {
		RemoteExecutor reSG = new RemoteExecutor(sgmaster);

		SGHelper.killAllSG(reSG);
		SGHelper.runSG(reSG, SG_PATH, null);
		List<JSONObject> docsToPost = new ArrayList<JSONObject>();

		HashMap<String, Object> objToPostAtt = new HashMap<String, Object>();
		objToPostAtt.put("_id", "attachment_doc1");
		String attachmentItem = "{\"foo.txt\":{\"content_type\":\"text\\/plain\",\"data\": \"VGhpcyBpcyBhIGJhc2U2NCBlbmNvZGVkIHRleHQ=\"}}";
		objToPostAtt.put("_attachments", new JSONObject(attachmentItem));
		docsToPost.add(new JSONObject(objToPostAtt));

		HashMap<String, Object> objToPostMultAtt = new HashMap<String, Object>();
		objToPostMultAtt.put("_id", "attachment_doc2");
		String attachmentMultItem = "{\"foo.txt\":{\"content_type\":\"text\\/plain\",\"data\": \"VGhpcyBpcyBhIGJhc2U2NCBlbmNvZGVkIHRleHQ=\"},\"bar.txt\":{\"content_type\":\"text\\/plain\",\"data\": \"VGhpcyBpcyBhIGJhc2U2NCBlbmNvZGVkIHRleHQ=\"}}";
		objToPostMultAtt
				.put("_attachments", new JSONObject(attachmentMultItem));
		docsToPost.add(new JSONObject(objToPostMultAtt));

		for (JSONObject jsonOject : docsToPost) {
			RestClient rc = new RestClient(sgmaster.buildSgAdminUrl());

			Response response = rc.sendPost(DEFAULT_SG_BUCKET + "/", jsonOject,
					null);
			Assert.assertEquals(200, response.getStatus());
			String rev = getRevById(rc, jsonOject.getString("_id"));
			ImmutableMap<String, String> queryParam = ImmutableMap
					.<String, String> builder().put("rev", rev).build();

			attachmentItem = "{\"foo.txt\":{\"content_type\":\"text\\/plain\",\"data\": \"AGhpcyBpcyBhIGJhc2U2NCBlbmNvZGVkIHRleHQ=\"}}";

			rc = new RestClient(sgmaster.buildSgUrl());
			response = rc.sendPut(
					DEFAULT_SG_BUCKET + "/" + jsonOject.getString("_id")
							+ "/foo.txt", attachmentItem, queryParam);
			checkUnauthorized401(response);
		}

	}

	/*
	 * public void getDocsWithAttachmentAdmin() throws JSchException,
	 * IOException, SftpException, CouchbaseLiteException, JSONException {
	 * RemoteExecutor reSG = new RemoteExecutor(sgmaster);
	 * 
	 * SGHelper.killAllSG(reSG); SGHelper.runSG(reSG, SG_PATH, null); RestClient
	 * rc = new RestClient(sgmaster.buildSgAdminUrl());
	 * 
	 * List<String> ids = addDocsWithAttachment(rc, 1);
	 * List<ImmutableMap<String, String>> queryParams = Arrays.asList( null,
	 * ImmutableMap.<String, String> builder() .put("attachments",
	 * "true").build(), ImmutableMap .<String, String>
	 * builder().put("attachments", "false") .build(), ImmutableMap.<String,
	 * String> builder().put("revs", "true") .build(), ImmutableMap.<String,
	 * String> builder().put("revs", "false") .build(), ImmutableMap.<String,
	 * String> builder().put("atts_since", "1") .build(), ImmutableMap.<String,
	 * String> builder().put("atts_since", "2") .build(), ImmutableMap.<String,
	 * String> builder().put("atts_since", "0") .build()); for
	 * (ImmutableMap<String, String> queryParam : queryParams) { Response
	 * response = rc.getClientResponse(DEFAULT_SG_BUCKET + "/" + ids.get(0),
	 * queryParam); Assert.assertEquals(200, response.getStatus());
	 * Assert.assertEquals(Response.Status.OK.toString(), response
	 * .getStatusInfo().toString()); MultivaluedMap<String, String> headers =
	 * response .getStringHeaders(); Set<String> expectedHeadersKeys = new
	 * HashSet<String>( Arrays.asList("Date", "Content-Length", "Etag",
	 * "Content-Type", "Server")); Assert.assertEquals("Found headers: " +
	 * headers, expectedHeadersKeys, headers.keySet());
	 * Assert.assertTrue("Found entity tag:" + headers.get("Etag"),
	 * headers.get("Etag").get(0).startsWith("1-"));
	 * Assert.assertEquals("Found headers: " + headers,
	 * MediaType.APPLICATION_JSON, headers.get("Content-Type") .get(0));
	 * Assert.assertTrue("Found headers: " + headers,
	 * Integer.valueOf(headers.get("Content-Length").get(0)) > 0);
	 * Assert.assertTrue("Found headers: " + headers, headers
	 * .get("Server").get(0).startsWith("Couchbase Sync Gateway"));
	 * assertTrue(response.getDate().compareTo(new Date()) < 0);
	 * 
	 * Assert.assertEquals(MediaType.APPLICATION_JSON_TYPE,
	 * response.getMediaType()); Assert.assertTrue(response.getLength() > 0);
	 * String regex; String json = response.readEntity(String.class);
	 * System.out.println(json); if (queryParam != null &&
	 * queryParam.equals(ImmutableMap .<String, String> builder().put("revs",
	 * "true") .build())) { regex =
	 * "\\{\"_attachments\":\\{\"foo.txt\":\\{\"content_type\":\"text\\\\\\\\/plain\",\"digest\":\"sha1-ortavo\\+7s1JYc91U8\\/4oa9lKJFg=\",\"length\":29,\"revpos\":1,\"stub\":true\\}\\},\"_id\":\"attachment_doc1\",\"_rev\":\"1-(.*)\",\"_revisions\":\\{\"ids\":\\[\"4de9e8927ebfbb93e0c3fb0f64e31c4a\"\\],\"start\":1\\}\\}"
	 * ; } else if (queryParam != null && queryParam.equals(ImmutableMap
	 * .<String, String> builder() .put("attachments", "true").build())) { regex
	 * =
	 * "\\{\"_attachments\":\\{\"foo.txt\":\\{\"content_type\":\"text\\\\\\\\/plain\",\"data\":\"VGhpcyBpcyBhIGJhc2U2NCBlbmNvZGVkIHRleHQ=\",\"digest\":\"sha1-ortavo\\+7s1JYc91U8\\/4oa9lKJFg=\",\"length\":29,\"revpos\":1\\}\\},\"_id\":\"attachment_doc1\",\"_rev\":\"1-4de9e8927ebfbb93e0c3fb0f64e31c4a\"}"
	 * ; } else { regex =
	 * "\\{\"_attachments\":\\{\"foo.txt\":\\{\"content_type\":\"text\\\\\\\\/plain\",\"digest\":\"sha1-ortavo\\+7s1JYc91U8\\/4oa9lKJFg=\",\"length\":29,\"revpos\":1,\"stub\":true\\}\\},\"_id\":\"attachment_doc1\",\"_rev\":\"1-(.*)\"\\}"
	 * ; } assertTrue("Found in response:" + json, json.matches(regex));
	 * 
	 * } }
	 * 
	 * public void getDocsWithAttachment() throws JSchException, IOException,
	 * SftpException, CouchbaseLiteException, JSONException { RemoteExecutor
	 * reSG = new RemoteExecutor(sgmaster);
	 * 
	 * SGHelper.killAllSG(reSG); SGHelper.runSG(reSG, SG_PATH, null); RestClient
	 * rc = new RestClient(sgmaster.buildSgAdminUrl());
	 * 
	 * List<String> ids = addDocsWithAttachment(rc, 1);
	 * List<ImmutableMap<String, String>> queryParams = Arrays.asList( null,
	 * ImmutableMap.<String, String> builder() .put("attachments",
	 * "true").build(), ImmutableMap .<String, String>
	 * builder().put("attachments", "false") .build(), ImmutableMap.<String,
	 * String> builder().put("revs", "true") .build(), ImmutableMap.<String,
	 * String> builder().put("revs", "false") .build(), ImmutableMap.<String,
	 * String> builder().put("atts_since", "1") .build(), ImmutableMap.<String,
	 * String> builder().put("atts_since", "2") .build(), ImmutableMap.<String,
	 * String> builder().put("atts_since", "0") .build()); rc = new
	 * RestClient(sgmaster.buildSgUrl()); for (ImmutableMap<String, String>
	 * queryParam : queryParams) { Response response =
	 * rc.getClientResponse(DEFAULT_SG_BUCKET + "/" + ids.get(0), queryParam);
	 * checkUnauthorized401(response); } }
	 */

	public void getDocsWithAttachmentWithoutRevAdmin() throws JSchException,
			IOException, SftpException, CouchbaseLiteException, JSONException {
		RemoteExecutor reSG = new RemoteExecutor(sgmaster);

		SGHelper.killAllSG(reSG);
		SGHelper.runSG(reSG, SG_PATH, null);
		RestClient rc = new RestClient(sgmaster.buildSgAdminUrl());

		List<String> ids = addDocsWithAttachment(rc, 1);
		Response response = rc.getClientResponse(
				DEFAULT_SG_BUCKET + "/" + ids.get(0) + "/foo.txt", null);
		Assert.assertEquals(200, response.getStatus());
		Assert.assertEquals(Response.Status.OK.toString(), response
				.getStatusInfo().toString());
		MultivaluedMap<String, String> headers = response.getStringHeaders();
		Set<String> expectedHeadersKeys = new HashSet<String>(Arrays.asList(
				"Date", "Content-Length", "Etag", "Content-Type", "Server"));
		Assert.assertEquals("Found headers: " + headers, expectedHeadersKeys,
				headers.keySet());
		Assert.assertTrue("Found entity tag:" + headers.get("Etag"), headers
				.get("Etag").get(0).equals("sha1-ortavo+7s1JYc91U8/4oa9lKJFg="));
		Assert.assertEquals("Found headers: " + headers, MediaType.TEXT_PLAIN,
				headers.get("Content-Type").get(0));
		Assert.assertTrue("Found headers: " + headers,
				Integer.valueOf(headers.get("Content-Length").get(0)) > 0);
		Assert.assertTrue("Found headers: " + headers, headers.get("Server")
				.get(0).startsWith("Couchbase Sync Gateway"));
		assertTrue(response.getDate().compareTo(new Date()) < 0);

		Assert.assertEquals(MediaType.TEXT_PLAIN.toString(), response
				.getMediaType().toString());
		Assert.assertTrue(response.getLength() > 0);
		String json = response.readEntity(String.class);
		String regex = "This is a base64 encoded text";
		assertEquals("Found in response:" + json, json, regex);

	}
}
