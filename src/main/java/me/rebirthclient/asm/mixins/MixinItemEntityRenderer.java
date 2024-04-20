package me.rebirthclient.asm.mixins;

import me.rebirthclient.mod.modules.render.TwoDItem;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.rebirthclient.api.util.Wrapper.mc;

@Mixin(ItemEntityRenderer.class)
public abstract class MixinItemEntityRenderer extends EntityRenderer<ItemEntity> {
    @Final
    @Shadow
    private ItemRenderer itemRenderer;

    @Final
    @Shadow
    private Random random;
    @Shadow
    private int getRenderedAmount(ItemStack stack) {
        int i = 1;
        if (stack.getCount() > 48) {
            i = 5;
        } else if (stack.getCount() > 32) {
            i = 4;
        } else if (stack.getCount() > 16) {
            i = 3;
        } else if (stack.getCount() > 1) {
            i = 2;
        }

        return i;
    }
    protected MixinItemEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Inject(method = "render(Lnet/minecraft/entity/ItemEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("HEAD"), cancellable = true)
    public void render(ItemEntity itemEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        if (!TwoDItem.INSTANCE.isOn()) return;
        ci.cancel();
        matrixStack.push();
        ItemStack itemStack = itemEntity.getStack();
        int j = itemStack.isEmpty() ? 187 : Item.getRawId(itemStack.getItem()) + itemStack.getDamage();
        this.random.setSeed(j);
        BakedModel bakedModel = this.itemRenderer.getModel(itemStack, itemEntity.getWorld(), null, itemEntity.getId());
        boolean bl = bakedModel.hasDepth();
        int k = this.getRenderedAmount(itemStack);
        float l = MathHelper.sin(((float)itemEntity.getItemAge() + g) / 10.0F + itemEntity.uniqueOffset) * 0.1F + 0.1F;
        float m = bakedModel.getTransformation().getTransformation(ModelTransformationMode.GROUND).scale.y();
        matrixStack.translate(0.0F, l + 0.25F * m, 0.0F);
        double cameraX = mc.gameRenderer.getCamera().getPos().x;
        double cameraY = mc.gameRenderer.getCamera().getPos().y;
        double cameraZ = mc.gameRenderer.getCamera().getPos().z;
        double dx = itemEntity.getX() - cameraX;
        double dy = itemEntity.getY() - cameraY;
        double dz = itemEntity.getZ() - cameraZ;
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
        float pitch = (float) MathHelper.wrapDegrees(-Math.toDegrees(Math.atan2(dy, horizontalDistance)));

        double angle = Math.atan2(dz, -dx) * (180 / Math.PI) - 90;
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) angle));
        matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(pitch));
        if (itemStack.getItem() instanceof BlockItem) {
            matrixStack.scale(1.4f, 1.4f, 1.4f);
        } else {
            matrixStack.scale(1.1f, 1.1f, 1.1f);
        }
        float o = bakedModel.getTransformation().ground.scale.x();
        float p = bakedModel.getTransformation().ground.scale.y();
        float q = bakedModel.getTransformation().ground.scale.z();
        float s;
        float t;
        if (!bl) {
            float r = -0.0F * (float)(k - 1) * 0.5F * o;
            s = -0.0F * (float)(k - 1) * 0.5F * p;
            t = -0.09375F * (float)(k - 1) * 0.5F * q;
            matrixStack.translate(r, s, t);
        }

        for(int u = 0; u < k; ++u) {
            matrixStack.push();
            if (u > 0) {
                if (bl) {
                    s = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    t = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    float v = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    matrixStack.translate(s, t, v);
                } else {
                    s = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
                    t = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
                    matrixStack.translate(s, t, 0.0F);
                }
            }

            this.itemRenderer.renderItem(itemStack, ModelTransformationMode.GROUND, false, matrixStack, vertexConsumerProvider, i, OverlayTexture.DEFAULT_UV, bakedModel);
            matrixStack.pop();
            if (!bl) {
                matrixStack.translate(0.0F * o, 0.0F * p, 0.09375F * q);
            }
        }

        matrixStack.pop();
        super.render(itemEntity, f, g, matrixStack, vertexConsumerProvider, i);
    }
}
