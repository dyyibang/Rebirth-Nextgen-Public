package me.rebirthclient.mod.modules.player;

import me.rebirthclient.api.events.eventbus.EventHandler;
import me.rebirthclient.api.events.impl.PacketEvent;
import me.rebirthclient.api.util.BlockUtil;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.api.util.MovementUtil;
import me.rebirthclient.mod.modules.Module;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

public class OBSClip extends Module {
    public OBSClip() {
        super("OBSClip", Category.Player);
    }

    @EventHandler
    public void onPacket(PacketEvent.Send event) {
        if (nullCheck()) return;
        if (!mc.player.isOnGround() || MovementUtil.isMoving()) return;
        if (!newBedrockCheck()) return;
        if (event.getPacket() instanceof PlayerMoveC2SPacket) {
            event.cancel();
        }
    }

    public static boolean newBedrockCheck() {
        BlockPos playerBlockPos = EntityUtil.getPlayerPos(true);
        for (int xOffset = -1; xOffset <= 1; xOffset++) {
            for (int yOffset = -1; yOffset <= 1; yOffset++) {
                for (int zOffset = -1; zOffset <= 1; zOffset++) {
                    BlockPos offsetPos = playerBlockPos.add(xOffset, yOffset, zOffset);
                    if (mc.world.getBlockState(offsetPos).getBlock() == Blocks.BEDROCK) {
                        if (mc.player.getBoundingBox().intersects(new Box(offsetPos))) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
    public static boolean isInsideBedrock() {
        for (int y = 0; y < 2; y++) {
            BlockPos pos = EntityUtil.getPlayerPos().up(y);
            for (Direction i : Direction.values()) {
                if (i == Direction.UP || i == Direction.DOWN) continue;
                if (BlockUtil.getBlock(pos.offset(i)) == Blocks.BEDROCK) {
                    if (mc.player.getBoundingBox().intersects(new Box(pos.offset(i)))) return true;
                }
            }
        }
        return false;
    }
}
