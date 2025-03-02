# Configurable Death

A Minecraft Forge mod that allows you to configure what happens when a player dies.

## Features

- Configure which items are kept on death (inventory, armor, hotbar, mainhand, offhand)
- Configure durability loss for kept and dropped items
- Configure experience loss and recovery
- Configure hunger and saturation retention
- Persistent storage of death inventory data (items are not lost when server restarts)

## Recent Changes

### Version 1.1.0
- Added persistent storage for death inventory data
- Fixed issue where items would be lost when server restarts before player respawns
- Items, XP, and hunger data are now saved to world data and will persist through server restarts

## Configuration

All features can be configured in the server config file:

- `keepInventoryOnDeath`: Should players keep their entire inventory on death?
- `keepArmorOnDeath`: Should players keep their armor on death?
- `keepHotbarOnDeath`: Should players keep their hotbar items on death?
- `keepMainhandOnDeath`: Should players keep their mainhand item on death?
- `keepOffhandOnDeath`: Should players keep their offhand item on death?
- `keepMainInventoryOnDeath`: Should players keep their main inventory (non-hotbar) items on death?
- `durabilityLossOnKeptItems`: Percentage of durability lost on kept items (0.0 to 1.0)
- `durabilityLossOnDrops`: Percentage of durability lost on dropped items (0.0 to 1.0)
- `enableExperienceModule`: Enable the experience module
- `droppedXPPercent`: Percentage of XP dropped on death (0.0 to 1.0)
- `recoverableXPPercent`: Percentage of dropped XP that can be recovered (0.0 to 1.0)
- `keepFoodLevel`: Should players keep their food level on death?
- `maxFoodLevel`: Maximum food level players can respawn with
- `minFoodLevel`: Minimum food level players can respawn with
- `keepSaturation`: Should players keep their saturation on death?

## License

This project is licensed under the GNU GPL3 License - see the LICENSE file for details. 