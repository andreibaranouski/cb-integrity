package com.couchbase.integrations.helpers;

import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;

import com.couchbase.integrations.rest.RestClient;
import com.couchbase.lite.util.Log;

public class CBHelper {

	public static void createBucket(RestClient rest, String bucketName) {
		Form formData = new Form().param("name", bucketName)
				.param("ramQuotaMB", "256").param("authType", "sasl")
				.param("replicaNumber", "1").param("proxyPort", "11211")
				.param("bucketType", "membase").param("replicaIndex", "0")
				.param("threadsNumber", "3").param("flushEnabled", "1")
				.param("evictionPolicy", "valueOnly").param("saslPassword", "");
		// parallelDBAndViewCompaction:false
		// autoCompactionDefined:false
		// saslPassword:
		// authType:sasl
		
//		parallelDBAndViewCompaction:false
//		autoCompactionDefined:false
//		saslPassword:	
			
//		threadsNumber:3
//		replicaIndex:0
//		replicaNumber:1
//		authType:sasl
//		evictionPolicy:valueOnly
//		ramQuotaMB:100
//		bucketType:membase
//		name:db
		Response resp = rest.sendPost("pools/default/buckets", formData, null);
		Log.i(Log.TAG, resp + "");

	}

	public static void deleteBucket(RestClient rest, String bucketName) {
		rest.delete("pools/default/buckets/" + bucketName, null, null);

	}

}
