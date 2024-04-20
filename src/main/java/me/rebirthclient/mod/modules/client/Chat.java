package me.rebirthclient.mod.modules.client;

import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.settings.impl.BooleanSetting;
import me.rebirthclient.mod.settings.impl.SliderSetting;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;

import java.util.HashMap;

public class Chat extends Module {
    public static Chat INSTANCE;
    public SliderSetting animateTime = add(new SliderSetting("AnimationTime", 0, 1000));
    public SliderSetting animateOffset = add(new SliderSetting("AnimationOffset", -200, 100));
    public BooleanSetting keepHistory = add(new BooleanSetting("KeepHistory"));
    public BooleanSetting infiniteChat = add(new BooleanSetting("InfiniteChat"));
    public Chat() {
        super("Chat", Category.Client);
        INSTANCE = this;
    }
    public static HashMap<OrderedText, StringVisitable> chatMessage = new HashMap<>();
    @Override
    public void enable() {
        this.state = true;
    }

    @Override
    public void disable() {
        this.state = true;
    }

    @Override
    public boolean isOn() {
        return true;
    }
}
