package me.libraryaddict.libraries.Abilities;

import me.libraryaddict.Hungergames.Interfaces.Disableable;
import me.libraryaddict.Hungergames.Types.AbilityListener;
import me.libraryaddict.Hungergames.Types.HungergamesApi;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

public class Digger extends AbilityListener implements Disableable {
    public int delayInTicks = 30;
    public String diggerBlock = Material.DRAGON_EGG.name();
    private Material diggerBlockMat = Material.matchMaterial(diggerBlock);
    public int goDownY = 5;
    public int goSideways = 5;
    public String messageAfterPlaced = ChatColor.RED + "You placed the egg. Run!";
    public String nameOfItem = "Mining Mole";

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (item != null && item.getType() == diggerBlockMat && isSpecialItem(item, nameOfItem) && hasAbility(event.getPlayer())) {
            final Block b = event.getBlock();
            b.setType(Material.AIR);
            event.getPlayer().sendMessage(messageAfterPlaced);
            Bukkit.getScheduler().scheduleSyncDelayedTask(HungergamesApi.getHungergames(), new Runnable() {
                public void run() {
                    int dist = (int) Math.ceil((double) (goSideways - 1) / 2);
                    for (int y = -1; y >= -goDownY; y--) {
                        for (int x = -dist; x <= dist; x++) {
                            for (int z = -dist; z <= dist; z++) {
                                if (b.getY() + y <= 0)
                                    continue;
                                Block block = b.getWorld().getBlockAt(b.getX() + x, b.getY() + y, b.getZ() + z);
                                if (block.getType() != Material.BEDROCK)
                                    block.setType(Material.AIR);
                            }
                        }
                    }
                }
            }, delayInTicks);
        }
    }
}
