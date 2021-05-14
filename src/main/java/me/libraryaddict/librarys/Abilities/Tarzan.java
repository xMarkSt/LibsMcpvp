package me.libraryaddict.librarys.Abilities;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

import me.libraryaddict.Hungergames.Interfaces.Disableable;
import me.libraryaddict.Hungergames.Types.AbilityListener;

public class Tarzan extends AbilityListener implements Disableable {

    private HashMap<BlockFace, Byte> faces = new HashMap<BlockFace, Byte>();
    //private ArrayList<BlockFace> faces = new ArrayList<>();
    private ArrayList<Material> ignoreBlockTypes = new ArrayList<>();
    public int scanDownRadius = 5;
    public int scanSidewaysRadius = 5;
    public int scanUpRadius = 5;

    public Tarzan() {
        faces.put(BlockFace.SOUTH, (byte) 1);
        faces.put(BlockFace.WEST, (byte) 2);
        faces.put(BlockFace.NORTH, (byte) 4);
        faces.put(BlockFace.EAST, (byte) 8);
        ignoreBlockTypes.add(Material.AIR);
        ignoreBlockTypes.add(Material.WATER);
        ignoreBlockTypes.add(Material.LAVA);
        ignoreBlockTypes.add(Material.SNOW);
        ignoreBlockTypes.add(Material.LEGACY_LONG_GRASS);
        ignoreBlockTypes.add(Material.RED_MUSHROOM);
        ignoreBlockTypes.add(Material.LEGACY_RED_ROSE);
        ignoreBlockTypes.add(Material.LEGACY_YELLOW_FLOWER);
        ignoreBlockTypes.add(Material.BROWN_MUSHROOM);
        ignoreBlockTypes.add(Material.LEGACY_SIGN_POST);
        ignoreBlockTypes.add(Material.LEGACY_WALL_SIGN);
        ignoreBlockTypes.add(Material.FIRE);
        ignoreBlockTypes.add(Material.TORCH);
        ignoreBlockTypes.add(Material.REDSTONE_WIRE);
        ignoreBlockTypes.add(Material.LEGACY_REDSTONE_TORCH_OFF);
        ignoreBlockTypes.add(Material.LEGACY_REDSTONE_TORCH_ON);
        ignoreBlockTypes.add(Material.VINE);
        ignoreBlockTypes.add(Material.LEGACY_WATER_LILY);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (hasAbility(event.getPlayer()) && event.getPlayer().getItemInHand() != null
                && event.getPlayer().getItemInHand().getType() == Material.VINE) {
            Location loc = event.getPlayer().getLocation();
            for (int x = -scanSidewaysRadius; x < scanSidewaysRadius; x++) {
                for (int z = -scanSidewaysRadius; z < scanSidewaysRadius; z++) {
                    for (int y = -scanDownRadius; y < scanUpRadius; y++) {
                        if (loc.getY() + y <= 0)
                            continue;
                        Block b = loc.getWorld().getBlockAt(loc.getBlockX() + x, loc.getBlockY() + y, loc.getBlockZ() + z);
                        if (b.getType() == Material.AIR) {
                            ArrayList<BlockFace> faces = new ArrayList<>();
                            MultipleFacing blockData = (MultipleFacing) Bukkit.createBlockData(Material.VINE);
                            if (b.getRelative(BlockFace.UP).getType() == Material.VINE)
                                blockData = (MultipleFacing) b.getRelative(BlockFace.UP).getBlockData();
                            else
                                for (BlockFace face : BlockFace.values()) {
                                    Block block = b.getRelative(face);
                                    if (!ignoreBlockTypes.contains(block.getType()))
                                        blockData.setFace(face, true);
                                }
                            if (blockData.getFaces().size() > 0) {
                                b.setBlockData(blockData, false);
                            }
                        }
                    }
                }
            }
        }
    }
}
