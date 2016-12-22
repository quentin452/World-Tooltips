package ninja.genuine.tooltips;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.common.config.Property.Type;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLModDisabledEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import ninja.genuine.tooltips.client.KeyBindingConfig;
import ninja.genuine.tooltips.client.RenderEvent;
import ninja.genuine.tooltips.client.gui.ColorConfigGui;
import ninja.genuine.tooltips.system.Tooltip;

@Mod(modid = WorldTooltips.MODID, name = WorldTooltips.NAME, version = WorldTooltips.VERSION, canBeDeactivated = true, clientSideOnly = true, updateJSON = WorldTooltips.URL
		+ "update.json", useMetadata = true, guiFactory = "ninja.genuine.tooltips.client.TooltipsGuiFactory")
public class WorldTooltips {

	@Instance(WorldTooltips.MODID)
	public static WorldTooltips instance;
	public static Configuration config;
	public static final String MODID = "worldtooltips";
	public static final String NAME = "World-Tooltips";
	public static final String GUIID = "worldtooltipsgui";
	public static final String URL = "http://genuine.ninja/world-tooltips/";
	public static final String VERSION = "1.2.3";
	public static final String DESC = "Choose a color in hexidecimal (ie: 0xAB12cd or #AB12cd) \nYou can look up your favorite colors online.";
	public RenderEvent events;
	public Property colorBackground, overrideOutlineColor;
	public boolean hideModName, overrideOutline;
	public float alpha;
	public int maxDistance;
	private KeyBinding configKey;
	private boolean enabled = false;

	public WorldTooltips() {
		instance = this;
	}

	@EventHandler
	public void pre(FMLPreInitializationEvent event) {
		config = new Configuration(event.getSuggestedConfigurationFile(), VERSION);
		enabled = config.get("Appearance", "Enable Mod", true, "Enable rendering the tooltips.").getBoolean();
		sync();
		configKey = new KeyBindingConfig();
		ClientRegistry.registerKeyBinding(configKey);
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		events = new RenderEvent();
		Tooltip.init();
		if (enabled)
			enable();
		MinecraftForge.EVENT_BUS.register(this);
	}

	@EventHandler
	public void post(FMLPostInitializationEvent event) {
		events.post();
	}

	public void enable() {
		MinecraftForge.EVENT_BUS.register(events);
		enabled = true;
	}

	@EventHandler
	public void disable(FMLModDisabledEvent event) {
		MinecraftForge.EVENT_BUS.unregister(events);
		enabled = false;
	}

	@SubscribeEvent
	public void keypress(KeyInputEvent event) {
		// TODO Implement a config gui when I can figure out a nice color picker. It's getting there.
		if (configKey.isPressed())
			Minecraft.getMinecraft().displayGuiScreen(new ColorConfigGui(Minecraft.getMinecraft().currentScreen));
	}

	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
		if (event.getModID().equals(MODID)) {
			if (event.getConfigID().equals(GUIID)) {
				boolean tmp = enabled;
				enabled = config.get("Appearance", "Enable Mod", true, "Enable rendering the tooltips.").getBoolean();
				if (tmp != enabled) {
					if (enabled)
						enable();
					else
						disable(null);
				}
				sync();
				events.sync();
			}
		}
	}

	public void sync() {
		hideModName = config.getBoolean("Hide Mod Name", "Appearance", false, "Hide mod names on tooltips.");
		maxDistance = config.getInt("Maximum Draw Distance", "Appearance", 8, 2, 64, "Set the maximum distance that tooltips should be displayed from.");
		overrideOutline = config.getBoolean("Override Outline", "Appearance", false, "If enabled outline color will be manually set instead of default behavior.");
		alpha = config.getFloat("Transparency", "Appearance", 0.8F, 0.0F, 1.0F, "Set the opacity for the tooltips; 0 being completely invisible and 1 being completely opaque.");
		colorBackground = config.get("Appearance", "Background Color", "0x100010", DESC, Type.COLOR);
		overrideOutlineColor = config.get("Appearance", "Outline Color", "0x5000FF", DESC, Type.COLOR);
		config.save();
	}
}
