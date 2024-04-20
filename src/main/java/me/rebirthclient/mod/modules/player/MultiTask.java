package me.rebirthclient.mod.modules.player;

import me.rebirthclient.mod.modules.Module;

public class MultiTask extends Module {
    public static MultiTask INSTANCE;
    public MultiTask() {
        super("MultiTask", Category.Player);
        INSTANCE = this;
    }
}
