package twopiradians.minewatch.common.item.weapon;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.EntityViewRenderEvent.FOVModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.entity.EntityTracerBullet;
import twopiradians.minewatch.common.item.ModItems;
import twopiradians.minewatch.common.item.armor.ModArmor;
import twopiradians.minewatch.common.sound.ModSoundEvents;

public class ItemTracerPistol extends ModWeapon
{	
	public ItemTracerPistol() {
		super();
		this.setMaxDamage(100);
		this.hasOffhand = true;
		this.material = ModItems.tracer;
		this.cooldown = 40;
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
			if (player.getHeldItemMainhand() != null && player.getHeldItemMainhand().getItem() != Items.AIR && player.getHeldItemMainhand().getItem() instanceof ItemTracerPistol) {
				for (int i=0; i<2; i++)
					player.world.spawnEntity(new EntityTracerBullet(player.world, player, EnumHand.MAIN_HAND));
				player.world.playSound(null, player.posX, player.posY, player.posZ, ModSoundEvents.tracerPistol, SoundCategory.PLAYERS, 1.0f, player.world.rand.nextFloat()/20+0.95f);	
				if (count == 20 && !ModArmor.isSet((EntityPlayer)player, ModItems.tracer))
					player.getHeldItemMainhand().damageItem(1, player);
			}
			if (player.getHeldItemOffhand() != null && player.getHeldItemOffhand().getItem() != Items.AIR && player.getHeldItemOffhand().getItem() instanceof ItemTracerPistol) {
				for (int i=0; i<2; i++)
					player.world.spawnEntity(new EntityTracerBullet(player.world, player, EnumHand.OFF_HAND));
				player.world.playSound(null, player.posX, player.posY, player.posZ, ModSoundEvents.tracerPistol, SoundCategory.PLAYERS, 1.0f, player.world.rand.nextFloat()/20+0.95f);
				if (count == 20 && !ModArmor.isSet((EntityPlayer)player, ModItems.tracer))
					player.getHeldItemOffhand().damageItem(1, player);
			}

			if (count <= 1)
				doCooldown((EntityPlayer)player, player.getActiveHand());
		}
		if (player.world.isRemote) {
			
			float f6 = 0.91F;
			BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos = BlockPos.PooledMutableBlockPos.retain(player.posX, player.getEntityBoundingBox().minY - 1.0D, player.posZ);
			if (player.onGround)
				f6 = player.world.getBlockState(blockpos$pooledmutableblockpos).getBlock().slipperiness * 0.91F;
			float f7 = 0.16277136F / (f6 * f6 * f6);
			float friction = 0;
			if (player.onGround)
				friction = player.getAIMoveSpeed() * f7;
			else if (player.isInWater() || player.isInLava())
				friction = 0.02f;
			else
				friction = player.jumpMovementFactor;
			double slow = 1;
			if (player.isInWater())
				slow = 0.08d;
			else if (player.isInLava())
				slow = 0.05d;
			player.motionX *= slow;
			player.motionY *= slow;
			player.motionZ *= slow;
			player.moveRelative(player.moveStrafing*5f, player.moveForward*5f, friction);
		}
	}

	/** How long it takes to use or consume an item*/
	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 20;
	}
	
	/**Change the FOV when scoped*/
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onEvent(FOVModifier event) {
		if (event.getEntity() != null && event.getEntity() instanceof EntityPlayer
				&& ((((EntityPlayer)event.getEntity()).getHeldItemMainhand() != null && ((EntityPlayer)event.getEntity()).getHeldItemMainhand().getItem() instanceof ItemTracerPistol)
						|| (((EntityPlayer)event.getEntity()).getHeldItemOffhand() != null && ((EntityPlayer)event.getEntity()).getHeldItemOffhand().getItem() instanceof ItemTracerPistol))) {
			event.setFOV(Minecraft.getMinecraft().gameSettings.fovSetting);
		}
	}
}
