package com.anar4732.rg.gui.controls;

import net.minecraft.client.Minecraft;
import team.creative.creativecore.common.gui.GuiControl;
import team.creative.creativecore.common.gui.controls.simple.GuiTextfield;
import team.creative.creativecore.common.gui.event.GuiControlEvent;

import javax.annotation.Nullable;

public class GuiTextFieldExtended extends GuiTextfield {
	private String suggestion;
	
	public GuiTextFieldExtended(String name) {
		super(name);
	}
	
	public GuiTextFieldExtended(String name, String text) {
		super(name, text);
	}
	
	@Override
	public void tick() {
		super.tick();
		if (this.suggestion != null && !this.suggestion.isEmpty()) {
			if (this.getText().isEmpty()) {
				super.setSuggestion(this.suggestion);
			} else {
				super.setSuggestion(null);
			}
		}
	}
	
	@Override
	@Deprecated
	public void setSuggestion(@Nullable String s) {
		this.setSuggestionE(s);
	}
	
	public GuiTextFieldExtended setSuggestionE(@Nullable String s) {
		this.suggestion = s;
		super.setSuggestion(s);
		return this;
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == 257) {
			raiseEvent(new GuiTextFieldOnEnterEvent(this));
			return true;
		}
		boolean b = super.keyPressed(keyCode, scanCode, modifiers);
		if (keyCode == Minecraft.getInstance().options.keyInventory.getKey().getValue() && isFocused()) {
			b = true;
		}
		return b;
	}
	
	public static class GuiTextFieldOnEnterEvent extends GuiControlEvent {
		public GuiTextFieldOnEnterEvent(GuiControl control) {
			super(control);
		}
		
		@Override
		public boolean cancelable() {
			return false;
		}
	}
	
}