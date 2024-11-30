package chylex.hee.entity.mob;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemMinecart;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.Constants;

import chylex.hee.block.BlockCrossedDecoration;
import chylex.hee.entity.GlobalMobData.IIgnoreEnderGoo;
import chylex.hee.entity.mob.util.IEndermanRenderer;
import chylex.hee.init.BlockList;
import chylex.hee.init.ItemList;
import chylex.hee.mechanics.causatum.CausatumMeters;
import chylex.hee.mechanics.causatum.CausatumUtils;
import chylex.hee.mechanics.misc.Baconizer;
import chylex.hee.packets.PacketPipeline;
import chylex.hee.packets.client.C00ClearInventorySlot;
import chylex.hee.proxy.ModCommonProxy;
import chylex.hee.system.util.BlockPosM;
import chylex.hee.system.util.IItemSelector;
import chylex.hee.system.util.MathUtil;

public class EntityMobBabyEnderman extends EntityMob implements IEndermanRenderer, IIgnoreEnderGoo {

    private EntityPlayer target;
    private final List<ItemPriorityLevel> itemPriorities = new ArrayList<>();
    private ItemPriorityLevel carryingLevel = ItemPriorityLevel.RANDOM;
    private byte itemDecisionTimer, attentionLossTimer = -125;
    private boolean isFamilyChosen, isScared;

    public EntityMobBabyEnderman(World world) {
        super(world);
        setSize(0.5F, 1.26F);
        stepHeight = 1F;

        for (ItemPriorityLevel level : ItemPriorityLevel.values) itemPriorities.add(level);
        for (int a = 0, index1, index2, size = itemPriorities.size(); a < rand.nextInt(20); a++) {
            index1 = rand.nextInt(size);
            index2 = rand.nextInt(size);

            if (index1 == index2) continue;
            Collections.swap(itemPriorities, index1, index2);
        }
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(ModCommonProxy.opMobs ? 15D : 11D);
        getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(ModCommonProxy.opMobs ? 0.75D : 0.7D);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataWatcher.addObject(16, new ItemStack(Blocks.bedrock));
    }

    @Override
    protected Entity findPlayerToAttack() {
        return entityToAttack;
    }

