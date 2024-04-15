package com.anar4732.rg;

import com.anar4732.rg.portal.PortalProjectile;
import com.anar4732.rg.util.TeleportDestination;
import com.anar4732.rg.util.TeleportationTools;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import org.jetbrains.annotations.Nullable;
import team.creative.creativecore.common.util.type.Color;

import java.util.List;

public class PortalGunItem extends Item {
	public PortalGunItem() {
		super(new Properties().tab(CreativeModeTab.TAB_MISC).stacksTo(1));
	}
	
	@Override
	public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
		super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
			pTooltipComponents.add(new TextComponent(""));
			pTooltipComponents.add(new TranslatableComponent("item.rg.portal_gun.tooltip", RickGunModClient.keyPortalGunConfig.getTranslatedKeyMessage().getString().toUpperCase(), RickGunModClient.keyResetPortals.getTranslatedKeyMessage().getString().toUpperCase()));
		});
	}
	
	public InteractionResultHolder<ItemStack> use(Level level, Player pPlayer, InteractionHand pUsedHand) {
		if (level.isClientSide || pUsedHand != InteractionHand.MAIN_HAND) {
			return InteractionResultHolder.success(pPlayer.getItemInHand(pUsedHand));
		}
		
		ServerPlayer player = (ServerPlayer) pPlayer;
		ItemStack stack = pPlayer.getItemInHand(pUsedHand);
		
		if (player.isCrouching()) {
			if (stack.getOrCreateTag().contains("level") && player.getOffhandItem().isEmpty()) {
				ItemStack capsule = new ItemStack(RickGunMod.CAPSULE.get());
				capsule.getOrCreateTag().putInt("level", stack.getTag().getInt("level"));
				player.setItemInHand(InteractionHand.OFF_HAND, capsule);
				stack.getTag().remove("level");
			} else if (!stack.getOrCreateTag().contains("level") && player.getOffhandItem().getItem() == RickGunMod.CAPSULE.get()) {
				stack.getTag().putInt("level", player.getOffhandItem().getTag().contains("level") ? player.getOffhandItem().getTag().getInt("level") : 0);
				player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
			}
			return InteractionResultHolder.success(stack);
		}
		
		// Not working bc portal block have no bounding box
		HitResult hitResult = pPlayer.pick(player.getReachDistance(), 1F, false);
		if (hitResult.getType() == HitResult.Type.BLOCK && level.getBlockState(((BlockHitResult) hitResult).getBlockPos()).getBlock() == RickGunMod.PORTAL_BLOCK.get()) {
			TeleportationTools.cancelPortalPair(player, ((BlockHitResult) hitResult).getBlockPos());
			return InteractionResultHolder.success(pPlayer.getItemInHand(pUsedHand));
		}
		
		if (!stack.getOrCreateTag().contains("portal_gun_cords")) {
			player.displayClientMessage(new TextComponent("No destination set!"), true);
			return InteractionResultHolder.fail(pPlayer.getItemInHand(pUsedHand));
		}
		
		int l = 0;
		if (stack.getTag().contains("level")) {
			l = stack.getTag().getInt("level");
		} else {
			player.displayClientMessage(new TextComponent("No capsule!"), true);
			return InteractionResultHolder.fail(pPlayer.getItemInHand(pUsedHand));
		}
		
		if (l == 0 && player.isCreative()) {
			l = 4;
		}
		
		if (l <= 0) {
			player.displayClientMessage(new TextComponent("No charges left!"), true);
			return InteractionResultHolder.fail(pPlayer.getItemInHand(pUsedHand));
		}
		
		if (player.getRandom().nextInt(12) == 0) {
			l--;
		}
		
		stack.getTag().putInt("level", l);
		
		CompoundTag cordsTag = (CompoundTag) stack.getTag().get("portal_gun_cords");
		int destX = cordsTag.getInt("x");
		int destY = cordsTag.getInt("y");
		int destZ = cordsTag.getInt("z");
		String destDim = cordsTag.getString("dimension");
		String destName = cordsTag.getString("name");
		TeleportDestination dest = new TeleportDestination(destName, destDim, new BlockPos(destX, destY, destZ), Direction.UP);
		
		PortalProjectile projectile = new PortalProjectile(RickGunMod.ENTITY_PORTAL_PROJECTILE.get(), player, level);
		projectile.setColor(cordsTag.contains("color") ? cordsTag.getInt("color") : Color.GREEN.toInt());
		projectile.setDestination(dest);
		projectile.setPlayerId(player.getUUID());
		projectile.shootFromRotation(player, pPlayer.getXRot(), pPlayer.getYRot(), 0.0F, 1.5F, 1.0F);
		level.addFreshEntity(projectile);
		
		return InteractionResultHolder.success(stack);
	}
	
	public int getLevel(ItemStack stack) {
		if (stack.getOrCreateTag().contains("level")) {
			return stack.getTag().getInt("level");
		}
		return -1;
	}
}