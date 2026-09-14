package silence.simsool.profileviewer.ui.tabs;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import silence.simsool.lucent.ui.utils.nvg.Fonts;
import silence.simsool.lucent.ui.utils.nvg.NVGRenderer;
import silence.simsool.lucent.ui.widget.components.TextBox;
import silence.simsool.profileviewer.api.data.MemberData;
import silence.simsool.profileviewer.api.data.MuseumData;
import silence.simsool.profileviewer.api.nbt.ParsedItem;
import silence.simsool.profileviewer.api.repo.ItemRepo;
import silence.simsool.profileviewer.ui.RenderHelper;

public class MuseumTabRenderer {

	public enum MuseumCategory {
		ALL("All", "\uEA40"),
		COMBAT("Combat", "\uE8EF"),
		DUNGEONEERING("Dungeons", "\uE3AF"),
		FARMING("Farming", "\uE8B8"),
		FISHING("Fishing", "\uE8F6"),
		FORAGING("Foraging", "\uE520"),
		HUNTING("Hunting", "\uE566"),
		MINING("Mining", "\uE3E8"),
		SPECIAL("Special", "\uE87D");

		public final String label;
		public final String icon;

		MuseumCategory(String label, String icon) {
			this.label = label;
			this.icon = icon;
		}
	}

	public enum MuseumFilter {
		ALL("All", 0xFF10B981),
		DONATED("Donated", 0xFF38BDF8),
		PARENT_DONATED("Donated (Through Parent)", 0xFF10B981),
		MISSING("Missing", 0xFFEF4444);

		public final String label;
		public final int color;

		MuseumFilter(String label, int color) {
			this.label = label;
			this.color = color;
		}
	}

	public static class MuseumCatalogItem {
		public final String id;
		public final String name;
		public final String parentId;
		public final MuseumCategory category;

		public MuseumCatalogItem(String id, String name, String parentId, MuseumCategory category) {
			this.id = id;
			this.name = name;
			this.parentId = parentId;
			this.category = category;
		}
	}

	public static final TextBox searchBox = new TextBox(0, 0, 160, 26, "");
	public static MuseumCategory activeCategory = MuseumCategory.ALL;
	public static MuseumFilter currentFilter = MuseumFilter.ALL;
	public static boolean filterDropdownOpen = false;

	// Comprehensive skyblock museum catalog
	private static final List<MuseumCatalogItem> CATALOG = new ArrayList<>();

	static {
		try (var stream = MuseumTabRenderer.class.getResourceAsStream("/assets/profileviewer/data/museum_catalog.json")) {
			if (stream != null) {
				JsonArray array = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonArray();
				for (JsonElement element : array) {
					JsonObject object = element.getAsJsonObject();
					MuseumCategory category = MuseumCategory.valueOf(object.get("category").getAsString());
					String parent = object.has("parent") && !object.get("parent").isJsonNull() ? object.get("parent").getAsString() : null;
					CATALOG.add(new MuseumCatalogItem(object.get("id").getAsString(), object.get("name").getAsString(), parent, category));
				}
			}
		} catch (Exception ignored) {}
	}

