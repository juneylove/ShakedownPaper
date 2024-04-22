package me.juneylove.shakedown.listeners;

import io.netty.channel.*;
import me.juneylove.shakedown.listeners.packetlisteners.EntityDataPacketListener;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.NoSuchElementException;

// source/reference for serverbound handler: https://www.spigotmc.org/threads/nms-wasd-enderman-controls.574550/

public class ClientboundHandler extends ChannelOutboundHandlerAdapter {

    public static String handlerName = "me.juneylove.shakedown:clientbound_handler";

    public final Player player;

    private ClientboundHandler(Player player) {
        this.player = player;
    }

    public static synchronized void attach(Player player) {
        ChannelPipeline pipe = getPipeline(player);
        ChannelHandler handler = pipe.get(handlerName);
        if (handler != null) {
            detach(player);
        }
        pipe.addBefore("packet_handler", handlerName, new ClientboundHandler(player));
    }

    public static synchronized void detach(Player player) {
        try {
            getPipeline(player).remove(handlerName);
        } catch (NoSuchElementException ignored) {}
    }

    public static ChannelPipeline getPipeline(Player player) {
        CraftPlayer p = (CraftPlayer) player;
        return p.getHandle().connection.connection.channel.pipeline();
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {

        if (msg instanceof ClientboundSetEntityDataPacket packet) {
            EntityDataPacketListener.handlePacket(packet, player);
        }

        super.write(ctx, msg, promise);

    }

}
