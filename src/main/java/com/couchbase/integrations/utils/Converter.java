package com.couchbase.integrations.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.OrderedJSONObject;

public class Converter {

	public static OrderedJSONObject inputStreamInJSONObject(InputStream in)
			throws IOException, org.apache.wink.json4j.JSONException {
		BufferedReader streamReader = new BufferedReader(new InputStreamReader(
				in, "UTF-8"));
		StringBuilder responseStrBuilder = new StringBuilder();

		String inputStr;
		while ((inputStr = streamReader.readLine()) != null)
			responseStrBuilder.append(inputStr);
		return new OrderedJSONObject(responseStrBuilder.toString());
	}

	public static OrderedJSONObject StringInJSONObject(String str)
			throws JSONException {
		return new OrderedJSONObject(str);
	}

}
