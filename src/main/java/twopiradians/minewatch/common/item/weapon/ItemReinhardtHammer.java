package twopiradians.minewatch.common.item.weapon;

import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Multimap;

import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import twopiradians.minewatch.common.item.ModItems;
import twopiradians.minewatch.common.item.armor.ModArmor;
import twopiradians.minewatch.common.sound.ModSoundEvents;

public class ItemReinhardtHammer extends ModWeapon 
{
	public ItemReinhardtHammer() {
		super();
		this.material = ModItems.reinhardt;
		this.setMaxDamage(100);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack) {
		Multimap<String, AttributeModifier> multimap = super.getAttributeModifiers(slot, stack);
		if (slot == EntityEquipmentSlot.MAINHAND)
			multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getAttributeUnlocalizedName(), 
					new AttributeModifier(ATTACK_DAMAGE_MODIFIER, SharedMonsterAttributes.ATTACK_DAMAGE.getAttributeUnlocalizedName(), 75d/DAMAGE_SCALE-1, 0));
		return multimap;
	}

	@Override
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
		if (!player.worldObj.isRemote && player.getHeldItemMainhand() != null && 
				player.getHeldItemMainhand().getItem() instanceof ItemReinhardtHammer) {
			if (player.getCooldownTracker().hasCooldown(this))
				return true;
			AxisAlignedBB aabb = entity.getEntityBoundingBox().expandXyz(2);
			List<Entity> list = player.worldObj.getEntitiesWithinAABBExcludingEntity(player, aabb);
			if (!list.isEmpty()) {
				Iterator<Entity> iterator = list.iterator();            
				while (iterator.hasNext()) {
					Entity entityInArea = iterator.next();
					entityInArea.attackEntityFrom(DamageSource.causePlayerDamage(player), 75/DAMAGE_SCALE);
				}
			}
			if (!ModArmor.isSet(player, material))
				player.getHeldItemMainhand().damageItem(1, player);
			player.getCooldownTracker().setCooldown(this, 20);
			player.worldObj.playSound(null, player.posX, player.posY, player.posZ, 
					ModSoundEvents.reinhardtRocketHammer, SoundCategory.PLAYERS, 1.0f, player.worldObj.rand.nextFloat()/2+0.75f);
		}
		return false;
	}
	
	/**Reinhardt Hammer attack*/
	@SubscribeEvent
	public void onEvent(PlayerInteractEvent.LeftClickEmpty event) {
		if (event.getWorld() != null && event.getEntityPlayer().getHeldItemMainhand() != null 
				&& event.getEntityPlayer().getHeldItemMainhand().getItem() instanceof ItemReinhardtHammer) {
			EntityPlayer player = event.getEntityPlayer();
			if (player.getCooldownTracker().hasCooldown(player.getHeldItemMainhand().getItem())) {
				player.worldObj.playSound(player, player.posX, player.posY, player.posZ, 
						ModSoundEvents.reinhardtRocketHammer, SoundCategory.PLAYERS, 1.0f, event.getWorld().rand.nextFloat()/2+0.75f);
			}
		}
	}
}
