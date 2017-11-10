package twopiradians.minewatch.common.potion;

import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.CommonProxy.EnumParticle;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.tickhandler.TickHandler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Identifier;
import twopiradians.minewatch.packet.SPacketSimple;

public class PotionFrozen extends Potion {

	public PotionFrozen(boolean isBadEffectIn, int liquidColorIn) {
		super(isBadEffectIn, liquidColorIn);
		MinecraftForge.EVENT_BUS.register(this);
		this.setPotionName("Frozen");
	}

	@Override
	public boolean isReady(int duration, int amplifier) {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderInventoryEffect(int x, int y, PotionEffect effect, net.minecraft.client.Minecraft mc) { 
		GlStateManager.pushMatrix();
		Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation(Minewatch.MODID+":textures/effects/frozen.png"));
		Minecraft.getMinecraft().currentScreen.drawTexturedModalRect(x+6, y+8, 0, 0, 16, 16);
		GlStateManager.popMatrix();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderHUDEffect(int x, int y, PotionEffect effect, net.minecraft.client.Minecraft mc, float alpha) { 
		GlStateManager.pushMatrix();
		Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation(Minewatch.MODID+":textures/effects/frozen.png"));
		Minecraft.getMinecraft().ingameGUI.drawTexturedModalRect(x+3, y+4, 0, 0, 18, 18);
		GlStateManager.popMatrix();
	}

	@Override
	public void removeAttributesModifiersFromEntity(EntityLivingBase entity, AbstractAttributeMap map, int amplifier) {
		super.removeAttributesModifiersFromEntity(entity, map, amplifier);

		Minewatch.network.sendToDimension(new SPacketSimple(23, entity, false, entity.posX, entity.posY+entity.height/2, entity.posZ), entity.worldObj.provider.getDimension());
		entity.worldObj.playSound(null, entity.getPosition(), ModSoundEvents.meiUnfreeze, SoundCategory.NEUTRAL, 0.8f, 1.0f);

		// remove potion effect on client
		if (entity instanceof EntityPlayerMP)
			Minewatch.network.sendTo(new SPacketSimple(8), (EntityPlayerMP) entity);
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void colorEntities(RenderLivingEvent.Pre<EntityLivingBase> event) {
		if (TickHandler.hasHandler(event.getEntity(), Identifier.POTION_FROZEN) || 
				(event.getEntity().getActivePotionEffect(ModPotions.frozen) != null && 
				event.getEntity().getActivePotionEffect(ModPotions.frozen).getDuration() > 0)) {
			int freeze = TickHandler.getHandler(event.getEntity(), Identifier.POTION_FROZEN) != null ? 
					TickHandler.getHandler(event.getEntity(), Identifier.POTION_FROZEN).ticksLeft : 30;
					event.getEntity().maxHurtTime = -1;
					event.getEntity().hurtTime = -1;
					GlStateManager.color(1f-freeze/30f, 1f-freeze/120f, 1f);
					Random rand = event.getEntity().worldObj.rand;
					if (rand.nextInt(130 - freeze*2) == 0)
						event.getEntity().worldObj.spawnParticle(EnumParticleTypes.SNOW_SHOVEL, 
								(event.getEntity().posX+rand.nextDouble()-0.5d)*event.getEntity().width, 
								event.getEntity().posY+rand.nextDouble()-0.5d+event.getEntity().height/2, 
								(event.getEntity().posZ+rand.nextDouble()-0.5d)*event.getEntity().width, 
								(rand.nextDouble()-0.5d)*0.5d, 
								rand.nextDouble()-0.5d, 
								(rand.nextDouble()-0.5d)*0.5d, 
								new int[0]);
					if (rand.nextInt(70 - freeze*2) == 0)
						Minewatch.proxy.spawnParticlesCustom(EnumParticle.CIRCLE, event.getEntity().worldObj, 
								event.getEntity().posX+rand.nextDouble()-0.5d, 
								event.getEntity().posY+rand.nextDouble()-0.5d+event.getEntity().height/2, 
								event.getEntity().posZ+rand.nextDouble()-0.5d, 
								0, (rand.nextDouble())*0.2f, 0, 0x5BC8E0, 0xAED4FF, rand.nextFloat(), 8, 2.5f, 2f, 0, 0);
		}
	}

}