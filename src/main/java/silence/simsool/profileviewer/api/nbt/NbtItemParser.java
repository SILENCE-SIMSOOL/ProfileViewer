package silence.simsool.profileviewer.api.nbt;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.UUID;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.ResolvableProfile;

public class NbtItemParser {

	public static List<ParsedItem> parseBase64Nbt(String base64Data) {
		List<ParsedItem> items = new ArrayList<>();
		if (base64Data == null || base64Data.isEmpty()) return items;

		try {
			byte[] bytes = Base64.getDecoder().decode(base64Data.trim());
			CompoundTag compound = NbtIo.readCompressed(new ByteArrayInputStream(bytes), NbtAccounter.unlimitedHeap());
			if (compound.contains("i")) {
				ListTag list = compound.getList("i").orElse(new ListTag());
				for (int idx = 0; idx < list.size(); idx++) {
					CompoundTag itemTag = list.getCompound(idx).orElse(null);
					if (itemTag == null || itemTag.isEmpty()) {
						items.add(ParsedItem.EMPTY);
						continue;
					}

					ParsedItem item = parseSingleItem(itemTag);
					items.add(item);
				}
			}
		} catch (Exception error) {
			throw new IllegalArgumentException("Could not decode inventory NBT", error);
		}

		return items;
	}

