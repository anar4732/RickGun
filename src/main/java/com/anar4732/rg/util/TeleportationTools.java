package com.anar4732.rg.util;

import com.anar4732.rg.RickGunMod;
import com.anar4732.rg.portal.PortalTE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.util.ITeleporter;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class TeleportationTools {
	public static void cancelPortalPair(Player player, BlockPos pos) {
		if (player.getLevel().getBlockEntity(pos) instanceof PortalTE portalTE) {
			portalTE.setTimeout(20);
		}
	}
	
	private static boolean canPlacePortal(Level level, BlockPos pos) {
		return level.isEmptyBlock(pos) || level.getBlockState(pos).getMaterial().isReplaceable();
	}
	
	private static boolean canCollideWith(Level level, BlockPos pos) {
		if (level.isEmptyBlock(pos)) {
			return false;
		}
		VoxelShape box = level.getBlockState(pos).getCollisionShape(level, pos);
		return !box.isEmpty();
	}
	
	public static void makePortalPair(Player player, BlockPos selectedBlock, Direction selectedSide, TeleportDestination dest, int portalColor) {
		Level sourceLevel = player.getLevel();
		if (sourceLevel.isClientSide) {
			return;
		}
		
		BlockPos sourcePortalPos = findBestPosition(sourceLevel, selectedBlock, selectedSide);
		if (sourcePortalPos == null) { // cant_find_portal_spot
			player.displayClientMessage(new TextComponent("Can't find a spot for the portal"), true);
			return;
		}
		
		Level destLevel = dest.getLevel((ServerLevel) sourceLevel);
		
		if (destLevel == null) { // destination_not_found
			player.displayClientMessage(new TextComponent("Destination world not found"), true);
			return;
		}
		
		if (destLevel == sourceLevel && dest.getPos().distSqr(selectedBlock) < 16) { // destination_too_close
			player.displayClientMessage(new TextComponent("Destination too close"), true);
			return;
		}
		
		if (destLevel.getBlockState(dest.getPos()).getBlock() == RickGunMod.PORTAL_BLOCK.get()) { // portal_already_there
			player.displayClientMessage(new TextComponent("Portal already there"), true);
			return;
		}
		
		if (dest.getSide() == Direction.DOWN) {
			if (!canPlacePortal(destLevel, dest.getPos()) || canCollideWith(destLevel, dest.getPos().below())) { // destination_obstructed
				player.displayClientMessage(new TextComponent("Destination obstructed"), true);
				return;
			}
		} else {
			if (!canPlacePortal(destLevel, dest.getPos()) || canCollideWith(destLevel, dest.getPos().above())) { // destination_obstructed
				player.displayClientMessage(new TextComponent("Destination obstructed"), true);
				return;
			}
		}
		
		sourceLevel.setBlock(sourcePortalPos, RickGunMod.PORTAL_BLOCK.get().defaultBlockState(), 3);
		PortalTE source = (PortalTE) sourceLevel.getBlockEntity(sourcePortalPos);
		
		if (source == null) {
			sourceLevel.setBlock(sourcePortalPos, Blocks.AIR.defaultBlockState(), 3);
			player.displayClientMessage(new TextComponent("Failed to create the portal"), true);
			return;
		}
		
		destLevel.setBlock(dest.getPos(), RickGunMod.PORTAL_BLOCK.get().defaultBlockState(), 3);
		PortalTE destination = (PortalTE) destLevel.getBlockEntity(dest.getPos());
		
		if (destination == null) {
			sourceLevel.setBlock(sourcePortalPos, Blocks.AIR.defaultBlockState(), 3);
			destLevel.setBlock(dest.getPos(), Blocks.AIR.defaultBlockState(), 3);
			player.displayClientMessage(new TextComponent("Failed to create the destination portal"), true);
			return;
		}
		
		source.setTimeout(PortalTE.PORTAL_TIMEOUT);
		source.setOther(dest);
		source.setPortalSide(selectedSide);
		source.setColor(portalColor);
		source.setOwnerId(player.getUUID());
		
		destination.setTimeout(PortalTE.PORTAL_TIMEOUT);
		destination.setOther(new TeleportDestination("", sourceLevel.dimension().location().toString(), sourcePortalPos, selectedSide));
		destination.setPortalSide(dest.getSide());
		destination.setColor(portalColor);
		destination.setOwnerId(player.getUUID());
	}
	
	public static void makePortalPair(Level sourceLevel, BlockPos selectedBlock, Direction selectedSide, TeleportDestination dest, int portalColor) {
		BlockPos sourcePortalPos = findBestPosition(sourceLevel, selectedBlock, selectedSide);
		if (sourcePortalPos == null || sourceLevel.isClientSide) {
			return;
		}
		
		Level destLevel = dest.getLevel((ServerLevel) sourceLevel);
		if (destLevel.getBlockState(dest.getPos()).getBlock() == RickGunMod.PORTAL_BLOCK.get()) {
			return;
		}
		
		if (dest.getSide() == Direction.DOWN) {
			if (!destLevel.isEmptyBlock(dest.getPos()) || !destLevel.isEmptyBlock(dest.getPos().below())) {
				return;
			}
		} else {
			if (!destLevel.isEmptyBlock(dest.getPos()) || !destLevel.isEmptyBlock(dest.getPos().above())) {
				return;
			}
		}
		
		sourceLevel.setBlock(sourcePortalPos, RickGunMod.PORTAL_BLOCK.get().defaultBlockState(), 3);
		PortalTE source = (PortalTE) sourceLevel.getBlockEntity(sourcePortalPos);
		
		destLevel.setBlock(dest.getPos(), RickGunMod.PORTAL_BLOCK.get().defaultBlockState(), 3);
		PortalTE destination = (PortalTE) destLevel.getBlockEntity(dest.getPos());
		
		if (source == null || destination == null) {
			RickGunMod.LOGGER.error("Failed to create portal pair");
			return;
		}
		
		source.setTimeout(PortalTE.PORTAL_TIMEOUT);
		source.setOther(dest);
		source.setPortalSide(selectedSide);
		
		destination.setTimeout(PortalTE.PORTAL_TIMEOUT);
		destination.setOther(new TeleportDestination("", sourceLevel.dimension().location().toString(), sourcePortalPos, selectedSide));
		destination.setPortalSide(dest.getSide());
	}
	
	@Nullable
	public static BlockPos findBestPosition(Level level, BlockPos selectedBlock, Direction selectedSide) {
		if (selectedSide == Direction.UP) {
			if (level.isEmptyBlock(selectedBlock.above()) && level.isEmptyBlock(selectedBlock.above(2))) {
				return selectedBlock.above();
			}
			return null;
		}
		
		if (selectedSide == Direction.DOWN) {
			if (level.isEmptyBlock(selectedBlock.below()) && level.isEmptyBlock(selectedBlock.below(2))) {
				return selectedBlock.below();
			}
			return null;
		}
		
		selectedBlock = selectedBlock.relative(selectedSide);
		
		if (level.isEmptyBlock(selectedBlock.below())) {
			selectedBlock = selectedBlock.below();
		}
		
		if (!level.isEmptyBlock(selectedBlock.below())) {
			return findBestPosition(level, selectedBlock.below(), Direction.UP);
		}
		
		return selectedBlock;
	}
	
	public static void teleportToDimension(Player player, ResourceLocation dimension, double x, double y, double z) {
		ResourceLocation oldDimension = player.getLevel().dimension().location();
		
		if (oldDimension.equals(dimension)) {
			player.teleportTo(x + 0.5, y + .05, z + 0.5);
			return;
		}
		
		ServerLevel level = ((ServerLevel) player.getLevel()).getServer().getLevel(ResourceKey.create(Registry.DIMENSION_REGISTRY, dimension));
		
		if (level == null) {
			RickGunMod.LOGGER.error("Failed to teleport player to dimension {}", dimension);
			return;
		}
		
		player.changeDimension(level);
		player.teleportTo(x, y, z);
	}
	
	public static Entity teleportEntity(Entity entity, Level destLevel, double x, double y, double z, Direction facing) {
		Level level = entity.getLevel();
		if (level.equals(destLevel)) {
			if (facing != null) {
				fixOrientation(entity, x, y, z, facing);
			}
			entity.teleportTo(x, y, z);
			return entity;
		} else {
			return entity.changeDimension((ServerLevel) destLevel, new ITeleporter() {
				@Override
				public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destWorld, float yaw, Function<Boolean, Entity> repositionEntity) {
					entity = repositionEntity.apply(false);
					if (facing != null) {
						fixOrientation(entity, x, y, z, facing);
					}
					entity.teleportTo(x, y, z);
					return entity;
				}
			});
		}
	}
	
	private static void fixOrientation(Entity entity, double x, double y, double z, Direction facing) {
		if (facing != Direction.DOWN && facing != Direction.UP) {
			facePosition(entity, x, y, z, new BlockPos(x, y, z).relative(facing, 4));
		}
	}
	
	private static void facePosition(Entity entity, double x, double y, double z, BlockPos pos) {
		double d0 = pos.getX() - x;
		double d1 = pos.getY() - (y + entity.getEyeHeight());
		double d2 = pos.getZ() - z;
		
		double d3 = Math.sqrt(d0 * d0 + d2 * d2);
		float f = (float) (Math.atan2(d2, d0) * (180D / Math.PI)) - 90.0F;
		float f1 = (float) (-(Math.atan2(d1, d3) * (180D / Math.PI)));
		entity.setXRot(updateRotation(entity.getXRot(), f1));
		entity.setYRot(updateRotation(entity.getYRot(), f));
	}
	
	private static float updateRotation(float angle, float targetAngle) {
		float f = Mth.wrapDegrees(targetAngle - angle);
		return angle + f;
	}
	
}