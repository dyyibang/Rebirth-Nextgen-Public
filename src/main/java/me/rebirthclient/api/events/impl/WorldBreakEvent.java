package me.rebirthclient.api.events.impl;

import me.rebirthclient.api.events.Event;
import net.minecraft.client.render.BlockBreakingInfo;
import net.minecraft.util.math.BlockPos;

public class WorldBreakEvent extends Event {
    private final BlockBreakingInfo blockBreakingInfo;
    public WorldBreakEvent(BlockBreakingInfo pos) {
        super(Stage.Pre);
        this.blockBreakingInfo = pos;
    }

    public BlockPos getPos() {
        return blockBreakingInfo.getPos();
    }

    public int getId() {
        return blockBreakingInfo.getActorId();
    }
}
