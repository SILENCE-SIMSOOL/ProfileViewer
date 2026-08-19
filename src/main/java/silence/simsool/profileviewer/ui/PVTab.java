package silence.simsool.profileviewer.ui;
 
import silence.simsool.lucent.general.utils.L10n;

public enum PVTab {
	OVERVIEW("pv.tab.overview", "\uE88A"),
	GEAR("pv.tab.gear", "\uE8C9"),
	PETS("pv.tab.pets", "\uE91D"),
	DUNGEONS("pv.tab.dungeons", "\uE834"),
	SLAYER("pv.tab.slayer", "\uE8E8"),
	MINING("pv.tab.mining", "\uE52F"),
	FORAGING("pv.tab.foraging", "\uE520"),
	GARDEN("pv.tab.garden", "\uE56C"),
	FISHING("pv.tab.fishing", "\uEA40"),

	MUSEUM("pv.tab.museum", "\uE88F"),
	RIFT("pv.tab.rift", "\uE3E7"),
	CHOCOLATE_FACTORY("pv.tab.chocolate_factory", "\uE5D2"),
	COLLECTIONS("pv.tab.collections", "\uE660");

	public final String translationKey;
	public final String icon;

	PVTab(String translationKey, String icon) {
		this.translationKey = translationKey;
		this.icon = icon;
	}

	public String getTitle() {
		return L10n.translate(translationKey);
	}
}