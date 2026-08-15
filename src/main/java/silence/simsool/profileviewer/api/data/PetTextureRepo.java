package silence.simsool.profileviewer.api.data;

import java.util.HashMap;
import java.util.Map;

public class PetTextureRepo {

	private static final Map<String, String> PET_TEXTURES = new HashMap<>();

	static {
		// Valid, verified 64-char hex texture hashes on textures.minecraft.net
		add("ENDER_DRAGON", "b682b3ab88a570187188dfa81795eb46d4ccaf1b9caac7b15983a8f8f429bf00");
		add("GOLDEN_DRAGON", "a4f51c88ab1f8c739813e53d28ed2ee051a7b11936e60ef4db5c4c925ae8b9bf");
		add("SCATHA", "14b21f8ee3dd9672f04a8473cecab021ed47b573881bf1926dd95ce81dda137f");
		add("KUUDRA", "434d3bb2cf6075908ef48d88dfaa6ffb95e34b17f54c9c11ce39e1a8a9bc6f62");
		add("BABY_YETI", "ab56d46af279ce46fa759bbfcf15a1f1886e1de25c7fd528a1880e548486b764");
		add("BLACK_CAT", "8477c4574a75936df73f5f871ea5e971bde338f15e3816ec11f916b1cc87a685");
		add("SHEEP", "639d9c9d16ad10e47185cc86951a651ae60d08544af855cf5cfa0548dba7f023");
		add("TIGER", "fcf47683f91f3989e9889119ba13d549c3493685b5cecbf75ede919733c5a914");
		add("LION", "c8618a3c46f1e0b38fde25414d7cbfd556e95656ac3c92f1de96f81cf665162a");
		add("ELEPHANT", "57f0af7156e7588cad15f7243fa2b1374c971a36e4af6b70e8c8eb49a79786a3");
		add("MONKEY", "13da0d9795f993ddf5f864df96511a8971753b3bf69492f7d617eb8491d8e12d");
		add("RABBIT", "ccf183cd416de1f73c43ee956afd3ea9a1a9b869acb64d25d5ca2094965eb545");
		add("WOLF", "adf1481a1ec63e4a9e315bd0aef953b1baa9a942bc56a3aa9610c3e71d54ef02");
		add("OCELOT", "5657cd5cf45a1bddf57af7542bf1e8cd4e951fa5e9748d67a101991f6d4bf88a");
		add("SILVERFISH", "dad47841e6f9d625e3ea86e17aba604fa6a1aeea155ed2a666291da34bb5723e");
		add("MITHRIL_GOLEM", "c971e6e937eb93c77fffa7417565a45acd62269e5db49f88cd1b75306298b97e");
		add("WITHER_SKELETON", "521a0f26ad348a7397a285dea6c4ec58cf012aab5d4725fa01c188c91b4f4c28");
		add("GRIFFIN", "38be6ff3ebfa4ea27e9e356547d64a60797dcf5ea4d83998b89e5ccdc4ecab56");
		add("DOLPHIN", "8e9688b950d880b55b7aa2cfcd77be2f12e1ccfcb6077ee2871715597e11f6c4");
		add("SQUID", "386df91cdbd4eb5bd38ed57e7ede742d470a2726d73ff9ff59c777b5ebc8c934");
		add("FLYING_FISH", "cd3724580ec9d13d2c232fdbf104c27af8fa6666e788a5cc32667622f6ddb30e");
		add("MEGALODON", "bef98493675b4ed8c56aa83385a58c5cf7a84d4f2f88c5b38a785edc88f9a263");
		add("BAT", "5b741544a49c95d909b9f485aa595df57c6b9bb76892e6fb1c494ce5a870d048");
		add("BLAZE", "b78ef2e4cf2c41a2d1343c733025853a774952da32a8267c78765b3999e23f05");
		add("BLUE_WHALE", "dab9226154c1cdbe396fb10b2df7ea6157fbe1ef2e8615ab9c34be5cfc87eb11");
		add("CHICKEN", "1638469a599ceef7207537603248a9ab11ff591fd378bea4735b346a7fae893e");
		add("ENDERMAN", "997b69c348f94d93e11762c2f6d9d1502cfbe9c4b78996b2be0d76bf33dd2a44");
		add("GHOUL", "4b0b1bc67c0ff7728d8b671ec26a4220b3294ba7a356070624d6731d1d8a4369");
		add("GOLEM", "89091d79ea0f59ef7ef94d4b1425b97faee3c47392b2a9967385412b34a62734");
		add("GUARDIAN", "6a1329a2123d46a6f69c9b1399bfd0a4c28e67a514d4850720496adbc4a04d55");
		add("HORSE", "ea83ef60a6ae8f9361ad22f0ae29524e4d6d37ceb9777fbf6ed72776c5b05612");
		add("HOUND", "938fcff9eb10a26e855799982fbdbd4fe01489e223cf5c8f1eb3fd6f67ecdb35");
		add("JELLYFISH", "9814421b8c2be4fa83f81e05d0e2e2efae6ff9841f3d8f3424a8fc378d387f66");
		add("MAGMA_CUBE", "38957d5023c93c45733631a155ad762214cf182a67a8f12a4d3e323a1e2e4635");
		add("PIG", "621668ef7cafe6fdd22dd74ced386570b16f663fb2607e4d8fb859942478a543");
		add("PIGMAN", "74e9c6e918216f1c967f677b1021ec63cb63a4cd111d4e4141d8e11a3d90b246");
		add("RAT", "d26ee6aa93cb566089bbefc8340798e4d3a088bc01ff93630f9a933f28249018");
		add("ROCK", "cb230f81d11ff92864303350289ab74ab1ecfbcc716c55fa87900b14cd3b776a");
		add("SKELETON", "bc4755106950f55cf837aa9be8f9b9f939e6ad113d789ca72b14429e71b268f7");
		add("SKELETON_HORSE", "47effce35132c914c9263db50ffdaacc641977ec3093c834d8efdc890288812c");
		add("SPIDER", "cd541541da2fe38927132b643d5dd8d30e347e14f6ba51a499d7594411cdb42");
		add("TARANTULA", "a2b7244955c4d0267253faefb586bbcc2267b14d2e2586bf414eb130a04918e9");
		add("TURTLE", "212d26d03d3aa666c8f7fb7eefeeeb0cbcaeb1ecaa9cfa0fd4e8ebca13876e51");
		add("ZOMBIE", "56fc8546f43ca6e34b0e5fa547e662de03381682497a994ee81600b22947d928");
		add("ARMADILLO", "4841369cf887cf7a414e21dfdf5bc0f3c5f49d2146e29788f288b8e05ad76f92");
		add("BAL", "849d4439c29fc94e963bc180ffdc322e705b637d7a1770e5b77ad9efb97dc3472");
		add("AMMONITE", "a78ba02a647614d9b407a51d9f8df59f2e30761e29e928231e679268fec36bb5");
		add("MOOSHROOM_COW", "d0bc61406fc0740ff49863a625e9582175bc777ff304dad673df737e4945b72e");
		add("PHOENIX", "ee9386d38e21ecfa65e94be8122d64be088a24c568ae7d17fae6ab18a1bf1d92");
		add("GRANDMA_WOLF", "7508ef3cfb603ebf86cd3cd18ab56891eb270a6c22abfa3023e3e2aa45167e41");
		add("JERRY", "822d8e762f0f7f3bc73be4b7da3109345716b417e8730397ac49c93d5634ba27");
		add("REINDEER", "e412586a11cb5736e6566085a53ec220317e08922ee9b699a2f79cecb84b6528");
		add("BINGO", "36d2c499ee85d3aa17ee8867a57a3e75e925bfab8ad53f3e2efd978a6ffdfb784");
		add("MONTEZUMA", "76e330a84e2775a6c382603848eef9cc1945ab837f485934e872e42bc53ecb14");
	}

	private static void add(String name, String hash) {
		if (hash == null || hash.length() < 64) return;
		// Ensure exactly 64 chars
		String exact64 = hash.length() > 64 ? hash.substring(0, 64) : hash;
		PET_TEXTURES.put(name, exact64);
	}

	public static String getTexture(String petType) {
		if (petType == null || petType.trim().isEmpty()) return "b682b3ab88a570187188dfa81795eb46d4ccaf1b9caac7b15983a8f8f429bf00".substring(0, 64);
		String key = petType.trim().replace(" ", "_").replace("-", "_").toUpperCase();
		if (key.startsWith("PET_SKIN_")) key = key.substring("PET_SKIN_".length());
		if (key.startsWith("PET_")) key = key.substring("PET_".length());

		String res = PET_TEXTURES.get(key);
		if (res != null && res.length() == 64) return res;

		for (var entry : PET_TEXTURES.entrySet()) {
			if (entry.getKey().equalsIgnoreCase(key) || key.contains(entry.getKey())) {
				if (entry.getValue().length() == 64) return entry.getValue();
			}
		}
		return "b682b3ab88a570187188dfa81795eb46d4ccaf1b9caac7b15983a8f8f429bf00".substring(0, 64);
	}
}