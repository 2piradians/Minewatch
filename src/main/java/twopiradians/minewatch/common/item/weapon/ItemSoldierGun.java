package twopiradians.minewatch.common.item.weapon;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import twopiradians.minewatch.common.entity.EntitySoldierBullet;
import twopiradians.minewatch.common.hero.Hero;
import twopiradians.minewatch.common.item.armor.ModArmor;
import twopiradians.minewatch.common.sound.ModSoundEvents;

public class ItemSoldierGun extends ModWeapon
{	
	public ItemSoldierGun() {
		super(Hero.SOLDIER76, 30);
		this.setMaxDamage(100);
		this.hasOffhand = false;
		this.cooldown = 30;
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand) {
		playerIn.setActiveHand(hand);
		return new ActionResult<ItemStack>(EnumActionResult.PASS, playerIn.getHeldItem(hand));
	}

	@Override
	public void onUsingTick(ItemStack stack, EntityLivingBase entity, int count) {		
		if (!entity.world.isRemote && entity instanceof EntityPlayer && !((EntityPlayer)entity).getCooldownTracker().hasCooldown(this)) {
			if (entity.getHeldItemMainhand() != null && entity.getHeldItemMainhand().getItem() != Items.AIR && entity.getHeldItemMainhand().getItem() instanceof ItemSoldierGun
					&& entity.ticksExisted % 2 == 0) {
				entity.world.spawnEntity(new EntitySoldierBullet(entity.world, entity, EnumHand.MAIN_HAND));
				entity.world.playSound(null, entity.posX, entity.posY, entity.posZ, ModSoundEvents.soldierGun, SoundCategory.PLAYERS, 1.0f, entity.world.rand.nextFloat()/20+0.95f);	
				System.out.println(((EntityPlayer)entity).getCooldownTracker().getCooldown(this, 0));
				this.subtractFromCurrentAmmo((EntityPlayer) entity, 1);
				if (count == 50 && !(ModArmor.SetManager.playersWearingSets.get(entity.getPersistentID()) == hero))
					entity.getHeldItemMainhand().damageItem(1, entity);
			}
			else if (entity.getHeldItemOffhand() != null && entity.getHeldItemOffhand().getItem() != Items.AIR && entity.getHeldItemOffhand().getItem() instanceof ItemSoldierGun
					&& entity.ticksExisted % 2 == 0) {
				entity.world.spawnEntity(new EntitySoldierBullet(entity.world, entity, EnumHand.OFF_HAND));
				this.subtractFromCurrentAmmo((EntityPlayer) entity, 1);
				entity.world.playSound(null, entity.posX, entity.posY, entity.posZ, ModSoundEvents.soldierGun, SoundCategory.PLAYERS, 1.0f, entity.world.rand.nextFloat()/20+0.95f);	
				if (count == 50 && !(ModArmor.SetManager.playersWearingSets.get(entity.getPersistentID()) == hero))
					entity.getHeldItemOffhand().damageItem(1, entity);
			}
			
			if (count <= 1)
				doCooldown((EntityPlayer)entity, entity.getActiveHand());
		}

	}

	/** How long it takes to use or consume an item*/
	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 50;
	}
}
