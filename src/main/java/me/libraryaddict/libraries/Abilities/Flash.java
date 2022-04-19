package me.libraryaddict.libraries.Abilities;

import java.util.HashSet;
import java.util.List;

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

public class Flash extends AbilityListener implements Disableable {

    public boolean addMoreCooldownForLargeDistances = true;
    public String cooldownMessage = ChatColor.BLUE + "You can use this again in %s seconds!";
    public String flashItemName = ChatColor.WHITE + "Flash";
    private Material flashOffItemMat = Material.TORCH;
    private Material flashOnItemMat = Material.REDSTONE_TORCH;
    public boolean giveWeakness = true;
    private HashSet<Material> ignoreBlockTypes = new HashSet<>();
    public int maxTeleportDistance = 200;
    public int normalCooldown = 30;

    public Flash() {
        ignoreBlockTypes.add(Material.AIR);
        ignoreBlockTypes.add(Material.WATER);
        ignoreBlockTypes.add(Material.LAVA);
        ignoreBlockTypes.add(Material.SNOW);
        ignoreBlockTypes.add(Material.TALL_GRASS);
        ignoreBlockTypes.add(Material.SEAGRASS);
        ignoreBlockTypes.add(Material.RED_MUSHROOM);
        ignoreBlockTypes.add(Material.DANDELION);
        ignoreBlockTypes.add(Material.POPPY);
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
                    if (item.getType() != flashOffItemMat) {
                        item.setType(flashOffItemMat);
                    }
                } else {
                    List<Block> b = p.getLastTwoTargetBlocks(ignoreBlockTypes, maxTeleportDistance);
                    if (b.size() > 1 && b.get(1).getType() != Material.AIR) {
                        double dist = p.getLocation().distance(b.get(0).getLocation());
                        if (dist > 2) {
                            Location loc = b.get(0).getLocation().clone().add(0.5, 0.5, 0.5);
                            item.setType(flashOffItemMat);
                            int hisCooldown = normalCooldown;
                            if (addMoreCooldownForLargeDistances && (dist / 2) > 30)
                                hisCooldown += (int) (dist / 2);
                            setCooldown(p, hisCooldown + HungergamesApi.getHungergames().currentTime);
                            Location pLoc = p.getLocation();
                            loc.setPitch(pLoc.getPitch());
                            loc.setYaw(pLoc.getYaw());
                            p.eject();
                            p.teleport(loc);
                            pLoc.getWorld().playSound(pLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1.2F);
                            pLoc.getWorld().playSound(loc, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1.2F);
                            pLoc.getWorld().spawnParticle(Particle.PORTAL, pLoc, 32);
                            pLoc.getWorld().spawnParticle(Particle.PORTAL, loc, 32);
                            if (giveWeakness)
                                p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, (int) ((dist / 2) * 20), 1));
                            pLoc.getWorld().strikeLightningEffect(loc);
                            return;
                        }
                    }
                    if (item.getType() != flashOnItemMat) {
                        item.setType(flashOnItemMat);
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
                        i.setType(flashOnItemMat);
                    }
                }
            }
            if (isSpecialItem(p.getItemOnCursor(), flashItemName)) {
                p.getItemOnCursor().setType(flashOnItemMat);
            }
        }
    }

    private void setCooldown(Player p, int newCooldown) {
        p.setMetadata("FlashCooldown", new FixedMetadataValue(HungergamesApi.getHungergames(), newCooldown));
    }
}
