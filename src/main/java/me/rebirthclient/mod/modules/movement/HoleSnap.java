package me.rebirthclient.mod.modules.movement;

import com.mojang.blaze3d.systems.RenderSystem;
import me.rebirthclient.Rebirth;
import me.rebirthclient.api.events.eventbus.EventHandler;
import me.rebirthclient.api.events.impl.MoveEvent;
import me.rebirthclient.api.events.impl.PacketEvent;
import me.rebirthclient.api.managers.CommandManager;
import me.rebirthclient.api.util.ColorUtil;
import me.rebirthclient.api.util.CombatUtil;
import me.rebirthclient.api.util.MovementUtil;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.miscellaneous.AutoPeek;
import me.rebirthclient.mod.settings.impl.BooleanSetting;
import me.rebirthclient.mod.settings.impl.ColorSetting;
import me.rebirthclient.mod.settings.impl.SliderSetting;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class HoleSnap extends Module {
    public static HoleSnap INSTANCE;
    public BooleanSetting any = add(new BooleanSetting("Any"));
    private final SliderSetting range = this.add(new SliderSetting("Range", 1, 50));
    private final SliderSetting timeoutTicks = this.add(new SliderSetting("TimeOut", 0, 100));
    public final SliderSetting multiplier = add(new SliderSetting("Timer", 0.1, 8, 0.1));

    public final ColorSetting color = add(new ColorSetting("Color", new Color(255, 255, 255, 100)));
    public SliderSetting circleSize = add(new SliderSetting("CircleSize", 0.1f, 2.5f));
    public BooleanSetting fade = add(new BooleanSetting("Fade"));
    public SliderSetting segments = add(new SliderSetting("Segments", 0, 360));
    boolean resetMove = false;
    private BlockPos holePos;
    private int stuckTicks;
    private int enabledTicks;

    public HoleSnap() {
        super("HoleSnap", "HoleSnap", Category.Movement);
        INSTANCE = this;
    }
    @Override
    public void onEnable() {
        resetMove = false;
    }

    @Override
    public void onDisable() {
        if (Rebirth.TIMER.get() == multiplier.getValueFloat()) {
            Rebirth.TIMER.reset();
        }
        if (resetMove) {
            MovementUtil.setMotionX(0);
            MovementUtil.setMotionZ(0);
        }
        this.holePos = null;
        this.stuckTicks = 0;
        this.enabledTicks = 0;
    }

    @EventHandler
    public void onReceivePacket(PacketEvent.Receive event) {
        if (event.getPacket() instanceof PlayerPositionLookS2CPacket) {
            this.disable();
        }
    }

    Vec3d targetPos;

    @EventHandler
    public void onMove(MoveEvent event) {
        if (multiplier.getValue() != 1)
            Rebirth.TIMER.timer = multiplier.getValueFloat();
        ++enabledTicks;
        if (enabledTicks > timeoutTicks.getValue() - 1) {
            disable();
            return;
        }
        if (!mc.player.isAlive() || mc.player.isFallFlying()) {
            disable();
            return;
        }
        if (stuckTicks > 4) {
            disable();
            return;
        }
        holePos = CombatUtil.getHole((float) range.getValue(), true, any.getValue());
        if (holePos == null) {
            CommandManager.sendChatMessage("\u00a7e[!] \u00a7fHoles?");
            disable();
            return;
        }
        Vec3d playerPos = mc.player.getPos();
        targetPos = new Vec3d(holePos.getX() + 0.5, mc.player.getY(), holePos.getZ() + 0.5);
        if (CombatUtil.isDoubleHole(holePos)) {
            Direction facing = CombatUtil.is3Block(holePos);
            if (facing != null) {
                targetPos = targetPos.add(new Vec3d(facing.getVector().getX() * 0.5, facing.getVector().getY() * 0.5, facing.getVector().getZ() * 0.5));
            }
        }

        resetMove = true;
        float rotation = getRotationTo(playerPos, targetPos).x;
        float yawRad = rotation / 180.0f * 3.1415927f;
        double dist = playerPos.distanceTo(targetPos);
        double cappedSpeed = Math.min(0.2873, dist);
        double x = -(float) Math.sin(yawRad) * cappedSpeed;
        double z = (float) Math.cos(yawRad) * cappedSpeed;
        event.setX(x);
        event.setZ(z);
        if (Math.abs(x) < 0.1 && Math.abs(z) < 0.1 && playerPos.y <= holePos.getY() + 0.5) {
            disable();
        }
        if (mc.player.horizontalCollision) {
            stuckTicks++;
        } else {
            stuckTicks = 0;
        }
    }

    @Override
    public void onRender3D(MatrixStack matrixStack, float partialTicks) {
        if (targetPos == null || holePos == null) {
            return;
        }
        GL11.glEnable(GL11.GL_BLEND);
        Color color = this.color.getValue();

        Vec3d pos = new Vec3d(targetPos.x, holePos.getY(), targetPos.getZ());
        if (fade.getValue()) {
            double temp = 0.01;
            for (double i = 0; i < circleSize.getValue(); i += temp) {
                AutoPeek.doCircle(matrixStack, ColorUtil.injectAlpha(color, (int) Math.min(color.getAlpha() * 2 / (circleSize.getValue() / temp), 255)), i, pos, segments.getValueInt());
            }
        } else {
            AutoPeek.doCircle(matrixStack, color, circleSize.getValue(), pos, segments.getValueInt());
        }
        RenderSystem.setShaderColor(1, 1, 1, 1);
        GL11.glDisable(GL11.GL_BLEND);
    }
    public static Vec2f getRotationTo(Vec3d posFrom, Vec3d posTo) {
        Vec3d vec3d = posTo.subtract(posFrom);
        return getRotationFromVec(vec3d);
    }

    private static Vec2f getRotationFromVec(Vec3d vec) {
        double d = vec.x;
        double d2 = vec.z;
        double xz = Math.hypot(d, d2);
        d2 = vec.z;
        double d3 = vec.x;
        double yaw = normalizeAngle(Math.toDegrees(Math.atan2(d2, d3)) - 90.0);
        double pitch = normalizeAngle(Math.toDegrees(-Math.atan2(vec.y, xz)));
        return new Vec2f((float) yaw, (float) pitch);
    }

    private static double normalizeAngle(double angleIn) {
        double angle = angleIn;
        if ((angle %= 360.0) >= 180.0) {
            angle -= 360.0;
        }
        if (angle < -180.0) {
            angle += 360.0;
        }
        return angle;
    }
}