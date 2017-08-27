package twopiradians.minewatch.common.item.weapon;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.EntityViewRenderEvent.FOVModifier;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.EntityMWThrowable;
import twopiradians.minewatch.common.entity.EntityWidowmakerBullet;
import twopiradians.minewatch.common.item.armor.ItemMWArmor;
import twopiradians.minewatch.common.sound.ModSoundEvents;

public class ItemWidowmakerRifle extends ItemMWWeapon {

	private static final ResourceLocation SCOPE = new ResourceLocation(Minewatch.MODID + ":textures/gui/widowmaker_scope.png");
	private static final ResourceLocation SCOPE_BACKGROUND = new ResourceLocation(Minewatch.MODID + ":textures/gui/widowmaker_scope_background.png");

	public ItemWidowmakerRifle() {
		super(30);
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
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		if (this.canUse(player, true)) {
			player.setActiveHand(hand);
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
		}
		else
			return new ActionResult<ItemStack>(EnumActionResult.PASS, player.getHeldItem(hand));
	}

	@Override
	public void onItemLeftClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand) { 
		if (this.canUse(player, true)) {
			// scoped
			if (Minewatch.keys.rmb(player) && player.getActiveItemStack() == stack) {
				if (!player.world.isRemote) {
					System.out.println("server: "+(this.getMaxItemUseDuration(stack)-player.getItemInUseCount()));
					EntityWidowmakerBullet bullet = new EntityWidowmakerBullet(player.world, player, true, 13);
					bullet.setAim(player, player.rotationPitch, player.rotationYaw, 5.0F, 0F, 0F, null, true);
					player.world.spawnEntity(bullet);
					player.world.playSound(null, player.posX, player.posY, player.posZ, ModSoundEvents.widowmakerScopedShoot, SoundCategory.PLAYERS, player.world.rand.nextFloat()+0.5F, player.world.rand.nextFloat()/2+0.75f);	
					if (!player.getCooldownTracker().hasCooldown(this))
						player.getCooldownTracker().setCooldown(this, 10);
					this.subtractFromCurrentAmmo(player, 3);
					if (player.world.rand.nextInt(25) == 0 && !(ItemMWArmor.SetManager.playersWearingSets.get(player.getPersistentID()) == hero))
						stack.damageItem(1, player);
					player.stopActiveHand();
				}
				else {
					System.out.println("client: "+(this.getMaxItemUseDuration(stack)-((EntityPlayer)player).getItemInUseCount()));
					Vec3d vec = EntityMWThrowable.getShootingPos(player, player.rotationPitch, player.rotationYaw, EnumHand.MAIN_HAND);
					Minewatch.proxy.spawnParticlesSpark(player.world, vec.xCoord, vec.yCoord, vec.zCoord, 0xF9394F, 0x5A575A, 5, 1);
				}
			}
			// unscoped
			else if (!Minewatch.keys.rmb(player)) {
				if (!world.isRemote) {
					EntityWidowmakerBullet bullet = new EntityWidowmakerBullet(world, player, false, 13);
					bullet.setAim(player, player.rotationPitch, player.rotationYaw, 3.0F, 1F, 1F, hand, true);
					world.spawnEntity(bullet);
					world.playSound(null, player.posX, player.posY, player.posZ, ModSoundEvents.widowmakerUnscopedShoot, SoundCategory.PLAYERS, world.rand.nextFloat()+0.5F, world.rand.nextFloat()/2+0.75f);	
					this.subtractFromCurrentAmmo(player, 1);
					if (world.rand.nextInt(25) == 0 && !(ItemMWArmor.SetManager.playersWearingSets.get(player.getPersistentID()) == hero))
						player.getHeldItem(hand).damageItem(1, player);
				}
				else {
					Vec3d vec = EntityMWThrowable.getShootingPos(player, player.rotationPitch, player.rotationYaw, hand);
					Minewatch.proxy.spawnParticlesSpark(world, vec.xCoord, vec.yCoord, vec.zCoord, 0xF9394F, 0x5A575A, 5, 1);
				}
			}
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void changeFOV(FOVModifier event) {
		if (event.getEntity() instanceof EntityPlayer && (((EntityPlayer)event.getEntity()).getHeldItemMainhand() != null && 
				((EntityPlayer)event.getEntity()).getHeldItemMainhand().getItem() == this && 
				Minecraft.getMinecraft().gameSettings.thirdPersonView == 0) && 
				Minewatch.keys.rmb((EntityPlayer) event.getEntity())) {
			event.setFOV(20f);
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void renderScope(RenderGameOverlayEvent.Post event) {
		EntityPlayer player = Minecraft.getMinecraft().player;
		if (player != null && player.getHeldItemMainhand() != null && player.getHeldItemMainhand().getItem() == this &&
				/*Minewatch.keys.rmb(player) &&*/ Minecraft.getMinecraft().gameSettings.thirdPersonView == 0) {
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

			// power
			GlStateManager.pushMatrix();
			GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			int power = player.getActiveItemStack() == player.getHeldItemMainhand() ? (int) Math.min((this.getMaxItemUseDuration(player.getHeldItemMainhand())-player.getItemInUseCount())/15d*100d, 100) : 0;
			int powerWidth = Minecraft.getMinecraft().fontRendererObj.getStringWidth(power+"%");
			Minecraft.getMinecraft().fontRendererObj.drawString(power+"%", (int) width/2-powerWidth/2, (int) height/2+40, 0);
			GlStateManager.popMatrix();

		}
	}
}
