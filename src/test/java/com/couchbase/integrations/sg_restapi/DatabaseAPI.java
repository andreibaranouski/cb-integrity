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

//Sync Gateway POST _session, password is not checked
//https://groups.google.com/forum/#!topic/mobile-couchbase/oFXUG6rSMLA

//http PUT :4985/sync_gateway/_user/jens name=jens email=je...@someemail.com password=1234 
//HTTP/1.1 201 Created
//Content-Length: 0
//Content-Type: text/plain; charset=utf-8
//Date: Tue, 10 Mar 2015 20:30:08 GMT
//Server: Couchbase Sync Gateway/1.00

public class DatabaseAPI extends BaseApiTest {

	// TODO add negative cases to try to create database via http request

	// GET /{db} -> {"error":"Unauthorized","reason":"Login required"}
	public void getDB() throws JSchException, IOException, SftpException,
			CouchbaseLiteException, JSONException {
		RemoteExecutor reSG = new RemoteExecutor(sgmaster);

		SGHelper.killAllSG(reSG);
		SGHelper.runSG(reSG, SG_PATH, null);
		List<String> urls = Arrays.asList(sgmaster.buildSgUrl());
		for (String url : urls) {
			RestClient rc = new RestClient(url);
			Response response = rc.getClientResponse(DEFAULT_SG_BUCKET, null);
			checkUnauthorized401(response);
		}
	}

	// GET /{db_fake} ->
	// {"error":"Bad Request","reason":"invalid database name "db_fake"}
	public void getNonExistDB() throws JSchException, IOException,
			SftpException, CouchbaseLiteException, JSONException {
		RemoteExecutor reSG = new RemoteExecutor(sgmaster);

		SGHelper.killAllSG(reSG);
		SGHelper.runSG(reSG, SG_PATH, null);
		List<String> urls = Arrays.asList(sgmaster.buildSgUrl(),
				sgmaster.buildSgAdminUrl());
		for (String url : urls) {
			RestClient rc = new RestClient(url);
			Response response = rc.getClientResponse(
					DEFAULT_SG_BUCKET.toUpperCase(), null);
			checkBadRequest400(response);
		}
	}

	// GET /{db} with Admin API (empty db)
	public void getEmptyDBAdmin() throws JSchException, IOException,
			SftpException, CouchbaseLiteException, JSONException {
		RemoteExecutor reSG = new RemoteExecutor(sgmaster);

		SGHelper.killAllSG(reSG);
		SGHelper.runSG(reSG, SG_PATH, null);
		List<String> urls = Arrays.asList(sgmaster.buildSgAdminUrl());
		for (String url : urls) {
			RestClient rc = new RestClient(url);
			Response response = rc.getClientResponse(DEFAULT_SG_BUCKET, null);
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
			OrderedJSONObject json = response
					.readEntity(OrderedJSONObject.class);
			String regex = "\\{\"committed_update_seq\":0,\"compact_running\":false,\"db_name\":\"sync_gateway\",\"disk_format_version\":0,\"instance_start_time\":1426202(.*),\"purge_seq\":0,\"update_seq\":0\\}";
			assertTrue("Found in response:" + json,
					json.toString().matches(regex));

		}
	}

	// GET /{db} with Admin API (not empty db)
	public void getDBAdmin() throws JSchException, IOException, SftpException,
			CouchbaseLiteException, JSONException {
		RemoteExecutor reSG = new RemoteExecutor(sgmaster);

		SGHelper.killAllSG(reSG);
		SGHelper.runSG(reSG, SG_PATH, null);
		List<String> urls = Arrays.asList(sgmaster.buildSgAdminUrl());
		for (String url : urls) {
			RestClient rc = new RestClient(url);
			addDocs(rc, 2);
			Response response = rc.getClientResponse(DEFAULT_SG_BUCKET, null);
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
			OrderedJSONObject json = response
					.readEntity(OrderedJSONObject.class);
			String regex = "\\{\"committed_update_seq\":2,\"compact_running\":false,\"db_name\":\"sync_gateway\",\"disk_format_version\":0,\"instance_start_time\":142(.*),\"purge_seq\":0,\"update_seq\":2\\}";
			assertTrue("Found in response:" + json,
					json.toString().matches(regex));

		}
	}

	public void getAllDocsEmptyDb() throws JSchException, IOException,
			SftpException, CouchbaseLiteException, JSONException {
		RemoteExecutor reSG = new RemoteExecutor(sgmaster);

		SGHelper.killAllSG(reSG);
		SGHelper.runSG(reSG, SG_PATH, null);
		List<String> urls = Arrays.asList(sgmaster.buildSgUrl());
		for (String url : urls) {
			RestClient rc = new RestClient(url);
			Response response = rc.getClientResponse(DEFAULT_SG_BUCKET
					+ "/_all_docs", null);
			checkUnauthorized401(response);
		}
	}

