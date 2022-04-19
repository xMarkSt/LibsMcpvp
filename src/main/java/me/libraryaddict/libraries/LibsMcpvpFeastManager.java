package me.libraryaddict.libraries;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import me.libraryaddict.Hungergames.Interfaces.ChestManager;
import me.libraryaddict.Hungergames.Listeners.LibsFeastManager;
import me.libraryaddict.Hungergames.Types.HungergamesApi;

public class LibsMcpvpFeastManager extends LibsFeastManager {

    @Override
    public void generateChests(final Location loc, int height) {
        final int h = height - 1;
        ChestManager cm = HungergamesApi.getChestManager();
        for (int x = -h; x <= h; x++) {
            for (int z = -h; z <= h; z++) {
                Block block = loc.clone().add(x, 0, z).getBlock();
                Block b = block;
                if (x == 0 && z == 0)
                    gen.setBlockFast(b, Material.ENCHANTING_TABLE.createBlockData());
                else if (Math.abs(x + z) % 2 == 0) {
                    gen.addToProcessedBlocks(block);
                    block.setType(Material.CHEST, false);
                    Chest chest = (Chest) block.getState();
                    cm.fillChest(chest.getInventory());
                    chest.update();
                }
            }
        }
    }
}
