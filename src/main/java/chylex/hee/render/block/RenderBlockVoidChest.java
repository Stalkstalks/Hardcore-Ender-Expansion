package chylex.hee.render.block;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.world.IBlockAccess;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import chylex.hee.proxy.ModCommonProxy;
import chylex.hee.tileentity.TileEntityVoidChest;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;

public class RenderBlockVoidChest implements ISimpleBlockRenderingHandler {

    private final TileEntityVoidChest chestRenderer = new TileEntityVoidChest();

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
        GL11.glRotatef(90F, 0F, 1F, 0F);
        GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
        TileEntityRendererDispatcher.instance.renderTileEntityAt(chestRenderer, 0D, 0D, 0D, 0F);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId,
            RenderBlocks renderer) {
        return true;
    }

    @Override
    public boolean shouldRender3DInInventory(int modelId) {
        return true;
    }

    @Override
    public int getRenderId() {
        return ModCommonProxy.renderIdVoidChest;
    }
}
