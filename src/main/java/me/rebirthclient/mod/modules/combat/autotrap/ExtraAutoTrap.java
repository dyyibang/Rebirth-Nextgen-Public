package me.rebirthclient.mod.modules.combat.autotrap;

import me.rebirthclient.api.util.*;
import me.rebirthclient.mod.modules.ExtraModule;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.awt.*;
import java.util.HashMap;

public class ExtraAutoTrap extends ExtraModule {
    public ExtraAutoTrap() {
        super();
    }
    public static final HashMap<BlockPos, placePosition> PlaceMap = new HashMap<>();
    public static void addBlock(BlockPos pos) {
        if (BlockUtil.clientCanPlace(pos, true) && !PlaceMap.containsKey(pos)) PlaceMap.put(pos, new placePosition(pos));
    }

    private void drawBlock(BlockPos pos, double alpha, Color color, MatrixStack matrixStack) {
        if (AutoTrap.INSTANCE.sync.getValue()) {
            color = AutoTrap.INSTANCE.color.getValue();
        }
        Render3DUtil.draw3DBox(matrixStack, new Box(pos), ColorUtil.injectAlpha(color, (int) alpha), AutoTrap.INSTANCE.outline.getValue(), AutoTrap.INSTANCE.box.getValue());
    }

    @Override
    public void onRender3D(MatrixStack matrixStack, float partialTicks) {
        if (!AutoTrap.INSTANCE.render.getValue()) return;
        if (PlaceMap.isEmpty()) return;
        boolean shouldClear = true;
        for (placePosition placePosition : PlaceMap.values()) {
            if (!BlockUtil.clientCanPlace(placePosition.pos, true)) {
                placePosition.isAir = false;
            }
            if (!placePosition.timer.passedMs((long) (AutoTrap.INSTANCE.delay.getValue() + 100)) && placePosition.isAir) {
                placePosition.firstFade.reset();
            }
            if (placePosition.firstFade.easeOutQuad() == 1) continue;
            shouldClear = false;
            drawBlock(placePosition.pos, (double) AutoTrap.INSTANCE.color.getValue().getAlpha() * (1 - placePosition.firstFade.easeOutQuad()), placePosition.posColor, matrixStack);
        }
        if (shouldClear) PlaceMap.clear();
    }


    public static class placePosition {
        public final FadeUtils firstFade;
        public final BlockPos pos;
        public final Color posColor;
        public final Timer timer;
        public boolean isAir;
        public placePosition(BlockPos placePos) {
            this.firstFade = new FadeUtils((long) AutoTrap.INSTANCE.fadeTime.getValue());
            this.pos = placePos;
            this.posColor = AutoTrap.INSTANCE.color.getValue();
            this.timer = new Timer();
            this.isAir = true;
            timer.reset();
        }
    }
}
