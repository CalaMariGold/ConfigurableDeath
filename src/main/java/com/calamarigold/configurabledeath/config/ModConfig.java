package com.calamarigold.configurabledeath.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ModConfig {
    public static ForgeConfigSpec.BooleanValue keepInventoryOnDeath;
    public static ForgeConfigSpec.BooleanValue keepArmorOnDeath;
    public static ForgeConfigSpec.BooleanValue keepHotbarOnDeath;
    public static ForgeConfigSpec.BooleanValue keepMainhandOnDeath;
    public static ForgeConfigSpec.BooleanValue keepOffhandOnDeath;
    public static ForgeConfigSpec.BooleanValue keepMainInventoryOnDeath;

    public static ForgeConfigSpec.DoubleValue durabilityLossOnKeptItems;
    public static ForgeConfigSpec.DoubleValue durabilityLossOnDrops;




    public static void init(ForgeConfigSpec.Builder server) {
        server.comment("Death settings");

        keepInventoryOnDeath = server
                .comment("Should players keep their inventory on death?")
                .define("death.keepInventory", false);

        keepArmorOnDeath = server
                .comment("Should players keep their armor on death?")
                .define("death.keepArmor", true);

        keepHotbarOnDeath = server
                .comment("Should players keep their non-mainhand hotbar items on death?")
                .define("death.keepHotbar", true);

        keepMainhandOnDeath = server
                .comment("Should players keep their mainhand item on death?")
                .define("death.keepMainhand", false);

        keepOffhandOnDeath = server
                .comment("Should players keep their offhand item on death?")
                .define("death.keepOffhand", false);

        keepMainInventoryOnDeath = server
                .comment("Set to true to keep main inventory (non-equipped non-hotbar) items on death")
                .define("death.keepMainInventoryOnDeath", false);

        durabilityLossOnKeptItems = server
                .comment("Percent of durability lost on death for kept items")
                .defineInRange("death.durabilityLossOnKeptItems", 0.0, 0.0, 1.0);

        durabilityLossOnDrops = server
                .comment("Percent of durability lost on death for drops")
                .defineInRange("death.durabilityLossOnDrops", 0.0, 0.0, 1.0);

    }
}