	private static ParsedItem parseSingleItem(CompoundTag tag) {
		ParsedItem item = new ParsedItem();
		if (tag == null || tag.isEmpty()) return item;

		int numId = 0;
		if (tag.contains("id")) {
			try {
				numId = tag.getShort("id").orElse((short) 0);
			} catch (Exception e) {
				try { numId = tag.getInt("id").orElse(0); } catch (Exception ignored) {}
			}
			if (numId == 0) {
				item.mcId = tag.getString("id").orElse("");
			}
		}

		int damage = 0;
		if (tag.contains("Damage")) {
			try { damage = tag.getShort("Damage").orElse((short) 0); } catch (Exception ignored) {}
		}

		if (tag.contains("Count")) item.count = tag.getByte("Count").orElse((byte) 1);

		String skullTexture = "";
		String skullOwnerName = "";
		Integer dyedColor = null;

		if (tag.contains("tag")) {
			CompoundTag subTag = tag.getCompound("tag").orElse(new CompoundTag());

			if (subTag.contains("display")) {
				CompoundTag disp = subTag.getCompound("display").orElse(new CompoundTag());
				if (disp.contains("Name")) {
					item.displayName = disp.getString("Name").orElse("");
				}
				if (disp.contains("Lore")) {
					ListTag loreList = disp.getList("Lore").orElse(new ListTag());
					for (int i = 0; i < loreList.size(); i++) {
						item.lore.add(loreList.getString(i).orElse(""));
					}
				}
				if (disp.contains("color")) {
					try {
						dyedColor = disp.getInt("color").orElse(null);
					} catch (Exception ignored) {}
				}
			}

			if (subTag.contains("ExtraAttributes")) {
				CompoundTag ea = subTag.getCompound("ExtraAttributes").orElse(new CompoundTag());
				if (ea.contains("id")) item.skyblockId = ea.getString("id").orElse("");
				item.uuid = ea.getString("uuid").orElse("");
				item.upgradeScore = calculateUpgradeScore(ea);
				if (ea.contains("skin")) {
					String skinVal = ea.getString("skin").orElse("");
					if (!skinVal.isEmpty()) {
						ItemStack skinStack = silence.simsool.profileviewer.api.repo.ItemRepo.getItemStack("PET_SKIN_" + skinVal);
						if (skinStack.isEmpty()) {
							skinStack = silence.simsool.profileviewer.api.repo.ItemRepo.getItemStack(skinVal);
						}
						if (!skinStack.isEmpty() && skinStack.has(DataComponents.PROFILE)) {
							item.itemStack = skinStack.copy();
							item.itemStack.setCount(Math.max(1, item.count));
						}
					}
				}
				if (dyedColor == null && ea.contains("color")) {
					String colorStr = ea.getString("color").orElse("");
					if (colorStr.contains(":")) {
						String[] parts = colorStr.split(":");
						if (parts.length == 3) {
							try {
								int r = Integer.parseInt(parts[0]);
								int g = Integer.parseInt(parts[1]);
								int b = Integer.parseInt(parts[2]);
								dyedColor = (r << 16) | (g << 8) | b;
							} catch (Exception ignored) {}
						}
					} else if (!colorStr.isEmpty()) {
						try { dyedColor = Integer.parseInt(colorStr); } catch (Exception ignored) {}
					}
				}
			}

			if (subTag.contains("ench") || subTag.contains("enchantments")) {
				item.hasGlint = true;
			}

			// Skull Texture & Owner
			if (subTag.contains("SkullOwner")) {
				try {
					CompoundTag skullOwner = subTag.getCompound("SkullOwner").orElse(null);
					if (skullOwner != null) {
						CompoundTag props = skullOwner.getCompound("Properties").orElse(skullOwner.getCompound("properties").orElse(null));
						if (props != null) {
							ListTag texList = props.getList("textures").orElse(props.getList("Textures").orElse(new ListTag()));
							if (!texList.isEmpty()) {
								CompoundTag texObj = texList.getCompound(0).orElse(new CompoundTag());
								skullTexture = texObj.getString("Value").orElse(texObj.getString("value").orElse(""));
							}
						}
					}
					if (skullTexture.isEmpty()) skullOwnerName = subTag.getString("SkullOwner").orElse("");
				} catch (Exception ignored) {}
			}

			detectRarity(item);
		}

		// Resolve ItemStack
		if (item.itemStack.isEmpty() && !skullTexture.isEmpty()) {
			item.itemStack = createSkull(skullTexture, item.count);
		}
		if (item.itemStack.isEmpty() || item.itemStack.getItem() == Items.PLAYER_HEAD && !item.itemStack.has(DataComponents.PROFILE)) {
			item.itemStack = resolveItemStack(numId, item.mcId, item.skyblockId, damage, item.count);
		}
		if (item.itemStack.isEmpty() && !skullOwnerName.isEmpty()) {
			item.itemStack = new ItemStack(Items.PLAYER_HEAD, Math.max(1, item.count));
			item.itemStack.set(DataComponents.PROFILE, ResolvableProfile.createUnresolved(skullOwnerName));
		}

		if (item.displayName.isEmpty()) {
			item.displayName = item.skyblockId.isEmpty() ? (!item.itemStack.isEmpty() ? item.itemStack.getHoverName().getString() : item.mcId) : item.skyblockId;
		}

		if (!item.itemStack.isEmpty()) {
			if (!item.displayName.isEmpty()) {
				item.itemStack.set(DataComponents.CUSTOM_NAME, Component.literal(item.displayName));
			}
			if (!item.lore.isEmpty()) {
				List<Component> loreComponents = new ArrayList<>();
				for (String line : item.lore) {
					loreComponents.add(Component.literal(line));
				}
				item.itemStack.set(DataComponents.LORE, new ItemLore(loreComponents));
			}
			if (item.hasGlint) {
				item.itemStack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
			}
			if (dyedColor != null) {
				item.itemStack.set(DataComponents.DYED_COLOR, new DyedItemColor(dyedColor));
			}
			if (tag.contains("tag")) {
				CompoundTag subTag = tag.getCompound("tag").orElse(null);
				if (subTag != null && subTag.contains("ExtraAttributes")) {
					CompoundTag ea = subTag.getCompound("ExtraAttributes").orElse(null);
					if (ea != null) {
						item.itemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(ea));
					}
				}
			}
		}

