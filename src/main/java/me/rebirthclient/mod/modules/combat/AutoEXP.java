package me.rebirthclient.mod.modules.combat;

import me.rebirthclient.api.events.eventbus.EventHandler;
import me.rebirthclient.api.events.eventbus.EventPriority;
import me.rebirthclient.api.events.impl.RotateEvent;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.api.util.InventoryUtil;
import me.rebirthclient.api.util.Timer;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.settings.impl.BindSetting;
import me.rebirthclient.mod.settings.impl.BooleanSetting;
import me.rebirthclient.mod.settings.impl.SliderSetting;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;


public class AutoEXP extends Module {


    public static AutoEXP INSTANCE;
    public final BindSetting throwBind =
            add(new BindSetting("ThrowBind", -1));
    private final SliderSetting delay =
            add(new SliderSetting("Delay", 0, 5));
    public final BooleanSetting down =
            add(new BooleanSetting("Down"));
    public final BooleanSetting allowGui =
            add(new BooleanSetting("AllowGui"));
    public final BooleanSetting onlyBroken =
            add(new BooleanSetting("OnlyBroken"));
    private final BooleanSetting usingPause =
            add(new BooleanSetting("UsingPause"));
    private final BooleanSetting onlyGround =
            add(new BooleanSetting("OnlyGround"));
    private final BooleanSetting inventory =
            add(new BooleanSetting("InventorySwap"));
    private final Timer delayTimer = new Timer();

    public AutoEXP() {
        super("AutoEXP", Category.Combat);
        INSTANCE = this;
    }

    private boolean throwing = false;

    @Override
    public void onUpdate() {
        throwing = checkThrow();
        if (isThrow() && delayTimer.passedMs(delay.getValueInt() * 20L) && (!onlyGround.getValue() || mc.player.isOnGround()))
            throwExp();
    }

    public void throwExp() {
        int oldSlot = mc.player.getInventory().selectedSlot;
        int newSlot;
        if (inventory.getValue() && (newSlot = InventoryUtil.findItemInventorySlot(Items.EXPERIENCE_BOTTLE)) != -1) {
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, newSlot, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
            mc.player.networkHandler.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, EntityUtil.getWorldActionId(mc.world)));
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, newSlot, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
            EntityUtil.sync();
            delayTimer.reset();
        } else if ((newSlot = InventoryUtil.findItem(Items.EXPERIENCE_BOTTLE)) != -1) {
            InventoryUtil.doSwap(newSlot);
            mc.player.networkHandler.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, EntityUtil.getWorldActionId(mc.world)));
            InventoryUtil.doSwap(oldSlot);
            delayTimer.reset();
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void RotateEvent(RotateEvent event) {
        if (!down.getValue()) return;
        if (isThrow()) event.setPitch(90);
    }

    public boolean isThrow() {
        return throwing;
    }

    public boolean checkThrow() {
        if (isOff()) return false;
        if (mc.currentScreen instanceof ChatScreen) return false;
        if (!allowGui.getValue() && mc.currentScreen != null) return false;
        if (usingPause.getValue() && mc.player.isUsingItem()) {
            return false;
        }
        if (InventoryUtil.findItem(Items.EXPERIENCE_BOTTLE) == -1 && (!inventory.getValue() || InventoryUtil.findItemInventorySlot(Items.EXPERIENCE_BOTTLE) == -1))
            return false;
        if (!throwBind.isPressed()) return false;
        if (onlyBroken.getValue()) {
            DefaultedList<ItemStack> armors = mc.player.getInventory().armor;
            for (ItemStack armor : armors) {
                if (armor.isEmpty()) continue;
                if (EntityUtil.getDamagePercent(armor) >= 100) continue;
                return true;
            }
        } else {
            return true;
        }
        return false;
    }
}
