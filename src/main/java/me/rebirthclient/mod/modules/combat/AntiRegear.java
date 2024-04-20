package me.rebirthclient.mod.modules.combat;

import me.rebirthclient.api.util.BlockUtil;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.settings.impl.SliderSetting;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.util.math.MathHelper;

public class AntiRegear extends Module {
    private final SliderSetting safeRange =
            add(new SliderSetting("SafeRange", 0, 8));
    private final SliderSetting range =
            add(new SliderSetting("Range", 0, 8));
    public AntiRegear() {
        super("AntiRegear", "Shulker nuker", Category.Combat);
    }

    @Override
    public void onUpdate() {
        if (PacketMine.breakPos != null && mc.world.getBlockState(PacketMine.breakPos).getBlock() instanceof ShulkerBoxBlock) {
            return;
        }
        if (getBlock() != null) {
           PacketMine.INSTANCE.mine(getBlock().getPos());
        }
    }

    private ShulkerBoxBlockEntity getBlock() {
        for (BlockEntity entity : BlockUtil.getTileEntities()) {
            if (entity instanceof ShulkerBoxBlockEntity shulker) {
                if (MathHelper.sqrt((float) mc.player.squaredDistanceTo(shulker.getPos().toCenterPos())) <= safeRange.getValue()) {
                    continue;
                }
                if (MathHelper.sqrt((float) mc.player.squaredDistanceTo(shulker.getPos().toCenterPos())) <= range.getValue()) {
                    return shulker;
                }
            }
        }
        return null;
    }
}