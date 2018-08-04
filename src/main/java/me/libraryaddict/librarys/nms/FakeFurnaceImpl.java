package me.libraryaddict.librarys.nms;

import lombok.SneakyThrows;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/* package */ class FakeFurnaceImpl extends TileEntityFurnace implements FakeFurnace {
    private double burnSpeed;
    private int lastID;
    // To access the chests
    public int link;
    private double meltSpeed;
    /*
     * I'm internally using "myCookTime" to not lose any precision, but for
     * displaying the progress I still have to use "cookTime"
     */
    private double myCookTime;
    private int burnTime;
    private int cookTime;

    @SneakyThrows(IllegalAccessException.class) // Shouldn't happen
    public FakeFurnaceImpl() {
        link = 0;
        burnSpeed = 1.0D;
        meltSpeed = 1.0D;
        myCookTime = 0.0D;
        COOK_TIME_FIELD.setInt(this, 0);
        BURN_TIME_FIELD.setInt(this, 0);
        TICKS_FOR_CURRENT_FUEL_FIELD.setInt(this, 0);
        lastID = 0;
    }

    private static final Field COOK_TIME_FIELD, BURN_TIME_FIELD, TICKS_FOR_CURRENT_FUEL_FIELD;

    static {
        try {
            COOK_TIME_FIELD = TileEntityFurnace.class.getDeclaredField("cookTime");
            BURN_TIME_FIELD = TileEntityFurnace.class.getDeclaredField("burnTime");
            TICKS_FOR_CURRENT_FUEL_FIELD = TileEntityFurnace.class.getDeclaredField("ticksForCurrentFuel");
            COOK_TIME_FIELD.setAccessible(true);
            BURN_TIME_FIELD.setAccessible(true);
            TICKS_FOR_CURRENT_FUEL_FIELD.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Can't find a necessary field for the fake furnace!");
        }
    }

    // Read from save

    @Override
    public boolean a(EntityHuman entityhuman) { // canUse
        return true;
    }

    @Override
    public void burn() {
        // Can't burn? Goodbye
        if (!canBurn()) {
            return;
        }
        ItemStack itemstack = getContents().get(0) != null ? RecipesFurnace.getInstance().getResult(getContents().get(0)) : null;
        // Nothing in there? Then put something there.
        if (getContents().get(2) == null) {
            getContents().set(2, itemstack.cloneItemStack());
        }
        // Burn ahead
        else if (getContents().get(2).doMaterialsMatch(itemstack)) {
            getContents().get(2).setCount(getContents().get(2).getCount() + itemstack.getCount());
        }
        // Consume the ingredient item
        Item craftingResult = getContents().get(0).getItem().q();
        if (craftingResult != null) {
            getContents().set(0, new ItemStack(craftingResult));
        } else {
            getContents().get(0).setCount(getContents().get(0).getCount() - 1);
            // Let 0 be null
            if (getContents().get(0).getCount() <= 0) {
                getContents().set(0, null);
            }
        }
    }

    private boolean canBurn() {
        // No ingredient, no recipe
        if (getContents().get(0) == null) {
            return false;
        }
        ItemStack itemstack = RecipesFurnace.getInstance().getResult(getContents().get(0));
        // No recipe, no burning
        if (itemstack == null) {
            return false;
        }
        // Free space? Let's burn!
        else if (getContents().get(2) == null) {
            return true;
        }
        // Materials don't match? Too bad.
        else if (!getContents().get(2).doMaterialsMatch(itemstack)) {
            return false;
        }
        // As long as there is space, we can burn
        else if ((getContents().get(2).getCount() + itemstack.getCount() <= getMaxStackSize())
                && (getContents().get(2).getCount() + itemstack.getCount() <= getContents().get(2).getMaxStackSize())) {
            return true;
        }
        return false;
    }

    private double getBurnSpeed(ItemStack item) {
        if (item == null) {
            return 0.0D;
        }
        // CUSTOM FUEL HERE
        return 1.0D;
    }

    private int getFuelTime(ItemStack item) {
        if (item == null) {
            return 0;
        }
        // CUSTOM FUEL HERE
        // Lava should melt 128 items, not 100
        if (Item.getId(item.getItem()) == org.bukkit.Material.LAVA_BUCKET.getId()) {
            return 25600;
        } else {
            return fuelTime(item);
        }
    }

    public final void c() {
        tick();
    }

    @SneakyThrows(IllegalAccessException.class)
    public void tick() {
        int newID = getContents().get(0) == null ? 0 : Item.getId(getContents().get(0).getItem());
        // Has the item been changed?
        if (newID != lastID) {
            // Then reset the progress!
            myCookTime = 0.0D;
            lastID = newID;
            // And, most important: change the melt speed
            meltSpeed = getContents().get(0) != null ? 1 : 0;
        }
        // So, can we now finally burn?
        if (canBurn() && !isBurning() && (getFuelTime(getContents().get(1)) > 0)) {
            // I have no idea what "ticksForCurrentFuel" is good for, but it
            // works fine like this
            TICKS_FOR_CURRENT_FUEL_FIELD.setInt(this, burnTime = getFuelTime(getContents().get(1)));
            // Before we remove the item: how fast does it burn?
            burnSpeed = getBurnSpeed(getContents().get(1));
            // If it's a container item (lava bucket), we only consume its
            // getContents() (not like evil Notch!)

            // If it's not a container, consume it! Om nom nom nom!
            {
                getContents().get(1).setCount(getContents().get(1).getCount() - 1);
                // Let 0 be null
                if (getContents().get(1).getCount() <= 0) {
                    getContents().set(1, null);
                }
            }
        }
        // Now, burning?
        if (isBurning()) {
            // Then move on
            burnTime--;
            // I'm using a double here because of the custom recipes.
            // The faster this fuel burns and the faster the recipe melts, the
            // faster we're done
            myCookTime += burnSpeed * meltSpeed;
            // Finished burning?
            if (myCookTime >= 200.0D) {
                myCookTime -= 200.0D;
                burn();
            }
        }
        // If it's not burning, we reset the burning progress!
        else {
            myCookTime = 0.0D;
        }
        // And for the display (I'm using floor rather than round to not cause
        // the client to do shit when we not really reached 200):
        cookTime = (int) Math.floor(myCookTime);
    }

    @Override
    public void showTo(Player player) {
        ((CraftPlayer) player).getHandle().openTileEntity(this);
    }

    @Override
    public List<org.bukkit.inventory.ItemStack> getItems() {
        List<org.bukkit.inventory.ItemStack> items = new ArrayList<org.bukkit.inventory.ItemStack>();
        for (ItemStack stack : getContents()) {
            items.add(CraftItemStack.asBukkitCopy(stack));
        }
        return items;
    }
}