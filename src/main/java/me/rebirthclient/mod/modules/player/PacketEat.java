package me.rebirthclient.mod.modules.player;

import me.rebirthclient.api.events.eventbus.EventHandler;
import me.rebirthclient.api.events.impl.PacketEvent;
import me.rebirthclient.mod.modules.Module;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;

public class PacketEat extends Module {
	public static PacketEat INSTANCE;
	public PacketEat() {
		super("PacketEat", Category.Player);
		INSTANCE = this;
	}

	@EventHandler
	public void onPacket(PacketEvent.Send event) {
		if (event.getPacket() instanceof PlayerActionC2SPacket packet && packet.getAction() == PlayerActionC2SPacket.Action.RELEASE_USE_ITEM && mc.player.getActiveItem().getItem().isFood()) {
			event.cancel();
		}
	}
}