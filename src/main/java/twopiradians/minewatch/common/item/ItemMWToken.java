package twopiradians.minewatch.common.item;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.config.Config;
import twopiradians.minewatch.common.hero.EnumHero;

public class ItemMWToken extends Item {
	
	@Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, net.minecraft.enchantment.Enchantment enchantment) {
        return false;
    }
	
	@Override
	public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
		return false;
	}
	
	@SubscribeEvent
	public void onEvent(LivingDropsEvent event) {
		if (!event.getEntityLiving().world.isRemote && event.getEntityLiving() instanceof EntityLiving 
				&& event.getEntityLiving().getEntityWorld().rand.nextInt(100) < Config.tokenDropRate * (1 + event.getLootingLevel())) {
			int i = event.getEntityLiving().world.rand.nextInt(EnumHero.values().length);
			ItemStack stack;
			if (event.getEntityLiving().getEntityWorld().rand.nextInt(100) < Config.wildCardRate)
				stack = new ItemStack(ModItems.wild_card_token);
			else 
				stack = new ItemStack(EnumHero.values()[i].token);
			EntityItem drop = new EntityItem(event.getEntityLiving().world, event.getEntityLiving().posX, 
					event.getEntityLiving().posY, event.getEntityLiving().posZ, stack);
			event.getDrops().add(drop);
		}
	}	

	public static class ItemWildCardToken extends ItemMWToken {
		
		@Override
		public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
			if (world.isRemote)
				Minewatch.proxy.openWildCardGui();
			
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
		}

		@Override
		@SideOnly(Side.CLIENT)
		public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
			tooltip.add(TextFormatting.GOLD+"Right-click this token to exchange for another hero token of your choice.");
		}
	}
}
