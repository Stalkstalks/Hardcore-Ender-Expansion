package chylex.hee.item;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Facing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import chylex.hee.entity.boss.EntityBossEnderDemon;
import chylex.hee.entity.boss.EntityMiniBossEnderEye;
import chylex.hee.entity.boss.EntityMiniBossFireFiend;
import chylex.hee.entity.mob.EntityMobAngryEnderman;
import chylex.hee.entity.mob.EntityMobBabyEnderman;
import chylex.hee.entity.mob.EntityMobEnderGuardian;
import chylex.hee.entity.mob.EntityMobEndermage;
import chylex.hee.entity.mob.EntityMobFireGolem;
import chylex.hee.entity.mob.EntityMobHauntedMiner;
import chylex.hee.entity.mob.EntityMobHomelandEnderman;
import chylex.hee.entity.mob.EntityMobInfestedBat;
import chylex.hee.entity.mob.EntityMobLouse;
import chylex.hee.entity.mob.EntityMobParalyzedEnderman;
import chylex.hee.entity.mob.EntityMobScorchingLens;
import chylex.hee.entity.mob.EntityMobVampiricBat;
import chylex.hee.system.util.BlockPosM;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemSpawnEggs extends ItemMonsterPlacer {

    private static final EggData[] eggTypes = new EggData[] {
            /* 0 */ new EggData(
                    "angryEnderman",
                    EntityMobAngryEnderman.class,
                    new int[] { 22, 22, 22 },
                    new int[] { 87, 45, 45 }),
            /* 1 */ new EggData(
                    "babyEnderman",
                    EntityMobBabyEnderman.class,
                    new int[] { 22, 22, 22 },
                    new int[] { 91, 67, 124 }),
            /* 2 */ new EggData(
                    "enderGuardian",
                    EntityMobEnderGuardian.class,
                    new int[] { 22, 22, 22 },
                    new int[] { 68, 45, 87 }),
            /* 3 */ new EggData(
                    "vampireBat",
                    EntityMobVampiricBat.class,
                    new int[] { 76, 62, 48 },
                    new int[] { 70, 35, 71 }),
            /* 4 */ new EggData(
                    "infestedBat",
                    EntityMobInfestedBat.class,
                    new int[] { 76, 62, 48 },
                    new int[] { 129, 48, 39 }),
            /* 5 */ new EggData(
                    "fireGolem",
                    EntityMobFireGolem.class,
                    new int[] { 68, 16, 0 },
                    new int[] { 210, 142, 50 }),
            /* 6 */ new EggData(
                    "scorchingLens",
                    EntityMobScorchingLens.class,
                    new int[] { 255, 112, 0 },
                    new int[] { 253, 9, 8 }),
            /* 7 */ new EggData(
                    "enderEye",
                    EntityMiniBossEnderEye.class,
                    new int[] { 22, 22, 22 },
                    new int[] { 255, 255, 255 }),
            /* 8 */ new EggData(
                    "fireFiend",
                    EntityMiniBossFireFiend.class,
                    new int[] { 68, 16, 0 },
                    new int[] { 33, 0, 0 }),
            /* 9 */ new EggData(
                    "enderDemon",
                    EntityBossEnderDemon.class,
                    new int[] { 22, 22, 22 },
                    new int[] { 86, 21, 111 }),
            /* 10 */ new EggData(
                    "brainlessEnderman",
                    EntityMobParalyzedEnderman.class,
                    new int[] { 22, 22, 22 },
                    new int[] { 190, 190, 190 }),
            /* 11 */ new EggData("louse", EntityMobLouse.class, new int[] { 45, 45, 45 }, new int[] { 80, 0, 140 }),
            /* 12 */ new EggData(
                    "hauntedMiner",
                    EntityMobHauntedMiner.class,
                    new int[] { 48, 23, 23 },
                    new int[] { 170, 72, 37 }),
            /* 13 */ new EggData(
                    "homelandEnderman",
                    EntityMobHomelandEnderman.class,
                    new int[] { 22, 22, 22 },
                    new int[] { 199, 44, 44 }),
            /* 14 */ new EggData(
                    "endermage",
                    EntityMobEndermage.class,
                    new int[] { 22, 22, 22 },
                    new int[] { 217, 210, 84 }) };

    private static EggData getEggData(ItemStack is) {
        int damage = is.getItemDamage();
        return damage >= 0 && damage < eggTypes.length ? eggTypes[damage] : null;
    }

    public static int getDamageForMob(Class<? extends EntityLiving> mobClass) {
        for (int damage = 0; damage < eggTypes.length; damage++) {
            if (eggTypes[damage].entityClass == mobClass) return damage;
        }

        return -1;
    }

    public static Class<? extends EntityLiving> getMobFromDamage(int damage) {
        return damage >= 0 && damage < eggTypes.length ? eggTypes[damage].entityClass : null;
    }

    public static String getMobName(Class<?> mobClass) {
        for (EggData eggData : eggTypes) {
            if (eggData.entityClass == mobClass)
                return StatCollector.translateToLocal("entity." + eggData.entityName + ".name");
        }

        return "Unknown Mob";
    }

    public ItemSpawnEggs() {
        setHasSubtypes(true);
    }

    @Override
    public String getItemStackDisplayName(ItemStack is) {
        String s = StatCollector.translateToLocal(getUnlocalizedName() + ".name").trim();

        EggData egg = getEggData(is);
        if (egg != null) s += " " + StatCollector.translateToLocal("entity." + egg.entityName + ".name");

        return s;
    }

    @Override
    public boolean onItemUse(ItemStack is, EntityPlayer player, World world, int x, int y, int z, int side, float hitX,
            float hitY, float hitZ) {
        if (world.isRemote) return true;

        Block block = BlockPosM.tmp(x, y, z).getBlock(world);
        x += Facing.offsetsXForSide[side];
        y += Facing.offsetsYForSide[side];
        z += Facing.offsetsZForSide[side];

        EggData egg = getEggData(is);

        if (egg != null) {
            egg.spawnMob(
                    world,
                    x + 0.5D,
                    y + (side == 1 && block != null && block.getRenderType() == 11 ? 0.5D : 0D),
                    z + 0.5D,
                    is);

            if (!player.capabilities.isCreativeMode) --is.stackSize;
        }

        return true;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack is, World world, EntityPlayer player) {
        if (world.isRemote) return is;

        MovingObjectPosition mop = getMovingObjectPositionFromPlayer(world, player, true);

        if (mop != null && mop.typeOfHit == MovingObjectType.BLOCK) {
            int x = mop.blockX, y = mop.blockY, z = mop.blockZ;

            if (!world.canMineBlock(player, x, y, z) || !player.canPlayerEdit(x, y, z, mop.sideHit, is)) return is;

            if (BlockPosM.tmp(x, y, z).getMaterial(world) == Material.water) {
                EggData egg = getEggData(is);

                if (egg != null) {
                    egg.spawnMob(world, x, y, z, is);
                    if (!player.capabilities.isCreativeMode) --is.stackSize;
                }
            }
        }

        return is;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getColorFromItemStack(ItemStack is, int pass) {
        EggData egg = getEggData(is);
        return egg != null ? (pass == 0 ? egg.primaryColor : egg.secondaryColor) : 16777215;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item item, CreativeTabs tab, List list) {
        for (int a = 0; a < eggTypes.length; a++) list.add(new ItemStack(item, 1, a));
    }

    static class EggData {

        final String entityName;
        final Class<? extends EntityLiving> entityClass;
        final int primaryColor, secondaryColor;

        EggData(String entityName, Class<? extends EntityLiving> entityClass, int[] rgbPrimaryColor,
                int[] rgbSecondaryColor) {
            this.entityName = entityName;
            this.entityClass = entityClass;
            this.primaryColor = (rgbPrimaryColor[0] << 16) | (rgbPrimaryColor[1] << 8) | rgbPrimaryColor[2];
            this.secondaryColor = (rgbSecondaryColor[0] << 16) | (rgbSecondaryColor[1] << 8) | rgbSecondaryColor[2];
        }

        public EntityLiving spawnMob(World world, double x, double y, double z, ItemStack is) {
            EntityLiving e = null;

            try {
                e = entityClass.getConstructor(World.class).newInstance(world);
            } catch (NoSuchMethodError | InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
                ex.printStackTrace();
                return null;
            }

            if (e == null) return null;

            e.setLocationAndAngles(x, y, z, MathHelper.wrapAngleTo180_float(itemRand.nextFloat() * 360F), 0F);
            e.rotationYawHead = e.rotationYaw;
            e.renderYawOffset = e.rotationYaw;
            e.onSpawnWithEgg((IEntityLivingData) null);
            world.spawnEntityInWorld(e);
            e.playLivingSound();

            if (is.hasDisplayName()) e.setCustomNameTag(is.getDisplayName());

            return e;
        }
    }
}
