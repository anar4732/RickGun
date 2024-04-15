package com.anar4732.rg.gui.controls;

import com.anar4732.rg.util.ClientThings;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.client.render.GuiRenderHelper;
import team.creative.creativecore.common.gui.GuiChildControl;
import team.creative.creativecore.common.gui.controls.inventory.GuiSlotViewer;
import team.creative.creativecore.common.util.math.geo.Rect;

public class GuiSlotViewerCustom extends GuiSlotViewer {
	public GuiSlotViewerCustom(ItemStack stack) {
		super(stack);
	}
	
	@Override
	@OnlyIn(value = Dist.CLIENT)
	protected void renderContent(PoseStack matrix, GuiChildControl control, Rect rect, int mouseX, int mouseY) {
		matrix.pushPose();
		matrix.translate(1, 1, 10);
		GuiRenderHelper.drawItemStack(matrix, getStack(), 1F);
		GuiRenderHelper.drawItemStackDecorations(matrix, getStack());
		matrix.translate(-1, -1, 10);
		if (rect.inside(mouseX, mouseY))
			ClientThings.SLOT_HIGHLIGHT.render(matrix, rect.getWidth(), rect.getHeight());
		matrix.popPose();
	}
}