package com.anar4732.rg.network;

import com.anar4732.rg.PortalGunItem;
import com.anar4732.rg.gui.PortalGunGUI;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import team.creative.creativecore.common.network.CreativePacket;

public class OpenPortalConfigMenuPacket extends CreativePacket {
	@Override
	public void executeClient(Player player) {
	
	}
	
	@Override
	public void executeServer(ServerPlayer player) {
		ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
		if (stack.getItem() instanceof PortalGunItem) {
			CompoundTag persistent = player.getPersistentData();
			CompoundTag tag = new CompoundTag();
			if (persistent.contains("portal_gun_cords")) {
				tag.put("portal_gun_cords", persistent.getList("portal_gun_cords", 10));
			}
			if (stack.getOrCreateTag().contains("portal_gun_cords")) {
				tag.put("curr_portal_gun_cords", stack.getTag().get("portal_gun_cords"));
			}
			PortalGunGUI.GUI.open(tag, player);
		}
	}
}