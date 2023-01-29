package chylex.hee.packets;

import net.minecraft.entity.player.EntityPlayer;

import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;

public abstract class AbstractPacket {

    public abstract void write(ByteBuf buffer);

    public abstract void read(ByteBuf buffer);

    public abstract void handle(Side side, EntityPlayer player);
}
