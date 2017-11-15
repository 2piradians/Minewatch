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
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.client.model.ModelMWArmor;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.config.Config;
import twopiradians.minewatch.common.entity.projectile.EntityMeiBlast;
import twopiradians.minewatch.common.entity.projectile.EntityMeiCrystal;
import twopiradians.minewatch.common.entity.projectile.EntityMeiIcicle;
import twopiradians.minewatch.common.hero.Ability;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.potion.ModPotions;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.tickhandler.TickHandler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Handler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Identifier;
import twopiradians.minewatch.common.util.EntityHelper;
import twopiradians.minewatch.common.util.Handlers;
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
			Minewatch.proxy.stopSound(player, ModSoundEvents.meiCrystalStart, SoundCategory.PLAYERS);
			Minewatch.proxy.playFollowingSound(entity, ModSoundEvents.meiCrystalStop, SoundCategory.PLAYERS, 1.0f, 1.0f, false);
			Minecraft.getMinecraft().gameSettings.thirdPersonView = thirdPersonView;
			return super.onClientRemove();
		}
		@Override
		public Handler onServerRemove() {
			if (player != null)
			EnumHero.MEI.ability2.keybind.setCooldown(player, 240, false); 
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
			EntityHelper.setAim(bullet, player, player.rotationPitch, player.rotationYaw, 20, 0.6F, hand, 14, 0.8f);
			world.spawnEntity(bullet);
			world.playSound(null, player.posX, player.posY, player.posZ, 
					ModSoundEvents.meiShoot, SoundCategory.PLAYERS, world.rand.nextFloat()/3, 
					world.rand.nextFloat()/2+0.75f);	

			this.subtractFromCurrentAmmo(player, 1);
			if (world.rand.nextInt(200) == 0)
				player.getHeldItem(hand).damageItem(1, player);
		}
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		// shoot
		if (this.canUse(player, true, hand, false)) {//TODO delay
			if (!world.isRemote) {
				EntityMeiIcicle icicle = new EntityMeiIcicle(world, player, hand.ordinal());
				EntityHelper.setAim(icicle, player, player.rotationPitch, player.rotationYaw, 100, 0.4F, hand, 8, 0.35f);
				world.spawnEntity(icicle);
				if (!player.getCooldownTracker().hasCooldown(this))
					player.getCooldownTracker().setCooldown(this, 24);
				world.playSound(null, player.posX, player.posY, player.posZ, ModSoundEvents.meiIcicleShoot, 
						SoundCategory.PLAYERS, world.rand.nextFloat()+0.5F, world.rand.nextFloat()/20+0.95f);	
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
		
		if (isSelected && entity instanceof EntityPlayer) {	
			EntityPlayer player = (EntityPlayer) entity;

			Handler handler = TickHandler.getHandler(player, Identifier.MEI_CRYSTAL);
			if (!world.isRemote && handler != null && 
					(KeyBind.RMB.isKeyDown(player) || KeyBind.LMB.isKeyDown(player)) &&
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

	@Override
	@SideOnly(Side.CLIENT)
	public void preRenderArmor(EntityLivingBase entity, ModelMWArmor model) {
		// frozen coloring
		if (TickHandler.hasHandler(entity, Identifier.POTION_FROZEN) || 
				(entity != null && entity.getActivePotionEffect(ModPotions.frozen) != null && 
				entity.getActivePotionEffect(ModPotions.frozen).getDuration() > 0)) {
			int freeze = TickHandler.getHandler(entity, Identifier.POTION_FROZEN) != null ? 
					TickHandler.getHandler(entity, Identifier.POTION_FROZEN).ticksLeft : 30;
					entity.maxHurtTime = -1;
					entity.hurtTime = -1;
					GlStateManager.color(1f-freeze/30f, 1f-freeze/120f, 1f);
		}
	}

	@SubscribeEvent
	public void preventDamage(LivingHurtEvent event) {
		if (event.getEntityLiving() != null && TickHandler.hasHandler(event.getEntityLiving(), Identifier.MEI_CRYSTAL)) 
			event.setCanceled(true);
	}

	@SubscribeEvent
	public void preventDamage(LivingAttackEvent event) {
		if (event.getEntityLiving() != null && TickHandler.hasHandler(event.getEntityLiving(), Identifier.MEI_CRYSTAL)) 
			event.setCanceled(true);
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void moveCrystalCamera(FOVUpdateEvent event) {
		if (Minecraft.getMinecraft().world != null &&
				TickHandler.hasHandler(Minecraft.getMinecraft().player, Identifier.MEI_CRYSTAL)) 
			event.setNewfov(event.getFov()+0.8f);
	}

}