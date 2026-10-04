package silence.simsool.profileviewer;

import static silence.simsool.lucent.Lucent.mc;

import java.util.UUID;
import org.lwjgl.glfw.GLFW;

import com.mojang.brigadier.arguments.StringArgumentType;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.KeyMapping;
import silence.simsool.lucent.general.utils.LucentUtils;
import silence.simsool.lucent.general.utils.useful.UChat;
import silence.simsool.lucent.general.utils.useful.UScreen;
import silence.simsool.profileviewer.api.PlayerDbApi;
import silence.simsool.profileviewer.api.PvAuth;
import silence.simsool.profileviewer.ui.ProfileViewerScreen;

public class ProfileViewer implements ClientModInitializer {

	public static final String MOD_ID = "profileviewer";
	public static final String NAME = "ProfileViewer";
	public static final String VERSION = "1.0.0";

	public static KeyMapping.Category KEYBINDING_CATEGORY = KeyMapping.Category.register(LucentUtils.id("profileviewer"));
	public static KeyMapping OPEN_PV_KEY;

	@Override
	public void onInitializeClient() {
		OPEN_PV_KEY = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.profileviewer.open",
				GLFW.GLFW_KEY_UNKNOWN,
				KEYBINDING_CATEGORY
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (OPEN_PV_KEY.consumeClick()) {
				if (client.player != null && UScreen.isScreenClose()) openProfileViewer(client.player.getGameProfile().name(), client.player.getGameProfile().id());
			}
		});

		PvAuth.authenticateAsync();
		silence.simsool.profileviewer.api.repo.PetRepo.init();
		silence.simsool.profileviewer.api.repo.ItemRepo.init();

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			registerCommands(dispatcher, "pv");
			registerCommands(dispatcher, "sbpv");
			registerCommands(dispatcher, "profileviewer");
		});
	}

	private void registerCommands(com.mojang.brigadier.CommandDispatcher<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> dispatcher, String commandName) {
		dispatcher.register(ClientCommands.literal(commandName)
			.executes(context -> {
				if (mc.player != null) {
					openProfileViewer(mc.player.getGameProfile().name(), mc.player.getGameProfile().id());
				}
				return 1;
			})
			.then(ClientCommands.argument("player", StringArgumentType.string())
				.suggests((context, builder) -> {
					String remaining = builder.getRemaining().toLowerCase();
					if (mc.getConnection() != null) {
						for (var info : mc.getConnection().getOnlinePlayers()) {
							if (info.getProfile() != null && info.getProfile().name() != null) {
								String name = info.getProfile().name();
								if (name.toLowerCase().startsWith(remaining)) {
									builder.suggest(name);
								}
							}
						}
					}
					return builder.buildFuture();
				})
				.executes(context -> {
					String playerName = StringArgumentType.getString(context, "player");
					resolveAndOpen(playerName);
					return 1;
				})
			)
		);
	}

	public static void resolveAndOpen(String input) {
		UChat.chat("&7[&bProfileViewer&7] &f" + input + "&7님의 프로필을 불러오는 중...");
		PlayerDbApi.resolveGameProfile(input).whenComplete((profile, error) -> mc.execute(() -> {
			if (error != null) {
				UChat.chat("&c[ProfileViewer] Player lookup failed. Please retry.");
				return;
			}
			if (profile == null) {
				UChat.chat("&c[ProfileViewer] 플레이어 '" + input + "'를 찾을 수 없습니다.");
				return;
			}
			UScreen.setScreenMC(new ProfileViewerScreen(profile.name(), profile.id()));
		}));
	}

	public static void openProfileViewer(String username, UUID uuid) {
		UScreen.setScreenMC(new ProfileViewerScreen(username, uuid));
	}
}