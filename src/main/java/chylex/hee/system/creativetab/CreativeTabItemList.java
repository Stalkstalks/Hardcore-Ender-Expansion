package chylex.hee.system.creativetab;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.item.Item;

public class CreativeTabItemList {

    private final List<Block> blocks = new ArrayList<>();
    private final List<Item> items = new ArrayList<>();

    public CreativeTabItemList addBlocks(Block... blocks) {
        for (Block block : blocks) {
            this.blocks.add(block);
            block.setCreativeTab(ModCreativeTab.tabMain);
        }

        return this;
    }

    public List<Block> getBlocks() {
        return blocks;
    }

    public CreativeTabItemList addItems(Item... items) {
        for (Item item : items) {
            this.items.add(item);
            item.setCreativeTab(ModCreativeTab.tabMain);
        }

        return this;
    }

    public List<Item> getItems() {
        return items;
    }
}
