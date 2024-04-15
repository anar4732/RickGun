package com.anar4732.rg;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CapsuleItem extends Item {
	public CapsuleItem() {
		super(new Properties().tab(CreativeModeTab.TAB_MISC).stacksTo(1));
	}
	
	@Override
	public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
		super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
			pTooltipComponents.add(new TextComponent(""));
			pTooltipComponents.add(new TranslatableComponent("item.rg.capsule.tooltip"));
		});
	}
	
	public int getLevel(ItemStack stack) {
		if (stack.getOrCreateTag().contains("level")) {
			return stack.getTag().getInt("level");
		}
		return 0;
	}
}