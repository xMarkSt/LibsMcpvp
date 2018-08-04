package me.libraryaddict.librarys.Abilities;

import java.util.*;

import me.libraryaddict.Hungergames.Types.AbilityListener;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.libraryaddict.Hungergames.Events.TimeSecondEvent;
import me.libraryaddict.Hungergames.Interfaces.Disableable;
import me.libraryaddict.Hungergames.Types.HungergamesApi;
import me.libraryaddict.librarys.nms.NMS;

public class Flash extends AbilityListener implements Disableable {

    public boolean addMoreCooldownForLargeDistances = true;
    public String cooldownMessage = ChatColor.BLUE + "You can use this again in %s seconds!";
    public String flashItemName = ChatColor.WHITE + "Flash";
    public int flashOffItemId = Material.TORCH.getId();
    public int flashOnItemId = Material.REDSTONE_TORCH_ON.getId();
    public boolean giveWeakness = true;
    //private HashSet<Byte> ignoreBlockTypes = new HashSet<Byte>();
    private Set<Material> ignoreBlockTypes = new HashSet<Material>();
    public int maxTeleportDistance = 200;
    public int normalCooldown = 30;

    public Flash() {
        ignoreBlockTypes.add(Material.AIR);
        ignoreBlockTypes.add(Material.SNOW);
        ignoreBlockTypes.add(Material.LONG_GRASS);
        ignoreBlockTypes.add(Material.RED_MUSHROOM);
        ignoreBlockTypes.add(Material.RED_ROSE);
        ignoreBlockTypes.add(Material.YELLOW_FLOWER);
        ignoreBlockTypes.add(Material.BROWN_MUSHROOM);
        ignoreBlockTypes.add(Material.SIGN_POST);
        ignoreBlockTypes.add(Material.WALL_SIGN);
        ignoreBlockTypes.add(Material.FIRE);
        ignoreBlockTypes.add(Material.TORCH);
        ignoreBlockTypes.add(Material.REDSTONE_WIRE);
        ignoreBlockTypes.add(Material.REDSTONE_TORCH_OFF);
        ignoreBlockTypes.add(Material.REDSTONE_TORCH_ON);
        ignoreBlockTypes.add(Material.VINE);
        ignoreBlockTypes.add(Material.WATER_LILY);
    }

    public int getCooldown(Player p) {
        return (p.hasMetadata("FlashCooldown") ? p.getMetadata("FlashCooldown").get(0).asInt() : 0);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        Player p = event.getPlayer();
        if (event.getAction().name().contains("RIGHT") && hasAbility(p)) {
            if (isSpecialItem(item, flashItemName)) {
                event.setCancelled(true);
                p.updateInventory();
                if (getCooldown(p) > HungergamesApi.getHungergames().currentTime) {
                    p.sendMessage(String.format(cooldownMessage, getCooldown(p) - HungergamesApi.getHungergames().currentTime));
                    if (item.getTypeId() != flashOffItemId) {
                        item.setTypeId(flashOffItemId);
                    }
                } else {
                    Block b = p.getTargetBlock(ignoreBlockTypes, maxTeleportDistance);
                    if (b.getType() != Material.AIR) {
                        double dist = p.getLocation().distance(b.getLocation());
                        if (dist > 2) {
                            Location loc = b.getLocation().clone().add(0.5, 0.5, 0.5);
                            item.setTypeId(flashOffItemId);
                            int hisCooldown = normalCooldown;
                            if (addMoreCooldownForLargeDistances && (dist / 2) > 30)
                                hisCooldown += (int) (dist / 2);
                            setCooldown(p, hisCooldown + HungergamesApi.getHungergames().currentTime);
                            Location pLoc = p.getLocation();
                            loc.setPitch(pLoc.getPitch());
                            loc.setYaw(pLoc.getYaw());
                            p.eject();
                            p.teleport(loc);
                            pLoc.getWorld().playSound(pLoc, Sound.ENTITY_ENDERMEN_TELEPORT, 1, 1.2F);
                            pLoc.getWorld().playSound(loc, Sound.ENTITY_ENDERMEN_TELEPORT, 1, 1.2F);
                            NMS.showPortalEffect(pLoc);
                            NMS.showPortalEffect(loc);
                            if (giveWeakness)
                                p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, (int) ((dist / 2) * 20), 1), true);
                            pLoc.getWorld().strikeLightningEffect(loc);
                            return;
                        }
                    }
                    if (item.getTypeId() != flashOnItemId) {
                        item.setTypeId(flashOnItemId);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onSecond(TimeSecondEvent event) {
        for (Player p : getMyPlayers()) {
            if (HungergamesApi.getHungergames().currentTime == getCooldown(p)) {
                for (ItemStack i : p.getInventory().getContents()) {
                    if (isSpecialItem(i, flashItemName)) {
                        i.setTypeId(flashOnItemId);
                    }
                }
            }
            if (isSpecialItem(p.getItemOnCursor(), flashItemName)) {
                p.getItemOnCursor().setTypeId(flashOnItemId);
            }
        }
    }

    private void setCooldown(Player p, int newCooldown) {
        p.setMetadata("FlashCooldown", new FixedMetadataValue(HungergamesApi.getHungergames(), newCooldown));
    }
}
