package com.anar4732.rg.gui;

import com.anar4732.rg.PortalGunItem;
import com.anar4732.rg.gui.controls.GuiButtonExtended;
import com.anar4732.rg.gui.controls.GuiTextFieldExtended;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import team.creative.creativecore.client.render.text.CompiledText;
import team.creative.creativecore.common.gui.Align;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.collection.GuiComboBox;
import team.creative.creativecore.common.gui.controls.parent.GuiScrollY;
import team.creative.creativecore.common.gui.controls.simple.GuiColorPicker;
import team.creative.creativecore.common.gui.controls.simple.GuiColorPlate;
import team.creative.creativecore.common.gui.controls.simple.GuiLabel;
import team.creative.creativecore.common.gui.controls.simple.GuiTextfield;
import team.creative.creativecore.common.gui.handler.GuiCreator;
import team.creative.creativecore.common.gui.sync.GuiSyncLocal;
import team.creative.creativecore.common.util.math.geo.Rect;
import team.creative.creativecore.common.util.type.Color;

import java.util.Map;
import java.util.stream.Collectors;

public class PortalGunGUI extends GUICore {
	public static final GuiCreator.GuiCreatorBasic GUI = GuiCreator.register("cui", new GuiCreator.GuiCreatorBasic((nbt, player) -> new PortalGunGUI(nbt)));
	public static final ResourceLocation TEXTURE_MAIN = new ResourceLocation("rg:textures/gui/main.png");
	
	public final GuiSyncLocal<CompoundTag> ADD = getSyncHolder().register("add", nbt -> {
		if (!isClient()) {
			ServerPlayer player = (ServerPlayer) getPlayer();
			CompoundTag persistent = player.getPersistentData();
			ListTag list = persistent.contains("portal_gun_cords") ? persistent.getList("portal_gun_cords", 10) : new ListTag();
			list.add(nbt);
			persistent.put("portal_gun_cords", list);
		}
	});
	
	public final GuiSyncLocal<CompoundTag> REMOVE = getSyncHolder().register("rem", nbt -> {
		if (!isClient()) {
			ServerPlayer player = (ServerPlayer) getPlayer();
			CompoundTag persistent = player.getPersistentData();
			CompoundTag sendingTag = new CompoundTag();
			if (persistent.contains("portal_gun_cords")) {
				ListTag list = persistent.getList("portal_gun_cords", 10);
				list.remove(nbt);
				persistent.put("portal_gun_cords", list);
				sendingTag.put("portal_gun_cords", persistent.getList("portal_gun_cords", 10));
				PortalGunGUI.GUI.open(sendingTag, getPlayer());
			}
		}
	});
	
	public final GuiSyncLocal<CompoundTag> SET = getSyncHolder().register("set", nbt -> {
		if (!isClient()) {
			ServerPlayer player = (ServerPlayer) getPlayer();
			ItemStack stack = player.getMainHandItem();
			if (stack.getItem() instanceof PortalGunItem) {
				stack.getOrCreateTag().put("portal_gun_cords", nbt);
			}
			closeTopLayer();
		}
	});
	
	private final CompoundTag nbt;
	private GuiTextfield x;
	private GuiTextfield y;
	private GuiTextfield z;
	private GuiComboBox dim;
	private GuiTextFieldExtended namef;
	private GuiLabel addbtn;
	private GuiLabel setbtn;
	private GuiColorPlate color;
	
	public PortalGunGUI(CompoundTag nbt) {
		super("cui", 300, 200, nbt);
		this.nbt = nbt;
	}
	
	@Override
	protected void initClient(CompoundTag nbt) {

	}
	
	private final GuiParent scroll = new GuiScrollY("scroll");
	private String selected = "";
	
