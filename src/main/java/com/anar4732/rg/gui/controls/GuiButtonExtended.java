package com.anar4732.rg.gui.controls;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.GuiChildControl;
import team.creative.creativecore.common.gui.controls.simple.GuiButton;
import team.creative.creativecore.common.gui.style.GuiStyle;
import team.creative.creativecore.common.util.math.geo.Rect;

import java.util.function.Consumer;

public class GuiButtonExtended extends GuiButton {
	
	@OnlyIn(Dist.CLIENT)
	private GuiStyle style;
	
	@OnlyIn(Dist.CLIENT)
	private int textTopPadding;
	
	public GuiButtonExtended(String name, Consumer<Integer> pressed) {
		super(name, pressed);
	}
	
	public GuiButtonExtended(String name, int width, int height, Consumer<Integer> pressed) {
		super(name, width, height, pressed);
	}
	
	@OnlyIn(Dist.CLIENT)
	public GuiButtonExtended setStyle(GuiStyle style) {
		this.style = style;
		return this;
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public GuiStyle getStyle() {
		if (style == null)
			return super.getStyle();
		return style;
	}
	
	@OnlyIn(Dist.CLIENT)
	public GuiButtonExtended setTextTopPadding(int lineSpacing) {
		this.textTopPadding = lineSpacing;
		return this;
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	protected void renderContent(PoseStack matrix, GuiChildControl control, Rect rect, int mouseX, int mouseY) {
		matrix.pushPose();
		if (textTopPadding != 0) {
			matrix.translate(0, textTopPadding, 0);
		}
		super.renderContent(matrix, control, rect, mouseX, mouseY);
		matrix.popPose();
	}
	
}