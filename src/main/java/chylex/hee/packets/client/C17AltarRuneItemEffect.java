package chylex.hee.packets.client;

import java.util.Random;

import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import chylex.hee.HardcoreEnderExpansion;
import chylex.hee.mechanics.essence.EssenceType;
import chylex.hee.mechanics.essence.RuneItem;
import chylex.hee.packets.AbstractClientPacket;
import chylex.hee.system.util.BlockPosM;
import chylex.hee.system.util.DragonUtil;
import chylex.hee.tileentity.TileEntityEssenceAltar;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;

public class C17AltarRuneItemEffect extends AbstractClientPacket {

    private BlockPosM pos;
    private byte essenceId, runeArrayIndex;

    public C17AltarRuneItemEffect() {}

    public C17AltarRuneItemEffect(TileEntityEssenceAltar altar, byte runeArrayIndex) {
        this.pos = new BlockPosM(altar.xCoord, altar.yCoord, altar.zCoord);
        this.essenceId = altar.getEssenceType().id;
        this.runeArrayIndex = runeArrayIndex;
    }

    @Override
    public void write(ByteBuf buffer) {
        buffer.writeLong(pos.toLong()).writeByte(essenceId).writeByte(runeArrayIndex);
    }

    @Override
    public void read(ByteBuf buffer) {
        pos = new BlockPosM(buffer.readLong());
        essenceId = buffer.readByte();
        runeArrayIndex = buffer.readByte();
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected void handle(EntityClientPlayerMP player) {
        RuneItem runeItem = EssenceType.getById(essenceId).itemsNeeded[runeArrayIndex];
        ItemStack is = runeItem.getShowcaseItem();
        World world = player.worldObj;
        Random rand = world.rand;

        for (int a = 0; a < 42; a++) {
            double[] vec = DragonUtil.getNormalizedVector(rand.nextDouble() - 0.5D, rand.nextDouble() - 0.5D);
            HardcoreEnderExpansion.fx.item(
                    is,
                    world,
                    pos.x + 0.5D + rand.nextDouble() * 0.4D - 0.2D,
                    pos.y + 1.1D + rand.nextDouble() * 0.4D,
                    pos.z + 0.5D + rand.nextDouble() * 0.2D - 0.1D,
                    vec[0] * 0.1D,
                    0.1D,
                    vec[1] * 0.1D);
        }

        world.playSound(pos.x, pos.y, pos.z, runeItem.soundEffect, 1F, 0.9F + rand.nextFloat() * 0.1F, false);
    }
}
