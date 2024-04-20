package me.rebirthclient.mod;

import me.rebirthclient.api.util.Wrapper;

public class Mod implements Wrapper {
    public String name;
    public Mod(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
