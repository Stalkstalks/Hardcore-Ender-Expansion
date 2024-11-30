package chylex.hee.system.savedata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.IdentityHashMap;
import java.util.Map;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;

import chylex.hee.system.logging.Log;
import chylex.hee.system.logging.Stopwatch;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public final class WorldDataHandler {

    private static WorldDataHandler instance;

    public static void register() {
        if (instance == null) MinecraftForge.EVENT_BUS.register(instance = new WorldDataHandler());
    }

    public static <T> T get(Class<? extends WorldSavefile> cls) {
        Stopwatch.timeAverage("WorldDataHandler - get", 160000);

        WorldSavefile savefile = instance.cache.get(cls);

        if (savefile == null) {
            try {
                instance.cache.put(cls, savefile = cls.newInstance());

                File file = new File(instance.worldSaveDir, savefile.filename);

                if (file.exists()) {
                    try {
                        Stopwatch.time("WorldDataHandler - load " + savefile.filename);
                        savefile.loadFromNBT(CompressedStreamTools.readCompressed(new FileInputStream(file)));
                        Stopwatch.finish("WorldDataHandler - load " + savefile.filename);
                    } catch (IOException ioe) {
                        Log.throwable(ioe, "Error reading NBT file - $0", cls.getName());
                    }
                } else savefile.loadFromNBT(new NBTTagCompound());
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException("Could not construct a new instance of WorldSavefile - " + cls.getName(), e);
            }
        }

        Stopwatch.finish("WorldDataHandler - get");

        return (T) savefile;
    }

    public static String getWorldIdentifier(World world) {
        return world.getSaveHandler().getWorldDirectoryName() + world.getWorldInfo().getWorldName()
                + world.getWorldInfo().getSeed();
    }

    public static void forceSave() {
        instance.saveModified();
    }

    private final Map<Class<? extends WorldSavefile>, WorldSavefile> cache = new IdentityHashMap<>();
    private File worldSaveDir;
    private String worldIdentifier = "";

    private WorldDataHandler() {}

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load e) {
        if (e.world.isRemote) return;

        String id = getWorldIdentifier(e.world);

        if (!worldIdentifier.equals(id)) {
            Log.debug("Clearing cache - old $0, new $1", worldIdentifier, id);
            cache.clear();
            worldIdentifier = id;

            File root = DimensionManager.getCurrentSaveRootDirectory();

            if (root != null) {
                worldSaveDir = new File(root, "hee");
                if (!worldSaveDir.exists()) worldSaveDir.mkdirs();
            }
        }
    }

    @SubscribeEvent
    public void onWorldSave(WorldEvent.Save e) {
        saveModified();
    }

    private void saveModified() {
        if (worldSaveDir == null) return;

        for (WorldSavefile savefile : cache.values()) {
            if (savefile.wasModified()) {
                NBTTagCompound nbt = new NBTTagCompound();
                savefile.saveToNBT(nbt);

                try {
                    Stopwatch.time("WorldDataHandler - save " + savefile.filename);
                    CompressedStreamTools
                            .writeCompressed(nbt, new FileOutputStream(new File(worldSaveDir, savefile.filename)));
                    Stopwatch.finish("WorldDataHandler - save " + savefile.filename);
                } catch (Exception ex) {
                    Log.throwable(ex, "Error writing WorldData file $0", savefile.getClass());
                }
            }
        }
    }
}
