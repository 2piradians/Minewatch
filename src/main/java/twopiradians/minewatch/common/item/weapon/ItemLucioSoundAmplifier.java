package twopiradians.minewatch.common.item.weapon;

import java.util.HashMap;
import java.util.UUID;

import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.config.Config;
import twopiradians.minewatch.common.entity.hero.EntityHero;
import twopiradians.minewatch.common.entity.hero.EntityLucio;
import twopiradians.minewatch.common.entity.projectile.EntityLucioSonic;
import twopiradians.minewatch.common.hero.Ability;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.hero.SetManager;
import twopiradians.minewatch.common.sound.FollowingSound;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.util.EntityHelper;
import twopiradians.minewatch.common.util.TickHandler;
import twopiradians.minewatch.common.util.TickHandler.Handler;
import twopiradians.minewatch.common.util.TickHandler.Identifier;
import twopiradians.minewatch.packet.SPacketSimple;

public class ItemLucioSoundAmplifier extends ItemMWWeapon {

	public int affectedEntities;
	public static HashMap<UUID, FollowingSound> healSounds = Maps.newHashMap();
	public static HashMap<UUID, FollowingSound> speedSounds = Maps.newHashMap();

	public static final Handler AMP = new Handler(Identifier.LUCIO_AMP, false) {
		@Override
		public Handler onServerRemove() {
			EnumHero.LUCIO.ability2.keybind.setCooldown(entityLiving, 240, false); 
			return super.onServerRemove();
		}
	};

	public static final Handler SONIC = new Handler(Identifier.LUCIO_SONIC, true) {
		@Override
		public boolean onServerTick() {
			EnumHand hand = null;
			if (this.number >= 0 && this.number < EnumHand.values().length)
				hand = EnumHand.values()[(int) this.number];
			if (hand != null && this.ticksLeft < this.initialTicks && this.ticksLeft % 2 == 0 && entityLiving != null && entityLiving.getHeldItem(hand) != null && 
					entityLiving.getHeldItem(hand).getItem() == EnumHero.LUCIO.weapon && 
					EnumHero.LUCIO.weapon.canUse(entityLiving, false, hand, false, EnumHero.LUCIO.ability1, EnumHero.LUCIO.ability2)) {
				EntityLucioSonic sonic = new EntityLucioSonic(entityLiving.world, entityLiving, hand.ordinal());
				EntityHelper.setAim(sonic, entityLiving, entityLiving.rotationPitch, entityLiving.rotationYawHead, 50f, 0, hand, 12, 0.15f);
				entityLiving.world.spawnEntity(sonic);
				if (this.ticksLeft >= this.initialTicks - 2)
					ModSoundEvents.LUCIO_SHOOT.playFollowingSound(entityLiving, entityLiving.world.rand.nextFloat()+0.5F, entityLiving.world.rand.nextFloat()/20+0.95f, false);
				EnumHero.LUCIO.weapon.subtractFromCurrentAmmo(entityLiving, 1);
				if (entityLiving.world.rand.nextInt(25) == 0)
					entityLiving.getHeldItem(hand).damageItem(1, entityLiving);
			}
			return super.onServerTick();
		}

		@Override
		public Handler onServerRemove() {
			EnumHero.LUCIO.weapon.setCooldown(entityLiving, 10);
			return super.onServerRemove();
		}
	};

