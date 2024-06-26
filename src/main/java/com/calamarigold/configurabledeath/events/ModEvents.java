package com.calamarigold.configurabledeath.events;

import com.calamarigold.configurabledeath.config.ModConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.InteractionHand;

import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Mod.EventBusSubscriber
public class ModEvents {
    private static final Map<UUID, ItemStack[]> savedInventories = new HashMap<>();
    private static final Map<UUID, ItemStack[]> savedArmor = new HashMap<>();
    private static final Map<UUID, ItemStack> savedMainHandItems = new HashMap<>();
    private static final Map<UUID, ItemStack> savedOffHandItems = new HashMap<>();
    private static final Map<UUID, ItemStack[]> savedHotbarItems = new HashMap<>();
    private static final Map<UUID, ItemStack[]> savedMainInventoryItems = new HashMap<>();

    private static final Map<UUID, Integer> playerXPLevels = new HashMap<>();

    private static final Map<UUID, Integer> playerHungerLevels = new HashMap<>();
    private static final Map<UUID, Float> playerSaturationLevels = new HashMap<>();

    public static final Logger LOGGER = LogManager.getLogger("ConfigurableDeath");




    // Overloaded method for a single ItemStack
    private static void applyDurabilityLoss(ItemStack item, double durabilityLossPercentage) {
        if (item != null && item.isDamageableItem()) {
            int durabilityLoss = (int) Math.ceil(item.getMaxDamage() * durabilityLossPercentage);
            item.setDamageValue(item.getDamageValue() + durabilityLoss); // Directly set the new durability value

            if (item.getDamageValue() > item.getMaxDamage()) {
                item.shrink(1); // remove if item breaks
            }
        }
    }

