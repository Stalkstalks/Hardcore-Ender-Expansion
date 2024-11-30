package chylex.hee.world;

import net.minecraft.world.WorldProviderEnd;
import net.minecraft.world.chunk.IChunkProvider;

import chylex.hee.system.savedata.WorldDataHandler;
import chylex.hee.system.savedata.types.DragonSavefile;

public class WorldProviderHardcoreEnd extends WorldProviderEnd {

    @Override
    public IChunkProvider createChunkGenerator() {
        return new ChunkProviderHardcoreEnd(worldObj, worldObj.getSeed());
    }

    @Override
    public long getSeed() {
        return super.getSeed() + WorldDataHandler.<DragonSavefile>get(DragonSavefile.class).getDragonDeathAmount();
    }
}
