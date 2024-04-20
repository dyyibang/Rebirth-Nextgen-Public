package me.rebirthclient.mod.modules.combat;

import me.rebirthclient.Rebirth;
import me.rebirthclient.api.events.eventbus.EventHandler;
import me.rebirthclient.api.events.eventbus.EventPriority;
import me.rebirthclient.api.events.impl.RotateEvent;
import me.rebirthclient.api.util.CombatUtil;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.asm.accessors.ILivingEntity;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.settings.impl.BooleanSetting;
import me.rebirthclient.mod.settings.impl.EnumSetting;
import me.rebirthclient.mod.settings.impl.SliderSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class Aura extends Module {

    public static Aura INSTANCE;
    public static Entity target;
    public final EnumSetting page = add(new EnumSetting("Page", Page.General));

    public final SliderSetting range =
            add(new SliderSetting("Range", 0.1f, 7.0f, v -> page.getValue() == Page.General));
    private final SliderSetting cooldown =
            add(new SliderSetting("Cooldown", 0f, 1.2f, 0.01, v -> page.getValue() == Page.General));
     private final SliderSetting wallRange =
            add(new SliderSetting("WallRange", 0.1f, 7.0f, v -> page.getValue() == Page.General));
    private final BooleanSetting whileEating =
            add(new BooleanSetting("WhileUsing", v -> page.getValue() == Page.General));
    private final BooleanSetting weaponOnly =
            add(new BooleanSetting("WeaponOnly", v -> page.getValue() == Page.General));
    private final BooleanSetting rotate =
            add(new BooleanSetting("Rotate", v -> page.getValue() == Page.Rotate).setParent());
    private final BooleanSetting newRotate =
            add(new BooleanSetting("NewRotate", v -> rotate.isOpen() && page.getValue() == Page.Rotate));
    private final SliderSetting yawStep =
            add(new SliderSetting("YawStep", 0.1f, 1.0f, v -> rotate.isOpen() && newRotate.getValue() && page.getValue() == Page.Rotate));
    private final BooleanSetting checkLook =
            add(new BooleanSetting("CheckLook", v -> rotate.isOpen() && newRotate.getValue() && page.getValue() == Page.Rotate));
    private final SliderSetting fov =
            add(new SliderSetting("Fov", 0f, 30f, v -> rotate.isOpen() && newRotate.getValue() && checkLook.getValue() && page.getValue() == Page.Rotate));

    private final EnumSetting targetMode =
            add(new EnumSetting("Filter", TargetMode.DISTANCE, v -> page.getValue() == Page.Target));
    public final BooleanSetting Players = add(new BooleanSetting("Players", v -> page.getValue() == Page.Target));
    public final BooleanSetting Mobs = add(new BooleanSetting("Mobs", v -> page.getValue() == Page.Target));
    public final BooleanSetting Animals = add(new BooleanSetting("Animals", v -> page.getValue() == Page.Target));
    public final BooleanSetting Villagers = add(new BooleanSetting("Villagers", v -> page.getValue() == Page.Target));
    public final BooleanSetting Slimes = add(new BooleanSetting("Slimes", v -> page.getValue() == Page.Target));
    public Vec3d directionVec = null;
    private float lastYaw = 0;
    private float lastPitch = 0;
    public Aura() {
        super("Aura", "Attacks players in radius", Category.Combat);
        INSTANCE = this;
    }

    @Override
    public void onDisable() {
        lastYaw = Rebirth.RUN.lastYaw;
        lastPitch = Rebirth.RUN.lastPitch;
    }

    @Override
    public void onUpdate() {
        if (check()) {
            target = getTarget();
            if (target == null) {
                return;
            }
            doAura();
        } else {
            target = null;
        }
    }

    @EventHandler(priority =  EventPriority.HIGH - 2)
    public void onRotate(RotateEvent event) {
        if (target != null && newRotate.getValue() && directionVec != null) {
            float[] newAngle = injectStep(EntityUtil.getLegitRotations(directionVec), yawStep.getValueFloat());
            lastYaw = newAngle[0];
            lastPitch = newAngle[1];
            event.setYaw(lastYaw);
            event.setPitch(lastPitch);
        } else {
            lastYaw = Rebirth.RUN.lastYaw;
            lastPitch = Rebirth.RUN.lastPitch;
        }
    }
    private boolean check() {
        if (weaponOnly.getValue() && !EntityUtil.isHoldingWeapon(mc.player)) {
            return false;
        }
        if (!(Math.max(((ILivingEntity) mc.player).getLastAttackedTicks() / getAttackCooldownProgressPerTick(), 0.0F) >= cooldown.getValue()))
            return false;
        return whileEating.getValue() || !mc.player.isUsingItem();
    }

    public static float getAttackCooldownProgressPerTick() {
        return (float) (1.0 / mc.player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_SPEED) * 20.0);
    }

    private void doAura() {
        if (!check()) {
            return;
        }
        if (rotate.getValue()) {
            if (!faceVector(target.getPos().add(0, 1.5, 0))) return;
        }
        if (Criticals.INSTANCE.isOn()) Criticals.INSTANCE.doCrit();
        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    public boolean faceVector(Vec3d directionVec) {
        if (!newRotate.getValue()) {
            EntityUtil.faceVector(directionVec);
            return true;
        } else {
            this.directionVec = directionVec;
            float[] angle = EntityUtil.getLegitRotations(directionVec);
            if (Math.abs(MathHelper.wrapDegrees(angle[0] - lastYaw)) < fov.getValueFloat() && Math.abs(MathHelper.wrapDegrees(angle[1] - lastPitch)) < fov.getValueFloat()) {
                EntityUtil.sendYawAndPitch(angle[0], angle[1]);
                return true;
            }
        }
        return !checkLook.getValue();
    }
    
    private Entity getTarget() {
        Entity target = null;
        double distance = range.getValue();
        double maxHealth = 36.0;
        for (Entity entity : mc.world.getEntities()) {
            if (!isEnemy(entity)) continue;
            if (!mc.player.canSee(entity) && mc.player.distanceTo(entity) > wallRange.getValue()) {
                continue;
            }
            if (!CombatUtil.isValid(entity, range.getValue())) continue;

            if (target == null) {
                target = entity;
                distance = mc.player.distanceTo(entity);
                maxHealth = EntityUtil.getHealth(entity);
            } else {
                if (entity instanceof PlayerEntity && EntityUtil.isArmorLow((PlayerEntity) entity, 10)) {
                    target = entity;
                    break;
                }
                if (targetMode.getValue() == TargetMode.HEALTH && EntityUtil.getHealth(entity) < maxHealth) {
                    target = entity;
                    maxHealth = EntityUtil.getHealth(entity);
                    continue;
                }
                if (targetMode.getValue() == TargetMode.DISTANCE && mc.player.distanceTo(entity) < distance) {
                    target = entity;
                    distance = mc.player.distanceTo(entity);
                }
            }
        }
        return target;
    }
    private boolean isEnemy(Entity entity) {
        if (entity instanceof SlimeEntity && Slimes.getValue()) return true;
        if (entity instanceof PlayerEntity && Players.getValue()) return true;
        if (entity instanceof VillagerEntity && Villagers.getValue()) return true;
        if (!(entity instanceof VillagerEntity) && entity instanceof MobEntity && Mobs.getValue()) return true;
        return entity instanceof AnimalEntity && Animals.getValue();
    }

    private float[] injectStep(float[] angle, float steps) {
        if (steps < 0.1f) steps = 0.1f;

        if (steps > 1) steps = 1;

        if (steps < 1 && angle != null) {
            float packetYaw = lastYaw;
            float diff = MathHelper.wrapDegrees(angle[0] - packetYaw);

            if (Math.abs(diff) > 90 * steps) {
                angle[0] = (packetYaw + (diff * ((90 * steps) / Math.abs(diff))));
            }

            float packetPitch = lastPitch;
            diff = angle[1] - packetPitch;
            if (Math.abs(diff) > 90 * steps) {
                angle[1] = (packetPitch + (diff * ((90 * steps) / Math.abs(diff))));
            }
        }

        return new float[]{
                angle[0],
                angle[1]
        };
    }
    private enum TargetMode {
        DISTANCE,
        HEALTH,
    }

    public enum Page {
        General,
        Rotate,
        Target
    }
}