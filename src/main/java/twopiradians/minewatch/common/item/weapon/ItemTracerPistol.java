package twopiradians.minewatch.common.item.weapon;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.client.key.Keys;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.config.Config;
import twopiradians.minewatch.common.entity.hero.EntityHero;
import twopiradians.minewatch.common.entity.projectile.EntityTracerBullet;
import twopiradians.minewatch.common.hero.RenderManager;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.util.EntityHelper;
import twopiradians.minewatch.packet.SPacketSimple;

public class ItemTracerPistol extends ItemMWWeapon {

	public ItemTracerPistol() {
		super(20);
		this.hasOffhand = true;
	}

	@Override
	public void onItemLeftClick(ItemStack stack, World world, EntityLivingBase player, EnumHand hand) { 
		// shoot
		if (this.canUse(player, true, hand, false) && !world.isRemote) {
			EntityTracerBullet bullet = new EntityTracerBullet(player.world, player, hand.ordinal());
			EntityHelper.setAim(bullet, player, player.rotationPitch, player.rotationYawHead, -1, 2, hand, 7, 0.58f);
			player.world.spawnEntity(bullet);
			ModSoundEvents.TRACER_SHOOT.playSound(player, 1.0f, player.world.rand.nextFloat()/20+0.95f);
			this.subtractFromCurrentAmmo(player, 1);
			if (world.rand.nextInt(40) == 0)
				player.getHeldItem(hand).damageItem(1, player);
		}
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isSelected) {
		super.onUpdate(stack, world, entity, slot, isSelected);

		// dash
		if (entity instanceof EntityLivingBase && ((EntityLivingBase)entity).getHeldItemMainhand() == stack && (hero.ability2.isSelected((EntityLivingBase) entity) || hero.ability2.isSelected((EntityLivingBase) entity, Keys.KeyBind.RMB)) &&
				!world.isRemote && this.canUse((EntityLivingBase) entity, true, EnumHand.MAIN_HAND, true)) {
			entity.setSneaking(false);
			ModSoundEvents.TRACER_BLINK.playSound(entity, 1, world.rand.nextFloat()/2f+0.75f);
			if (entity instanceof EntityPlayerMP)
				Minewatch.network.sendTo(new SPacketSimple(0), (EntityPlayerMP) entity);
			else if (entity instanceof EntityHero)
				SPacketSimple.move((EntityLivingBase) entity, 9, false, true);
			hero.ability2.keybind.setCooldown((EntityLivingBase) entity, 3, true); 
			hero.ability2.subtractUse((EntityLivingBase) entity);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void preRenderGameOverlay(Pre event, EntityPlayer player, double width, double height) {
		// tracer's dash
		if (event.getType() == ElementType.CROSSHAIRS && Minecraft.getMinecraft().gameSettings.thirdPersonView == 0) {
			GlStateManager.enableBlend();

			double scale = 3d*Config.guiScale;
			GlStateManager.translate(width/2, height/2, 0);
			GlStateManager.scale(scale, scale, 1);
			Minecraft.getMinecraft().getTextureManager().bindTexture(RenderManager.ABILITY_OVERLAY);
			int uses = this.hero.ability2.getUses(player);
			GuiUtils.drawTexturedModalRect(-5, 8, 1, uses > 2 ? 239 : 243, 10, 4, 0);
			GlStateManager.scale(0.75f, 0.75f, 1);
			GuiUtils.drawTexturedModalRect(-5, 8, 1, uses > 1 ? 239 : 243, 10, 4, 0);
			GlStateManager.scale(0.75f, 0.75f, 1);
			GuiUtils.drawTexturedModalRect(-5, 8, 1, uses > 0 ? 239 : 243, 10, 4, 0);

			GlStateManager.disableBlend();
		}
	}

}