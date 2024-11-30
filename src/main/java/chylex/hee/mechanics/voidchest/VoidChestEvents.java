package chylex.hee.mechanics.voidchest;

import java.util.Iterator;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;

import com.google.common.collect.Lists;

import chylex.hee.entity.technical.EntityTechnicalVoidChest;
import chylex.hee.packets.PacketPipeline;
import chylex.hee.packets.client.C09SimpleEvent;
import chylex.hee.packets.client.C09SimpleEvent.EventType;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public final class VoidChestEvents {

    public static void register() {
        MinecraftForge.EVENT_BUS.register(new VoidChestEvents());
        PlayerVoidChest.register();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPlayerDrops(PlayerDropsEvent e) {
        if (!e.entity.worldObj.isRemote && e.entity.dimension == 1 && !e.drops.isEmpty()) {
            boolean triggered = false;

            if (e.entity.posY <= -8D) {
                InventoryVoidChest voidChest = PlayerVoidChest.getInventory(e.entityPlayer);

                for (Iterator<EntityItem> iter = e.drops.iterator(); iter.hasNext();) {
                    EntityItem entity = iter.next();

                    if (entity.posY <= -8D) {
                        voidChest.putItem(entity.getEntityItem());
                        iter.remove();

                        if (!triggered) {
                            triggered = true;
                            PacketPipeline.sendToPlayer(e.entityPlayer, new C09SimpleEvent(EventType.SHOW_VOID_CHEST));
                        }
                    }
                }
            }

            if (!e.drops.isEmpty()) {
                for (EntityItem entity : e.drops) e.entity.worldObj.spawnEntityInWorld(entity);
                e.entity.worldObj.spawnEntityInWorld(
                        new EntityTechnicalVoidChest(
                                e.entity.worldObj,
                                e.entity.posX,
                                e.entity.posY,
                                e.entity.posZ,
                                (EntityPlayerMP) e.entityPlayer,
                                e.drops,
                                true));
                e.drops.clear();
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onItemToss(ItemTossEvent e) {
        if (!e.entity.worldObj.isRemote && e.entity.dimension == 1) {
            if (e.entity.posY <= -8D) {
                PlayerVoidChest.getInventory(e.player).putItem(e.entityItem.getEntityItem());
                e.setCanceled(true);
            } else {
                e.entity.worldObj.spawnEntityInWorld(
                        new EntityTechnicalVoidChest(
                                e.entity.worldObj,
                                e.entity.posX,
                                e.entity.posY,
                                e.entity.posZ,
                                (EntityPlayerMP) e.player,
                                Lists.newArrayList(e.entityItem),
                                false));
            }
        }
    }

    private VoidChestEvents() {}
}
