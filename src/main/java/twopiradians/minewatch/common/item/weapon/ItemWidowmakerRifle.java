package twopiradians.minewatch.common.item.weapon;

import java.util.ArrayList;

import javax.annotation.Nullable;
import javax.vecmath.Vector2f;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.EntityViewRenderEvent.FOVModifier;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.client.model.ModelMWArmor;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.ability.EntityWidowmakerHook;
import twopiradians.minewatch.common.entity.ability.EntityWidowmakerMine;
import twopiradians.minewatch.common.entity.projectile.EntityWidowmakerBullet;
import twopiradians.minewatch.common.hero.Ability;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.util.EntityHelper;
import twopiradians.minewatch.common.util.TickHandler;
import twopiradians.minewatch.common.util.TickHandler.Handler;
import twopiradians.minewatch.common.util.TickHandler.Identifier;
import twopiradians.minewatch.packet.SPacketSimple;

public class ItemWidowmakerRifle extends ItemMWWeapon {

	private static final ResourceLocation SCOPE = new ResourceLocation(Minewatch.MODID + ":textures/gui/widowmaker_scope.png");
	private static final ResourceLocation SCOPE_BACKGROUND = new ResourceLocation(Minewatch.MODID + ":textures/gui/widowmaker_scope_background.png");
	private static final ResourceLocation ROPE = new ResourceLocation(Minewatch.MODID, "textures/entity/widowmaker_hook_rope.png");

	private boolean prevScoped;
	private float unscopedSensitivity;

	public static final Handler HOOK = new Handler(Identifier.WIDOWMAKER_HOOK, true) {
		private void move() {
			if (entityLiving.getPositionVector().equals(position))
				number++;
			else
				number = 0;
			position = entityLiving.getPositionVector();
			entityLiving.fallDistance = 0;
			Vec3d target = new Vec3d(entity.posX, entity.posY+entityLiving.height/2f, entity.posZ);
			Vec3d motion = target.subtract(entityLiving.getPositionVector()).normalize().scale(16/20d);
			entityLiving.motionX = motion.xCoord;
			entityLiving.motionY = motion.yCoord;
			entityLiving.motionZ = motion.zCoord;
			if (KeyBind.JUMP.isKeyDown(entityLiving) || number > 5 ||
					entityLiving.getDistance(target.xCoord, target.yCoord, target.zCoord) < 1.5d) {
				this.ticksLeft = 1;
				double modifier = KeyBind.JUMP.isKeyDown(entityLiving) ? 1.5d : 0.5d;
				entityLiving.motionX *= modifier;
				entityLiving.motionY *= modifier;
				entityLiving.motionZ *= modifier;
			}
		}
		@Override
		@SideOnly(Side.CLIENT)
		public boolean onClientTick() {
			if (entity instanceof EntityWidowmakerHook && ((EntityWidowmakerHook)entity).facing != null) 
				move();
			return super.onClientTick();
		}
		@Override
		public boolean onServerTick() {
			if (entity instanceof EntityWidowmakerHook && ((EntityWidowmakerHook)entity).facing != null) 
				move();
			return super.onServerTick();
		}
		@Override
		@SideOnly(Side.CLIENT)
		public Handler onClientRemove() {
			TickHandler.unregister(true, TickHandler.getHandler(handler -> handler.identifier == Identifier.ABILITY_USING && handler.ability == EnumHero.WIDOWMAKER.ability2, true));
			entity.setDead();
			return super.onClientRemove();
		}
		@Override
		public Handler onServerRemove() {
			TickHandler.unregister(false, TickHandler.getHandler(handler -> handler.identifier == Identifier.ABILITY_USING && handler.ability == EnumHero.WIDOWMAKER.ability2, false));
			if (entity instanceof EntityWidowmakerHook && ((EntityWidowmakerHook)entity).facing != null)
				EnumHero.WIDOWMAKER.ability2.keybind.setCooldown(player, 160, false); 
			entity.setDead();
			return super.onServerRemove();
		}
	};

	public ItemWidowmakerRifle() {
		super(30);
		this.saveEntityToNBT = true;
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return Integer.MAX_VALUE;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		return EnumAction.BOW;
	}

