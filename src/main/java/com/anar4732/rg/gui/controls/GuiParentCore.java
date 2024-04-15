package com.anar4732.rg.gui.controls;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.loading.FMLEnvironment;
import team.creative.creativecore.common.gui.GuiChildControl;
import team.creative.creativecore.common.gui.GuiControl;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.gui.style.ControlFormatting;
import team.creative.creativecore.common.gui.style.GuiStyle;

public abstract class GuiParentCore extends GuiParent {
	public static final ControlFormatting NESTED_NO_PADDING = new ControlFormatting(ControlFormatting.ControlStyleBorder.NONE, 0, ControlFormatting.ControlStyleFace.NESTED_BACKGROUND);
	
	@OnlyIn(Dist.CLIENT)
	private GuiStyle style;
	
	protected GuiParentCore(String name, GuiFlow flow, int width, int height) {
		super(name, flow, width, height);
	}
	
	@Override
	@Deprecated
	public GuiChildControl add(GuiControl control) {
		return super.add(control);
	}
	
	@Override
	public void init() {
		create();
		super.init();
		reflow();
	}
	
	protected abstract void create();
	
	public GuiChildControl add(GuiControl control, int x, int y, int width, int height) {
		GuiChildControl child = super.add(control);
		child.setX(x);
		child.setY(y);
		child.setWidth(width);
		child.setHeight(height);
		return child;
	}
	
	@OnlyIn(Dist.CLIENT)
	public GuiParentCore setStyle(GuiStyle style) {
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
	
	@Override
	public void flowX(int width, int preferred) {
	}
	
	@Override
	public void flowY(int height, int preferred) {
	}
	
	@Override
	public void reflow() {
		if (FMLEnvironment.dist.isDedicatedServer())
			return;

		for (GuiChildControl child : controls) {
			child.flowX();
			child.flowY();
		}
	}
	
	@Override
	public ControlFormatting getControlFormatting() {
		return NESTED_NO_PADDING;
	}
	
	@Override
	public int getMinHeight() {
		return this.preferredHeight;
	}
	
	@Override
	public int getMinWidth() {
		return this.preferredWidth;
	}
}