package twopiradians.minewatch.common.item.weapon;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
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
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.EntityAnaBullet;
import twopiradians.minewatch.common.entity.EntityAnaSleepDart;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.tickhandler.TickHandler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Handler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Identifier;
import twopiradians.minewatch.packet.SPacketSimple;

public class ItemAnaRifle extends ItemMWWeapon {

	private static final ResourceLocation SCOPE = new ResourceLocation(Minewatch.MODID + ":textures/gui/ana_scope.png");
	private static final ResourceLocation SCOPE_BACKGROUND = new ResourceLocation(Minewatch.MODID + ":textures/gui/ana_scope_background.png");

	public static final Handler SLEEP = new Handler(Identifier.ANA_SLEEP, true) {

	};

	public ItemAnaRifle() {
		super(30);
		this.savePlayerToNBT = true;
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
	public void onItemLeftClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand) { 
		// shoot
		if (this.canUse(player, true, hand)) {
			if (!world.isRemote) {
				EntityAnaBullet bullet = new EntityAnaBullet(world, player, 
						hero.playersUsingAlt.containsKey(player.getPersistentID()) && 
						hero.playersUsingAlt.get(player.getPersistentID()));
				bullet.setAim(player, player.rotationPitch, player.rotationYaw, 10.0F, 0.1F, 0F, null, true);
				world.spawnEntity(bullet);
				world.playSound(null, player.posX, player.posY, player.posZ, 
						ModSoundEvents.anaShoot, SoundCategory.PLAYERS, 
						world.rand.nextFloat()+0.5F, world.rand.nextFloat()/2+0.75f);	
				this.subtractFromCurrentAmmo(player, 1, hand);
				if (!player.getCooldownTracker().hasCooldown(this))
					player.getCooldownTracker().setCooldown(this, 20);
				if (world.rand.nextInt(10) == 0)
					player.getHeldItem(hand).damageItem(1, player);
			}
			player.stopActiveHand();
		}
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
		super.onUpdate(stack, world, entity, itemSlot, isSelected);

		if (isSelected && entity instanceof EntityPlayer) {	
			EntityPlayer player = (EntityPlayer) entity;

			// sleep dart
			if (!world.isRemote && hero.ability2.isSelected(player) && 
					this.canUse(player, true, EnumHand.MAIN_HAND)) {
				hero.ability2.keybind.setCooldown(player, 24, false); //TODO 240
				EntityAnaSleepDart dart = new EntityAnaSleepDart(world, player);
				dart.setAim(player, player.rotationPitch, player.rotationYaw, 10.0F, 0.1F, 0F, null, true);
				world.spawnEntity(dart);
				world.playSound(null, player.posX, player.posY, player.posZ, //TODO add sound
						ModSoundEvents.anaShoot, SoundCategory.PLAYERS, 
						world.rand.nextFloat()+0.5F, world.rand.nextFloat()/2+0.75f);	
				if (!player.getCooldownTracker().hasCooldown(this))
					player.getCooldownTracker().setCooldown(this, 20);
				if (world.rand.nextInt(10) == 0)
					player.getHeldItem(EnumHand.MAIN_HAND).damageItem(1, player);
			}

			// health particles
			if (world.isRemote && entity.ticksExisted % 5 == 0 && this.canUse(player, false, EnumHand.MAIN_HAND)) {
				AxisAlignedBB aabb = entity.getEntityBoundingBox().expandXyz(30);
				List<Entity> list = entity.world.getEntitiesWithinAABBExcludingEntity(entity, aabb);
				for (Entity entity2 : list) 
					if (entity2 instanceof EntityLivingBase 
							&& ((EntityLivingBase)entity2).getHealth() < ((EntityLivingBase)entity2).getMaxHealth()) 
						Minewatch.proxy.spawnParticlesAnaHealth((EntityLivingBase) entity2);
			}
		}

		// scope while right click
		if (entity instanceof EntityPlayer && ((EntityPlayer)entity).getActiveItemStack() != stack && 
				Minewatch.keys.rmb((EntityPlayer)entity) && isSelected && this.getCurrentAmmo((EntityPlayer) entity) > 0) 
			((EntityPlayer)entity).setActiveHand(EnumHand.MAIN_HAND);
	}

