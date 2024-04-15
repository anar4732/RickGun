package com.anar4732.rg.network;

import com.anar4732.rg.PortalGunItem;
import com.anar4732.rg.portal.PortalTE;
import com.anar4732.rg.util.TeleportationTools;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import team.creative.creativecore.common.network.CreativePacket;

public class ResetPortalsPacket extends CreativePacket {
	@Override
	public void executeClient(Player player) {
	
	}
	
	@Override
	public void executeServer(ServerPlayer player) {
		ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
		if (stack.getItem() instanceof PortalGunItem) {
			PortalTE.PORTALS.stream().filter(te -> te.isOwner(player) && te.getTimeout() > 20).forEach(portalTE -> portalTE.setTimeout(20));
		}
	}
}