package me.libraryaddict.libraries.Abilities;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import me.libraryaddict.Hungergames.Types.AbilityListener;
import me.libraryaddict.Hungergames.Types.HungergamesApi;

public class Trapper extends AbilityListener {
    public boolean immuneToOwnTraps = false;
    public String trapperItem = Material.STRING.name();
    private Material trapperItemMat = Material.matchMaterial(trapperItem);
    public String trapperItemName = "Trapper's String";

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Block b = event.getTo().getBlock();
        if (b.getType() == Material.TRIPWIRE && b.hasMetadata("Trapper")
                && HungergamesApi.getPlayerManager().getGamer(event.getPlayer()).isAlive()) {
            if (immuneToOwnTraps && b.getMetadata("Trapper").get(0).asString().equals(event.getPlayer().getName()))
                return;
            b.setType(Material.LEGACY_WEB);
            b.removeMetadata("Trapper", HungergamesApi.getHungergames());
            b.getWorld().playSound(b.getLocation().clone(), Sound.UI_BUTTON_CLICK, 1, 10);
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (isSpecialItem(item, trapperItemName) && item.getType() == trapperItemMat) {
            event.getBlock().setMetadata("Trapper",
                    new FixedMetadataValue(HungergamesApi.getHungergames(), event.getPlayer().getName()));
        }
    }

}
