package chylex.hee.mechanics.orb;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import chylex.hee.system.util.ItemPattern;
import cpw.mods.fml.common.registry.GameRegistry;

public final class OrbAcquirableItems {

    public static final WeightedItemList idList = new WeightedItemList();
    public static final List<ItemPattern> blacklist = new ArrayList<>();

    public static void initialize(boolean firstTime) {
        /* yeet */
    }

    public static WeightedItem getRandomItem(World world, Random rand) {
        /* Yeet */
        return null;
    }

    private static String getModID(ItemStack is) {
        try {
            return GameRegistry.findUniqueIdentifierFor(is.getItem()).modId;
        } catch (NullPointerException e) {
            return "<null>";
        }
    }

    private OrbAcquirableItems() {}
}
