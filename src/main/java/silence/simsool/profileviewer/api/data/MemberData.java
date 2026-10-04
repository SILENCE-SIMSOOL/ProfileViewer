package silence.simsool.profileviewer.api.data;

import com.google.gson.JsonObject;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MemberData {
	private static final Logger LOGGER = LoggerFactory.getLogger(MemberData.class);
	public final Map<String, String> sectionErrors = new LinkedHashMap<>();

	private void recordError(String section, Exception error) {
		sectionErrors.put(section, "Could not read " + section + " data. Refresh to retry.");
		LOGGER.warn("Could not parse profile section: {}", section, error);
	}

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
	public ForagingData foraging = new ForagingData();
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
				parseEssenceObject(curr.getAsJsonObject("essence"), m);
			}
		}
		if (m.essence.isEmpty()) {
			if (member.has("essence") && member.get("essence").isJsonObject()) {
				parseEssenceObject(member.getAsJsonObject("essence"), m);
			} else if (member.has("player_data") && member.get("player_data").isJsonObject()) {
				JsonObject pd = member.getAsJsonObject("player_data");
				if (pd.has("essence") && pd.get("essence").isJsonObject()) {
					parseEssenceObject(pd.getAsJsonObject("essence"), m);
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

		try { m.skills = SkillsData.fromJson(member); } catch (Exception error) { m.recordError("skills", error); }
		try { m.inventory = InventoryData.fromJson(member); } catch (Exception error) { m.recordError("inventory", error); }
		try { m.pets = PetData.fromJson(member); } catch (Exception error) { m.recordError("pets", error); }
		try { m.dungeons = DungeonData.fromJson(member); } catch (Exception error) { m.recordError("dungeons", error); }
		try { m.slayer = SlayerData.fromJson(member); } catch (Exception error) { m.recordError("slayer", error); }
		try { m.mining = MiningData.fromJson(member); } catch (Exception error) { m.recordError("mining", error); }
		try { m.foraging = ForagingData.fromJson(member); } catch (Exception error) { m.recordError("foraging", error); }
		try { m.garden = GardenData.fromJson(member); } catch (Exception error) { m.recordError("garden", error); }
		try { m.fishing = FishingData.fromJson(member); } catch (Exception error) { m.recordError("fishing", error); }
		try { m.museum = MuseumData.fromJson(member); } catch (Exception error) { m.recordError("museum", error); }
		try { m.rift = RiftData.fromJson(member); } catch (Exception error) { m.recordError("rift", error); }
		try { m.collections = CollectionData.fromJson(member); } catch (Exception error) { m.recordError("collections", error); }
		try { m.cf = CfData.fromJson(member); } catch (Exception error) { m.recordError("cf", error); }

		if (member.has("networth") && member.get("networth").isJsonObject()) {
			try { m.networth = NetworthData.fromJson(member.getAsJsonObject("networth"), bankBalance); } catch (Exception error) { m.recordError("networth", error); }
		} else if (member.has("net_worth") && member.get("net_worth").isJsonObject()) {
			try { m.networth = NetworthData.fromJson(member.getAsJsonObject("net_worth"), bankBalance); } catch (Exception error) { m.recordError("networth", error); }
		} else {
			try { m.networth = NetworthData.calculate(m, bankBalance); } catch (Exception error) { m.recordError("networth", error); }
		}

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
		try { m.dungeons = DungeonData.fromJson(dataObj); } catch (Exception error) { m.recordError("dungeons", error); }
		if (dataObj.has("slayer") && dataObj.get("slayer").isJsonObject()) {
			m.slayer = SlayerData.fromJson(dataObj.getAsJsonObject("slayer"));
		}
		try { m.mining = MiningData.fromJson(dataObj); } catch (Exception error) { m.recordError("mining", error); }
		try { m.garden = GardenData.fromJson(dataObj); } catch (Exception error) { m.recordError("garden", error); }
		try { m.fishing = FishingData.fromJson(dataObj); } catch (Exception error) { m.recordError("fishing", error); }
		try { m.museum = MuseumData.fromJson(dataObj); } catch (Exception error) { m.recordError("museum", error); }
		try { m.rift = RiftData.fromJson(dataObj); } catch (Exception error) { m.recordError("rift", error); }
		try { m.inventory = InventoryData.fromJson(dataObj); } catch (Exception error) { m.recordError("inventory", error); }
		try { m.pets = PetData.fromJson(dataObj); } catch (Exception error) { m.recordError("pets", error); }

		if (dataObj.has("essence") && dataObj.get("essence").isJsonObject()) {
			parseEssenceObject(dataObj.getAsJsonObject("essence"), m);
		}

		return m;
	}

	private static void parseEssenceObject(JsonObject ess, MemberData m) {
		if (ess == null || m == null) return;
		for (var entry : ess.entrySet()) {
			long amount = 0;
			if (entry.getValue().isJsonObject()) {
				JsonObject obj = entry.getValue().getAsJsonObject();
				if (obj.has("current")) amount = obj.get("current").getAsLong();
				else if (obj.has("amount")) amount = obj.get("amount").getAsLong();
				else if (obj.has("total")) amount = obj.get("total").getAsLong();
				else if (obj.has("value")) amount = obj.get("value").getAsLong();
				else if (obj.has("count")) amount = obj.get("count").getAsLong();
			} else if (entry.getValue().isJsonPrimitive()) {
				amount = entry.getValue().getAsLong();
			}

			String rawKey = entry.getKey().toLowerCase(java.util.Locale.ROOT).trim();
			String cleanKey = rawKey.replace("essence_", "").replace("_essence", "").trim();

			m.essence.put(rawKey, amount);
			m.essence.put(cleanKey, amount);

			if (cleanKey.equals("wood") || cleanKey.equals("foraging") || cleanKey.equals("forest")) {
				m.essence.put("wood", amount);
				m.essence.put("foraging", amount);
				m.essence.put("forest", amount);
			}
		}
	}
}