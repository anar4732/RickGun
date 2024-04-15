package com.anar4732.rg.util;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.style.GuiStyle;
import team.creative.creativecore.common.gui.style.display.DisplayColor;
import team.creative.creativecore.common.gui.style.display.DisplayTextureStretch;
import team.creative.creativecore.common.gui.style.display.StyleDisplay;

import static com.anar4732.rg.gui.PortalGunGUI.TEXTURE_MAIN;

@OnlyIn(Dist.CLIENT)
public class ClientThings {
	public static final GuiStyle STYLE_SCROLL = new GuiStyle() {{
		border = StyleDisplay.NONE;
		borderThick = StyleDisplay.NONE;
		secondaryBackground = StyleDisplay.NONE;
		clickable = new DisplayTextureStretch(TEXTURE_MAIN, 243, 134, 13, 16);
	}};
	
	public static final GuiStyle BIG_SLOT_STYLE = new GuiStyle() {{
		border = StyleDisplay.NONE;
		borderThick = StyleDisplay.NONE;
		slot = new DisplayTextureStretch(TEXTURE_MAIN, 0, 175, 36, 36);
	}};
	
	public static final DisplayColor SLOT_HIGHLIGHT = new DisplayColor(1, 1, 1, 0.1F);
}