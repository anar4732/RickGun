package com.anar4732.rg.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public class TeleportDestination {
	private final String name;
	private final String dimension;
	private final BlockPos pos; // The position of the portal tile entity itself
	private Direction side; // The side on which to render the portal. UP is for a horizontal portal
	
	public TeleportDestination(String name, String dimension, BlockPos pos, Direction side) {
		this.name = name;
		this.dimension = dimension;
		this.pos = pos;
		this.side = side;
	}
	
	public TeleportDestination(CompoundTag tag) {
		name = tag.getString("name");
		dimension = tag.getString("dimension");
		pos = new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"));
		side = Direction.from3DDataValue(tag.getByte("side"));
	}
	
	public CompoundTag toNBT() {
		CompoundTag tc = new CompoundTag();
		tc.putString("name", getName());
		tc.putString("dimension", getDimension());
		tc.putByte("side", (byte) getSide().ordinal());
		tc.putInt("x", getPos().getX());
		tc.putInt("y", getPos().getY());
		tc.putInt("z", getPos().getZ());
		return tc;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDimension() {
		return dimension;
	}
	
	public Level getLevel(ServerLevel level) {
		return level.getServer().getLevel(ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(getDimension())));
	}
	
	public BlockPos getPos() {
		return pos;
	}
	
	public Direction getSide() {
		return side;
	}
	
	public void setSide(Direction direction) {
		side = direction;
	}
}