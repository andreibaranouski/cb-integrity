package com.couchbase.integrations;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.integrations.helpers.LiteHelper;
import com.couchbase.integrations.helpers.SGHelper;
import com.couchbase.integrations.rest.RestClient;
import com.couchbase.integrations.shell.RemoteExecutor;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.UnsavedRevision;
import com.couchbase.lite.replicator.Replication;
import com.couchbase.lite.util.Log;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

public class ApiTest extends LiteTestCase {

	// ./sync_gateway -url=http://127.0.0.1:8091 -bucket=db -pretty
	// curl -X PUT localhost:4985/sync_gateway/_user/GUEST --data
	// '{"disabled":false, "admin_channels":["public"]}'

	// https://github.com/couchbase/sync_gateway/issues/652
	public void changeDocOnCB() throws JSchException, IOException,
			SftpException, CouchbaseLiteException {
		RemoteExecutor reSG = new RemoteExecutor("localhost", "andrei",
				"resetm33");

		List<Document> docs = LiteHelper.createDocuments(database, 1);

		SGHelper.killAllSG(reSG);
		SGHelper.runSG(reSG, SG_PATH, "src/main/java/sg_config.conf");

		Replication replicationPush = database.createPushReplication(new URL(
				sgmaster.buildSgUrl() + "sync_gateway"));
		replicationPush.setContinuous(true);

		Replication replicationPull = database.createPullReplication(new URL(
				sgmaster.buildSgUrl() + "sync_gateway"));
		replicationPull.setContinuous(true);
		replicationPush.setContinuous(true);
		replicationPush.start();
		replicationPull.start();

		Query queryAllDocs = database.createAllDocumentsQuery();
		QueryEnumerator queryEnumerator = queryAllDocs.run();
		Assert.assertEquals(1, queryEnumerator.getCount());

		RestClient rc = new RestClient(sgmaster.buildSgUrl()
				+ "sync_gateway/_all_docs");

		for (Document doc : docs) {
			JsonDocument cbdoc = JsonDocument.create(doc.getId(), null);
			JsonDocument cbResponse = bucket.upsert(cbdoc);
			Assert.assertNotNull(cbResponse);

		}

		queryAllDocs = database.createAllDocumentsQuery();
		queryEnumerator = queryAllDocs.run();
		Assert.assertEquals(1, queryEnumerator.getCount());
		for (Iterator<QueryRow> it = queryEnumerator; it.hasNext();) {
			QueryRow row = it.next();
			Log.i(Log.TAG, "Currennt revision on CBLite "
					+ row.getDocument().getId() + ": "
					+ row.getDocument().getCurrentRevisionId());
			UnsavedRevision unsavedRev = row.getDocument().createRevision();
			unsavedRev.save();
		}

		queryAllDocs = database.createAllDocumentsQuery();
		queryEnumerator = queryAllDocs.run();
		Assert.assertEquals(1, queryEnumerator.getCount());
		for (Iterator<QueryRow> it = queryEnumerator; it.hasNext();) {
			QueryRow row = it.next();
			Log.i(Log.TAG, "Currennt revision on CBLite "
					+ row.getDocument().getId() + ": "
					+ row.getDocument().getCurrentRevisionId());
			UnsavedRevision unsavedRev = row.getDocument().createRevision();
			unsavedRev.save();
		}

		Log.i(Log.TAG, rc.getResponse() + "");

	}

	public void testAPIManager() throws Exception {

		LiteHelper.createDocuments(database, 10);

		JsonDocument doc = JsonDocument.create("walter", null);
		bucket.upsert(doc);

		// Process sgProcess=runSG();
		// LocalExecutor le = new LocalExecutor();
		// le.runCommand(new String[] {
		// "/home/andrei/couchbase_src/sync_gateway/bin/sync_gateway",
		// "src/main/java/sg_config.conf" });

		RemoteExecutor re = new RemoteExecutor("localhost", "andrei",
				"resetm33");

		SGHelper.killAllSG(re);
		SGHelper.runSG(re, SG_PATH, "src/main/java/sg_config.conf");

		Replication replicationPush = database.createPushReplication(new URL(
				sgmaster.buildSgUrl() + "sync_gateway"));
		replicationPush.setContinuous(true);

		Replication replicationPull = database.createPullReplication(new URL(
				sgmaster.buildSgUrl() + "sync_gateway"));
		replicationPull.setContinuous(true);
		replicationPush.setContinuous(true);

		replicationPush.start();
		replicationPull.start();

		RestClient rc = new RestClient(sgmaster.buildSgUrl());
		rc.getResponse();
		rc.getResponse("sync_gateway/_all_docs");

		Query queryAllDocs = database.createAllDocumentsQuery();
		QueryEnumerator queryEnumerator = queryAllDocs.run();
		for (Iterator<QueryRow> it = queryEnumerator; it.hasNext();) {
			QueryRow row = it.next();
			Log.i(Log.TAG, row.getDocument().getId());
			Assert.assertFalse(row.getDocument().getId().equals("walter"));
		}

	}

}