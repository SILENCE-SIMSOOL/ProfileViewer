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
	public ItemStack itemStack = ItemStack.EMPTY;

	public static final ParsedItem EMPTY = new ParsedItem();

	public boolean isEmpty() {
		return (itemStack.isEmpty() && mcId.equals("minecraft:air")) && displayName.isEmpty();
	}
}