package chylex.hee.world.feature;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;
import chylex.hee.block.BlockList;
import chylex.hee.system.logging.Log;
import chylex.hee.system.util.MathUtil;
import chylex.hee.world.feature.blobs.old.CavePopulator;
import chylex.hee.world.feature.blobs.old.FlowerPopulator;
import chylex.hee.world.feature.blobs.old.LakePopulator;
import chylex.hee.world.feature.blobs.old.ObsidianSpikePopulator;
import chylex.hee.world.feature.blobs.old.OrePopulator;
import chylex.hee.world.feature.blobs.old.Populator;
import chylex.hee.world.util.BlockLocation;
import chylex.hee.world.util.WorldGeneratorBlockList;

public class WorldGenBlobOld extends WorldGenerator{
	@Override
	public boolean generate(World world, Random rand, int x, int y, int z){
		if (world.getBlock(x-8,y,z) != Blocks.air ||
			world.getBlock(x+8,y,z) != Blocks.air ||
			world.getBlock(x,y,z-8) != Blocks.air ||
			world.getBlock(x,y,z+8) != Blocks.air ||
			world.getBlock(x,y-8,z) != Blocks.air ||
			world.getBlock(x,y+8,z) != Blocks.air)return false;

		float rad = rand.nextFloat()*0.8F+rand.nextFloat()*1.9F+1.95F;
		
		canGenerate = true;
		createBlob(blocks,rand,x,y,z,rad,0);
		if (!canGenerate)return false;
		
		return true;
	}
	
	private static final float twoPI = (float)(Math.PI*2D);
	
	private void createBlob(WorldGeneratorBlockList blocks, Random random, int x, int y, int z, float rad, int iteration){
		BlockLocation blockLoc;
		
		double xx, yy, zz;
		for(xx = x-rad; xx <= x+rad; xx++){
			for(yy = y-rad; yy <= y+rad; yy++){
				for(zz = z-rad; zz <= z+rad; zz++){
					if (Math.sqrt(MathUtil.square(xx-x)+MathUtil.square(yy-y)+MathUtil.square(zz-z)) < rad){
						blockLoc = new BlockLocation((int)Math.floor(xx),(int)Math.floor(yy),(int)Math.floor(zz));
						
						if (Math.abs(genCenterX-blockLoc.x) < 15 && Math.abs(genCenterZ-blockLoc.z) < 15)blocks.add(blockLoc);
						else{
							canGenerate = false;
							return;
						}
					}
				}
			}
		}
		
		if (iteration < 4 && random.nextInt(9-iteration*2) > 1){
			for(int a = 0; a < random.nextInt(3-(iteration>>1))+1; a++){
				float a1 = random.nextFloat()*twoPI, a2 = random.nextFloat()*twoPI;
				float len = (rad*0.4F)+(random.nextFloat()*0.65F*rad);
				createBlob(blocks,random,
						   (int)Math.floor(x+MathHelper.cos(a1)*len),
						   (int)Math.floor(y+MathHelper.cos(a2)*len),
						   (int)Math.floor(z+MathHelper.sin(a1)*len),rad*(1F+random.nextFloat()*0.4F-0.2F),iteration+1);
			}
		}
	}
}
