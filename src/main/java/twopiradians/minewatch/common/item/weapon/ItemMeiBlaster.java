package twopiradians.minewatch.common.item.weapon;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.config.Config;
import twopiradians.minewatch.common.entity.ability.EntityMeiCrystal;
import twopiradians.minewatch.common.entity.ability.EntityMeiIcicle;
import twopiradians.minewatch.common.entity.projectile.EntityMeiBlast;
import twopiradians.minewatch.common.hero.Ability;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.util.EntityHelper;
import twopiradians.minewatch.common.util.Handlers;
import twopiradians.minewatch.common.util.TickHandler;
import twopiradians.minewatch.common.util.TickHandler.Handler;
import twopiradians.minewatch.common.util.TickHandler.Identifier;
import twopiradians.minewatch.packet.SPacketSimple;

public class ItemMeiBlaster extends ItemMWWeapon {

	public static final Handler CRYSTAL = new Handler(Identifier.MEI_CRYSTAL, false) {
		@SideOnly(Side.CLIENT)
		@Override
		public boolean onClientTick() {
			if (this.entityLiving != null) {
				this.entityLiving.extinguish();
				if (this.entityLiving == Minecraft.getMinecraft().player)
					Minecraft.getMinecraft().gameSettings.thirdPersonView = 1;
			}

			return super.onClientTick();
		}
		@Override
		public boolean onServerTick() {
			if (this.entityLiving != null) {
				if (this.ticksLeft % 2 == 0)
					this.entityLiving.heal(3.75f*Config.damageScale);
				this.entityLiving.extinguish();
			}
			return super.onServerTick();
		}
		@SideOnly(Side.CLIENT)
		@Override
		public Handler onClientRemove() {
			ModSoundEvents.MEI_CRYSTAL_START.stopSound(Minecraft.getMinecraft().player);
			ModSoundEvents.MEI_CRYSTAL_STOP.playFollowingSound(entity, 1, 1, false);
			if (entity == Minecraft.getMinecraft().player)
				Minecraft.getMinecraft().gameSettings.thirdPersonView = thirdPersonView;
			return super.onClientRemove();
		}
		@Override
		public Handler onServerRemove() {
			EnumHero.MEI.ability2.keybind.setCooldown(entityLiving, 240, false); 
			return super.onServerRemove();
		}
	};

	@SideOnly(Side.CLIENT)
	public static int thirdPersonView;

	public ItemMeiBlaster() {
		super(30);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public void onItemLeftClick(ItemStack stack, World world, EntityLivingBase player, EnumHand hand) { 
		// shoot
		if (this.canUse(player, true, hand, false) && !world.isRemote) {
			EntityMeiBlast bullet = new EntityMeiBlast(world, player, hand.ordinal());
			EntityHelper.setAim(bullet, player, player.rotationPitch, player.rotationYawHead, 20, 0.6F, hand, 14, 0.8f);
			world.spawnEntity(bullet);
			ModSoundEvents.MEI_SHOOT_0.playSound(player, world.rand.nextFloat()/3, world.rand.nextFloat()/2+0.75f);
			this.subtractFromCurrentAmmo(player, 1);
			if (world.rand.nextInt(200) == 0)
				player.getHeldItem(hand).damageItem(1, player);
		}
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityLivingBase player, EnumHand hand) {
		// shoot
		if (this.canUse(player, true, hand, false)) {//TODO delay
			if (!world.isRemote) {
				EntityMeiIcicle icicle = new EntityMeiIcicle(world, player, hand.ordinal());
				EntityHelper.setAim(icicle, player, player.rotationPitch, player.rotationYawHead, 100, 0.4F, hand, 8, 0.35f);
				world.spawnEntity(icicle);
				this.setCooldown(player, 24);
				ModSoundEvents.MEI_SHOOT_1.playSound(player, world.rand.nextFloat()+0.5F, world.rand.nextFloat()/20+0.95f);
				if (world.rand.nextInt(8) == 0)
					player.getHeldItem(hand).damageItem(1, player);
				this.subtractFromCurrentAmmo(player, 25, hand);
			}
		}

		return new ActionResult(EnumActionResult.PASS, player.getHeldItem(hand));
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isSelected) {	
		super.onUpdate(stack, world, entity, slot, isSelected);

		if (isSelected && entity instanceof EntityLivingBase) {	
			EntityLivingBase player = (EntityLivingBase) entity;

			Handler handler = TickHandler.getHandler(player, Identifier.MEI_CRYSTAL);
			if (!world.isRemote && handler != null && 
					(KeyBind.RMB.isKeyDown(player, true) || KeyBind.LMB.isKeyDown(player, true)) &&
					hero.ability2.keybind.getCooldown(player) == 0) {
				TickHandler.unregister(false, TickHandler.getHandler(player, Identifier.MEI_CRYSTAL),
						TickHandler.getHandler(player, Identifier.PREVENT_MOVEMENT),
						TickHandler.getHandler(player, Identifier.PREVENT_INPUT),
						TickHandler.getHandler(player, Identifier.PREVENT_ROTATION),
						TickHandler.getHandler(player, Identifier.ABILITY_USING));
				Minewatch.network.sendToAll(new SPacketSimple(32, player, false));
			}

			// cryo-freeze
			if (!world.isRemote && hero.ability2.isSelected(player) && 
					this.canUse(player, true, EnumHand.MAIN_HAND, true)) {
				EntityMeiCrystal crystal = new EntityMeiCrystal(world, player);
				world.spawnEntity(crystal);
				TickHandler.register(false, CRYSTAL.setEntity(player).setTicks(80),
						Handlers.PREVENT_MOVEMENT.setEntity(player).setTicks(80),
						Handlers.PREVENT_INPUT.setEntity(player).setTicks(80),
						Handlers.PREVENT_ROTATION.setEntity(player).setTicks(80),
						Ability.ABILITY_USING.setEntity(player).setTicks(80).setAbility(hero.ability2));
				Minewatch.network.sendToAll(new SPacketSimple(32, player, true));
			}

		}
	}

	@SubscribeEvent
	public void preventDamage(LivingHurtEvent event) {
		if (event.getEntityLiving() != null && TickHandler.hasHandler(event.getEntityLiving(), Identifier.MEI_CRYSTAL) &&
				!event.getSource().canHarmInCreative()) 
			event.setCanceled(true);
	}

	@SubscribeEvent
	public void preventDamage(LivingAttackEvent event) {
		if (event.getEntityLiving() != null && TickHandler.hasHandler(event.getEntityLiving(), Identifier.MEI_CRYSTAL) &&
				!event.getSource().canHarmInCreative()) 
			event.setCanceled(true);
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void moveCrystalCamera(FOVUpdateEvent event) {
		if (Minecraft.getMinecraft().world != null &&
				TickHandler.hasHandler(Minecraft.getMinecraft().player, Identifier.MEI_CRYSTAL)) 
			event.setNewfov(event.getFov()+0.8f);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void preRenderGameOverlay(Pre event, EntityPlayer player, double width, double height) {
		// mei's crystal cancel overlay
		if (TickHandler.hasHandler(player, Identifier.MEI_CRYSTAL)) {
			GlStateManager.enableBlend();

			double scale = 0.8d*Config.guiScale;
			GlStateManager.scale(scale, scale, 1);
			GlStateManager.translate((int) ((width - 256*scale)/2d / scale), (int) ((height - 256*scale)/2d / scale), 0);
			Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(Minewatch.MODID, "textures/gui/mei_crystal.png"));
			GuiUtils.drawTexturedModalRect(0, 0, 0, 0, 256, 256, 0);

			GlStateManager.disableBlend();
		}
	}

}