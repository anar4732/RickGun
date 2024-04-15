package com.anar4732.rg.gui.controls;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.Align;
import team.creative.creativecore.common.gui.GuiChildControl;
import team.creative.creativecore.common.gui.controls.parent.GuiScrollY;
import team.creative.creativecore.common.gui.style.ControlFormatting;
import team.creative.creativecore.common.gui.style.GuiStyle;
import team.creative.creativecore.common.util.math.geo.Rect;

public class GuiScrollYStyled extends GuiScrollY {
	@OnlyIn(Dist.CLIENT)
	private final GuiStyle style;
	private final int scrollThingMaxHeight = 10;
	
	public GuiScrollYStyled(GuiStyle style) {
		this.style = style;
		this.scrollbarWidth = 5;
		this.align = Align.CENTER;
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public GuiStyle getStyle() {
		return style;
	}
	
	@Override
	protected void renderContent(PoseStack matrix, GuiChildControl control, ControlFormatting formatting, int borderWidth, Rect controlRect, Rect realRect, int mouseX, int mouseY) {
		controlRect.shrink(formatting.padding);
		matrix.pushPose();
		matrix.translate(borderWidth + formatting.padding, borderWidth + formatting.padding, 0);
		renderContent(matrix, control, controlRect, controlRect.intersection(realRect), mouseX, mouseY);
		matrix.popPose();
		
		realRect.scissor();
		GuiStyle style = getStyle();
		
		scrolled.tick();
		
		int completeHeight = control.getHeight() - style.getBorder(formatting.border) * 2;
		
		int scrollThingHeight = Math.max(10, Math.min(scrollThingMaxHeight, (int) ((float) scrollThingMaxHeight / cachedHeight * scrollThingMaxHeight)));

		double percent = scrolled.current() / maxScroll;
		
		style.get(ControlFormatting.ControlStyleFace.CLICKABLE, false).render(matrix, controlRect
				.getWidth() + formatting.padding * 2 - scrollbarWidth + borderWidth, (int) (percent * (completeHeight - scrollThingHeight)) + borderWidth, scrollbarWidth, scrollThingHeight);
		
		maxScroll = Math.max(0, (cachedHeight - completeHeight) + formatting.padding * 2 + 1);
	}
}