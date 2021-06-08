package me.libraryaddict.librarys.Abilities;

import java.util.HashMap;
import java.util.Random;

import me.libraryaddict.Hungergames.Interfaces.Disableable;
import me.libraryaddict.Hungergames.Types.AbilityListener;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class Monk extends AbilityListener implements Disableable {

    public int cooldown = 15;
    public String monkCooldownMessage = ChatColor.BLUE + "You may monk them again in %s seconds!";
    public String monkedMessage = ChatColor.BLUE + "Monked!";
    public String monkItem = Material.BLAZE_ROD.name();
    private Material monkItemMat = Material.matchMaterial(monkItem);
    public String monkItemName = ChatColor.WHITE + "Monk Staff";
    private transient HashMap<ItemStack, Long> monkStaff = new HashMap<ItemStack, Long>();
    public boolean sendThroughInventory = true;

    @EventHandler
    public void onRightClick(PlayerInteractEntityEvent event) {
        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
        if (event.getRightClicked() instanceof Player && isSpecialItem(item, monkItemName) && item.getType() == monkItemMat
                && hasAbility(event.getPlayer())) {
            long lastUsed = 0;
            if (monkStaff.containsKey(item))
                lastUsed = monkStaff.get(item);
            if (lastUsed + (1000 * cooldown) > System.currentTimeMillis()) {
                event.getPlayer().sendMessage(
                        String.format(monkCooldownMessage,
                                (-((System.currentTimeMillis() - (lastUsed + (1000 * cooldown))) / 1000))));
            } else {
                PlayerInventory inv = ((Player) event.getRightClicked()).getInventory();
                int slot = new Random().nextInt(sendThroughInventory ? 36 : 9);
                ItemStack replaced = inv.getItemInMainHand();
                if (replaced == null)
                    replaced = new ItemStack(Material.AIR);
                ItemStack replacer = inv.getItem(slot);
                if (replacer == null)
                    replacer = new ItemStack(Material.AIR);
                inv.setItemInMainHand(replacer);
                inv.setItem(slot, replaced);
                monkStaff.put(item, System.currentTimeMillis());
                event.getPlayer().sendMessage(monkedMessage);
            }
        }
    }

}
