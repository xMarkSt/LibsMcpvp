package me.libraryaddict.libraries.nms;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface FakeFurnace {
    void tick();

    void showTo(Player player);

    List<ItemStack> getItems();
}