	public void getAllDocsEmptyDbAdmin() throws JSchException, IOException,
			SftpException, CouchbaseLiteException, JSONException {
		RemoteExecutor reSG = new RemoteExecutor(sgmaster);

		SGHelper.killAllSG(reSG);
		SGHelper.runSG(reSG, SG_PATH, null);
		List<String> urls = Arrays.asList(sgmaster.buildSgAdminUrl());
		for (String url : urls) {
			RestClient rc = new RestClient(url);
			Response response = rc.getClientResponse(DEFAULT_SG_BUCKET
					+ "/_all_docs", null);
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
			OrderedJSONObject json = new OrderedJSONObject(
					response.readEntity(String.class));
			String regex = "\\{\"rows\":\\[\\],\"total_rows\":0,\"update_seq\":0\\}";
			assertTrue("Found in response:" + json,
					json.toString().matches(regex));
		}
	}

	public void getAllDocsNonExistDbAdmin() throws JSchException, IOException,
			SftpException, CouchbaseLiteException, JSONException {
		RemoteExecutor reSG = new RemoteExecutor(sgmaster);
		SGHelper.killAllSG(reSG);
		SGHelper.runSG(reSG, SG_PATH, null);
		List<String> urls = Arrays.asList(sgmaster.buildSgAdminUrl());
		for (String url : urls) {
			RestClient rc = new RestClient(url);
			Response response = rc.getClientResponse(
					DEFAULT_SG_BUCKET.toUpperCase() + "/_all_docs", null);
			checkBadRequest400(response);
		}
	}

	public void getAllDocsAdmin() throws JSchException, IOException,
			SftpException, CouchbaseLiteException, JSONException {
		RemoteExecutor reSG = new RemoteExecutor(sgmaster);

		SGHelper.killAllSG(reSG);
		SGHelper.runSG(reSG, SG_PATH, null);
		List<ImmutableMap<String, String>> queryParams = Arrays.asList(
				null,
				ImmutableMap.<String, String> builder().put("access", "true")
						.build(),
				ImmutableMap.<String, String> builder().put("access", "false")
						.build(),
				ImmutableMap.<String, String> builder().put("channels", "true")
						.build(),
				ImmutableMap.<String, String> builder()
						.put("channels", "false").build(),
				ImmutableMap.<String, String> builder()
						.put("include_docs", "true").build(),
				ImmutableMap.<String, String> builder()
						.put("include_docs", "false").build(),
				ImmutableMap.<String, String> builder().put("revs", "true")
						.build(),
				ImmutableMap.<String, String> builder().put("revs", "false")
						.build(),
				ImmutableMap.<String, String> builder()
						.put("update_seq", "true").build(), ImmutableMap
						.<String, String> builder().put("update_seq", "false")
						.build());

		RestClient rc = new RestClient(sgmaster.buildSgAdminUrl());
		addDocs(rc, 2);
		for (ImmutableMap<String, String> queryParam : queryParams) {

			Response response = rc.getClientResponse(DEFAULT_SG_BUCKET
					+ "/_all_docs", queryParam);
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
			OrderedJSONObject json = new OrderedJSONObject(
					response.readEntity(String.class));
			String regex = "\\{\"rows\":\\[\\{\"key\":\"(.*)\",\"id\":\"(.*)\",\"value\":\\{\"rev\":\"1-(.*)\"\\}\\},\\{\"key\":\"(.*)\",\"id\":\"(.*)\",\"value\":\\{\"rev\":\"1-(.*)\"\\}\\}\\],\"total_rows\":2,\"update_seq\":2\\}";
			if (queryParam != null
					&& queryParam.equals(ImmutableMap
							.<String, String> builder()
							.put("update_seq", "true").build())) {
				regex = "\\{\"rows\":\\[\\{\"key\":\"(.*)\",\"id\":\"(.*)\",\"value\":\\{\"rev\":\"1-(.*)\"\\},\"update_seq\":1\\},\\{\"key\":\"(.*)\",\"id\":\"(.*)\",\"value\":\\{\"rev\":\"1-(.*)\"\\},\"update_seq\":2\\}\\],\"total_rows\":2,\"update_seq\":2\\}";
			} else if (queryParam != null
					&& queryParam.equals(ImmutableMap
							.<String, String> builder()
							.put("include_docs", "true").build())) {
				regex = "\\{\"rows\":\\[\\{\"key\":\"(.*)\",\"id\":\"(.*)\",\"value\":\\{\"rev\":\"1-(.*)\"\\},\"doc\":\\{\"_id\":\"(.*)\",\"_rev\":\"1-(.*)\",\"doc1\":\"value1\"\\}\\},\\{\"key\":\"(.*)\",\"id\":\"(.*)\",\"value\":\\{\"rev\":\"1-(.*)\"\\},\"doc\":\\{\"_id\":\"(.*)\",\"_rev\":\"1-(.*)\",\"doc2\":\"value2\"\\}\\}\\],\"total_rows\":2,\"update_seq\":2\\}";
			}
			assertTrue("Found in response:" + json,

			json.toString().matches(regex));
		}
	}

