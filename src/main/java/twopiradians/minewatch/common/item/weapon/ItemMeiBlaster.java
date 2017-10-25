package twopiradians.minewatch.common.item.weapon;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.client.model.ModelMWArmor;
import twopiradians.minewatch.common.entity.EntityMeiBlast;
import twopiradians.minewatch.common.entity.EntityMeiIcicle;
import twopiradians.minewatch.common.potion.ModPotions;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.tickhandler.TickHandler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Identifier;
import twopiradians.minewatch.common.util.EntityHelper;

public class ItemMeiBlaster extends ItemMWWeapon {

	public ItemMeiBlaster() {
		super(30);
	}

	@Override
	public void onItemLeftClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand) { 
		// shoot
		if (this.canUse(player, true, hand, false) && !world.isRemote) {
			EntityMeiBlast bullet = new EntityMeiBlast(world, player, hand.ordinal());
			EntityHelper.setAim(bullet, player, player.rotationPitch, player.rotationYaw, 20, 0.6F, hand, 14, 0.8f);
			world.spawnEntity(bullet);
			world.playSound(null, player.posX, player.posY, player.posZ, 
					ModSoundEvents.meiShoot, SoundCategory.PLAYERS, world.rand.nextFloat()/3, 
					world.rand.nextFloat()/2+0.75f);	

			this.subtractFromCurrentAmmo(player, 1);
			if (world.rand.nextInt(200) == 0)
				player.getHeldItem(hand).damageItem(1, player);
		}
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		// shoot
		if (this.canUse(player, true, hand, false)) {//TODO delay
			if (!world.isRemote) {
				EntityMeiIcicle icicle = new EntityMeiIcicle(world, player, hand.ordinal());
				EntityHelper.setAim(icicle, player, player.rotationPitch, player.rotationYaw, 100, 0.4F, hand, 8, 0.35f);
				world.spawnEntity(icicle);
				if (!player.getCooldownTracker().hasCooldown(this))
					player.getCooldownTracker().setCooldown(this, 24);
				world.playSound(null, player.posX, player.posY, player.posZ, ModSoundEvents.meiIcicleShoot, 
						SoundCategory.PLAYERS, world.rand.nextFloat()+0.5F, world.rand.nextFloat()/20+0.95f);	
				if (world.rand.nextInt(8) == 0)
					player.getHeldItem(hand).damageItem(1, player);
				this.subtractFromCurrentAmmo(player, 25, hand);
			}
		}

		return new ActionResult(EnumActionResult.PASS, player.getHeldItem(hand));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void preRenderArmor(EntityLivingBase entity, ModelMWArmor model) {
		// frozen coloring
		if (TickHandler.hasHandler(entity, Identifier.POTION_FROZEN) || 
				(entity != null && entity.getActivePotionEffect(ModPotions.frozen) != null && 
				entity.getActivePotionEffect(ModPotions.frozen).getDuration() > 0)) {
			int freeze = TickHandler.getHandler(entity, Identifier.POTION_FROZEN) != null ? 
					TickHandler.getHandler(entity, Identifier.POTION_FROZEN).ticksLeft : 30;
					entity.maxHurtTime = -1;
					entity.hurtTime = -1;
					GlStateManager.color(1f-freeze/30f, 1f-freeze/120f, 1f);
		}
	}

}
