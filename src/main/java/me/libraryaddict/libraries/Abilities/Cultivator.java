package me.libraryaddict.libraries.Abilities;

import me.libraryaddict.Hungergames.Interfaces.Disableable;
import me.libraryaddict.Hungergames.Types.AbilityListener;

import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;

public class Cultivator extends AbilityListener implements Disableable {

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if (hasAbility(event.getPlayer())) {
            Block b = event.getBlock();
            if (b.getType() == Material.LEGACY_SAPLING) {
                BlockData data = b.getBlockData();
                b.setType(Material.AIR);
                boolean success;
                switch (data.getMaterial()) {
                    case SPRUCE_SAPLING:
                        success = b.getWorld().generateTree(b.getLocation(), TreeType.REDWOOD);
                        break;
                    case BIRCH_SAPLING:
                        success = b.getWorld().generateTree(b.getLocation(), TreeType.BIRCH);
                        break;
                    case JUNGLE_SAPLING:
                        success = b.getWorld().generateTree(b.getLocation(), TreeType.SMALL_JUNGLE);
                        break;
                    case ACACIA_SAPLING:
                        success = b.getWorld().generateTree(b.getLocation(), TreeType.ACACIA);
                        break;
                    case DARK_OAK_SAPLING:
                        success = b.getWorld().generateTree(b.getLocation(), TreeType.DARK_OAK);
                        break;
                    default:
                        success = b.getWorld().generateTree(b.getLocation(), TreeType.TREE);
                }

                if (!success)
                    b.setBlockData(data, false);
            } else if (b.getType() == Material.WHEAT) {
                Ageable blockData = (Ageable) b.getBlockData();
                blockData.setAge(blockData.getMaximumAge());
            }
        }
    }
}
