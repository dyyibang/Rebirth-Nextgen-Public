package me.rebirthclient.asm.mixins;

import me.rebirthclient.api.interfaces.IChatHudLine;
import net.minecraft.client.gui.hud.ChatHudLine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ChatHudLine.Visible.class)
public class MixinChatHudLineVisible implements IChatHudLine {
    @Unique
    private int id = 0;
    @Override
    public int rebirth_nextgen_master$getId() {
        return id;
    }

    @Override
    public void rebirth_nextgen_master$setId(int id) {
        this.id = id;
    }
}
