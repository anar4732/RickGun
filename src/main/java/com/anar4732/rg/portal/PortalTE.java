package com.anar4732.rg.portal;

import com.anar4732.rg.RickGunMod;
import com.anar4732.rg.util.TeleportDestination;
import com.anar4732.rg.util.TeleportationTools;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class PortalTE extends BlockEntity {
	public static final List<PortalTE> PORTALS = new ArrayList<>();
	
	public static final int PORTAL_TIMEOUT = 15 * 20;
	public static final int PORTAL_TIMEOUT_AFTER_ENTITY = 5 * 20;
	
    private int timeout;
    private boolean soundStart = false;
    private boolean soundEnd = false;
    private int start; // Client side only
    private TeleportDestination other;
    private Direction portalSide; // Side to render the portal on
    private AABB box = null;
    private final Set<UUID> blackListed = new HashSet<>(); // Entities can only go through the portal one time
	private int portalColor;
	private UUID ownerId;
	
	public PortalTE(BlockPos pos, BlockState state) {
		super(RickGunMod.TE_PORTAL.get(), pos, state);
		PORTALS.add(this);
	}
	
	public void tick() {
        if (!level.isClientSide) {
            tickTime();
            if (timeout <= 0) {
                killPortal();
                getOther().ifPresent(PortalTE::killPortal);
                return;
            }

            if ((!soundStart) && timeout > PORTAL_TIMEOUT - 10) {
                soundStart = true;
	            level.playSound(null, getBlockPos(), RickGunMod.SOUND_PORTAL.get(), SoundSource.BLOCKS, 1F, 1F);
            }

            if ((!soundEnd) && timeout < 15) {
                soundEnd = true;
                if (PORTAL_TIMEOUT > 0.01f) {
	                level.playSound(null, getBlockPos(), RickGunMod.SOUND_PORTAL.get(), SoundSource.BLOCKS, 1F, 1F);
                }
            }
	        
	        if (timeout < 20 || start < 20) {
				return;
	        }

            getOther().ifPresent(otherPortal -> {
                double otherX = otherPortal.getBlockPos().getX() + .5;
                double otherY = otherPortal.getBlockPos().getY() + .5;
                double otherZ = otherPortal.getBlockPos().getZ() + .5;
                List<Entity> entities = level.getEntities(null, getTeleportBox());
                for (Entity entity : entities) {
                    if (!(entity instanceof PortalProjectile) && !blackListed.contains(entity.getUUID())) {
                        otherPortal.addBlackList(entity.getUUID());
                        double oy = otherY;
                        if (otherPortal.getPortalSide() == Direction.DOWN) {
                            oy -= entity.getBbHeight() + .7;
                        }
						Vec3 dm = entity.getDeltaMovement();
	                    entity = TeleportationTools.teleportEntity(entity, otherPortal.getLevel(), otherX, oy, otherZ, otherPortal.getPortalSide());
	                    entity.setDeltaMovement(dm);
	                    ((ServerLevel) entity.getLevel()).getServer().getPlayerList().broadcast(null, entity.getX(), entity.getY(), entity.getZ(), 24, entity.level.dimension(), new ClientboundSetEntityMotionPacket(entity));
	                    setTimeout(PORTAL_TIMEOUT_AFTER_ENTITY);
                        otherPortal.setTimeout(PORTAL_TIMEOUT_AFTER_ENTITY);
	                    level.playSound(null, entity.blockPosition(), RickGunMod.SOUND_TELEPORT.get(), SoundSource.BLOCKS, 1F, 1F);
                    }
                }
            });
        }
    }
	
	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}
	
	@Override
	public @NotNull CompoundTag getUpdateTag() {
		CompoundTag tag = super.getUpdateTag();
		saveAdditional(tag);
		return tag;
	}
	
	public AABB getTeleportBox() {
        if (box == null) {
			BlockPos pos = getBlockPos();
            switch (portalSide) {
                case DOWN:
                    box = new AABB(pos.getX() - .7, pos.getY() + .5, pos.getZ() - .7, pos.getX() + 1.7, pos.getY() + 1, pos.getZ() + 1.7);
                    break;
                case UP:
                    box = new AABB(pos.getX() - .7, pos.getY() - .2, pos.getZ() - .7, pos.getX() + 1.7, pos.getY() + .5, pos.getZ() + 1.7);
                    break;
                case SOUTH:
                    box = new AABB(pos.getX() - .2, pos.getY() - .2, pos.getZ() - .2, pos.getX() + 1.2, pos.getY() + 2.2, pos.getZ() + 0.2);
                    break;
                case NORTH:
                    box = new AABB(pos.getX() - .2, pos.getY() - .2, pos.getZ() + .8, pos.getX() + 1.2, pos.getY() + 2.2, pos.getZ() + 1.2);
                    break;
                case EAST:
                    box = new AABB(pos.getX() - .2, pos.getY() - .2, pos.getZ() - .2, pos.getX() + 0.2, pos.getY() + 2.2, pos.getZ() + 1.2);
                    break;
                case WEST:
                    box = new AABB(pos.getX() + .8, pos.getY() - .2, pos.getZ() - .2, pos.getX() + 1.2, pos.getY() + 2.2, pos.getZ() + 1.2);
                    break;
            }
        }
        return box;
    }

    public void addBlackList(UUID uuid) {
        blackListed.add(uuid);
        markDirtyQuick();
    }

    public int getTimeout() {
        return timeout;
    }

    public int getStart() {
        return start;
    }

    private void sync() {
	    if (level != null) {
		    level.markAndNotifyBlock(getBlockPos(), getLevel().getChunkAt(getBlockPos()), getBlockState(), getBlockState(), 3, 512);
	    }
    }

    private void markDirtyQuick() {
        if (getLevel() != null) {
	        getLevel().setBlocksDirty(this.getBlockPos(), getBlockState(), getBlockState());
        }
    }

    public Direction getPortalSide() {
        return portalSide;
    }

    public void setPortalSide(Direction portalSide) {
        this.portalSide = portalSide;
        box = null;
        sync();
    }

    public void tickTime() {
        timeout--;
		start++;
        getOther().ifPresent(otherPortal -> {
            int otherTimeout = otherPortal.getTimeout();
            if (timeout > otherTimeout) {
                timeout = otherTimeout;
            }
        });
        sync();
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
        sync();
    }

    public void setOther(TeleportDestination other) {
        this.other = other;
        markDirtyQuick();
    }

    public void killPortal() {
	    assert level != null;
	    level.setBlock(getBlockPos(), Blocks.AIR.defaultBlockState(), 3);
    }
	
	@Override
	public void setRemoved() {
		super.setRemoved();
		PORTALS.remove(this);
	}
	
	private Optional<PortalTE> getOther() {
		if (level == null || level.isClientSide || other == null) {
			return Optional.empty();
		}
        Level otherWorld = other.getLevel((ServerLevel) level);
		if (otherWorld == null) {
			RickGunMod.LOGGER.error("Cannot find other world! {}", other.getDimension(), new RuntimeException());
			return Optional.empty();
		}
        BlockEntity te = otherWorld.getBlockEntity(other.getPos());
        if (te instanceof PortalTE portalTE) {
            return Optional.of(portalTE);
        }
	    return Optional.empty();
    }
	
	@Override
	public void load(@NotNull CompoundTag tag) {
		super.load(tag);
		timeout = tag.getInt("timeout");
		start = tag.getInt("start");
		portalSide = tag.getByte("portalSide") == 127 ? null : Direction.from3DDataValue(tag.getByte("portalSide"));
		if (tag.contains("other")) {
			other = new TeleportDestination(tag.getCompound("other"));
		} else {
			other = null;
		}
		ListTag list = tag.getList("blackListed", 8);
		blackListed.clear();
		for (int i = 0; i < list.size(); i++) {
			blackListed.add(UUID.fromString(list.getString(i)));
		}
		portalColor = tag.getInt("portalColor");
		if (tag.contains("ownerId")) {
			ownerId = tag.getUUID("ownerId");
		}
	}
	
	@Override
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		tag.putInt("timeout", timeout);
		tag.putInt("start", start);
		tag.putByte("portalSide", portalSide == null ? 127 : (byte) portalSide.ordinal());
		if (other != null)
			tag.put("other", other.toNBT());
		ListTag list = new ListTag();
		for (UUID uuid : blackListed) {
			list.add(StringTag.valueOf(uuid.toString()));
		}
		tag.put("blackListed", list);
		tag.putInt("portalColor", portalColor);
		if (ownerId != null) {
			tag.putUUID("ownerId", ownerId);
		}
	}
	
	public void setColor(int color) {
		this.portalColor = color;
	}
	
	public int getColor() {
		return portalColor;
	}
	
	public void setOwnerId(UUID ownerId) {
		this.ownerId = ownerId;
	}
	
	public boolean isOwner(Player player) {
		return ownerId != null && ownerId.equals(player.getUUID());
	}
}