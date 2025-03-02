package com.calamarigold.configurabledeath.events;

import com.calamarigold.configurabledeath.ConfigurableDeath;
import com.calamarigold.configurabledeath.config.ModConfig;
import com.calamarigold.configurabledeath.util.ModLogger;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.InteractionHand;

import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.server.ServerStartedEvent;


import java.util.Random;
import java.util.UUID;

@Mod.EventBusSubscriber
public class ModEvents {
    // Overloaded method for a single ItemStack
    private static void applyDurabilityLoss(ItemStack item, double durabilityLossPercentage) {
        if (item != null && !item.isEmpty() && item.isDamageableItem()) {
            int maxDamage = item.getMaxDamage();
            int currentDamage = item.getDamageValue();
            int durabilityLoss = (int) Math.ceil(maxDamage * durabilityLossPercentage);
            
            // Ensure we don't exceed max damage
            int newDamage = Math.min(currentDamage + durabilityLoss, maxDamage);
            item.setDamageValue(newDamage);

            // If the item would break, remove it
            if (newDamage >= maxDamage) {
                item.shrink(1);
            }
        }
    }

    // Existing method for an array of ItemStacks
    private static void applyDurabilityLoss(ItemStack[] items, double durabilityLossPercentage) {
        if (items != null) {
            for (ItemStack item : items) {
                if (item != null) { 
                    applyDurabilityLoss(item, durabilityLossPercentage); // Call the single ItemStack method
                }
            }
        }
    }

