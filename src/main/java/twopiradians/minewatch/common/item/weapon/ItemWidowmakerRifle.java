package twopiradians.minewatch.common.item.weapon;

import java.util.ArrayList;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.client.event.EntityViewRenderEvent.FOVModifier;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.ability.EntityWidowmakerHook;
import twopiradians.minewatch.common.entity.ability.EntityWidowmakerMine;
import twopiradians.minewatch.common.entity.projectile.EntityWidowmakerBullet;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.util.EntityHelper;
import twopiradians.minewatch.common.util.TickHandler;
import twopiradians.minewatch.common.util.TickHandler.Identifier;

public class ItemWidowmakerRifle extends ItemMWWeapon {

	private static final ResourceLocation SCOPE = new ResourceLocation(Minewatch.MODID + ":textures/gui/widowmaker_scope.png");
	private static final ResourceLocation SCOPE_BACKGROUND = new ResourceLocation(Minewatch.MODID + ":textures/gui/widowmaker_scope_background.png");

	private boolean prevScoped;
	private float unscopedSensitivity;

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
	public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
		super.onUpdate(stack, world, entity, itemSlot, isSelected);

		// scope while right click
		if (entity instanceof EntityLivingBase && ((EntityLivingBase)entity).getActiveItemStack() != stack && 
				((EntityLivingBase)entity).getHeldItemMainhand() == stack && isScoped((EntityLivingBase) entity, stack)) 
			((EntityLivingBase)entity).setActiveHand(EnumHand.MAIN_HAND);
		// unset active hand while reloading
		else if (entity instanceof EntityLivingBase && ((EntityLivingBase)entity).getActiveItemStack() == stack && 
				!isScoped((EntityLivingBase) entity, stack))
			((EntityLivingBase)entity).resetActiveHand();

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
				EntityHelper.setAim(projectile, player, player.rotationPitch, player.rotationYawHead, 19, 0, EnumHand.OFF_HAND, 23, 0.5f);
				world.spawnEntity(projectile);
				ModSoundEvents.WIDOWMAKER_MINE_THROW.playSound(player, 1, 1);
				player.getHeldItem(EnumHand.MAIN_HAND).damageItem(1, player);
				hero.ability2.keybind.setCooldown(player, 160, false); 
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
					player.stopActiveHand();
				}
				else 
					player.stopActiveHand();
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
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void renderArms(RenderSpecificHandEvent event) {
		// render arms while holding weapons - modified from ItemRenderer#renderArmFirstPerson
		ItemStack stack = Minewatch.proxy.getClientPlayer() == null ? null : Minewatch.proxy.getClientPlayer().getHeldItemMainhand();
		if (event.getHand() == EnumHand.OFF_HAND && stack != null && stack.getItem() == this) {
			GlStateManager.pushMatrix();
			Minecraft mc = Minecraft.getMinecraft();
			AbstractClientPlayer player = mc.player;
			float partialTicks = mc.getRenderPartialTicks();
			float swing = player.getSwingProgress(partialTicks);	
			float f7 = event.getHand() == EnumHand.MAIN_HAND ? swing : 0.0F;
			// would move hand to follow item - but equippedProgress is private
			float mainProgress = 0.0F;// - (mc.getItemRenderer().prevEquippedProgressMainHand + (this.equippedProgressMainHand - this.prevEquippedProgressMainHand) * partialTicks);
			float offProgress = 0.0F;// - (mc.getItemRenderer().prevEquippedProgressOffHand + (this.equippedProgressOffHand - this.prevEquippedProgressOffHand) * partialTicks);
			float progress = event.getHand() == EnumHand.MAIN_HAND ? mainProgress : offProgress;
			EnumHandSide side = event.getHand() == EnumHand.MAIN_HAND ? player.getPrimaryHand() : player.getPrimaryHand().opposite();
			boolean flag = side != EnumHandSide.LEFT;
			float f = flag ? 1.0F : -1.0F;
			float f1 = MathHelper.sqrt(f7);
			float f2 = -0.3F * MathHelper.sin(f1 * (float)Math.PI);
			float f3 = 0.4F * MathHelper.sin(f1 * ((float)Math.PI * 2F));
			float f4 = -0.4F * MathHelper.sin(f3 * (float)Math.PI);
			GlStateManager.translate(f * (f2 + 0.64000005F), f3 + -0.6F + progress * -0.6F, f4 + -0.71999997F);
			GlStateManager.rotate(f * 45.0F, 0.0F, 1.0F, 0.0F);
			float f5 = MathHelper.sin(f3 * f3 * (float)Math.PI);
			float f6 = MathHelper.sin(f1 * (float)Math.PI);
			GlStateManager.rotate(f * f6 * 70.0F, 0.0F, 1.0F, 0.0F);
			GlStateManager.rotate(f * f5 * -20.0F, 0.0F, 0.0F, 1.0F);
			AbstractClientPlayer abstractclientplayer = mc.player;
			mc.getTextureManager().bindTexture(abstractclientplayer.getLocationSkin());
			GlStateManager.translate(f * -1.0F, 3.6F, 3.5F);
			GlStateManager.rotate(f * 120.0F, 0.0F, 0.0F, 1.0F);
			GlStateManager.rotate(200.0F, 1.0F, 0.0F, 0.0F);
			GlStateManager.rotate(f * -135.0F, 0.0F, 1.0F, 0.0F);
			GlStateManager.translate(f * 5.6F, 0.0F, 0.0F);
			RenderPlayer renderplayer = (RenderPlayer)mc.getRenderManager().getEntityRenderObject(abstractclientplayer);
			GlStateManager.disableCull();

			if (flag)
				renderplayer.renderRightArm(abstractclientplayer);
			else
				renderplayer.renderLeftArm(abstractclientplayer);

			GlStateManager.enableCull();
			GlStateManager.popMatrix();
		}
	}

}