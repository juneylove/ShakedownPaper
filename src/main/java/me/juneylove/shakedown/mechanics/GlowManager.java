package me.juneylove.shakedown.mechanics;

import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.ChatFormatting;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.scores.PlayerTeam;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_20_R1.CraftServer;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.*;

public class GlowManager {

    static List<GlowingPlayer> glowingPlayers = new ArrayList<>();
    static List<Integer> entityIds = new ArrayList<>(); // duplicate tracking of ids for easy referencing

    static HashMap<NamedTextColor, Integer> colorCodes = new HashMap<>();

    static {
        colorCodes.put(NamedTextColor.BLACK, 0);
        colorCodes.put(NamedTextColor.DARK_BLUE, 1);
        colorCodes.put(NamedTextColor.DARK_GREEN, 2);
        colorCodes.put(NamedTextColor.DARK_AQUA, 3);
        colorCodes.put(NamedTextColor.DARK_RED, 4);
        colorCodes.put(NamedTextColor.DARK_PURPLE, 5);
        colorCodes.put(NamedTextColor.GOLD, 6);
        colorCodes.put(NamedTextColor.GRAY, 7);
        colorCodes.put(NamedTextColor.DARK_GRAY, 8);
        colorCodes.put(NamedTextColor.BLUE, 9);
        colorCodes.put(NamedTextColor.GREEN, 10);
        colorCodes.put(NamedTextColor.AQUA, 11);
        colorCodes.put(NamedTextColor.RED, 12);
        colorCodes.put(NamedTextColor.LIGHT_PURPLE, 13);
        colorCodes.put(NamedTextColor.YELLOW, 14);
        colorCodes.put(NamedTextColor.WHITE, 15);
    }

    public static void glowTicker() {
        for (GlowingPlayer glowingPlayer : List.copyOf(glowingPlayers)) {
            glowingPlayer.tick();
        }
    }

    public static void addGlow(Player player, NamedTextColor color, Set<String> showToIgns, int durationTicks) {
        glowingPlayers.add(new GlowingPlayer(player, color, showToIgns, durationTicks));
    }

    public static void onPlayerRejoin(Player player) {

        for (GlowingPlayer glowingPlayer : glowingPlayers) {

            if (player.getName().equals(glowingPlayer.ign)) {
                int priorId = glowingPlayer.entityId;
                glowingPlayer.entityId = player.getEntityId();
                entityIds.remove((Integer) priorId);
                entityIds.add(glowingPlayer.entityId);
                glowingPlayer.start();
            }

        }

    }

    public static boolean shouldShowGlow(int glowingEntityId, String visibleToIgn) {

        if (entityIds.contains(glowingEntityId)) {
            for (GlowingPlayer glowingPlayer : glowingPlayers) {
                if (glowingPlayer.entityId == glowingEntityId) {

                    if (Respawn.IsTempSpec(glowingPlayer.ign)) return false;

                    for (String visibleTo : glowingPlayer.showGlowTo) {
                        if (visibleTo.equals(visibleToIgn)) return true;
                    }

                }
            }
        }
        return false;

    }

    // ==========

    private static class GlowingPlayer {

        String ign;
        int entityId;
        Set<String> showGlowTo;
        int remainingTicks;

        NamedTextColor color;
        String teamName;

        GlowingPlayer(Player player, NamedTextColor color, Set<String> players, int durationTicks) {
            ign = player.getName();
            entityId = player.getEntityId();
            entityIds.add(player.getEntityId());
            showGlowTo = players;
            remainingTicks = durationTicks;

            this.color = color;
            StringBuilder builder = new StringBuilder(ign + "-glowTo-");
            for (String ign : players) {
                builder.append(ign).append("-");
            }
            teamName = builder.substring(0, builder.length()-1);

            start();
        }

        protected void tick() {

            remainingTicks--;
            if (remainingTicks == 0) {
                stop();
            }

        }

        protected void start() {

            Player player = Bukkit.getPlayer(ign);
            if (player == null) return;

            SynchedEntityData data = ((CraftPlayer)player).getHandle().getEntityData();
            byte baseEntityData = data.get(new EntityDataAccessor<>(0, EntityDataSerializers.BYTE));
            baseEntityData = (byte) (baseEntityData | 0b01000000);
            data.set(new EntityDataAccessor<>(0, EntityDataSerializers.BYTE), baseEntityData);
            ClientboundSetEntityDataPacket packet1 = new ClientboundSetEntityDataPacket(((CraftPlayer)player).getHandle().getId(), Objects.requireNonNull(data.getNonDefaultValues()));

            PlayerTeam team = new PlayerTeam(new ServerScoreboard(((CraftServer) Bukkit.getServer()).getServer()), teamName);
            ChatFormatting glowColor = ChatFormatting.getById(colorCodes.get(color));
            if (glowColor != null) team.setColor(glowColor);
            ClientboundSetPlayerTeamPacket packet2 = ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, true);

            ClientboundSetPlayerTeamPacket packet3 = ClientboundSetPlayerTeamPacket.createPlayerPacket(team, ign, ClientboundSetPlayerTeamPacket.Action.ADD);

            for (String ign : showGlowTo) {
                Player visibleTo = Bukkit.getPlayer(ign);
                if (visibleTo == null) continue;
                ((CraftPlayer)visibleTo).getHandle().connection.send(packet1);
                ((CraftPlayer)visibleTo).getHandle().connection.send(packet2);
                ((CraftPlayer)visibleTo).getHandle().connection.send(packet3);
            }

        }

        protected void stop() {

            Player player = Bukkit.getPlayer(ign);
            if (player == null) return;

            glowingPlayers.remove(this);
            entityIds.remove((Integer) entityId);

            SynchedEntityData data = ((CraftPlayer)player).getHandle().getEntityData();
            ClientboundSetEntityDataPacket packet1 = new ClientboundSetEntityDataPacket(((CraftPlayer)player).getHandle().getId(), Objects.requireNonNull(data.getNonDefaultValues()));

            PlayerTeam team = new PlayerTeam(new ServerScoreboard(((CraftServer) Bukkit.getServer()).getServer()), teamName);
            ClientboundSetPlayerTeamPacket packet2 = ClientboundSetPlayerTeamPacket.createRemovePacket(team);

            // send unmodified server-side data because player isn't actually glowing, should reset it client-side
            for (String ign : showGlowTo) {
                Player visibleTo = Bukkit.getPlayer(ign);
                if (visibleTo == null) continue;
                ((CraftPlayer)visibleTo).getHandle().connection.send(packet1);
                ((CraftPlayer)visibleTo).getHandle().connection.send(packet2);
            }

        }

    }

}
