package silence.simsool.profileviewer.api.data;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public final class ProfileJson {
	private ProfileJson() {}

	public static JsonObject catalog(String name) {
		try (var stream = ProfileJson.class.getResourceAsStream("/assets/profileviewer/data/" + name + ".json")) {
			if (stream == null) throw new IllegalStateException("Missing profile catalog: " + name);
			return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
		} catch (IOException error) {
			throw new IllegalStateException("Cannot read profile catalog: " + name, error);
		}
	}

	public static JsonElement value(JsonObject root, String... path) {
		JsonElement value = root;
		for (String key : path) {
			if (value == null || value.isJsonNull()) return null;
			value = value.getAsJsonObject().get(key);
		}
		return value == null || value.isJsonNull() ? null : value;
	}

	public static JsonObject object(JsonObject root, String... path) {
		JsonElement value = value(root, path);
		return value == null ? new JsonObject() : value.getAsJsonObject();
	}

	public static JsonArray array(JsonObject root, String... path) {
		JsonElement value = value(root, path);
		return value == null ? new JsonArray() : value.getAsJsonArray();
	}

	public static long number(JsonObject root, String... path) {
		JsonElement value = value(root, path);
		return value == null ? 0 : value.getAsLong();
	}

	public static String string(JsonObject root, String... path) {
		JsonElement value = value(root, path);
		return value == null ? "" : value.getAsString();
	}
}
