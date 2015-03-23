package com.couchbase.integrations.helpers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Future;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.couchbase.integrations.LiteTestCase;
import com.couchbase.lite.AsyncTask;
import com.couchbase.lite.Attachment;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Manager;
import com.couchbase.lite.SavedRevision;
import com.couchbase.lite.Status;
import com.couchbase.lite.UnsavedRevision;
import com.couchbase.lite.internal.Body;
import com.couchbase.lite.internal.RevisionInternal;
import com.couchbase.lite.router.Router;
import com.couchbase.lite.router.URLConnection;
import com.couchbase.lite.util.Log;

public class LiteHelper {

	public InputStream getAsset(String name) {
		return this.getClass().getResourceAsStream("../../../../" + name);
	}

	protected Map<String, Object> userProperties(Map<String, Object> properties) {
		Map<String, Object> result = new HashMap<String, Object>();

		for (String key : properties.keySet()) {
			if (!key.startsWith("_")) {
				result.put(key, properties.get(key));
			}
		}

		return result;
	}

	public Map<String, Object> getReplicationAuthParsedJson()
			throws IOException {
		String authJson = "{\n" + "    \"facebook\" : {\n"
				+ "        \"email\" : \"jchris@couchbase.com\"\n" + "     }\n"
				+ "   }\n";
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> authProperties = mapper.readValue(authJson,
				new TypeReference<HashMap<String, Object>>() {
				});
		return authProperties;

	}

	public Map<String, Object> getPushReplicationParsedJson(URL url)
			throws IOException {

		Map<String, Object> targetProperties = new HashMap<String, Object>();
		targetProperties.put("url", url.toExternalForm());

		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put("source", LiteTestCase.DEFAULT_LITE_DB);
		properties.put("target", targetProperties);
		return properties;
	}

	public Map<String, Object> getPullReplicationParsedJson(URL url)
			throws IOException {

		Map<String, Object> sourceProperties = new HashMap<String, Object>();
		sourceProperties.put("url", url.toExternalForm());

		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put("source", sourceProperties);
		properties.put("target", LiteTestCase.DEFAULT_LITE_DB);
		return properties;
	}

