package com.calamarigold.configurabledeath.events;

import com.calamarigold.configurabledeath.ConfigurableDeath;
import com.calamarigold.configurabledeath.util.ModLogger;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DeathInventoryData extends SavedData {
    private static final String DATA_NAME = "configurable_death_inventory";
    
    private final Map<UUID, ItemStack[]> savedInventories = new HashMap<>();
    private final Map<UUID, ItemStack[]> savedArmor = new HashMap<>();
    private final Map<UUID, ItemStack> savedMainHandItems = new HashMap<>();
    private final Map<UUID, ItemStack> savedOffHandItems = new HashMap<>();
    private final Map<UUID, ItemStack[]> savedHotbarItems = new HashMap<>();
    private final Map<UUID, ItemStack[]> savedMainInventoryItems = new HashMap<>();
    private final Map<UUID, Integer> playerXPLevels = new HashMap<>();
    private final Map<UUID, Integer> playerHungerLevels = new HashMap<>();
    private final Map<UUID, Float> playerSaturationLevels = new HashMap<>();

    public DeathInventoryData() {
        // Default constructor required for loading
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        // Save inventories
        CompoundTag inventoriesTag = new CompoundTag();
        savedInventories.forEach((uuid, items) -> {
            ListTag itemsList = new ListTag();
            for (ItemStack item : items) {
                CompoundTag itemTag = new CompoundTag();
                if (item != null) {
                    item.save(itemTag);
                }
                itemsList.add(itemTag);
            }
            inventoriesTag.put(uuid.toString(), itemsList);
        });
        tag.put("savedInventories", inventoriesTag);

        // Save armor
        CompoundTag armorTag = new CompoundTag();
        savedArmor.forEach((uuid, items) -> {
            ListTag itemsList = new ListTag();
            for (ItemStack item : items) {
                CompoundTag itemTag = new CompoundTag();
                if (item != null) {
                    item.save(itemTag);
                }
                itemsList.add(itemTag);
            }
            armorTag.put(uuid.toString(), itemsList);
        });
        tag.put("savedArmor", armorTag);

        // Save main hand items
        CompoundTag mainHandTag = new CompoundTag();
        savedMainHandItems.forEach((uuid, item) -> {
            CompoundTag itemTag = new CompoundTag();
            if (item != null) {
                item.save(itemTag);
            }
            mainHandTag.put(uuid.toString(), itemTag);
        });
        tag.put("savedMainHandItems", mainHandTag);

        // Save off hand items
        CompoundTag offHandTag = new CompoundTag();
        savedOffHandItems.forEach((uuid, item) -> {
            CompoundTag itemTag = new CompoundTag();
            if (item != null) {
                item.save(itemTag);
            }
            offHandTag.put(uuid.toString(), itemTag);
        });
        tag.put("savedOffHandItems", offHandTag);

        // Save hotbar items
        CompoundTag hotbarTag = new CompoundTag();
        savedHotbarItems.forEach((uuid, items) -> {
            ListTag itemsList = new ListTag();
            for (ItemStack item : items) {
                CompoundTag itemTag = new CompoundTag();
                if (item != null) {
                    item.save(itemTag);
                }
                itemsList.add(itemTag);
            }
            hotbarTag.put(uuid.toString(), itemsList);
        });
        tag.put("savedHotbarItems", hotbarTag);

        // Save main inventory items
        CompoundTag mainInventoryTag = new CompoundTag();
        savedMainInventoryItems.forEach((uuid, items) -> {
            ListTag itemsList = new ListTag();
            for (ItemStack item : items) {
                CompoundTag itemTag = new CompoundTag();
                if (item != null) {
                    item.save(itemTag);
                }
                itemsList.add(itemTag);
            }
            mainInventoryTag.put(uuid.toString(), itemsList);
        });
        tag.put("savedMainInventoryItems", mainInventoryTag);

        // Save XP levels
        CompoundTag xpTag = new CompoundTag();
        playerXPLevels.forEach((uuid, level) -> xpTag.putInt(uuid.toString(), level));
        tag.put("playerXPLevels", xpTag);

        // Save hunger levels
        CompoundTag hungerTag = new CompoundTag();
        playerHungerLevels.forEach((uuid, level) -> hungerTag.putInt(uuid.toString(), level));
        tag.put("playerHungerLevels", hungerTag);

        // Save saturation levels
        CompoundTag saturationTag = new CompoundTag();
        playerSaturationLevels.forEach((uuid, level) -> saturationTag.putFloat(uuid.toString(), level));
        tag.put("playerSaturationLevels", saturationTag);

        return tag;
    }

    public static DeathInventoryData load(CompoundTag tag) {
        DeathInventoryData data = new DeathInventoryData();
        
        // Load inventories
        if (tag.contains("savedInventories")) {
            CompoundTag inventoriesTag = tag.getCompound("savedInventories");
            for (String uuidStr : inventoriesTag.getAllKeys()) {
                UUID uuid = UUID.fromString(uuidStr);
                ListTag itemsList = inventoriesTag.getList(uuidStr, Tag.TAG_COMPOUND);
                ItemStack[] items = new ItemStack[itemsList.size()];
                for (int i = 0; i < itemsList.size(); i++) {
                    CompoundTag itemTag = itemsList.getCompound(i);
                    items[i] = ItemStack.of(itemTag);
                }
                data.savedInventories.put(uuid, items);
            }
        }

        // Load armor
        if (tag.contains("savedArmor")) {
            CompoundTag armorTag = tag.getCompound("savedArmor");
            for (String uuidStr : armorTag.getAllKeys()) {
                UUID uuid = UUID.fromString(uuidStr);
                ListTag itemsList = armorTag.getList(uuidStr, Tag.TAG_COMPOUND);
                ItemStack[] items = new ItemStack[itemsList.size()];
                for (int i = 0; i < itemsList.size(); i++) {
                    CompoundTag itemTag = itemsList.getCompound(i);
                    items[i] = ItemStack.of(itemTag);
                }
                data.savedArmor.put(uuid, items);
            }
        }

        // Load main hand items
        if (tag.contains("savedMainHandItems")) {
            CompoundTag mainHandTag = tag.getCompound("savedMainHandItems");
            for (String uuidStr : mainHandTag.getAllKeys()) {
                UUID uuid = UUID.fromString(uuidStr);
                CompoundTag itemTag = mainHandTag.getCompound(uuidStr);
                if (!itemTag.isEmpty()) {
                    data.savedMainHandItems.put(uuid, ItemStack.of(itemTag));
                }
            }
        }

        // Load off hand items
        if (tag.contains("savedOffHandItems")) {
            CompoundTag offHandTag = tag.getCompound("savedOffHandItems");
            for (String uuidStr : offHandTag.getAllKeys()) {
                UUID uuid = UUID.fromString(uuidStr);
                CompoundTag itemTag = offHandTag.getCompound(uuidStr);
                if (!itemTag.isEmpty()) {
                    data.savedOffHandItems.put(uuid, ItemStack.of(itemTag));
                }
            }
        }

        // Load hotbar items
        if (tag.contains("savedHotbarItems")) {
            CompoundTag hotbarTag = tag.getCompound("savedHotbarItems");
            for (String uuidStr : hotbarTag.getAllKeys()) {
                UUID uuid = UUID.fromString(uuidStr);
                ListTag itemsList = hotbarTag.getList(uuidStr, Tag.TAG_COMPOUND);
                ItemStack[] items = new ItemStack[itemsList.size()];
                for (int i = 0; i < itemsList.size(); i++) {
                    CompoundTag itemTag = itemsList.getCompound(i);
                    items[i] = ItemStack.of(itemTag);
                }
                data.savedHotbarItems.put(uuid, items);
            }
        }

        // Load main inventory items
        if (tag.contains("savedMainInventoryItems")) {
            CompoundTag mainInventoryTag = tag.getCompound("savedMainInventoryItems");
            for (String uuidStr : mainInventoryTag.getAllKeys()) {
                UUID uuid = UUID.fromString(uuidStr);
                ListTag itemsList = mainInventoryTag.getList(uuidStr, Tag.TAG_COMPOUND);
                ItemStack[] items = new ItemStack[itemsList.size()];
                for (int i = 0; i < itemsList.size(); i++) {
                    CompoundTag itemTag = itemsList.getCompound(i);
                    items[i] = ItemStack.of(itemTag);
                }
                data.savedMainInventoryItems.put(uuid, items);
            }
        }

        // Load XP levels
        if (tag.contains("playerXPLevels")) {
            CompoundTag xpTag = tag.getCompound("playerXPLevels");
            for (String uuidStr : xpTag.getAllKeys()) {
                UUID uuid = UUID.fromString(uuidStr);
                data.playerXPLevels.put(uuid, xpTag.getInt(uuidStr));
            }
        }

        // Load hunger levels
        if (tag.contains("playerHungerLevels")) {
            CompoundTag hungerTag = tag.getCompound("playerHungerLevels");
            for (String uuidStr : hungerTag.getAllKeys()) {
                UUID uuid = UUID.fromString(uuidStr);
                data.playerHungerLevels.put(uuid, hungerTag.getInt(uuidStr));
            }
        }

        // Load saturation levels
        if (tag.contains("playerSaturationLevels")) {
            CompoundTag saturationTag = tag.getCompound("playerSaturationLevels");
            for (String uuidStr : saturationTag.getAllKeys()) {
                UUID uuid = UUID.fromString(uuidStr);
                data.playerSaturationLevels.put(uuid, saturationTag.getFloat(uuidStr));
            }
        }

        return data;
    }

    // Static method to get or create the data
    public static DeathInventoryData get(ServerLevel level) {
        DimensionDataStorage storage = level.getDataStorage();
        return storage.computeIfAbsent(DeathInventoryData::load, DeathInventoryData::new, DATA_NAME);
    }

    // Methods to access and modify the data
    public Map<UUID, ItemStack[]> getSavedInventories() {
        return savedInventories;
    }

    public Map<UUID, ItemStack[]> getSavedArmor() {
        return savedArmor;
    }

    public Map<UUID, ItemStack> getSavedMainHandItems() {
        return savedMainHandItems;
    }

    public Map<UUID, ItemStack> getSavedOffHandItems() {
        return savedOffHandItems;
    }

    public Map<UUID, ItemStack[]> getSavedHotbarItems() {
        return savedHotbarItems;
    }

    public Map<UUID, ItemStack[]> getSavedMainInventoryItems() {
        return savedMainInventoryItems;
    }

    public Map<UUID, Integer> getPlayerXPLevels() {
        return playerXPLevels;
    }

    public Map<UUID, Integer> getPlayerHungerLevels() {
        return playerHungerLevels;
    }

    public Map<UUID, Float> getPlayerSaturationLevels() {
        return playerSaturationLevels;
    }

    // Mark the data as dirty whenever it's modified
    public void setDirty() {
        super.setDirty();
    }

    // Debug method to log the current state
    public void logState() {
        ModLogger.debug("Death Inventory Data state:");
        ModLogger.debug("Saved inventories: {}", savedInventories.size());
        ModLogger.debug("Saved armor: {}", savedArmor.size());
        ModLogger.debug("Saved main hand items: {}", savedMainHandItems.size());
        ModLogger.debug("Saved off hand items: {}", savedOffHandItems.size());
        ModLogger.debug("Saved hotbar items: {}", savedHotbarItems.size());
        ModLogger.debug("Saved main inventory items: {}", savedMainInventoryItems.size());
        ModLogger.debug("Player XP levels: {}", playerXPLevels.size());
        ModLogger.debug("Player hunger levels: {}", playerHungerLevels.size());
        ModLogger.debug("Player saturation levels: {}", playerSaturationLevels.size());
    }
} 