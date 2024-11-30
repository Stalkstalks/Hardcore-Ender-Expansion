package chylex.hee.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

import chylex.hee.block.BlockCustomSpawner;
import chylex.hee.system.logging.Log;
import chylex.hee.system.util.BlockPosM;
import chylex.hee.tileentity.spawner.BlobEndermanSpawnerLogic;
import chylex.hee.tileentity.spawner.CustomSpawnerLogic;
import chylex.hee.tileentity.spawner.LouseRavagedSpawnerLogic;
import chylex.hee.tileentity.spawner.SilverfishDungeonSpawnerLogic;
import chylex.hee.tileentity.spawner.SilverfishRavagedSpawnerLogic;
import chylex.hee.tileentity.spawner.TowerEndermanSpawnerLogic;

public class TileEntityCustomSpawner extends TileEntity {

    private byte logicId;
    private CustomSpawnerLogic logic;
    private int actualX, actualY = -1, actualZ;

    public TileEntityCustomSpawner setLogicId(int id) {
        createLogic((byte) id);
        return this;
    }

    private void createLogic(byte id) {
        switch (id) {
            case BlockCustomSpawner.metaTowerEnderman:
                logic = new TowerEndermanSpawnerLogic(this);
                break;
            case BlockCustomSpawner.metaSilverfishDungeon:
                logic = new SilverfishDungeonSpawnerLogic(this);
                break;
            case BlockCustomSpawner.metaRavagedLouse:
                logic = new LouseRavagedSpawnerLogic(this);
                break;
            case BlockCustomSpawner.metaRavagedSilverfish:
                logic = new SilverfishRavagedSpawnerLogic(this);
                break;
            case BlockCustomSpawner.metaBlobEnderman:
                logic = new BlobEndermanSpawnerLogic(this);
                break;
            default:
                Log.error(
                        "Unable to find spawner logic $0, this is not supposed to happen! Substituting empty logic to prevent crashes.",
                        id);
                logic = new CustomSpawnerLogic.BrokenSpawnerLogic(this);
        }

        this.logicId = id;
    }

    @Override
    public void updateEntity() {
        if (actualY == -1) {
            actualX = xCoord;
            actualY = yCoord;
            actualZ = zCoord;
        } else if (xCoord == actualX && yCoord == actualY && zCoord == actualZ) logic.updateSpawner();

        super.updateEntity();
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound tag = new NBTTagCompound();
        writeToNBT(tag);
        tag.removeTag("SpawnPotentials");
        tag.removeTag("actualPos");
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, tag);
    }

    @Override
    public boolean receiveClientEvent(int eventNb, int arg) {
        return logic.setDelayToMin(eventNb) || super.receiveClientEvent(eventNb, arg);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setByte("logicId", logicId);
        nbt.setLong("actualPos", BlockPosM.tmp(actualX, actualY, actualZ).toLong());
        logic.writeToNBT(nbt);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        createLogic(nbt.getByte("logicId"));

        BlockPosM actualPos = BlockPosM.fromNBT(nbt, "actualPos");
        actualX = actualPos.x;
        actualY = actualPos.y;
        actualZ = actualPos.z;

        logic.readFromNBT(nbt);
    }

    public CustomSpawnerLogic getSpawnerLogic() {
        return logic;
    }
}
