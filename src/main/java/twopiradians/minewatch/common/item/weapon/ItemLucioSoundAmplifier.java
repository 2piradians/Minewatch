package twopiradians.minewatch.common.item.weapon;

import java.util.HashMap;
import java.util.UUID;

import com.google.common.collect.Maps;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import net.minecraftforge.client.event.RenderWorldLastEvent;
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
import twopiradians.minewatch.common.item.armor.ItemMWArmor;
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
					ItemMWArmor.SetManager.getWornSet(player) == hero) {
				if (world.isRemote && player == Minewatch.proxy.getClientPlayer())
					this.affectedEntities = 0;
				else if (!world.isRemote && player instanceof EntityLucio)
					((EntityLucio)player).affectedEntities.clear();
				for (Entity entity2 : world.getEntitiesWithinAABBExcludingEntity(player, 
						new AxisAlignedBB(player.getPosition().add(-10, -10, -10), 
								player.getPosition().add(10, 10, 10))))
					// nearby
					if (entity2 instanceof EntityLivingBase && entity2.getDistanceToEntity(player) <= 10 &&
					EntityHelper.shouldHit(player, entity2, true)) {
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
			if (ItemMWArmor.SetManager.getWornSet(event.getEntityLiving()) != EnumHero.LUCIO ||
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
	public void preRenderGameOverlay(Pre event, EntityPlayer player, double width, double height) {
		if (player.getHeldItemMainhand() != null && player.getHeldItemMainhand().getItem() == this) {
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

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void renderCircle(RenderWorldLastEvent event) {
		for (Entity entity : Minecraft.getMinecraft().world.loadedEntityList) {
			if (entity instanceof EntityLivingBase && ItemMWArmor.SetManager.getWornSet(entity) == EnumHero.LUCIO && 
					((EntityLivingBase) entity).getHeldItemMainhand() != null && 
					((EntityLivingBase) entity).getHeldItemMainhand().getItem() == this &&
					EntityHelper.shouldTarget(entity, Minecraft.getMinecraft().player, true)) {

				float partialTicks = event.getPartialTicks();
				Entity player = Minecraft.getMinecraft().getRenderViewEntity();
				if (player == null)
					player = Minecraft.getMinecraft().player;
				double entityX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double)partialTicks;
				double entityY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double)partialTicks;
				double entityZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double)partialTicks;
				double playerX = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double)partialTicks;
				double playerY = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double)partialTicks;
				double playerZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double)partialTicks;
				double x = entityX-playerX;
				double y = entityY-playerY;
				double z = entityZ-playerZ;

				GlStateManager.pushMatrix();
				GlStateManager.enableBlend();
				GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
				Minecraft.getMinecraft().getRenderManager().renderEngine.bindTexture(new ResourceLocation(Minewatch.MODID, "textures/gui/lucio_circle.png"));
				GlStateManager.depthMask(false);
				float f = 10;//this.shadowSize;

				int i = MathHelper.floor(entityX - (double)f);
				int j = MathHelper.floor(entityX + (double)f);
				int k = MathHelper.floor(entityY - (double)f);
				int l = MathHelper.floor(entityY + 1);
				int i1 = MathHelper.floor(entityZ - (double)f);
				int j1 = MathHelper.floor(entityZ + (double)f);
				double d2 = x - entityX;
				double d3 = y - entityY;
				double d4 = z - entityZ;
				Tessellator tessellator = Tessellator.getInstance();
				BufferBuilder vertexbuffer = tessellator.getBuffer();
				vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);

				boolean heal = isAlternate(((EntityLivingBase)entity).getHeldItemMainhand());
				for (BlockPos blockpos : BlockPos.getAllInBoxMutable(new BlockPos(i, k, i1), new BlockPos(j, l, j1)))
				{
					IBlockState iblockstate = entity.world.getBlockState(blockpos.down());

					if (iblockstate.getRenderType() != EnumBlockRenderType.INVISIBLE && entity.world.getLightFromNeighbors(blockpos) > 3)
					{
						this.renderShadowSingle(entity.world, iblockstate, 
								heal ? 253f/255f : 9f/255f, heal ? 253f/255f : 222f/255f, heal ? 71f/255f : 123f/255f, 
										x, y, z, blockpos, 1, 10, d2, d3, d4);
					}
				}

				tessellator.draw();
				GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
				GlStateManager.disableBlend();
				GlStateManager.depthMask(true);
				GlStateManager.popMatrix();
			}
		}
	}

	@SideOnly(Side.CLIENT)
	private void renderShadowSingle(World world, IBlockState state, float red, float green, float blue, double x, double y, double z, BlockPos pos, float one, float size, double x2, double y2, double z2)
	{
		if (!state.isTranslucent() || state.getBlock() == Blocks.SNOW_LAYER)
		{
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder vertexbuffer = tessellator.getBuffer();
			double d0 = ((double)one - (y - ((double)pos.getY() + y2)) / 2.0D) * 0.5D * (double)world.getLightBrightness(pos);

			if (d0 >= 0.0D)
			{
				if (d0 > 1.0D)
				{
					d0 = 1.0D;
				}

				AxisAlignedBB axisalignedbb = state.getBoundingBox(world, pos);
				double d1 = (double)pos.getX() + axisalignedbb.minX + x2;
				double d2 = (double)pos.getX() + axisalignedbb.maxX + x2;
				double d3 = (double)pos.getY() + axisalignedbb.minY + y2 + 0.015625D - (1d-axisalignedbb.maxY);
				double d4 = (double)pos.getZ() + axisalignedbb.minZ + z2;
				double d5 = (double)pos.getZ() + axisalignedbb.maxZ + z2;
				float f = (float)((x - d1) / 2.0D / (double)size + 0.5D);
				float f1 = (float)((x - d2) / 2.0D / (double)size + 0.5D);
				float f2 = (float)((z - d4) / 2.0D / (double)size + 0.5D);
				float f3 = (float)((z - d5) / 2.0D / (double)size + 0.5D);
				vertexbuffer.pos(d1, d3, d4).tex((double)f, (double)f2).color(red, green, blue, (float)d0).endVertex();
				vertexbuffer.pos(d1, d3, d5).tex((double)f, (double)f3).color(red, green, blue, (float)d0).endVertex();
				vertexbuffer.pos(d2, d3, d5).tex((double)f1, (double)f3).color(red, green, blue, (float)d0).endVertex();
				vertexbuffer.pos(d2, d3, d4).tex((double)f1, (double)f2).color(red, green, blue, (float)d0).endVertex();
			}
		}
	}

}