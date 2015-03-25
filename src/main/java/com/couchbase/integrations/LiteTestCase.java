package com.couchbase.integrations;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import org.codehaus.jackson.map.ObjectMapper;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.integrations.helpers.CBHelper;
import com.couchbase.integrations.helpers.IniReader;
import com.couchbase.integrations.helpers.LiteHelper;
import com.couchbase.integrations.objects.Server;
import com.couchbase.integrations.rest.RestClient;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Manager;
import com.couchbase.lite.router.URLStreamHandlerFactory;
import com.couchbase.lite.util.Log;

public class LiteTestCase extends LiteTestCaseBase {

	public static final String TAG = "IntegrationTestCase";
	public static final String DEFAULT_LITE_DB = "integration-test";
	public static final String DEFAULT_CB_BUCKET = "db";
	public static final String DEFAULT_SG_BUCKET = "sync_gateway";
	LiteHelper liteHelper;
	public Bucket bucket;
	public Cluster cluster;

	private static boolean initializedUrlHandler = false;

	protected ObjectMapper mapper = new ObjectMapper();

	protected Manager manager = null;
	protected Database database = null;

	protected Server master;
	protected Server sgmaster;
	protected String SG_PATH = "";

	@Override
	protected void setUp() throws Exception {
		try {
			Log.v(TAG, "setUp");
			super.setUp();

			// for some reason a traditional static initializer causes junit to
			// die
			if (!initializedUrlHandler) {
				URLStreamHandlerFactory.registerSelfIgnoreError();
				initializedUrlHandler = true;
			}
			liteHelper = new LiteHelper();
			loadCustomProperties();
			startCBLite();
			startDatabase();

			SG_PATH = System.getProperties().getProperty("sg_path");

			IniReader iniReader = new IniReader("src/main/java/server.ini");
			List<Server> servers = iniReader.getCBServers();
			Server master = servers.get(0);
			RestClient rc = new RestClient(master.buildCbUrl(),
					master.getUser(), master.getPassword());

			List<Server> sgservers = iniReader.getSGServers();
			sgmaster = sgservers.get(0);

			CBHelper.deleteBucket(rc, DEFAULT_CB_BUCKET);
			sleep(2);
			CBHelper.createBucket(rc, DEFAULT_CB_BUCKET);
			sleep(10);
			cluster = CouchbaseCluster.create(master.getIp());
			bucket = cluster.openBucket(DEFAULT_CB_BUCKET, "");
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}

	}

	protected void startCBLite() throws IOException {
		LiteTestContext context = new LiteTestContext();
		Manager.enableLogging(TAG, Log.VERBOSE);
		Manager.enableLogging(Log.TAG, Log.VERBOSE);
		Manager.enableLogging(Log.TAG_SYNC, Log.VERBOSE);
		Manager.enableLogging(Log.TAG_SYNC_ASYNC_TASK, Log.VERBOSE);
		Manager.enableLogging(Log.TAG_BATCHER, Log.VERBOSE);
		Manager.enableLogging(Log.TAG_QUERY, Log.VERBOSE);
		Manager.enableLogging(Log.TAG_VIEW, Log.VERBOSE);
		Manager.enableLogging(Log.TAG_CHANGE_TRACKER, Log.VERBOSE);
		Manager.enableLogging(Log.TAG_BLOB_STORE, Log.VERBOSE);
		Manager.enableLogging(Log.TAG_DATABASE, Log.VERBOSE);
		Manager.enableLogging(Log.TAG_LISTENER, Log.VERBOSE);
		Manager.enableLogging(Log.TAG_MULTI_STREAM_WRITER, Log.VERBOSE);
		Manager.enableLogging(Log.TAG_REMOTE_REQUEST, Log.VERBOSE);
		Manager.enableLogging(Log.TAG_ROUTER, Log.VERBOSE);
		manager = new Manager(context, Manager.DEFAULT_OPTIONS);
	}

	protected void stopCBLite() {
		if (manager != null) {
			manager.close();
		}
	}

	protected Database startDatabase() throws CouchbaseLiteException {
		database = ensureEmptyDatabase(DEFAULT_LITE_DB);
		return database;
	}

	protected void stopDatabase() {
		if (database != null) {
			database.close();
		}
	}

	protected Database ensureEmptyDatabase(String dbName)
			throws CouchbaseLiteException {
		Database db = manager.getExistingDatabase(dbName);
		if (db != null) {
			db.delete();
		}
		db = manager.getDatabase(dbName);
		return db;
	}

	protected void loadCustomProperties() throws IOException {

		Properties systemProperties = System.getProperties();
		InputStream mainProperties = liteHelper.getAsset("test.properties");
		if (mainProperties != null) {
			systemProperties.load(mainProperties);
		}
		try {
			InputStream localProperties = liteHelper
					.getAsset("local-test.properties");
			if (localProperties != null) {
				systemProperties.load(localProperties);
			}
		} catch (IOException e) {
			Log.w(TAG,
					"Error trying to read from local-test.properties, does this file exist?");
		}
	}

	protected String getReplicationProtocol() {
		return System.getProperty("replicationProtocol");
	}

	protected String getReplicationServer() {
		return System.getProperty("replicationServer");
	}

	protected int getReplicationPort() {
		return Integer.parseInt(System.getProperty("replicationPort"));
	}

	protected String getReplicationAdminUser() {
		return System.getProperty("replicationAdminUser");
	}

	protected String getReplicationAdminPassword() {
		return System.getProperty("replicationAdminPassword");
	}

	protected String getReplicationDatabase() {
		return System.getProperty("replicationDatabase");
	}

	protected URL getReplicationURL() {
		try {
			if (getReplicationAdminUser() != null
					&& getReplicationAdminUser().trim().length() > 0) {
				return new URL(String.format("%s://%s:%s@%s:%d/%s",
						getReplicationProtocol(), getReplicationAdminUser(),
						getReplicationAdminPassword(), getReplicationServer(),
						getReplicationPort(), getReplicationDatabase()));
			} else {
				return new URL(String.format("%s://%s:%d/%s",
						getReplicationProtocol(), getReplicationServer(),
						getReplicationPort(), getReplicationDatabase()));
			}
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException(e);
		}
	}

	protected URL getReplicationSubURL(String subIndex) {
		try {
			if (getReplicationAdminUser() != null
					&& getReplicationAdminUser().trim().length() > 0) {
				return new URL(String.format("%s://%s:%s@%s:%d/%s%s",
						getReplicationProtocol(), getReplicationAdminUser(),
						getReplicationAdminPassword(), getReplicationServer(),
						getReplicationPort(), getReplicationDatabase(),
						subIndex));
			} else {
				return new URL(String.format("%s://%s:%d/%s%s",
						getReplicationProtocol(), getReplicationServer(),
						getReplicationPort(), getReplicationDatabase(),
						subIndex));
			}
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException(e);
		}
	}

	protected boolean isTestingAgainstSyncGateway() {
		return getReplicationPort() == 4984;
	}

	protected URL getReplicationURLWithoutCredentials()
			throws MalformedURLException {
		return new URL(String.format("%s://%s:%d/%s", getReplicationProtocol(),
				getReplicationServer(), getReplicationPort(),
				getReplicationDatabase()));
	}

	protected void sleep(int seconds) {
		Log.i(Log.TAG, "sleep in " + seconds + "....");
		try {
			Thread.sleep(seconds * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void tearDown() throws Exception {
		Log.v(TAG, "tearDown");
		super.tearDown();
		stopDatabase();
		stopCBLite();
	}

}
