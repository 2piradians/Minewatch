package twopiradians.minewatch.common.item.weapon;

import java.util.Iterator;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.EntityAnaBullet;
import twopiradians.minewatch.common.hero.Hero;
import twopiradians.minewatch.common.sound.ModSoundEvents;

public class ItemAnaRifle extends ModWeapon 
{
	public ItemAnaRifle() {
		super(Hero.ANA, 30);
		this.setMaxDamage(100);
		this.cooldown = 30;
		this.scope = new ResourceLocation(Minewatch.MODID + ":textures/gui/ana_scope.png");
	}

	@Override
	public void onShoot(World worldIn, EntityPlayer playerIn, EnumHand hand) {
		if (!worldIn.isRemote) {
			EntityAnaBullet bullet = new EntityAnaBullet(worldIn, playerIn, false);//TODO have as scroll wheel change like torb/mercy
			bullet.setAim(playerIn, playerIn.rotationPitch, playerIn.rotationYaw, 5.0F, 0.3F);
			worldIn.spawnEntity(bullet);
			worldIn.playSound(null, playerIn.posX, playerIn.posY, playerIn.posZ, 
					ModSoundEvents.reaperShotgun, SoundCategory.PLAYERS, 1.0f, worldIn.rand.nextFloat()/2+0.75f);	
		}
	}
	
	@Override
	public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		
		if (entityIn != null && entityIn instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer)entityIn;

			// sleep dart
			if (isSelected && Minewatch.keys.ability1(player)) {
				
			}
			
			// biotic grenade
			if (isSelected && Minewatch.keys.ability2(player)) {
				
			}
			
			//Ana's Rifle
			if (player.getHeldItemMainhand() != null && player.getHeldItemMainhand().getItem() instanceof ItemAnaRifle 
					&& false && entityIn.ticksExisted % 10 == 0) {//TODO have as scroll wheel change like torb/mercy
				AxisAlignedBB aabb = entityIn.getEntityBoundingBox().expandXyz(30);
				List<Entity> list = entityIn.world.getEntitiesWithinAABBExcludingEntity(entityIn, aabb);
				if (!list.isEmpty()) {
					Iterator<Entity> iterator = list.iterator();            
					while (iterator.hasNext()) {
						Entity entityInArea = iterator.next();
						if (entityInArea != null && entityInArea instanceof EntityPlayer 
								&& ((EntityPlayer)entityInArea).isOnSameTeam(player) 
								&& ((EntityPlayer)entityInArea).getHealth() < ((EntityPlayer)entityInArea).getMaxHealth()) {
							Minewatch.proxy.spawnParticlesHealthPlus(player.world, entityInArea.posX, 
									entityInArea.posY+2.5d, entityInArea.posZ, 0, 0, 0, 3);
						}
						else if (entityInArea != null && entityInArea instanceof EntityLiving) {
							entityInArea.world.spawnParticle(EnumParticleTypes.HEART, entityInArea.posX, 
									entityInArea.posY+2d, entityInArea.posZ, 0, 1, 0, new int[0]);
						}
					}
				}
			}
		}
	}
}
