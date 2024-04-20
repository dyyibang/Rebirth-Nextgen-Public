package me.rebirthclient.mod.modules.render;

import me.rebirthclient.api.util.FadeUtils;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.settings.impl.BooleanSetting;
import me.rebirthclient.mod.settings.impl.SliderSetting;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.util.math.MatrixStack;

public class CameraClip extends Module {
    public static CameraClip INSTANCE;
    public CameraClip() {
        super("CameraClip", Category.Render);
        INSTANCE = this;
    }

    public SliderSetting distance = add(new SliderSetting("Distance", 1f, 20f));
    public SliderSetting animateTime = add(new SliderSetting("AnimationTime", 0, 1000));
    private final BooleanSetting antiFront = add(new BooleanSetting("AntiFront"));
    private final FadeUtils animation = new FadeUtils(300);
    boolean first = false;
    @Override
    public void onRender3D(MatrixStack matrixStack, float partialTicks) {
        if (mc.options.getPerspective() == Perspective.THIRD_PERSON_FRONT && antiFront.getValue())
            mc.options.setPerspective(Perspective.FIRST_PERSON);
        animation.setLength(animateTime.getValueInt());
        if (mc.options.getPerspective() == Perspective.FIRST_PERSON) {
            if (!first) {
                first = true;
                animation.reset();
            }
        } else {
            if (first) {
                first = false;
                animation.reset();
            }
        }
    }

    public double getDistance() {
        double quad = mc.options.getPerspective() == Perspective.FIRST_PERSON ? 1 - animation.easeOutQuad() : animation.easeOutQuad();
        return 1d + ((distance.getValue() - 1d) * quad);
    }
}
