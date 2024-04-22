package me.juneylove.shakedown.listeners.packetlisteners;

import me.juneylove.shakedown.mechanics.GlowManager;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

public class EntityDataPacketListener {


    public static void handlePacket(ClientboundSetEntityDataPacket packet, Player player) {

        List<SynchedEntityData.DataValue<?>> data = packet.packedItems();
        int entityId = packet.id();

        if (GlowManager.shouldShowGlow(entityId, player.getName())) {

            byte baseEntityData = 0b00000000;
            int listIndex = 0;
            boolean foundInList = false;
            //noinspection rawtypes
            for (SynchedEntityData.DataValue dataValue : data) {
                if (dataValue.id() == 0) {
                    baseEntityData = (byte) dataValue.value();
                    foundInList = true;
                    break;
                }
                listIndex++;
            }

            baseEntityData = (byte) (baseEntityData | 0b01000000);

            if (foundInList) {
                data.set(listIndex, SynchedEntityData.DataValue.create(new EntityDataAccessor<>(0, EntityDataSerializers.BYTE), baseEntityData));
            } else {
                data.add(SynchedEntityData.DataValue.create(new EntityDataAccessor<>(0, EntityDataSerializers.BYTE), baseEntityData));
            }

        }

    }
}
