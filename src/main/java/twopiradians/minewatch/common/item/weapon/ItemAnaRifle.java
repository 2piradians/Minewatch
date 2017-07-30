package twopiradians.minewatch.common.item.weapon;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.client.event.EntityViewRenderEvent.FOVModifier;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.EntityAnaBullet;
import twopiradians.minewatch.common.item.armor.ItemMWArmor;
import twopiradians.minewatch.common.sound.ModSoundEvents;

public class ItemAnaRifle extends ItemMWWeapon 
{
	private static final ResourceLocation SCOPE = new ResourceLocation(Minewatch.MODID + ":textures/gui/ana_scope.png");
	private static final ResourceLocation SCOPE_BACKGROUND = new ResourceLocation(Minewatch.MODID + ":textures/gui/ana_scope_background.png");

	public ItemAnaRifle() {
		super(30);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public void onItemLeftClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand) { 
		if (!world.isRemote && this.canUse(player, true)) {
			EntityAnaBullet bullet = new EntityAnaBullet(world, player, 
					hero.playersUsingAlt.containsKey(player.getPersistentID()) && 
					hero.playersUsingAlt.get(player.getPersistentID()));
			bullet.setAim(player, player.rotationPitch, player.rotationYaw, 5.0F, 0.3F);
			world.spawnEntity(bullet);
			world.playSound(null, player.posX, player.posY, player.posZ, 
					ModSoundEvents.reaperShotgun, SoundCategory.PLAYERS, // TODO change sound
					world.rand.nextFloat()+0.5F, world.rand.nextFloat()/2+0.75f);	
			this.subtractFromCurrentAmmo(player, 1);
			if (!player.getCooldownTracker().hasCooldown(this))
				player.getCooldownTracker().setCooldown(this, 20);
			if (world.rand.nextInt(25) == 0 && !(ItemMWArmor.SetManager.playersWearingSets.get(player.getPersistentID()) == hero))
				player.getHeldItem(hand).damageItem(1, player);
		}
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
		super.onUpdate(stack, world, entity, itemSlot, isSelected);

		if (isSelected && entity != null && entity instanceof EntityPlayer && this.canUse((EntityPlayer) entity, false)) {
			EntityPlayer player = (EntityPlayer)entity;

			// sleep dart
			if (isSelected && Minewatch.keys.ability1(player)) {

			}

			// biotic grenade
			if (isSelected && Minewatch.keys.ability2(player)) {

			}

			// health particles
			if (world.isRemote && entity.ticksExisted % 10 == 0) {
				AxisAlignedBB aabb = entity.getEntityBoundingBox().expandXyz(30);
				List<Entity> list = entity.world.getEntitiesWithinAABBExcludingEntity(entity, aabb);
				for (Entity entity2 : list) 
					if (entity2 instanceof EntityLivingBase 
							&& ((EntityLivingBase)entity2).getHealth() < ((EntityLivingBase)entity2).getMaxHealth()) 
						Minewatch.proxy.spawnParticlesHealthPlus((EntityLivingBase) entity2);
			}
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void changeFOV(FOVModifier event) {
		if (event.getEntity() instanceof EntityPlayer && (((EntityPlayer)event.getEntity()).getHeldItemMainhand() != null 
				&& ((EntityPlayer)event.getEntity()).getHeldItemMainhand().getItem() == this) && 
				Minewatch.keys.rmb((EntityPlayer) event.getEntity())) {
			event.setFOV(20f);
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void renderScope(RenderGameOverlayEvent.Post event) {
		EntityPlayer player = Minecraft.getMinecraft().player;
		if (player != null && player.getHeldItemMainhand() != null && player.getHeldItemMainhand().getItem() == this &&
				Minewatch.keys.rmb(player)) {
			double height = event.getResolution().getScaledHeight_double();
			double width = event.getResolution().getScaledWidth_double();
			int imageSize = 256;
			GlStateManager.pushMatrix();
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			// scope
			GlStateManager.color(1, 1, 1, 0.22f);
			Minecraft.getMinecraft().getTextureManager().bindTexture(SCOPE);
			GuiUtils.drawTexturedModalRect((int) (width/2-imageSize/2), (int) (height/2-imageSize/2), 0, 0, 256, 256, 0);
			// background
			GlStateManager.color(1, 1, 1, 0.1f);
			GlStateManager.scale(width/imageSize, height/imageSize, 1);
			Minecraft.getMinecraft().getTextureManager().bindTexture(SCOPE_BACKGROUND);
			GuiUtils.drawTexturedModalRect(0, 0, 0, 0, 256, 256, 0);
			GlStateManager.popMatrix();
		}
	}
}