    @Override
    public void onLivingUpdate() {
        if (isWet()) {
            attackEntityFrom(DamageSource.drown, 1F);
        }

        isJumping = false;

        if (entityToAttack != null) {
            faceEntity(entityToAttack, 100F, 100F);
        }

        boolean hasIS = isCarrying();

        if (!worldObj.isRemote) {
            if (target == null) {
                if (!hasIS && !isScared
                        && rand.nextInt(550) == 0
                        && worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing")) { // set target
                    List<EntityPlayer> list = worldObj
                            .getEntitiesWithinAABB(EntityPlayer.class, boundingBox.expand(6D, 3D, 6D));

                    if (!list.isEmpty()) {
                        target = list.get(rand.nextInt(list.size()));

                        ItemStack headArmor = target.getCurrentArmor(3);
                        if (headArmor != null && headArmor.getItem() == ItemList.enderman_head) target = null;
                        else attentionLossTimer = (byte) (64 + rand.nextInt(62));
                    }
                } else { // find stuff on the ground
                    List<EntityItem> list = worldObj
                            .getEntitiesWithinAABB(EntityItem.class, boundingBox.expand(1D, 0D, 1D));

                    if (!list.isEmpty() && ++itemDecisionTimer > rand.nextInt(70) + 15) {
                        int carryingLevelIndex = itemPriorities.indexOf(carryingLevel);

                        EntityItem item = list.get(rand.nextInt(list.size()));
                        ItemStack is = item.getEntityItem();

                        for (ItemPriorityLevel level : itemPriorities) {
                            if (level.isValid(is)) {
                                if (itemPriorities.indexOf(level) < carryingLevelIndex) {
                                    if (hasIS) {
                                        EntityItem newItem = new EntityItem(worldObj, posX, posY, posZ, getCarrying());
                                        float power = 0.3F, yawRadians = (float) Math.toRadians(rotationYaw),
                                                randomAngle = rand.nextFloat() * (float) Math.PI * 2F;

                                        newItem.motionX = (-MathHelper.sin(yawRadians) * MathHelper.cos(yawRadians)
                                                * power);
                                        newItem.motionZ = (MathHelper.cos(yawRadians) * MathHelper.cos(yawRadians)
                                                * power);
                                        newItem.motionY = (-MathHelper.sin((float) Math.toRadians(rotationPitch))
                                                * power + 0.1F);

                                        power = 0.02F * rand.nextFloat();
                                        newItem.motionX += MathHelper.cos(randomAngle) * power;
                                        newItem.motionY += (rand.nextFloat() - rand.nextFloat()) * 0.1F;
                                        newItem.motionZ += MathHelper.sin(randomAngle) * power;

                                        worldObj.spawnEntityInWorld(newItem);
                                    }

                                    setCarriedItemStack(is);
                                    item.setDead();
                                }

                                break;
                            }
                        }

                        itemDecisionTimer = 0;
                    }
                }
            } else if (target != null) {
                if (--attentionLossTimer < -124 || target.isDead) target = null; // is target dead
                else if (!hasIS && getDistanceToEntity(target) < 1.8D) { // steal from target
                    for (int attempt = 0, slot; attempt < 60; attempt++) {
                        slot = rand.nextInt(target.inventory.mainInventory.length);
                        if (slot == target.inventory.currentItem) continue;

                        ItemStack is = target.inventory.mainInventory[slot];
                        if (is != null) {
                            ItemStack carrying = is.copy();
                            carrying.stackSize = 1;
                            setCarriedItemStack(carrying);

                            if (--target.inventory.mainInventory[slot].stackSize == 0) {
                                target.inventory.mainInventory[slot] = null;
                                PacketPipeline.sendToPlayer(target, new C00ClearInventorySlot(slot));
                            }

                            break;
                        }
                    }

                    PathEntity escapePath = null;
                    BlockPosM tmpPos = BlockPosM.tmp();

                    for (int pathatt = 0; pathatt < 100; pathatt++) {
                        double ang = rand.nextDouble() * 2D * Math.PI, len = 8D + rand.nextDouble() * 6D;
                        tmpPos.set(posX + Math.cos(ang) * len, posY + rand.nextInt(4) - 2, posZ + Math.sin(ang) * len);

                        Block low = tmpPos.getBlock(worldObj);
                        if ((low.getMaterial() == Material.air || low == BlockList.crossed_decoration)
                                && tmpPos.moveUp().getMaterial(worldObj) == Material.air) {
                            escapePath = worldObj.getEntityPathToXYZ(
                                    this,
                                    tmpPos.x,
                                    tmpPos.y,
                                    tmpPos.z,
                                    16F,
                                    false,
                                    true,
                                    false,
                                    false);
                            break;
                        }
                    }

                    if (escapePath != null) setPathToEntity(escapePath);
                    target = null;
                }
            }

            entityToAttack = target;
        }

        super.onLivingUpdate();
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        boolean flag = super.attackEntityFrom(source, amount);
        if (flag) CausatumUtils.increase(source, CausatumMeters.END_MOB_DAMAGE, amount);

        if (flag && !isFamilyChosen && !worldObj.isRemote && source.getEntity() instanceof EntityPlayer) {
            List<EntityEnderman> endermanList = worldObj
                    .getEntitiesWithinAABB(EntityEnderman.class, boundingBox.expand(32D, 32D, 32D));
            Collections.sort(endermanList, new DistanceComparator(this));

            int familySize = Math.min(endermanList.size(), 2 + rand.nextInt(3) + rand.nextInt(2));
            for (int a = 0; a < familySize; a++) {
                EntityEnderman orig = endermanList.get(a);
                EntityMobAngryEnderman angryEnderman = new EntityMobAngryEnderman(
                        worldObj,
                        orig.posX,
                        orig.posY,
                        orig.posZ);
                angryEnderman.copyLocationAndAnglesFrom(orig);
                angryEnderman.setTarget(source.getEntity());

                orig.setDead();
                worldObj.spawnEntityInWorld(angryEnderman);
            }

            isFamilyChosen = isScared = true;
        }

        entityToAttack = null;

        return flag;
    }

