package com.couchbase.integrations;

import java.net.URL;
import java.util.Iterator;

import junit.framework.Assert;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.integrations.helpers.LiteHelper;
import com.couchbase.integrations.helpers.SGHelper;
import com.couchbase.integrations.rest.RestClient;
import com.couchbase.integrations.shell.RemoteExecutor;
import com.couchbase.lite.Database;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.replicator.Replication;
import com.couchbase.lite.util.Log;

public class Crash extends LiteTestCase {

	// ./sync_gateway -url=http://127.0.0.1:8091 -bucket=db -pretty
	// curl -X PUT localhost:4985/sync_gateway/_user/GUEST --data
	// '{"disabled":false, "admin_channels":["public"]}'

	public void testAPIManager() throws Exception {
		Manager manager = this.manager;
		Assert.assertTrue(manager != null);
		for (String dbName : manager.getAllDatabaseNames()) {
			Database db = manager.getDatabase(dbName);
			Log.i(TAG, "Database '" + dbName + "':" + db.getDocumentCount()
					+ " documents");
		}

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
		SGHelper.runSG(re,
				"/home/andrei/couchbase_src/sync_gateway/bin/sync_gateway",
				"src/main/java/sg_config.conf");
		// re.execute("/home/andrei/couchbase_src/sync_gateway/bin/sync_gateway src/main/java/sg_config.conf");

		Replication replicationPush = database.createPushReplication(new URL(
				"http://127.0.0.1:4984/sync_gateway"));
		replicationPush.setContinuous(true);

		Replication replicationPull = database.createPullReplication(new URL(
				"http://127.0.0.1:4984/sync_gateway"));
		replicationPull.setContinuous(true);
		replicationPush.setContinuous(true);

		replicationPush.start();
		replicationPull.start();

		RestClient rc = new RestClient("http://127.0.0.1:4985/");
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