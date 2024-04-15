package com.anar4732.rg.util;

import net.minecraft.client.KeyMapping;

import static com.anar4732.rg.RickGunMod.MODID;

public class SimpleKeyMapping extends KeyMapping {
	private final Runnable onPress;
	
	public SimpleKeyMapping(String pName, int pKeyCode, Runnable onPress) {
		super(pName, pKeyCode, "key.categories." + MODID);
		this.onPress = onPress;
	}
	
	@Override
	public void setDown(boolean value) {
		super.setDown(value);
		if (value) {
			onPress.run();
		}
	}
}