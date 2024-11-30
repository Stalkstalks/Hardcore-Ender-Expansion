package chylex.hee.api.message.handlers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.minecraft.item.ItemStack;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Function;

import chylex.hee.api.message.MessageHandler;
import chylex.hee.api.message.MessageRunner;
import chylex.hee.api.message.element.IntValue;
import chylex.hee.api.message.element.ItemPatternValue;
import chylex.hee.api.message.element.SpawnEntryValue;
import chylex.hee.api.message.element.StringValue;
import chylex.hee.api.message.element.WeightedLootValue;
import chylex.hee.api.message.utils.MessageLogger;
import chylex.hee.api.message.utils.RunEvent;
import chylex.hee.entity.block.EntityBlockHomelandCache;
import chylex.hee.system.util.ItemPattern;
import chylex.hee.world.loot.LootItemStack;
import chylex.hee.world.loot.WeightedLootList;
import chylex.hee.world.structure.island.biome.IslandBiomeBase;
import chylex.hee.world.structure.island.biome.IslandBiomeBurningMountains;
import chylex.hee.world.structure.island.biome.IslandBiomeEnchantedIsland;
import chylex.hee.world.structure.island.biome.IslandBiomeInfestedForest;
import chylex.hee.world.structure.island.biome.data.BiomeContentVariation;
import chylex.hee.world.structure.island.biome.feature.forest.ravageddungeon.RavagedDungeonLoot;
import chylex.hee.world.structure.island.biome.feature.island.StructureHiddenCellar;
import chylex.hee.world.structure.island.biome.feature.island.laboratory.LaboratoryContent;
import chylex.hee.world.structure.tower.ComponentTower;
import chylex.hee.world.util.SpawnEntry;

public final class ImcWorldHandlers extends ImcHandler {

    private static final Map<String, WeightedLootList> lootNames = new HashMap<>();
    private static final Map<String, Pair<IslandBiomeBase, BiomeContentVariation>> biomeNames = new HashMap<>();

    static {
        lootNames.put("DungeonTowerChest", ComponentTower.lootTower);
        lootNames.put("DungeonTowerFurnaceFuel", ComponentTower.lootFuel);
        lootNames.put("RavagedDungeonGeneral", RavagedDungeonLoot.lootGeneral);
        lootNames.put("RavagedDungeonUncommon", RavagedDungeonLoot.lootUncommon);
        lootNames.put("RavagedDungeonRare", RavagedDungeonLoot.lootRare);
        lootNames.put("RavagedDungeonFinalRoom", RavagedDungeonLoot.lootEnd);
        lootNames.put("HiddenCellarNormalHomeland", StructureHiddenCellar.normalChestVariation[0]);
        lootNames.put("HiddenCellarRareHomeland", StructureHiddenCellar.rareChestVariation[0]);
        lootNames.put("HiddenCellarNormalLaboratory", StructureHiddenCellar.normalChestVariation[1]);
        lootNames.put("HiddenCellarRareLaboratory", StructureHiddenCellar.rareChestVariation[1]);
        lootNames.put("HomelandCache", EntityBlockHomelandCache.loot);
        lootNames.put("LaboratorySmallChest", LaboratoryContent.smallChestLoot);
        lootNames.put("LaboratoryLargeChest", LaboratoryContent.largeChestLoot);

        biomeNames.put("InfestedForest.Deep", Pair.of(IslandBiomeBase.infestedForest, IslandBiomeInfestedForest.DEEP));
        biomeNames.put(
                "InfestedForest.Ravaged",
                Pair.of(IslandBiomeBase.infestedForest, IslandBiomeInfestedForest.RAVAGED));
        biomeNames.put(
                "BurningMountains.Scorching",
                Pair.of(IslandBiomeBase.burningMountains, IslandBiomeBurningMountains.SCORCHING));
        biomeNames.put(
                "BurningMountains.Mine",
                Pair.of(IslandBiomeBase.burningMountains, IslandBiomeBurningMountains.MINE));
        biomeNames.put(
                "EnchantedIsland.Homeland",
                Pair.of(IslandBiomeBase.enchantedIsland, IslandBiomeEnchantedIsland.HOMELAND));
        biomeNames.put(
                "EnchantedIsland.Laboratory",
                Pair.of(IslandBiomeBase.enchantedIsland, IslandBiomeEnchantedIsland.LABORATORY));
    }

    private static final StringValue lootName = StringValue.function(new Function<String, Boolean>() {

        @Override
        public Boolean apply(String input) {
            return Boolean.valueOf(lootNames.containsKey(input));
        }
    });

    private static final StringValue biomeName = StringValue.function(new Function<String, Boolean>() {

        @Override
        public Boolean apply(String input) {
            return Boolean.valueOf(biomeNames.containsKey(input));
        }
    });

    private static final MessageHandler lootAdd = new MessageHandler() {

        @Override
        public void call(MessageRunner runner) {
            WeightedLootList list = lootNames.get(runner.getString("list"));
            LootItemStack toAdd = runner.<LootItemStack>getValue("item");

            for (LootItemStack item : list) {
                if (item.getItem() == toAdd.getItem()) {
                    MessageLogger.logFail("The item was already in the list.");
                    return;
                }
            }

            list.add(toAdd);
            MessageLogger.logOk("Added 1 item to the list.");
        }
    };

    private static final MessageHandler lootRemove = new MessageHandler() {

        @Override
        public void call(MessageRunner runner) {
            WeightedLootList list = lootNames.get(runner.getString("list"));
            int limit = runner.getInt("limit");

            ItemPattern pattern = runner.<ItemPattern>getValue("search");
            pattern.setDamageValues(ArrayUtils.EMPTY_INT_ARRAY);
            pattern.setNBT(null);
            // reset search

            int size = list.size();

            for (Iterator<LootItemStack> iter = list.iterator(); iter.hasNext();) {
                if (pattern.matches(new ItemStack(iter.next().getItem()))) {
                    iter.remove();
                    if (limit > 0 && --limit == 0) break;
                }
            }

            size = size - list.size();

            if (size == 0) MessageLogger.logWarn("Did not find any items to remove.");
            else MessageLogger.logOk("Removed $0 item(s).", size);
        }
    };

    private static final MessageHandler biomeMobAdd = new MessageHandler() {

        @Override
        public void call(MessageRunner runner) {
            Pair<IslandBiomeBase, BiomeContentVariation> pair = biomeNames.get(runner.getString("biome"));
            pair.getLeft().getSpawnEntries(pair.getRight()).add(runner.<SpawnEntry>getValue("mob"));
            MessageLogger.logOk("Added 1 entry to the list.");
        }
    };

    @Override
    public void register() {
        register("HEE:World:LootAdd", lootAdd, RunEvent.LOADCOMPLETE).addProp("list", lootName)
                .addProp("item", WeightedLootValue.any());

        register("HEE:World:LootRemove", lootRemove, RunEvent.LOADCOMPLETE).addProp("list", lootName)
                .addProp("search", ItemPatternValue.any()).addProp("limit", IntValue.positiveOrZero());

        register("HEE:World:BiomeMobAdd", biomeMobAdd, RunEvent.LOADCOMPLETE).addProp("biome", biomeName)
                .addProp("mob", SpawnEntryValue.any());
    }
}
