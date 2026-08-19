package silence.simsool.profileviewer.api.data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import silence.simsool.profileviewer.api.nbt.NbtItemParser;
import silence.simsool.profileviewer.api.nbt.ParsedItem;

public class MuseumData {

	public double totalValue = 0;
	public int donatedItems = 0;

	// Item ID -> List of parsed stacks (e.g. 4 armor pieces or single weapon)
	public Map<String, List<ParsedItem>> donatedItemsMap = new LinkedHashMap<>();
	public List<ParsedItem> specialItems = new ArrayList<>();

	public List<String> weapons = new ArrayList<>();
	public List<String> armorSets = new ArrayList<>();
	public List<String> rarities = new ArrayList<>();
	public List<String> special = new ArrayList<>();

	public static MuseumData fromJson(JsonObject member) {
		return fromJson(member, UUID.randomUUID());
	}

	public static MuseumData fromJson(JsonObject members, UUID userUuid) {
		MuseumData data = new MuseumData();
		if (members == null) return data;

		String dashless = userUuid.toString().replace("-", "");
		JsonObject userMuseum = null;
		if (members.has(dashless) && members.get(dashless).isJsonObject()) {
			userMuseum = members.getAsJsonObject(dashless);
		} else if (members.has(userUuid.toString()) && members.get(userUuid.toString()).isJsonObject()) {
			userMuseum = members.getAsJsonObject(userUuid.toString());
		} else if (members.has("value") || members.has("items")) {
			userMuseum = members;
		}

		if (userMuseum == null) return data;

		if (userMuseum.has("value") && userMuseum.get("value").isJsonPrimitive()) {
			data.totalValue = userMuseum.get("value").getAsDouble();
		}

		if (userMuseum.has("items") && userMuseum.get("items").isJsonObject()) {
			JsonObject itemsObj = userMuseum.getAsJsonObject("items");
			for (Map.Entry<String, JsonElement> entry : itemsObj.entrySet()) {
				String rawId = entry.getKey();
				String formatted = formatMuseumName(rawId);
				categorizeItem(data, rawId, formatted);
				data.donatedItems++;

				List<ParsedItem> stacks = new ArrayList<>();
				if (entry.getValue().isJsonObject()) {
					JsonObject itemObj = entry.getValue().getAsJsonObject();
					if (itemObj.has("items") && itemObj.get("items").isJsonObject()) {
						JsonObject cont = itemObj.getAsJsonObject("items");
						if (cont.has("data") && cont.get("data").isJsonPrimitive()) {
							stacks = NbtItemParser.parseBase64Nbt(cont.get("data").getAsString());
						}
					}
				}
				data.donatedItemsMap.put(rawId.toUpperCase(), stacks);
			}
		}

		if (userMuseum.has("special") && userMuseum.get("special").isJsonArray()) {
			JsonArray specArr = userMuseum.getAsJsonArray("special");
			for (JsonElement elem : specArr) {
				if (elem.isJsonObject() && elem.getAsJsonObject().has("items")) {
					JsonObject itemContainer = elem.getAsJsonObject().getAsJsonObject("items");
					if (itemContainer.has("data") && itemContainer.get("data").isJsonPrimitive()) {
						List<ParsedItem> parsed = NbtItemParser.parseBase64Nbt(itemContainer.get("data").getAsString());
						for (ParsedItem p : parsed) {
							if (!p.isEmpty()) {
								data.specialItems.add(p);
								data.special.add(p.displayName.isEmpty() ? p.skyblockId : p.displayName);
								data.donatedItems++;
							}
						}
					}
				}
			}
		}

		return data;
	}

	private static void categorizeItem(MuseumData data, String rawId, String formatted) {
		String id = rawId.toUpperCase();
		if (id.contains("SWORD") || id.contains("BLADE") || id.contains("BOW") || id.contains("KATANA") || id.contains("DAGGER") || id.contains("AXE") || id.contains("SCYTHE") || id.contains("STAFF") || id.contains("WAND") || id.contains("HYPERION") || id.contains("TERMINATOR") || id.contains("CLAYMORE")) {
			data.weapons.add(formatted);
		} else if (id.contains("HELMET") || id.contains("CHESTPLATE") || id.contains("LEGGINGS") || id.contains("BOOTS") || id.contains("ARMOR") || id.contains("SET")) {
			data.armorSets.add(formatted);
		} else if (id.contains("TALISMAN") || id.contains("RING") || id.contains("ARTIFACT") || id.contains("RELIC") || id.contains("ORB") || id.contains("HEADING") || id.contains("CLOAK") || id.contains("BELT") || id.contains("GLOVE")) {
			data.rarities.add(formatted);
		} else {
			data.special.add(formatted);
		}
	}

	private static String formatMuseumName(String id) {
		String[] parts = id.replace("_", " ").toLowerCase().split(" ");
		StringBuilder sb = new StringBuilder();
		for (String p : parts) {
			if (!p.isEmpty()) {
				sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1)).append(" ");
			}
		}
		return sb.toString().trim();
	}
}