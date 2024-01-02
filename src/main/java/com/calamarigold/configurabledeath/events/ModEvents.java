package com.calamarigold.configurabledeath.events;

import com.calamarigold.configurabledeath.config.ModConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
public class ModEvents {
    private static final Map<UUID, ItemStack[]> savedInventories = new HashMap<>();
    private static final Map<UUID, ItemStack[]> savedArmor = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        UUID playerID = player.getUUID();

        if (ModConfig.keepInventoryOnDeath.get()) {
            Container playerInventory = player.getInventory();
            ItemStack[] inventoryContents = new ItemStack[playerInventory.getContainerSize()];
            for (int i = 0; i < playerInventory.getContainerSize(); i++) {
                inventoryContents[i] = playerInventory.getItem(i).copy();
                playerInventory.setItem(i, ItemStack.EMPTY);
            }
            savedInventories.put(playerID, inventoryContents);
        }

        if (ModConfig.keepArmorOnDeath.get()) {
            ItemStack[] armorContents = new ItemStack[player.getInventory().armor.size()];
            for (int i = 0; i < player.getInventory().armor.size(); i++) {
                armorContents[i] = player.getItemBySlot(EquipmentSlot.byTypeAndIndex(EquipmentSlot.Type.ARMOR, i)).copy();
                player.setItemSlot(EquipmentSlot.byTypeAndIndex(EquipmentSlot.Type.ARMOR, i), ItemStack.EMPTY);
            }
            savedArmor.put(playerID, armorContents);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        // The 'event' parameter already provides the player directly in Forge 1.19.2.
        ServerPlayer player = (ServerPlayer) event.getEntity();
        UUID playerID = player.getUUID();

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
    }

}
