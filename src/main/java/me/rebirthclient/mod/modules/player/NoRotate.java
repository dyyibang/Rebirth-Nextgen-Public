/**
 * Anti-Invis Module
 */
package me.rebirthclient.mod.modules.player;

import me.rebirthclient.api.events.eventbus.EventHandler;
import me.rebirthclient.api.events.impl.PacketEvent;
import me.rebirthclient.asm.accessors.IPlayerPositionLookS2CPacket;
import me.rebirthclient.mod.modules.Module;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;

public class NoRotate extends Module {
	public static NoRotate INSTANCE;
	public NoRotate() {
		super("NoRotate", Category.Player);
		INSTANCE = this;
	}

	@EventHandler
	public void onPacketReceive(PacketEvent.Receive event){
		if(nullCheck()) return;
		if(event.getPacket() instanceof PlayerPositionLookS2CPacket packet){
			float yaw = mc.player.getYaw();
			float pitch = mc.player.getPitch();
			((IPlayerPositionLookS2CPacket) packet).setYaw(yaw);
			((IPlayerPositionLookS2CPacket) packet).setPitch(pitch);
		}
	}
}