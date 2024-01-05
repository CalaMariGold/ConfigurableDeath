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

    public static ForgeConfigSpec.BooleanValue keepFoodLevel;
    public static ForgeConfigSpec.IntValue maxFoodLevel;
    public static ForgeConfigSpec.IntValue minFoodLevel;
    public static ForgeConfigSpec.BooleanValue keepSaturation;


    public static void init(ForgeConfigSpec.Builder server) {
        server.comment("Inventory drop settings");
        server.comment("");
        keepInventoryOnDeath = server
                .comment("Should players keep their inventory on death?")
                .define("itemDrops.keepInventory", false);

        keepArmorOnDeath = server
                .comment("Should players keep their armor on death?")
                .define("itemDrops.keepArmor", true);

        keepHotbarOnDeath = server
                .comment("Should players keep their non-mainhand hotbar items on death?")
                .define("itemDrops.keepHotbar", true);

        keepMainhandOnDeath = server
                .comment("Should players keep their mainhand item on death?")
                .define("itemDrops.keepMainhand", false);

        keepOffhandOnDeath = server
                .comment("Should players keep their offhand item on death?")
                .define("itemDrops.keepOffhand", false);

        keepMainInventoryOnDeath = server
                .comment("Set to true to keep main inventory (non-equipped non-hotbar) items on death")
                .define("itemDrops.keepMainInventoryOnDeath", false);


        server.comment("Durability loss settings");
        server.comment("");
        durabilityLossOnKeptItems = server
                .comment("Percent of durability lost on death for kept items")
                .defineInRange("durability.durabilityLossOnKeptItems", 0.0, 0.0, 1.0);

        durabilityLossOnDrops = server
                .comment("Percent of durability lost on death for drops")
                .defineInRange("durability.durabilityLossOnDrops", 0.0, 0.0, 1.0);

        server.comment("Hunger settings");
        server.comment("");
        keepFoodLevel = server
                .comment("Set to true to retain food level on death")
                .define("hunger.keepFoodLevel", true);

        maxFoodLevel = server
                .comment("Highest amount of food level you can respawn with")
                .defineInRange("hunger.maxFoodLevel", 20, 0, 20);

        minFoodLevel = server
                .comment("Lowest amount of food level you can respawn with")
                .defineInRange("hunger.minFoodLevel", 14, 0, 20);

        keepSaturation = server
                .comment("Set to true to retain saturation on death")
                .define("hunger.keepSaturation", false);
    }
}