    // Helper method to check if an item has Curse of Vanishing
    private static boolean hasCurseOfVanishing(ItemStack item) {
        if (item == null || item.isEmpty()) {
            return false;
        }
        
        // Use the proper Minecraft enchantment registry to check for Curse of Vanishing
        return EnchantmentHelper.getItemEnchantmentLevel(Enchantments.VANISHING_CURSE, item) > 0;
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
        
        ModLogger.info("Player {} died, processing death inventory", player.getName().getString());

        // Get the persistent data storage
        DeathInventoryData data = DeathInventoryData.get((ServerLevel) player.level);

        boolean keepInventory = ModConfig.keepInventoryOnDeath.get();
        boolean keepArmor = ModConfig.keepArmorOnDeath.get() && !keepInventory; // Only keep armor separately if not keeping entire inventory
        boolean keepHotbar = ModConfig.keepHotbarOnDeath.get() && !keepInventory; // Only keep hotbar separately if not keeping entire inventory
        boolean keepMainhand = ModConfig.keepMainhandOnDeath.get() && !keepInventory && !keepHotbar; // Only keep mainhand separately if not keeping inventory or hotbar
        boolean keepOffhand = ModConfig.keepOffhandOnDeath.get() && !keepInventory; // Only keep offhand separately if not keeping entire inventory
        boolean keepMainInventory = ModConfig.keepMainInventoryOnDeath.get() && !keepInventory; // Only keep main inventory separately if not keeping entire inventory
        
        ModLogger.debug("Configuration: keepInventory={}, keepArmor={}, keepHotbar={}, keepMainhand={}, keepOffhand={}, keepMainInventory={}",
                keepInventory, keepArmor, keepHotbar, keepMainhand, keepOffhand, keepMainInventory);

        // Store the player's current hunger and saturation level
        data.getPlayerHungerLevels().put(playerID, player.getFoodData().getFoodLevel());
        data.getPlayerSaturationLevels().put(playerID, player.getFoodData().getSaturationLevel());
        data.setDirty(); // Mark data as dirty to ensure it's saved

        // Handle XP dropping
        if (ModConfig.enableExperienceModule.get()) {
            player.skipDropExperience();

            int currentTotalXP = player.totalExperience;
            int xpToDrop = (int) (currentTotalXP * ModConfig.droppedXPPercent.get());
            int xpToKeep = currentTotalXP - xpToDrop;
            
            // Store XP to be restored on respawn
            if (xpToKeep > 0) {
                data.getPlayerXPLevels().put(playerID, xpToKeep);
                data.setDirty();
            }
            
            // Reset player XP
            player.experienceLevel = 0;
            player.experienceProgress = 0;
            player.totalExperience = 0;

            // Drop the recoverable portion of XP as an orb
            int xpToDropOnGround = (int) (xpToDrop * ModConfig.recoverableXPPercent.get());
            if (xpToDropOnGround > 0) {
                player.level.addFreshEntity(new ExperienceOrb(player.level, player.getX(), player.getY(), player.getZ(), xpToDropOnGround));
                ModLogger.debug("Dropped {} XP orbs for player {}", xpToDropOnGround, player.getName().getString());
            }
        }

        // Create temporary storage for items we'll save
        ItemStack[] inventoryContents = null;
        ItemStack[] armorContents = null;
        ItemStack[] hotbarContents = null;
        ItemStack mainHandItem = null;
        ItemStack offHandItem = null;
        ItemStack[] mainInventoryContents = null;

        // First, copy all the items we need to save
        if (keepInventory) {
            ModLogger.debug("Saving player inventory for {}", player.getName().getString());
            Container playerInventory = player.getInventory();
            inventoryContents = new ItemStack[playerInventory.getContainerSize()];
            for (int i = 0; i < playerInventory.getContainerSize(); i++) {
                ItemStack item = playerInventory.getItem(i).copy();
                // Don't save items with Curse of Vanishing
                if (!hasCurseOfVanishing(item)) {
                    inventoryContents[i] = item;
                } else {
                    ModLogger.debug("Item with Curse of Vanishing found in slot {}, it will be lost", i);
                    inventoryContents[i] = ItemStack.EMPTY;
                }
            }
        }

        if (keepArmor) {
            armorContents = new ItemStack[player.getInventory().armor.size()];
            for (int i = 0; i < player.getInventory().armor.size(); i++) {
                ItemStack item = player.getItemBySlot(EquipmentSlot.byTypeAndIndex(EquipmentSlot.Type.ARMOR, i)).copy();
                // Don't save armor with Curse of Vanishing
                if (!hasCurseOfVanishing(item)) {
                    armorContents[i] = item;
                } else {
                    ModLogger.debug("Armor with Curse of Vanishing found in slot {}, it will be lost", i);
                    armorContents[i] = ItemStack.EMPTY;
                }
            }
        }

        if (keepHotbar) {
            int hotbarSize = 9; // Assuming a standard hotbar size
            hotbarContents = new ItemStack[hotbarSize];
            for (int i = 0; i < hotbarSize; i++) {
                ItemStack item = player.getInventory().items.get(i).copy();
                // Don't save hotbar items with Curse of Vanishing
                if (!hasCurseOfVanishing(item)) {
                    hotbarContents[i] = item;
                } else {
                    ModLogger.debug("Hotbar item with Curse of Vanishing found in slot {}, it will be lost", i);
                    hotbarContents[i] = ItemStack.EMPTY;
                }
            }
        }

        if (keepMainhand) {
            ItemStack item = player.getMainHandItem().copy();
            // Don't save mainhand item with Curse of Vanishing
            if (!hasCurseOfVanishing(item)) {
                mainHandItem = item;
                ModLogger.debug("Saved main hand item: {}", mainHandItem);
            } else {
                ModLogger.debug("Main hand item has Curse of Vanishing, it will be lost");
                mainHandItem = ItemStack.EMPTY;
            }
        }

        if (keepOffhand) {
            ItemStack item = player.getOffhandItem().copy();
            // Don't save offhand item with Curse of Vanishing
            if (!hasCurseOfVanishing(item)) {
                offHandItem = item;
            } else {
                ModLogger.debug("Offhand item has Curse of Vanishing, it will be lost");
                offHandItem = ItemStack.EMPTY;
            }
        }

        if (keepMainInventory) {
            // The main inventory excludes armor slots and hotbar slots
            int inventoryStart = 9; // The start of the main inventory after the hotbar
            int inventoryEnd = 36; // The end of the main inventory before the armor slots
            mainInventoryContents = new ItemStack[inventoryEnd - inventoryStart];
            for (int i = inventoryStart; i < inventoryEnd; i++) {
                ItemStack item = player.getInventory().items.get(i).copy();
                // Don't save main inventory items with Curse of Vanishing
                if (!hasCurseOfVanishing(item)) {
                    mainInventoryContents[i - inventoryStart] = item;
                } else {
                    ModLogger.debug("Main inventory item with Curse of Vanishing found in slot {}, it will be lost", i);
                    mainInventoryContents[i - inventoryStart] = ItemStack.EMPTY;
                }
            }
        }

        // Apply durability loss to kept items if the config option is not zero
        double durabilityLossPercentage = ModConfig.durabilityLossOnKeptItems.get();
        if (durabilityLossPercentage > 0) {
            ModLogger.debug("Applying {}% durability loss to kept items", durabilityLossPercentage * 100);
            applyDurabilityLoss(inventoryContents, durabilityLossPercentage);
            applyDurabilityLoss(armorContents, durabilityLossPercentage);
            if (mainHandItem != null) {
                applyDurabilityLoss(mainHandItem, durabilityLossPercentage);
            }
            if (offHandItem != null) {
                applyDurabilityLoss(offHandItem, durabilityLossPercentage);
            }
            applyDurabilityLoss(hotbarContents, durabilityLossPercentage);
            applyDurabilityLoss(mainInventoryContents, durabilityLossPercentage);
        }

        // Now save all the items to persistent storage
        if (keepInventory && inventoryContents != null) {
            data.getSavedInventories().put(playerID, inventoryContents);
            data.setDirty();
            
            // Clear the inventory to prevent drops
            Container playerInventory = player.getInventory();
            for (int i = 0; i < playerInventory.getContainerSize(); i++) {
                playerInventory.setItem(i, ItemStack.EMPTY);
            }
        }

        if (keepArmor && armorContents != null) {
            data.getSavedArmor().put(playerID, armorContents);
            data.setDirty();
            
            // Clear the armor slots
            for (int i = 0; i < player.getInventory().armor.size(); i++) {
                player.setItemSlot(EquipmentSlot.byTypeAndIndex(EquipmentSlot.Type.ARMOR, i), ItemStack.EMPTY);
            }
        }

        if (keepHotbar && hotbarContents != null) {
            data.getSavedHotbarItems().put(playerID, hotbarContents);
            data.setDirty();
            
            // Clear the hotbar slots
            for (int i = 0; i < 9; i++) {
                player.getInventory().items.set(i, ItemStack.EMPTY);
            }
        }

        if (keepMainhand && mainHandItem != null) {
            data.getSavedMainHandItems().put(playerID, mainHandItem);
            data.setDirty();
            
            // Clear the main hand slot
            player.getInventory().setItem(player.getInventory().selected, ItemStack.EMPTY);
        }

        if (keepOffhand && offHandItem != null) {
            data.getSavedOffHandItems().put(playerID, offHandItem);
            data.setDirty();
            
            // Clear the offhand slot
            player.getInventory().offhand.set(0, ItemStack.EMPTY);
        }

        if (keepMainInventory && mainInventoryContents != null) {
            data.getSavedMainInventoryItems().put(playerID, mainInventoryContents);
            data.setDirty();
            
            // Clear the main inventory slots
            for (int i = 9; i < 36; i++) {
                player.getInventory().items.set(i, ItemStack.EMPTY);
            }
        }
        
        ModLogger.info("Successfully processed death inventory for player {}", player.getName().getString());
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        UUID playerID = player.getUUID();
        
        ModLogger.info("Player {} respawned, restoring saved inventory", player.getName().getString());

        // Get the persistent data storage
        DeathInventoryData data = DeathInventoryData.get((ServerLevel) player.level);

        // Log what data is available for restoration
        ModLogger.debug("Available data for restoration: keepInventory={}, keepArmor={}, keepHotbar={}, keepMainhand={}, keepOffhand={}, keepMainInventory={}",
                data.getSavedInventories().containsKey(playerID),
                data.getSavedArmor().containsKey(playerID),
                data.getSavedHotbarItems().containsKey(playerID),
                data.getSavedMainHandItems().containsKey(playerID),
                data.getSavedOffHandItems().containsKey(playerID),
                data.getSavedMainInventoryItems().containsKey(playerID));

        // Handle food level
        if (ModConfig.keepFoodLevel.get() && data.getPlayerHungerLevels().containsKey(playerID)) {
            int deathHungerLevel = data.getPlayerHungerLevels().get(playerID);
            int respawnHungerLevel = Math.min(
                Math.max(ModConfig.minFoodLevel.get(), deathHungerLevel),
                ModConfig.maxFoodLevel.get()
            );
            
            player.getFoodData().setFoodLevel(respawnHungerLevel);
            ModLogger.debug("Restored food level to {} for player {}", 
                respawnHungerLevel, player.getName().getString());

            // Handle saturation
            if (ModConfig.keepSaturation.get() && data.getPlayerSaturationLevels().containsKey(playerID)) {
                float saturation = data.getPlayerSaturationLevels().get(playerID);
                // Saturation can't be higher than food level
                saturation = Math.min(saturation, respawnHungerLevel);
                player.getFoodData().setSaturation(saturation);
                ModLogger.debug("Restored saturation to {} for player {}", 
                    saturation, player.getName().getString());
                
                data.getPlayerSaturationLevels().remove(playerID);
                data.setDirty();
            }
            
            data.getPlayerHungerLevels().remove(playerID);
            data.setDirty();
        }

        // Restore XP if enabled and available
        if (ModConfig.enableExperienceModule.get() && data.getPlayerXPLevels().containsKey(playerID)) {
            int xpToRestore = data.getPlayerXPLevels().get(playerID);
            if (xpToRestore > 0) {
                player.giveExperiencePoints(xpToRestore);
                ModLogger.debug("Restored {} XP for player {}", xpToRestore, player.getName().getString());
            }
            data.getPlayerXPLevels().remove(playerID);
            data.setDirty();
        }

        // First, check if we have a full inventory saved - if so, restore only that
        if (ModConfig.keepInventoryOnDeath.get() && data.getSavedInventories().containsKey(playerID)) {
            ModLogger.debug("Restoring full inventory for player {}", player.getName().getString());
            ItemStack[] inventoryContents = data.getSavedInventories().remove(playerID);
            data.setDirty();
            
            if (inventoryContents != null) {
                Container playerInventory = player.getInventory();
                int restoredItems = 0;
                
                for (int i = 0; i < Math.min(playerInventory.getContainerSize(), inventoryContents.length); i++) {
                    ItemStack item = inventoryContents[i];
                    if (item != null && !item.isEmpty()) {
                        playerInventory.setItem(i, item);
                        restoredItems++;
                    }
                }
                
                ModLogger.debug("Restored {} items to inventory for player {}", 
                    restoredItems, player.getName().getString());
            }
            
            // Clean up any other saved items since we've restored the full inventory
            if (data.getSavedArmor().containsKey(playerID)) {
                data.getSavedArmor().remove(playerID);
                data.setDirty();
            }
            if (data.getSavedHotbarItems().containsKey(playerID)) {
                data.getSavedHotbarItems().remove(playerID);
                data.setDirty();
            }
            if (data.getSavedMainHandItems().containsKey(playerID)) {
                data.getSavedMainHandItems().remove(playerID);
                data.setDirty();
            }
            if (data.getSavedOffHandItems().containsKey(playerID)) {
                data.getSavedOffHandItems().remove(playerID);
                data.setDirty();
            }
            if (data.getSavedMainInventoryItems().containsKey(playerID)) {
                data.getSavedMainInventoryItems().remove(playerID);
                data.setDirty();
            }
            
            ModLogger.info("Successfully restored inventory for player {}", player.getName().getString());
            return; // Exit early since we've restored everything
        }

        // Otherwise, restore individual components
        
        // Restore main inventory items if enabled and available
        if (ModConfig.keepMainInventoryOnDeath.get() && data.getSavedMainInventoryItems().containsKey(playerID)) {
            ItemStack[] mainInventoryContents = data.getSavedMainInventoryItems().remove(playerID);
            data.setDirty();
            
            if (mainInventoryContents != null) {
                for (int i = 0; i < Math.min(mainInventoryContents.length, player.getInventory().items.size() - 9); i++) {
                    ItemStack invItem = mainInventoryContents[i];
                    if (invItem != null && !invItem.isEmpty()) {
                        // Calculate the actual inventory slot (offset by 9 for hotbar)
                        int slotIndex = i + 9;
                        
                        // Get the current item in this inventory slot
                        ItemStack currentItem = player.getInventory().items.get(slotIndex);
                        
                        if (currentItem.isEmpty()) {
                            // Slot is empty, restore the item
                            player.getInventory().items.set(slotIndex, invItem);
                            ModLogger.debug("Restored inventory item {} in slot {} for player {}", 
                                invItem.getDisplayName().getString(), slotIndex, player.getName().getString());
                        } else if (ItemStack.isSameItemSameTags(currentItem, invItem) && currentItem.isStackable()) {
                            // Same item type and stackable, try to stack
                            int newCount = Math.min(currentItem.getCount() + invItem.getCount(), currentItem.getMaxStackSize());
                            currentItem.setCount(newCount);
                            ModLogger.debug("Stacked inventory item in slot {} for player {}", 
                                slotIndex, player.getName().getString());
                        } else {
                            // Different item or not stackable, try to find an empty slot
                            int emptySlot = player.getInventory().getFreeSlot();
                            if (emptySlot >= 0) {
                                player.getInventory().setItem(emptySlot, invItem);
                                ModLogger.debug("Placed inventory item in slot {} for player {}", 
                                    emptySlot, player.getName().getString());
                            } else {
                                // No empty slot, drop the item
                                player.drop(invItem, false);
                                ModLogger.debug("Dropped inventory item for player {} due to full inventory", 
                                    player.getName().getString());
                            }
                        }
                    }
                }
            }
        }
        
        // Restore armor if enabled and available
        if (ModConfig.keepArmorOnDeath.get() && data.getSavedArmor().containsKey(playerID)) {
            ItemStack[] armorContents = data.getSavedArmor().remove(playerID);
            data.setDirty();
            
            if (armorContents != null) {
                for (int i = 0; i < Math.min(armorContents.length, player.getInventory().armor.size()); i++) {
                    ItemStack armorItem = armorContents[i];
                    if (armorItem != null && !armorItem.isEmpty()) {
                        EquipmentSlot slot = EquipmentSlot.byTypeAndIndex(EquipmentSlot.Type.ARMOR, i);
                        ItemStack currentArmor = player.getItemBySlot(slot);
                        
                        if (currentArmor.isEmpty()) {
                            // Slot is empty, restore the armor
                            player.setItemSlot(slot, armorItem);
                            ModLogger.debug("Restored armor item {} in slot {} for player {}", 
                                armorItem.getDisplayName().getString(), slot, player.getName().getString());
                        } else {
                            // Slot is occupied, try to find an empty inventory slot
                            int emptySlot = player.getInventory().getFreeSlot();
                            if (emptySlot >= 0) {
                                player.getInventory().setItem(emptySlot, armorItem);
                                ModLogger.debug("Placed armor item in inventory slot {} for player {}", 
                                    emptySlot, player.getName().getString());
                            } else {
                                // No empty slot, drop the item
                                player.drop(armorItem, false);
                                ModLogger.debug("Dropped armor item for player {} due to full inventory", 
                                    player.getName().getString());
                            }
                        }
                    }
                }
            }
        }
        
        // Restore hotbar if enabled and available
        if (ModConfig.keepHotbarOnDeath.get() && data.getSavedHotbarItems().containsKey(playerID)) {
            ItemStack[] hotbarContents = data.getSavedHotbarItems().remove(playerID);
            data.setDirty();
            
            if (hotbarContents != null) {
                for (int i = 0; i < Math.min(hotbarContents.length, 9); i++) {
                    ItemStack hotbarItem = hotbarContents[i];
                    if (hotbarItem != null && !hotbarItem.isEmpty()) {
                        ItemStack currentItem = player.getInventory().items.get(i);
                        
                        if (currentItem.isEmpty()) {
                            // Slot is empty, restore the item
                            player.getInventory().items.set(i, hotbarItem);
                            ModLogger.debug("Restored hotbar item {} in slot {} for player {}", 
                                hotbarItem.getDisplayName().getString(), i, player.getName().getString());
                        } else if (ItemStack.isSameItemSameTags(currentItem, hotbarItem) && currentItem.isStackable()) {
                            // Same item type and stackable, try to stack
                            int newCount = Math.min(currentItem.getCount() + hotbarItem.getCount(), currentItem.getMaxStackSize());
                            currentItem.setCount(newCount);
                            ModLogger.debug("Stacked hotbar item in slot {} for player {}", 
                                i, player.getName().getString());
                        } else {
                            // Different item or not stackable, try to find an empty slot
                            int emptySlot = player.getInventory().getFreeSlot();
                            if (emptySlot >= 0) {
                                player.getInventory().setItem(emptySlot, hotbarItem);
                                ModLogger.debug("Placed hotbar item in inventory slot {} for player {}", 
                                    emptySlot, player.getName().getString());
                            } else {
                                // No empty slot, drop the item
                                player.drop(hotbarItem, false);
                                ModLogger.debug("Dropped hotbar item for player {} due to full inventory", 
                                    player.getName().getString());
                            }
                        }
                    }
                }
            }
        }
        
        // Restore offhand if enabled and available
        if (ModConfig.keepOffhandOnDeath.get() && data.getSavedOffHandItems().containsKey(playerID)) {
            ItemStack offHand = data.getSavedOffHandItems().remove(playerID);
            data.setDirty();
            
            if (offHand != null && !offHand.isEmpty()) {
                ItemStack currentOffhand = player.getOffhandItem();
                
                if (currentOffhand.isEmpty()) {
                    // Offhand slot is empty, restore the item
                    player.setItemInHand(InteractionHand.OFF_HAND, offHand);
                    ModLogger.debug("Restored offhand item {} for player {}", 
                        offHand.getDisplayName().getString(), player.getName().getString());
                } else {
                    // Offhand slot is occupied
                    if (ItemStack.isSameItemSameTags(currentOffhand, offHand) && currentOffhand.isStackable()) {
                        // Same item type and stackable, try to stack
                        int newCount = Math.min(currentOffhand.getCount() + offHand.getCount(), currentOffhand.getMaxStackSize());
                        currentOffhand.setCount(newCount);
                        ModLogger.debug("Stacked offhand item for player {}", player.getName().getString());
                    } else {
                        // Different item or not stackable, try to find an empty slot
                        int emptySlot = player.getInventory().getFreeSlot();
                        if (emptySlot >= 0) {
                            player.getInventory().setItem(emptySlot, offHand);
                            ModLogger.debug("Placed offhand item in slot {} for player {}", 
                                emptySlot, player.getName().getString());
                        } else {
                            // No empty slot, drop the item
                            player.drop(offHand, false);
                            ModLogger.debug("Dropped offhand item for player {} due to full inventory", 
                                player.getName().getString());
                        }
                    }
                }
            }
        }
        
        // Restore mainhand if enabled and available
        if (ModConfig.keepMainhandOnDeath.get() && data.getSavedMainHandItems().containsKey(playerID)) {
            ItemStack mainHand = data.getSavedMainHandItems().remove(playerID);
            data.setDirty();
            
            if (mainHand != null && !mainHand.isEmpty()) {
                int selectedSlot = player.getInventory().selected;
                ItemStack currentItem = player.getInventory().getItem(selectedSlot);
                
                if (currentItem.isEmpty()) {
                    // Slot is empty, restore the item
                    player.getInventory().items.set(selectedSlot, mainHand);
                    ModLogger.debug("Restored main hand item {} for player {}", 
                        mainHand.getDisplayName().getString(), player.getName().getString());
                } else {
                    // Slot is occupied
                    if (ItemStack.isSameItemSameTags(currentItem, mainHand) && currentItem.isStackable()) {
                        // Same item type and stackable, try to stack
                        int newCount = Math.min(currentItem.getCount() + mainHand.getCount(), currentItem.getMaxStackSize());
                        currentItem.setCount(newCount);
                        ModLogger.debug("Stacked main hand item for player {}", player.getName().getString());
                    } else {
                        // Different item or not stackable, try to find an empty slot
                        int emptySlot = player.getInventory().getFreeSlot();
                        if (emptySlot >= 0) {
                            player.getInventory().setItem(emptySlot, mainHand);
                            ModLogger.debug("Placed main hand item in slot {} for player {}", 
                                emptySlot, player.getName().getString());
                        } else {
                            // No empty slot, drop the item
                            player.drop(mainHand, false);
                            ModLogger.debug("Dropped main hand item for player {} due to full inventory", 
                                player.getName().getString());
                        }
                    }
                }
            }
        }
        
        ModLogger.info("Successfully restored inventory for player {}", player.getName().getString());
    }

}