		return item;
	}

	private static int calculateUpgradeScore(CompoundTag attributes) {
		int score = 0;
		if (attributes.getInt("rarity_upgrades").orElse(0) > 0) score++;
		if (!attributes.getString("modifier").orElse("").isEmpty()) score++;
		for (String key : List.of("drill_part_fuel_tank", "drill_part_engine", "drill_part_upgrade_module", "fishing_hook", "fishing_line", "fishing_sinker")) {
			if (attributes.contains(key)) score++;
		}
		score += scoreLevels(attributes.getCompound("enchantments").orElse(null), 4, true);
		score += scoreLevels(attributes.getCompound("attributes").orElse(null), 7, false);
		CompoundTag gems = attributes.getCompound("gems").orElse(null);
		if (gems != null) {
			for (String slot : gems.keySet()) {
				String quality = gems.getString(slot).orElse("");
				int qualityLevel = switch (quality) {
					case "ROUGH" -> 0;
					case "FLAWED" -> 1;
					case "FINE" -> 2;
					case "FLAWLESS" -> 3;
					case "PERFECT" -> 4;
					default -> -1;
				};
				boolean jasper = slot.startsWith("JASPER_") || "JASPER".equals(gems.getString(slot + "_gem").orElse(""));
				score += Math.max(0, qualityLevel - (jasper ? 2 : 3));
			}
		}
		score += attributes.getInt("divan_powder_coating").orElse(0);
		return score;
	}

	private static int scoreLevels(CompoundTag values, int threshold, boolean countUltimate) {
		if (values == null) return 0;
		int score = 0;
		for (String key : values.keySet()) {
			int level = values.getInt(key).orElse(0);
			if (countUltimate && key.startsWith("ultimate_")) score += level;
			if (level > threshold) score += level - threshold;
		}
		return score;
	}

	public static String fixBase64Padding(String base64) {
		if (base64 == null) return "";
		String clean = base64.trim().replaceAll("=+$", "");
		int remainder = clean.length() % 4;
		if (remainder > 0) {
			clean += "=".repeat(4 - remainder);
		}
		return clean;
	}

	public static ItemStack createSkull(String texture, int count) {
		ItemStack stack = new ItemStack(Items.PLAYER_HEAD, Math.max(1, count));
		if (texture == null || texture.trim().isEmpty()) return stack;
		try {
			String clean = texture.trim();
			String encoded = null;

			if (clean.startsWith("http://") || clean.startsWith("https://")) {
				String json = "{\"textures\":{\"SKIN\":{\"url\":\"" + clean + "\"}}}";
				encoded = java.util.Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
			} else if (clean.matches("^[0-9a-fA-F]{64}$")) {
				String json = "{\"textures\":{\"SKIN\":{\"url\":\"http://textures.minecraft.net/texture/" + clean + "\"}}}";
				encoded = java.util.Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
			} else {
				String padded = fixBase64Padding(clean);
				try {
					byte[] decoded = java.util.Base64.getDecoder().decode(padded);
					String decodedStr = new String(decoded, StandardCharsets.UTF_8);
					if (decodedStr.contains("{") && decodedStr.contains("textures")) {
						encoded = padded;
					}
				} catch (Exception ignored) {}
			}

			if (encoded == null) {
				return stack;
			}

			UUID uuid = UUID.nameUUIDFromBytes(encoded.getBytes(StandardCharsets.UTF_8));
			Property property = new Property("textures", encoded);
			var multimap = com.google.common.collect.ImmutableMultimap.<String, Property>builder().put("textures", property).build();
			PropertyMap propertyMap = new PropertyMap(multimap);

			ResolvableProfile profile = ResolvableProfile.createResolved(new GameProfile(uuid, "_", propertyMap));
			stack.set(DataComponents.PROFILE, profile);
		} catch (Exception ignored) {}
		return stack;
	}

	public static ItemStack createPetItemStack(String type, String rarity, String skin) {
		return silence.simsool.profileviewer.api.repo.PetRepo.getPetItemStack(type, rarity, 100, skin, null);
	}

	public static ItemStack createPetItemStack(String type, String rarity, String skin, int level, String heldItem) {
		return silence.simsool.profileviewer.api.repo.PetRepo.getPetItemStack(type, rarity, level, skin, heldItem);
	}

	public static ItemStack resolveItemStack(int numId, String mcId, String sbId, int damage, int count) {
		int safeCount = Math.max(1, count);

		// 1. Resolve by ItemRepo & SkyBlock ID (First Priority for Custom SkyBlock Items)
		if (sbId != null && !sbId.isEmpty()) {
			ItemStack repoStack = silence.simsool.profileviewer.api.repo.ItemRepo.getItemStack(sbId);
			if (!repoStack.isEmpty()) {
				repoStack.setCount(safeCount);
				return repoStack;
			}
			ItemStack sbStack = resolveBySkyBlockId(sbId, safeCount);
			if (!sbStack.isEmpty()) return sbStack;
		}

		// 2. Resolve by Numerical ID and Damage using LegacyIdMap (Vanilla Fallback)
		if (numId > 0) {
			Item mcItem = LegacyIdMap.getItem(numId, damage);
			if (mcItem != null && mcItem != Items.AIR) {
				return new ItemStack(mcItem, safeCount);
			}
		}

		// 3. Resolve by String MC ID
		if (mcId != null && !mcId.isEmpty() && !mcId.equals("minecraft:air")) {
			String cleanId = mcId.contains(":") ? mcId : "minecraft:" + mcId;
			Identifier res = Identifier.tryParse(cleanId);
			if (res != null) {
				Item mcItem = BuiltInRegistries.ITEM.getValue(res);
				if (mcItem != null && mcItem != Items.AIR) {
					return new ItemStack(mcItem, safeCount);
				}
			}
		}

		return ItemStack.EMPTY;
	}

	private static ItemStack resolveBySkyBlockId(String sbId, int count) {
		String id = sbId.toUpperCase();
		if (id.contains("HELMET") || id.contains("HAT") || id.contains("MASK") || id.contains("CROWN") || id.contains("GOGGLES") || id.contains("HOOD")) {
			if (id.contains("LEATHER") || id.contains("FARMER") || id.contains("LANTERN")) return new ItemStack(Items.LEATHER_HELMET, count);
			if (id.contains("IRON")) return new ItemStack(Items.IRON_HELMET, count);
			if (id.contains("GOLD")) return new ItemStack(Items.GOLDEN_HELMET, count);
			if (id.contains("CHAIN")) return new ItemStack(Items.CHAINMAIL_HELMET, count);
			if (id.contains("DIAMOND") || id.contains("NECRON") || id.contains("STORM") || id.contains("MAXOR") || id.contains("GOLDOR") || id.contains("DIVAN")) return new ItemStack(Items.DIAMOND_HELMET, count);
			return ItemStack.EMPTY;
		}
		if (id.contains("CHESTPLATE") || id.contains("TUNIC") || id.contains("CLOAK")) {
			if (id.contains("LEATHER") || id.contains("CLOAK")) return new ItemStack(Items.LEATHER_CHESTPLATE, count);
			if (id.contains("IRON")) return new ItemStack(Items.IRON_CHESTPLATE, count);
			if (id.contains("GOLD")) return new ItemStack(Items.GOLDEN_CHESTPLATE, count);
			if (id.contains("CHAIN")) return new ItemStack(Items.CHAINMAIL_CHESTPLATE, count);
			return new ItemStack(Items.DIAMOND_CHESTPLATE, count);
		}
		if (id.contains("LEGGINGS") || id.contains("PANTS") || id.contains("TROUSERS") || id.contains("BELT")) {
			if (id.contains("LEATHER") || id.contains("BELT")) return new ItemStack(Items.LEATHER_LEGGINGS, count);
			if (id.contains("IRON")) return new ItemStack(Items.IRON_LEGGINGS, count);
			if (id.contains("GOLD")) return new ItemStack(Items.GOLDEN_LEGGINGS, count);
			if (id.contains("CHAIN")) return new ItemStack(Items.CHAINMAIL_LEGGINGS, count);
			return new ItemStack(Items.DIAMOND_LEGGINGS, count);
		}
		if (id.contains("BOOTS") || id.contains("SHOES") || id.contains("GLOVE") || id.contains("GAUNTLET")) {
			if (id.contains("LEATHER") || id.contains("GLOVE") || id.contains("RANCHER")) return new ItemStack(Items.LEATHER_BOOTS, count);
			if (id.contains("IRON")) return new ItemStack(Items.IRON_BOOTS, count);
			if (id.contains("GOLD")) return new ItemStack(Items.GOLDEN_BOOTS, count);
			if (id.contains("CHAIN")) return new ItemStack(Items.CHAINMAIL_BOOTS, count);
			return new ItemStack(Items.DIAMOND_BOOTS, count);
		}
		if (id.contains("SWORD") || id.contains("BLADE") || id.contains("KATANA") || id.contains("SCYTHE") || id.contains("DAGGER") || id.contains("HYPERION") || id.contains("ASTRAEA") || id.contains("SCYLLA") || id.contains("VALKYRIE") || id.contains("CLAYMORE") || id.contains("LIVID") || id.contains("SHADOW_FURY")) {
			if (id.contains("ROGUE") || id.contains("WOOD")) return new ItemStack(Items.WOODEN_SWORD, count);
			if (id.contains("STONE")) return new ItemStack(Items.STONE_SWORD, count);
			if (id.contains("IRON") || id.contains("CLEAVER")) return new ItemStack(Items.IRON_SWORD, count);
			if (id.contains("GOLD")) return new ItemStack(Items.GOLDEN_SWORD, count);
			return new ItemStack(Items.DIAMOND_SWORD, count);
		}
		if (id.contains("BOW") || id.contains("TERMINATOR") || id.contains("JUJU") || id.contains("SHORTBOW")) return new ItemStack(Items.BOW, count);
		if (id.contains("DRILL") || id.contains("PICKAXE") || id.contains("STONK")) return new ItemStack(Items.DIAMOND_PICKAXE, count);
		if (id.contains("AXE") || id.contains("CHOPPER") || id.contains("TREE")) return new ItemStack(Items.DIAMOND_AXE, count);
		if (id.contains("HOE") || id.contains("DICER") || id.contains("HARVESTER")) return new ItemStack(Items.DIAMOND_HOE, count);
		if (id.contains("ROD")) return new ItemStack(Items.FISHING_ROD, count);
		if (id.contains("TALISMAN") || id.contains("RING") || id.contains("ARTIFACT") || id.contains("RELIC") || id.contains("ORB") || id.contains("HEART") || id.contains("SCARF") || id.contains("FEATHER") || id.contains("PEST") || id.contains("BOOK")) {
			return new ItemStack(Items.PLAYER_HEAD, count);
		}
		if (id.contains("POTION")) return new ItemStack(Items.POTION, count);
		if (id.contains("STAR")) return new ItemStack(Items.NETHER_STAR, count);
		if (id.contains("COMPASS")) return new ItemStack(Items.COMPASS, count);
		if (id.contains("CLOCK")) return new ItemStack(Items.CLOCK, count);
		if (id.contains("GEM")) return new ItemStack(Items.EMERALD, count);
		return ItemStack.EMPTY;
	}


	private static final Pattern RARITY_LINE = Pattern.compile("^(?:[a-z]\\s+)?(VERY SPECIAL|UNCOMMON|COMMON|RARE|EPIC|LEGENDARY|MYTHIC|DIVINE|SPECIAL)(?:\\s|$)");
	private static void detectRarity(ParsedItem item) {
		for (int i = item.lore.size() - 1; i >= 0; i--) {
			String line = item.lore.get(i).replaceAll("§[0-9a-fk-orA-FK-OR]", "").strip();
			var match = RARITY_LINE.matcher(line);
			if (!match.find()) continue;
			item.rarity = match.group(1).replace(' ', '_').toUpperCase(Locale.ROOT);
			item.rarityColor = switch (item.rarity) {
				case "UNCOMMON" -> 0xFF55FF55;
				case "RARE" -> 0xFF5555FF;
				case "EPIC" -> 0xFFAA00AA;
				case "LEGENDARY" -> 0xFFFFAA00;
				case "MYTHIC" -> 0xFFFF55FF;
				case "DIVINE" -> 0xFF55FFFF;
				case "SPECIAL", "VERY_SPECIAL" -> 0xFFFF5555;
				default -> 0xFFFFFFFF;
			};
			break;
		}
	}
}