	public void postAllDocs() throws JSchException, IOException, SftpException,
			CouchbaseLiteException, JSONException {
		RemoteExecutor reSG = new RemoteExecutor(sgmaster);

		SGHelper.killAllSG(reSG);
		SGHelper.runSG(reSG, SG_PATH, null);
		List<ImmutableMap<String, String>> queryParams = Arrays.asList(
				null,
				ImmutableMap.<String, String> builder().put("access", "true")
						.build(),
				ImmutableMap.<String, String> builder().put("access", "false")
						.build(),
				ImmutableMap.<String, String> builder().put("channels", "true")
						.build(),
				ImmutableMap.<String, String> builder()
						.put("channels", "false").build(),
				ImmutableMap.<String, String> builder()
						.put("include_docs", "true").build(),
				ImmutableMap.<String, String> builder()
						.put("include_docs", "false").build(),
				ImmutableMap.<String, String> builder().put("revs", "true")
						.build(),
				ImmutableMap.<String, String> builder().put("revs", "false")
						.build(),
				ImmutableMap.<String, String> builder()
						.put("update_seq", "true").build(), ImmutableMap
						.<String, String> builder().put("update_seq", "false")
						.build());

		RestClient rcAdmin = new RestClient(sgmaster.buildSgAdminUrl());
		List<String> docIds = addDocs(rcAdmin, 2);

		RestClient rc = new RestClient(sgmaster.buildSgUrl());
		HashMap<String, List<String>> objToPost = new HashMap<String, List<String>>();
		objToPost.put("keys", Arrays.asList(docIds.get(0)));
		JSONObject jsonData = new JSONObject(objToPost);

		for (ImmutableMap<String, String> queryParam : queryParams) {

			Response response = rc.sendPost(DEFAULT_SG_BUCKET + "/_all_docs",
					jsonData, queryParam);
			checkUnauthorized401(response);
		}
	}

	public void postAllDocsAdmin() throws JSchException, IOException,
			SftpException, CouchbaseLiteException, JSONException {
		RemoteExecutor reSG = new RemoteExecutor(sgmaster);

		SGHelper.killAllSG(reSG);
		SGHelper.runSG(reSG, SG_PATH, null);
		List<ImmutableMap<String, String>> queryParams = Arrays.asList(
				null,
				ImmutableMap.<String, String> builder().put("access", "true")
						.build(),
				ImmutableMap.<String, String> builder().put("access", "false")
						.build(),
				ImmutableMap.<String, String> builder().put("channels", "true")
						.build(),
				ImmutableMap.<String, String> builder()
						.put("channels", "false").build(),
				ImmutableMap.<String, String> builder()
						.put("include_docs", "true").build(),
				ImmutableMap.<String, String> builder()
						.put("include_docs", "false").build(),
				ImmutableMap.<String, String> builder().put("revs", "true")
						.build(),
				ImmutableMap.<String, String> builder().put("revs", "false")
						.build(),
				ImmutableMap.<String, String> builder()
						.put("update_seq", "true").build(), ImmutableMap
						.<String, String> builder().put("update_seq", "false")
						.build());

		RestClient rc = new RestClient(sgmaster.buildSgAdminUrl());
		List<String> docIds = addDocs(rc, 2);

		HashMap<String, List<String>> objToPost = new HashMap<String, List<String>>();
		objToPost.put("keys", Arrays.asList(docIds.get(0)));
		JSONObject jsonData = new JSONObject(objToPost);

		for (ImmutableMap<String, String> queryParam : queryParams) {

			Response response = rc.sendPost(DEFAULT_SG_BUCKET + "/_all_docs",
					jsonData, queryParam);
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
			OrderedJSONObject json = new OrderedJSONObject(
					response.readEntity(String.class));
			String regex = "\\{\"rows\":\\[\\{\"key\":\""
					+ docIds.get(0)
					+ "\",\"id\":\""
					+ docIds.get(0)
					+ "\",\"value\":\\{\"rev\":\"1-(.*)\"\\}\\}\\],\"total_rows\":1,\"update_seq\":2\\}";
			if (queryParam != null
					&& queryParam.equals(ImmutableMap
							.<String, String> builder()
							.put("update_seq", "true").build())) {
				regex = "\\{\"rows\":\\[\\{\"key\":\""
						+ docIds.get(0)
						+ "\",\"id\":\""
						+ docIds.get(0)
						+ "\",\"value\":\\{\"rev\":\"1-(.*)\"}\\}\\],\"total_rows\":1,\"update_seq\":2\\}";
			} else if (queryParam != null
					&& queryParam.equals(ImmutableMap
							.<String, String> builder()
							.put("include_docs", "true").build())) {
				regex = "\\{\"rows\":\\[\\{\"key\":\""
						+ docIds.get(0)
						+ "\",\"id\":\""
						+ docIds.get(0)
						+ "\",\"value\":\\{\"rev\":\"1-(.*)\"\\},\"doc\":\\{\"_id\":\""
						+ docIds.get(0)
						+ "\",\"_rev\":\"1-(.*)\",\"doc1\":\"value1\"\\}\\}\\],\"total_rows\":1,\"update_seq\":2\\}";
			}
			assertTrue("Found in response:" + json,
					json.toString().matches(regex));
		}
	}

