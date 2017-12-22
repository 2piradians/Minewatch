package twopiradians.minewatch.common.item.weapon;

import java.util.ArrayList;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumAction;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.EntityViewRenderEvent.FOVModifier;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.common.CommonProxy.EnumParticle;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.ability.EntityAnaSleepDart;
import twopiradians.minewatch.common.entity.projectile.EntityAnaBullet;
import twopiradians.minewatch.common.hero.Ability;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.util.EntityHelper;
import twopiradians.minewatch.common.util.TickHandler;
import twopiradians.minewatch.common.util.TickHandler.Handler;
import twopiradians.minewatch.common.util.TickHandler.Identifier;
import twopiradians.minewatch.packet.SPacketSimple;

public class ItemAnaRifle extends ItemMWWeapon {

	private static final ResourceLocation SCOPE = new ResourceLocation(Minewatch.MODID+":textures/gui/ana_scope.png");
	private static final ResourceLocation SCOPE_BACKGROUND = new ResourceLocation(Minewatch.MODID+":textures/gui/ana_scope_background.png");
	private static final ResourceLocation SLEEP_OVERLAY = new ResourceLocation(Minewatch.MODID+":textures/gui/ana_sleep.png");
	private static final ResourceLocation SLEEP_BACKGROUND = new ResourceLocation(Minewatch.MODID+":textures/gui/ana_sleep_background.png");

	private boolean prevScoped;
	private float unscopedSensitivity;

	public static final Handler SLEEP = new Handler(Identifier.ANA_SLEEP, true) {
		@SideOnly(Side.CLIENT)
		@Override
		public boolean onClientTick() {
			if (this.ticksLeft < this.initialTicks - 10 && this.ticksLeft > 10) {
				// sleep particles in overlay
				if (this.ticksLeft % 3 == 0 && entity == Minecraft.getMinecraft().thePlayer && player != null &&
						Minecraft.getMinecraft().gameSettings.thirdPersonView == 0) {
					Vec3d eyes = EntityHelper.getPositionEyes(player).add(player.getLookVec());
					Minewatch.proxy.spawnParticlesCustom(EnumParticle.SLEEP, player.worldObj, 
							eyes.xCoord+player.worldObj.rand.nextFloat()-0.5f, 
							eyes.yCoord+player.worldObj.rand.nextFloat()-0.5f, 
							eyes.zCoord+player.worldObj.rand.nextFloat()-0.5f, 
							0, 0.02f, 0, 0xFFFFFF, 0xACD8E5, 1f, 20, 0.5f, 0.7f, 
							(player.worldObj.rand.nextFloat()-0.5f)*0.8f, (player.worldObj.rand.nextFloat()-0.5f)*0.05f);
				}
				// sleep particles over entity
				if (this.ticksLeft % 7 == 0) 
					Minewatch.proxy.spawnParticlesCustom(EnumParticle.SLEEP, entity.worldObj, 
							entity.posX-entity.getEyeHeight()/2d, 
							entity.posY+entity.width/2d, 
							entity.posZ+entity.width/2d, 
							(entity.worldObj.rand.nextFloat()-0.5f)*0.1f, 
							0.2f*Math.max(entity.width, 1), 
							(entity.worldObj.rand.nextFloat()-0.5f)*0.1f, 0xFFFFFF, 0xACD8E5, 1f, 
							(int) ((entity.worldObj.rand.nextInt(20)+10)*Math.max(entity.width, 1)), 2, 4, 
							(entity.worldObj.rand.nextFloat()-0.5f)*0.8f, (entity.worldObj.rand.nextFloat()-0.5f)*0.05f);
				// smokey particles on entity
				Minewatch.proxy.spawnParticlesCustom(EnumParticle.CIRCLE, entity.worldObj, 
						entity.posX+(entity.worldObj.rand.nextFloat()-0.5f)*entity.height, 
						entity.posY+0.2f, 
						entity.posZ+(entity.worldObj.rand.nextFloat()-0.5f)*entity.width, 
						0, 0.1f*entity.width, 0, 0x828BA5, 0xBDCAEF, 0.5f, (int) (13*entity.width), 4, 4, 0, 0);
			}
			return super.onClientTick();
		}
	};

