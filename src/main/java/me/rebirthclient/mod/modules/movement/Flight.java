package me.rebirthclient.mod.modules.movement;

import me.rebirthclient.api.events.eventbus.EventHandler;
import me.rebirthclient.api.events.impl.MoveEvent;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.settings.impl.SliderSetting;

import static me.rebirthclient.api.util.MovementUtil.*;

public class Flight extends Module {
    public static Flight INSTANCE;

    public Flight() {
        super("Flight", "me", Category.Movement);
        INSTANCE = this;
    }


    public SliderSetting speed = add(new SliderSetting("Speed", 0.1f, 10.0f));
    private final SliderSetting sneakDownSpeed = add(new SliderSetting("DownSpeed", 0.1F, 10.0F));
    private final SliderSetting upSpeed = add(new SliderSetting("UpSpeed", 0.1F, 10.0F));
    public SliderSetting downFactor = add(new SliderSetting("DownFactor", 0.0f, 1f, 0.000001f));
    private MoveEvent event;
    @EventHandler
    public void onMove(MoveEvent event) {
        if (nullCheck()) return;
        this.event = event;
        if (!(mc.options.sneakKey.isPressed() && mc.player.input.jumping)) {
            if (mc.options.sneakKey.isPressed()) {
                setY(-sneakDownSpeed.getValue());
            }
            else if (mc.player.input.jumping) {
                setY(upSpeed.getValue());
            } else {
                setY(-downFactor.getValue());
            }
        } else {
            setY(0);
        }
        double[] dir = directionSpeed(speed.getValue());
        setX(dir[0]);
        setZ(dir[1]);
    }

    private void setX(double f) {
        event.setX(f);
        setMotionX(f);
    }

    private void setY(double f) {
        event.setY(f);
        setMotionY(f);
    }

    private void setZ(double f) {
        event.setZ(f);
        setMotionZ(f);
    }
}