	public void postBulkDocs() throws JSchException, IOException,
			SftpException, CouchbaseLiteException, JSONException {
		RemoteExecutor reSG = new RemoteExecutor(sgmaster);

		SGHelper.killAllSG(reSG);
		SGHelper.runSG(reSG, SG_PATH, null);
		RestClient rc = new RestClient(sgmaster.buildSgUrl());
		List<ImmutableMap<String, String>> queryParams = Arrays.asList(null,
				ImmutableMap.<String, String> builder()
						.put("new_edits", "true").build(), ImmutableMap
						.<String, String> builder().put("new_edits", "false")
						.build());

		int numDocs = 2;

		List<JSONObject> docsToPost = new ArrayList<JSONObject>();
		for (int i = 0; i < numDocs; i++) {
			HashMap<String, String> objToPost = new HashMap<String, String>();
			objToPost.put("doc" + (i + 1), "value" + (i + 1));
			docsToPost.add(new JSONObject(objToPost));
		}

		HashMap<String, List<JSONObject>> objToPost = new HashMap<String, List<JSONObject>>();
		objToPost.put("docs", docsToPost);
		JSONObject jsonData = new JSONObject(objToPost);

		for (ImmutableMap<String, String> queryParam : queryParams) {

			Response response = rc.sendPost(DEFAULT_SG_BUCKET + "/_bulk_docs",
					jsonData, queryParam);
			checkUnauthorized401(response);
		}
	}

