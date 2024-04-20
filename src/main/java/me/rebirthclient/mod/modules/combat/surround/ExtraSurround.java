package me.rebirthclient.mod.modules.combat.surround;

import me.rebirthclient.api.util.*;
import me.rebirthclient.mod.modules.ExtraModule;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

import java.awt.*;
import java.util.HashMap;

public class ExtraSurround extends ExtraModule {
    public static final HashMap<BlockPos, placePosition> PlaceMap = new HashMap<>();
    public static void addBlock(BlockPos pos) {
        if (BlockUtil.clientCanPlace(pos, true)) PlaceMap.put(pos, new placePosition(pos));
    }

    private void drawBlock(BlockPos pos, int alpha, Color color, MatrixStack matrixStack) {
        Render3DUtil.draw3DBox(matrixStack, new Box(pos), ColorUtil.injectAlpha(color, alpha), Surround.INSTANCE.outline.getValue(), Surround.INSTANCE.box.getValue());
    }

    private BlockPos lastPos = null;

    @Override
    public void onRender3D(MatrixStack matrixStack, float partialTicks) {
        if (!Surround.INSTANCE.render.getValue()) return;
        if (Surround.INSTANCE.moveReset.getValue() && !EntityUtil.getPlayerPos().equals(lastPos)) {
            lastPos = EntityUtil.getPlayerPos();
            PlaceMap.clear();
        }
        if (false && Surround.INSTANCE.isOn() && Surround.INSTANCE.pre.getValue() && (!Surround.INSTANCE.onlyGround.getValue() || mc.player.isOnGround())) {
            BlockPos pos = EntityUtil.getPlayerPos();
            for (Direction i : Direction.values()) {
                if (i == Direction.UP) continue;
                BlockPos offsetPos = pos.offset(i);
                addBlock(offsetPos);
                if (BlockUtil.getPlaceSide(offsetPos) == null) {
                    if (BlockUtil.canReplace(offsetPos)) addBlock(offsetPos.down());
                }
                if (Surround.checkSelf(offsetPos) && Surround.INSTANCE.extend.getValue()) {
                    for (Direction i2 : Direction.values()) {
                        if (i2 == Direction.UP) continue;
                        BlockPos offsetPos2 = offsetPos.offset(i2);
                        if (Surround.checkSelf(offsetPos2)) {
                            for (Direction i3 : Direction.values()) {
                                if (i3 == Direction.UP) continue;
                                addBlock(offsetPos2);
                                BlockPos offsetPos3 = offsetPos2.offset(i3);
                                addBlock(offsetPos3);
                                if (BlockUtil.getPlaceSide(offsetPos3) == null) {
                                    addBlock(offsetPos3.down());
                                }
                            }
                        }
                        addBlock(offsetPos2);
                        if (BlockUtil.getPlaceSide(offsetPos2) == null) {
                            addBlock(offsetPos2.down());
                        }
                    }
                }
            }
        }
        if (PlaceMap.isEmpty()) return;
        boolean shouldClear = true;
        Surround module = Surround.INSTANCE;
        Color currentColor = module.color.getValue();
        for (placePosition placePosition : PlaceMap.values()) {
            if (!BlockUtil.clientCanPlace(placePosition.pos, true)) {
                placePosition.isAir = false;
            }
            if (!placePosition.timer.passedMs((long) (Surround.INSTANCE.delay.getValue() + 100)) && placePosition.isAir) {
                placePosition.firstFade.reset();
            }
            if (placePosition.firstFade.easeOutQuad() == 1) continue;
            shouldClear = false;
            drawBlock(placePosition.pos, (int) ((double) currentColor.getAlpha() * (1 - placePosition.firstFade.easeOutQuad())), currentColor, matrixStack);
        }
        if (shouldClear) PlaceMap.clear();
    }


    public static class placePosition {
        public final FadeUtils firstFade;
        public final BlockPos pos;
        public final Timer timer;
        public boolean isAir;
        public placePosition(BlockPos placePos) {
            this.firstFade = new FadeUtils((long) Surround.INSTANCE.fadeTime.getValue());
            this.pos = placePos;
            this.timer = new Timer();
            this.isAir = true;
            timer.reset();
        }
    }
}
