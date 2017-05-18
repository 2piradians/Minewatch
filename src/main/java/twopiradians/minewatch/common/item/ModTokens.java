package twopiradians.minewatch.common.item;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ModTokens extends Item
{
	@SubscribeEvent
	public void onEvent(LivingDropsEvent event) {
		if (!event.getEntityLiving().world.isRemote && event.getEntityLiving() instanceof EntityLiving 
				&& event.getEntityLiving().getEntityWorld().rand.nextDouble() < 0.01d * (1+event.getLootingLevel())) {
			int i = event.getEntityLiving().world.rand.nextInt(ModItems.tokens.size());
			ItemStack stack = new ItemStack(ModItems.tokens.get(i));
			EntityItem drop = new EntityItem(event.getEntityLiving().world, event.getEntityLiving().posX, 
					event.getEntityLiving().posY, event.getEntityLiving().posZ, stack);
			event.getDrops().add(drop);
		}
	}

	public static class ItemReaperToken extends ModTokens {}

	public static class ItemReinhardtToken extends ModTokens {}

	public static class ItemAnaToken extends ModTokens {}

	public static class ItemHanzoToken extends ModTokens {}
	
	public static class ItemGenjiToken extends ModTokens {}
}
