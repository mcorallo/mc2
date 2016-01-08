package it.mcsquared.engine.util;

import java.lang.reflect.Type;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class JsonUtils {
	public static final String DATE_FORMAT = "yyyy-MM-dd";
	public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

	public static String toJson(Object object) {
		GsonBuilder gb = new GsonBuilder();
		gb.registerTypeAdapter(Timestamp.class, new DateTimeTypeAdapter());
		gb.registerTypeAdapter(Date.class, new DateTypeAdapter());
		return gb.create().toJson(object);
	}

	@SuppressWarnings("unchecked")
	public static <T> T fromJson(String jsonString, Class<?> objectClass) {
		GsonBuilder gb = new GsonBuilder();
		gb.registerTypeAdapter(Timestamp.class, new DateTimeTypeAdapter());
		gb.registerTypeAdapter(Date.class, new DateTypeAdapter());
		return (T) gb.create().fromJson(jsonString, objectClass);
	}

	private static class DateTypeAdapter implements JsonSerializer<Date>, JsonDeserializer<Date> {
		private final DateFormat dateFormat;

		private DateTypeAdapter() {
			dateFormat = new SimpleDateFormat(DATE_FORMAT);
		}

		@Override
		public synchronized JsonElement serialize(Date date, Type type, JsonSerializationContext jsonSerializationContext) {
			return new JsonPrimitive(dateFormat.format(date));
		}

		@Override
		public synchronized Date deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) {
			try {
				return new Date(dateFormat.parse(jsonElement.getAsString()).getTime());
			} catch (ParseException e1) {
				throw new JsonParseException(e1);
			}
		}
	}

	private static class DateTimeTypeAdapter implements JsonSerializer<Timestamp>, JsonDeserializer<Timestamp> {

		private final DateFormat dateTimeFormat;

		private DateTimeTypeAdapter() {
			dateTimeFormat = new SimpleDateFormat(DATETIME_FORMAT, Locale.US);
//			dateTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		}

		@Override
		public synchronized JsonElement serialize(Timestamp timestamp, Type type, JsonSerializationContext jsonSerializationContext) {
			return new JsonPrimitive(dateTimeFormat.format(timestamp));
		}

		@Override
		public synchronized Timestamp deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) {
			// Long l = Long.parseLong(jsonElement.getAsString());
			// return new Timestamp(l);

			try {
				Long l = Long.parseLong(jsonElement.getAsString());
				return new Timestamp(l);
			} catch (NumberFormatException e) {
				try {
					return new Timestamp(dateTimeFormat.parse(jsonElement.getAsString()).getTime());
				} catch (ParseException e2) {
					throw new JsonParseException(e2);
				}
			}

		}
	}

	public static JsonObject toJsonObject(Object object) {
		Gson gson = new Gson();
		return gson.fromJson(gson.toJson(object), JsonObject.class);
	}
}