	@SubscribeEvent
	public void wakeUpSleeping(LivingHurtEvent event) {
		Handler handler = TickHandler.getHandler(event.getEntity(), Identifier.ANA_SLEEP);
		if (handler != null && (event.getSource().getSourceOfDamage() == null || 
				!(event.getSource().getSourceOfDamage() instanceof EntityAnaSleepDart))) {
			System.out.println("wake up");
			TickHandler.unregister(event.getEntity().world.isRemote, handler,
					TickHandler.getHandler(event.getEntity(), Identifier.PREVENT_INPUT),
					TickHandler.getHandler(event.getEntity(), Identifier.PREVENT_MOVEMENT),
					TickHandler.getHandler(event.getEntity(), Identifier.PREVENT_ROTATION));
			Minewatch.network.sendToAll(new SPacketSimple(11, event.getEntity(), false));
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void colorEntities(RenderLivingEvent.Pre<EntityLivingBase> event) {
		Handler handler = TickHandler.getHandler(event.getEntity(), Identifier.ANA_SLEEP);
		if (handler != null) {
			GlStateManager.pushMatrix();
			float rotation = 0;//handler.ticksLeft*4f % 180;
			float percent = rotation/180;
			GlStateManager.rotate(180, 0, 0, 0);// play around with 0, 90, and 180
			GlStateManager.rotate(rotation, 0, 0, 1);
			GlStateManager.rotate(180, 0, 1, 0);
			GlStateManager.translate(event.getX()*-2f * percent + event.getY() * (1f-percent), 
					event.getY()*-2f * (1f-percent) + event.getX() * (1f-percent), 
					0 * (1f-percent));
			/*GlStateManager.translate(event.getX()*-2f * percent, 
					-event.getY()*2f * (1f-percent),  works for 0 and 180
					0* (1f-percent));*/
			/*System.out.println("rotation: "+rotation+", percent: "+percent);
			System.out.println(event.getX());*/
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void colorEntities(RenderLivingEvent.Post<EntityLivingBase> event) {
		if (TickHandler.hasHandler(event.getEntity(), Identifier.ANA_SLEEP)) 
			GlStateManager.popMatrix();
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void changeFOV(FOVModifier event) {
		if (event.getEntity() instanceof EntityPlayer && (((EntityPlayer)event.getEntity()).getHeldItemMainhand() != null 
				&& ((EntityPlayer)event.getEntity()).getHeldItemMainhand().getItem() == this && 
				Minecraft.getMinecraft().gameSettings.thirdPersonView == 0) && 
				Minewatch.keys.rmb((EntityPlayer) event.getEntity()) && 
				this.getCurrentAmmo((EntityPlayer) event.getEntity()) > 0) {
			event.setFOV(20f);
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void renderScope(RenderGameOverlayEvent.Pre event) {
		EntityPlayer player = Minecraft.getMinecraft().player;
		if (event.getType() == ElementType.ALL && player != null && player.getHeldItemMainhand() != null && player.getHeldItemMainhand().getItem() == this &&
				Minewatch.keys.rmb(player) && Minecraft.getMinecraft().gameSettings.thirdPersonView == 0 && 
				this.getCurrentAmmo((EntityPlayer) player) > 0) {
			double height = event.getResolution().getScaledHeight_double();
			double width = event.getResolution().getScaledWidth_double();
			int imageSize = 256;

			GlStateManager.pushMatrix();
			GlStateManager.enableBlend();
			// scope
			GlStateManager.color(1, 1, 1, 0.6f);
			double scale = 2;
			GlStateManager.scale(scale, scale, 1);
			Minecraft.getMinecraft().getTextureManager().bindTexture(SCOPE);
			GuiUtils.drawTexturedModalRect((int) (width/2/scale-imageSize/2), (int) (height/2/scale-imageSize/2), 0, 0, imageSize, imageSize, 0);
			GlStateManager.scale(1/scale, 1/scale, 1);
			// background
			GlStateManager.color(1, 1, 1, 1f);
			scale = Math.max(height/imageSize, width/imageSize);
			GlStateManager.scale(scale, scale, 1);
			Minecraft.getMinecraft().getTextureManager().bindTexture(SCOPE_BACKGROUND);
			GuiUtils.drawTexturedModalRect((int) ((width/2/scale-imageSize/2)), (int) ((height/2/scale-imageSize/2)), 0, 0, imageSize, imageSize, 0);
			GlStateManager.popMatrix();
		}
	}
}
