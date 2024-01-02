package com.calamarigold.configurabledeath;import com.calamarigold.configurabledeath.config.ModConfig;import net.minecraftforge.common.ForgeConfigSpec;import net.minecraftforge.eventbus.api.IEventBus;import net.minecraftforge.fml.ModLoadingContext;import net.minecraftforge.fml.common.Mod;import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;import org.apache.logging.log4j.LogManager;import org.apache.logging.log4j.Logger;@Mod("configurabledeath")public class ConfigurableDeath {    private static final ForgeConfigSpec serverConfig;    public static final Logger LOGGER = LogManager.getLogger();    static {        ForgeConfigSpec.Builder serverBuilder = new ForgeConfigSpec.Builder();        ModConfig.init(serverBuilder);        serverConfig = serverBuilder.build();    }    public ConfigurableDeath() {        ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.SERVER, serverConfig);        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();        modEventBus.addListener(this::setup);        // Register other mod events here    }    private void setup(final FMLCommonSetupEvent event) {        // Log the current config values for debugging purposes        boolean keepInventory = ModConfig.keepInventoryOnDeath.get();        boolean keepArmor = ModConfig.keepArmorOnDeath.get();        // You can use your mod's logger to print out the configuration values        ConfigurableDeath.LOGGER.info("Keep Inventory on Death: " + keepInventory);        ConfigurableDeath.LOGGER.info("Keep Armor on Death: " + keepArmor);        // Perform setup tasks here    }}