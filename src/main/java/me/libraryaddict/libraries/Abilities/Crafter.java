package me.libraryaddict.libraries.Abilities;

import me.libraryaddict.Hungergames.Events.PlayerKilledEvent;
import me.libraryaddict.Hungergames.Interfaces.Disableable;
import me.libraryaddict.Hungergames.Types.AbilityListener;
import me.libraryaddict.Hungergames.Types.HungergamesApi;
import me.libraryaddict.libraries.nms.FakeFurnace;
import me.libraryaddict.libraries.nms.NMS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class Crafter extends AbilityListener implements Disableable {

    public String craftingStarItem = Material.NETHER_STAR.name();
    private Material craftingStarItemMat = Material.matchMaterial(craftingStarItem);
    public String craftingStarItemName = ChatColor.WHITE + "Crafting Star";
    public String furnacePowderItem = Material.BLAZE_POWDER.name();
    private Material furnacePowderItemMat = Material.matchMaterial(furnacePowderItem);
    public String furnacePowderItemName = ChatColor.WHITE + "Furnace Powder";
    private transient Map<ItemStack, FakeFurnace> furnaces = new HashMap<ItemStack, FakeFurnace>();

    public Crafter() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(
                HungergamesApi.getHungergames(),
                () -> furnaces.values().forEach(FakeFurnace::tick),
                1, 1
        );
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if ((event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR)
                && hasAbility(event.getPlayer())) {
            Player p = event.getPlayer();
            if (isSpecialItem(item, craftingStarItemName) && craftingStarItemMat == item.getType()) {
                p.openWorkbench(null, true);
            } else if (isSpecialItem(item, furnacePowderItemName) && furnacePowderItemMat == item.getType()) {
                if (!furnaces.containsKey(item)) {
                    furnaces.put(item, NMS.createFakeFurnace());
                }
                furnaces.get(item).showTo(p);
            }
        }
    }

    @EventHandler
    public void onKilled(PlayerKilledEvent event) {
        Iterator<ItemStack> itel = event.getDrops().iterator();
        List<ItemStack> drops = new ArrayList<ItemStack>();
        while (itel.hasNext()) {
            ItemStack item = itel.next();
            if (item != null && furnaces.containsKey(item)) {
                FakeFurnace furnace = furnaces.remove(item);
                if (furnace != null) {
                    drops.addAll(furnace.getItems().stream()
                            .map(i -> item)
                            .collect(Collectors.toList()));
                }
            }
        }
        event.getDrops().addAll(drops);
    }

}
