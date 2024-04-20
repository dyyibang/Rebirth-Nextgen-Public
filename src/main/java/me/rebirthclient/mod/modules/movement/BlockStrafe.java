package me.rebirthclient.mod.modules.movement;

import me.rebirthclient.api.events.eventbus.EventHandler;
import me.rebirthclient.api.events.impl.MoveEvent;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.combat.AutoAnchor;
import me.rebirthclient.mod.modules.combat.AutoPush;
import me.rebirthclient.mod.settings.impl.SliderSetting;

public class BlockStrafe extends Module {
    public static BlockStrafe INSTANCE;
    private final SliderSetting speed =
            add(new SliderSetting("Speed", 0, 20, 1));
    private final SliderSetting aSpeed =
            add(new SliderSetting("AnchorSpeed", 0, 20, 1));
    private final SliderSetting aForward =
            add(new SliderSetting("AnchorForward", 0, 20, 0.25));
    public BlockStrafe() {
        super("BlockStrafe", Category.Movement);
        INSTANCE = this;
    }

    @EventHandler
    public void onMove(MoveEvent event) {
        if (!EntityUtil.isInsideBlock()) return;
        if (AutoPush.isInWeb(mc.player)) return;
        double speed = AutoAnchor.INSTANCE.currentPos == null ? this.speed.getValue() : aSpeed.getValue();
        double moveSpeed = 0.2873 / 100 * speed;
        double n = mc.player.input.movementForward;
        double n2 = mc.player.input.movementSideways;
        double n3 = mc.player.getYaw();
        if (n == 0.0 && n2 == 0.0) {
            if (AutoAnchor.INSTANCE.currentPos == null) {
                event.setX(0.0);
                event.setZ(0.0);
            } else {
                moveSpeed = 0.2873 / 100 * aForward.getValue();
                event.setX(1 * moveSpeed * -Math.sin(Math.toRadians(n3)));
                event.setZ(1 * moveSpeed * Math.cos(Math.toRadians(n3)));
            }
            return;
        } else if (n != 0.0 && n2 != 0.0) {
            n *= Math.sin(0.7853981633974483);
            n2 *= Math.cos(0.7853981633974483);
        }
        event.setX((n * moveSpeed * -Math.sin(Math.toRadians(n3)) + n2 * moveSpeed * Math.cos(Math.toRadians(n3))));
        event.setZ((n * moveSpeed * Math.cos(Math.toRadians(n3)) - n2 * moveSpeed * -Math.sin(Math.toRadians(n3))));
    }
}
