package chylex.hee.entity.mob;

import java.util.UUID;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import chylex.hee.entity.GlobalMobData.IIgnoreEnderGoo;
import chylex.hee.entity.fx.FXType;
import chylex.hee.init.ItemList;
import chylex.hee.mechanics.causatum.CausatumMeters;
import chylex.hee.mechanics.causatum.CausatumUtils;
import chylex.hee.mechanics.misc.Baconizer;
import chylex.hee.packets.PacketPipeline;
import chylex.hee.packets.client.C21EffectEntity;
import chylex.hee.proxy.ModCommonProxy;
import chylex.hee.system.util.MathUtil;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class EntityMobEnderGuardian extends EntityMob implements IIgnoreEnderGoo {

    private static final AttributeModifier dashModifier = (new AttributeModifier(
            UUID.fromString("69B01060-4B09-4D5C-A6FA-22BEFB9C2D02"),
            "Guardian dash speed boost",
            1.2D,
            1)).setSaved(false);

    private byte attackTimer, dashCooldown;

    public EntityMobEnderGuardian(World world) {
        super(world);
        setSize(1.5F, 3.2F);
    }

    @Override
    protected Entity findPlayerToAttack() {
        EntityPlayer player = worldObj.getClosestVulnerablePlayerToEntity(this, 3D);
        return player != null && canEntityBeSeen(player) ? player : null;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(ModCommonProxy.opMobs ? 0.7D : 0.65D);
        getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(ModCommonProxy.opMobs ? 100D : 80D);
        getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(ModCommonProxy.opMobs ? 25D : 17D);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();

        if (attackTimer > 0) --attackTimer;

        if (!worldObj.isRemote && !isDead) {
            if (dashCooldown > 0) {
                if (--dashCooldown == 70)
                    getEntityAttribute(SharedMonsterAttributes.movementSpeed).removeModifier(dashModifier);
                else if (dashCooldown > 1 && dashCooldown < 70
                        && ((ModCommonProxy.opMobs && rand.nextInt(3) == 0) || rand.nextInt(5) == 0))
                    --dashCooldown;
            } else if (dashCooldown == 0 && entityToAttack != null
                    && MathUtil.distance(posX - entityToAttack.posX, posZ - entityToAttack.posZ) < 4D
                    && Math.abs(posY - entityToAttack.posY) <= 3) {
                        dashCooldown = 80;
                        getEntityAttribute(SharedMonsterAttributes.movementSpeed).applyModifier(dashModifier);
                        PacketPipeline.sendToAllAround(
                                this,
                                64D,
                                new C21EffectEntity(FXType.Entity.ENDER_GUARDIAN_DASH, this));
                    }
        }
    }

    @Override
    public float getAIMoveSpeed() {
        return (float) getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue() * 0.3F;
    }

    @Override
    public boolean attackEntityAsMob(Entity entity) {
        attackTimer = 8;
        worldObj.setEntityState(this, (byte) 4);

        float damage = (float) this.getEntityAttribute(SharedMonsterAttributes.attackDamage).getAttributeValue();

        if (entity.attackEntityFrom(DamageSource.causeMobDamage(this), damage)) {
            entity.addVelocity(
                    -MathHelper.sin(MathUtil.toRad(rotationYaw)) * 1.7D,
                    0.2D,
                    MathHelper.cos(MathUtil.toRad(rotationYaw)) * 1.7D);
            motionX *= 0.8D;
            motionZ *= 0.8D;

            if (dashCooldown > 70) {
                motionX *= 0.5D;
                motionZ *= 0.5D;
                dashCooldown = 71;
            }

            EnchantmentHelper.func_151385_b(this, entity);
            return true;
        } else return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void handleHealthUpdate(byte eventId) {
        if (eventId == 4) attackTimer = 8;
        else super.handleHealthUpdate(eventId);
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (source == DamageSource.fallingBlock || source == DamageSource.anvil) amount *= 0.8F;
        else if (source.isProjectile()) amount *= 0.5F;
        else if (source.isFireDamage() || source == DamageSource.drown) amount *= 0.2F;
        else if (source == DamageSource.cactus) amount = 0F;

        if (amount >= 0.1F && super.attackEntityFrom(source, amount)) {
            CausatumUtils.increase(source, CausatumMeters.END_MOB_DAMAGE, amount * 0.5F);
            return true;
        } else return false;
    }

    @Override
    protected void dropFewItems(boolean recentlyHit, int looting) {
        int amount = rand.nextInt(2 + looting) - rand.nextInt(2);
        for (int a = 0; a < amount; a++) dropItem(Items.ender_pearl, 1);

        amount = 1 + rand.nextInt(3 + (looting >> 1));
        for (int a = 0; a < amount; a++) dropItem(Item.getItemFromBlock(Blocks.obsidian), 1);

        if (recentlyHit) {
            amount = rand.nextInt(4 - rand.nextInt(3) + (looting >> 1));
            for (int a = 0; a < amount; a++) dropItem(ItemList.obsidian_fragment, 1);
        }
    }

    @Override
    public void addVelocity(double xVelocity, double yVelocity, double zVelocity) {
        double mp = rand.nextInt(5) == 0 ? 0D : 0.3D + rand.nextDouble() * 0.2D;
        super.addVelocity(xVelocity * mp, yVelocity * mp, zVelocity * mp);
    }

    @SideOnly(Side.CLIENT)
    public int getAttackTimerClient() {
        return attackTimer;
    }

    @Override
    protected String getLivingSound() {
        return Baconizer.soundNormal(super.getLivingSound());
    }

    @Override
    protected String getHurtSound() {
        return Baconizer.soundNormal(super.getHurtSound());
    }

    @Override
    protected String getDeathSound() {
        return Baconizer.soundDeath(super.getDeathSound());
    }

    @Override
    public String getCommandSenderName() {
        return hasCustomNameTag() ? getCustomNameTag()
                : StatCollector.translateToLocal(Baconizer.mobName("entity.enderGuardian.name"));
    }
}
