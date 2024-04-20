package me.rebirthclient.mod.modules.combat;

import me.rebirthclient.Rebirth;
import me.rebirthclient.api.managers.CommandManager;
import me.rebirthclient.api.util.*;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.settings.impl.BooleanSetting;
import me.rebirthclient.mod.settings.impl.EnumSetting;
import me.rebirthclient.mod.settings.impl.SliderSetting;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

import static me.rebirthclient.api.util.BlockUtil.canReplace;

public class Burrow extends Module {
    public static Burrow INSTANCE;
    public final BooleanSetting enderChest =
            add(new BooleanSetting("EnderChest"));
    public final SliderSetting multiPlace =
            add(new SliderSetting("MultiPlace", 1, 4, 1));
    private final EnumSetting rotate = add(new EnumSetting("RotateMode", RotateMode.Bypass));
    private final BooleanSetting breakCrystal =
            add(new BooleanSetting("Break"));
    private final BooleanSetting wait =
            add(new BooleanSetting("Wait"));
    private final BooleanSetting aboveHead = add(new BooleanSetting("AboveHead").setParent());
    private final BooleanSetting center = add(new BooleanSetting("Center", v -> aboveHead.isOpen()));
    private final BooleanSetting inventory =
            add(new BooleanSetting("InventorySwap"));
    private final EnumSetting lagMode = add(new EnumSetting("LagMode", LagBackMode.Normal));
    int progress = 0;
    List<BlockPos> placePos = new ArrayList<>();
    BlockPos movedPos = null;
    public Burrow() {
        super("Burrow", Category.Combat);
        INSTANCE = this;
    }

