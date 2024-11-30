package chylex.hee.packets;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.tileentity.TileEntity;

import org.apache.commons.io.FilenameUtils;

import chylex.hee.HardcoreEnderExpansion;
import chylex.hee.system.logging.Stopwatch;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLEmbeddedChannel;
import cpw.mods.fml.common.network.FMLEventChannel;
import cpw.mods.fml.common.network.FMLNetworkEvent.ClientCustomPacketEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ServerCustomPacketEvent;
import cpw.mods.fml.common.network.FMLOutboundHandler;
import cpw.mods.fml.common.network.FMLOutboundHandler.OutboundTarget;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gnu.trove.map.hash.TByteObjectHashMap;
import gnu.trove.map.hash.TObjectByteHashMap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;

public class PacketPipeline {

    private static PacketPipeline instance;
    private static final String channelName = "hee";

    public static synchronized void initializePipeline() {
        if (instance != null) throw new RuntimeException("Packet pipeline has already been registered!");
        instance = new PacketPipeline();
        instance.load();
    }

    private FMLEventChannel eventDrivenChannel;
    private EnumMap<Side, FMLEmbeddedChannel> channels;

    private final TByteObjectHashMap<Class<? extends AbstractPacket>> idToPacket = new TByteObjectHashMap<>();
    private final TObjectByteHashMap<Class<? extends AbstractPacket>> packetToId = new TObjectByteHashMap<>();

    private PacketPipeline() {}

    private void load() {
        Stopwatch.time("PacketPipeline");

        eventDrivenChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel(channelName);
        eventDrivenChannel.register(this);

        try {
            Field channelField = FMLEventChannel.class.getDeclaredField("channels");
            channelField.setAccessible(true);
            channels = (EnumMap) channelField.get(eventDrivenChannel);

            int id = -1;

            File sourceFile = HardcoreEnderExpansion.sourceFile;
            List<String> classes = new ArrayList<>();

            if (sourceFile.isDirectory()) {
                File root = Paths.get(sourceFile.getPath(), "chylex", "hee", "packets").toFile();

                for (String name : new File(root, "client").list()) {
                    if (name.startsWith("C"))
                        classes.add("chylex.hee.packets.client." + FilenameUtils.removeExtension(name));
                }

                for (String name : new File(root, "server").list()) {
                    if (name.startsWith("S"))
                        classes.add("chylex.hee.packets.server." + FilenameUtils.removeExtension(name));
                }
            } else {
                try (ZipFile zip = new ZipFile(sourceFile)) {
                    for (ZipEntry entry : Collections.list(zip.entries())) {
                        String name = entry.getName();

                        if (name.startsWith("chylex/hee/packets/")) {
                            if (name.startsWith("chylex/hee/packets/client/C")
                                    || name.startsWith("chylex/hee/packets/server/S")) {
                                classes.add(FilenameUtils.removeExtension(name.replace('/', '.')));
                            }
                        }
                    }
                } catch (IOException e) {
                    throw e;
                }
            }

            Collections.sort(classes);

            for (String cls : classes) registerPacket(++id, (Class<? extends AbstractPacket>) Class.forName(cls));
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException | IOException
                | ClassNotFoundException e) {
            throw new RuntimeException("Unable to load the Packet system!", e);
        }

        Stopwatch.finish("PacketPipeline");
    }

    private void registerPacket(int id, Class<? extends AbstractPacket> cls) {
        idToPacket.put((byte) id, cls);
        packetToId.put(cls, (byte) id);
    }

    private FMLProxyPacket writePacket(AbstractPacket packet) {
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeByte(packetToId.get(packet.getClass()));
        packet.write(buffer);
        return new FMLProxyPacket(buffer, channelName);
    }

    private void readPacket(FMLProxyPacket fmlPacket, Side side) {
        ByteBuf buffer = fmlPacket.payload();

        try {
            AbstractPacket packet = idToPacket.get(buffer.readByte()).newInstance();
            packet.read(buffer.slice());

            switch (side) {
                case CLIENT:
                    packet.handle(Side.CLIENT, getClientPlayer());
                    break;

                case SERVER:
                    packet.handle(Side.SERVER, ((NetHandlerPlayServer) fmlPacket.handler()).playerEntity);
                    break;
            }
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @SideOnly(Side.CLIENT)
    private EntityPlayer getClientPlayer() {
        return Minecraft.getMinecraft().thePlayer;
    }

    // EVENTS AND DISPATCHING

    @SubscribeEvent
    public void onClientPacketReceived(ClientCustomPacketEvent e) {
        readPacket(e.packet, Side.CLIENT);
    }

    @SubscribeEvent
    public void onServerPacketReceived(ServerCustomPacketEvent e) {
        readPacket(e.packet, Side.SERVER);
    }

    public static void sendToAll(AbstractPacket packet) {
        FMLEmbeddedChannel channel = instance.channels.get(Side.SERVER);
        channel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(OutboundTarget.ALL);
        channel.writeAndFlush(instance.writePacket(packet))
                .addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    public static void sendToPlayer(EntityPlayer player, AbstractPacket packet) {
        FMLEmbeddedChannel channel = instance.channels.get(Side.SERVER);
        channel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(OutboundTarget.PLAYER);
        channel.attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(player);
        channel.writeAndFlush(instance.writePacket(packet))
                .addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    public static void sendToAllAround(int dimension, double x, double y, double z, double range,
            AbstractPacket packet) {
        FMLEmbeddedChannel channel = instance.channels.get(Side.SERVER);
        channel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(OutboundTarget.ALLAROUNDPOINT);
        channel.attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(new TargetPoint(dimension, x, y, z, range));
        channel.writeAndFlush(instance.writePacket(packet))
                .addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    public static void sendToAllAround(Entity entity, double range, AbstractPacket packet) {
        FMLEmbeddedChannel channel = instance.channels.get(Side.SERVER);
        channel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(OutboundTarget.ALLAROUNDPOINT);
        channel.attr(FMLOutboundHandler.FML_MESSAGETARGETARGS)
                .set(new TargetPoint(entity.dimension, entity.posX, entity.posY, entity.posZ, range));
        channel.writeAndFlush(instance.writePacket(packet))
                .addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    public static void sendToAllAround(TileEntity tile, double range, AbstractPacket packet) {
        FMLEmbeddedChannel channel = instance.channels.get(Side.SERVER);
        channel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(OutboundTarget.ALLAROUNDPOINT);
        channel.attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(
                new TargetPoint(
                        tile.getWorldObj().provider.dimensionId,
                        tile.xCoord + 0.5D,
                        tile.yCoord + 0.5D,
                        tile.zCoord + 0.5D,
                        range));
        channel.writeAndFlush(instance.writePacket(packet))
                .addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    public static void sendToDimension(int dimension, AbstractPacket packet) {
        FMLEmbeddedChannel channel = instance.channels.get(Side.SERVER);
        channel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(OutboundTarget.DIMENSION);
        channel.attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(dimension);
        channel.writeAndFlush(instance.writePacket(packet))
                .addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    public static void sendToServer(AbstractPacket packet) {
        FMLEmbeddedChannel channel = instance.channels.get(Side.CLIENT);
        channel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(OutboundTarget.TOSERVER);
        channel.writeAndFlush(instance.writePacket(packet))
                .addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }
}
