/**
 * NoFall Module
 */
package me.rebirthclient.mod.modules.movement;

import me.rebirthclient.api.events.eventbus.EventHandler;
import me.rebirthclient.api.events.impl.PacketEvent;
import me.rebirthclient.asm.accessors.IPlayerMoveC2SPacket;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.settings.impl.SliderSetting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class NoFall extends Module {
	private final SliderSetting distance =
			new SliderSetting("Distance", 0.0f, 8.0f, 0.1);
	public NoFall() {
		super("NoFall", Category.Movement);
		this.setDescription("Prevents fall damage.");
		add(distance);
	}

	@Override
	public void onUpdate() {
		for (ItemStack is : mc.player.getArmorItems()) {
			if (is.getItem() == Items.ELYTRA) {
				return;
			}
		}
		if(mc.player.fallDistance >= distance.getValue() - 0.1) {
			//mc.player.networkHandler.sendPacket(new OnGroundOnly(true));
		}
	}

	@EventHandler
	public void onPacketSend(PacketEvent.Send event) {
		if (nullCheck()) {
			return;
		}
		for (ItemStack is : mc.player.getArmorItems()) {
			if (is.getItem() == Items.ELYTRA) {
				return;
			}
		}
		if (event.getPacket() instanceof PlayerMoveC2SPacket packet) {
			if (mc.player.fallDistance >= (float) this.distance.getValue()) {
				((IPlayerMoveC2SPacket) packet).setOnGround(true);
			}
		}
	}
}
