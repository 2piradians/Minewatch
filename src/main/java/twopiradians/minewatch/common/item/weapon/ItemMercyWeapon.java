package twopiradians.minewatch.common.item.weapon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import com.google.common.collect.Maps;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.EntityMWThrowable;
import twopiradians.minewatch.common.entity.EntityMercyBeam;
import twopiradians.minewatch.common.entity.EntityMercyBullet;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.item.armor.ItemMWArmor;
import twopiradians.minewatch.common.sound.ModSoundEvents;

public class ItemMercyWeapon extends ItemMWWeapon {

	public static HashMap<EntityPlayer, Integer> notRegening = Maps.newHashMap();
	public static HashMap<EntityPlayer, EntityMercyBeam> beams = Maps.newHashMap();

	public ItemMercyWeapon() {
		super(20);
		this.savePlayerToNBT = true;
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		boolean battleMercy = false;
		if (stack.hasTagCompound()) {
			UUID uuid = stack.getTagCompound().getUniqueId("player");
			battleMercy = uuid != null && hero.playersUsingAlt.containsKey(uuid) && hero.playersUsingAlt.get(uuid);		
		}
		return battleMercy ? "Caduceus Blaster" : "Caduceus Staff";
	}

	@Override
	public void onItemLeftClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand) { 
		if (this.canUse(player, true, hand) && hero.playersUsingAlt.containsKey(player.getPersistentID()) && 
				hero.playersUsingAlt.get(player.getPersistentID())) {
			if (!world.isRemote) {
				EntityMercyBullet bullet = new EntityMercyBullet(world, player);
				bullet.setAim(player, player.rotationPitch, player.rotationYaw, 2.0F, 0.3F, 0F, hand, true);
				world.spawnEntity(bullet);
				world.playSound(null, player.posX, player.posY, player.posZ, 
						ModSoundEvents.mercyShoot, SoundCategory.PLAYERS, world.rand.nextFloat()+0.5F, 
						world.rand.nextFloat()/2+0.75f);	

				this.subtractFromCurrentAmmo(player, 1, hand);
				if (!player.getCooldownTracker().hasCooldown(this))
					player.getCooldownTracker().setCooldown(this, 5);
				if (world.rand.nextInt(20) == 0)
					player.getHeldItem(hand).damageItem(1, player);
			}
			else {
				Vec3d vec = EntityMWThrowable.getShootingPos(player, player.rotationPitch, player.rotationYaw, hand);
				Minewatch.proxy.spawnParticlesSpark(world, vec.xCoord, vec.yCoord, vec.zCoord, 0x25307E, 0xE0DFCF, 3, 3);
			}
		}
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isSelected) {	
		super.onUpdate(stack, world, entity, slot, isSelected);

		if (isSelected && entity instanceof EntityPlayer) {
			if (beams.containsKey(entity) && beams.get(entity).isDead)
				beams.remove(entity);
			if (this.getItemStackDisplayName(stack).equals("Caduceus Staff") && 
					(Minewatch.keys.rmb((EntityPlayer) entity) || Minewatch.keys.lmb((EntityPlayer) entity)) &&
					!ItemMercyWeapon.beams.containsKey(entity)) {
				EntityMercyBeam beam = new EntityMercyBeam(world, (EntityPlayer) entity);
				world.spawnEntity(beam);
				beams.put((EntityPlayer) entity, beam);
			}
		}
	}

	@SubscribeEvent
	public void addToNotRegening(LivingHurtEvent event) {
		if (event.getEntity() instanceof EntityPlayer && !event.getEntity().world.isRemote &&
				ItemMWArmor.SetManager.playersWearingSets.get(event.getEntity().getPersistentID()) == EnumHero.MERCY) {
			notRegening.put((EntityPlayer) event.getEntity(), 20);
			((EntityPlayer)event.getEntity()).removePotionEffect(MobEffects.REGENERATION);
		}
	}

	@SubscribeEvent
	public void serverSide(WorldTickEvent event) {
		if (event.phase == TickEvent.Phase.END && event.world.getTotalWorldTime() % 6 == 0) {
			// regeneration
			ArrayList<EntityPlayer> toRemove = new ArrayList<EntityPlayer>();
			for (EntityPlayer player : notRegening.keySet()) {
				if (notRegening.get(player) > 1)
					notRegening.put(player, notRegening.get(player)-1);
				else
					toRemove.add(player);
			}
			for (EntityPlayer player : toRemove)
				notRegening.remove(player);
		}
	}

	@SubscribeEvent
	public void renderStaffBeam(PlayerTickEvent event) {
		/*if (event.phase == TickEvent.Phase.END && event.side == Side.CLIENT) {
			Tessellator tessellator = Tessellator.getInstance();
            VertexBuffer vertexbuffer = tessellator.getBuffer();
            Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(Minewatch.MODID+":textures/gui/white.png"));
            GlStateManager.glTexParameteri(3553, 10242, 10497);
            GlStateManager.glTexParameteri(3553, 10243, 10497);
            GlStateManager.disableLighting();
            GlStateManager.disableCull();
            GlStateManager.disableBlend();
            GlStateManager.depthMask(true);
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.pushMatrix();
            GlStateManager.color(1, 1, 1, 1);
            vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            vertexbuffer.pos(0, 0, 0).endVertex();
            vertexbuffer.pos(5000, 0, 0).endVertex();
            vertexbuffer.pos(50, 5000, 0).endVertex();
            vertexbuffer.pos(0, 5000, 0).endVertex();
            tessellator.draw();
            GlStateManager.popMatrix();
		}*/

	}

}