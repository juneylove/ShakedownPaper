package me.juneylove.shakedown.mechanics;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R1.CraftServer;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

public class NPC {

    ServerPlayer npc;
    ServerLevel nmsWorld;
    GameProfile gameProfile;

    List<Player> visibleTo;

    static HashMap<String, String> skins = new HashMap<>();
    static HashMap<String, String> signatures = new HashMap<>();

    public NPC(String playerName, Location location, List<Player> visibleTo) {

        location.subtract(0.0, 1.0, 0.0);

        Location priorSpawn = location.getWorld().getSpawnLocation();
        location.getWorld().setSpawnLocation(location);

        MinecraftServer nmsServer = ((CraftServer) Bukkit.getServer()).getServer();
        nmsWorld = ((CraftWorld)location.getWorld()).getHandle();
        gameProfile = new GameProfile(UUID.randomUUID(), playerName);
        npc = new ServerPlayer(nmsServer, nmsWorld, gameProfile);

        npc.teleportTo(nmsWorld, location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());

        for (Player player : visibleTo) {
            addNPCPacket(player);
            sendSetNPCSkinPacket(player, playerName);
        }
        this.visibleTo = visibleTo;

        location.getWorld().setSpawnLocation(priorSpawn);

    }

    public void remove() {

        for (Player player : visibleTo) {
            removeNPCPacket(player);
        }

    }

    public static void loadSkin(String username) {

        if (skins.containsKey(username)) return; // skin already loaded

        try {
            HttpsURLConnection connection = (HttpsURLConnection) new URL(String.format("https://api.ashcon.app/mojang/v2/user/%s", username)).openConnection();
            if (connection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                ArrayList<String> lines = new ArrayList<>();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                reader.lines().forEach(lines::add);

                String reply = String.join(" ",lines);
                int indexOfValue = reply.indexOf("\"value\": \"");
                int indexOfSignature = reply.indexOf("\"signature\": \"");
                String skin = reply.substring(indexOfValue + 10, reply.indexOf("\"", indexOfValue + 10));
                String signature = reply.substring(indexOfSignature + 14, reply.indexOf("\"", indexOfSignature + 14));

                skins.put(username, skin);
                signatures.put(username, signature);
            }

            else {
                Bukkit.getConsoleSender().sendMessage("Connection could not be opened when fetching player skin (Response code " + connection.getResponseCode() + ", " + connection.getResponseMessage() + ")");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }



    }

    // ==========

    private void addNPCPacket(Player player) {

        ServerGamePacketListenerImpl connection = ((CraftPlayer) player).getHandle().connection;
        connection.send(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, npc));
        connection.send(new ClientboundAddPlayerPacket(npc));
        connection.send(new ClientboundRotateHeadPacket(npc, (byte) (npc.getBukkitYaw() * 256 / 360)));

        connection.send(new ClientboundTeleportEntityPacket(npc));

    }

    private void removeNPCPacket(Player player) {

        ServerGamePacketListenerImpl connection = ((CraftPlayer) player).getHandle().connection;
        connection.send(new ClientboundRemoveEntitiesPacket(npc.getId()));

    }

    private void sendSetNPCSkinPacket(Player player, String username) { // The username is the name for the player that has the skin

        if (!skins.containsKey(username)) {
            loadSkin(username);
        }

        gameProfile.getProperties().put("textures", new Property("textures", skins.get(username), signatures.get(username)));

        // The client settings.
        SynchedEntityData data = npc.getEntityData();
        data.set(new EntityDataAccessor<>(17, EntityDataSerializers.BYTE), (byte)126);
        ClientboundSetEntityDataPacket packet = new ClientboundSetEntityDataPacket(npc.getId(), Objects.requireNonNull(data.getNonDefaultValues()));
        ((CraftPlayer)player).getHandle().connection.send(packet);

    }

}
