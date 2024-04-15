package com.anar4732.rg.util;

import com.anar4732.rg.CapsuleItem;
import com.anar4732.rg.RickGunMod;
import com.google.common.collect.Lists;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CapsuleFillingRecipe extends CustomRecipe {
	public CapsuleFillingRecipe(ResourceLocation rl) {
		super(rl);
	}
	
	@Override
	public boolean matches(CraftingContainer inv, @NotNull Level level) {
		ItemStack main = ItemStack.EMPTY;
		List<ItemStack> list = Lists.newArrayList();
		
		for (int i = 0; i < inv.getContainerSize(); ++i) {
			ItemStack stack = inv.getItem(i);
			if (!stack.isEmpty()) {
				if (stack.getItem() instanceof CapsuleItem) {
					if (!main.isEmpty()) {
						return false;
					}
					
					main = stack;
				} else {
					if (!(stack.getItem() instanceof EnderpearlItem)) {
						return false;
					}
					
					list.add(stack);
				}
			}
		}
		
		return !main.isEmpty() && !list.isEmpty();
	}
	
	@Override
	public @NotNull ItemStack assemble(CraftingContainer pInv) {
		List<EnderpearlItem> list = Lists.newArrayList();
		ItemStack main = ItemStack.EMPTY;
		
		for (int i = 0; i < pInv.getContainerSize(); ++i) {
			ItemStack stack = pInv.getItem(i);
			if (!stack.isEmpty()) {
				Item item = stack.getItem();
				if (item instanceof CapsuleItem) {
					if (!main.isEmpty()) {
						return ItemStack.EMPTY;
					}
					
					main = stack.copy();
				} else {
					if (!(item instanceof EnderpearlItem)) {
						return ItemStack.EMPTY;
					}
					
					list.add((EnderpearlItem) item);
				}
			}
		}
		
		if (!main.isEmpty() && !list.isEmpty()) {
			int levels2Add = list.size();
			int levels = main.getOrCreateTag().contains("level") ? main.getTag().getInt("level") : 0;
			if (levels + levels2Add <= 4 && levels + levels2Add > 0) {
				main.getOrCreateTag().putInt("level", levels + levels2Add);
				return main;
			}
		}
		
		return ItemStack.EMPTY;
	}
	
	@Override
	public boolean canCraftInDimensions(int pWidth, int pHeight) {
		return pWidth * pHeight >= 2;
	}
	
	@Override
	public @NotNull RecipeSerializer<?> getSerializer() {
		return RickGunMod.CAPSULE_FILLING_RECIPE.get();
	}
}