    @Override
    public void onUpdate() {
        movedPos = null;
        if (!mc.player.isOnGround()) {
            return;
        }
        if (AutoPush.isInWeb(mc.player)) {
            return;
        }
        int oldSlot = mc.player.getInventory().selectedSlot;
        int block;
        if ((block = getBlock()) == -1) {
            CommandManager.sendChatMessage("\u00a7e[?] \u00a7c\u00a7oObsidian" + (enderChest.getValue() ? "/EnderChest" : "") + "?");
            disable();
            return;
        }
        BlockPos pos1 = new BlockPosX(mc.player.getX() + 0.3, mc.player.getY() + 0.5, mc.player.getZ() + 0.3);
        BlockPos pos2 = new BlockPosX(mc.player.getX() - 0.3, mc.player.getY() + 0.5, mc.player.getZ() + 0.3);
        BlockPos pos3 = new BlockPosX(mc.player.getX() + 0.3, mc.player.getY() + 0.5, mc.player.getZ() - 0.3);
        BlockPos pos4 = new BlockPosX(mc.player.getX() - 0.3, mc.player.getY() + 0.5, mc.player.getZ() - 0.3);
        BlockPos playerPos = EntityUtil.getPlayerPos(true);
        if (!canPlace(pos1) && !canPlace(pos2) && !canPlace(pos3) && !canPlace(pos4)) {
            if (!wait.getValue()) disable();
            return;
        }
        boolean above = false;
        BlockPos headPos = EntityUtil.getPlayerPos().up(2);
        boolean rotate = this.rotate.getValue() == RotateMode.Normal;
        CombatUtil.attackCrystal(mc.player.getBoundingBox(), rotate, false);
        if (mc.player.isInSneakingPose() || Trapped(headPos) || Trapped(headPos.add(1, 0, 0)) || Trapped(headPos.add(-1, 0, 0)) || Trapped(headPos.add(0, 0, 1)) || Trapped(headPos.add(0, 0, -1)) || Trapped(headPos.add(1, 0, -1)) || Trapped(headPos.add(-1, 0, -1)) || Trapped(headPos.add(1, 0, 1)) || Trapped(headPos.add(-1, 0, 1))) {
            above = true;
            if (!aboveHead.getValue()) {
                if (!wait.getValue()) disable();
                return;
            }
            boolean moved = false;
            BlockPos offPos = playerPos;
            if (checkSelf(offPos) && !canReplace(offPos)) {
                gotoPos(offPos);
            } else {
                for (final Direction facing : Direction.values()) {
                    if (facing == Direction.UP || facing == Direction.DOWN) continue;
                    offPos = playerPos.offset(facing);
                    if (checkSelf(offPos) && !canReplace(offPos)) {
                        gotoPos(offPos);
                        moved = true;
                        break;
                    }
                }
                if (!moved) {
                    for (final Direction facing : Direction.values()) {
                        if (facing == Direction.UP || facing == Direction.DOWN) continue;
                        offPos = playerPos.offset(facing);
                        if (checkSelf(offPos)) {
                            gotoPos(offPos);
                            moved = true;
                            break;
                        }
                    }
                    if (!moved) {
                        if (!center.getValue()) {
                            return;
                        }
                        for (final Direction facing : Direction.values()) {
                            if (facing == Direction.UP || facing == Direction.DOWN) continue;
                            offPos = playerPos.offset(facing);
                            if (canGoto(offPos)) {
                                gotoPos(offPos);
                                moved = true;
                                break;
                            }
                        }
                        if (!moved) {
                            if (!wait.getValue()) disable();
                            return;
                        }
                    }
                }
            }
        } else {
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 0.4199999868869781, mc.player.getZ(), false));
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 0.7531999805212017, mc.player.getZ(), false));
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 0.9999957640154541, mc.player.getZ(), false));
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 1.1661092609382138, mc.player.getZ(), false));
        }
        doSwap(block);
        progress = 0;
        placePos.clear();
        if (this.rotate.getValue() == RotateMode.Bypass) {
            EntityUtil.sendYawAndPitch(mc.player.getYaw(), 90);
        }
        placeBlock(playerPos, rotate);
        placeBlock(pos1, rotate);
        placeBlock(pos2, rotate);
        placeBlock(pos3, rotate);
        placeBlock(pos4, rotate);
        if (inventory.getValue()) {
            doSwap(block);
            EntityUtil.sync();
        } else {
            doSwap(oldSlot);
        }
        switch (above ? LagBackMode.Strict : (LagBackMode) lagMode.getValue()) {
            case Strict -> {
                double distance = 0;
                BlockPos bestPos = null;
                for (int i = 0; i < 10; i++) {
                    BlockPos pos = EntityUtil.getPlayerPos().up(i);
                    if (!canGoto(pos))
                        continue;
                    if (MathHelper.sqrt((float) mc.player.squaredDistanceTo(pos.toCenterPos())) < 2) continue;
                    if (bestPos == null || mc.player.squaredDistanceTo(pos.toCenterPos()) < distance) {
                        bestPos = pos;
                        distance = mc.player.squaredDistanceTo(pos.toCenterPos());
                    }
                }
                if (bestPos != null) {
                    mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(bestPos.getX() + 0.5, bestPos.getY(), bestPos.getZ() + 0.5, false));
                }
            }
            case Normal -> {
                mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 1.9400880035762786, mc.player.getZ(), false));
                mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() - 1.0, mc.player.getZ(), false));
            }
            case Old ->
                    mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 1.9400880035762786, mc.player.getZ(), false));
            case Void ->
                    mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), -68, mc.player.getZ(), false));
            case OBSTest -> {
                double distance = 0;
                BlockPos bestPos = null;
                for (int i = 5; i < 20; i++) {
                    BlockPos pos = EntityUtil.getPlayerPos().up(i);
                    if (!canGoto(pos))
                        continue;
                    if (MathHelper.sqrt((float) mc.player.squaredDistanceTo(pos.toCenterPos())) < 2) continue;
                    if (bestPos == null || mc.player.squaredDistanceTo(pos.toCenterPos()) < distance) {
                        bestPos = pos;
                        distance = mc.player.squaredDistanceTo(pos.toCenterPos());
                    }
                }
                if (bestPos != null) {
                    mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(bestPos.getX() + 0.5, bestPos.getY(), bestPos.getZ() + 0.5, false));
                }
            }
        }
        disable();
    }

    private void placeBlock(BlockPos pos, boolean rotate) {
        if (canPlace(pos) && !placePos.contains(pos) && progress < multiPlace.getValueInt()) {
            placePos.add(pos);
            progress++;
            Direction side;
            if ((side = BlockUtil.getPlaceSide(pos)) == null) return;
            BlockUtil.placedPos.add(pos);
            BlockUtil.clickBlock(pos.offset(side), side.getOpposite(), rotate);
        }
    }

    private void doSwap(int slot) {
        if (inventory.getValue()) {
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slot, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
        } else {
            InventoryUtil.doSwap(slot);
        }
    }

    private void gotoPos(BlockPos offPos) {
        movedPos = offPos;
        if (Math.abs(offPos.getX() + 0.5 - mc.player.getX()) < Math.abs(offPos.getZ() + 0.5 - mc.player.getZ())) {
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 0.2, mc.player.getZ() + (offPos.getZ() + 0.5 - mc.player.getZ()), true));
        } else {
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX() + (offPos.getX() + 0.2 - mc.player.getX()), mc.player.getY() + 0.2, mc.player.getZ(), true));
        }
    }

    private boolean canGoto(BlockPos pos) {
        return !BlockUtil.getState(pos).blocksMovement() && !BlockUtil.getState(pos.up()).blocksMovement();
    }

    private boolean canPlace(BlockPos pos) {
        if (BlockUtil.getPlaceSide(pos) == null) {
            return false;
        }
        if (!BlockUtil.canReplace(pos)) {
            return false;
        }
        return !hasEntity(pos);
    }

    private boolean hasEntity(BlockPos pos) {
        for (Entity entity : mc.world.getNonSpectatingEntities(Entity.class, new Box(pos))) {
            if (entity == mc.player) continue;
            if (!entity.isAlive() || entity instanceof ItemEntity || entity instanceof ExperienceOrbEntity || entity instanceof ExperienceBottleEntity || entity instanceof ArrowEntity || entity instanceof EndCrystalEntity && breakCrystal.getValue() || entity instanceof ArmorStandEntity && Rebirth.HUD.obsMode.getValue())
                continue;
            return true;
        }
        return false;
    }

    private boolean checkSelf(BlockPos pos) {
        return mc.player.getBoundingBox().intersects(new Box(pos));
    }

    private boolean Trapped(BlockPos pos) {
        return mc.world.canCollide(mc.player, new Box(pos)) && checkSelf(pos.down(2));
    }

    private int getBlock() {
        if (inventory.getValue()) {
            if (InventoryUtil.findBlockInventorySlot(Blocks.OBSIDIAN) != -1 || !enderChest.getValue()) {
                return InventoryUtil.findBlockInventorySlot(Blocks.OBSIDIAN);
            }
            return InventoryUtil.findBlockInventorySlot(Blocks.ENDER_CHEST);
        } else {
            if (InventoryUtil.findBlock(Blocks.OBSIDIAN) != -1 || !enderChest.getValue()) {
                return InventoryUtil.findBlock(Blocks.OBSIDIAN);
            }
            return InventoryUtil.findBlock(Blocks.ENDER_CHEST);
        }
    }

    private enum RotateMode {
        Bypass,
        Normal,
        None
    }

    private enum LagBackMode {
        Normal,
        Strict,
        Void,
        OBSTest,
        Old
    }
}