	@Override
	public void create() {
		if (!isClient())
			return;
		
		add(new GuiLabel("title").setTitle(new TextComponent("Portal Gun Config")), 95, 0);
		add(x = new GuiTextFieldExtended("x").setSuggestionE("X").setNumbersIncludingNegativeOnly(), 155, 10, 40, 14);
		add(y = new GuiTextFieldExtended("Y").setSuggestionE("Y").setNumbersIncludingNegativeOnly(), 200, 10, 40, 14);
		add(z = new GuiTextFieldExtended("Z").setSuggestionE("Z").setNumbersIncludingNegativeOnly(), 245, 10, 40, 14);
		
		x.setText(String.valueOf(getPlayer().getBlockX()));
		y.setText(String.valueOf(getPlayer().getBlockY()));
		z.setText(String.valueOf(getPlayer().getBlockZ()));
		
//		add(dim = new GuiTextFieldExtended("Dimension").setSuggestionE("minecraft:overworld"), 0, 10, 150, 14);
		add(dim = new GuiComboBox("Dimension", () -> {
			return Minecraft.getInstance().getConnection().levels().stream()
			                           .map(ResourceKey::location)
			                           .map(ResourceLocation::toString)
			                           .map(this::compileText)
			                           .toArray(CompiledText[]::new);
		}), 0, 10, 150, 14);
		
		int dimIndex = Minecraft.getInstance().getConnection().levels().stream()
		                       .map(ResourceKey::location)
		                       .map(ResourceLocation::toString)
		                       .toList()
		                       .indexOf(getPlayer().getLevel().dimension().location().toString());
		dim.select(dimIndex);
		
		add(namef = new GuiTextFieldExtended("Name").setSuggestionE("Name"), 0, 27, 220, 15);
		add(addbtn = new GuiButtonExtended("Add", b -> {
			try {
				int x = Integer.parseInt(this.x.getText());
				int y = Integer.parseInt(this.y.getText());
				int z = Integer.parseInt(this.z.getText());
				String dimension = getDimension();
				addCord(x, y, z, dimension, namef.getText().isEmpty() ? null : namef.getText());
				CompoundTag tag = new CompoundTag();
				tag.putInt("x", x);
				tag.putInt("y", y);
				tag.putInt("z", z);
				tag.putString("dimension", dimension);
				tag.putString("name", namef.getText().isEmpty() ? getFullName(x, y, z, dimension) : namef.getText());
				tag.putInt("color", color.getColor().toInt());
				ADD.send(tag);
			} catch (NumberFormatException ignored) {}
		}).setTitle(new TextComponent("+")).setAlign(Align.CENTER), 270, 27, 10, 10);
		
		add(setbtn = new GuiButtonExtended("Set", b -> {
			try {
				int x = Integer.parseInt(this.x.getText());
				int y = Integer.parseInt(this.y.getText());
				int z = Integer.parseInt(this.z.getText());
				String dimension = getDimension();
				CompoundTag tag = new CompoundTag();
				tag.putInt("x", x);
				tag.putInt("y", y);
				tag.putInt("z", z);
				tag.putString("dimension", dimension);
				tag.putString("name", namef.getText().isEmpty() ? getFullName(x, y, z, dimension) : namef.getText());
				tag.putInt("color", color.getColor().toInt());
				SET.send(tag);
			} catch (NumberFormatException ignored) {}
		}).setTitle(new TextComponent("SET")).setAlign(Align.CENTER), 235, 27, 30, 10);
		
		if (nbt.contains("curr_portal_gun_cords")) {
			selected = nbt.getCompound("curr_portal_gun_cords").getString("name");
		}
		
		if (nbt.contains("portal_gun_cords")) {
			ListTag list = nbt.getList("portal_gun_cords", 10);
			for (int i = 0; i < list.size(); i++) {
				CompoundTag tag = list.getCompound(i);
				addCord(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"), tag.getString("dimension"), tag.contains("name") ? tag.getString("name") : null);
			}
		}
		
		add(scroll, 0, 45, 285, 140);
		
		add(color = new GuiColorPlate("color", Color.GREEN) {
			@Override
			public boolean mouseClicked(Rect rect, double x, double y, int button) {
				this.setColor(this.getColor() == Color.GREEN ? new Color(0xfffffa0f) : Color.GREEN);
				return true;
			}
		}, 222, 27, 10, 15);
	}
	
	private CompiledText compileText(String s) {
		CompiledText t = new CompiledText(150, 14);
		t.setText(new TextComponent(s));
		return t;
	}
	
	@NotNull
	private static String getFullName(int x, int y, int z, String dimension) {
		return x + ", " + y + ", " + z + " in " + dimension;
	}
	
	private String getDimension() {
		return Minecraft.getInstance().getConnection().levels().stream()
		                .map(ResourceKey::location)
		                .map(ResourceLocation::toString).toList().get(dim.getIndex());
	}
	
	@Override
	public void tick() {
		super.tick();
		if (x != null && y != null && z != null && dim != null && namef != null) {
			if (namef.getText().isEmpty()) {
				if (!x.getText().isEmpty() && !y.getText().isEmpty() && !z.getText().isEmpty() && !getDimension().isEmpty()) {
					try {
						namef.setSuggestionE(getFullName(Integer.parseInt(x.getText()), Integer.parseInt(y.getText()), Integer.parseInt(z.getText()), getDimension()));
					} catch (NumberFormatException e) {
						namef.setSuggestionE("Name");
					}
				} else {
					namef.setSuggestionE("Name");
				}
			}
			setbtn.setEnabled(!x.getText().isEmpty() && !y.getText().isEmpty() && !z.getText().isEmpty());
			addbtn.setEnabled(!x.getText().isEmpty() && !y.getText().isEmpty() && !z.getText().isEmpty());
		}
	}
	
	private void addCord(int x, int y, int z, String dimension, String name) {
		String full = name == null ? getFullName(x, y, z, dimension) : name;
		GuiButtonExtended button;
		scroll.add(button = (GuiButtonExtended) new GuiButtonExtended("button", b -> {
			CompoundTag tag = new CompoundTag();
			tag.putInt("x", x);
			tag.putInt("y", y);
			tag.putInt("z", z);
			tag.putString("dimension", dimension);
			tag.putString("name", full);
			tag.putInt("color", color.getColor().toInt());
			if (Screen.hasShiftDown()) {
				REMOVE.send(tag);
				return;
			}
			SET.send(tag);
		}).setTitle(new TextComponent(full)));
		if (selected.equals(full)) {
			button.setEnabled(false);
		}
		reflow();
	}
}