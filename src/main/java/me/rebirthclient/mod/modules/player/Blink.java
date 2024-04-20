package me.rebirthclient.mod.modules.player;

import com.mojang.authlib.GameProfile;
import me.rebirthclient.api.events.eventbus.EventHandler;
import me.rebirthclient.api.events.impl.PacketEvent;
import me.rebirthclient.mod.modules.Module;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

import java.util.ArrayList;
import java.util.UUID;

public class Blink
        extends Module {
    public static Blink INSTANCE = new Blink();
    private final ArrayList<Packet> packetsList = new ArrayList<>();
    public static OtherClientPlayerEntity fakePlayer;
    public Blink() {
        super("Blink", "Fake lag", Category.Player);
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        packetsList.clear();
        if (nullCheck()) {
            disable();
            return;
        }
        fakePlayer = new OtherClientPlayerEntity(mc.world, new GameProfile(UUID.fromString("11451466-6666-6666-6666-666666666601"), mc.player.getName().getString()));
        fakePlayer.copyPositionAndRotation(mc.player);
        fakePlayer.getInventory().clone(mc.player.getInventory());
        mc.world.addPlayer(-5, fakePlayer);
    }

    @Override
    public void onUpdate() {
        if (mc.player.isDead()) {
            packetsList.clear();
            disable();
        }
    }

    @Override
    public void onLogin() {
        if (this.isOn()) {
            packetsList.clear();
            disable();
        }
    }

    @EventHandler
    public void onSendPacket(PacketEvent.Send event) {
        Packet<?> t = event.getPacket();
        if (t instanceof PlayerMoveC2SPacket) {
            packetsList.add(event.getPacket());
            event.cancel();
        }
    }

    @Override
    public void onDisable() {
        if (nullCheck()) {
            packetsList.clear();
            disable();
            return;
        }
        if (fakePlayer != null) {
            fakePlayer.kill();
            fakePlayer.setRemoved(Entity.RemovalReason.KILLED);
            fakePlayer.onRemoved();
            fakePlayer = null;
        }
        for (Packet packet : packetsList) {
            mc.player.networkHandler.sendPacket(packet);
        }
    }

    @Override
    public String getInfo() {
        return String.valueOf(packetsList.size());
    }
}