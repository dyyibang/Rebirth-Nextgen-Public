package me.rebirthclient.api.managers;

import me.rebirthclient.Rebirth;
import me.rebirthclient.api.events.eventbus.EventHandler;
import me.rebirthclient.api.events.impl.DeathEvent;
import me.rebirthclient.api.events.impl.PacketEvent;
import me.rebirthclient.api.events.impl.TotemEvent;
import me.rebirthclient.api.events.impl.UpdateWalkingEvent;
import me.rebirthclient.api.util.Wrapper;
import me.rebirthclient.mod.modules.Module;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;

import java.util.ArrayList;
import java.util.HashMap;

public class PopManager implements Wrapper {
    public PopManager() {
        Rebirth.EVENT_BUS.subscribe(this);
    }

    public final HashMap<String, Integer> popContainer = new HashMap<>();
    public final ArrayList<PlayerEntity> deadPlayer = new ArrayList<>();

    public Integer getPop(String s) {
        return popContainer.getOrDefault(s, 0);
    }
    public void update() {
        if (Module.nullCheck()) return;
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == null || player.isAlive()) {
                deadPlayer.remove(player);
                continue;
            }
            if (deadPlayer.contains(player)) {
                continue;
            }
            Rebirth.EVENT_BUS.post(new DeathEvent(player));
            onDeath(player);
            deadPlayer.add(player);
        }
    }

    @EventHandler
    public void updateWalking(UpdateWalkingEvent event) {
        update();
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive event) {
        if (Module.nullCheck()) return;
        if (event.getPacket() instanceof EntityStatusS2CPacket packet) {
            if (packet.getStatus() == EntityStatuses.USE_TOTEM_OF_UNDYING) {
                Entity entity = packet.getEntity(mc.world);
                if(entity instanceof PlayerEntity player) {
                    onTotemPop(player);
                }
            }
        }
    }

    public void onDeath(PlayerEntity player) {
        popContainer.remove(player.getName().getString());
    }

    public void onTotemPop(PlayerEntity player) {
        int l_Count = 1;
        if (popContainer.containsKey(player.getName().getString())) {
            l_Count = popContainer.get(player.getName().getString());
            popContainer.put(player.getName().getString(), ++l_Count);
        } else {
            popContainer.put(player.getName().getString(), l_Count);
        }
        Rebirth.EVENT_BUS.post(new TotemEvent(player));
    }
}
