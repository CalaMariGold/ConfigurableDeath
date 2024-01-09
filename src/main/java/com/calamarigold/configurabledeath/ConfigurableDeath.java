package com.calamarigold.configurabledeath;import com.calamarigold.configurabledeath.config.ModConfig;import net.minecraftforge.common.ForgeConfigSpec;import net.minecraftforge.eventbus.api.IEventBus;import net.minecraftforge.eventbus.api.SubscribeEvent;import net.minecraftforge.fml.ModLoadingContext;import net.minecraftforge.fml.common.Mod;import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;import org.apache.logging.log4j.LogManager;import org.apache.logging.log4j.Logger;@Mod("configurabledeath")public class ConfigurableDeath {    private static final ForgeConfigSpec serverConfig;    public static final Logger LOGGER = LogManager.getLogger("ConfigurableDeath");    static {        ForgeConfigSpec.Builder serverBuilder = new ForgeConfigSpec.Builder();        ModConfig.init(serverBuilder);        serverConfig = serverBuilder.build();    }    public ConfigurableDeath() {        ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.SERVER, serverConfig);        LOGGER.info("[Configurable Death] Initializing...");        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();        modEventBus.addListener(this::setup);        // Register other mod events here    }    private void setup(final FMLCommonSetupEvent event) {        LOGGER.info("[Configurable Death] Setting up...");        event.enqueueWork(() -> {            // Perform setup tasks that depend on config values here        });    }}