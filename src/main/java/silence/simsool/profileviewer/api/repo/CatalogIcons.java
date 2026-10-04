package silence.simsool.profileviewer.api.repo;

import com.google.gson.JsonObject;
import java.util.Locale;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import silence.simsool.profileviewer.api.data.ProfileJson;
import silence.simsool.profileviewer.api.nbt.NbtItemParser;

public final class CatalogIcons {
	private CatalogIcons() {}

	public static ItemStack item(String id) {
		ItemStack stack = ItemRepo.getItemStack(id);
		if (!stack.isEmpty()) return stack;
		Identifier key = Identifier.tryParse(id.toLowerCase(Locale.ROOT));
		if (key != null && BuiltInRegistries.ITEM.containsKey(key)) return new ItemStack(BuiltInRegistries.ITEM.getValue(key));
		return new ItemStack(Items.BARRIER);
	}

	public static ItemStack icon(JsonObject definition, String title) {
		String texture = ProfileJson.string(definition, "texture");
		ItemStack stack = texture.isEmpty() ? item(ProfileJson.string(definition, "item")) : NbtItemParser.createSkull(texture.replaceFirst("[^A-Za-z0-9+/=].*$", ""), 1);
		stack.set(DataComponents.CUSTOM_NAME, Component.literal(title));
		return stack;
	}
}