	public static float render(MemberData data, float startX, float startY, float width, float mouseX, float mouseY, float delta) {
		RenderHelper.clearGlobalSlots();
		float curY = startY;

		MuseumData m = (data != null && data.museum != null) ? data.museum : new MuseumData();

		// 1. Top Control Bar: Category Pills on Left, Selector Dropdown and Search Box on Right
		float catX = startX;
		float catH = 28f;

		for (MuseumCategory cat : MuseumCategory.values()) {
			boolean isSel = (cat == activeCategory);
			float catW = NVGRenderer.textWidth(cat.label, Fonts.PRETENDARD_SEMIBOLD, 13f) + 34f;
			boolean hov = mouseX >= catX && mouseX <= catX + catW && mouseY >= curY && mouseY <= curY + catH;

			int bgCol = isSel ? 0xFF6366F1 : (hov ? 0x336366F1 : 0x1AFFFFFF);
			int textCol = isSel ? 0xFFFFFFFF : (hov ? 0xFFA5B4FC : RenderHelper.FONT_MUTED);

			NVGRenderer.rect(catX, curY, catW, catH, bgCol, 6f);
			if (isSel) NVGRenderer.outlineRect(catX, curY, catW, catH, 1.2f, 0xFF818CF8, 6f);

			NVGRenderer.text(cat.icon, catX + 8f, curY + 6.5f, Fonts.MATERIAL_ICONS_ROUND, textCol, 14f);
			NVGRenderer.text(cat.label, catX + 26f, curY + 6.5f, Fonts.PRETENDARD_SEMIBOLD, textCol, 13f);

			catX += catW + 6f;
		}

		// Right controls: [Filter: Selector] and [Search Box]
		float searchW = 150f;
		float searchX = startX + width - searchW;
		float filterW = 160f;
		float btnX = searchX - filterW - 10f;
		float btnY = curY;
		float btnH = 26f;

		// Filter Selector Button (Interactive Selector Modal)
		boolean hovBtn = mouseX >= btnX && mouseX <= btnX + filterW && mouseY >= btnY && mouseY <= btnY + btnH;
		NVGRenderer.rect(btnX, btnY, filterW, btnH, hovBtn ? 0xFF2A2D3D : 0xFF1C1E2A, 6f);
		NVGRenderer.outlineRect(btnX, btnY, filterW, btnH, 1.2f, filterDropdownOpen ? 0xFF818CF8 : 0xFF4B5563, 6f);

		NVGRenderer.text("\uE152", btnX + 8f, btnY + 5.5f, Fonts.MATERIAL_ICONS_ROUND, currentFilter.color, 14f);
		String filterText = currentFilter.label;
		NVGRenderer.text(filterText, btnX + 26f, btnY + 6.5f, Fonts.PRETENDARD_SEMIBOLD, currentFilter.color, 12f);
		NVGRenderer.text(filterDropdownOpen ? "\uE316" : "\uE313", btnX + filterW - 16f, btnY + 6f, Fonts.MATERIAL_ICONS_ROUND, RenderHelper.FONT_MUTED, 14f);

		// Search Box on Far Right
		NVGRenderer.text("\uE8B6", searchX - 20f, curY + 5f, Fonts.MATERIAL_ICONS_ROUND, RenderHelper.FONT_MUTED, 16f);
		searchBox.setPosition((int) searchX, (int) curY);
		searchBox.render(null, (int) mouseX, (int) mouseY, delta);

		curY += catH + 16f;

		// 2. 15-Column Museum Grid
		int cols = 15;
		float slotSize = 34f;
		float slotGap = 4f;
		float gridW = cols * slotSize + (cols - 1) * slotGap;
		float gridStartX = startX + (width - gridW) / 2f;

		String q = searchBox.getValue().trim().toUpperCase(Locale.ROOT);

		List<MuseumCatalogItem> filtered = new ArrayList<>();
		for (MuseumCatalogItem item : CATALOG) {
			if (activeCategory != MuseumCategory.ALL && item.category != activeCategory) continue;

			boolean isDonated = isDonated(m, item.id);
			boolean isParent = !isDonated && item.parentId != null && isDonated(m, item.parentId);
			boolean isMissing = !isDonated && !isParent;

			if (!q.isEmpty() && !item.name.toUpperCase(Locale.ROOT).contains(q) && !item.id.contains(q)) {
				continue;
			}

			if (currentFilter == MuseumFilter.DONATED && !isDonated) continue;
			if (currentFilter == MuseumFilter.PARENT_DONATED && !isParent) continue;
			if (currentFilter == MuseumFilter.MISSING && !isMissing) continue;

			filtered.add(item);
		}

		int rows = Math.max(8, (filtered.size() + cols - 1) / cols);
		float gridH = rows * slotSize + (rows - 1) * slotGap + 24f;

		RenderHelper.drawModernCard(startX, curY, width, gridH, 12f, false);

		for (int i = 0; i < filtered.size(); i++) {
			MuseumCatalogItem cat = filtered.get(i);
			int r = i / cols;
			int c = i % cols;

			float sx = gridStartX + c * (slotSize + slotGap);
			float sy = curY + 12f + r * (slotSize + slotGap);

			boolean isDonated = isDonated(m, cat.id);
			boolean isParent = !isDonated && cat.parentId != null && isDonated(m, cat.parentId);

			boolean hov = mouseX >= sx && mouseX <= sx + slotSize && mouseY >= sy && mouseY <= sy + slotSize;
			RenderHelper.drawItemSlotBg(sx, sy, slotSize, hov, isDonated ? 0x4438BDF8 : (isParent ? 0x4410B981 : 0x22FFFFFF), 0x5511131E, 4f);

			ItemStack stack;
			if (isDonated) {
				List<ParsedItem> pList = donatedStacks(m, cat.id);
				stack = (pList != null && !pList.isEmpty()) ? pList.get(0).toItemStack() : ItemRepo.getItemStack(cat.id);
				if (stack.isEmpty()) stack = new ItemStack(Items.DIAMOND_SWORD);
			} else if (isParent) {
				stack = ItemRepo.getItemStack("LIME_DYE");
				if (stack.isEmpty()) stack = new ItemStack(Items.SLIME_BALL);
				stack.set(DataComponents.CUSTOM_NAME, Component.literal("§a" + cat.name));
				List<Component> lore = new ArrayList<>();
				lore.add(Component.literal("§aDonated (Through Parent: " + formatItemName(cat.parentId) + ")"));
				stack.set(DataComponents.LORE, new ItemLore(lore));
			} else {
				stack = ItemRepo.getItemStack("GRAY_DYE");
				if (stack.isEmpty()) stack = new ItemStack(Items.GUNPOWDER);
				stack.set(DataComponents.CUSTOM_NAME, Component.literal("§c" + cat.name));
				List<Component> lore = new ArrayList<>();
				lore.add(Component.literal("§cThis item has not been donated!"));
				stack.set(DataComponents.LORE, new ItemLore(lore));
			}

			RenderHelper.registerItemSlot(sx, sy, slotSize, stack);
		}

		// 3. Dropdown Selector Overlay (if open)
		if (filterDropdownOpen) {
			float dropY = btnY + btnH + 4f;
			float itemH = 24f;
			float dropH = MuseumFilter.values().length * itemH + 6f;

			NVGRenderer.rect(btnX, dropY, filterW, dropH, 0xF8181A26, 6f);
			NVGRenderer.outlineRect(btnX, dropY, filterW, dropH, 1.2f, 0xFF818CF8, 6f);

			for (int idx = 0; idx < MuseumFilter.values().length; idx++) {
				MuseumFilter mf = MuseumFilter.values()[idx];
				float iy = dropY + 3f + idx * itemH;
				boolean hov = mouseX >= btnX && mouseX <= btnX + filterW && mouseY >= iy && mouseY <= iy + itemH;

				if (hov) NVGRenderer.rect(btnX + 3f, iy, filterW - 6f, itemH, 0x22FFFFFF, 4f);

				boolean isCurrent = (mf == currentFilter);
				NVGRenderer.text(mf.label, btnX + 10f, iy + 6f, Fonts.PRETENDARD_SEMIBOLD, isCurrent ? mf.color : RenderHelper.FONT_SECONDARY, 11.5f);
				if (isCurrent) {
					NVGRenderer.text("\uE876", btnX + filterW - 16f, iy + 6f, Fonts.MATERIAL_ICONS_ROUND, mf.color, 12f);
				}
			}
		}

		curY += gridH + 16f;
		return curY - startY;
	}

