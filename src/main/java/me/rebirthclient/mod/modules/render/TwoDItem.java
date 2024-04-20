package me.rebirthclient.mod.modules.render;

import me.rebirthclient.mod.modules.Module;

public class TwoDItem extends Module {

    public static TwoDItem INSTANCE;
    public TwoDItem() {
        super("2DItem", Category.Render);
        INSTANCE = this;
    }
}
