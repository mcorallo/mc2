package it.mcsquared.engine.util;

import static org.junit.Assert.*;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class JsonUtilsTest {

	@Test
	public void testToJson() {
		Map<String, Object> map = new HashMap<>();
		map.put("test", "value");
		String json = JsonUtils.toJson(map);
		assertEquals("{\"test\":\"value\"}", json);
	}

	@Test
	public void dateSerializationTest() {
		SimpleDateFormat dateFormat = new SimpleDateFormat(JsonUtils.DATE_FORMAT);

		Map<String, Object> map = new HashMap<>();
		Date date = new Date(System.currentTimeMillis());
		map.put("test", date);
		String json = JsonUtils.toJson(map);
		assertTrue(json.contains("\"" + dateFormat.format(date) + "\""));
	}

	@Test
	public void dateDeserializationTest() throws ParseException {
		String dateString = "2015-06-22";
		String json = "{\"test\":\"" + dateString + "\"}";
		TestClass object = JsonUtils.fromJson(json, TestClass.class);

		SimpleDateFormat dateFormat = new SimpleDateFormat(JsonUtils.DATE_FORMAT);
		Date date = new Date(dateFormat.parse(dateString).getTime());

		assertEquals(date, object.test);
	}

	@Test
	public void dateDeserializationErrorTest() throws ParseException {
		String dateString = "asd";
		String json = "{\"test\":\"" + dateString + "\"}";
		boolean pass = false;
		try {
			JsonUtils.fromJson(json, TestClass.class);
		} catch (JsonParseException e) {
			pass = true;
		}
		assertTrue(pass);
	}

	@Test
	public void timestampSerializationTest() {
		SimpleDateFormat dateFormat = new SimpleDateFormat(JsonUtils.DATE_FORMAT);

		Map<String, Object> map = new HashMap<>();
		Timestamp date = new Timestamp(System.currentTimeMillis());
		map.put("test2", date);
		String json = JsonUtils.toJson(map);
		assertTrue(json.contains("\"" + dateFormat.format(date) + " "));
	}

	@Test
	public void timestampDeserializationTest() throws ParseException {
		String dateString = "2015-06-22 00:00:00";
		String json = "{\"test2\":\"" + dateString + "\"}";
		TestClass object = JsonUtils.fromJson(json, TestClass.class);

		SimpleDateFormat dateFormat = new SimpleDateFormat(JsonUtils.DATETIME_FORMAT);
		Timestamp date = new Timestamp(dateFormat.parse(dateString).getTime());

		assertEquals(date, object.test2);
	}

	@Test
	public void timestampLongDeserializationTest() throws ParseException {
		String dateString = System.currentTimeMillis() + "";
		String json = "{\"test2\":\"" + dateString + "\"}";
		TestClass object = JsonUtils.fromJson(json, TestClass.class);

		Timestamp date = new Timestamp(Long.parseLong(dateString));

		assertEquals(date, object.test2);
	}

	@Test
	public void timestampDeserializationErrorTest() throws ParseException {
		String dateString = "asd";
		String json = "{\"test2\":\"" + dateString + "\"}";
		boolean pass = false;
		try {
			JsonUtils.fromJson(json, TestClass.class);
		} catch (JsonParseException e) {
			pass = true;
		}
		assertTrue(pass);
	}

	@Test
	public void testFromJson() {
		String json = "{\"test\":\"value\"}";
		Map<String, Object> map = JsonUtils.fromJson(json, Map.class);
		assertEquals("value", map.get("test"));
	}

	@Test
	public void testToJsonObject() {
		Map<String, Object> map = new HashMap<>();
		map.put("test", "value");
		JsonObject json = JsonUtils.toJsonObject(map);
		assertEquals("value", json.get("test").getAsString());
	}

}

class TestClass {
	Date test;
	Timestamp test2;
}
