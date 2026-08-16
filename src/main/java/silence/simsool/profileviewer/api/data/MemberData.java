package silence.simsool.profileviewer.api.data;

import com.google.gson.JsonObject;

public class MemberData {

	public double purse = 0;
	public int skyBlockLevel = 0;
	public int skyBlockLevelProgress = 0;
	public int fairySouls = 0;
	public long firstJoin = 0;

	public SkillsData skills = new SkillsData();
	public InventoryData inventory = new InventoryData();
	public PetData pets = new PetData();
	public DungeonData dungeons = new DungeonData();
	public SlayerData slayer = new SlayerData();
	public MiningData mining = new MiningData();
	public GardenData garden = new GardenData();
	public FishingData fishing = new FishingData();
	public MuseumData museum = new MuseumData();
	public RiftData rift = new RiftData();
	public CollectionData collections = new CollectionData();
	public CfData cf = new CfData();
	public NetworthData networth = new NetworthData();

	// Overview Specific Stats
	public boolean cookieBuffActive = false;
	public long lastLogin = 0;
	public double playtimeHours = 0;
	public long totalKills = 0;
	public long totalDeaths = 0;
	public java.util.Map<String, Long> essence = new java.util.LinkedHashMap<>();

	public static MemberData fromJson(JsonObject member, double bankBalance) {
		MemberData m = new MemberData();
		if (member == null) return m;

		if (member.has("currencies") && member.get("currencies").isJsonObject()) {
			JsonObject curr = member.getAsJsonObject("currencies");
			if (curr.has("coin_purse")) m.purse = curr.get("coin_purse").getAsDouble();
			if (curr.has("essence") && curr.get("essence").isJsonObject()) {
				JsonObject ess = curr.getAsJsonObject("essence");
				for (var entry : ess.entrySet()) {
					long amount = 0;
					if (entry.getValue().isJsonObject() && entry.getValue().getAsJsonObject().has("current")) {
						amount = entry.getValue().getAsJsonObject().get("current").getAsLong();
					} else if (entry.getValue().isJsonPrimitive()) {
						amount = entry.getValue().getAsLong();
					}
					m.essence.put(entry.getKey().toLowerCase(java.util.Locale.ROOT), amount);
				}
			}
		}
		if (member.has("leveling") && member.get("leveling").isJsonObject()) {
			JsonObject lvl = member.getAsJsonObject("leveling");
			int exp = lvl.has("experience") ? lvl.get("experience").getAsInt() : 0;
			m.skyBlockLevel = exp / 100;
			m.skyBlockLevelProgress = exp % 100;
		}
		if (member.has("fairy_soul") && member.get("fairy_soul").isJsonObject()) {
			JsonObject fs = member.getAsJsonObject("fairy_soul");
			if (fs.has("total_collected")) m.fairySouls = fs.get("total_collected").getAsInt();
		}
		if (member.has("profile") && member.get("profile").isJsonObject()) {
			JsonObject prof = member.getAsJsonObject("profile");
			if (prof.has("first_join")) m.firstJoin = prof.get("first_join").getAsLong();
			if (prof.has("last_save")) m.lastLogin = prof.get("last_save").getAsLong();
			if (prof.has("cookie_buff_active")) m.cookieBuffActive = prof.get("cookie_buff_active").getAsBoolean();
		}

		if (member.has("player_stats") && member.get("player_stats").isJsonObject()) {
			JsonObject ps = member.getAsJsonObject("player_stats");
			if (ps.has("kills") && ps.get("kills").isJsonObject()) {
				for (var entry : ps.getAsJsonObject("kills").entrySet()) {
					if (entry.getValue().isJsonPrimitive()) m.totalKills += entry.getValue().getAsLong();
				}
			}
			if (ps.has("deaths") && ps.get("deaths").isJsonObject()) {
				for (var entry : ps.getAsJsonObject("deaths").entrySet()) {
					if (entry.getValue().isJsonPrimitive()) m.totalDeaths += entry.getValue().getAsLong();
				}
			}
			if (ps.has("playtime")) {
				m.playtimeHours = ps.get("playtime").getAsDouble() / 3600.0;
			} else if (ps.has("time_spent")) {
				m.playtimeHours = ps.get("time_spent").getAsDouble() / 3600.0;
			}
		}

		try { m.skills = SkillsData.fromJson(member); } catch (Exception ignored) {}
		try { m.inventory = InventoryData.fromJson(member); } catch (Exception ignored) {}
		try { m.pets = PetData.fromJson(member); } catch (Exception ignored) {}
		try { m.dungeons = DungeonData.fromJson(member); } catch (Exception ignored) {}
		try { m.slayer = SlayerData.fromJson(member); } catch (Exception ignored) {}
		try { m.mining = MiningData.fromJson(member); } catch (Exception ignored) {}
		try { m.garden = GardenData.fromJson(member); } catch (Exception ignored) {}
		try { m.fishing = FishingData.fromJson(member); } catch (Exception ignored) {}
		try { m.museum = MuseumData.fromJson(member); } catch (Exception ignored) {}
		try { m.rift = RiftData.fromJson(member); } catch (Exception ignored) {}
		try { m.collections = CollectionData.fromJson(member); } catch (Exception ignored) {}
		try { m.cf = CfData.fromJson(member); } catch (Exception ignored) {}
		try { m.networth = NetworthData.calculate(m, bankBalance); } catch (Exception ignored) {}

		return m;
	}


