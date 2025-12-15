package com.calamarigold.configurabledeath.config;

import net.minecraftforge.common.ForgeConfigSpec;
import java.util.List;
import java.util.ArrayList;

public class ModConfig {
    public static ForgeConfigSpec.BooleanValue keepInventoryOnDeath;
    public static ForgeConfigSpec.BooleanValue keepArmorOnDeath;
    public static ForgeConfigSpec.BooleanValue keepHotbarOnDeath;
    public static ForgeConfigSpec.BooleanValue keepMainhandOnDeath;
    public static ForgeConfigSpec.BooleanValue keepOffhandOnDeath;
    public static ForgeConfigSpec.BooleanValue keepMainInventoryOnDeath;
    // Unified rule-based system: Format "damageSource:part:action"
    // damageSource: player, lava, fall, etc.
    // part: hotbar, armor, mainhand, offhand, mainInventory, inventory (all)
    // action: drop (force drop) or keep (force keep)
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> damageSourceRules;

    public static ForgeConfigSpec.DoubleValue durabilityLossOnKeptItems;
    public static ForgeConfigSpec.DoubleValue durabilityLossOnDrops;

    public static ForgeConfigSpec.BooleanValue keepFoodLevel;
    public static ForgeConfigSpec.IntValue maxFoodLevel;
    public static ForgeConfigSpec.IntValue minFoodLevel;
    public static ForgeConfigSpec.BooleanValue keepSaturation;

    public static ForgeConfigSpec.BooleanValue enableExperienceModule;
    public static ForgeConfigSpec.DoubleValue droppedXPPercent;
    public static ForgeConfigSpec.DoubleValue recoverableXPPercent;
    
    // Logging config option
    public static ForgeConfigSpec.BooleanValue enableDetailedLogging;

