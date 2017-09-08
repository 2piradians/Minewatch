package twopiradians.minewatch.common.item.weapon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.common.collect.Maps;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.EntityGenjiShuriken;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.packet.SPacketTriggerAbility;

public class ItemGenjiShuriken extends ItemMWWeapon {

	private static HashMap<EntityPlayer, Integer> deflecting = Maps.newHashMap();
	public static HashMap<EntityPlayer, Integer> clientStriking = Maps.newHashMap();
	private static HashMap<EntityPlayerMP, Integer> serverStriking = Maps.newHashMap();

	public ItemGenjiShuriken() {
		super(40);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		return EnumAction.BLOCK;
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return Integer.MAX_VALUE;//TODO 8?
	}

	@Override
	public void onItemLeftClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand) { 
		if (!player.world.isRemote && this.canUse(player, true, hand) && player.ticksExisted % 3 == 0) {
			EntityGenjiShuriken shuriken = new EntityGenjiShuriken(player.world, player);
			shuriken.setAim(player, player.rotationPitch, player.rotationYaw, 3F, 1.0F, 1F, hand, false);
			player.world.spawnEntity(shuriken);
			player.world.playSound(null, player.posX, player.posY, player.posZ, 
					ModSoundEvents.genjiShoot, SoundCategory.PLAYERS, world.rand.nextFloat()+0.5F, 
					player.world.rand.nextFloat()/2+0.75f);	
			this.subtractFromCurrentAmmo(player, 1, hand);
			if (!player.getCooldownTracker().hasCooldown(this) && this.getCurrentAmmo(player) % 3 == 0 &&
					this.getCurrentAmmo(player) != this.getMaxAmmo(player))
				player.getCooldownTracker().setCooldown(this, 15);
			if (player.world.rand.nextInt(24) == 0)
				player.getHeldItem(hand).damageItem(1, player);
		}
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		if (!player.world.isRemote && this.canUse(player, true, hand)) {
			for (int i = 0; i < Math.min(3, this.getCurrentAmmo(player)); i++) {
				EntityGenjiShuriken shuriken = new EntityGenjiShuriken(player.world, player);
				shuriken.setAim(player, player.rotationPitch, player.rotationYaw + (1 - i)*8, 3F, 1.0F, 0F, hand, false);
				player.world.spawnEntity(shuriken);
			}
			player.world.playSound(null, player.posX, player.posY, player.posZ, 
					ModSoundEvents.genjiShoot, SoundCategory.PLAYERS, 1.0f, player.world.rand.nextFloat()/2+0.75f);
			this.subtractFromCurrentAmmo(player, 3, hand);
			if (world.rand.nextInt(8) == 0)
				player.getHeldItem(hand).damageItem(1, player);
			if (!player.getCooldownTracker().hasCooldown(this))
				player.getCooldownTracker().setCooldown(this, 15);
		}

		return new ActionResult(EnumActionResult.PASS, player.getHeldItem(hand));
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isSelected) {
		super.onUpdate(stack, world, entity, slot, isSelected);

		// strike
		ArrayList<EntityPlayer> toRemove = new ArrayList<EntityPlayer>();
		if (!world.isRemote && isSelected) {
			for (EntityPlayerMP player : serverStriking.keySet()) {
				if (player == entity) {
					hero.ability2.toggled.put(player.getPersistentID(), true);
					Minewatch.network.sendTo(new SPacketTriggerAbility(3), (EntityPlayerMP) entity);
					player.setActiveHand(EnumHand.MAIN_HAND);

					AxisAlignedBB aabb = player.getEntityBoundingBox().expandXyz(1.3d);
					List<Entity> list = player.world.getEntitiesWithinAABBExcludingEntity(player, aabb);

					for (Entity entityCollided : list) {
						if (entityCollided instanceof EntityLivingBase) {
							if (((EntityLivingBase)entityCollided).attackEntityFrom(DamageSource.causePlayerDamage(player), 50F*ItemMWWeapon.damageScale))
								entityCollided.world.playSound(null, entityCollided.getPosition(), ModSoundEvents.hurt, SoundCategory.PLAYERS, 0.3f, entityCollided.world.rand.nextFloat()/2+0.75f);
						}
					}

					if (serverStriking.get(player) > 1)
						serverStriking.put(player, serverStriking.get(player)-1);
					else {
						stack.getTagCompound().removeTag("useSword");
						player.resetActiveHand();
						toRemove.add(player);
						hero.ability2.keybind.setCooldown(player, 10, false); //TODO 160
					}
				}
			}
			for (EntityPlayer player : toRemove)
				serverStriking.remove(player);
		}

		// deflect
		toRemove = new ArrayList<EntityPlayer>();//TODO stop if not selected (detect nbt)
		if (!world.isRemote && isSelected) {
			for (EntityPlayer player : deflecting.keySet()) {
				if (player == entity) {
					hero.ability1.toggled.put(player.getPersistentID(), true);
					AxisAlignedBB aabb = player.getEntityBoundingBox().expandXyz(3);
					List<Entity> list = player.world.getEntitiesWithinAABBExcludingEntity(player, aabb);
					boolean playSound = false;

					for (Entity entityCollided : list) {
						if (entityCollided instanceof EntityArrow && ((EntityArrow)entityCollided).shootingEntity != player
								&& player.getLookVec().dotProduct(new Vec3d(entityCollided.motionX, entityCollided.motionY, entityCollided.motionZ)) < -0.1d &&
								Math.abs(entityCollided.motionX)+Math.abs(entityCollided.motionY)+Math.abs(entityCollided.motionZ) > 0) { 
							EntityArrow ent = (EntityArrow) entityCollided;
							ent.shootingEntity = player;
							double velScale = Math.sqrt(ent.motionX*ent.motionX + ent.motionY*ent.motionY + ent.motionZ*ent.motionZ)*1.2d;
							ent.setPosition(player.posX + player.getLookVec().xCoord, player.posY + player.eyeHeight, player.posZ + player.getLookVec().zCoord);
							ent.motionX = player.getLookVec().xCoord*velScale;	
							ent.motionY = player.getLookVec().yCoord*velScale;	
							ent.motionZ = player.getLookVec().zCoord*velScale;	
							ent.velocityChanged = true;
							playSound = true;
						}
						else if (entityCollided instanceof EntityThrowable && player.getLookVec().dotProduct(new Vec3d(entityCollided.motionX, entityCollided.motionY, entityCollided.motionZ)) < -0.1d) {
							EntityThrowable ent = (EntityThrowable) entityCollided;
							double velScale = Math.sqrt(ent.motionX*ent.motionX + ent.motionY*ent.motionY + ent.motionZ*ent.motionZ)*1.2d;
							ent.motionX = player.getLookVec().xCoord*velScale;	
							ent.motionY = player.getLookVec().yCoord*velScale;	
							ent.motionZ = player.getLookVec().zCoord*velScale;		
							ent.velocityChanged = true;
							playSound = true;
						}
						else if (entityCollided instanceof EntityFireball /*&& ((EntityFireball)entityCollided).shootingEntity != player*/
								&& player.getLookVec().dotProduct(new Vec3d(entityCollided.motionX, entityCollided.motionY, entityCollided.motionZ)) < -0.1d) {
							EntityFireball ent = (EntityFireball) entityCollided;
							ent.accelerationX = 0;
							ent.accelerationY = 0;
							ent.accelerationZ = 0;
							ent.shootingEntity = player;
							double velScale = Math.sqrt(ent.motionX*ent.motionX + ent.motionY*ent.motionY + ent.motionZ*ent.motionZ)*1.2d;
							ent.motionX = player.getLookVec().xCoord*velScale;	
							ent.motionY = player.getLookVec().yCoord*velScale;	
							ent.motionZ = player.getLookVec().zCoord*velScale;		
							ent.velocityChanged = true;
							playSound = true;
						}
					}
					if (playSound) {
						world.playSound(null, entity.getPosition(), ModSoundEvents.genjiDeflectHit, SoundCategory.PLAYERS, 0.6f, world.rand.nextFloat()/6f+0.9f);
						player.setActiveHand(EnumHand.MAIN_HAND);
					}

					if (deflecting.get(player) > 1)
						deflecting.put(player, deflecting.get(player)-1);
					else {
						stack.getTagCompound().removeTag("useSword");
						toRemove.add(player);
						hero.ability1.keybind.setCooldown(player, 10, false); //TODO 160
					}
				}
			}
			for (EntityPlayer player : toRemove)
				deflecting.remove(player);
		}

		if (entity instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer)entity;

			// deflect
			if (isSelected && !world.isRemote && hero.ability1.isSelected((EntityPlayer) entity) &&
					!deflecting.containsKey(entity)) {
				if (!stack.hasTagCompound())
					stack.setTagCompound(new NBTTagCompound());
				NBTTagCompound nbt = stack.getTagCompound();
				nbt.setBoolean("useSword", true);
				stack.setTagCompound(nbt);
				deflecting.put(player, 40);
				world.playSound(null, entity.getPosition(), ModSoundEvents.genjiDeflect, SoundCategory.PLAYERS, 1.0f, 1.0f);
			}

			// strike
			if (isSelected && !world.isRemote && hero.ability2.isSelected((EntityPlayer) entity) &&
					!serverStriking.containsKey(entity) && entity instanceof EntityPlayerMP) {
				if (!stack.hasTagCompound())
					stack.setTagCompound(new NBTTagCompound());
				NBTTagCompound nbt = stack.getTagCompound();
				nbt.setBoolean("useSword", true);
				stack.setTagCompound(nbt);
				((EntityPlayer) entity).setActiveHand(EnumHand.MAIN_HAND);
				serverStriking.put((EntityPlayerMP) player, 8);
				Minewatch.network.sendTo(new SPacketTriggerAbility(3, true), (EntityPlayerMP) entity);
				world.playSound(null, entity.getPosition(), ModSoundEvents.genjiStrike, SoundCategory.PLAYERS, 2f, 1.0f);
			}
		}
	}	

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void clientSide(PlayerTickEvent event) {
		if (event.phase == Phase.END && event.player.world.isRemote) {
			ArrayList<EntityPlayer> toRemove = new ArrayList<EntityPlayer>();
			for (EntityPlayer player : clientStriking.keySet()) {
				if (player == event.player && player instanceof EntityPlayerSP) {
					((EntityPlayerSP)player).movementInput.sneak = true;
					player.setActiveHand(EnumHand.MAIN_HAND);
					if (event.player.ticksExisted % 2 == 0) {
						if (clientStriking.get(player) > 1)
							clientStriking.put(player, clientStriking.get(player)-1);
						else {
							player.resetActiveHand();
							toRemove.add(player);
						}
					}
				}
			}
			for (EntityPlayer player : toRemove)
				clientStriking.remove(player);
		}
	}

	@SubscribeEvent
	public void onKill(LivingDeathEvent event) {
		// remove strike cooldown if killed by Genji
		if (event.getEntityLiving() != null && !event.getEntityLiving().world.isRemote && 
				event.getSource().getEntity() instanceof EntityPlayer) 
			hero.ability2.keybind.setCooldown((EntityPlayer) event.getSource().getEntity(), 0, false);
	}

}
