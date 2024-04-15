package com.anar4732.rg.portal;

import com.anar4732.rg.util.TeleportDestination;
import com.anar4732.rg.util.TeleportationTools;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public class PortalProjectile extends ThrowableProjectile {
	protected static final EntityDataAccessor<Integer> DATA_COLOR = SynchedEntityData.defineId(PortalProjectile.class, EntityDataSerializers.INT);
    private TeleportDestination destination;
    private UUID playerId;
	
	public PortalProjectile(EntityType<? extends PortalProjectile> pEntityType, Level pLevel) {
		super(pEntityType, pLevel);
	}
	
	public PortalProjectile(EntityType<? extends PortalProjectile> pEntityType, double pX, double pY, double pZ, Level pLevel) {
		super(pEntityType, pX, pY, pZ, pLevel);
	}
	
	public PortalProjectile(EntityType<? extends PortalProjectile> pEntityType, LivingEntity pShooter, Level pLevel) {
		super(pEntityType, pShooter, pLevel);
	}
	
	@Override
	public void defineSynchedData() {
		this.entityData.define(DATA_COLOR, 0xffffff);
	}
	
	public void setDestination(TeleportDestination destination) {
        this.destination = destination;
    }

    public void setPlayerId(UUID playerId) {
        this.playerId = playerId;
    }
	
	@Override
	public void tick() {
		super.tick();
		if (this.tickCount > 6) {
			Vec3 dv = this.getDeltaMovement().normalize();
			this.onHitBlock(new BlockHitResult(this.position(), Direction.getNearest(dv.x, dv.y, dv.z), this.blockPosition(), false));
		}
	}
	
	@Override
	protected float getGravity() {
		return 0F;
	}
	
	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		if (destination != null) {
			tag.put("destination", destination.toNBT());
		}
		if (playerId != null) {
			tag.putUUID("playerId", playerId);
		}
		tag.putInt("portalColor", this.getColor());
	}
	
	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		if (tag.contains("destination")) {
			destination = new TeleportDestination(tag.getCompound("destination"));
		} else {
			destination = null;
		}
		if (tag.hasUUID("playerId")) {
			playerId = tag.getUUID("playerId");
		} else {
			playerId = null;
		}
		setColor(tag.getInt("portalColor"));
	}
	
	@Override
	protected void onHitBlock(@NotNull BlockHitResult result) {
		super.onHitBlock(result);
		if (!level.isClientSide) {
			if (result.getType() == HitResult.Type.BLOCK) {
				Player player = null;
				if (playerId != null) {
					MinecraftServer server = this.level.getServer();
					player = playerId == null ? null : server.getPlayerList().getPlayer(playerId);
				}
				if (result.getDirection() == Direction.UP || result.getDirection() == Direction.DOWN) {
					destination.setSide(result.getDirection());
				} else {
					destination.setSide(result.getDirection().getOpposite());
				}
				if (player != null) {
					TeleportationTools.makePortalPair(player, result.getBlockPos(), result.getDirection(), destination, getColor());
				} else {
					TeleportationTools.makePortalPair(level, result.getBlockPos(), result.getDirection(), destination, getColor());
				}
			}
			this.remove(RemovalReason.DISCARDED);
		}
	}
	
	public void setColor(int color) {
		this.entityData.set(DATA_COLOR, color);
	}
	
	public int getColor() {
		return this.entityData.get(DATA_COLOR);
	}
}