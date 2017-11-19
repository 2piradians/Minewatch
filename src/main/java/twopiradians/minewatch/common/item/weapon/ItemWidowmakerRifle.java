package twopiradians.minewatch.common.item.weapon;

import java.util.ArrayList;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.event.EntityViewRenderEvent.FOVModifier;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.ability.EntityWidowmakerMine;
import twopiradians.minewatch.common.entity.projectile.EntityWidowmakerBullet;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.util.EntityHelper;

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
				player.playSound(ModSoundEvents.widowmakerCharge, 0.3f, 1f);
			else if (time == 10)
				player.playSound(ModSoundEvents.widowmakerCharge, 0.5f, 1.1f);
			else if (time == 15) {
				player.playSound(ModSoundEvents.widowmakerCharge, 0.8f, 1.8f);
				player.playSound(ModSoundEvents.widowmakerCharge, 0.1f, 1f);
			}
		}
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
		super.onUpdate(stack, world, entity, itemSlot, isSelected);

		// scope while right click
		if (entity instanceof EntityLivingBase && ((EntityLivingBase)entity).getActiveItemStack() != stack && 
				isScoped((EntityLivingBase) entity, stack)) 
			((EntityLivingBase)entity).setActiveHand(EnumHand.MAIN_HAND);
		// unset active hand while reloading
		else if (entity instanceof EntityLivingBase && ((EntityLivingBase)entity).getActiveItemStack() == stack && 
				!isScoped((EntityLivingBase) entity, stack))
			((EntityLivingBase)entity).resetActiveHand();

		if (isSelected && entity instanceof EntityLivingBase) {	
			EntityLivingBase player = (EntityLivingBase) entity;

			// venom mine
			if (!world.isRemote && hero.ability1.isSelected(player) && 
					this.canUse(player, true, EnumHand.MAIN_HAND, true)) {
				EntityWidowmakerMine mine = new EntityWidowmakerMine(world, player);
				EntityHelper.setAim(mine, player, player.rotationPitch, player.rotationYawHead, 19, 0, null, 0, 0);
				world.playSound(null, player.getPosition(), ModSoundEvents.widowmakerMineThrow, SoundCategory.PLAYERS, 1.0f, 1.0f);
				world.spawnEntity(mine);
				player.getHeldItem(EnumHand.MAIN_HAND).damageItem(1, player);
				hero.ability1.keybind.setCooldown(player, 300, false); 
				if (hero.ability1.entities.get(player) instanceof EntityWidowmakerMine && 
						hero.ability1.entities.get(player).isEntityAlive()) 
					hero.ability1.entities.get(player).isDead = true;
				hero.ability1.entities.put(player, mine);
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
					player.world.playSound(null, player.posX, player.posY, player.posZ, ModSoundEvents.widowmakerScopedShoot, SoundCategory.PLAYERS, player.world.rand.nextFloat()+0.5F, player.world.rand.nextFloat()/2+0.75f);	
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
					world.playSound(null, player.posX, player.posY, player.posZ, ModSoundEvents.widowmakerUnscopedShoot, SoundCategory.PLAYERS, world.rand.nextFloat()/2f+0.2f, world.rand.nextFloat()/2+0.75f);	
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
				(entity.getActiveItemStack() == stack || KeyBind.RMB.isKeyDown(entity)) && EnumHero.WIDOWMAKER.weapon.getCurrentAmmo(entity) > 0;
	}

	//PORT correct scope scale
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void renderScope(RenderGameOverlayEvent.Pre event) {
		EntityPlayer player = Minecraft.getMinecraft().player;
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
				double height = event.getResolution().getScaledHeight_double();
				double width = event.getResolution().getScaledWidth_double();
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

}