	public ItemLucioSoundAmplifier() {
		super(30);
		MinecraftForge.EVENT_BUS.register(this);
		this.showHealthParticles = true;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityLivingBase player, EnumHand hand) {
		// soundwave
		if (hand == EnumHand.MAIN_HAND && !world.isRemote && this.canUse(player, true, hand, false, hero.ability1, hero.ability2) && 
				hero.ability1.isSelected(player, false, hero.ability1, hero.ability2) && this.getCurrentAmmo(player) >= 4) {
			this.subtractFromCurrentAmmo(player, 4, EnumHand.MAIN_HAND);
			hero.ability1.keybind.setCooldown(player, 80, false);
			player.getHeldItem(hand).damageItem(1, player);
			Minewatch.network.sendToDimension(new SPacketSimple(38, player, false), world.provider.getDimension());
			// if player, needs to get player motion from client
			if (player instanceof EntityPlayerMP)
				Minewatch.network.sendTo(new SPacketSimple(39, false, (EntityPlayerMP) player), (EntityPlayerMP) player);
			else
				soundwave(player, player.motionX, player.motionY, player.motionZ);
		}

		return new ActionResult(EnumActionResult.PASS, player.getHeldItem(hand));
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isSelected) {
		super.onUpdate(stack, world, entity, slot, isSelected);

		if (entity instanceof EntityLivingBase && isSelected) {
			EntityLivingBase player = (EntityLivingBase)entity;
			boolean doPassive = player.ticksExisted % 3 == 0;

			// crossfade
			if (!world.isRemote && hero.ability3.isSelected(player, true, hero.ability1, hero.ability2) && 
					this.canUse(player, true, EnumHand.MAIN_HAND, true, hero.ability1, hero.ability2)) {
				ModSoundEvents.LUCIO_CROSSFADE.playFollowingSound(player, 1, 1, false);
				setAlternate(stack, !isAlternate(stack));
				doPassive = true;
			}

			boolean heal = isAlternate(stack);
			boolean amp = TickHandler.hasHandler(player, Identifier.LUCIO_AMP);

			// passive
			if (doPassive && this.canUse(player, true, EnumHand.MAIN_HAND, true, hero.ability1, hero.ability2) &&
					SetManager.getWornSet(player) == hero) {
				if (world.isRemote && player == Minewatch.proxy.getClientPlayer())
					this.affectedEntities = 0;
				else if (!world.isRemote && player instanceof EntityLucio)
					((EntityLucio)player).affectedEntities.clear();
				for (Entity entity2 : world.getEntitiesWithinAABBExcludingEntity(player, 
						new AxisAlignedBB(player.getPosition().add(-10, -10, -10), 
								player.getPosition().add(10, 10, 10))))
					// nearby
					if (entity2 instanceof EntityLivingBase && entity2.getDistanceToEntity(player) <= 10 &&
					EntityHelper.shouldHit(player, entity2, true) && !(entity2 instanceof EntityArmorStand)) {
						if (!world.isRemote) {
							if (heal)
								EntityHelper.attemptDamage(player, entity2, amp ? -7.02f : -2.4375f, true);
							else
								((EntityLivingBase)entity2).addPotionEffect(new PotionEffect(MobEffects.SPEED, 5, amp ? 3 : 1, true, false));
							if (player instanceof EntityLucio)
								((EntityLucio)player).affectedEntities.add((EntityLivingBase) entity2);
						}
						else if (player == Minewatch.proxy.getClientPlayer())
							++this.affectedEntities;
					}
				// self
				if (!world.isRemote)
					if (heal)
						EntityHelper.attemptDamage(player, player, amp ? -5.265f : -1.828125f, true);
					else
						player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 5, amp ? 3 : 1, true, false));
			}

