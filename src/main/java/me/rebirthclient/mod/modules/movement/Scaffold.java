package me.rebirthclient.mod.modules.movement;

import me.rebirthclient.api.events.eventbus.EventHandler;
import me.rebirthclient.api.events.eventbus.EventPriority;
import me.rebirthclient.api.events.impl.MoveEvent;
import me.rebirthclient.api.events.impl.RotateEvent;
import me.rebirthclient.api.util.*;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.settings.impl.BooleanSetting;
import me.rebirthclient.mod.settings.impl.SliderSetting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class Scaffold extends Module {
    private final BooleanSetting tower =
            add(new BooleanSetting("Tower"));
    private final BooleanSetting rotate =
            add(new BooleanSetting("Rotate"));
    public SliderSetting rotateTime = add(new SliderSetting("KeepRotate", 0, 3000, 10));

    public Scaffold() {
        super("Scaffold", Category.Movement);
    }
    private final Timer timer = new Timer();
    private final Timer lastTimer = new Timer();

    private float[] angle = null;

    @EventHandler(priority =  EventPriority.HIGH)
    public void onRotation(RotateEvent event) {
        if (rotate.getValue() && !timer.passedMs(rotateTime.getValueInt()) && angle != null) {
            event.setYaw(angle[0]);
            event.setPitch(angle[1]);
        }
    }

    private final Timer timer2 = new Timer();
    private BlockPos lastPos;
    @Override
    public void onUpdate() {
        if (nullCheck()) return;
        if (!tower.getValue()) return;
        if (mc.options.jumpKey.isPressed() && !MovementUtil.isMoving()) {
            if (lastTimer.passed(500)) {
                lastTimer.reset();
                lastPos = null;
            }
            if (timer2.passed(3000)) {
                MovementUtil.setMotionY(-0.28);
                timer2.reset();
                lastPos = null;
            } else {
                if (lastPos == null || lastPos.equals(EntityUtil.getPlayerPos())) {
                    lastPos = EntityUtil.getPlayerPos().up();
                    MovementUtil.setMotionY(0.42);
                    MovementUtil.setMotionX(0);
                    MovementUtil.setMotionZ(0);
                }
            }
        } else {
            timer2.reset();
            lastPos = null;
        }
    }

    @EventHandler
    public void onMove(MoveEvent event) {
        int block = InventoryUtil.findBlock();
        if (block == -1) return;
        BlockPos placePos = EntityUtil.getPlayerPos().down();
        if (BlockUtil.clientCanPlace(placePos, false)) {
            int old = mc.player.getInventory().selectedSlot;
            if (BlockUtil.getPlaceSide(placePos) == null) {
                double distance = 1000;
                BlockPos bestPos = null;
                for (Direction i : Direction.values()) {
                    if (i == Direction.UP) continue;
                    if (BlockUtil.canPlace(placePos.offset(i))) {
                        if (bestPos == null || mc.player.squaredDistanceTo(placePos.offset(i).toCenterPos()) < distance) {
                            bestPos = placePos.offset(i);
                            distance = mc.player.squaredDistanceTo(placePos.offset(i).toCenterPos());
                        }
                    }
                }
                if (bestPos != null) {
                    placePos = bestPos;
                } else {
                    return;
                }
            }
            if (rotate.getValue()) {
                Direction side = BlockUtil.getPlaceSide(placePos);
                angle = EntityUtil.getLegitRotations(placePos.offset(side).toCenterPos().add(side.getOpposite().getVector().getX() * 0.5, side.getOpposite().getVector().getY() * 0.5, side.getOpposite().getVector().getZ() * 0.5));
                timer.reset();
            }
            InventoryUtil.doSwap(block);
            BlockUtil.placeBlock(placePos, rotate.getValue(), false);
            InventoryUtil.doSwap(old);
            lastTimer.reset();
        }
    }
}
