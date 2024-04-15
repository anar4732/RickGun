package com.anar4732.rg;

import com.anar4732.rg.network.OpenPortalConfigMenuPacket;
import com.anar4732.rg.network.ResetPortalsPacket;
import com.anar4732.rg.portal.PortalProjectileRender;
import com.anar4732.rg.portal.PortalTERenderer;
import com.anar4732.rg.util.SimpleKeyMapping;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import team.creative.creativecore.CreativeCore;

@Mod.EventBusSubscriber(modid = RickGunMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class RickGunModClient {
	public static final KeyMapping keyPortalGunConfig = new SimpleKeyMapping("key.portalGunConfig", 'V', () -> {
		Minecraft mc = Minecraft.getInstance();
		if (mc.screen == null && mc.player != null && mc.player.getMainHandItem().getItem() instanceof PortalGunItem) {
			CreativeCore.NETWORK.sendToServer(new OpenPortalConfigMenuPacket());
		}
	});
	
	public static final KeyMapping keyResetPortals = new SimpleKeyMapping("key.ResetPortals", 'R', () -> {
		Minecraft mc = Minecraft.getInstance();
		if (mc.screen == null && mc.player != null && mc.player.getMainHandItem().getItem() instanceof PortalGunItem) {
			CreativeCore.NETWORK.sendToServer(new ResetPortalsPacket());
		}
	});
	
	@SubscribeEvent
	public static void onFMLClientSetup(FMLClientSetupEvent event) {
		ItemProperties.register(RickGunMod.PORTAL_GUN.get(), new ResourceLocation("level"), (stack, level, entity, seed) -> {
			return ((PortalGunItem) stack.getItem()).getLevel(stack);
		});
		ItemProperties.register(RickGunMod.CAPSULE.get(), new ResourceLocation("level"), (stack, level, entity, seed) -> {
			return ((CapsuleItem) stack.getItem()).getLevel(stack);
		});
		
		BlockEntityRenderers.register(RickGunMod.TE_PORTAL.get(), PortalTERenderer::create);
		EntityRenderers.register(RickGunMod.ENTITY_PORTAL_PROJECTILE.get(), PortalProjectileRender::new);
		ClientRegistry.registerKeyBinding(keyPortalGunConfig);
		ClientRegistry.registerKeyBinding(keyResetPortals);
	}
	
	@SubscribeEvent
	public static void onColorHandler(ColorHandlerEvent.Item event) {
		event.getItemColors().register((pStack, pTintIndex) -> {
			return pStack.getOrCreateTag().contains("color") ? pStack.getOrCreateTag().getInt("color") : 0xFFFFFF;
		}, RickGunMod.PORTAL_PROJECTILE.get());
	}
}