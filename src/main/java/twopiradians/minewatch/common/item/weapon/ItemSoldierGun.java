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
import twopiradians.minewatch.common.item.ModItems;
import twopiradians.minewatch.common.item.armor.ModArmor;
import twopiradians.minewatch.common.sound.ModSoundEvents;

public class ItemSoldierGun extends ModWeapon
{	
	public ItemSoldierGun() {
		super();
		this.setMaxDamage(100);
		this.hasOffhand = false;
		this.material = ModItems.soldier;
		this.cooldown = 30;
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand) {
		playerIn.setActiveHand(hand);
		return new ActionResult<ItemStack>(EnumActionResult.PASS, playerIn.getHeldItem(hand));
	}

	@Override
	public void onUsingTick(ItemStack stack, EntityLivingBase player, int count) {
		if (!player.world.isRemote && player instanceof EntityPlayer) {
			if (player.getHeldItemMainhand() != null && player.getHeldItemMainhand().getItem() != Items.AIR && player.getHeldItemMainhand().getItem() instanceof ItemSoldierGun
					&& player.ticksExisted % 2 == 0) {
				player.world.spawnEntity(new EntitySoldierBullet(player.world, player, EnumHand.MAIN_HAND));
				player.world.playSound(null, player.posX, player.posY, player.posZ, ModSoundEvents.soldierGun, SoundCategory.PLAYERS, 1.0f, player.world.rand.nextFloat()/20+0.95f);	
				if (count == 50 && !ModArmor.isSet((EntityPlayer)player, ModItems.tracer))
					player.getHeldItemMainhand().damageItem(1, player);
			}
			else if (player.getHeldItemOffhand() != null && player.getHeldItemOffhand().getItem() != Items.AIR && player.getHeldItemOffhand().getItem() instanceof ItemSoldierGun
					&& player.ticksExisted % 2 == 0) {
				player.world.spawnEntity(new EntitySoldierBullet(player.world, player, EnumHand.OFF_HAND));
				player.world.playSound(null, player.posX, player.posY, player.posZ, ModSoundEvents.soldierGun, SoundCategory.PLAYERS, 1.0f, player.world.rand.nextFloat()/20+0.95f);	
				if (count == 50 && !ModArmor.isSet((EntityPlayer)player, ModItems.tracer))
					player.getHeldItemOffhand().damageItem(1, player);
			}
			
			if (count <= 1)
				doCooldown((EntityPlayer)player, player.getActiveHand());
		}

	}

	/** How long it takes to use or consume an item*/
	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 50;
	}
}
