package me.rebirthclient.api.managers;

import me.rebirthclient.Rebirth;
import me.rebirthclient.api.events.Event;
import me.rebirthclient.api.events.eventbus.EventHandler;
import me.rebirthclient.api.events.eventbus.EventPriority;
import me.rebirthclient.api.events.impl.PacketEvent;
import me.rebirthclient.api.events.impl.RotateEvent;
import me.rebirthclient.api.events.impl.UpdateWalkingEvent;
import me.rebirthclient.api.events.impl.WorldBreakEvent;
import me.rebirthclient.api.util.*;
import me.rebirthclient.asm.accessors.IPlayerMoveC2SPacket;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.combat.surround.Surround;
import net.minecraft.client.util.Session;
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;

public class RunManager implements Wrapper {
    public RunManager() {
        Rebirth.EVENT_BUS.subscribe(this);
    }

    public final HashMap<Integer, BlockPos> breakMap = new HashMap<>();
    @EventHandler
    public void onWorldBreak(WorldBreakEvent event) {
        if (event.getId() == mc.player.getId()) return;
        breakMap.put(event.getId(), event.getPos());
    }
    public float preYaw = 0;
    public float prePitch = 0;
    @EventHandler(priority = EventPriority.HIGH + 1)
    public void onUpdateWalking(UpdateWalkingEvent event) {
        if (mc.player == null) return;
        if (Surround.INSTANCE.enableInHole.getValue() && !Surround.INSTANCE.isOn() && BlockUtil.isHole(EntityUtil.getPlayerPos())) {
            Surround.INSTANCE.enable();
        }
        if (event.getStage() == Event.Stage.Pre) {
            preYaw = mc.player.getYaw();
            prePitch = mc.player.getPitch();
            RotateEvent rotateEvent = new RotateEvent(preYaw, prePitch);
            Rebirth.EVENT_BUS.post(rotateEvent);
            mc.player.setYaw(rotateEvent.getYaw());
            mc.player.setPitch(rotateEvent.getPitch());
        } else if (event.getStage() == Event.Stage.Post) {
            mc.player.setYaw(preYaw);
            mc.player.setPitch(prePitch);
        }
    }
    public static final Timer ROTATE_TIMER = new Timer();
    public static Vec3d directionVec = null;
    @EventHandler(priority =  EventPriority.HIGH + 1)
    public void onRotation(RotateEvent event) {
        if (mc.player == null) return;
        if (directionVec != null && !ROTATE_TIMER.passed((long) (Rebirth.HUD.rotateTime.getValue() * 1000))) {
            float[] angle = EntityUtil.getLegitRotations(directionVec);
            event.setYaw(angle[0]);
            event.setPitch(angle[1]);
        }
    }
    private static float renderPitch;
    private static float renderYawOffset;
    private static float prevPitch;
    private static float prevRenderYawOffset;
    private static float prevRotationYawHead;
    private static float rotationYawHead;
    private int ticksExisted;
    public float lastYaw = 0;
    public float lastPitch = 0;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPacketSend(PacketEvent.Send event) {
        if (event.isCancel()) return;
        if (directionVec != null && !ROTATE_TIMER.passed((long) (Rebirth.HUD.rotateTime.getValue() * 1000)) && !EntityUtil.rotating && Rebirth.HUD.rotatePlus.getValue()) {
            if (event.getPacket() instanceof PlayerMoveC2SPacket packet) {
                if (!packet.changesLook()) return;
                float yaw = packet.getYaw(114514);
                float pitch = packet.getPitch(114514);
                if (yaw == mc.player.getYaw() && pitch == mc.player.getPitch()) {
                    float[] angle = EntityUtil.getLegitRotations(directionVec);
                    ((IPlayerMoveC2SPacket) event.getPacket()).setYaw(angle[0]);
                    ((IPlayerMoveC2SPacket) event.getPacket()).setPitch(angle[1]);
                }
            }
        }
        if (event.getPacket() instanceof PlayerMoveC2SPacket packet) {
            float yaw = packet.getYaw(114514);
            float pitch = packet.getPitch(114514);
            if (yaw == 114514 || pitch == 114514) return;
            lastYaw = yaw;
            lastPitch = pitch;
            set(lastYaw, lastPitch);
        }
    }


    @EventHandler(priority = EventPriority.HIGH)
    public void onReceivePacket(PacketEvent.Receive event) {
        if (event.getPacket() instanceof PlayerPositionLookS2CPacket packet) {
            lastYaw = packet.getYaw();
            lastPitch = packet.getPitch();
            set(packet.getYaw(), packet.getPitch());
        }
    }
    @EventHandler
    public void onUpdateWalkingPre(UpdateWalkingEvent event) {
        set(lastYaw, lastPitch);
    }
    private void set(float yaw, float pitch) {
        if (Module.nullCheck()) return;
        if (mc.player.age == ticksExisted) {
            return;
        }

        ticksExisted = mc.player.age;
        prevPitch = renderPitch;

        prevRenderYawOffset = renderYawOffset;
        renderYawOffset = getRenderYawOffset(yaw, prevRenderYawOffset);

        prevRotationYawHead = rotationYawHead;
        rotationYawHead = yaw;

        renderPitch = pitch;
    }

    public static float getRenderPitch() {
        return renderPitch;
    }

    public static float getRotationYawHead() {
        return rotationYawHead;
    }

    public static float getRenderYawOffset() {
        return renderYawOffset;
    }

    public static float getPrevPitch() {
        return prevPitch;
    }

    public static float getPrevRotationYawHead() {
        return prevRotationYawHead;
    }

    public static float getPrevRenderYawOffset() {
        return prevRenderYawOffset;
    }

    private float getRenderYawOffset(float yaw, float offsetIn) {
        float result = offsetIn;
        float offset;

        double xDif = mc.player.getX() - mc.player.prevX;
        double zDif = mc.player.getZ() - mc.player.prevZ;

        if (xDif * xDif + zDif * zDif > 0.0025000002f) {
            offset = (float) MathHelper.atan2(zDif, xDif) * 57.295776f - 90.0f;
            float wrap = MathHelper.abs(MathHelper.wrapDegrees(yaw) - offset);
            if (95.0F < wrap && wrap < 265.0F) {
                result = offset - 180.0F;
            } else {
                result = offset;
            }
        }

        if (mc.player.handSwingProgress > 0.0F) {
            result = yaw;
        }

        result = offsetIn + MathHelper.wrapDegrees(result - offsetIn) * 0.3f;
        offset = MathHelper.wrapDegrees(yaw - result);

        if (offset < -75.0f) {
            offset = -75.0f;
        } else if (offset >= 75.0f) {
            offset = 75.0f;
        }

        result = yaw - offset;
        if (offset * offset > 2500.0f) {
            result += offset * 0.2f;
        }

        return result;
    }
}
