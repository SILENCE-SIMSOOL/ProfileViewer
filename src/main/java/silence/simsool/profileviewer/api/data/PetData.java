package silence.simsool.profileviewer.api.data;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import silence.simsool.profileviewer.api.nbt.NbtItemParser;

public class PetData {

	private static final int[] BASE_XP_TABLE = {
		50, 100, 150, 250, 500, 750, 1000, 1250, 1500, 1750, 2000, 2500, 3000, 3500, 4000, 4500, 5000, 5500, 6000, 7000, 8000, 9000, 10000, 11500, 13000, 14500, 16000, 17500, 19000, 20500, 22000, 25000, 28000, 31000, 34000, 37000, 40000, 44000, 48000, 52000, 56000, 60000, 65000, 70000, 75000, 80000, 85000, 90000, 95000, 100000, 110000, 120000, 130000, 140000, 150000, 160000, 170000, 180000, 190000, 200000, 215000, 230000, 245000, 260000, 275000, 290000, 305000, 320000, 335000, 350000, 370000, 390000, 410000, 430000, 450000, 470000, 490000, 510000, 530000, 550000, 600000, 650000, 700000, 750000, 800000, 850000, 900000, 950000, 1000000, 1100000, 1200000, 1300000, 1400000, 1500000, 1600000, 1700000, 1800000, 1900000, 2000000, 2500000
	};

	public static class PetItem {
		public String uuid = "";
		public String type = "";
		public String rarity = "COMMON";
		public int level = 1;
		public int maxLevel = 100;
		public double exp = 0;
		public double currentLevelExp = 0;
		public double nextLevelExp = 0;
		public float progressToNextLevel = 0f;
		public float progressToMax = 0f;
		public boolean active = false;
		public String heldItem = "";
		public int candyUsed = 0;
		public String skin = "";
		public String displayName = "";
		public List<String> lore = new ArrayList<>();
		public ItemStack itemStack = ItemStack.EMPTY;

		public int getRarityColor() {
			return switch (rarity.toUpperCase()) {
				case "UNCOMMON" -> 0xFF55FF55;
				case "RARE" -> 0xFF5555FF;
				case "EPIC" -> 0xFFAA00AA;
				case "LEGENDARY" -> 0xFFFFAA00;
				case "MYTHIC" -> 0xFFFF55FF;
				case "DIVINE" -> 0xFF55FFFF;
				default -> 0xFFFFFFFF;
			};
		}
	}

	public static char getRarityCode(String rarity) {
		return switch (rarity.toUpperCase()) {
			case "UNCOMMON" -> 'a';
			case "RARE" -> '9';
			case "EPIC" -> '5';
			case "LEGENDARY" -> '6';
			case "MYTHIC" -> 'd';
			case "DIVINE" -> 'b';
			default -> 'f';
		};
	}

	public List<PetItem> pets = new ArrayList<>();
	public PetItem activePet = null;

