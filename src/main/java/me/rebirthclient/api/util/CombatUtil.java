package me.rebirthclient.api.util;

import me.rebirthclient.Rebirth;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;

import java.util.ArrayList;
import java.util.List;

import static me.rebirthclient.api.util.BlockUtil.getState;
import static me.rebirthclient.api.util.BlockUtil.isAir;
public class CombatUtil implements Wrapper {
    public static final Timer breakTimer = new Timer();
    public static List<PlayerEntity> getEnemies(double range) {
        List<PlayerEntity> list = new ArrayList<>();
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (!isValid(player, range)) continue;
            list.add(player);
        }
        return list;
    }

    public static void attackCrystal(BlockPos pos, boolean rotate, boolean eatingPause) {
        for (EndCrystalEntity entity : mc.world.getNonSpectatingEntities(EndCrystalEntity.class, new Box(pos))) {
            attackCrystal(entity, rotate, eatingPause);
            break;
        }
    }

    public static void attackCrystal(Box box, boolean rotate, boolean eatingPause) {
        for (EndCrystalEntity entity : mc.world.getNonSpectatingEntities(EndCrystalEntity.class, box)) {
            attackCrystal(entity, rotate, eatingPause);
            break;
        }
    }

    public static void attackCrystal(Entity crystal, boolean rotate, boolean usingPause) {
        if (!CombatUtil.breakTimer.passedMs((long) (Rebirth.HUD.attackDelay.getValue() * 1000))) return;
        if (usingPause && EntityUtil.isUsing())
            return;
        if (crystal != null) {
            CombatUtil.breakTimer.reset();
            mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.attack(crystal, mc.player.isSneaking()));
            mc.player.swingHand(Hand.MAIN_HAND);
            if (rotate) EntityUtil.faceVector(new Vec3d(crystal.getX(), crystal.getY() + 0.25, crystal.getZ()));
        }
    }
    public static boolean isValid(Entity entity, double range) {
        boolean invalid = entity == null || !entity.isAlive() || entity.equals(mc.player) || entity instanceof PlayerEntity && Rebirth.FRIEND.isFriend(entity.getName().getString()) || mc.player.squaredDistanceTo(entity) > MathUtil.square(range);

        return !invalid;
    }

    public static BlockPos getHole(float range, boolean doubleHole, boolean any) {
        BlockPos bestPos = null;
        double bestDistance = range + 1;
        for (BlockPos pos : BlockUtil.getSphere(range)) {
            if (BlockUtil.isHole(pos, true, true, any) || doubleHole && isDoubleHole(pos)) {
                double distance = MathHelper.sqrt((float) mc.player.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
                if (bestPos == null || distance < bestDistance) {
                    bestPos = pos;
                    bestDistance = distance;
                }
            }
        }
        return bestPos;
    }
    public static boolean isDoubleHole(BlockPos pos) {
        Direction unHardFacing = is3Block(pos);
        if (unHardFacing != null) {
            pos = pos.offset(unHardFacing);
            unHardFacing = is3Block(pos);
            return unHardFacing != null;
        }
        return false;
    }
    public static Direction is3Block(BlockPos pos) {
        if (!isHard(pos.down())) {
            return null;
        }
        if (!isAir(pos) || !isAir(pos.up()) || !isAir(pos.up(2))) {
            return null;
        }
        int progress = 0;
        Direction unHardFacing = null;
        for (Direction facing : Direction.values()) {
            if (facing == Direction.UP || facing == Direction.DOWN) continue;
            if (isHard(pos.offset(facing))) {
                progress++;
                continue;
            }
            int progress2 = 0;
            for (Direction facing2 : Direction.values()) {
                if (facing2 == Direction.DOWN || facing2 == facing.getOpposite()) {
                    continue;
                }
                if (isHard(pos.offset(facing).offset(facing2))) {
                    progress2++;
                }
            }
            if (progress2 == 4) {
                progress++;
                continue;
            }
            unHardFacing = facing;
        }
        if (progress == 3) {
            return unHardFacing;
        }
        return null;
    }
    public static PlayerEntity getClosestEnemy(double distance) {
        PlayerEntity closest = null;

        for (PlayerEntity player : getEnemies(distance)) {
            if (closest == null) {
                closest = player;
                continue;
            }

            if (!(mc.player.getEyePos().squaredDistanceTo(player.getPos()) < mc.player.squaredDistanceTo(closest))) continue;

            closest = player;
        }
        return closest;
    }
    public static Vec3d getEntityPosVec(PlayerEntity entity, int ticks) {
        return entity.getPos().add(getMotionVec(entity, ticks, true));
    }

    public static Vec3d getMotionVec(Entity entity, int ticks, boolean collision) {
        double dX = entity.getX() - entity.prevX;
        double dY = entity.getY() - entity.prevY;
        double dZ = entity.getZ() - entity.prevZ;
        double entityMotionPosX = 0;
        double entityMotionPosY = 0;
        double entityMotionPosZ = 0;
        if (collision) {
            for (double i = 1; i <= ticks; i = i + 0.5) {
                if (!mc.world.canCollide(entity, entity.getBoundingBox().offset(new Vec3d(dX * i, dY * i, dZ * i)))) {
                    entityMotionPosX = dX * i;
                    entityMotionPosY = dY * i;
                    entityMotionPosZ = dZ * i;
                } else {
                    break;
                }
            }
        } else {
            entityMotionPosX = dX * ticks;
            entityMotionPosY = dY * ticks;
            entityMotionPosZ = dZ * ticks;
        }

        return new Vec3d(entityMotionPosX, entityMotionPosY, entityMotionPosZ);
    }
    public static boolean terrainIgnore = false;

    public static boolean isHard(BlockPos pos) {
        return getState(pos).getBlock() == Blocks.OBSIDIAN || getState(pos).getBlock() == Blocks.ENDER_CHEST || getState(pos).getBlock() == Blocks.BEDROCK || getState(pos).getBlock() == Blocks.ANVIL;
    }

}
