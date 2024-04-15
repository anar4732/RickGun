package com.anar4732.rg.portal;

import com.anar4732.rg.RickGunMod;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GLUtil;
import team.creative.creativecore.common.util.type.Color;

@OnlyIn(Dist.CLIENT)
public class PortalProjectileRender<T extends Entity> extends EntityRenderer<T> {
    private final ItemRenderer itemRenderer;

    public PortalProjectileRender(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }
	
	@Override
	public void render(@NotNull T entity, float pEntityYaw, float pPartialTick, PoseStack stack, MultiBufferSource pBuffer, int pPackedLight) {
		super.render(entity, pEntityYaw, pPartialTick, stack, pBuffer, pPackedLight);
		
		stack.pushPose();
		stack.mulPose(Vector3f.YP.rotationDegrees(-this.entityRenderDispatcher.camera.getYRot()));
		stack.mulPose(Vector3f.XP.rotationDegrees((this.entityRenderDispatcher.options.getCameraType().ordinal() == 2 ? -1 : 1) * this.entityRenderDispatcher.camera.getXRot()));
		stack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
		
		ItemStack itemStack = new ItemStack(RickGunMod.PORTAL_PROJECTILE.get());
		itemStack.getOrCreateTag().putInt("color", ((PortalProjectile) entity).getColor());
		
		this.itemRenderer.renderStatic(itemStack, ItemTransforms.TransformType.GROUND, 15728880, OverlayTexture.NO_OVERLAY, stack, pBuffer, 0);

		stack.popPose();
	}
	
    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull Entity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}