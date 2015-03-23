package com.couchbase.integrations.sg_restapi;

import java.io.IOException;
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

import com.couchbase.integrations.helpers.SGHelper;
import com.couchbase.integrations.rest.RestClient;
import com.couchbase.integrations.shell.RemoteExecutor;
import com.couchbase.lite.CouchbaseLiteException;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

public class ServerAPI extends BaseApiTest {

	// sudo justniffer -i lo -r -p "port 4985"

	public void getRoot() throws JSchException, IOException, SftpException,
			CouchbaseLiteException, JSONException {
		RemoteExecutor reSG = new RemoteExecutor(sgmaster);

		SGHelper.killAllSG(reSG);
		SGHelper.runSG(reSG, SG_PATH, null);
		List<String> urls = Arrays.asList(sgmaster.buildSgUrl(),
				sgmaster.buildSgAdminUrl());
		for (String url : urls) {
			RestClient rc = new RestClient(url);
			Response response = rc.getClientResponse();
			assertEquals(200, response.getStatus());
			assertEquals(Response.Status.OK, response.getStatus());
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
			assertTrue(response.getLength() > 0);
			OrderedJSONObject json = response
					.readEntity(OrderedJSONObject.class);
			String regex = "\\{\"couchdb\":\"Welcome\",\"vendor\":\\{\"name\":\"Couchbase Sync Gateway\",\"version\":(.*)\\},\"version\":\"Couchbase Sync Gateway\\\\/(.*)\"\\}";
			if (url.equals(sgmaster.buildSgAdminUrl())) {
				regex = "\\{\"ADMIN\":true,\"couchdb\":\"Welcome\",\"vendor\":\\{\"name\":\"Couchbase Sync Gateway\",\"version\":(.*)\\},\"version\":\"Couchbase Sync Gateway\\\\/(.*)\"\\}";
			}
			assertTrue("Found in response:" + json,
					json.toString().matches(regex));
		}
	}

	// https://github.com/couchbase/sync_gateway/issues/740
	public void putDbAdmin() throws JSchException, IOException, SftpException,
			CouchbaseLiteException, JSONException {
		RemoteExecutor reSG = new RemoteExecutor(sgmaster);

		SGHelper.killAllSG(reSG);
		SGHelper.runSG(reSG, SG_PATH, null);
		List<String> urls = Arrays.asList(sgmaster.buildSgAdminUrl());
		for (String url : urls) {
			RestClient rc = new RestClient(url);
			Response response = rc.sendPut(DEFAULT_SG_BUCKET + "1", null, null);
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

	public void putDb() throws JSchException, IOException, SftpException,
			CouchbaseLiteException, JSONException {
		RemoteExecutor reSG = new RemoteExecutor(sgmaster);

		SGHelper.killAllSG(reSG);
		SGHelper.runSG(reSG, SG_PATH, null);
		List<String> urls = Arrays.asList(sgmaster.buildSgUrl());
		for (String url : urls) {
			RestClient rc = new RestClient(url);
			Response response = rc.sendPut(DEFAULT_SG_BUCKET, null, null);
			checkUnauthorized401(response);
		}
	}

	// see https://github.com/couchbase/sync_gateway/issues/740 as start point
	public void putDbFakeDataAdmin() throws JSchException, IOException,
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
			Response response = rc.sendPut(DEFAULT_SG_BUCKET + "1/", jsonData,
					null);
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
					"text/plain; charset=utf-8", headers.get("Content-Type")
							.get(0));
			Assert.assertTrue("Found headers: " + headers,
					Integer.valueOf(headers.get("Content-Length").get(0)) == 0);
			Assert.assertTrue("Found headers: " + headers, headers
					.get("Server").get(0).startsWith("Couchbase Sync Gateway"));
			assertTrue(response.getDate().compareTo(new Date()) < 0);

			Assert.assertEquals("text/plain; charset=utf-8", response
					.getMediaType().toString());
			Assert.assertTrue(response.getLength() == 0);
			String json = response.readEntity(String.class);
			assertEquals("Found in response:" + json, json, "");

		}
	}

	public void putDuplicateDbFakeDataAdmin() throws JSchException,
			IOException, SftpException, CouchbaseLiteException, JSONException {
		RemoteExecutor reSG = new RemoteExecutor(sgmaster);

		SGHelper.killAllSG(reSG);
		SGHelper.runSG(reSG, SG_PATH, null);
		List<String> urls = Arrays.asList(sgmaster.buildSgAdminUrl());
		for (String url : urls) {
			RestClient rc = new RestClient(url);
			HashMap<String, String> objToPost = new HashMap<String, String>();
			objToPost.put("doc", "value");
			JSONObject jsonData = new JSONObject(objToPost);
			Response response = rc.sendPut(DEFAULT_SG_BUCKET + "1/", jsonData,
					null);
			response = rc.sendPut(DEFAULT_SG_BUCKET + "1/", jsonData, null);
			Assert.assertEquals(412, response.getStatus());
			Assert.assertEquals(Response.Status.PRECONDITION_FAILED.toString(),
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
			String expected = "{\"error\":\"Precondition Failed\",\"reason\":\"Duplicate database name \\\"sync_gateway1\\\"\"}";
			assertEquals("Found in response:" + json, json.toString(), expected);

		}
	}

	public void putDbFakeData() throws JSchException, IOException,
			SftpException, CouchbaseLiteException, JSONException {
		RemoteExecutor reSG = new RemoteExecutor(sgmaster);

		SGHelper.killAllSG(reSG);
		SGHelper.runSG(reSG, SG_PATH, null);
		List<String> urls = Arrays.asList(sgmaster.buildSgUrl());
		for (String url : urls) {
			RestClient rc = new RestClient(url);
			HashMap<String, String> objToPost = new HashMap<String, String>();
			objToPost.put("doc", "value");
			JSONObject jsonData = new JSONObject(objToPost);
			Response response = rc.sendPut(DEFAULT_SG_BUCKET + "1/", jsonData,
					null);
			checkForbidden403(response);
		}
	}

	// TODO add negative case related to issue found in doc
	// Traun Leyden: are these docs correct?
	// http://developer.couchbase.com/mobile/develop/references/sync-gateway/rest-api/server/index.html
	//
	// because according to these docs, the _session API is only valid under
	// /{db}/_session
	// http://developer.couchbase.com/mobile/develop/references/sync-gateway/admin-rest-api/session/get---db--_session--session-id-/index.html
	// [12:06:41 AM] Jens Alfke: Yeah, that's wrong. Must've been copied over
	// from Couchbase Server docs. (And even there it's weirdly structured
	// because _session is a separate endpoint, not the same as /.)

}