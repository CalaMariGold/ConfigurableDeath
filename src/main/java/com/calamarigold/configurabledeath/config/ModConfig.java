package com.calamarigold.configurabledeath.config;
import net.minecraftforge.common.ForgeConfigSpec;

public class ModConfig {
    public static ForgeConfigSpec.BooleanValue keepInventoryOnDeath;
    public static ForgeConfigSpec.BooleanValue keepArmorOnDeath;

    public static void init(ForgeConfigSpec.Builder server) {
        server.comment("Death settings");

        keepInventoryOnDeath = server
                .comment("Should players keep their inventory on death?")
                .define("death.keepInventory", false);

        keepArmorOnDeath = server
                .comment("Should players keep their armor on death?")
                .define("death.keepArmor", false);
    }
}