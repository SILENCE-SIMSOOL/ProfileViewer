package silence.simsool.profileviewer.api.data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class SkyBlockProfileData {

	public String profileId = "";
	public String cuteName = "Unknown";
	public String gameMode = "normal";
	public boolean selected = false;
	public UUID targetUser;

	public MemberData member = new MemberData();
	public List<CoopMember> coopMembers = new ArrayList<>();
	public BankingData banking = new BankingData();

	public static class CoopMember {
		public UUID uuid;
		public String name = "";
		public CoopMember(UUID uuid, String name) {
			this.uuid = uuid;
			this.name = name;
		}
	}

	public static class BankingData {
		public double balance = 0;
	}

	public static SkyBlockProfileData fromJson(JsonObject json, UUID user) {
		if (json == null) return null;
		SkyBlockProfileData data = new SkyBlockProfileData();
		data.targetUser = user;
		data.profileId = json.has("profile_id") ? json.get("profile_id").getAsString() : "";
		data.cuteName = json.has("cute_name") ? json.get("cute_name").getAsString() : "Default";
		data.gameMode = json.has("game_mode") ? json.get("game_mode").getAsString() : "normal";
		data.selected = json.has("selected") && json.get("selected").getAsBoolean();

		if (json.has("banking") && json.get("banking").isJsonObject()) {
			JsonObject b = json.getAsJsonObject("banking");
			if (b.has("balance")) data.banking.balance = b.get("balance").getAsDouble();
		}

		String userKey = user.toString().replace("-", "").toLowerCase();
		if (json.has("members") && json.get("members").isJsonObject()) {
			JsonObject membersObj = json.getAsJsonObject("members");
			JsonObject matchedMemberObj = null;

			for (var entry : membersObj.entrySet()) {
				String key = entry.getKey();
				try {
					UUID mUuid = key.contains("-") ? UUID.fromString(key) : fromDashless(key);
					data.coopMembers.add(new CoopMember(mUuid, ""));
				} catch (Exception ignored) {}

				String cleanKey = key.replace("-", "").toLowerCase();
				if (cleanKey.equals(userKey)) {
					if (entry.getValue().isJsonObject()) {
						matchedMemberObj = entry.getValue().getAsJsonObject();
					}
				}
			}

			// If exact match not found (e.g. single member coop or different case), use the first member
			if (matchedMemberObj == null && !membersObj.entrySet().isEmpty()) {
				var firstEntry = membersObj.entrySet().iterator().next();
				if (firstEntry.getValue().isJsonObject()) {
					matchedMemberObj = firstEntry.getValue().getAsJsonObject();
				}
			}

			if (matchedMemberObj != null) {
				data.member = MemberData.fromJson(matchedMemberObj, data.banking.balance);
			}
		}

		return data;
	}

	public static SkyBlockProfileData fromSkyCryptJson(JsonObject json, UUID user) {
		if (json == null) return null;
		SkyBlockProfileData data = new SkyBlockProfileData();
		data.targetUser = user;
		data.profileId = json.has("profile_id") ? json.get("profile_id").getAsString() : "";
		data.cuteName = json.has("cute_name") ? json.get("cute_name").getAsString() : "Default";
		data.gameMode = json.has("game_mode") ? json.get("game_mode").getAsString() : "normal";
		data.selected = json.has("current") && json.get("current").getAsBoolean();
		data.member = MemberData.fromSkyCryptJson(json);
		return data;
	}

	private static UUID fromDashless(String s) {
		if (s == null || s.length() != 32) return UUID.randomUUID();
		return UUID.fromString(s.replaceFirst(
			"(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})",
			"$1-$2-$3-$4-$5"
		));
	}
}