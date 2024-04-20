package me.rebirthclient.api.util;

import me.rebirthclient.asm.accessors.IExplosion;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class DamageUtil implements Wrapper {
    public static Explosion explosion = new Explosion(mc.world, null, 0, 0, 0, 6f, false, Explosion.DestructionType.DESTROY);
    public static float anchorDamage(BlockPos pos, PlayerEntity target, PlayerEntity predict){
        if (BlockUtil.getBlock(pos) == Blocks.RESPAWN_ANCHOR) {
            BlockState oldState = BlockUtil.getState(pos);
            mc.world.setBlockState(pos, Blocks.AIR.getDefaultState());
            float damage = calculateDamage(pos.toCenterPos(), target, predict, 5);
            mc.world.setBlockState(pos, oldState);
            return damage;
        } else {
            return calculateDamage(pos.toCenterPos(), target, predict, 5);
        }
    }
    public static float calculateDamage(BlockPos pos, PlayerEntity target, PlayerEntity predict, float power){
        return calculateDamage(pos.toCenterPos().add(0,-0.5,0), target, predict, power);
    }
    public static float calculateDamage(Vec3d explosionPos, PlayerEntity target, PlayerEntity predict, float power) {
        if (mc.world.getDifficulty() == Difficulty.PEACEFUL)
            return 0f;
        ((IExplosion) explosion).setWorld(mc.world);
        ((IExplosion) explosion).setX(explosionPos.x);
        ((IExplosion) explosion).setY(explosionPos.y);
        ((IExplosion) explosion).setZ(explosionPos.z);
        ((IExplosion) explosion).setPower(power);

        if (!new Box(
                MathHelper.floor(explosionPos.x - 11d),
                MathHelper.floor(explosionPos.y - 11d),
                MathHelper.floor(explosionPos.z - 11d),
                MathHelper.floor(explosionPos.x + 13d),
                MathHelper.floor(explosionPos.y + 13d),
                MathHelper.floor(explosionPos.z + 13d)).intersects(predict.getBoundingBox())
        ) {
            return 0f;
        }

        if (!target.isImmuneToExplosion() && !target.isInvulnerable()) {
            double distExposure = MathHelper.sqrt((float) predict.squaredDistanceTo(explosionPos)) / 12d;
            if (distExposure <= 1.0) {
                double xDiff = predict.getX() - explosionPos.x;
                double yDiff = predict.getY() - explosionPos.y;
                double zDiff = predict.getX() - explosionPos.z;
                double diff = MathHelper.sqrt((float) (xDiff * xDiff + yDiff * yDiff + zDiff * zDiff));
                if (diff != 0.0) {
                    double exposure = Explosion.getExposure(explosionPos, predict);
                    double finalExposure = (1.0 - distExposure) * exposure;

                    float toDamage = (float) Math.floor((finalExposure * finalExposure + finalExposure) / 2.0 * 7.0 * 12d + 1.0);

                    if (mc.world.getDifficulty() == Difficulty.EASY) {
                        toDamage = Math.min(toDamage / 2f + 1f, toDamage);
                    } else if (mc.world.getDifficulty() == Difficulty.HARD) {
                        toDamage = toDamage * 3f / 2f;
                    }

                    toDamage = net.minecraft.entity.DamageUtil.getDamageLeft(toDamage, target.getArmor(), (float) Objects.requireNonNull(target.getAttributeInstance(EntityAttributes.GENERIC_ARMOR_TOUGHNESS)).getValue());

                    if (target.hasStatusEffect(StatusEffects.RESISTANCE)) {
                        int resistance = 25 - (Objects.requireNonNull(target.getStatusEffect(StatusEffects.RESISTANCE)).getAmplifier() + 1) * 5;
                        float resistance_1 = toDamage * resistance;
                        toDamage = Math.max(resistance_1 / 25f, 0f);
                    }

                    if (toDamage <= 0f) {
                        toDamage = 0f;
                    } else {
                        int protAmount = EnchantmentHelper.getProtectionAmount(target.getArmorItems(), explosion.getDamageSource());
                        if (protAmount > 0) {
                            toDamage = net.minecraft.entity.DamageUtil.getInflictedDamage(toDamage, protAmount);
                        }
                    }
                    return toDamage;
                }
            }
        }
        return 0f;
    }

    public static float calculateDamage(double posX, double posY, double posZ, @NotNull Entity entity, @Nullable Entity predictEntity, float power) {
        if (predictEntity == null) {
            predictEntity = entity;
        }
        float doubleExplosionSize = 12.0f;
        double v = (1.0 - MathHelper.sqrt((float) predictEntity.squaredDistanceTo(posX, posY, posZ)) / (double) doubleExplosionSize) * getBlockDensity(new Vec3d(posX, posY, posZ), predictEntity.getBoundingBox());
        float damage = (int) ((v * v + v) / 2.0 * 7.0 * (double) doubleExplosionSize + 1.0);
        double finald = 1.0;
        if (entity instanceof LivingEntity) {
            finald = DamageUtil.getBlastReduction((LivingEntity) entity, getDamageMultiplied(damage), new Explosion(mc.world, null, posX, posY, posZ, power, false, Explosion.DestructionType.DESTROY));
        }
        return (float) finald;
    }

    public static float getBlastReduction(LivingEntity entity, float damageI, Explosion explosion) {
        float damage = damageI;
        if (entity instanceof PlayerEntity ep) {
            damage = net.minecraft.entity.DamageUtil.getDamageLeft(damage, (float) ep.getArmor(), (float) ep.getAttributeInstance(EntityAttributes.GENERIC_ARMOR_TOUGHNESS).getValue());
            int k = EnchantmentHelper.getProtectionAmount(ep.getArmorItems(), explosion.getDamageSource());
            float f = MathHelper.clamp((float) k, 0.0f, 20.0f);
            damage *= 1.0f - f / 25.0f;
            if (entity.hasStatusEffect(StatusEffects.RESISTANCE)) {
                int resistance = 25 - (Objects.requireNonNull(entity.getStatusEffect(StatusEffects.RESISTANCE)).getAmplifier() + 1) * 5;
                float resistance_1 = damage * resistance;
                damage = Math.max(resistance_1 / 25f, 0f);
            }
            damage = Math.max(damage, 0.0f);
            return damage;
        }
        damage = net.minecraft.entity.DamageUtil.getDamageLeft(damage, (float) entity.getArmor(), (float) entity.getAttributeInstance(EntityAttributes.GENERIC_ARMOR_TOUGHNESS).getValue());
        return damage;
    }
    public static float getBlockDensity(Vec3d vec, Box bb)
    {
        double d0 = 1.0D / ((bb.maxX - bb.minX) * 2.0D + 1.0D);
        double d1 = 1.0D / ((bb.maxY - bb.minY) * 2.0D + 1.0D);
        double d2 = 1.0D / ((bb.maxZ - bb.minZ) * 2.0D + 1.0D);
        double d3 = (1.0D - Math.floor(1.0D / d0) * d0) / 2.0D;
        double d4 = (1.0D - Math.floor(1.0D / d2) * d2) / 2.0D;

        if (d0 >= 0.0D && d1 >= 0.0D && d2 >= 0.0D)
        {
            int j2 = 0;
            int k2 = 0;

            for (float f = 0.0F; f <= 1.0F; f = (float)((double)f + d0))
            {
                for (float f1 = 0.0F; f1 <= 1.0F; f1 = (float)((double)f1 + d1))
                {
                    for (float f2 = 0.0F; f2 <= 1.0F; f2 = (float)((double)f2 + d2))
                    {
                        double d5 = bb.minX + (bb.maxX - bb.minX) * (double)f;
                        double d6 = bb.minY + (bb.maxY - bb.minY) * (double)f1;
                        double d7 = bb.minZ + (bb.maxZ - bb.minZ) * (double)f2;

                        if (mc.world.raycast(new RaycastContext(new Vec3d(d5 + d3, d6, d7 + d4), vec, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, mc.player)) == null)
                        {
                            ++j2;
                        }

                        ++k2;
                    }
                }
            }

            return (float)j2 / (float)k2;
        }
        else
        {
            return 0.0F;
        }
    }
    public static float getDamageMultiplied(float damage) {
        int diff = mc.world.getDifficulty().getId();
        return damage * (diff == 0 ? 0.0f : (diff == 2 ? 1.0f : (diff == 1 ? 0.5f : 1.5f)));
    }
}
