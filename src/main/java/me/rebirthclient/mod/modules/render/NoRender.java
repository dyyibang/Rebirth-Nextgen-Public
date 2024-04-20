/**
 * NoOverlay Module
 */
package me.rebirthclient.mod.modules.render;

import me.rebirthclient.api.events.eventbus.EventHandler;
import me.rebirthclient.api.events.impl.PacketEvent;
import me.rebirthclient.api.events.impl.ParticleEvent;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.settings.Setting;
import me.rebirthclient.mod.settings.impl.BooleanSetting;
import net.minecraft.client.particle.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.thrown.EggEntity;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;

import java.lang.reflect.Field;

public class NoRender extends Module {
	public static NoRender INSTANCE;
	public BooleanSetting potions = new BooleanSetting("Potions");
	public BooleanSetting xp = new BooleanSetting("XP");
	public BooleanSetting arrows = new BooleanSetting("Arrows");
	public BooleanSetting eggs = new BooleanSetting("Eggs");
	public BooleanSetting armor = new BooleanSetting("Armor");
	public BooleanSetting hurtCam = new BooleanSetting("HurtCam");
	public BooleanSetting fireOverlay = new BooleanSetting("FireOverlay");
	public BooleanSetting waterOverlay = new BooleanSetting("WaterOverlay");
	public BooleanSetting blockOverlay = new BooleanSetting("BlockOverlay");
	public BooleanSetting portal = new BooleanSetting("Portal");
	public BooleanSetting totem = new BooleanSetting("Totem");
	public BooleanSetting nausea = new BooleanSetting("Nausea");
	public BooleanSetting blindness = new BooleanSetting("Blindness");
	public BooleanSetting fog = new BooleanSetting("Fog");
	public BooleanSetting darkness = new BooleanSetting("Darkness");
	public BooleanSetting fireEntity = new BooleanSetting("EntityFire");
	public BooleanSetting antiTitle = new BooleanSetting("Title");
	public BooleanSetting antiPlayerCollision = new BooleanSetting("PlayerCollision");
	public BooleanSetting effect = new BooleanSetting("Effect");
	public BooleanSetting elderGuardian = new BooleanSetting("Guardian");
	public BooleanSetting explosions = new BooleanSetting("Explosions");
	public BooleanSetting campFire = new BooleanSetting("CampFire");
	public BooleanSetting fireworks = new BooleanSetting("Fireworks");
	public NoRender() {
		super("NoRender", Category.Render);
		this.setDescription("Disables all overlays and potion effects.");
		INSTANCE = this;
		try {
			for (Field field : NoRender.class.getDeclaredFields()) {
				if (!Setting.class.isAssignableFrom(field.getType()))
					continue;
				Setting setting = (Setting) field.get(this);
				addSetting(setting);
			}
		} catch (Exception e) {
		}
	}

	@EventHandler
	public void onPacketReceive(PacketEvent.Receive event){
		if(event.getPacket() instanceof TitleS2CPacket && antiTitle.getValue()){
			event.setCancel(true);
		}
	}

	@Override
	public void onRender3D(MatrixStack matrixStack, float partialTicks) {
		for(Entity ent : mc.world.getEntities()){
			if(ent instanceof PotionEntity){
				if(potions.getValue())
					mc.world.removeEntity(ent.getId(), Entity.RemovalReason.KILLED);
			}
			if(ent instanceof ExperienceBottleEntity){
				if(xp.getValue())
					mc.world.removeEntity(ent.getId(), Entity.RemovalReason.KILLED);
			}
			if(ent instanceof ArrowEntity){
				if(arrows.getValue())
					mc.world.removeEntity(ent.getId(), Entity.RemovalReason.KILLED);
			}
			if(ent instanceof EggEntity){
				if(eggs.getValue())
					mc.world.removeEntity(ent.getId(), Entity.RemovalReason.KILLED);
			}
		}
	}

	@EventHandler
	public void onParticle(ParticleEvent.AddParticle event) {
		if (elderGuardian.getValue() && event.particle instanceof ElderGuardianAppearanceParticle) {
			event.setCancel(true);
		} else if (explosions.getValue() && event.particle instanceof ExplosionLargeParticle) {
			event.setCancel(true);
		} else if (campFire.getValue() && event.particle instanceof CampfireSmokeParticle) {
			event.setCancel(true);
		} else if (fireworks.getValue() && (event.particle instanceof FireworksSparkParticle.FireworkParticle || event.particle instanceof FireworksSparkParticle.Flash)) {
			event.setCancel(true);
		} else if (effect.getValue() && event.particle instanceof SpellParticle) {
			event.cancel();
		}
	}
}