	public ItemAnaRifle() {
		super(30);
		this.saveEntityToNBT = true;
		this.showHealthParticles = true;
		MinecraftForge.EVENT_BUS.register(this);
		this.addPropertyOverride(new ResourceLocation("scoping"), new IItemPropertyGetter() {
			@SideOnly(Side.CLIENT)
			public float apply(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn) {
				return entityIn != null && entityIn.isHandActive() && entityIn.getActiveItemStack() == stack ? 1.0F : 0.0F;
			}
		});
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
	public void onItemLeftClick(ItemStack stack, World worldObj, EntityLivingBase player, EnumHand hand) { 
		// shoot
		if (this.canUse(player, true, hand, false)) {
			if (!worldObj.isRemote) {
				EntityAnaBullet bullet = new EntityAnaBullet(worldObj, player, hand.ordinal(),
						isAlternate(stack));
				boolean scoped = isScoped(player, stack);
				EntityHelper.setAim(bullet, player, player.rotationPitch, player.rotationYawHead, scoped ? -1f : 90f, 0,  
						scoped ? null : hand, scoped ? 10 : 9, scoped ? 0 : 0.27f);
				worldObj.spawnEntityInWorld(bullet);
				ModSoundEvents.ANA_SHOOT.playSound(player, worldObj.rand.nextFloat()+0.5F, worldObj.rand.nextFloat()/2+0.75f);
				this.subtractFromCurrentAmmo(player, 1, hand);
				this.setCooldown(player, 20);
				if (worldObj.rand.nextInt(10) == 0)
					player.getHeldItem(hand).damageItem(1, player);
			}
			player.stopActiveHand();
		}
	}

	@Override
	public void onUpdate(ItemStack stack, World worldObj, Entity entity, int itemSlot, boolean isSelected) {
		super.onUpdate(stack, worldObj, entity, itemSlot, isSelected);

		if (isSelected && entity instanceof EntityLivingBase) {	
			EntityLivingBase player = (EntityLivingBase) entity;

			// sleep dart
			if (!worldObj.isRemote && hero.ability2.isSelected(player) && 
					this.canUse(player, true, EnumHand.MAIN_HAND, true)) {
				EntityAnaSleepDart dart = new EntityAnaSleepDart(worldObj, player, EnumHand.MAIN_HAND.ordinal());
				EntityHelper.setAim(dart, player, player.rotationPitch, player.rotationYawHead, 60, 0F, EnumHand.MAIN_HAND, 9, 0.27f);
				worldObj.spawnEntityInWorld(dart);
				ModSoundEvents.ANA_SLEEP_SHOOT.playSound(player, worldObj.rand.nextFloat()+0.5F, worldObj.rand.nextFloat()/2+0.75f);
				if (player instanceof EntityPlayerMP)
					Minewatch.network.sendTo(new SPacketSimple(21, false, (EntityPlayer) player, 10, 0, 0), (EntityPlayerMP) player);
				TickHandler.register(false, Ability.ABILITY_USING.setEntity(player).setTicks(10).setAbility(EnumHero.ANA.ability2));
				this.setCooldown(player, 20);
				if (worldObj.rand.nextInt(10) == 0)
					player.getHeldItem(EnumHand.MAIN_HAND).damageItem(1, player);
				hero.ability2.keybind.setCooldown(player, 240, false); 
			}
		}

		// scope while right click
		if (entity instanceof EntityLivingBase && ((EntityLivingBase)entity).getActiveItemStack() != stack && 
				isScoped((EntityLivingBase) entity, stack)) 
			((EntityLivingBase)entity).setActiveHand(EnumHand.MAIN_HAND);
		// unset active hand while reloading
		else if (entity instanceof EntityLivingBase && ((EntityLivingBase)entity).getActiveItemStack() == stack && 
				!isScoped((EntityLivingBase) entity, stack))
			((EntityLivingBase)entity).resetActiveHand();
	}

	@SubscribeEvent
	public void wakeUpSleeping(LivingHurtEvent event) {
		Handler handler = TickHandler.getHandler(event.getEntity(), Identifier.ANA_SLEEP);
		if (handler != null && (event.getSource().getSourceOfDamage() == null || 
				!(event.getSource().getSourceOfDamage() instanceof EntityAnaSleepDart))) {
			for (Identifier identifier : new Identifier[] {Identifier.ANA_SLEEP, 
					Identifier.PREVENT_INPUT, Identifier.PREVENT_MOVEMENT, Identifier.PREVENT_ROTATION}) {
				handler = TickHandler.getHandler(event.getEntity(), identifier);
				if (handler != null && handler.ticksLeft > 10)
					handler.ticksLeft = 10;
			}
			Minewatch.network.sendToAll(new SPacketSimple(11, event.getEntity(), false));
			ModSoundEvents.ANA_SLEEP_HIT.stopSound(event.getEntity().worldObj);
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void rotateSleeping(RenderLivingEvent.Pre<EntityLivingBase> event) {
		Handler handler = TickHandler.getHandler(event.getEntity(), Identifier.ANA_SLEEP);
		if (handler != null && event.getEntity().getHealth() > 0) {
			GlStateManager.pushMatrix();
			float rotation = handler.ticksLeft > 9 ? Math.min((handler.initialTicks-handler.ticksLeft)*4f, 90) :
				handler.ticksLeft*10f;
			float percent = rotation/90;
			GlStateManager.translate(event.getX()+event.getEntity().height/2*percent, event.getY()+event.getEntity().width/2*percent, 0);
			GlStateManager.rotate(rotation, 0, 0, 1);
			GlStateManager.translate(-event.getX(), -event.getY(), 0);
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void rotateSleeping(RenderLivingEvent.Post<EntityLivingBase> event) {
		if (TickHandler.hasHandler(event.getEntity(), Identifier.ANA_SLEEP) && event.getEntity().getHealth() > 0) 
			GlStateManager.popMatrix();
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void changeFOV(FOVModifier event) {
		if (event.getEntity() instanceof EntityPlayer && 
				isScoped((EntityPlayer) event.getEntity(), ((EntityPlayer)event.getEntity()).getHeldItemMainhand()) &&
				Minecraft.getMinecraft().gameSettings.thirdPersonView == 0) {
			event.setFOV(20f);
		}
	}

	/**Is this player scoping with the stack*/
	public static boolean isScoped(EntityLivingBase player, ItemStack stack) {
		return player != null && player.getHeldItemMainhand() != null && 
				player.getHeldItemMainhand().getItem() == EnumHero.ANA.weapon &&
				(player.getActiveItemStack() == stack || KeyBind.RMB.isKeyDown(player)) && EnumHero.ANA.weapon.getCurrentAmmo(player) > 0 &&
				!TickHandler.hasHandler(player, Identifier.ABILITY_USING) && !KeyBind.JUMP.isKeyDown(player);
	}

	//PORT correct scope scale
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void renderScope(RenderGameOverlayEvent.Pre event) {
		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
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

			if (Minecraft.getMinecraft().gameSettings.thirdPersonView == 0) {
				double height = event.getResolution().getScaledHeight_double();
				double width = event.getResolution().getScaledWidth_double();
				int imageSize = 256;

				Handler handler = TickHandler.getHandler(player, Identifier.ANA_SLEEP);
				if (handler != null) {
					// sleep overlay
					GlStateManager.pushMatrix();
					GlStateManager.enableBlend();
					GlStateManager.color(1, 1, 1, 0.9f);
					double scale = 0.5f;
					GlStateManager.scale(scale, scale, 1);
					Minecraft.getMinecraft().getTextureManager().bindTexture(SLEEP_OVERLAY);
					GuiUtils.drawTexturedModalRect((int) (width/2/scale-imageSize/2), (int) (height/3/scale-imageSize/2), 0, 0, imageSize, imageSize, 0);
					// background 
					GlStateManager.color(1, 1, 1, 1f);
					scale = Math.max(height/imageSize, width/imageSize)*2;
					GlStateManager.scale(scale, scale, 1);
					Minecraft.getMinecraft().getTextureManager().bindTexture(SLEEP_BACKGROUND);
					GuiUtils.drawTexturedModalRect((int) ((width/scale-imageSize/2)), (int) ((height/scale-imageSize/2)), 0, 0, imageSize, imageSize, 0);
					GlStateManager.popMatrix();
				}
				// scope
				if (isScoped(player, player.getHeldItemMainhand())) {
					GlStateManager.pushMatrix();
					GlStateManager.enableBlend();
					// scope
					GlStateManager.color(1, 1, 1, 0.6f);
					double scale = event.getResolution().getScaleFactor();
					GlStateManager.scale(scale, scale, 1);
					scale = 2;
					GlStateManager.scale(scale, scale, 1);
					Minecraft.getMinecraft().getTextureManager().bindTexture(SCOPE);
					GuiUtils.drawTexturedModalRect((int) (width/2/scale-imageSize/2), (int) (height/2/scale-imageSize/2), 0, 0, imageSize, imageSize, 0);
					GlStateManager.scale(1/scale, 1/scale, 1);
					// background
					scale = Math.max(height/imageSize, width/imageSize);
					GlStateManager.scale(width/256d, height/256d, 1);
					Minecraft.getMinecraft().getTextureManager().bindTexture(SCOPE_BACKGROUND);
					GuiUtils.drawTexturedModalRect(0, 0, 0, 0, imageSize, imageSize, 0);
					GlStateManager.popMatrix();
				}
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