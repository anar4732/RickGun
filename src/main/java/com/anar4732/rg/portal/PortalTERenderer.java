package com.anar4732.rg.portal;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;
import team.creative.creativecore.common.util.type.Color;

import static com.anar4732.rg.RickGunMod.MODID;

@OnlyIn(Dist.CLIENT)
public class PortalTERenderer implements BlockEntityRenderer<PortalTE> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(MODID, "textures/effects/portal.png");

    private static double angle = 0;
	
	public static BlockEntityRenderer<PortalTE> create(BlockEntityRendererProvider.Context pContext) {
		return new PortalTERenderer();
	}
	
    @Override
    public void render(PortalTE te, float pPartialTick, PoseStack stack, MultiBufferSource bufferSource, int pPackedLight, int pPackedOverlay) {
        if (te.getPortalSide() == null) {
            return;
        }
	    
	    stack.pushPose();
        RenderSystem.depthMask(true);
	    RenderSystem.enableBlend();
	    RenderSystem.enableDepthTest();
	    RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
	    
	    RenderSystem.setShader(GameRenderer::getPositionTexLightmapColorShader);
	    Color color = new Color(te.getColor());
	    RenderSystem.setShaderColor(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, 1.0F);
	    RenderSystem.setShaderTexture(0, TEXTURE);
		
        long time = System.currentTimeMillis();
        angle = (time / 400.0) % 360.0;
        float scale = 1.0f;
        int start = te.getStart();
        if (start < 20) {
            scale = start / 20.0f;
        } else {
            int timeout = te.getTimeout();
            if (timeout < 20) {
                scale = timeout / 20.0f;
            }
        }

        Face face = faces[te.getPortalSide().ordinal()];
        Face rface = revertedfaces[te.getPortalSide().ordinal()];
		
	    stack.translate(face.ox, face.oy, face.oz);
	    stack.scale(scale, scale, scale);
        renderQuadBright(stack, angle, face, rface);
	    stack.popPose();
		
		if (Minecraft.getInstance().getEntityRenderDispatcher().shouldRenderHitBoxes()) {
			AABB box = te.getTeleportBox().move(-te.getBlockPos().getX(), -te.getBlockPos().getY(), -te.getBlockPos().getZ());
			LevelRenderer.renderLineBox(stack, bufferSource.getBuffer(RenderType.lines()), box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, 0.0F, 1.0F, 0.0F, 1.0F);
	    }
    }
	
    public static void renderQuadBright(PoseStack stack, double angle, Face face, Face rface) {
        int brightness = 240;
        int b1 = brightness >> 16 & 65535;
        int b2 = brightness & 65535;
	    BufferBuilder buffer = Tesselator.getInstance().getBuilder();
	    buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_LIGHTMAP_COLOR);
        double f = 1.75;
	    Matrix4f matrix4f = stack.last().pose();
        renderFace(matrix4f, face, angle, b1, b2, buffer, f, 200);
        renderFace(matrix4f, face, -angle, b1, b2, buffer, f, 60);
        renderFace(matrix4f, rface, angle, b1, b2, buffer, f, 200);
        renderFace(matrix4f, rface, -angle, b1, b2, buffer, f, 60);
	    Tesselator.getInstance().end();
    }

    private static Face[] faces = new Face[6];
    private static Face[] revertedfaces = new Face[6];

    static {
        float half = 1.0F;
        faces[Direction.DOWN.ordinal()] =  new Face(0.5F, 0.9F, 0.5F,    -1, 0, -1,       -1, 0, 1,      1, 0, 1,      1, 0, -1);
        faces[Direction.UP.ordinal()] =    new Face(0.5F, 0.1F, 0.5F,    -1, 0, -1,       -1, 0, 1,      1, 0, 1,      1, 0, -1);
        faces[Direction.SOUTH.ordinal()] = new Face(0.5F, 1, 0.1F,      -half, -1, 0,    -half, 1, 0,   half, 1, 0,   half, -1, 0);
        faces[Direction.NORTH.ordinal()] = new Face(0.5F, 1, 0.9F,      -half, -1, 0,    -half, 1, 0,   half, 1, 0,   half, -1, 0);
        faces[Direction.EAST.ordinal()] =  new Face(0.1F, 1, 0.5F,       0, -1, -half,   0, 1, -half,   0, 1, half,   0, -1, half);
        faces[Direction.WEST.ordinal()] =  new Face(0.9F, 1, 0.5F,       0, -1, -half,   0, 1, -half,   0, 1, half,   0, -1, half);
        for (Direction facing : Direction.BY_3D_DATA) {
            revertedfaces[facing.ordinal()] = faces[facing.ordinal()].reverse();
        }
    }
	
	private record Face(float ox, float oy, float oz, float x0, float y0, float z0, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3) {
		public Face reverse() {
			return new Face(ox, oy, oz, x3, y3, z3, x2, y2, z2, x1, y1, z1, x0, y0, z0);
		}
	}

    private static void renderFace(Matrix4f matrix4f, Face face, double angle, int b1, int b2, VertexConsumer vertexConsumer, double f, int alpha) {
	    float u = (float) ((Math.cos(angle)) / f);
	    float v = (float) ((Math.sin(angle)) / f);
	    float swap;
	    vertexConsumer.vertex(matrix4f, face.x0, face.y0, face.z0).uv(u+.5F, v+.5F).uv2(b1, b2).color(255, 255, 255, alpha).endVertex();
	    
	    swap = u;
	    u = -v;
	    v = swap;
	    vertexConsumer.vertex(matrix4f, face.x1, face.y1, face.z1).uv(u+.5F, v+.5F).uv2(b1, b2).color(255, 255, 255, alpha).endVertex();
	    
	    swap = u;
	    u = -v;
	    v = swap;
	    vertexConsumer.vertex(matrix4f, face.x2, face.y2, face.z2).uv(u+.5F, v+.5F).uv2(b1, b2).color(255, 255, 255, alpha).endVertex();
	    
	    swap = u;
	    u = -v;
	    v = swap;
	    vertexConsumer.vertex(matrix4f, face.x3, face.y3, face.z3).uv(u+.5F, v+.5F).uv2(b1, b2).color(255, 255, 255, alpha).endVertex();
    }
}