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
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.client.event.EntityViewRenderEvent.FOVModifier;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.EntityAnaBullet;
import twopiradians.minewatch.common.sound.ModSoundEvents;

public class ItemAnaRifle extends ItemMWWeapon {

	private static final ResourceLocation SCOPE = new ResourceLocation(Minewatch.MODID + ":textures/gui/ana_scope.png");
	private static final ResourceLocation SCOPE_BACKGROUND = new ResourceLocation(Minewatch.MODID + ":textures/gui/ana_scope_background.png");

	public ItemAnaRifle() {
		super(30);
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
		if (this.canUse(player, true)) {
			if (!world.isRemote) {
				EntityAnaBullet bullet = new EntityAnaBullet(world, player, 
						hero.playersUsingAlt.containsKey(player.getPersistentID()) && 
						hero.playersUsingAlt.get(player.getPersistentID()));
				bullet.setAim(player, player.rotationPitch, player.rotationYaw, 10.0F, 0.1F, 0F, null, true);
				world.spawnEntityInWorld(bullet);
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

		// health particles
		if (isSelected && entity instanceof EntityPlayer && this.canUse((EntityPlayer) entity, false) &&
				world.isRemote && entity.ticksExisted % 5 == 0) {
			AxisAlignedBB aabb = entity.getEntityBoundingBox().expandXyz(30);
			List<Entity> list = entity.worldObj.getEntitiesWithinAABBExcludingEntity(entity, aabb);
			for (Entity entity2 : list) 
				if (entity2 instanceof EntityLivingBase 
						&& ((EntityLivingBase)entity2).getHealth() < ((EntityLivingBase)entity2).getMaxHealth()) 
					Minewatch.proxy.spawnParticlesAnaHealth((EntityLivingBase) entity2);
		}

		// scope while right click
		if (entity instanceof EntityPlayer && ((EntityPlayer)entity).getActiveItemStack() != stack && 
				Minewatch.keys.rmb((EntityPlayer)entity) && isSelected && this.getCurrentAmmo((EntityPlayer) entity) > 0) 
			((EntityPlayer)entity).setActiveHand(EnumHand.MAIN_HAND);

		// set player in nbt for model changer (in ClientProxy) to reference
		if (entity instanceof EntityPlayer && !entity.worldObj.isRemote && stack != null && stack.getItem() instanceof ItemAnaRifle) {
			if (!stack.hasTagCompound())
				stack.setTagCompound(new NBTTagCompound());
			NBTTagCompound nbt = stack.getTagCompound();
			if (!nbt.hasKey("playerLeast") || nbt.getLong("playerLeast") != (entity.getPersistentID().getLeastSignificantBits())) {
				nbt.setUniqueId("player", entity.getPersistentID());
				stack.setTagCompound(nbt);
			}
		}
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
		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
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
