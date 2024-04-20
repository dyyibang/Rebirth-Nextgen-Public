package me.rebirthclient.mod.modules.player;

import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.settings.impl.BooleanSetting;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.SwordItem;

public class NoTrace extends Module {

    public static NoTrace INSTANCE;
    public final BooleanSetting onlyPickaxe =
            add(new BooleanSetting("OnlyPickaxe"));
    public NoTrace() {
        super("NoTrace", Category.Player);
        INSTANCE = this;
    }

    public boolean canWork() {
        if (isOff()) return false;

        if (onlyPickaxe.getValue()) {
            return mc.player.getMainHandStack().getItem() instanceof PickaxeItem || mc.player.isUsingItem() && !(mc.player.getMainHandStack().getItem() instanceof SwordItem);
        }
        return true;
    }
}
