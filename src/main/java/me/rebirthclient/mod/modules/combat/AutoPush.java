package me.rebirthclient.mod.modules.combat;

import me.rebirthclient.api.util.*;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.settings.impl.BooleanSetting;
import me.rebirthclient.mod.settings.impl.SliderSetting;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.*;

public class AutoPush extends Module {
    private final BooleanSetting rotate = add(new BooleanSetting("Rotate"));
    private final BooleanSetting yawDeceive = add(new BooleanSetting("YawDeceive"));
    private final BooleanSetting pistonPacket = add(new BooleanSetting("PistonPacket"));
    private final BooleanSetting redStonePacket = add(new BooleanSetting("RedStonePacket"));
    private final BooleanSetting noEating = add(new BooleanSetting("NoEating"));
    private final BooleanSetting attackCrystal = add(new BooleanSetting("BreakCrystal"));
    private final BooleanSetting mine = add(new BooleanSetting("Mine"));
    private final BooleanSetting allowWeb = add(new BooleanSetting("AllowWeb"));
    private final SliderSetting updateDelay = add(new SliderSetting("UpdateDelay", 0, 500));
    private final BooleanSetting selfGround = add(new BooleanSetting("SelfGround"));
    private final BooleanSetting onlyGround = add(new BooleanSetting("OnlyGround"));
    private final BooleanSetting checkPiston = add(new BooleanSetting("CheckPiston"));
    private final BooleanSetting autoDisable = add(new BooleanSetting("AutoDisable"));
    private final BooleanSetting pullBack = add(new BooleanSetting("PullBack").setParent());
    private final BooleanSetting onlyBurrow = add(new BooleanSetting("OnlyBurrow", v -> pullBack.isOpen()));
    private final SliderSetting range = add(new SliderSetting("Range", 0.0, 6.0));
    private final SliderSetting placeRange = add(new SliderSetting("PlaceRange", 0.0, 6.0));
    private final SliderSetting surroundCheck = add(new SliderSetting("SurroundCheck", 0, 4));
    private final BooleanSetting inventory =
            add(new BooleanSetting("InventorySwap"));
    private final Timer timer = new Timer();
    private PlayerEntity displayTarget = null;

    public AutoPush() {
        super("AutoPush", "use piston push hole fag", Category.Combat);
    }

    @Override
    public void onEnable() {
        AutoCrystal.INSTANCE.lastBreakTimer.reset();
        //CrystalAura.INSTANCE.faceTimer.reset();
    }

    public static void pistonFacing(Direction i) {
        if (i == Direction.EAST) {
            EntityUtil.sendYawAndPitch(-90.0f, 5.0f);
        } else if (i == Direction.WEST) {
            EntityUtil.sendYawAndPitch(90.0f, 5.0f);
        } else if (i == Direction.NORTH) {
            EntityUtil.sendYawAndPitch(180.0f, 5.0f);
        } else if (i == Direction.SOUTH) {
            EntityUtil.sendYawAndPitch(0.0f, 5.0f);
        }
    }

    static boolean isTargetHere(BlockPos pos, Entity target) {
        return new Box(pos).intersects(target.getBoundingBox());
    }

