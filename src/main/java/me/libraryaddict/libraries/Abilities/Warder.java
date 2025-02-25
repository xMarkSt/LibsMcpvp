package me.libraryaddict.libraries.Abilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.libraryaddict.Hungergames.Hungergames;
import me.libraryaddict.Hungergames.Events.TimeSecondEvent;
import me.libraryaddict.Hungergames.Interfaces.Disableable;
import me.libraryaddict.Hungergames.Types.AbilityListener;
import me.libraryaddict.Hungergames.Types.HungergamesApi;

public class Warder extends AbilityListener implements Disableable {
    private transient HashMap<ItemStack, Integer> cooldownItems = new HashMap<ItemStack, Integer>();
    public String cooldownMessage = ChatColor.BLUE + "You can use this again in %s seconds";
    public int cooldownSeconds = 60;
    public int fieldExistsFor = 5;
    private transient Hungergames hg = HungergamesApi.getHungergames();
    private ArrayList<Material> ignoreBlockTypes = new ArrayList<>();
    public String item = Material.BOOK.name();
    private Material itemMat = Material.matchMaterial(item);
    private transient HashMap<List<Block>, Integer> portals = new HashMap<List<Block>, Integer>();
    public int portalsHeight = 2;
    public int portalsWidth = 3;
    public String[] potionEffects = new String[] { "SPEED 100 0", "DAMAGE_RESISTANCE 600 0" };
    public String warderBookName = ChatColor.WHITE + "Tome of Protection";
    public int warmupTicks = 10;

    public Warder() {
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
        ignoreBlockTypes.add(Material.LEGACY_PORTAL);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && isSpecialItem(event.getItem(), warderBookName)
                && event.getItem().getType() == itemMat && hasAbility(event.getPlayer())) {
            if (cooldownItems.containsKey(event.getItem())) {
                event.getPlayer()
                        .sendMessage(String.format(cooldownMessage, cooldownItems.get(event.getItem()) - hg.currentTime));
            } else {
                boolean facing = (int) Math.abs(Math.round(event.getPlayer().getLocation().getYaw() / 90F)) % 2 == 1;
                Location starter = event.getClickedBlock().getRelative(event.getBlockFace()).getLocation();
                final List<Block> portalBlocks = new ArrayList<Block>();
                for (int y = 0; y < portalsHeight; y++) {
                    boolean[] blocked = new boolean[2];
                    for (int xx = 0; xx <= portalsWidth; xx++) {
                        for (int i = 0; i <= 1; i++) {
                            if (blocked[i]) {
                                continue;
                            }
                            int x = (i == 0 ? xx : -xx);
                            Block b = starter.clone().add(facing ? 0 : x, y, facing ? x : 0).getBlock();
                            if (!ignoreBlockTypes.contains(b.getType())) {
                                blocked[i] = true;
                                continue;
                            }
                            portalBlocks.add(b);
                        }
                    }
                }
                Bukkit.getScheduler().scheduleSyncDelayedTask(hg, new Runnable() {
                    public void run() {
                        for (Block b : portalBlocks)
                            if (ignoreBlockTypes.contains(b.getType())) {
                                b.setType(Material.LEGACY_PORTAL, false);
                            }
                    }
                }, warmupTicks);
                Bukkit.getScheduler().scheduleSyncDelayedTask(hg, new Runnable() {
                    public void run() {
                        portals.remove(portalBlocks);
                        for (Block b : portalBlocks)
                            if (b.getType() == Material.LEGACY_PORTAL)
                                b.setType(Material.AIR);
                    }
                }, warmupTicks + (fieldExistsFor * 20));
                portals.put(portalBlocks, hg.currentTime + fieldExistsFor + (warmupTicks / 20));
                cooldownItems.put(event.getItem(), hg.currentTime + cooldownSeconds);
            }
        }
    }

    @EventHandler
    public void onPortalEnter(EntityPortalEnterEvent event) {
        if (event.getEntity() instanceof Player)
            for (List<Block> blocks : portals.keySet())
                if (blocks.contains(event.getLocation().getBlock())) {
                    for (String string : potionEffects) {
                        String[] effect = string.split(" ");
                        PotionEffect potionEffect = new PotionEffect(PotionEffectType.getByName(effect[0].toUpperCase()),
                                Integer.parseInt(effect[1]), Integer.parseInt(effect[2]));
                        ((Player) event.getEntity()).addPotionEffect(potionEffect);
                    }
                    break;
                }
    }

    @EventHandler
    public void onTimeSecond(TimeSecondEvent event) {
        if (cooldownItems.containsValue(hg.currentTime)) {
            for (ItemStack item : cooldownItems.keySet()) {
                if (cooldownItems.get(item) == hg.currentTime) {
                    cooldownItems.remove(item);
                }
            }
        }
    }
}
