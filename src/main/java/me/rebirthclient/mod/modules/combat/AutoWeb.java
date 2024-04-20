package me.rebirthclient.mod.modules.combat;

import me.rebirthclient.Rebirth;
import me.rebirthclient.api.events.eventbus.EventHandler;
import me.rebirthclient.api.events.eventbus.EventPriority;
import me.rebirthclient.api.events.impl.RotateEvent;
import me.rebirthclient.api.util.*;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.settings.impl.BooleanSetting;
import me.rebirthclient.mod.settings.impl.EnumSetting;
import me.rebirthclient.mod.settings.impl.SliderSetting;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;

import static me.rebirthclient.api.util.BlockUtil.*;

public class AutoWeb extends Module {
    public AutoWeb() {
        super("AutoWeb", Category.Combat);
    }

    public final EnumSetting page = add(new EnumSetting("Page", Page.General));
    public final SliderSetting delay =
            add(new SliderSetting("Delay", 0, 500, v -> page.getValue() == Page.General));
    public final SliderSetting multiPlace =
            add(new SliderSetting("MultiPlace", 1, 10, v -> page.getValue() == Page.General));
    public final SliderSetting predictTicks =
            add(new SliderSetting("PredictTicks", 0.0, 50, 1, v -> page.getValue() == Page.General));
    private final BooleanSetting checkMine =
            add(new BooleanSetting("CheckMine", v -> page.getValue() == Page.General));
    private final BooleanSetting face =
            add(new BooleanSetting("Face", v -> page.getValue() == Page.General));
    private final BooleanSetting leg =
            add(new BooleanSetting("Leg", v -> page.getValue() == Page.General));
    private final BooleanSetting down =
            add(new BooleanSetting("Down", v -> page.getValue() == Page.General));
    private final BooleanSetting usingPause =
            add(new BooleanSetting("UsingPause", v -> page.getValue() == Page.General));
    public final SliderSetting placeRange =
            add(new SliderSetting("PlaceRange", 0.0, 6.0, 0.1, v -> page.getValue() == Page.General));
    public final SliderSetting targetRange =
            add(new SliderSetting("TargetRange", 0.0, 8.0, 0.1, v -> page.getValue() == Page.General));

    private final BooleanSetting rotate =
            add(new BooleanSetting("Rotate", v -> page.getValue() == Page.Rotate).setParent());
    private final SliderSetting yawStep =
            add(new SliderSetting("YawStep", 0.1f, 1.0f, 0.01f, v -> rotate.isOpen() && page.getValue() == Page.Rotate));
    private final BooleanSetting checkLook =
            add(new BooleanSetting("CheckLook", v -> rotate.isOpen() && page.getValue() == Page.Rotate));
    private final SliderSetting fov =
            add(new SliderSetting("Fov", 0f, 30f, v -> rotate.isOpen() && checkLook.getValue() && page.getValue() == Page.Rotate));

    private final Timer timer = new Timer();
    public Vec3d directionVec = null;
    private float lastYaw = 0;
    private float lastPitch = 0;

    @EventHandler(priority = EventPriority.HIGH - 2)
    public void onRotate(RotateEvent event) {
        if (directionVec != null) {
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

    int progress = 0;

    private final ArrayList<BlockPos> pos = new ArrayList<>();
    @Override
    public void onUpdate() {
        pos.clear();
        progress = 0;
        if (!timer.passedMs(delay.getValueInt())) {
            return;
        }
        directionVec = null;
        if (getWebSlot() == -1) {
            return;
        }
        if (usingPause.getValue() && mc.player.isUsingItem()) {
            return;
        }
        for (PlayerEntity player : CombatUtil.getEnemies(targetRange.getValue())) {
            Vec3d playerPos = predictTicks.getValue() > 0 ? CombatUtil.getEntityPosVec(player, predictTicks.getValueInt()) : player.getPos();
            if (leg.getValue()) {
                placeWeb(new BlockPosX(playerPos.getX(), playerPos.getY(), playerPos.getZ()));
            }
            if (down.getValue()) {
                placeWeb(new BlockPosX(playerPos.getX(), playerPos.getY() - 0.8, playerPos.getZ()));
            }
            if (face.getValue()) {
                placeWeb(new BlockPosX(playerPos.getX(), playerPos.getY() + 1.2, playerPos.getZ()));
            }
        }
    }
    private boolean placeWeb(BlockPos pos) {
        if (this.pos.contains(pos)) return false;
        this.pos.add(pos);
        if (progress >= multiPlace.getValueInt()) return false;
        if (getWebSlot() == -1) {
            return false;
        }
        if (checkMine.getValue() && Rebirth.BREAK.isMining(pos)) return false;
        if (BlockUtil.getPlaceSide(pos, placeRange.getValue()) != null && mc.world.isAir(pos)) {
            int oldSlot = mc.player.getInventory().selectedSlot;
            int webSlot = getWebSlot();
            if (!placeBlock(pos, rotate.getValue(), webSlot)) return false;
            if (pos.equals(PacketMine.breakPos)) {
                PacketMine.breakPos = null;
            }
            progress++;
            doSwap(oldSlot);
            timer.reset();
            return true;
        }
        return false;
    }

    public boolean placeBlock(BlockPos pos, boolean rotate, int slot) {
        if (airPlace()) {
            for (Direction i : Direction.values()) {
                if (mc.world.isAir(pos.offset(i))) {
                    return clickBlock(pos, i, rotate, slot);
                }
            }
        }
        Direction side = getPlaceSide(pos);
        if (side == null) return false;
        BlockUtil.placedPos.add(pos);
        return clickBlock(pos.offset(side), side.getOpposite(), rotate, slot);
    }

    public boolean clickBlock(BlockPos pos, Direction side, boolean rotate, int slot) {
        Vec3d directionVec = new Vec3d(pos.getX() + 0.5 + side.getVector().getX() * 0.5, pos.getY() + 0.5 + side.getVector().getY() * 0.5, pos.getZ() + 0.5 + side.getVector().getZ() * 0.5);
        if (rotate) {
            if (!faceVector(directionVec)) return false;
        }
        doSwap(slot);
        mc.player.swingHand(Hand.MAIN_HAND);
        BlockHitResult result = new BlockHitResult(directionVec, side, pos, false);
        mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, result, getWorldActionId(mc.world)));
        return true;
    }

    private boolean faceVector(Vec3d directionVec) {
        this.directionVec = directionVec;
        float[] angle = EntityUtil.getLegitRotations(directionVec);
        if (Math.abs(MathHelper.wrapDegrees(angle[0] - lastYaw)) < fov.getValueFloat() && Math.abs(MathHelper.wrapDegrees(angle[1] - lastPitch)) < fov.getValueFloat()) {
            EntityUtil.sendYawAndPitch(angle[0], angle[1]);
            return true;
        }
        return !checkLook.getValue();
    }

    private float[] injectStep(float[] angle, float steps) {
        if (steps < 0.01f) steps = 0.01f;

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

    private void doSwap(int slot) {
        InventoryUtil.doSwap(slot);
    }

    private int getWebSlot() {
        return InventoryUtil.findBlock(Blocks.COBWEB);
    }

    public enum Page {
        General,
        Rotate
    }
}