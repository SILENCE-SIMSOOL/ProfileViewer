package silence.simsool.profileviewer.api.nbt;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;

public class ParsedItem {

	public String skyblockId = "";
	public String mcId = "minecraft:air";
	public int count = 1;
	public String displayName = "";
	public List<String> lore = new ArrayList<>();
	public String rarity = "COMMON";
	public int rarityColor = 0xFFFFFFFF;
	public boolean hasGlint = false;
	public double estimatedValue = 0;
	public int upgradeScore = 0;
	public ItemStack itemStack = ItemStack.EMPTY;

	public static final ParsedItem EMPTY = new ParsedItem();

	public boolean isEmpty() {
		return (itemStack.isEmpty() && mcId.equals("minecraft:air")) && displayName.isEmpty();
	}

	public ItemStack toItemStack() {
		ItemStack stack = itemStack != null && !itemStack.isEmpty() ? itemStack.copy() : new ItemStack(net.minecraft.world.item.Items.PAPER);
		if (displayName != null && !displayName.isEmpty()) {
			stack.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.literal(displayName));
		}
		if (lore != null && !lore.isEmpty()) {
			List<net.minecraft.network.chat.Component> compLore = new ArrayList<>();
			for (String l : lore) {
				compLore.add(net.minecraft.network.chat.Component.literal(l));
			}
			stack.set(net.minecraft.core.component.DataComponents.LORE, new net.minecraft.world.item.component.ItemLore(compLore));
		}
		return stack;
	}
}