    public static void init(ForgeConfigSpec.Builder server) {
        // Create the itemDrops category
        ForgeConfigSpec.Builder itemDropsBuilder = server.comment("Inventory drop settings").push("itemDrops");
        
        keepInventoryOnDeath = itemDropsBuilder
                .comment("Should players keep their entire inventory on death?")
                .define("keepInventory", false);

        keepArmorOnDeath = itemDropsBuilder
                .comment("Should players keep their armor on death?")
                .define("keepArmor", false);

        keepHotbarOnDeath = itemDropsBuilder
                .comment("Should players keep their non-mainhand hotbar items on death?")
                .define("keepHotbar", false);

        keepMainhandOnDeath = itemDropsBuilder
                .comment("Should players keep their mainhand item on death?")
                .define("keepMainhand", false);

        keepOffhandOnDeath = itemDropsBuilder
                .comment("Should players keep their offhand item on death?")
                .define("keepOffhand", false);

        keepMainInventoryOnDeath = itemDropsBuilder
                .comment("Set to true to keep main inventory (non-equipped non-hotbar) items on death")
                .define("keepMainInventoryOnDeath", false);

        damageSourceRules = itemDropsBuilder
                .comment("Specific overrides for damage source + inventory part combinations.",
                        "Format: \"damageSource:part:action\" (colon-separated)",
                        "",
                        "Priority order:",
                        "  1. Specific rules (this config) - override base config for that part",
                        "  2. Base config (keepHotbar, keepArmor, etc.) - default behavior",
                        "",
                        "Available damage source types:",
                        "  - player: Killed by another player (PvP)",
                        "  - mob: Killed by a mob",
                        "  - arrow: Killed by an arrow",
                        "  - fall: Fall damage",
                        "  - fire: Fire damage",
                        "  - lava: Lava damage",
                        "  - drown: Drowning",
                        "  - explosion: Explosion damage",
                        "  - magic: Magic damage",
                        "  - wither: Wither effect",
                        "  - starve: Starvation",
                        "  - cactus: Cactus damage",
                        "  - sweetBerryBush: Sweet berry bush damage",
                        "  - sting: Bee sting",
                        "  - thorns: Thorns enchantment",
                        "  - outOfWorld: Void damage",
                        "  - generic: Generic damage",
                        "  - onFire: On fire",
                        "  - inFire: In fire",
                        "  - lightningBolt: Lightning strike",
                        "  - hotFloor: Magma block damage",
                        "  - inWall: Suffocation",
                        "  - cramming: Entity cramming",
                        "  - dryout: Dry out",
                        "  - freeze: Freezing",
                        "  - stalagmite: Stalagmite damage",
                        "  - outsideBorder: Outside world border",
                        "  - genericKill: Generic kill",
                        "",
                        "Parts: hotbar, armor, mainhand, offhand, mainInventory, inventory (all items)",
                        "Actions: drop (force drop) or keep (force keep)",
                        "",
                        "Examples:",
                        "  \"player:hotbar:drop\" - Hotbar drops on PvP deaths (even if keepHotbar=true)",
                        "  \"lava:armor:keep\" - Armor always kept on lava deaths (even if keepArmor=false)",
                        "  \"player:inventory:drop\" - Entire inventory drops on PvP deaths (whitelist behavior)",
                        "  \"fall:inventory:keep\" - Entire inventory kept on fall damage (blacklist behavior)",
                        "  \"fall:hotbar:keep\" - Hotbar always kept on fall damage",
                        "",
                        "Note: Rules are evaluated per-part, so you can mix different behaviors.",
                        "Example: keepHotbar=true, but \"player:hotbar:drop\" means hotbar kept normally, dropped on PvP.")
                .defineList("damageSourceRules", new ArrayList<String>(), obj -> obj instanceof String);
        
        itemDropsBuilder.pop(); // End itemDrops category

        // Create the durability category
        ForgeConfigSpec.Builder durabilityBuilder = server.comment("Durability loss settings").push("durability");
        
        durabilityLossOnKeptItems = durabilityBuilder
                .comment("Percent of durability lost on death for kept items")
                .defineInRange("durabilityLossOnKeptItems", 0.0, 0.0, 1.0);

        durabilityLossOnDrops = durabilityBuilder
                .comment("Percent of durability lost on death for drops")
                .defineInRange("durabilityLossOnDrops", 0.0, 0.0, 1.0);
        
        durabilityBuilder.pop(); // End durability category

        // Create the experience category
        ForgeConfigSpec.Builder experienceBuilder = server.comment("Experience settings").push("experience");
        
        enableExperienceModule = experienceBuilder
                .comment("Enable experience settings")
                .define("enableExperienceModule", false);
                
        droppedXPPercent = experienceBuilder
                .comment("Percent of experience dropped on death")
                .defineInRange("droppedXPPercent", 0.50, 0.0, 1.0);

        recoverableXPPercent = experienceBuilder
                .comment("Percent of dropped experience that can be recovered")
                .defineInRange("recoverableXPPercent", 0.25, 0.0, 1.0);
        
        experienceBuilder.pop(); // End experience category

        // Create the hunger category
        ForgeConfigSpec.Builder hungerBuilder = server.comment("Hunger settings").push("hunger");
        
        keepFoodLevel = hungerBuilder
                .comment("Set to true to retain food level on death")
                .define("keepFoodLevel", false);

        maxFoodLevel = hungerBuilder
                .comment("Highest amount of food level you can respawn with")
                .defineInRange("maxFoodLevel", 20, 0, 20);

        minFoodLevel = hungerBuilder
                .comment("Lowest amount of food level you can respawn with")
                .defineInRange("minFoodLevel", 14, 0, 20);

        keepSaturation = hungerBuilder
                .comment("Set to true to retain saturation on death")
                .define("keepSaturation", false);
        
        hungerBuilder.pop(); // End hunger category
        
        // Create the debug category
        ForgeConfigSpec.Builder debugBuilder = server.comment("Debug settings").push("debug");
        
        enableDetailedLogging = debugBuilder
                .comment("Enable detailed logging for debugging purposes. Set to false to reduce log spam.")
                .define("enableDetailedLogging", false);
                
        debugBuilder.pop(); // End debug category
    }
}