	private static boolean isDonated(MuseumData data, String id) {
		if (data.donatedItemsMap.containsKey(id)) return true;
		return data.specialItems.stream().anyMatch(item -> id.equalsIgnoreCase(item.skyblockId));
	}

	private static List<ParsedItem> donatedStacks(MuseumData data, String id) {
		List<ParsedItem> stacks = data.donatedItemsMap.get(id);
		if (stacks != null) return stacks;
		return data.specialItems.stream().filter(item -> id.equalsIgnoreCase(item.skyblockId)).toList();
	}

	private static String formatItemName(String id) {
		String[] parts = id.replace("_", " ").toLowerCase(Locale.ROOT).split(" ");
		StringBuilder sb = new StringBuilder();
		for (String p : parts) {
			if (!p.isEmpty()) {
				sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1)).append(" ");
			}
		}
		return sb.toString().trim();
	}

	public static boolean mouseClicked(float mx, float my, float startX, float startY, float width) {
		if (searchBox.mouseClicked(mx, my, 0)) return true;

		float searchW = 150f;
		float searchX = startX + width - searchW;
		float filterW = 160f;
		float btnX = searchX - filterW - 10f;
		float btnY = startY;
		float btnH = 26f;

		// Filter dropdown clicks
		if (filterDropdownOpen) {
			float dropY = btnY + btnH + 4f;
			float itemH = 24f;
			for (int idx = 0; idx < MuseumFilter.values().length; idx++) {
				MuseumFilter mf = MuseumFilter.values()[idx];
				float iy = dropY + 3f + idx * itemH;
				if (mx >= btnX && mx <= btnX + filterW && my >= iy && my <= iy + itemH) {
					currentFilter = mf;
					filterDropdownOpen = false;
					return true;
				}
			}
			filterDropdownOpen = false;
			return true;
		}

		// Category clicks
		float catX = startX;
		float catH = 28f;
		for (MuseumCategory cat : MuseumCategory.values()) {
			float catW = NVGRenderer.textWidth(cat.label, Fonts.PRETENDARD_SEMIBOLD, 13f) + 34f;
			if (mx >= catX && mx <= catX + catW && my >= startY && my <= startY + catH) {
				activeCategory = cat;
				return true;
			}
			catX += catW + 6f;
		}

		// Toggle Filter Selector Dropdown
		if (mx >= btnX && mx <= btnX + filterW && my >= btnY && my <= btnY + btnH) {
			filterDropdownOpen = !filterDropdownOpen;
			return true;
		}

		return false;
	}

	public static boolean charTyped(char chr, int modifiers) {
		return searchBox.charTyped(chr, modifiers);
	}

	public static boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		return searchBox.keyPressed(keyCode, scanCode, modifiers);
	}
}