    public static boolean isInWeb(PlayerEntity player) {
        Vec3d playerPos = player.getPos();
        for (float x : new float[]{0, 0.3F, -0.3f}) {
            for (float z : new float[]{0, 0.3F, -0.3f}) {
                for (float y : new float[]{0, 1, -1}) {
                    BlockPosX pos = new BlockPosX(playerPos.getX() + x, playerPos.getY() + y, playerPos.getZ() + z);
                    if (isTargetHere(pos, player) && mc.world.getBlockState(pos).getBlock() == Blocks.COBWEB) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void onUpdate() {
        if (!timer.passedMs(updateDelay.getValue())) return;
        if (selfGround.getValue() && !mc.player.isOnGround()) {
            if (autoDisable.getValue()) disable();
            return;
        }
        if (findBlock(Blocks.REDSTONE_BLOCK) == -1 || findClass(PistonBlock.class) == -1) {
            if (autoDisable.getValue()) disable();
            return;
        }
        if (noEating.getValue() && EntityUtil.isUsing())
            return;
        timer.reset();
        for (PlayerEntity target : CombatUtil.getEnemies(range.getValue())) {
            if (!canPush(target) || (!target.isOnGround() && onlyGround.getValue()))
                continue;
            if (isInWeb(target) && !allowWeb.getValue()) continue;
            displayTarget = target;
            if (doPush(EntityUtil.getEntityPos(target), target)) return;
        }
        if (autoDisable.getValue()) disable();
        this.displayTarget = null;
    }

    private boolean checkPiston(BlockPos targetPos) {
        for (Direction i : Direction.values()) {
            if (i == Direction.DOWN || i == Direction.UP) continue;
            BlockPos pos = targetPos.up();
            if (getBlock(pos.offset(i)) instanceof PistonBlock) {
                if (getBlockState(pos.offset(i)).get(FacingBlock.FACING).getOpposite() != i) continue;
                for (Direction i2 : Direction.values()) {
                    if (getBlock(pos.offset(i).offset(i2)) == Blocks.REDSTONE_BLOCK) {
                        if (mine.getValue()) {
                            mine(pos.offset(i).offset(i2));
                            if (autoDisable.getValue()) disable();
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean doPush(BlockPos targetPos, PlayerEntity target) {
        if (checkPiston.getValue() && checkPiston(targetPos)) return true;
        if (!mc.world.getBlockState(targetPos.up(2)).blocksMovement()) {
            for (Direction i : Direction.values()) {
                if (i == Direction.DOWN || i == Direction.UP) continue;
                BlockPos pos = targetPos.offset(i).up();
                if (getBlock(pos) instanceof PistonBlock && (!getBlockState(pos.offset(i, -2)).blocksMovement()) && (getBlock(pos.offset(i, -2).up()) == Blocks.AIR || getBlock(pos.offset(i, -2).up()) == Blocks.REDSTONE_BLOCK)) {
                    if (getBlockState(pos).get(FacingBlock.FACING).getOpposite() != i) continue;
                    for (Direction i2 : Direction.values()) {
                        if (getBlock(pos.offset(i2)) == Blocks.REDSTONE_BLOCK) {
                            if (mine.getValue()) mine(pos.offset(i2));
                            if (autoDisable.getValue()) disable();
                            return true;
                        }
                    }
                }
            }
            for (Direction i : Direction.values()) {
                if (i == Direction.DOWN || i == Direction.UP) continue;
                BlockPos pos = targetPos.offset(i).up();
                if (getBlock(pos) instanceof PistonBlock && (!getBlockState(pos.offset(i, -2)).blocksMovement()) && (getBlock(pos.offset(i, -2).up()) == Blocks.AIR || getBlock(pos.offset(i, -2).up()) == Blocks.REDSTONE_BLOCK)) {
                    if (getBlockState(pos).get(FacingBlock.FACING).getOpposite() != i) continue;
                    if (doPower(pos)) return true;
                }
            }
            for (Direction i : Direction.values()) {
                if (i == Direction.DOWN || i == Direction.UP) continue;
                BlockPos pos = targetPos.offset(i).up();
                if ((mc.player.getY() - target.getY() <= -1.0 || mc.player.getY() - target.getY() >= 2.0) && BlockUtil.distanceToXZ(pos.getX() + 0.5, pos.getZ() + 0.5) < 2.6) {
                    continue;
                }
                attackCrystal(pos);
                if (isTrueFacing(pos, i) && BlockUtil.clientCanPlace(pos, false) && (!getBlockState(pos.offset(i, -2)).blocksMovement()) && (!getBlockState(pos.offset(i, -2).up()).blocksMovement())) {
                    if (BlockUtil.getPlaceSide(pos) == null) {
                        if (downPower(pos)) break;
                    }
                    doPiston(i, pos, mine.getValue());
                    return true;
                }
            }

            if ((getBlock(targetPos) == Blocks.AIR && onlyBurrow.getValue()) || !pullBack.getValue()) {
                if (autoDisable.getValue()) disable();
                return true;
            }
            for (Direction i : Direction.values()) {
                if (i == Direction.DOWN || i == Direction.UP) continue;
                BlockPos pos = targetPos.offset(i).up();
                for (Direction i2 : Direction.values()) {
                    if (getBlock(pos) instanceof PistonBlock && getBlock(pos.offset(i2)) == Blocks.REDSTONE_BLOCK) {
                        if (getBlockState(pos).get(FacingBlock.FACING).getOpposite() != i) continue;
                        mine(pos.offset(i2));
                        if (autoDisable.getValue()) disable();
                        return true;
                    }
                }
            }
            for (Direction i : Direction.values()) {
                if (i == Direction.DOWN || i == Direction.UP) continue;
                BlockPos pos = targetPos.offset(i).up();
                for (Direction i2 : Direction.values()) {
                    if (getBlock(pos) instanceof PistonBlock && getBlock(pos.offset(i2)) == Blocks.AIR) {
                        if (getBlockState(pos).get(FacingBlock.FACING).getOpposite() != i) continue;
                        attackCrystal(pos.offset(i2));
                        if (doPower(pos, i2)) continue;
                        mine(pos.offset(i2));
                        return true;
                    }
                }
            }
            for (Direction i : Direction.values()) {
                if (i == Direction.DOWN || i == Direction.UP) continue;
                BlockPos pos = targetPos.offset(i).up();
                if ((mc.player.getY() - target.getY() <= -1.0 || mc.player.getY() - target.getY() >= 2.0) && BlockUtil.distanceToXZ(pos.getX() + 0.5, pos.getZ() + 0.5) < 2.6) {
                    continue;
                }
                attackCrystal(pos);
                if (isTrueFacing(pos, i) && BlockUtil.clientCanPlace(pos, false)) {
                    if (downPower(pos)) continue;
                    doPiston(i, pos, true);
                    return true;
                }
            }
        } else {
            for (Direction i : Direction.values()) {
                if (i == Direction.DOWN || i == Direction.UP) continue;
                BlockPos pos = targetPos.offset(i).up();
                if (getBlock(pos) instanceof PistonBlock && ((mc.world.isAir(pos.offset(i, -2)) && mc.world.isAir(pos.offset(i, -2).down())) || isTargetHere(pos.offset(i, 2), target))) {
                    if (getBlockState(pos).get(FacingBlock.FACING).getOpposite() != i) continue;
                    for (Direction i2 : Direction.values()) {
                        if (getBlock(pos.offset(i2)) == Blocks.REDSTONE_BLOCK) {
                            if (mine.getValue()) mine(pos.offset(i2));
                            if (autoDisable.getValue()) disable();
                            return true;
                        }
                    }
                }
            }
            for (Direction i : Direction.values()) {
                if (i == Direction.DOWN || i == Direction.UP) continue;
                BlockPos pos = targetPos.offset(i).up();
                if (getBlock(pos) instanceof PistonBlock && ((mc.world.isAir(pos.offset(i, -2)) && mc.world.isAir(pos.offset(i, -2).down())) || isTargetHere(pos.offset(i, 2), target))) {
                    if (getBlockState(pos).get(FacingBlock.FACING).getOpposite() != i) continue;
                    if (doPower(pos)) return true;
                }
            }

            for (Direction i : Direction.values()) {
                if (i == Direction.DOWN || i == Direction.UP) continue;
                BlockPos pos = targetPos.offset(i).up();
                if ((mc.player.getY() - target.getY() <= -1.0 || mc.player.getY() - target.getY() >= 2.0) && BlockUtil.distanceToXZ(pos.getX() + 0.5, pos.getZ() + 0.5) < 2.6) {
                    continue;
                }
                attackCrystal(pos);
                if (isTrueFacing(pos, i) && BlockUtil.clientCanPlace(pos, false) && ((mc.world.isAir(pos.offset(i, -2)) && mc.world.isAir(pos.offset(i, -2).down())) || isTargetHere(pos.offset(i, 2), target)) && (!getBlockState(pos.offset(i, -2).up()).blocksMovement())) {
                    if (BlockUtil.getPlaceSide(pos) == null) {
                        if (downPower(pos)) break;
                    }
                    doPiston(i, pos, mine.getValue());
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isTrueFacing(BlockPos pos, Direction facing) {
        if (yawDeceive.getValue()) return true;
        Direction side = BlockUtil.getPlaceSide(pos);
        if (side == null) side = Direction.UP;
        side = side.getOpposite();
        Vec3d hitVec = pos.offset(side.getOpposite()).toCenterPos().add(new Vec3d(side.getVector().getX() * 0.5, side.getVector().getY() * 0.5, side.getVector().getZ() * 0.5));
        return Direction.fromRotation(EntityUtil.getLegitRotations(hitVec)[0]) == facing;
    }

    private boolean doPower(BlockPos pos, Direction i2) {
        if (!BlockUtil.canPlace(pos.offset(i2), placeRange.getValue())) return true;
        int old = mc.player.getInventory().selectedSlot;
        int power = findBlock(Blocks.REDSTONE_BLOCK);
        doSwap(power);
        BlockUtil.placeBlock(pos.offset(i2), rotate.getValue(), redStonePacket.getValue());
        if (inventory.getValue()) {
            doSwap(power);
            EntityUtil.sync();
        } else {
            doSwap(old);
        }
        return false;
    }

    private boolean doPower(BlockPos pos) {
        Direction facing = BlockUtil.getBestNeighboring(pos, null);
        if (facing != null) {
            attackCrystal(pos.offset(facing));
            if (!doPower(pos, facing)) return true;
        }
        for (Direction i2 : Direction.values()) {
            attackCrystal(pos.offset(i2));
            if (doPower(pos, i2)) continue;
            return true;
        }
        return false;
    }

    private boolean downPower(BlockPos pos) {
        if (BlockUtil.getPlaceSide(pos) == null) {
            boolean noPower = true;
            for (Direction i2 : Direction.values()) {
                if (getBlock(pos.offset(i2)) == Blocks.REDSTONE_BLOCK) {
                    noPower = false;
                    break;
                }
            }
            if (noPower) {
                if (!BlockUtil.canPlace(pos.add(0, -1, 0), placeRange.getValue())) {
                    return true;
                } else {
                    int old = mc.player.getInventory().selectedSlot;
                    int power = findBlock(Blocks.REDSTONE_BLOCK);
                    doSwap(power);
                    BlockUtil.placeBlock(pos.add(0, -1, 0), rotate.getValue(), redStonePacket.getValue());
                    if (inventory.getValue()) {
                        doSwap(power);
                        EntityUtil.sync();
                    } else {
                        doSwap(old);
                    }
                }
            }
        }
        return false;
    }

    private void doPiston(Direction i, BlockPos pos, boolean mine) {
        if (BlockUtil.canPlace(pos, placeRange.getValue())) {
            int piston = findClass(PistonBlock.class);
            Direction side = BlockUtil.getPlaceSide(pos);
            if (rotate.getValue()) EntityUtil.facePosSide(pos.offset(side), side.getOpposite());
            if (yawDeceive.getValue()) pistonFacing(i);
            int old = mc.player.getInventory().selectedSlot;
            doSwap(piston);
            BlockUtil.placeBlock(pos, false, pistonPacket.getValue());
            if (inventory.getValue()) {
                doSwap(piston);
                EntityUtil.sync();
            } else {
                doSwap(old);
            }
            if (rotate.getValue()) EntityUtil.facePosSide(pos.offset(side), side.getOpposite());
            for (Direction i2 : Direction.values()) {
                if (getBlock(pos.offset(i2)) == Blocks.REDSTONE_BLOCK) {
                    if (mine) mine(pos.offset(i2));
                    if (autoDisable.getValue()) disable();
                    return;
                }
            }
            doPower(pos);
        }
    }

    @Override
    public String getInfo() {
        if (displayTarget != null) return displayTarget.getName().getString();
        return null;
    }

    private void doSwap(int slot) {
        if (inventory.getValue()) {
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slot, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
        } else {
            InventoryUtil.doSwap(slot);
        }
    }
    public int findBlock(Block blockIn) {
        if (inventory.getValue()) {
            return InventoryUtil.findBlockInventorySlot(blockIn);
        } else {
            return InventoryUtil.findBlock(blockIn);
        }
    }
    public int findClass(Class clazz) {
        if (inventory.getValue()) {
            return InventoryUtil.findClassInventorySlot(clazz);
        } else {
            return InventoryUtil.findClass(clazz);
        }
    }
    private void attackCrystal(BlockPos pos) {
        if (!attackCrystal.getValue()) return;
        for (Entity crystal : mc.world.getEntities()) {
            if (!(crystal instanceof EndCrystalEntity) || MathHelper.sqrt((float) crystal.squaredDistanceTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5)) > 2.0)
                continue;
            CombatUtil.attackCrystal(crystal, rotate.getValue(), false);
            return;
        }
    }

    private void mine(BlockPos pos) {
        PacketMine.INSTANCE.mine(pos);
    }

    private Block getBlock(BlockPos pos) {
        return mc.world.getBlockState(pos).getBlock();
    }

    private BlockState getBlockState(BlockPos pos) {
        return mc.world.getBlockState(pos);
    }

    private Boolean canPush(PlayerEntity player) {
        int progress = 0;
        if (!mc.world.isAir(new BlockPosX(player.getX() + 1, player.getY() + 0.5, player.getZ()))) progress++;
        if (!mc.world.isAir(new BlockPosX(player.getX() - 1, player.getY() + 0.5, player.getZ()))) progress++;
        if (!mc.world.isAir(new BlockPosX(player.getX(), player.getY() + 0.5, player.getZ() + 1))) progress++;
        if (!mc.world.isAir(new BlockPosX(player.getX(), player.getY() + 0.5, player.getZ() - 1))) progress++;
        if (!mc.world.isAir(new BlockPosX(player.getX(), player.getY() + 2.5, player.getZ()))) {
            for (Direction i : Direction.values()) {
                if (i == Direction.UP || i == Direction.DOWN) continue;
                BlockPos pos = EntityUtil.getEntityPos(player).offset(i);
                if (mc.world.isAir(pos) && mc.world.isAir(pos.up()) || isTargetHere(pos, player)) {
                    if (!mc.world.isAir(new BlockPosX(player.getX(), player.getY() + 0.5, player.getZ()))) return true;
                    return progress > surroundCheck.getValue() - 1;
                }
            }
            return false;
        }
        if (!mc.world.isAir(new BlockPosX(player.getX(), player.getY() + 0.5, player.getZ()))) return true;
        return progress > surroundCheck.getValue() - 1;
    }
}