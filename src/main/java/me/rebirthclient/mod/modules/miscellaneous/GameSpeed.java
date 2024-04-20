/**
 * Timer Module
 */
package me.rebirthclient.mod.modules.miscellaneous;

import me.rebirthclient.Rebirth;
import me.rebirthclient.api.events.eventbus.EventHandler;
import me.rebirthclient.api.events.impl.PacketEvent;
import me.rebirthclient.api.events.impl.RotateEvent;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.combat.AutoEXP;
import me.rebirthclient.mod.settings.impl.BooleanSetting;
import me.rebirthclient.mod.settings.impl.SliderSetting;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class GameSpeed extends Module {
	private final SliderSetting multiplier = add(new SliderSetting("Speed", 0.1f, 10f, 0.1f));
	public final BooleanSetting rotateControl =
			add(new BooleanSetting("RotateControl"));
	public static GameSpeed INSTANCE;
	public GameSpeed() {
		super("GameSpeed", Category.Miscellaneous);
		this.setDescription("Increases the speed of Minecraft.");
		INSTANCE = this;
	}

	private final me.rebirthclient.api.util.Timer packetListReset = new me.rebirthclient.api.util.Timer();
	private int normalLookPos;
	private int rotationMode;
	private int normalPos;
	private float lastPitch;
	private float lastYaw;

	public static float nextFloat(final float startInclusive, final float endInclusive) {
		return (startInclusive == endInclusive || endInclusive - startInclusive <= 0.0f) ? startInclusive : ((float) (startInclusive + (endInclusive - startInclusive) * Math.random()));
	}

	@EventHandler
	public final void onPacketSend(final PacketEvent.Send event) {
		if (nullCheck()) return;
		if (event.getPacket() instanceof PlayerMoveC2SPacket.PositionAndOnGround && rotationMode == 1) {
			normalPos++;
			if (normalPos > 20) {
				rotationMode = 2;
			}
		} else if (event.getPacket() instanceof PlayerMoveC2SPacket.Full && rotationMode == 2) {
			normalLookPos++;
			if (normalLookPos > 20) {
				rotationMode = 1;
			}
		}
	}

	public void onDisable() {
		Rebirth.TIMER.reset();
	}

	public void onEnable() {
		Rebirth.TIMER.reset();
		if (nullCheck()) return;
		lastYaw = mc.player.getYaw();
		lastPitch = mc.player.getPitch();
		packetListReset.reset();
	}

	@Override
	public void onRender3D(MatrixStack matrixStack, float partialTicks) {
		if (packetListReset.passedMs(1000L)) {
			normalPos = 0;
			normalLookPos = 0;
			rotationMode = 1;
			lastYaw = mc.player.getYaw();
			lastPitch = mc.player.getPitch();
			packetListReset.reset();
		}
		if (lastPitch > 85) lastPitch = 85;
		if (AutoEXP.INSTANCE.isThrow() && AutoEXP.INSTANCE.down.getValue()) {
			lastPitch = 85;
		}
		Rebirth.TIMER.set(multiplier.getValueFloat());
	}

	@EventHandler
	public final void RotateEvent(RotateEvent event) {
		if (rotateControl.getValue()) {
			switch (rotationMode) {
				case 1 -> event.setRotation(lastYaw, lastPitch);
				case 2 -> event.setRotation(lastYaw + nextFloat(1.0f, 3.0f), lastPitch + nextFloat(1.0f, 3.0f));
			}
		}
	}
}