	protected URLConnection sendRequest(Manager manager, String method,
			String path, Map<String, String> headers, Object bodyObj) {
		try {
			URL url = new URL("cblite://" + path);
			URLConnection conn = (URLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod(method);
			if (headers != null) {
				for (String header : headers.keySet()) {
					conn.setRequestProperty(header, headers.get(header));
				}
			}
			if (bodyObj != null) {
				conn.setDoInput(true);
				ByteArrayInputStream bais = new ByteArrayInputStream(
						new ObjectMapper().writeValueAsBytes(bodyObj));
				conn.setRequestInputStream(bais);
			}

			Router router = new com.couchbase.lite.router.Router(manager, conn);
			router.start();
			return conn;
		} catch (MalformedURLException e) {
			Assert.fail();
		} catch (IOException e) {
			Assert.fail();
		}
		return null;
	}

	protected Object parseJSONResponse(URLConnection conn) {
		Object result = null;
		Body responseBody = conn.getResponseBody();
		if (responseBody != null) {
			byte[] json = responseBody.getJson();
			String jsonString = null;
			if (json != null) {
				jsonString = new String(json);
				try {
					result = new ObjectMapper().readValue(jsonString,
							Object.class);
				} catch (Exception e) {
					Assert.fail();
				}
			}
		}
		return result;
	}

	protected Object sendBody(Manager manager, String method, String path,
			Object bodyObj, int expectedStatus, Object expectedResult) {
		URLConnection conn = sendRequest(manager, method, path, null, bodyObj);
		Object result = parseJSONResponse(conn);
		Log.v(LiteTestCase.TAG,
				String.format("%s %s --> %d", method, path,
						conn.getResponseCode()));
		Assert.assertEquals(expectedStatus, conn.getResponseCode());
		if (expectedResult != null) {
			Assert.assertEquals(expectedResult, result);
		}
		return result;
	}

	protected Object send(Manager manager, String method, String path,
			int expectedStatus, Object expectedResult) {
		return sendBody(manager, method, path, null, expectedStatus,
				expectedResult);
	}

	public static List<Document> createDocuments(Database db, final int n) {
		List<Document> docs = new ArrayList<Document>();
		// TODO should be changed to use db.runInTransaction
		for (int i = 0; i < n; i++) {
			Map<String, Object> properties = new HashMap<String, Object>();
			properties.put("testName", "testDatabase");
			properties.put("sequence", i);
			docs.add(createDocumentWithProperties(db, properties));
		}
		return docs;
	};

	@SuppressWarnings("rawtypes")
	static Future createDocumentsAsync(final Database db, final int n) {
		return db.runAsync(new AsyncTask() {
			public void run(Database database) {
				db.beginTransaction();
				createDocuments(db, n);
				db.endTransaction(true);
			}
		});

	};

	public static Document createDocumentWithProperties(Database db,
			Map<String, Object> properties) {
		Document doc = db.createDocument();
		Assert.assertNotNull(doc);
		Assert.assertNull(doc.getCurrentRevisionId());
		Assert.assertNull(doc.getCurrentRevision());
		Assert.assertNotNull("Document has no ID", doc.getId()); // 'untitled'
																	// docs are
																	// no longer
																	// untitled
																	// (8/10/12)
		try {
			doc.putProperties(properties);
		} catch (Exception e) {
			Log.e(LiteTestCase.TAG, "Error creating document", e);
			Assert.assertTrue("can't create new document in db:" + db.getName()
					+ " with properties:" + properties.toString(), false);
		}
		Assert.assertNotNull(doc.getId());
		Assert.assertNotNull(doc.getCurrentRevisionId());
		Assert.assertNotNull(doc.getUserProperties());

		// should be same doc instance, since there should only ever be a single
		// Document instance for a given document
		Assert.assertEquals(db.getDocument(doc.getId()), doc);

		Assert.assertEquals(db.getDocument(doc.getId()).getId(), doc.getId());

		return doc;
	}

	public static Document createDocWithAttachment(Database database,
			String attachmentName, String content,
			Map<String, Object> properties) throws Exception {

		Document doc = createDocumentWithProperties(database, properties);
		SavedRevision rev = doc.getCurrentRevision();

		Assert.assertEquals(rev.getAttachments().size(), 0);
		Assert.assertEquals(rev.getAttachmentNames().size(), 0);
		Assert.assertNull(rev.getAttachment(attachmentName));

		ByteArrayInputStream body = new ByteArrayInputStream(content.getBytes());

		UnsavedRevision rev2 = doc.createRevision();
		rev2.setAttachment(attachmentName, "text/plain; charset=utf-8", body);

		SavedRevision rev3 = rev2.save();
		Assert.assertNotNull(rev3);
		Assert.assertEquals(rev3.getAttachments().size(), 1);
		Assert.assertEquals(rev3.getAttachmentNames().size(), 1);

		Attachment attach = rev3.getAttachment(attachmentName);
		Assert.assertNotNull(attach);
		Assert.assertEquals(doc, attach.getDocument());
		Assert.assertEquals(attachmentName, attach.getName());
		List<String> attNames = new ArrayList<String>();
		attNames.add(attachmentName);
		Assert.assertEquals(rev3.getAttachmentNames(), attNames);

		Assert.assertEquals("text/plain; charset=utf-8",
				attach.getContentType());
		InputStream attachInputStream = attach.getContent();
		Assert.assertEquals(IOUtils.toString(attachInputStream, "UTF-8"),
				content);
		attachInputStream.close();
		Assert.assertEquals(content.getBytes().length, attach.getLength());

		return doc;

	}

	public static Document createDocWithAttachment(Database database,
			String attachmentName, String content) throws Exception {

		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put("foo", "bar");
		return createDocWithAttachment(database, attachmentName, content,
				properties);

	}

	protected String createDocumentsForPushReplication(Database database,
			String docIdTimestamp) throws CouchbaseLiteException {
		return createDocumentsForPushReplication(database, docIdTimestamp,
				"png");
	}

	protected String createDocumentsForPushReplication(Database database,
			String docIdTimestamp, String attachmentType)
			throws CouchbaseLiteException {
		String doc1Id;
		String doc2Id;// Create some documents:
		Map<String, Object> doc1Properties = new HashMap<String, Object>();
		doc1Id = String.format("doc1-%s", docIdTimestamp);
		doc1Properties.put("_id", doc1Id);
		doc1Properties.put("foo", 1);
		doc1Properties.put("bar", false);

		Body body = new Body(doc1Properties);
		RevisionInternal rev1 = new RevisionInternal(body, database);

		Status status = new Status();
		rev1 = database.putRevision(rev1, null, false, status);
		Assert.assertEquals(Status.CREATED, status.getCode());

		doc1Properties.put("_rev", rev1.getRevId());
		doc1Properties.put("UPDATED", true);

		@SuppressWarnings("unused")
		RevisionInternal rev2 = database.putRevision(new RevisionInternal(
				doc1Properties, database), rev1.getRevId(), false, status);
		Assert.assertEquals(Status.CREATED, status.getCode());

		Map<String, Object> doc2Properties = new HashMap<String, Object>();
		doc2Id = String.format("doc2-%s", docIdTimestamp);
		doc2Properties.put("_id", doc2Id);
		doc2Properties.put("baz", 666);
		doc2Properties.put("fnord", true);

		database.putRevision(new RevisionInternal(doc2Properties, database),
				null, false, status);
		Assert.assertEquals(Status.CREATED, status.getCode());

		Document doc2 = database.getDocument(doc2Id);
		UnsavedRevision doc2UnsavedRev = doc2.createRevision();
		if (attachmentType.equals("png")) {
			InputStream attachmentStream = getAsset("attachment.png");
			doc2UnsavedRev.setAttachment("attachment.png", "image/png",
					attachmentStream);
		} else if (attachmentType.equals("txt")) {
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < 1000; i++) {
				sb.append("This is a large attachemnt.");
			}
			ByteArrayInputStream attachmentStream = new ByteArrayInputStream(sb
					.toString().getBytes());
			doc2UnsavedRev.setAttachment("attachment.txt", "text/plain",
					attachmentStream);
		} else {
			throw new RuntimeException("invalid attachment type: "
					+ attachmentType);
		}
		SavedRevision doc2Rev = doc2UnsavedRev.save();
		Assert.assertNotNull(doc2Rev);

		return doc1Id;
	}

	protected Document createDocWithProperties(Database database,
			Map<String, Object> properties1) throws CouchbaseLiteException {
		Document doc1 = database.createDocument();
		UnsavedRevision revUnsaved = doc1.createRevision();
		revUnsaved.setUserProperties(properties1);
		SavedRevision rev = revUnsaved.save();
		Assert.assertNotNull(rev);
		return doc1;
	}

	public static SavedRevision createRevisionWithRandomProps(
			SavedRevision createRevFrom, boolean allowConflict)
			throws Exception {
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(UUID.randomUUID().toString(), "val");
		UnsavedRevision unsavedRevision = createRevFrom.createRevision();
		unsavedRevision.setUserProperties(properties);
		return unsavedRevision.save(allowConflict);
	}

}
