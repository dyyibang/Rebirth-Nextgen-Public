package me.rebirthclient.mod.modules.combat;

import me.rebirthclient.api.events.eventbus.EventHandler;
import me.rebirthclient.api.events.eventbus.EventPriority;
import me.rebirthclient.api.events.impl.PacketEvent;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.api.util.InventoryUtil;
import me.rebirthclient.api.util.Timer;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.settings.impl.EnumSetting;
import me.rebirthclient.mod.settings.impl.SliderSetting;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.screen.slot.SlotActionType;

public class AntiWeak extends Module {
    public AntiWeak() {
        super("AntiWeak", "anti weak", Category.Combat);
    }
    private final SliderSetting delay = add(new SliderSetting("Delay", 0, 500));
    private final EnumSetting swapMode =
            add(new EnumSetting("SwapMode", SwapMode.Bypass));
    public enum SwapMode {
        Normal, Silent, Bypass
    }

    @Override
    public String getInfo() {
        return swapMode.getValue().name();
    }

    private final Timer delayTimer = new Timer();
    private PlayerInteractEntityC2SPacket lastPacket = null;
    boolean ignore = false;
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPacketSend(PacketEvent.Send event) {
        if (nullCheck()) return;
        if (event.isCancel()) return;
        if (ignore) return;
        if (mc.player.getStatusEffect(StatusEffects.WEAKNESS) == null) return;
        if (mc.player.getMainHandStack().getItem() instanceof SwordItem)
            return;
        if (!delayTimer.passedMs(delay.getValue())) return;
        if (event.getPacket() instanceof PlayerInteractEntityC2SPacket packet && Criticals.getInteractType(packet) == Criticals.InteractType.ATTACK) {
            lastPacket = event.getPacket();
            delayTimer.reset();
            ignore = true;
            doAnti();
            ignore = false;
            event.cancel();
        }
    }
    private void doAnti() {
        if (lastPacket == null) return;
        int strong;
        if (swapMode.getValue() != SwapMode.Bypass) {
            strong = InventoryUtil.findClass(SwordItem.class);
        } else {
            strong = InventoryUtil.findClassInventorySlot(SwordItem.class);
        }
        if (strong == -1) return;
        int old = mc.player.getInventory().selectedSlot;
        if (swapMode.getValue() != SwapMode.Bypass) {
            InventoryUtil.doSwap(strong);
        } else {
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, strong, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
        }
        mc.player.networkHandler.sendPacket(lastPacket);
        if (swapMode.getValue() != SwapMode.Bypass) {
            if (swapMode.getValue() != SwapMode.Normal) InventoryUtil.doSwap(old);
        } else {
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, strong, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
            EntityUtil.sync();
        }
    }
}
