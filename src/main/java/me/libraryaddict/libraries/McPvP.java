package me.libraryaddict.libraries;

import java.io.File;

import me.libraryaddict.Hungergames.Hungergames;
import me.libraryaddict.Hungergames.Listeners.LibsFeastManager;
import me.libraryaddict.Hungergames.Types.HungergamesApi;
import me.libraryaddict.Hungergames.Types.Kit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class McPvP extends JavaPlugin implements Listener {
    private String currentVersion;
    private String latestVersion;

    @Override
    public void onEnable() {
        // Check if dependency loaded
        Plugin hg = getServer().getPluginManager().getPlugin("LibsHungergames");
        if (!(hg instanceof Hungergames)) {
            System.out.print("[Libs MCPVP] Failed to find dependency LibsHungergames. This plugin will not work!");
        }
        saveDefaultConfig();
        File file = new File(getDataFolder().toString() + "/kits.yml");
        ConfigurationSection config;
        if (!file.exists()) {
            saveResource("kits.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(file);
        HungergamesApi.getAbilityManager().initializeAllAbilitiesInPackage(this, "me.libraryaddict.libraries.Abilities");
        if (config.contains("Kits")) {
            for (String string : config.getConfigurationSection("Kits").getKeys(false)) {
                if (config.contains("BadKits") && config.getStringList("BadKits").contains(string))
                    continue;
                Kit kit = HungergamesApi.getKitManager().parseKit(config.getConfigurationSection("Kits." + string));
                HungergamesApi.getKitManager().addKit(kit);
            }
        }
        currentVersion = "v" + Bukkit.getPluginManager().getPlugin("LibsMcpvp").getDescription().getVersion();
        if (HungergamesApi.getHungergames().getConfig().getBoolean("CheckUpdates")) {
            Bukkit.getScheduler().scheduleAsyncDelayedTask(this, new Runnable() {
                public void run() {
                    try {
                        UpdateChecker updateChecker = new UpdateChecker();
                        updateChecker.checkUpdate(currentVersion);
                        latestVersion = updateChecker.getLatestVersion();
                        if (latestVersion != null) {
                            latestVersion = "v" + latestVersion;
                            for (Player p : Bukkit.getOnlinePlayers())
                                if (p.hasPermission("hungergames.update"))
                                    p.sendMessage(String.format(ChatColor.RED + "[Libs MCPVP] " + ChatColor.DARK_RED
                                            + "There is a update ready to be downloaded! You are using " + ChatColor.RED + "%s"
                                            + ChatColor.DARK_RED + ", the new version is " + ChatColor.RED + "%s"
                                            + ChatColor.DARK_RED + "!", currentVersion, latestVersion));
                        }
                    } catch (Exception ex) {
                        System.out.print(String.format("[Libs MCPVP] Failed to check for update: %s", ex.getMessage()));
                    }
                }
            });
        }
        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getPluginManager().registerEvents(new McpvpListener(this), this);
        if (getConfig().getBoolean("McpvpFeast")) {
            LibsFeastManager.setFeastManager(new LibsMcpvpFeastManager());
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        if (latestVersion != null && p.hasPermission("hungergames.update"))
            p.sendMessage(String.format(ChatColor.GOLD + "[Libs MCPVP] " + ChatColor.DARK_GREEN
                    + "There is a update ready to be downloaded! You are using " + ChatColor.GREEN + "%s" + ChatColor.DARK_GREEN
                    + ", the new version is " + ChatColor.GREEN + "%s" + ChatColor.DARK_GREEN + "!", currentVersion,
                    latestVersion));
    }
}
