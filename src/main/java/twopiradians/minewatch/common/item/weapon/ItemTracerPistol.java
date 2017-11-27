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
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.client.key.Keys;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.config.Config;
import twopiradians.minewatch.common.entity.hero.EntityHero;
import twopiradians.minewatch.common.entity.projectile.EntityTracerBullet;
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
			player.world.playSound(null, player.posX, player.posY, player.posZ, ModSoundEvents.tracerShoot, SoundCategory.PLAYERS, 1.0f, player.world.rand.nextFloat()/20+0.95f);	
			this.subtractFromCurrentAmmo(player, 1);
			if (world.rand.nextInt(40) == 0)
				player.getHeldItem(hand).damageItem(1, player);
		}
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isSelected) {
		super.onUpdate(stack, world, entity, slot, isSelected);

		// dash
		if (isSelected && entity instanceof EntityLivingBase && (hero.ability2.isSelected((EntityLivingBase) entity) || hero.ability2.isSelected((EntityLivingBase) entity, Keys.KeyBind.RMB)) &&
				!world.isRemote && this.canUse((EntityLivingBase) entity, true, EnumHand.MAIN_HAND, true)) {
			entity.setSneaking(false);
			world.playSound(null, entity.getPosition(), ModSoundEvents.tracerBlink, SoundCategory.PLAYERS, 1.0f, world.rand.nextFloat()/2f+0.75f);
			if (entity instanceof EntityPlayerMP)
				Minewatch.network.sendTo(new SPacketSimple(0), (EntityPlayerMP) entity);
			else if (entity instanceof EntityHero)
				SPacketSimple.move((EntityLivingBase) entity, 9, false, true);
			hero.ability2.subtractUse((EntityLivingBase) entity);
			hero.ability2.keybind.setCooldown((EntityLivingBase) entity, 3, true); 
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void preRenderGameOverlay(Pre event, EntityPlayer player, double width, double height) {
		// tracer's dash
		GlStateManager.enableBlend();

		double scale = 0.8d*Config.guiScale;
		GlStateManager.scale(scale, scale*4, 1);
		GlStateManager.translate((int) ((width - 83*scale)/2d / scale), (int) ((height- 80*scale)/8d / scale), 0);
		Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(Minewatch.MODID, "textures/gui/ability_overlay.png"));
		int uses = this.hero.ability2.getUses(player);
		GuiUtils.drawTexturedModalRect(23, 21, 1, uses > 2 ? 1011 : 1015, 40, 4, 0);
		GlStateManager.scale(0.75f, 0.75f, 1);
		GuiUtils.drawTexturedModalRect(37, 25, 1, uses > 1 ? 1011 : 1015, 40, 4, 0);
		GlStateManager.scale(0.75f, 0.75f, 1);
		GuiUtils.drawTexturedModalRect(56, 30, 1, uses > 0 ? 1011 : 1015, 40, 4, 0);

		GlStateManager.disableBlend();
	}

}