package com.anar4732.rg;

import com.anar4732.rg.gui.PortalGunGUI;
import com.anar4732.rg.network.OpenPortalConfigMenuPacket;
import com.anar4732.rg.network.ResetPortalsPacket;
import com.anar4732.rg.portal.PortalBlock;
import com.anar4732.rg.portal.PortalProjectile;
import com.anar4732.rg.portal.PortalTE;
import com.anar4732.rg.util.CapsuleFillingRecipe;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.ArmorDyeRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;
import team.creative.creativecore.CreativeCore;

@Mod(RickGunMod.MODID)
public class RickGunMod {
	public static final String MODID = "rg";
	public static final Logger LOGGER = LogUtils.getLogger();
	
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
	public static final RegistryObject<Item> PORTAL_GUN = ITEMS.register("portal_gun", PortalGunItem::new);
	public static final RegistryObject<Item> PORTAL_PROJECTILE = ITEMS.register("portal_projectile", () -> new Item(new Item.Properties()));
	public static final RegistryObject<Item> CAPSULE = ITEMS.register("capsule", CapsuleItem::new);
	
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
	public static final RegistryObject<Block> PORTAL_BLOCK = BLOCKS.register("portal_block", PortalBlock::new);
	
	public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MODID);
	public static final RegistryObject<SoundEvent> SOUND_TELEPORT = registerSoundEvent("teleport");
	public static final RegistryObject<SoundEvent> SOUND_PORTAL = registerSoundEvent("portal");
	
	public static final DeferredRegister<BlockEntityType<?>> TILE_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, MODID);
	public static final RegistryObject<BlockEntityType<PortalTE>> TE_PORTAL = TILE_ENTITIES.register("portal", () -> BlockEntityType.Builder.of(PortalTE::new,PORTAL_BLOCK.get()).build(null));
	
	public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, MODID);
	public static final RegistryObject<EntityType<PortalProjectile>> ENTITY_PORTAL_PROJECTILE = ENTITIES.register("portal_projectile", () ->
			EntityType.Builder.<PortalProjectile>of(PortalProjectile::new, MobCategory.MISC)
			                  .sized(0.25F, 0.25F)
			                  .clientTrackingRange(4)
			                  .updateInterval(10)
			                  .build("portal_projectile"));
	
	public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MODID);
	public static final RegistryObject<RecipeSerializer<?>> CAPSULE_FILLING_RECIPE = RECIPE_SERIALIZERS.register("capsule_filling_recipe", () -> new SimpleRecipeSerializer<>(CapsuleFillingRecipe::new));
	
	public RickGunMod() {
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		modEventBus.addListener(this::setup);
		ITEMS.register(modEventBus);
		BLOCKS.register(modEventBus);
		SOUNDS.register(modEventBus);
		TILE_ENTITIES.register(modEventBus);
		ENTITIES.register(modEventBus);
		RECIPE_SERIALIZERS.register(modEventBus);
	}
	
	private void setup(final FMLCommonSetupEvent e) {
		CreativeCore.NETWORK.registerType(OpenPortalConfigMenuPacket.class, OpenPortalConfigMenuPacket::new);
		CreativeCore.NETWORK.registerType(ResetPortalsPacket.class, ResetPortalsPacket::new);
		
		PortalGunGUI.GUI.getName(); // load the class
	}
	
	private static RegistryObject<SoundEvent> registerSoundEvent(String name) {
		return SOUNDS.register(name, () -> new SoundEvent(new ResourceLocation(MODID, name)));
	}
}