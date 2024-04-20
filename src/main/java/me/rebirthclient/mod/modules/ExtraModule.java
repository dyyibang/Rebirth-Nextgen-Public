package me.rebirthclient.mod.modules;

import me.rebirthclient.Rebirth;
import me.rebirthclient.api.util.Wrapper;
import net.minecraft.client.util.math.MatrixStack;

public class ExtraModule implements Wrapper {
    public ExtraModule() {
        Rebirth.EVENT_BUS.subscribe(this);
    }

    public void onRender3D(MatrixStack matrixStack, float partialTicks) {

    }
}