	@Override
	public void onUsingTick(ItemStack stack, EntityLivingBase player, int count) {
		if (player.world.isRemote) {
			int time = this.getMaxItemUseDuration(stack)-count-10;
			if (time == 4) 
				ModSoundEvents.WIDOWMAKER_CHARGE.playSound(player, 0.3f, 1f, true);
			else if (time == 10)
				ModSoundEvents.WIDOWMAKER_CHARGE.playSound(player, 0.5f, 1.1f, true);
			else if (time == 15) {
				ModSoundEvents.WIDOWMAKER_CHARGE.playSound(player, 0.8f, 1.8f, true);
				ModSoundEvents.WIDOWMAKER_CHARGE.playSound(player, 0.1f, 1f, true);
			}
		}
	}

	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World world, EntityLivingBase entity, int timeLeft) {
		if (!world.isRemote) 
			ModSoundEvents.WIDOWMAKER_UNSCOPE.playFollowingSound(entity, 1, 1, false);
		Minewatch.proxy.updateFOV();
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
		super.onUpdate(stack, world, entity, itemSlot, isSelected);

		// scope while right click
		if (entity instanceof EntityLivingBase && ((EntityLivingBase)entity).getActiveItemStack() != stack && 
				((EntityLivingBase)entity).getHeldItemMainhand() == stack && isScoped((EntityLivingBase) entity, stack)) {
			((EntityLivingBase)entity).setActiveHand(EnumHand.MAIN_HAND);
			if (!world.isRemote) {
				ModSoundEvents.WIDOWMAKER_SCOPE.playFollowingSound(entity, 1, 1, false);
				ModSoundEvents.WIDOWMAKER_SCOPE_VOICE.playFollowingSound(entity, 0.8f, 1, false);
			}
		}
		// unset active hand while reloading
		else if (entity instanceof EntityLivingBase && ((EntityLivingBase)entity).getActiveItemStack() == stack && 
				!isScoped((EntityLivingBase) entity, stack))
			((EntityLivingBase)entity).stopActiveHand();

		if (isSelected && entity instanceof EntityLivingBase && ((EntityLivingBase)entity).getHeldItemMainhand() == stack) {	
			EntityLivingBase player = (EntityLivingBase) entity;

			// venom mine
			if (!world.isRemote && hero.ability1.isSelected(player, true) && 
					this.canUse(player, true, EnumHand.MAIN_HAND, true)) {
				EntityWidowmakerMine mine = new EntityWidowmakerMine(world, player);
				EntityHelper.setAim(mine, player, player.rotationPitch, player.rotationYawHead, 19, 0, null, 0, 0);
				world.spawnEntity(mine);
				ModSoundEvents.WIDOWMAKER_MINE_THROW.playSound(player, 1, 1);
				player.getHeldItem(EnumHand.MAIN_HAND).damageItem(1, player);
				hero.ability1.keybind.setCooldown(player, 300, false); 
				if (hero.ability1.entities.get(player) instanceof EntityWidowmakerMine && 
						hero.ability1.entities.get(player).isEntityAlive()) 
					hero.ability1.entities.get(player).isDead = true;
				hero.ability1.entities.put(player, mine);
			}

			// hook
			if (!world.isRemote && hero.ability2.isSelected(player, true) && 
					this.canUse(player, true, EnumHand.MAIN_HAND, true)) {
				EntityWidowmakerHook projectile = new EntityWidowmakerHook(world, player, EnumHand.OFF_HAND.ordinal());
				EntityHelper.setAim(projectile, player, player.rotationPitch, player.rotationYawHead, 30, 0, EnumHand.OFF_HAND, 23, 0.5f);
				world.spawnEntity(projectile);
				TickHandler.register(false, HOOK.setEntity(projectile).setEntityLiving(player).setTicks(100),
						Ability.ABILITY_USING.setEntity(player).setTicks(100).setAbility(hero.ability2));
				Minewatch.network.sendToDimension(new SPacketSimple(55, player, false, projectile), world.provider.getDimension());
				ModSoundEvents.WIDOWMAKER_HOOK_THROW.playSound(player, 1, 1);
				player.getHeldItem(EnumHand.MAIN_HAND).damageItem(1, player);
			}
		}
	}

	@Override
	public void onItemLeftClick(ItemStack stack, World world, EntityLivingBase player, EnumHand hand) { 
		// shoot
		if (this.canUse(player, true, hand, false)) {
			// scoped
			if (KeyBind.RMB.isKeyDown(player) && player.getActiveItemStack() == stack) {
				if (!player.world.isRemote) {
					EntityWidowmakerBullet bullet = new EntityWidowmakerBullet(player.world, player, 2, true, 
							(int) (12+(120d-12d)*getPower(player)));
					EntityHelper.setAim(bullet, player, player.rotationPitch, player.rotationYawHead, -1, 0, null, 10, 0);
					player.world.spawnEntity(bullet);
					ModSoundEvents.WIDOWMAKER_SHOOT_1.playSound(player, player.world.rand.nextFloat()+0.5F, player.world.rand.nextFloat()/2+0.75f);
					this.setCooldown(player, 10);
					this.subtractFromCurrentAmmo(player, 3);
					if (player.world.rand.nextInt(10) == 0)
						stack.damageItem(1, player);
					player.resetActiveHand();
				}
				else 
					player.resetActiveHand();
			}
			// unscoped
			else if (!KeyBind.RMB.isKeyDown(player) && player.ticksExisted % 2 == 0) {
				if (!world.isRemote) {
					EntityWidowmakerBullet bullet = new EntityWidowmakerBullet(world, player, hand.ordinal(), false, 13);
					EntityHelper.setAim(bullet, player, player.rotationPitch, player.rotationYawHead, -1, 3, hand, 6, 0.43f);
					world.spawnEntity(bullet);
					ModSoundEvents.WIDOWMAKER_SHOOT_0.playSound(player, world.rand.nextFloat()/2f+0.2f, world.rand.nextFloat()/2+0.75f);
					this.subtractFromCurrentAmmo(player, 1);
					if (world.rand.nextInt(30) == 0)
						player.getHeldItem(hand).damageItem(1, player);
				}
			}
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void changeFOV(FOVModifier event) {
		if (event.getEntity() instanceof EntityPlayer && 
				isScoped((EntityPlayer) event.getEntity(), ((EntityPlayer) event.getEntity()).getHeldItemMainhand()) && 
				Minecraft.getMinecraft().gameSettings.thirdPersonView == 0) {
			event.setFOV(20f);
		}
	}

	/**Returns power: 0 - 1*/
	public double getPower(EntityLivingBase player) {
		return MathHelper.clamp((this.getMaxItemUseDuration(player.getHeldItemMainhand())-player.getItemInUseCount()-10)/15d, 0, 1);
	}

	/**Is this player scoping with the stack*/
	public static boolean isScoped(EntityLivingBase entity, ItemStack stack) {
		return entity != null && entity.getHeldItemMainhand() != null && 
				entity.getHeldItemMainhand().getItem() == EnumHero.WIDOWMAKER.weapon && !KeyBind.JUMP.isKeyDown(entity) &&
				(entity.getActiveItemStack() == stack || KeyBind.RMB.isKeyDown(entity)) && 
				(EnumHero.WIDOWMAKER.weapon.getCurrentAmmo(entity) > 0 || EnumHero.WIDOWMAKER.weapon.getMaxAmmo(entity) == 0);
	}

	//PORT correct scope scale
	@Override
	@SideOnly(Side.CLIENT)
	public void preRenderGameOverlay(Pre event, EntityPlayer player, double width, double height, EnumHand hand) {
		if (event.getType() == ElementType.ALL && player != null) {
			boolean scoped = isScoped(player, player.getHeldItemMainhand()) && 
					Minecraft.getMinecraft().gameSettings.thirdPersonView == 0;

			// change mouse sensitivity
			if (scoped != prevScoped) {
				if (scoped) {
					this.unscopedSensitivity = Minecraft.getMinecraft().gameSettings.mouseSensitivity;
					Minecraft.getMinecraft().gameSettings.mouseSensitivity = this.unscopedSensitivity / 2f;
				}
				else
					Minecraft.getMinecraft().gameSettings.mouseSensitivity = this.unscopedSensitivity;
				this.prevScoped = scoped;
			}

			// render scope
			if (scoped) {
				int imageSize = 256;

				// power
				GlStateManager.pushMatrix();
				GlStateManager.enableBlend();
				int power = player.getActiveItemStack() == player.getHeldItemMainhand() ? (int) (getPower(player)*100d) : 0;
				int powerWidth = Minecraft.getMinecraft().fontRendererObj.getStringWidth(power+"%");
				Minecraft.getMinecraft().fontRendererObj.drawString(power+"%", (int) width/2-powerWidth/2, (int) height/2+40, 0xFFFFFF);
				// scope
				Minecraft.getMinecraft().getTextureManager().bindTexture(SCOPE);
				GuiUtils.drawTexturedModalRect((int) (width/2-imageSize/2), (int) (height/2-imageSize/2), 0, 0, imageSize, imageSize, 0);
				// background
				GlStateManager.disableAlpha();
				GlStateManager.scale(width/256d, height/256d, 1);
				Minecraft.getMinecraft().getTextureManager().bindTexture(SCOPE_BACKGROUND);
				GuiUtils.drawTexturedModalRect(0, 0, 0, 0, imageSize, imageSize, 0);
				GlStateManager.popMatrix();
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ArrayList<String> getAllModelLocations(ArrayList<String> locs) {
		locs.add("_scoping");
		return super.getAllModelLocations(locs);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public String getModelLocation(ItemStack stack, @Nullable EntityLivingBase entity) {
		boolean scoping = entity instanceof EntityLivingBase && isScoped((EntityLivingBase) entity, stack);
		return scoping ? "_scoping" : "";
	}	
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldRenderHand(AbstractClientPlayer player, EnumHand hand) {
		return hand == EnumHand.OFF_HAND &&
				TickHandler.hasHandler(handler -> handler.identifier == Identifier.WIDOWMAKER_HOOK && handler.entityLiving == Minecraft.getMinecraft().player, true);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean preRenderArmor(EntityLivingBase entity, ModelMWArmor model) { 
		// hold left arm out while using hook TEST /kill breaks armor ?
		if (TickHandler.hasHandler(handler -> handler.identifier == Identifier.WIDOWMAKER_HOOK && handler.entityLiving == entity, true)) {
			model.bipedLeftArmwear.rotateAngleX = 5;
			model.bipedLeftArm.rotateAngleX = 5;
			model.bipedLeftArmwear.rotateAngleY = -0.2f;
			model.bipedLeftArm.rotateAngleY = -0.2f;
		}

		return false;
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void renderHookRope(RenderWorldLastEvent event) {
		for (Handler handler : TickHandler.getHandlers(true, null, Identifier.WIDOWMAKER_HOOK, null)) {
			// rope
			if (handler.entity instanceof EntityWidowmakerHook && 
					((EntityWidowmakerHook) handler.entity).getThrower() != null) {
				EntityWidowmakerHook entity = (EntityWidowmakerHook) handler.entity;
				Minecraft mc = Minecraft.getMinecraft();
				GlStateManager.pushMatrix();
				GlStateManager.enableLighting();
				mc.getTextureManager().bindTexture(ROPE);
				Tessellator tessellator = Tessellator.getInstance();
				VertexBuffer buffer = tessellator.getBuffer();
				buffer.begin(GL11.GL_QUAD_STRIP, DefaultVertexFormats.POSITION_TEX);

				double width = 0.04d;
				Vec3d playerPos = EntityHelper.getEntityPartialPos(Minewatch.proxy.getRenderViewEntity());
				Vec3d throwerPos = EntityHelper.getEntityPartialPos(entity.getThrower());
				Vector2f rotations = EntityHelper.getEntityPartialRotations(entity.getThrower());
				Vec3d shooting = EntityHelper.getShootingPos(entity.getThrower(), rotations.x, rotations.y, EnumHand.OFF_HAND, 23, 0.7f).subtract(throwerPos);

				// translate to thrower
				Vec3d translate = throwerPos.subtract(playerPos);
				GlStateManager.translate(translate.xCoord, translate.yCoord, translate.zCoord);
				
				Vec3d hookLook = entity.getLook(mc.getRenderPartialTicks()).scale(0.17d);
				Vec3d hookPos = EntityHelper.getEntityPartialPos(entity).addVector(0, entity.height/2f, 0).subtract(hookLook).subtract(throwerPos);
				double v = hookPos.distanceTo(shooting)*2d;

				double deg_to_rad = 0.0174532925d;
				double precision = 0.05d;
				double degrees = 360d;
				double steps = Math.round(degrees*precision);
				degrees += 21.2d;
				double angle = 0;

				for (int i=1; i<=steps; i+=2) {
					angle = degrees/steps*i;
					double circleX = Math.cos(angle*deg_to_rad);
					double circleY = Math.sin(angle*deg_to_rad);
					double circleZ = 0;//Math.cos(angle*deg_to_rad);
					Vec3d vec = new Vec3d(circleX, circleY, circleZ).scale(width).add(hookPos);
					buffer.pos(vec.xCoord, vec.yCoord, vec.zCoord).tex(i/steps, 0).endVertex();

					vec = new Vec3d(circleX, circleY, circleZ).scale(width).add(shooting);
					buffer.pos(vec.xCoord, vec.yCoord, vec.zCoord).tex(i/steps, v).endVertex();

					angle = degrees/steps*(i+1);
					circleX = Math.cos(angle*deg_to_rad);
					circleY = Math.sin(angle*deg_to_rad);
					circleZ = 0;//Math.cos(angle*deg_to_rad);
					vec = new Vec3d(circleX, circleY, circleZ).scale(width).add(hookPos);
					buffer.pos(vec.xCoord, vec.yCoord, vec.zCoord).tex((i+1)/steps, 0).endVertex();

					vec = new Vec3d(circleX, circleY, circleZ).scale(width).add(shooting);
					buffer.pos(vec.xCoord, vec.yCoord, vec.zCoord).tex((i+1)/steps, v).endVertex();
				}


				tessellator.draw();
				GlStateManager.popMatrix();
			}
		}
	}

}