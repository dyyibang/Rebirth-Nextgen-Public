package me.rebirthclient.mod.modules.movement;

import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.api.util.MovementUtil;
import net.minecraft.util.math.BlockPos;

public class AutoCenter extends Module {
    public static AutoCenter INSTANCE;
    public AutoCenter() {
        super("AutoCenter", "move center", Category.Movement);
        INSTANCE = this;
    }

    @Override
    public void onUpdate() {
        if (nullCheck()) {
            disable();
            return;
        }
        doCenter();
    }

    @Override
    public void onEnable() {
        if (nullCheck()) {
            disable();
            return;
        }
        doCenter();
    }

    private void doCenter() {
        if (EntityUtil.isElytraFlying()) {
            disable();
            return;
        }
        BlockPos blockPos = EntityUtil.getPlayerPos();
        if (mc.player.getX() - blockPos.getX() - 0.5 <= 0.2 && mc.player.getX() - blockPos.getX() - 0.5 >= -0.2 && mc.player.getZ() - blockPos.getZ() - 0.5 <= 0.2 && mc.player.getZ() - 0.5 - blockPos.getZ() >= -0.2) {
            disable();
        } else {
            MovementUtil.setMotionX((blockPos.getX() + 0.5 - mc.player.getX()) / 2);
            MovementUtil.setMotionZ((blockPos.getZ() + 0.5 - mc.player.getZ()) / 2);
        }
    }
    @Override
    public void onDisable() {
        MovementUtil.setMotionZ(0);
        MovementUtil.setMotionX(0);
    }
}
