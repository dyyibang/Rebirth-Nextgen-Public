package me.rebirthclient.mod.settings.impl;

import me.rebirthclient.Rebirth;
import me.rebirthclient.api.managers.ModuleManager;
import me.rebirthclient.mod.settings.Setting;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;

public class BindSetting extends Setting {
    private boolean isListening = false;
    private int key;
    private boolean isDown = false;
    public BindSetting(String name, int key) {
        super(name, ModuleManager.lastLoadMod.getName() + "_" + name);
        this.key = key;
    }

    @Override
    public void loadSetting() {
        setKey(Rebirth.CONFIG.getSettingInt(getLine(), key));
    }

    public int getKey() {
        return this.key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public String getBind() {
        if (key == -1) return "None";
        String kn = this.key > 0 ? GLFW.glfwGetKeyName(this.key, GLFW.glfwGetKeyScancode(this.key)) : "None";
        if (kn == null) {
            try {
                for (Field declaredField : GLFW.class.getDeclaredFields()) {
                    if (declaredField.getName().startsWith("GLFW_KEY_")) {
                        int a = (int) declaredField.get(null);
                        if (a == this.key) {
                            String nb = declaredField.getName().substring("GLFW_KEY_".length());
                            kn = nb.substring(0, 1).toUpperCase() + nb.substring(1).toLowerCase();
                        }
                    }
                }
            } catch (Exception ignored) {
                kn = "None";
            }
        }

        return (kn + "").toUpperCase();
    }

    public void setListening(boolean set) {
        isListening = set;
    }

    public boolean isListening() {
        return isListening;
    }

    public void setDown(boolean down) {
        this.isDown = down;
    }

    public boolean isPressed() {
        return isDown;
    }
}
