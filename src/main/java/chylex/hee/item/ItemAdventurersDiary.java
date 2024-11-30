package chylex.hee.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import chylex.hee.packets.PacketPipeline;
import chylex.hee.packets.client.C04OpenAdventurersDiary;
import chylex.hee.system.savedata.WorldDataHandler;
import chylex.hee.system.savedata.types.LoreSavefile;

public class ItemAdventurersDiary extends Item {

    @Override
    public ItemStack onItemRightClick(ItemStack is, World world, EntityPlayer player) {
        world.playSoundAtEntity(
                player,
                "hardcoreenderexpansion:player.random.pageflip",
                1.5F,
                0.5F * ((player.getRNG().nextFloat() - player.getRNG().nextFloat()) * 0.7F + 1.8F));

        if (!world.isRemote) {
            LoreSavefile file = WorldDataHandler.get(LoreSavefile.class);

            if (is.getItemDamage() == 0) {
                int nextPage = file.unlockNextPage(player);
                if (nextPage > 0) is.setItemDamage(nextPage);
            } else file.unlockPage(player, is.getItemDamage());

            PacketPipeline.sendToPlayer(player, new C04OpenAdventurersDiary(file.getUnlockedPages(player)));
        }

        return is;
    }

    @Override
    public String getItemStackDisplayName(ItemStack is) {
        return super.getItemStackDisplayName(is) + (is.getItemDamage() > 0 ? " (page " + is.getItemDamage() + ")" : "");
    }
}
