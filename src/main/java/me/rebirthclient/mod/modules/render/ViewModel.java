package me.rebirthclient.mod.modules.render;

import me.rebirthclient.api.events.eventbus.EventHandler;
import me.rebirthclient.api.events.impl.HeldItemRendererEvent;
import me.rebirthclient.asm.accessors.IHeldItemRenderer;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.settings.impl.BooleanSetting;
import me.rebirthclient.mod.settings.impl.SliderSetting;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.SwordItem;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RotationAxis;

public class ViewModel extends Module {
    public static ViewModel INSTANCE;
    public ViewModel() {
        super("ViewModel", Category.Render);
        INSTANCE = this;
    }

    public BooleanSetting swingAnimation = add(new BooleanSetting("SwingAnimation"));
    public BooleanSetting eatAnimation = add(new BooleanSetting("EatAnimation"));
    public BooleanSetting swordBlock = add(new BooleanSetting("SwordBlock"));
    public BooleanSetting oldAnimationsM = add(new BooleanSetting("DisableSwapMain"));
    public BooleanSetting oldAnimationsOff = add(new BooleanSetting("DisableSwapOff"));
    public final SliderSetting scaleMainX = add(new SliderSetting("ScaleMainX", 0.1f, 5f, 0.01));
    public final SliderSetting scaleMainY = add(new SliderSetting("ScaleMainY", 0.1f, 5f, 0.01));
    public final SliderSetting scaleMainZ = add(new SliderSetting("ScaleMainZ", 0.1f, 5f, 0.01));
    public final SliderSetting positionMainX = add(new SliderSetting("PositionMainX", -3.0f, 3f, 0.01));
    public final SliderSetting positionMainY = add(new SliderSetting("PositionMainY", -3.0f, 3f, 0.01));
    public final SliderSetting positionMainZ = add(new SliderSetting("PositionMainZ", -3.0f, 3f, 0.01));
    public final SliderSetting rotationMainX = add(new SliderSetting("RotationMainX", -180.0f, 180f, 0.01));
    public final SliderSetting rotationMainY = add(new SliderSetting("RotationMainY", -180.0f, 180f, 0.01));
    public final SliderSetting rotationMainZ = add(new SliderSetting("RotationMainZ", -180.0f, 180f, 0.01));
    public final SliderSetting scaleOffX = add(new SliderSetting("ScaleOffX", 0.1f, 5f, 0.01));
    public final SliderSetting scaleOffY = add(new SliderSetting("ScaleOffY", 0.1f, 5f, 0.01));
    public final SliderSetting scaleOffZ = add(new SliderSetting("ScaleOffZ", 0.1f, 5f, 0.01));
    public final SliderSetting positionOffX = add(new SliderSetting("PositionOffX", -3.0f, 3f, 0.01));
    public final SliderSetting positionOffY = add(new SliderSetting("PositionOffY", -3.0f, 3f, 0.01));
    public final SliderSetting positionOffZ = add(new SliderSetting("PositionOffZ", -3.0f, 3f, 0.01));
    public final SliderSetting rotationOffX = add(new SliderSetting("RotationOffX", -180.0f, 180f, 0.01));
    public final SliderSetting rotationOffY = add(new SliderSetting("RotationOffY", -180.0f, 180f, 0.01));
    public final SliderSetting rotationOffZ = add(new SliderSetting("RotationOffZ", -180.0f, 180f, 0.01));
    public BooleanSetting slowAnimation = add(new BooleanSetting("SlowAnimation"));
    public SliderSetting slowAnimationVal = add(new SliderSetting("SlowValue", 1, 50));
    public final SliderSetting eatX = add(new SliderSetting("EatX", -1f, 2f));
    public final SliderSetting eatY = add(new SliderSetting("EatY", -1f, 2f));

    @Override
    public void onRender3D(MatrixStack matrixStack, float partialTicks) {
        if (oldAnimationsM.getValue() && ((IHeldItemRenderer) mc.getEntityRenderDispatcher().getHeldItemRenderer()).getEquippedProgressMainHand() <= 1f) {
            ((IHeldItemRenderer) mc.getEntityRenderDispatcher().getHeldItemRenderer()).setEquippedProgressMainHand(1f);
            ((IHeldItemRenderer) mc.getEntityRenderDispatcher().getHeldItemRenderer()).setItemStackMainHand(mc.player.getMainHandStack());
        }

        if (oldAnimationsOff.getValue() && ((IHeldItemRenderer) mc.getEntityRenderDispatcher().getHeldItemRenderer()).getEquippedProgressOffHand() <= 1f) {
            ((IHeldItemRenderer) mc.getEntityRenderDispatcher().getHeldItemRenderer()).setEquippedProgressOffHand(1f);
            ((IHeldItemRenderer) mc.getEntityRenderDispatcher().getHeldItemRenderer()).setItemStackOffHand(mc.player.getOffHandStack());
        }
    }

    @EventHandler
    private void onHeldItemRender(HeldItemRendererEvent event) {
        if (event.getHand() == Hand.MAIN_HAND) {
            event.getStack().translate(positionMainX.getValueFloat(), positionMainY.getValueFloat(), positionMainZ.getValueFloat());
            event.getStack().scale(scaleMainX.getValueFloat(), scaleMainY.getValueFloat(), scaleMainZ.getValueFloat());
            event.getStack().multiply(RotationAxis.POSITIVE_X.rotationDegrees(rotationMainX.getValueFloat()));
            event.getStack().multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotationMainY.getValueFloat()));
            event.getStack().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rotationMainZ.getValueFloat()));
            if (swordBlock.getValue() && mc.player.getMainHandStack().getItem() instanceof SwordItem && mc.options.useKey.isPressed() && !mc.player.isUsingItem()) {
                event.getStack().multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-24));
                event.getStack().multiply(RotationAxis.POSITIVE_X.rotationDegrees(50));
                event.getStack().multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-50));
                event.getStack().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(80));
            }
        } else {
            event.getStack().translate(positionOffX.getValueFloat(), positionOffY.getValueFloat(), positionOffZ.getValueFloat());
            event.getStack().scale(scaleOffX.getValueFloat(), scaleOffY.getValueFloat(), scaleOffZ.getValueFloat());
            event.getStack().multiply(RotationAxis.POSITIVE_X.rotationDegrees(rotationOffX.getValueFloat()));
            event.getStack().multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotationOffY.getValueFloat()));
            event.getStack().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rotationOffZ.getValueFloat()));
        }
    }
}
