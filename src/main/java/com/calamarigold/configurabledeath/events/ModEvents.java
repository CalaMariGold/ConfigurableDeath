package com.calamarigold.configurabledeath.events;

import com.calamarigold.configurabledeath.config.ModConfig;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class ModEvents {
    @SubscribeEvent
    public static void onPlayerDeath(PlayerEvent.PlayerRespawnEvent event) {
        if (ModConfig.keepInventoryOnDeath.get()) {
            // Logic to keep inventory goes here
        }
        if (ModConfig.keepArmorOnDeath.get()) {
            // Logic to keep armor goes here
        }
    }

}
