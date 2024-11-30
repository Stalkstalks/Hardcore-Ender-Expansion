package chylex.hee.packets.client;

import net.minecraft.client.entity.EntityClientPlayerMP;

import chylex.hee.packets.AbstractClientPacket;
import chylex.hee.system.util.DragonUtil;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;

public class C01ParticleEndPortalCreation extends AbstractClientPacket {

    private int x, z;

    public C01ParticleEndPortalCreation() {}

    public C01ParticleEndPortalCreation(int x, int z) {
        this.x = x;
        this.z = z;
    }

    @Override
    public void write(ByteBuf buffer) {
        buffer.writeShort(x).writeShort(z);
    }

    @Override
    public void read(ByteBuf buffer) {
        this.x = buffer.readShort();
        this.z = buffer.readShort();
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected void handle(EntityClientPlayerMP player) {
        DragonUtil.portalEffectX = x;
        DragonUtil.portalEffectZ = z;
    }
}
