package silence.simsool.profileviewer.api;

import java.lang.reflect.Field;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import silence.simsool.profileviewer.api.data.GardenData;
import silence.simsool.profileviewer.api.data.InventoryData;
import silence.simsool.profileviewer.api.data.PlayerStatus;
import silence.simsool.profileviewer.api.data.RiftData;
import silence.simsool.profileviewer.api.data.SkillsData;
import silence.simsool.profileviewer.api.data.SkyBlockProfileData;

public class ProfileDataRegression {
	private static int checks;
	private static JsonObject json(String source) {
		return JsonParser.parseString(source).getAsJsonObject();
	}
	private static void check(boolean condition, String message) {
		checks++;
		if (!condition) throw new AssertionError(message);
	}
	public static void main(String[] args) throws Exception {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
		UUID user = UUID.fromString("00000000-0000-0000-0000-000000000001");
		check(SkyBlockProfileData.fromJson(json("{\"members\":{\"00000000000000000000000000000002\":{}}}"), user) == null, "Never select another coop member");
		check(SkyBlockProfileData.fromJson(json("{}"), user) == null, "Missing members is not a valid profile");
		check(PvApi.parseProfiles(json("{\"profiles\":null}"), user).isEmpty(), "Null profiles represents no profiles");
		boolean malformed = false;
		try { PvApi.parseProfiles(json("{}"), user); } catch (IllegalStateException expected) { malformed = true; }
		check(malformed, "Malformed response must not masquerade as no profiles");
		check(PlayerStatus.fromJson(json("{\"success\":false}")).status == PlayerStatus.Status.ERROR, "Failed status is unknown");
		check(PlayerStatus.fromJson(json("{\"success\":true,\"session\":{\"online\":false}}")).status == PlayerStatus.Status.OFFLINE, "Offline session");
		check(PlayerStatus.fromJson(json("{\"success\":true,\"session\":{\"online\":true,\"mode\":null}}")).status == PlayerStatus.Status.ONLINE, "Nullable online location");
		JsonObject experience = new JsonObject();
		SkillsData defaults = new SkillsData();
		defaults.skills.keySet().forEach(skill -> experience.addProperty("SKILL_" + skill.toUpperCase(), 1000000000));
		JsonObject playerData = new JsonObject();
		playerData.add("experience", experience);
		JsonObject member = new JsonObject();
		member.add("player_data", playerData);
		SkillsData skills = SkillsData.fromJson(member);
		double capAverage = skills.skills.entrySet().stream().filter(entry -> !entry.getKey().equals("carpentry") && !entry.getKey().equals("runecrafting") && !entry.getKey().equals("social")).mapToInt(entry -> entry.getValue().maxLevel).average().orElseThrow();
		check(skills.skillAverage == capAverage, "Maximum skill progress cannot add another level");
		GardenData garden = GardenData.fromJson(json("{\"commission_data\":{\"visits\":{\"a\":20},\"total_completed\":7,\"unique_npcs_served\":3}}"));
		check(garden.completedVisitors == 7 && garden.uniqueVisitors == 3, "Completed visitors use authoritative fields, not visits");
		check(!InventoryData.fromJson(json("{}")).available, "Inventory API disabled state");
		check(InventoryData.fromJson(json("{\"inventory\":{\"bag_contents\":{\"potion_bag\":{}}}}")).available, "Missing optional bag data does not discard inventory");
		RiftData rift = RiftData.fromJson(json("{\"currencies\":{\"motes_purse\":0},\"player_stats\":{\"rift\":{\"lifetime_motes_earned\":999}}}"));
		check(rift.motes == 0 && rift.lifetimeMotes == 999, "Lifetime motes are not the purse balance");
		check(PlayerDbApi.resolveGameProfile("../invalid").join() == null, "Reject invalid player input before networking");
		Field pendingField = PvAuth.class.getDeclaredField("authentication");
		pendingField.setAccessible(true);
		CompletableFuture<String> pending = new CompletableFuture<>();
		pendingField.set(null, pending);
		check(PvAuth.authenticateAsync() == pending && PvAuth.authenticateAsync() == pending, "Concurrent callers share pending authentication");
		pending.complete(null);
		FeatureRegression.run();
		System.out.println("Profile regression checks passed: " + checks);
	}
}