	public static PetData fromJson(JsonObject member) {
		PetData data = new PetData();
		if (member == null) return data;

		if (member.has("pets_data") && member.get("pets_data").isJsonObject()) {
			JsonObject pd = member.getAsJsonObject("pets_data");
			if (pd.has("pets") && pd.get("pets").isJsonArray()) {
				JsonArray arr = pd.getAsJsonArray("pets");
				for (JsonElement elem : arr) {
					if (elem.isJsonObject()) {
						JsonObject po = elem.getAsJsonObject();
						PetItem p = new PetItem();
						p.uuid = po.has("uniqueId") && !po.get("uniqueId").isJsonNull() ? po.get("uniqueId").getAsString() : (po.has("uuid") && !po.get("uuid").isJsonNull() ? po.get("uuid").getAsString() : "");
						p.type = po.has("type") ? po.get("type").getAsString() : "Unknown";
						p.rarity = po.has("tier") ? po.get("tier").getAsString() : "COMMON";
						p.exp = po.has("exp") ? po.get("exp").getAsDouble() : 0;
						p.active = po.has("active") && po.get("active").getAsBoolean();
						p.heldItem = po.has("heldItem") && !po.get("heldItem").isJsonNull() ? po.get("heldItem").getAsString() : "";
						p.candyUsed = po.has("candyUsed") && !po.get("candyUsed").isJsonNull() ? po.get("candyUsed").getAsInt() : 0;
						p.skin = po.has("skin") && !po.get("skin").isJsonNull() ? po.get("skin").getAsString() : "";

						calculatePetProgress(p);

						p.itemStack = NbtItemParser.createPetItemStack(p.type, p.rarity, p.skin, p.level, p.heldItem);

						String cleanName = p.type.replace("_", " ");
						char rCode = getRarityCode(p.rarity);
						p.displayName = "§" + rCode + "[Lv " + p.level + "] " + cleanName;
						p.lore.clear();
						p.lore.add("§8" + p.rarity + " PET");
						p.lore.add("");
						if (p.level >= p.maxLevel) {
							p.lore.add("§bMAX LEVEL REACHED!");
						} else {
							p.lore.add("§7Progress to Lv " + (p.level + 1) + ": §e" + String.format("%.1f", p.progressToNextLevel * 100) + "%");
							p.lore.add("§8(" + String.format("%,.0f", p.currentLevelExp) + " / " + String.format("%,.0f", p.nextLevelExp) + " XP)");
						}
						p.lore.add("§7Total Exp: §6" + String.format("%,.0f", p.exp));
						if (!p.heldItem.isEmpty()) {
							p.lore.add("§7Held Item: §a" + p.heldItem.replace("PET_ITEM_", "").replace("_", " "));
						}
						if (p.candyUsed > 0) {
							p.lore.add("§7Candy Used: §d" + p.candyUsed + "§7/10");
						}
						if (p.active) {
							p.lore.add("");
							p.lore.add("§a✔ Currently Active Pet");
						}
						p.lore.add("");
						p.lore.add("§" + rCode + "§l" + p.rarity + " PET");

						if (!p.itemStack.isEmpty()) {
							p.itemStack.set(DataComponents.CUSTOM_NAME, Component.literal(p.displayName));
							List<Component> loreList = new ArrayList<>();
							for (String l : p.lore) {
								loreList.add(Component.literal(l));
							}
							p.itemStack.set(DataComponents.LORE, new ItemLore(loreList));
						}

						data.pets.add(p);
						if (p.active) data.activePet = p;
					}
				}
			}
		}

		return data;
	}

	private static void calculatePetProgress(PetItem pet) {
		boolean isGoldenDragon = "GOLDEN_DRAGON".equalsIgnoreCase(pet.type);
		pet.maxLevel = isGoldenDragon ? 200 : 100;

		int offset = switch (pet.rarity.toUpperCase()) {
			case "UNCOMMON" -> 6;
			case "RARE" -> 11;
			case "EPIC" -> 16;
			case "LEGENDARY", "MYTHIC", "DIVINE" -> 20;
			default -> 0;
		};

		double accumulated = 0;
		int currentLvl = 1;
		double reqForNext = 0;
		double expInCurrent = pet.exp;

		if (isGoldenDragon) {
			// Golden Dragon uses scaled curve to 200
			for (int lvl = 1; lvl < 200; lvl++) {
				double req = getGDragXpForLevel(lvl);
				if (accumulated + req > pet.exp) {
					currentLvl = lvl;
					expInCurrent = pet.exp - accumulated;
					reqForNext = req;
					break;
				}
				accumulated += req;
				if (lvl == 199) {
					currentLvl = 200;
					expInCurrent = req;
					reqForNext = req;
				}
			}
		} else {
			int targetCap = pet.maxLevel;
			for (int lvl = 1; lvl < targetCap; lvl++) {
				int idx = (lvl - 1) + offset;
				double req = idx < BASE_XP_TABLE.length ? BASE_XP_TABLE[idx] : 2500000;
				if (accumulated + req > pet.exp) {
					currentLvl = lvl;
					expInCurrent = pet.exp - accumulated;
					reqForNext = req;
					break;
				}
				accumulated += req;
				if (lvl == targetCap - 1) {
					currentLvl = targetCap;
					expInCurrent = req;
					reqForNext = req;
				}
			}
		}

		pet.level = currentLvl;
		pet.currentLevelExp = Math.max(0, expInCurrent);
		pet.nextLevelExp = reqForNext;
		pet.progressToNextLevel = (pet.level >= pet.maxLevel || reqForNext <= 0) ? 1.0f : (float) Math.min(1.0, pet.currentLevelExp / pet.nextLevelExp);
		pet.progressToMax = (float) Math.min(1.0, pet.exp / (accumulated <= 0 ? 1 : accumulated));
	}

	private static double getGDragXpForLevel(int level) {
		if (level < 100) {
			int idx = (level - 1) + 20;
			return idx < BASE_XP_TABLE.length ? BASE_XP_TABLE[idx] : 2500000;
		}
		// 100 - 200 levels require approx 5.5M xp each
		return 5500000.0;
	}
}