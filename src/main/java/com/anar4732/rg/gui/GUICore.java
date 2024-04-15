package com.anar4732.rg.gui;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.loading.FMLEnvironment;
import team.creative.creativecore.common.gui.GuiChildControl;
import team.creative.creativecore.common.gui.GuiControl;
import team.creative.creativecore.common.gui.GuiLayer;

public abstract class GUICore extends GuiLayer {
	protected GUICore(String name, int width, int height, CompoundTag nbt) {
		super(name, width, height);
		if (FMLEnvironment.dist.isClient()) {
			initClient(nbt);
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	protected abstract void initClient(CompoundTag nbt);
	
	@Override
	@Deprecated
	public GuiChildControl add(GuiControl control) {
		return super.add(control);
	}
	
	public GuiChildControl add(GuiControl control, int y) {
		return add(control, (this.getWidth() / 2) - (control.getPreferredWidth() / 2), y);
	}
	
	public GuiChildControl add(GuiControl control, int x, int y) {
		return add(control, x, y, control.getPreferredWidth(), control.getPreferredHeight());
	}
	
	public GuiChildControl add(GuiControl control, int x, int y, int width, int height) {
		GuiChildControl child = super.add(control);
		child.setX(x);
		child.setY(y);
		if (FMLEnvironment.dist.isClient()) {
			child.setWidth(width);
			child.setHeight(height);
		}
		return child;
	}
	
	@Override
	public void reflow() {
		if (FMLEnvironment.dist.isDedicatedServer())
			return;
		
		if (!hasPreferredDimensions) {
			rect.maxX = preferredWidth() + getContentOffset() * 2;
			rect.maxY = preferredHeight() + getContentOffset() * 2;
		}
		
		for (GuiChildControl child : controls) {
			child.flowX();
			child.flowY();
		}
	}
}