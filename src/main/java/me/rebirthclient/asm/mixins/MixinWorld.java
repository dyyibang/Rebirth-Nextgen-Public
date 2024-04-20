package me.rebirthclient.asm.mixins;


import me.rebirthclient.api.util.CombatUtil;
import me.rebirthclient.mod.modules.player.GhostHand;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.rebirthclient.api.util.Wrapper.mc;
@Mixin(World.class)
public abstract class MixinWorld {

    @Inject(method = "getBlockState", at = @At("HEAD"), cancellable = true)
    public void blockStateHook(BlockPos pos, CallbackInfoReturnable<BlockState> cir) {
        if (mc.world != null && mc.world.isInBuildLimit(pos)) {
            if (CombatUtil.terrainIgnore) {
                WorldChunk worldChunk = mc.world.getChunk(pos.getX() >> 4, pos.getZ() >> 4);

                BlockState tempState = worldChunk.getBlockState(pos);
                if (tempState.getBlock() == Blocks.OBSIDIAN
                        || tempState.getBlock() == Blocks.BEDROCK
                        || tempState.getBlock() == Blocks.ENDER_CHEST
                        || tempState.getBlock() == Blocks.RESPAWN_ANCHOR
                ) return;
                cir.setReturnValue(Blocks.AIR.getDefaultState());
            } else if (GhostHand.INSTANCE.isActive) {
                WorldChunk worldChunk = mc.world.getChunk(pos.getX() >> 4, pos.getZ() >> 4);

                BlockState tempState = worldChunk.getBlockState(pos);
                if (tempState.getBlock() == Blocks.BEDROCK
                ) {
                    cir.setReturnValue(Blocks.AIR.getDefaultState());
                }
            }
        }
    }
}
