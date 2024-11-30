package chylex.hee.render.entity;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderEnderman;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.util.ResourceLocation;

import chylex.hee.mechanics.misc.Baconizer;
import chylex.hee.proxy.ModCommonProxy;
import chylex.hee.render.model.ModelBaconmanHead;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderMobEnderman extends RenderEnderman {

    public RenderMobEnderman() {
        if (ModCommonProxy.hardcoreEnderbacon) {
            ((ModelBiped) mainModel).bipedHead = new ModelBaconmanHead(mainModel, 0, 0);
        }
    }

    @Override
    protected int shouldRenderPass(EntityEnderman enderman, int pass, float partialTickTime) {
        if (pass != 0 || ModCommonProxy.hardcoreEnderbacon) return -1;
        else return super.shouldRenderPass(enderman, pass, partialTickTime);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityEnderman enderman) {
        return Baconizer.mobTexture(this, super.getEntityTexture(enderman));
    }
}
