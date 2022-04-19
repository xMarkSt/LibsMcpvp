package me.libraryaddict.libraries.Abilities;

import me.libraryaddict.Hungergames.Interfaces.Disableable;
import me.libraryaddict.Hungergames.Types.AbilityListener;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;

public class Lumberjack extends AbilityListener implements Disableable {
    public String[] materialsEffected = new String[] {
            "OAK_LOG", "BIRCH_LOG", "ACACIA_LOG", "DARK_OAK_LOG",
            "JUNGLE_LOG", "SPRUCE_LOG", "STRIPPED_OAK_LOG", "STRIPPED_BIRCH_LOG", "STRIPPED_ACACIA_LOG",
            "STRIPPED_DARK_OAK_LOG", "STRIPPED_JUNGLE_LOG", "STRIPPED_SPRUCE_LOG"
    };

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (hasAbility(event.getPlayer())) {
            for (String mat : materialsEffected) {
                if (event.getBlock().getType().name().equalsIgnoreCase(mat)) {
                    Block b = event.getBlock().getRelative(BlockFace.UP);
                    boolean hasUp = true;
                    while (hasUp) {
                        hasUp = false;
                        for (String type : materialsEffected) {
                            if (b.getType().name().equalsIgnoreCase(type)) {
                                b.breakNaturally();
                                b = b.getRelative(BlockFace.UP);
                                hasUp = true;
                                break;
                            }
                        }
                    }
                    break;
                }
            }
        }
    }

}
