package twopiradians.minewatch.common.item.weapon;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.EntityViewRenderEvent.FOVModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.item.armor.ModArmor;

public class ModWeapon extends Item
{
	/**Used to uniformly scale damage for all weapons/abilities*/
	public static final float DAMAGE_SCALE = 10f;

	/**Cooldown in ticks for MC cooldown and nbt cooldown (if hasOffhand)*/
	protected int cooldown;
	/**ArmorMaterial that determines which set this belongs to*/
	protected ArmorMaterial material;
	/**Will give shooting hand MC cooldown = cooldown/2 if true*/
	protected boolean hasOffhand;
	protected ResourceLocation scope;

	protected ModWeapon() {
		if (scope != null)
			MinecraftForge.EVENT_BUS.register(this);
	}

	/**Called on server when right click is held and cooldown is not active*/
	protected void onShoot(World worldIn, EntityPlayer playerIn, EnumHand hand) {}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand) {
		// check that item does not have MC cooldown (and nbt cooldown if it hasOffhand)
		if (cooldown >= 0 && playerIn != null && playerIn.getHeldItem(hand) != null 
				&& !playerIn.getCooldownTracker().hasCooldown(playerIn.getHeldItem(hand).getItem()) 
				&& (!hasOffhand || (playerIn.getHeldItem(hand).hasTagCompound() 
						&& playerIn.getHeldItem(hand).getTagCompound().getInteger("cooldown") <= 0))) {	
			if (!worldIn.isRemote) {
				onShoot(worldIn, playerIn, hand);
				// set MC cooldown/2 and nbt cooldown if hasOffhand, otherwise just set MC cooldown
				if (playerIn.getHeldItem(getInactiveHand(playerIn)) != null 
						&& playerIn.getHeldItem(getInactiveHand(playerIn)).getItem() != Items.AIR
						&& playerIn.getHeldItem(hand).getItem() != playerIn.getHeldItem(getInactiveHand(playerIn)).getItem())
					playerIn.getCooldownTracker().setCooldown(playerIn.getHeldItem(getInactiveHand(playerIn)).getItem(), cooldown+1);
				if (hasOffhand) {
					playerIn.getCooldownTracker().setCooldown(playerIn.getHeldItem(hand).getItem(), cooldown/2);
					playerIn.getHeldItem(hand).getTagCompound().setInteger("cooldown", cooldown);
				}
				else 
					playerIn.getCooldownTracker().setCooldown(playerIn.getHeldItem(hand).getItem(), cooldown);
				// only damage item if 
				if (!ModArmor.isSet(playerIn, material))
					playerIn.getHeldItem(hand).damageItem(1, playerIn);
			}
			return new ActionResult(EnumActionResult.SUCCESS, playerIn.getHeldItem(hand));
		}
		else
			return new ActionResult(EnumActionResult.PASS, playerIn.getHeldItem(hand));	
	}

	@Override
	public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {	
		if (cooldown > 0 && hasOffhand && !worldIn.isRemote) {
			if (!stack.hasTagCompound())
				stack.setTagCompound(new NBTTagCompound());
			int cooldown = stack.getTagCompound().getInteger("cooldown");
			if (stack.getTagCompound().getInteger("cooldown") > 0)
				stack.getTagCompound().setInteger("cooldown", --cooldown);
		}
		if (entityIn != null && entityIn instanceof AbstractClientPlayer && entityIn.isSneaking()
				&& ((EntityPlayer)entityIn).getHeldItemMainhand().getItem() instanceof ItemAnaRifle) {
			AbstractClientPlayer player = (AbstractClientPlayer)entityIn;
			net.minecraftforge.client.ForgeHooksClient.getOffsetFOV(player, 10);
		}
	}

	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return oldStack.getItem() != newStack.getItem();
	}

	public int getItemStackLimit(ItemStack stack) {
		return 1;
	}

	public int getItemEnchantability() {
		return 0;
	}

	public EnumHand getInactiveHand(EntityPlayer player) {
		if (player.getActiveHand() == EnumHand.MAIN_HAND)
			return EnumHand.OFF_HAND;
		else
			return EnumHand.MAIN_HAND;
	}


	@SubscribeEvent
	public void onEvent(FOVModifier event) {
		if (event.getEntity() != null && event.getEntity() instanceof EntityPlayer && event.getEntity().isSneaking()
				&& ((EntityPlayer)event.getEntity()).getHeldItemMainhand() != null
				&& ((EntityPlayer)event.getEntity()).getHeldItemMainhand().getItem() instanceof ItemAnaRifle) {
			event.setFOV(20f);
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onEvent(RenderGameOverlayEvent.Pre event) {
		EntityPlayer player = Minecraft.getMinecraft().player;
		if (scope != null && player != null && player.getHeldItemMainhand() != null && player.isSneaking()
				&& player.getHeldItemMainhand().getItem() instanceof ItemAnaRifle) {
			int height = event.getResolution().getScaledHeight();
			int width = event.getResolution().getScaledWidth();
			int imageSize = 256;
			GlStateManager.pushMatrix();
			Minecraft.getMinecraft().getTextureManager().bindTexture(scope);
			System.out.println(scope);
			GlStateManager.color(1, 1, 1, 0.165f);
			GlStateManager.enableBlend();
			GlStateManager.enableDepth();
			GuiUtils.drawTexturedModalRect(width/2-imageSize/2, height/2-imageSize/2, 0, 0, imageSize, imageSize, 0);
			GlStateManager.disableDepth();
			GlStateManager.popMatrix();
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onEvent(RenderPlayerEvent.Pre event) {
		/*//TODO Test this to make sure other clients see raised arms
		EntityPlayer player = event.getEntityPlayer();
		ItemStack itemMain = player.getHeldItemMainhand(); 
		ItemStack itemOff = player.getHeldItemOffhand();  
		RenderPlayer rp = (RenderPlayer)event.getRenderer();
		if (Minecraft.getMinecraft().gameSettings.mainHand.equals(EnumHandSide.RIGHT)) {
			if (itemMain != null && itemMain.getItem() == ModItems.reaper_shotgun) {
//				player.swingingHand = EnumHand.MAIN_HAND;
//				player.swingProgress = 2.0f;
				rp.getMainModel().rightArmPose = ModelBiped.ArmPose.BOW_AND_ARROW;
			}
			if (itemOff != null && itemOff.getItem() == ModItems.reaper_shotgun)
				System.out.println("Off hand shotgun.");

		}
		else {
			System.out.println("Left hand main.");
		}*/
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onEvent(RenderPlayerEvent.Post event) {
		/*//TODO Test this to make sure other clients see raised arms
		EntityPlayer player = event.getEntityPlayer();
		ItemStack itemMain = player.getHeldItemMainhand(); 
		ItemStack itemOff = player.getHeldItemOffhand();  
		RenderPlayer rp = (RenderPlayer)event.getRenderer();
		if (Minecraft.getMinecraft().gameSettings.mainHand.equals(EnumHandSide.RIGHT)) {
			if (itemMain != null && itemMain.getItem() == ModItems.reaper_shotgun) {
				rp.getMainModel().bipedRightArm.rotateAngleX = 0f;
			}
			if (itemOff != null && itemOff.getItem() == ModItems.reaper_shotgun)
				System.out.println("Off hand shotgun.");

		}
		else {
			System.out.println("Left hand main.");
		}*/
	}
}
