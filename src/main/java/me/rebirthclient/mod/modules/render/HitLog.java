package me.rebirthclient.mod.modules.render;

import me.rebirthclient.Rebirth;
import me.rebirthclient.api.events.eventbus.EventHandler;
import me.rebirthclient.api.events.impl.DeathEvent;
import me.rebirthclient.api.events.impl.TotemEvent;
import me.rebirthclient.api.util.Timer;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.settings.impl.SliderSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;

import java.awt.*;
import java.util.ArrayList;

public class HitLog
        extends Module {

    public static HitLog INSTANCE = new HitLog();
    private final ArrayList<Log> logList = new ArrayList<>();
    public SliderSetting animationSpeed = add(new SliderSetting("AnimationSpeed", 0.01, 0.5, 0.01));
    public SliderSetting stayTime = add(new SliderSetting("StayTime", 0.5, 5, 0.1));
    public HitLog() {
        super("HitLog", Category.Render);
        INSTANCE = this;
    }

    static double y = 0;

    @Override
    public void onRender2D(DrawContext drawContext, float tickDelta) {
        y = mc.getWindow().getHeight() / 4D - 40D;
        logList.removeIf(log -> log.timer.passed(stayTime.getValue() * 1000D) && log.alpha <= 10);
        for (Log log : new ArrayList<>(logList)) {
            boolean end = log.timer.passed(stayTime.getValue() * 1000D);
            log.alpha = animate(log.alpha, end ? 0 : 255, animationSpeed.getValue());
            log.y = animate(log.y, end ? mc.getWindow().getHeight() / 4D - 30D + mc.textRenderer.fontHeight : y, animationSpeed.getValue());
            drawContext.drawTextWithShadow(mc.textRenderer, log.text, (int) log.x, (int) log.y, new Color(255, 255, 255, (int) log.alpha).getRGB());
            if (!end) y -= mc.textRenderer.fontHeight + 2;
        }
    }

    @EventHandler
    public void onPlayerDeath(DeathEvent event) {
        PlayerEntity player = event.getPlayer();
        if (player == mc.player || player.distanceTo(mc.player) > 20) return;
        int popCount = Rebirth.POP.popContainer.getOrDefault(player.getName().getString(), 0);
        addLog("\u00a74\u00a7m" + player.getName().getString() + "\u00a7f " + popCount);
    }

    @EventHandler
    public void onTotem(TotemEvent event) {
        PlayerEntity player = event.getPlayer();
        if (player == mc.player || player.distanceTo(mc.player) > 20) return;
        int popCount = Rebirth.POP.popContainer.getOrDefault(player.getName().getString(), 1);
        addLog("\u00a7a" + player.getName().getString() + "\u00a7f " + popCount);
    }

    public void addLog(String text) {
        logList.add(new Log(text));
    }

    public static double animate(double current, double endPoint, double speed) {
        boolean shouldContinueAnimation = endPoint > current;
        double dif = Math.max(endPoint, current) - Math.min(endPoint, current);
        double factor = dif * speed;
        if (Math.abs(factor) <= 0.001) return endPoint;
        return current + (shouldContinueAnimation ? factor : -factor);
    }
    static class Log {
        final Timer timer;
        final String text;
        double x;
        double y;
        double alpha;
        public Log(String text) {
            this.timer = new Timer();
            this.text = text;
            this.x = mc.getWindow().getWidth() / 4D;
            this.y = HitLog.y - 20;
            this.alpha = 0;
        }
    }
}