	public static MemberData fromSkyCryptJson(JsonObject json) {
		MemberData m = new MemberData();
		if (json == null) return m;

		JsonObject dataObj = json.has("data") && json.get("data").isJsonObject() ? json.getAsJsonObject("data") : json;
		JsonObject rawObj = json.has("raw") && json.get("raw").isJsonObject() ? json.getAsJsonObject("raw") : null;

		if (rawObj != null) {
			return fromJson(rawObj, 0);
		}

		if (dataObj.has("purse") && dataObj.get("purse").isJsonPrimitive()) m.purse = dataObj.get("purse").getAsDouble();
		if (dataObj.has("skyblock_level")) {
			JsonObject sl = dataObj.getAsJsonObject("skyblock_level");
			if (sl.has("level") && sl.get("level").isJsonPrimitive()) m.skyBlockLevel = sl.get("level").getAsInt();
			if (sl.has("progress") && sl.get("progress").isJsonPrimitive()) m.skyBlockLevelProgress = (int)(sl.get("progress").getAsDouble() * 100);
		}
		if (dataObj.has("skills") && dataObj.get("skills").isJsonObject()) {
			m.skills = SkillsData.fromSkyCrypt(dataObj.getAsJsonObject("skills"));
		}
		if (dataObj.has("dungeons") && dataObj.get("dungeons").isJsonObject()) {
			try { m.dungeons = DungeonData.fromJson(dataObj); } catch (Exception ignored) {}
		} else {
			try { m.dungeons = DungeonData.fromJson(dataObj); } catch (Exception ignored) {}
		}
		if (dataObj.has("slayer") && dataObj.get("slayer").isJsonObject()) {
			m.slayer = SlayerData.fromJson(dataObj.getAsJsonObject("slayer"));
		}
		try { m.mining = MiningData.fromJson(dataObj); } catch (Exception ignored) {}
		try { m.garden = GardenData.fromJson(dataObj); } catch (Exception ignored) {}
		try { m.fishing = FishingData.fromJson(dataObj); } catch (Exception ignored) {}
		try { m.museum = MuseumData.fromJson(dataObj); } catch (Exception ignored) {}
		try { m.rift = RiftData.fromJson(dataObj); } catch (Exception ignored) {}
		try { m.inventory = InventoryData.fromJson(dataObj); } catch (Exception ignored) {}
		try { m.pets = PetData.fromJson(dataObj); } catch (Exception ignored) {}

		return m;
	}
}