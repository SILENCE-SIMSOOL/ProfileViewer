package silence.simsool.profileviewer.api.data;

import silence.simsool.profileviewer.api.repo.PetRepo;

public class PetTextureRepo {

	private static final String DEFAULT_HASH = "b682b3ab88a570187188dfa81795eb46d4ccaf1b9caac7b15983a8f8f429bf00";

	public static String getTexture(String petType) {
		if (petType == null || petType.trim().isEmpty()) return DEFAULT_HASH;
		String key = petType.trim().replace(" ", "_").replace("-", "_").toUpperCase();
		if (key.startsWith("PET_SKIN_")) key = key.substring("PET_SKIN_".length());
		if (key.startsWith("PET_")) key = key.substring("PET_".length());

		PetRepo.PetEntry entry = PetRepo.getPet(key);
		if (entry != null && !entry.tiers.isEmpty()) {
			for (PetRepo.PetTier tier : entry.tiers.values()) {
				if (tier.texture != null && !tier.texture.isEmpty()) {
					return tier.texture;
				}
			}
		}
		return DEFAULT_HASH;
	}
}