    @Override
    protected String getLivingSound() {
        return Baconizer.soundNormal("mob.endermen.idle");
    }

    @Override
    protected String getHurtSound() {
        return Baconizer.soundNormal("mob.endermen.hit");
    }

    @Override
    protected String getDeathSound() {
        return Baconizer.soundDeath("mob.endermen.death");
    }

    @Override
    protected float getSoundPitch() {
        return 1.25F;
    }

    @Override
    protected void dropFewItems(boolean recentlyHit, int looting) {
        if (isCarrying()) entityDropItem(getCarrying(), 0F);
    }

    @Override
    protected boolean isValidLightLevel() {
        return worldObj.provider.dimensionId == 1 || super.isValidLightLevel();
    }

    @Override
    protected void despawnEntity() {
        if (!isCarrying()) super.despawnEntity();
    }

    public void setCarriedItemStack(ItemStack is) {
        dataWatcher.updateObject(16, is);

        for (ItemPriorityLevel level : itemPriorities) {
            if (level.isValid(is)) {
                carryingLevel = level;
                break;
            }
        }
    }

    @Override
    public void setEquipmentDropChance(int slot, float chance) {
        super.setEquipmentDropChance(slot, chance);
        if (MathUtil.floatEquals(chance, 0F) && !isDead) setDead(); // autospawner protection
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbt) {
        super.writeEntityToNBT(nbt);

        // item priority list
        NBTTagList tagPriorities = new NBTTagList();
        for (ItemPriorityLevel level : itemPriorities) tagPriorities.appendTag(new NBTTagString(level.name()));
        nbt.setTag("priorities", tagPriorities);

        // carried item
        ItemStack is = getCarrying();
        if (is != null) nbt.setTag("carrying", is.writeToNBT(new NBTTagCompound()));

        // other
        nbt.setBoolean("isFamilyChosen", isFamilyChosen);
        nbt.setBoolean("isScared", isScared);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbt) {
        super.readEntityFromNBT(nbt);

        // item priority list
        NBTTagList tagPriorities = nbt.getTagList("priorities", Constants.NBT.TAG_STRING);

        if (tagPriorities.tagCount() > 0) {
            itemPriorities.clear();

            for (int a = 0; a < tagPriorities.tagCount(); a++) {
                itemPriorities.add(ItemPriorityLevel.valueOf(tagPriorities.getStringTagAt(a)));
            }
        }

        // carried item
        if (nbt.hasKey("carrying")) setCarriedItemStack(ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("carrying")));

        // other
        isFamilyChosen = nbt.getBoolean("isFamilyChosen");
        isScared = nbt.getBoolean("isScared");
    }

    @Override
    public boolean isScreaming() {
        return false;
    }

    @Override
    public boolean isCarrying() {
        ItemStack is = getCarrying();
        return is != null && is.getItem() != Item.getItemFromBlock(Blocks.bedrock);
    }

    @Override
    public ItemStack getCarrying() {
        return dataWatcher.getWatchableObjectItemStack(16);
    }

    @Override
    public String getCommandSenderName() {
        return hasCustomNameTag() ? getCustomNameTag()
                : StatCollector.translateToLocal(Baconizer.mobName("entity.babyEnderman.name"));
    }
}

class DistanceComparator<T extends Entity> implements Comparator<T> {

    private final Entity source;

    public DistanceComparator(Entity source) {
        this.source = source;
    }

    @Override
    public int compare(T e1, T e2) {
        return ((Float) e1.getDistanceToEntity(source)).compareTo(e2.getDistanceToEntity(source));
    }
}

enum ItemPriorityLevel {

    ENCHANTED(new IItemSelector() {

        @Override
        public boolean isValid(ItemStack is) {
            return is.isItemEnchanted();
        }
    }),