			// amp
			if (!world.isRemote && hero.ability2.isSelected(player) && 
					this.canUse(player, true, EnumHand.MAIN_HAND, true)) {
				TickHandler.register(false, AMP.setEntity(player).setTicks(60),
						Ability.ABILITY_USING.setEntity(player).setTicks(60).setAbility(hero.ability2));
				ModSoundEvents.LUCIO_AMP.playFollowingSound(player, 1, 1, false);
				ModSoundEvents.LUCIO_AMP_VOICE.playFollowingSound(player, 1, 1, false);
				if (player instanceof EntityPlayerMP)
					Minewatch.network.sendTo(new SPacketSimple(37), (EntityPlayerMP) player);
			}
		}
	}	

	@Override
	public void onItemLeftClick(ItemStack stack, World world, EntityLivingBase player, EnumHand hand) { 
		// sonic attack
		if (this.canUse(player, true, hand, false, hero.ability1, hero.ability2) && 
				!world.isRemote && !TickHandler.hasHandler(player, Identifier.LUCIO_SONIC)) {
			TickHandler.register(false, SONIC.setEntity(player).setTicks(10).setNumber(hand.ordinal()));
		}
	}

	/**Do soundwave effect - with specified motion instead of player's motion (bc players only have motion on client)*/
	public static void soundwave(EntityLivingBase player, double motionX, double motionY, double motionZ) {
		if (player != null) {
			boolean playSound = false;
			for (Entity entity : player.world.getEntitiesWithinAABBExcludingEntity(player, player.getEntityBoundingBox().grow(7))) 
				if (EntityHelper.shouldHit(player, entity, false) && EntityHelper.isInFieldOfVision(player, entity, 90)) {
					double distance = player.getDistanceToEntity(entity);
					Vec3d look = player.getLookVec().scale(2);
					Vec3d base = player.getLookVec().scale(player instanceof EntityHero ? 3 : 2);
					base = new Vec3d(base.x, base.y * 1.5d, base.z);
					entity.motionX += (Math.abs(motionX)*look.x+base.x) * (8-distance) / 8f;
					entity.motionY += (Math.abs(motionY+0.08d)*look.y+base.y+0.08d) * (8-distance) / 8f;
					entity.motionZ += (Math.abs(motionZ)*look.z+base.z) * (8-distance) / 8f;
					entity.velocityChanged = true;
					entity.onGround = false;
					entity.isAirBorne = true;
					if (!player.world.isRemote)
						EntityHelper.attemptDamage(player, entity, 25, false);
					playSound = true;
				}
			if (playSound && !player.world.isRemote)
				ModSoundEvents.LUCIO_SOUNDWAVE_VOICE.playFollowingSound(player, 1, 1, false);
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void clientTick(LivingUpdateEvent event) {
		if (event.getEntityLiving().world.isRemote) {
			ItemStack main = event.getEntityLiving().getHeldItemMainhand();
			UUID uuid = event.getEntityLiving().getPersistentID();
			boolean heal = ItemMWWeapon.isAlternate(main);
			// stop sounds if not holding amplifier
			if (SetManager.getWornSet(event.getEntityLiving()) != EnumHero.LUCIO ||
					main == null || main.getItem() != this) {
				FollowingSound.stopPlaying(healSounds.get(uuid));
				FollowingSound.stopPlaying(speedSounds.get(uuid));
				healSounds.remove(uuid);
				speedSounds.remove(uuid);
			}
			// play heal
			else if (heal && !healSounds.containsKey(uuid)) {
				FollowingSound.stopPlaying(speedSounds.get(uuid));
				speedSounds.remove(uuid);
				FollowingSound sound = (FollowingSound) ModSoundEvents.LUCIO_PASSIVE_HEAL.playFollowingSound(event.getEntityLiving(), 0.8f, 1, true);
				ModSoundEvents.LUCIO_PASSIVE_HEAL_VOICE.playFollowingSound(event.getEntityLiving(), 1.0f, 1.0f, false);
				if (sound != null) {
					sound.lucioSound = true;
					healSounds.put(uuid, sound);
				}
			}
			// play speed
			else if (!heal && !speedSounds.containsKey(uuid)) {
				FollowingSound.stopPlaying(healSounds.get(uuid));
				healSounds.remove(uuid);
				FollowingSound sound = (FollowingSound) ModSoundEvents.LUCIO_PASSIVE_SPEED.playFollowingSound(event.getEntityLiving(), 0.8f, 1, true);
				ModSoundEvents.LUCIO_PASSIVE_SPEED_VOICE.playFollowingSound(event.getEntityLiving(), 1.0f, 1.0f, false);
				if (sound != null) {
					sound.lucioSound = true;
					speedSounds.put(uuid, sound);
				}
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void preRenderGameOverlay(Pre event, EntityPlayer player, double width, double height, EnumHand hand) {
		if (hand == EnumHand.MAIN_HAND && event.getType() == ElementType.CROSSHAIRS && 
				SetManager.getWornSet(player) == hero) {
			// passive speed / heal
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

			GlStateManager.pushMatrix();
			boolean heal = isAlternate(player.getHeldItemMainhand());
			double scale = 0.3d*Config.guiScale;
			GlStateManager.scale(scale, scale, 1);
			GlStateManager.translate((int) ((width - 256*scale)/2d / scale), (int) ((height - 256*scale)/2d / scale), 0);
			Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(Minewatch.MODID, "textures/gui/lucio_passive.png"));
			GuiUtils.drawTexturedModalRect((int) ((heal ? -8 : 10) / scale), (int) (50 / scale), 0, heal ? 100 : 0, 256, 100, 0);
			GlStateManager.popMatrix();
			if (this.affectedEntities > 0)
				Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(String.valueOf(this.affectedEntities), 
						(int) (width/2d - Minecraft.getMinecraft().fontRenderer.getStringWidth(String.valueOf(this.affectedEntities))/2d)+1, 
						(int) (height/2d)+22, 0xFFFFFF);

			GlStateManager.disableBlend();
		}
	}

}