    // Existing method for an array of ItemStacks
    private static void applyDurabilityLoss(ItemStack[] items, double durabilityLossPercentage) {
        if (items != null) {
            for (ItemStack item : items) {
                applyDurabilityLoss(item, durabilityLossPercentage); // Call the single ItemStack method
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        double durabilityLossPercentage = ModConfig.durabilityLossOnDrops.get();
        if (durabilityLossPercentage > 0) {
            for (ItemEntity itemEntity : event.getDrops()) {
                ItemStack item = itemEntity.getItem();
                applyDurabilityLoss(item, durabilityLossPercentage);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        UUID playerID = player.getUUID();

        boolean keepInventory = ModConfig.keepInventoryOnDeath.get();
        boolean keepArmor = ModConfig.keepArmorOnDeath.get();
        boolean keepHotbar = ModConfig.keepHotbarOnDeath.get();
        boolean keepMainhand = ModConfig.keepMainhandOnDeath.get();
        boolean keepOffhand = ModConfig.keepOffhandOnDeath.get();
        boolean keepMainInventory = ModConfig.keepMainInventoryOnDeath.get();

        // Store the player's current hunger and saturation level
        playerHungerLevels.put(playerID, player.getFoodData().getFoodLevel());
        playerSaturationLevels.put(playerID, player.getFoodData().getSaturationLevel());

        // Handle XP dropping
        if (ModConfig.enableExperienceModule.get()) {
            player.skipDropExperience();

            int currentTotalXP = player.totalExperience;
            int xpToDrop = (int) (currentTotalXP * ModConfig.droppedXPPercent.get());
            int xpToKeep = currentTotalXP - xpToDrop;
            playerXPLevels.put(player.getUUID(), xpToKeep);
            player.experienceLevel = 0;
            player.experienceProgress = 0;

            // Drop the recoverable portion of XP as an orb
            int xpToDropOnGround = (int) (xpToDrop * ModConfig.recoverableXPPercent.get());
            if (xpToDropOnGround > 0) {
                player.level.addFreshEntity(new ExperienceOrb(player.level, player.getX(), player.getY(), player.getZ(), xpToDropOnGround));
            }
        }

        if (keepInventory) {
            Container playerInventory = player.getInventory();
            ItemStack[] inventoryContents = new ItemStack[playerInventory.getContainerSize()];
            for (int i = 0; i < playerInventory.getContainerSize(); i++) {
                inventoryContents[i] = playerInventory.getItem(i).copy();
                playerInventory.setItem(i, ItemStack.EMPTY);
            }
            savedInventories.put(playerID, inventoryContents);
        }

        if (keepArmor) {
            ItemStack[] armorContents = new ItemStack[player.getInventory().armor.size()];
            for (int i = 0; i < player.getInventory().armor.size(); i++) {
                armorContents[i] = player.getItemBySlot(EquipmentSlot.byTypeAndIndex(EquipmentSlot.Type.ARMOR, i)).copy();
                player.setItemSlot(EquipmentSlot.byTypeAndIndex(EquipmentSlot.Type.ARMOR, i), ItemStack.EMPTY);
            }
            savedArmor.put(playerID, armorContents);
        }

        if (keepHotbar) {
            int hotbarSize = 9; // Assuming a standard hotbar size
            ItemStack[] hotbarContents = new ItemStack[hotbarSize];
            for (int i = 0; i < hotbarSize; i++) {
                ItemStack item = player.getInventory().items.get(i);
                hotbarContents[i] = item.copy();
                player.getInventory().items.set(i, ItemStack.EMPTY); // Clear the slot to prevent item drop
            }
            savedHotbarItems.put(playerID, hotbarContents);
        }

        if (keepMainhand) {
            ItemStack mainHandItem = player.getMainHandItem().copy();
            savedMainHandItems.put(playerID, mainHandItem);
            player.getInventory().setItem(player.getInventory().selected, ItemStack.EMPTY); // Clear the main hand slot
        }

        if (keepOffhand) {
            ItemStack offHandItem = player.getOffhandItem().copy();
            savedOffHandItems.put(playerID, offHandItem);
            player.getInventory().offhand.set(0, ItemStack.EMPTY); // Clear the offhand slot
        }

        if (keepMainInventory) {
            // The main inventory excludes armor slots and hotbar slots
            int inventoryStart = 9; // The start of the main inventory after the hotbar
            int inventoryEnd = 36; // The end of the main inventory before the armor slots
            ItemStack[] mainInventoryContents = new ItemStack[inventoryEnd - inventoryStart];
            for (int i = inventoryStart; i < inventoryEnd; i++) {
                mainInventoryContents[i - inventoryStart] = player.getInventory().items.get(i).copy();
                player.getInventory().items.set(i, ItemStack.EMPTY); // Clear the slot to prevent item drop
            }
            savedMainInventoryItems.put(playerID, mainInventoryContents);
        }

        // Apply durability loss to kept items if the config option is not zero
        double durabilityLossPercentage = ModConfig.durabilityLossOnKeptItems.get();
        if (durabilityLossPercentage > 0) {
            applyDurabilityLoss(savedInventories.get(playerID), durabilityLossPercentage);
            applyDurabilityLoss(savedArmor.get(playerID), durabilityLossPercentage);
            if (savedMainHandItems.containsKey(playerID)) {
                applyDurabilityLoss(savedMainHandItems.get(playerID), durabilityLossPercentage);
            }
            if (savedOffHandItems.containsKey(playerID)) {
                applyDurabilityLoss(savedOffHandItems.get(playerID), durabilityLossPercentage);
            }
            applyDurabilityLoss(savedHotbarItems.get(playerID), durabilityLossPercentage);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();
        UUID playerID = player.getUUID();

        // Handle food level
        if (ModConfig.keepFoodLevel.get() && playerHungerLevels.containsKey(playerID)) {
            int deathHungerLevel = playerHungerLevels.get(playerID);
            int respawnHungerLevel = Math.max(ModConfig.minFoodLevel.get(), deathHungerLevel);
            respawnHungerLevel = Math.min(respawnHungerLevel, ModConfig.maxFoodLevel.get());
            player.getFoodData().setFoodLevel(respawnHungerLevel);

            if (ModConfig.keepSaturation.get() && playerSaturationLevels.containsKey(playerID)) {
                player.getFoodData().setSaturation(playerSaturationLevels.get(playerID));
                playerSaturationLevels.remove(playerID);
            }
            playerHungerLevels.remove(playerID);
        }

        // Restore XP
        if (playerXPLevels.containsKey(playerID)) {
            int xpToRestore = playerXPLevels.get(playerID);
            player.giveExperiencePoints(xpToRestore);
            playerXPLevels.remove(playerID);
        }

        if (ModConfig.keepInventoryOnDeath.get() && savedInventories.containsKey(playerID)) {
            ItemStack[] inventoryContents = savedInventories.remove(playerID);
            Container playerInventory = player.getInventory();
            for (int i = 0; i < playerInventory.getContainerSize(); i++) {
                playerInventory.setItem(i, inventoryContents[i]);
            }
        }

        if (ModConfig.keepArmorOnDeath.get() && savedArmor.containsKey(playerID)) {
            ItemStack[] armorContents = savedArmor.remove(playerID);
            for (int i = 0; i < armorContents.length; i++) {
                player.setItemSlot(EquipmentSlot.byTypeAndIndex(EquipmentSlot.Type.ARMOR, i), armorContents[i]);
            }
        }

        // Restore hotbar items
        if (ModConfig.keepHotbarOnDeath.get() && savedHotbarItems.containsKey(playerID)) {
            ItemStack[] hotbarContents = savedHotbarItems.remove(playerID);
            for (int i = 0; i < hotbarContents.length; i++) {
                player.getInventory().items.set(i, hotbarContents[i] != null ? hotbarContents[i] : ItemStack.EMPTY);
            }
        }

        // Restore mainhand item
        if (ModConfig.keepMainhandOnDeath.get() && savedMainHandItems.containsKey(playerID)) {
            ItemStack mainHand = savedMainHandItems.remove(playerID);
            if (mainHand != null && !mainHand.isEmpty()) {
                player.setItemInHand(InteractionHand.MAIN_HAND, mainHand);
            } else {
                // Ensure main hand item is set correctly without affecting hotbar
            }
        }

        // Restore offhand item
        if (ModConfig.keepOffhandOnDeath.get() && savedOffHandItems.containsKey(playerID)) {
            ItemStack offHand = savedOffHandItems.remove(playerID);
            if (offHand != null) {
                player.setItemInHand(InteractionHand.OFF_HAND, offHand);
            } else {
                player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
            }
        }

        if (ModConfig.keepMainInventoryOnDeath.get() && savedMainInventoryItems.containsKey(playerID)) {
            ItemStack[] mainInventoryContents = savedMainInventoryItems.remove(playerID);
            for (int i = 0; i < mainInventoryContents.length; i++) {
                if (mainInventoryContents[i] != null) {
                    player.getInventory().items.set(i + 9, mainInventoryContents[i]); // Restore starting from slot 9
                } else {
                    player.getInventory().items.set(i + 9, ItemStack.EMPTY);
                }
            }
        }
    }


}