    SHINYARMORTOOLS(new IItemSelector() {

        @Override
        public boolean isValid(ItemStack is) {
            Item item = is.getItem();

            if (item instanceof ItemArmor) {
                ArmorMaterial armorMat = ((ItemArmor) item).getArmorMaterial();
                return armorMat == ArmorMaterial.IRON || armorMat == ArmorMaterial.GOLD
                        || armorMat == ArmorMaterial.DIAMOND;
            } else if (item instanceof ItemTool || item instanceof ItemSword) {
                ToolMaterial toolMat = getMaterial(item);
                return toolMat == ToolMaterial.IRON || toolMat == ToolMaterial.GOLD || toolMat == ToolMaterial.EMERALD;
            } else return false;
        }

        private ToolMaterial getMaterial(Item item) {
            String matName = item instanceof ItemTool ? ((ItemTool) item).getToolMaterialName()
                    : item instanceof ItemSword ? ((ItemSword) item).getToolMaterialName() : null;
            if (matName == null) return null;

            for (ToolMaterial mat : ToolMaterial.values()) {
                if (mat.toString().equals(matName)) return mat;
            }

            return null;
        }
    }),

    SHINYBLOCKS(new IItemSelector() {

        @Override
        public boolean isValid(ItemStack is) {
            if (!(is.getItem() instanceof ItemBlock)) return false;
            Block block = Block.getBlockFromItem(is.getItem());
            return block == Blocks.diamond_block || block == Blocks.gold_block
                    || block == Blocks.iron_block
                    || block == Blocks.emerald_block
                    || block == Blocks.redstone_block
                    || block == Blocks.quartz_block;
        }
    }),

    SHINYITEMS(new IItemSelector() {

        @Override
        public boolean isValid(ItemStack is) {
            Item item = is.getItem();
            return item == Items.diamond || item == Items.gold_ingot
                    || item == Items.iron_ingot
                    || item == Items.gold_nugget
                    || item == Items.emerald;
        }
    }),

    IRONMAT(new IItemSelector() {

        @Override
        public boolean isValid(ItemStack is) {
            return (is.getItem() instanceof ItemBlock)
                    ? Block.getBlockFromItem(is.getItem()).getMaterial() == Material.iron
                    : is.getItem() instanceof ItemMinecart;
        }
    }),

    ENDER(new IItemSelector() {

        @Override
        public boolean isValid(ItemStack is) {
            Item item = is.getItem();
            return item == Items.ender_pearl || item == Items.ender_eye
                    || item == ItemList.end_powder
                    || item == ItemList.transference_gem
                    || item == ItemList.spatial_dash_gem;
        }
    }),

    FOOD(new IItemSelector() {

        @Override
        public boolean isValid(ItemStack is) {
            return is.getItemUseAction() == EnumAction.eat;
        }
    }),

    POTION(new IItemSelector() {

        @Override
        public boolean isValid(ItemStack is) {
            return is.getItem() == Items.potionitem || is.getItem() == Items.experience_bottle;
        }
    }),

    PLANTS(new IItemSelector() {

        @Override
        public boolean isValid(ItemStack is) {
            Item item = is.getItem();
            if (item instanceof IPlantable) return true;
            if (!(item instanceof ItemBlock)) return false;

            Block block = Block.getBlockFromItem(item);
            return block == BlockList.death_flower || (block == BlockList.crossed_decoration
                    && is.getItemDamage() == BlockCrossedDecoration.dataLilyFire);
        }
    }),

    WOOD(new IItemSelector() {

        @Override
        public boolean isValid(ItemStack is) {
            return (is.getItem() instanceof ItemBlock)
                    ? Block.getBlockFromItem(is.getItem()).getMaterial() == Material.wood
                    : is.getItem() == Items.stick;
        }
    }),

    GROUNDMAT(new IItemSelector() {

        @Override
        public boolean isValid(ItemStack is) {
            if (is.getItem() instanceof ItemBlock) {
                Material mat = Block.getBlockFromItem(is.getItem()).getMaterial();
                return mat == Material.rock || mat == Material.grass || mat == Material.ground || mat == Material.sand;
            }
            return false;
        }
    }),

    RANDOM(new IItemSelector() {

        @Override
        public boolean isValid(ItemStack is) {
            return true;
        }
    });

    static final ItemPriorityLevel[] values = values();

    private IItemSelector selector;

    private ItemPriorityLevel(IItemSelector selector) {
        this.selector = selector;
    }

    public boolean isValid(ItemStack is) {
        return selector.isValid(is);
    }
}
