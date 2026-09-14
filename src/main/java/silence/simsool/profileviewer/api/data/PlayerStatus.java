package silence.simsool.profileviewer.api.data;

import com.google.gson.JsonObject;

public class PlayerStatus {

	public enum Status {
		ONLINE,
		OFFLINE,
		ERROR
	}

	public Status status = Status.ERROR;
	public String location = "";
	public String gameType = "";
	public String map = "";

	public static PlayerStatus fromJson(JsonObject json) {
		PlayerStatus s = new PlayerStatus();
		if (json == null) return s;

		if (json.has("success") && json.get("success").getAsBoolean()) {
			if (json.has("session") && json.get("session").isJsonObject()) {
				JsonObject session = json.getAsJsonObject("session");
				boolean online = session.has("online") && session.get("online").getAsBoolean();
				s.status = online ? Status.ONLINE : Status.OFFLINE;
				if (session.has("mode") && !session.get("mode").isJsonNull()) s.location = session.get("mode").getAsString();
				if (session.has("gameType") && !session.get("gameType").isJsonNull()) s.gameType = session.get("gameType").getAsString();
				if (session.has("map") && !session.get("map").isJsonNull()) s.map = session.get("map").getAsString();
				return s;
			}
		}
		s.status = Status.ERROR;
		return s;
	}

	public String getDisplayText() {
		if (status == Status.ONLINE) {
			if (location != null && !location.isEmpty()) {
				String loc = location.replace("_", " ");
				loc = Character.toUpperCase(loc.charAt(0)) + (loc.length() > 1 ? loc.substring(1) : "");
				if (map != null && !map.isEmpty()) {
					return "Online - " + loc + " (" + map + ")";
				}
				return "Online - " + loc;
			}
			return "Online";
		} else if (status == Status.ERROR) {
			return "Status Unknown";
		}
		return "Offline";
	}
}