	public void postBulkDocsAdmin() throws JSchException, IOException,
			SftpException, CouchbaseLiteException, JSONException {
		RemoteExecutor reSG = new RemoteExecutor(sgmaster);

		SGHelper.killAllSG(reSG);
		SGHelper.runSG(reSG, SG_PATH, null);
		RestClient rc = new RestClient(sgmaster.buildSgAdminUrl());
		List<ImmutableMap<String, String>> queryParams = Arrays.asList(null,
				ImmutableMap.<String, String> builder()
						.put("new_edits", "true").build(), ImmutableMap
						.<String, String> builder().put("new_edits", "false")
						.build());

		int numDocs = 2;

		List<JSONObject> docsToPost = new ArrayList<JSONObject>();
		for (int i = 0; i < numDocs; i++) {
			HashMap<String, String> objToPost = new HashMap<String, String>();
			objToPost.put("doc" + (i + 1), "value" + (i + 1));
			docsToPost.add(new JSONObject(objToPost));
		}

		HashMap<String, List<JSONObject>> objToPost = new HashMap<String, List<JSONObject>>();
		objToPost.put("docs", docsToPost);
		JSONObject jsonData = new JSONObject(objToPost);

		for (ImmutableMap<String, String> queryParam : queryParams) {

			Response response = rc.sendPost(DEFAULT_SG_BUCKET + "/_bulk_docs",
					jsonData, queryParam);

			Assert.assertEquals(201, response.getStatus());
			Assert.assertEquals(Response.Status.CREATED.toString(), response
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
			JSONObject[] json = response.readEntity(JSONObject[].class);
			StringBuffer jsonStr = new StringBuffer("[");
			for (JSONObject jsonObject : json) {
				jsonStr.append(jsonObject.toString()).append(", ");
			}
			if (jsonStr.toString().endsWith(", ")) {
				jsonStr.delete(jsonStr.length() - 2, jsonStr.length());
			}
			jsonStr.append("]");
			String regex = "\\[\\{\"id\":\"(.*)\",\"rev\":\"1-(.*)\"\\}, \\{\"id\":\"(.*)\",\"rev\":\"1-(.*)\"\\}\\]";

			assertTrue("Found in response:" + jsonStr.toString(), jsonStr
					.toString().matches(regex));
		}
	}

	// to reproduce https://github.com/couchbase/sync_gateway/issues/738
	public void postBulkDocsBadRequestAdmin() throws JSchException,
			IOException, SftpException, CouchbaseLiteException, JSONException {
		RemoteExecutor reSG = new RemoteExecutor(sgmaster);

		SGHelper.killAllSG(reSG);
		SGHelper.runSG(reSG, SG_PATH, null);
		RestClient rc = new RestClient(sgmaster.buildSgAdminUrl());
		List<ImmutableMap<String, String>> queryParams = Arrays.asList(null,
				ImmutableMap.<String, String> builder()
						.put("new_edits", "true").build(), ImmutableMap
						.<String, String> builder().put("new_edits", "false")
						.build());

		int numDocs = 2;
		List<JSONObject> docsToPost = new ArrayList<JSONObject>();
		for (int i = 0; i < numDocs; i++) {
			HashMap<String, String> objToPost = new HashMap<String, String>();
			objToPost.put("doc" + (i + 1), "value" + (i + 1));
			docsToPost.add(new JSONObject(objToPost));
		}

		HashMap<String, String> objToPost = new HashMap<String, String>();
		objToPost.put("docs", docsToPost.toString());
		JSONObject jsonData = new JSONObject(objToPost);

		for (ImmutableMap<String, String> queryParam : queryParams) {

			Response response = rc.sendPost(DEFAULT_SG_BUCKET + "/_bulk_docs",
					jsonData, queryParam);

			Assert.assertEquals(201, response.getStatus());
			Assert.assertEquals(Response.Status.CREATED.toString(), response
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
			JSONObject[] json = response.readEntity(JSONObject[].class);
			StringBuffer jsonStr = new StringBuffer("[");
			for (JSONObject jsonObject : json) {
				jsonStr.append(jsonObject.toString()).append(", ");
			}
			if (jsonStr.toString().endsWith(", ")) {
				jsonStr.delete(jsonStr.length() - 2, jsonStr.length());
			}
			jsonStr.append("]");
			String regex = "\\[\\{\"id\":\"(.*)\",\"rev\":\"1-(.*)\"\\}, \\{\"id\":\"(.*)\",\"rev\":\"1-(.*)\"\\}\\]";

			assertTrue("Found in response:" + jsonStr.toString(), jsonStr
					.toString().matches(regex));
		}
	}

	public void postBulkDocsInvalidJsonData415Admin() throws JSchException,
			IOException, SftpException, CouchbaseLiteException, JSONException {
		RemoteExecutor reSG = new RemoteExecutor(sgmaster);

		SGHelper.killAllSG(reSG);
		SGHelper.runSG(reSG, SG_PATH, null);
		RestClient rc = new RestClient(sgmaster.buildSgAdminUrl());
		List<ImmutableMap<String, String>> queryParams = Arrays.asList(null,
				ImmutableMap.<String, String> builder()
						.put("new_edits", "true").build(), ImmutableMap
						.<String, String> builder().put("new_edits", "false")
						.build());

		int numDocs = 2;
		List<HashMap<String, String>> docsToPost = new ArrayList<HashMap<String, String>>();
		for (int i = 0; i < numDocs; i++) {
			HashMap<String, String> objToPost = new HashMap<String, String>();
			objToPost.put("doc" + (i + 1), "value" + (i + 1));
			docsToPost.add(objToPost);
		}

		HashMap<String, String> objToPost = new HashMap<String, String>();
		objToPost.put("docs", docsToPost.toString());
		// JSONObject jsonData = new JSONObject(objToPost);

		for (ImmutableMap<String, String> queryParam : queryParams) {

			Response response = rc.sendPost(DEFAULT_SG_BUCKET + "/_bulk_docs",
					objToPost.toString(), queryParam);

			checkUnsuportedMediaType415(response);

		}
	}

	public void postBulkDocsInvalidJsonData400Admin() throws JSchException,
			IOException, SftpException, CouchbaseLiteException, JSONException {
		RemoteExecutor reSG = new RemoteExecutor(sgmaster);

		SGHelper.killAllSG(reSG);
		SGHelper.runSG(reSG, SG_PATH, null);
		RestClient rc = new RestClient(sgmaster.buildSgAdminUrl());
		List<ImmutableMap<String, String>> queryParams = Arrays.asList(null,
				ImmutableMap.<String, String> builder()
						.put("new_edits", "true").build(), ImmutableMap
						.<String, String> builder().put("new_edits", "false")
						.build());

		HashMap<String, String> objToPost = new HashMap<String, String>();
		String body = "[{\"doc1\":\"value1\"}]";
		objToPost.put("docs", body);
		JSONObject jsonData = new JSONObject(objToPost);

		for (ImmutableMap<String, String> queryParam : queryParams) {

			Response response = rc.sendPost(DEFAULT_SG_BUCKET + "/_bulk_docs",
					jsonData, queryParam);

			Assert.assertEquals(415, response.getStatus());// TODO can't get 400

			Assert.assertEquals(Response.Status.UNSUPPORTED_MEDIA_TYPE
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
			String expected = "{\"error\":\"Unsupported Media Type\",\"reason\":\"Invalid content type text\\/plain\"}";
			assertEquals("Found in response:" + json, json.toString(), expected);

		}
	}

	// https://github.com/couchbase/sync_gateway/issues/739
	public void postBulkGetDocsIncompleteResponseAdmin() throws JSchException,
			IOException, SftpException, CouchbaseLiteException, JSONException {
		RemoteExecutor reSG = new RemoteExecutor(sgmaster);

		SGHelper.killAllSG(reSG);
		SGHelper.runSG(reSG, SG_PATH, null);
		RestClient rc = new RestClient(sgmaster.buildSgAdminUrl());
		List<ImmutableMap<String, String>> queryParams = Arrays.asList(
				null,
				ImmutableMap.<String, String> builder()
						.put("attachments", "true").build(), ImmutableMap
						.<String, String> builder().put("attachments", "false")
						.build(),
				ImmutableMap.<String, String> builder().put("revs", "true")
						.build(),
				ImmutableMap.<String, String> builder().put("revs", "false")
						.build());

		int numDocs = 2;
		List<String> docIds = addBulkDocs(rc, numDocs);

		HashMap<String, List<String>> objToPost = new HashMap<String, List<String>>();
		objToPost.put("keys", Arrays.asList(docIds.get(0)));
		JSONObject jsonData = new JSONObject(objToPost);

		for (ImmutableMap<String, String> queryParam : queryParams) {

			Response response = rc.sendPost(DEFAULT_SG_BUCKET + "/_bulk_get",
					jsonData, queryParam);// TODO after bug will be fixed

			Assert.assertEquals(406, response.getStatus());
			Assert.assertEquals(Response.Status.CREATED.toString(), response
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
			JSONObject[] json = response.readEntity(JSONObject[].class);
			StringBuffer jsonStr = new StringBuffer("[");
			for (JSONObject jsonObject : json) {
				jsonStr.append(jsonObject.toString()).append(", ");
			}
			if (jsonStr.toString().endsWith(", ")) {
				jsonStr.delete(jsonStr.length() - 2, jsonStr.length());
			}
			jsonStr.append("]");
			String regex = "\\[\\{\"id\":\"(.*)\",\"rev\":\"1-(.*)\"\\}, \\{\"id\":\"(.*)\",\"rev\":\"1-(.*)\"\\}\\]";

			assertTrue("Found in response:" + jsonStr.toString(), jsonStr
					.toString().matches(regex));
		}
	}

	public void postBulkGetDocsAdmin() throws JSchException, IOException,
			SftpException, CouchbaseLiteException, JSONException {
		RemoteExecutor reSG = new RemoteExecutor(sgmaster);

		SGHelper.killAllSG(reSG);
		SGHelper.runSG(reSG, SG_PATH, null);
		RestClient rc = new RestClient(sgmaster.buildSgAdminUrl());
		List<ImmutableMap<String, String>> queryParams = Arrays.asList(
				null,
				ImmutableMap.<String, String> builder()
						.put("attachments", "true").build(), ImmutableMap
						.<String, String> builder().put("attachments", "false")
						.build(),
				ImmutableMap.<String, String> builder().put("revs", "true")
						.build(),
				ImmutableMap.<String, String> builder().put("revs", "false")
						.build());

		int numDocs = 2;
		List<String> docIds = addBulkDocs(rc, numDocs);

		HashMap<String, List<JSONObject>> objToPost = new HashMap<String, List<JSONObject>>();
		objToPost.put(
				"docs",
				Arrays.asList(new JSONObject("{\"id\":\"" + docIds.get(0)
						+ "\"}")));
		JSONObject jsonData = new JSONObject(objToPost);

		for (ImmutableMap<String, String> queryParam : queryParams) {

			Response response = rc.sendPost(DEFAULT_SG_BUCKET + "/_bulk_get",
					jsonData, queryParam);

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
			Assert.assertTrue(
					"Found headers: " + headers,
					headers.get("Content-Type").get(0)
							.startsWith("multipart/mixed; boundary="));
			Assert.assertTrue("Found headers: " + headers,
					Integer.valueOf(headers.get("Content-Length").get(0)) > 0);
			Assert.assertTrue("Found headers: " + headers, headers
					.get("Server").get(0).startsWith("Couchbase Sync Gateway"));
			assertTrue(response.getDate().compareTo(new Date()) < 0);

			Assert.assertTrue(
					"Found Media Type: " + response.getMediaType(),
					response.getMediaType().toString()
							.startsWith("multipart/mixed; boundary="));
			Assert.assertTrue(response.getLength() > 0);
			String json = response.readEntity(String.class);
			String regex = "(.*)Content-Type: application\\/json\\{\"_id\":\"(.*)\",\"_rev\":\"1-(.*)\",\"doc1\":\"value1\"\\}(.*)";
			if (queryParam != null
					&& queryParam.equals(ImmutableMap
							.<String, String> builder().put("revs", "true")
							.build())) {
				regex = "(.*)Content-Type: application\\/json\\{\"_id\":\"(.*)\",\"_rev\":\"1-(.*)\",\"_revisions\":\\{\"ids\":\\[\"(.*)\"\\],\"start\":1\\},\"doc1\":\"value1\"\\}(.*)";
			}

			assertTrue("Found in response:" + json,
					json.replaceAll("[\\r\\n]+\\s", "").matches(regex));
		}
	}

	public void postBulkGetNonExistDbAdmin() throws JSchException, IOException,
			SftpException, CouchbaseLiteException, JSONException {
		RemoteExecutor reSG = new RemoteExecutor(sgmaster);

		SGHelper.killAllSG(reSG);
		SGHelper.runSG(reSG, SG_PATH, null);
		RestClient rc = new RestClient(sgmaster.buildSgAdminUrl());
		List<ImmutableMap<String, String>> queryParams = Arrays.asList(
				null,
				ImmutableMap.<String, String> builder()
						.put("attachments", "true").build(), ImmutableMap
						.<String, String> builder().put("attachments", "false")
						.build(),
				ImmutableMap.<String, String> builder().put("revs", "true")
						.build(),
				ImmutableMap.<String, String> builder().put("revs", "false")
						.build());

		int numDocs = 2;
		List<String> docIds = addBulkDocs(rc, numDocs);

		HashMap<String, List<JSONObject>> objToPost = new HashMap<String, List<JSONObject>>();
		objToPost.put(
				"docs",
				Arrays.asList(new JSONObject("{\"id\":\"" + docIds.get(0)
						+ "\"}")));
		JSONObject jsonData = new JSONObject(objToPost);

		for (ImmutableMap<String, String> queryParam : queryParams) {

			Response response = rc.sendPost(DEFAULT_SG_BUCKET.toUpperCase()
					+ "/_bulk_get", jsonData, queryParam);
			checkBadRequest400(response);
		}
	}

	public void postBulkGetDocsKeysNotExistAdmin() throws JSchException,
			IOException, SftpException, CouchbaseLiteException, JSONException {
		RemoteExecutor reSG = new RemoteExecutor(sgmaster);

		SGHelper.killAllSG(reSG);
		SGHelper.runSG(reSG, SG_PATH, null);
		RestClient rc = new RestClient(sgmaster.buildSgAdminUrl());
		List<ImmutableMap<String, String>> queryParams = Arrays.asList(
				null,
				ImmutableMap.<String, String> builder()
						.put("attachments", "true").build(), ImmutableMap
						.<String, String> builder().put("attachments", "false")
						.build(),
				ImmutableMap.<String, String> builder().put("revs", "true")
						.build(),
				ImmutableMap.<String, String> builder().put("revs", "false")
						.build());

		int numDocs = 2;
		List<String> docIds = addBulkDocs(rc, numDocs);

		HashMap<String, List<JSONObject>> objToPost = new HashMap<String, List<JSONObject>>();
		objToPost.put(
				"docs",
				Arrays.asList(new JSONObject("{\"key\":\"" + docIds.get(0)
						+ "\"}")));
		JSONObject jsonData = new JSONObject(objToPost);

		for (ImmutableMap<String, String> queryParam : queryParams) {

			Response response = rc.sendPost(DEFAULT_SG_BUCKET + "/_bulk_get",
					jsonData, queryParam);

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
			Assert.assertTrue(
					"Found headers: " + headers,
					headers.get("Content-Type").get(0)
							.startsWith("multipart/mixed; boundary="));
			Assert.assertTrue("Found headers: " + headers,
					Integer.valueOf(headers.get("Content-Length").get(0)) > 0);
			Assert.assertTrue("Found headers: " + headers, headers
					.get("Server").get(0).startsWith("Couchbase Sync Gateway"));
			assertTrue(response.getDate().compareTo(new Date()) < 0);

			Assert.assertTrue(
					"Found Media Type: " + response.getMediaType(),
					response.getMediaType().toString()
							.startsWith("multipart/mixed; boundary="));
			Assert.assertTrue(response.getLength() > 0);
			String json = response.readEntity(String.class);
			String regex = "(.*)Content-Type: application\\/json; error=\"true\"(.*)\\{\"error\":\"bad_request\",\"id\":\"\",\"reason\":\"Invalid doc\\/rev ID in _bulk_get\",\"status\":400\\}(.*)";
			assertTrue("Found in response:" + json,
					json.replaceAll("[\\r\\n]+\\s", "").matches(regex));
		}
	}

	public void getChangesAdmin() throws JSchException, IOException,
			SftpException, CouchbaseLiteException, JSONException {
		RemoteExecutor reSG = new RemoteExecutor(sgmaster);

		SGHelper.killAllSG(reSG);
		SGHelper.runSG(reSG, SG_PATH, null);
		RestClient rc = new RestClient(sgmaster.buildSgAdminUrl());
		List<ImmutableMap<String, String>> queryParams = Arrays.asList(
				null,
				ImmutableMap.<String, String> builder()
						.put("channels", "fake_channel").build(),
				ImmutableMap.<String, String> builder()
						.put("conflicts", "true").build(),
				ImmutableMap.<String, String> builder()
						.put("conflicts", "false").build(),

				ImmutableMap.<String, String> builder()
						.put("channels", "normal").build(),
				ImmutableMap.<String, String> builder()
						.put("channels", "continuous").build(),
				ImmutableMap.<String, String> builder()
						.put("channels", "longpoll").build(),
				ImmutableMap.<String, String> builder()
						.put("channels", "websocket").build(),
				// ImmutableMap.<String, String> builder()400
				// .put("filter", "sync_gateway/bychannel").build(),
				ImmutableMap.<String, String> builder().put("heartbeat", "1")
						.build(),
				ImmutableMap.<String, String> builder()
						.put("include_docs", "true").build(),
				ImmutableMap.<String, String> builder()
						.put("include_docs", "false").build(),
				ImmutableMap.<String, String> builder().put("limit", "0")
						.build(),
				ImmutableMap.<String, String> builder().put("limit", "1")
						.build(),
				ImmutableMap.<String, String> builder().put("limit", "10000")
						.build(),
				ImmutableMap.<String, String> builder().put("limit", "2")
						.build(),
				// ImmutableMap.<String, String> builder()400
				// .put("since", "fake_since").build(),
				ImmutableMap.<String, String> builder()
						.put("style", "main_only").build(), ImmutableMap
						.<String, String> builder().put("style", "all_docs")
						.build(),

				ImmutableMap.<String, String> builder().put("timeout", "60000")
						.build(),
				ImmutableMap.<String, String> builder().put("timeout", "80000")
						.build());

		int numDocs = 2;
		addBulkDocs(rc, numDocs);

		for (ImmutableMap<String, String> queryParam : queryParams) {

			Response response = rc.getClientResponse(DEFAULT_SG_BUCKET
					+ "/_changes", queryParam);

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
			String regex = "\\{\"results(.*)";

			if (queryParam != null
					&& queryParam.equals(ImmutableMap
							.<String, String> builder().put("revs", "true")
							.build())) {
				regex = "(.*)Content-Type: application\\/json\\{\"_id\":\"(.*)\",\"_rev\":\"1-(.*)\",\"_revisions\":\\{\"ids\":\\[\"(.*)\"\\],\"start\":1\\},\"doc1\":\"value1\"\\}(.*)";
			}

			assertTrue("Found in response:" + json,
					json.replaceAll("(\\t|\\r?\\n)+", "").matches(regex));
		}
	}

	public void getChanges400Admin() throws JSchException, IOException,
			SftpException, CouchbaseLiteException, JSONException {
		RemoteExecutor reSG = new RemoteExecutor(sgmaster);

		SGHelper.killAllSG(reSG);
		SGHelper.runSG(reSG, SG_PATH, null);
		RestClient rc = new RestClient(sgmaster.buildSgAdminUrl());
		List<ImmutableMap<String, String>> queryParams = Arrays.asList(
				ImmutableMap.<String, String> builder()
						.put("filter", "sync_gateway/bychannel").build(),
				ImmutableMap.<String, String> builder()
						.put("since", "fake_since").build());

		for (ImmutableMap<String, String> queryParam : queryParams) {

			Response response = rc.getClientResponse(DEFAULT_SG_BUCKET
					+ "/_changes", queryParam);

			assertEquals(400, response.getStatus());
			Assert.assertEquals(Response.Status.BAD_REQUEST.toString(),
					response.getStatusInfo().toString());
			MultivaluedMap<String, String> headers = response
					.getStringHeaders();
			Set<String> expectedHeadersKeys = new HashSet<String>(
					Arrays.asList("Date", "Content-Length", "Content-Type",
							"Server"));
			assertEquals("Found headers: " + headers, expectedHeadersKeys,
					headers.keySet());
			assertEquals("Found headers: " + headers,
					MediaType.APPLICATION_JSON, headers.get("Content-Type")
							.get(0));
			assertTrue("Found headers: " + headers,
					Integer.valueOf(headers.get("Content-Length").get(0)) > 0);
			assertTrue("Found headers: " + headers, headers.get("Server")
					.get(0).startsWith("Couchbase Sync Gateway"));
			assertTrue(response.getDate().compareTo(new Date()) < 0);
			assertEquals(MediaType.APPLICATION_JSON_TYPE,
					response.getMediaType());
			Assert.assertTrue(response.getLength() > 0);
			OrderedJSONObject json = response
					.readEntity(OrderedJSONObject.class);
			String expected = "{\"error\":\"Bad Request\",\"reason\":\"Missing 'channels' filter parameter\"}";
			if (queryParam.equals(ImmutableMap.<String, String> builder()
					.put("since", "fake_since").build())) {
				expected = "{\"error\":\"Bad Request\",\"reason\":\"Invalid sequence\"}";
			}
			System.out.println(json);
			System.out.println(expected);
			assertEquals("Found in response:" + json, json.toString(), expected);
		}
	}

	// TODO add more cases for _changes when docs have been changed

	// TODO

	// curl http://localhost:4984/sync_gateway/
	// {"error":"Unauthorized","reason":"Login required"}andrei@abv:/$
	// curl http://localhost:4985/sync_gateway/
	// {"committed_update_seq":0,"compact_running":false,"db_name":"sync_gateway","disk_format_version":0,"instance_start_time":1426194066706826,"purge_seq":0,"update_seq":0}andrei@abv:/$
	// curl http://localhost:4985/sync_gateway/_user/GUEST
	// {"name":"GUEST","all_channels":[],"disabled":true}andrei@abv:/$

	// https://forums.couchbase.com/t/sync-gateway-does-not-save-new-databases-added-by-rest-api/2301
	// curl -X PUT -H "Content-Type: application/json" -d
	// "{ \"server\": \"http://192.168.200.162:8091/\", \"sync\": \"function(doc) { channel(doc.channels); }\", \"bucket\": \"new-bucket\" }"
	// http://localhost:4985/new-bucket-sync/

}