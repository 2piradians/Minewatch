package twopiradians.minewatch.common.item.weapon;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.entity.projectile.EntityMoiraHealEnergy;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.util.EntityHelper;

public class ItemMoiraWeapon extends ItemMWWeapon {

	// TODO make 1st person left hand from 1st person right hand in json
	// TODO there's a little gap in the model - see if you can cover the gap by merging the edges together or something and fixing the uv to look nice
	// https://gyazo.com/6fe95e2f171bf0f5bccd3079566c6afc ^ (not super noticeable in-game, but would be nice to fix)
	
	public ItemMoiraWeapon() {
		super(0);
		this.maxCharge = 180;
		this.rechargeRate = 1/5f;
		this.hasOffhand = true;
		this.showHealthParticles = true;
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public void onItemLeftClick(ItemStack stack, World world, EntityLivingBase player, EnumHand hand) { 		
		if (hand == EnumHand.OFF_HAND && this.canUse(player, true, hand, false) && this.getCurrentCharge(player) >= 1) {
			this.subtractFromCurrentCharge(player, 1, player.ticksExisted % 10 == 0);
			if (!world.isRemote) {
				EntityMoiraHealEnergy energy = new EntityMoiraHealEnergy(world, player, hand.ordinal());
				EntityHelper.setAim(energy, player, player.rotationPitch, player.rotationYawHead, 10, 0,  
						hand, 9, 0.27f);
				world.spawnEntity(energy);
				//ModSoundEvents.ANA_SHOOT.playSound(player, world.rand.nextFloat()+0.5F, world.rand.nextFloat()/2+0.75f);
				if (world.rand.nextInt(100) == 0)
					player.getHeldItem(hand).damageItem(1, player);
			}
		}
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isSelected) {
		super.onUpdate(stack, world, entity, slot, isSelected);
		
		if (isSelected && entity instanceof EntityLivingBase && ((EntityLivingBase) entity).getHeldItemMainhand() == stack &&
				((EntityLivingBase)entity).getActiveItemStack() != stack) {	
			EntityLivingBase player = (EntityLivingBase) entity;

		}
	}	

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityLivingBase player, EnumHand hand) {
		return new ActionResult<ItemStack>(EnumActionResult.PASS, player.getHeldItem(hand));
	}
	
	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void renderArms(RenderSpecificHandEvent event) {
		// render arms while holding weapons - modified from ItemRenderer#renderArmFirstPerson
		if (event.getItemStack() != null && event.getItemStack().getItem() == this) {
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