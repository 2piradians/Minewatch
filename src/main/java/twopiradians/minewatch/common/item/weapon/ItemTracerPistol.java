package twopiradians.minewatch.common.item.weapon;

import java.util.UUID;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import twopiradians.minewatch.common.entity.EntityTracerBullet;
import twopiradians.minewatch.common.item.ModItems;
import twopiradians.minewatch.common.item.armor.ModArmor;
import twopiradians.minewatch.common.sound.ModSoundEvents;

public class ItemTracerPistol extends ModWeapon
{
	protected static final UUID MOVEMENT_SPEED_UUID = UUID.fromString("308e48ee-a300-4846-9b56-05e53e35eb8f");
	
	public ItemTracerPistol() {
		super();
		this.setMaxDamage(100);
		this.hasOffhand = true;
		this.material = ModItems.tracer;
		this.cooldown = 20;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand) {
		playerIn.setActiveHand(hand);
		return new ActionResult<ItemStack>(EnumActionResult.PASS, playerIn.getHeldItem(hand));
	}

	@Override
	public void onUsingTick(ItemStack stack, EntityLivingBase player, int count) {
		//FIXME player slowed down while using
		if (!player.world.isRemote && player instanceof EntityPlayer) {
			if (player.getHeldItemMainhand() != null && player.getHeldItemMainhand().getItem() != Items.AIR && player.getHeldItemMainhand().getItem() instanceof ItemTracerPistol
					&& player.ticksExisted % 2 == 0) {
				for (int i=0; i<2; i++)
					player.world.spawnEntity(new EntityTracerBullet(player.world, player));
				player.world.playSound(null, player.posX, player.posY, player.posZ, 
						ModSoundEvents.tracerPistol, SoundCategory.PLAYERS, 1.0f, player.world.rand.nextFloat()/20+0.95f);	
				if (count == 20 && !ModArmor.isSet((EntityPlayer)player, ModItems.tracer))
					player.getHeldItemMainhand().damageItem(1, player);
			}
			if (player.getHeldItemOffhand() != null && player.getHeldItemOffhand().getItem() != Items.AIR && player.getHeldItemOffhand().getItem() instanceof ItemTracerPistol
					&& player.ticksExisted % 2 == 1) {
				for (int i=0; i<2; i++)
					player.world.spawnEntity(new EntityTracerBullet(player.world, player));
				player.world.playSound(null, player.posX, player.posY, player.posZ, 
						ModSoundEvents.tracerPistol, SoundCategory.PLAYERS, 1.0f, player.world.rand.nextFloat()/20+0.95f);
				if (count == 20 && !ModArmor.isSet((EntityPlayer)player, ModItems.tracer))
					player.getHeldItemOffhand().damageItem(1, player);
			}
			if (count <= 1)
				doCooldown((EntityPlayer)player, player.getActiveHand());
		}
	}

	/** How long it takes to use or consume an item*/
	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 20;
	}
}
