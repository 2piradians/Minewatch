package twopiradians.minewatch.common.item.weapon;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.ability.EntityRoadhogHook;
import twopiradians.minewatch.common.entity.ability.EntityRoadhogScrap;
import twopiradians.minewatch.common.entity.projectile.EntityRoadhogBullet;
import twopiradians.minewatch.common.item.ModItems;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.util.EntityHelper;
import twopiradians.minewatch.common.util.TickHandler;
import twopiradians.minewatch.common.util.TickHandler.Handler;
import twopiradians.minewatch.common.util.TickHandler.Identifier;
import twopiradians.minewatch.packet.SPacketSimple;

public class ItemRoadhogWeapon extends ItemMWWeapon {

	private static final ResourceLocation CHAIN = new ResourceLocation(Minewatch.MODID, "textures/entity/roadhog_chain.png");
	public static final Handler HEALING = new Handler(Identifier.ROADHOG_HEALING, true) {
		@Override
		@SideOnly(Side.CLIENT)
		public boolean onClientTick() {

			return super.onClientTick();
		}
		@Override
		public boolean onServerTick() {

			return super.onServerTick();
		}
		@Override
		@SideOnly(Side.CLIENT)
		public Handler onClientRemove() {

			return super.onClientRemove();
		}
		@Override
		public Handler onServerRemove() {

			return super.onServerRemove();
		}
	};
	public static final Handler HOOKING = new Handler(Identifier.ROADHOG_HOOKING, true) {
		@Override
		@SideOnly(Side.CLIENT)
		public boolean onClientTick() {

			return super.onClientTick();
		}
		@Override
		public boolean onServerTick() {

			return super.onServerTick();
		}
		@Override
		@SideOnly(Side.CLIENT)
		public Handler onClientRemove() {

			return super.onClientRemove();
		}
		@Override
		public Handler onServerRemove() {

			return super.onServerRemove();
		}
	};
	public static final Handler HOOKED = new Handler(Identifier.ROADHOG_HOOKED, false) {
		@Override
		@SideOnly(Side.CLIENT)
		public boolean onClientTick() {

			return super.onClientTick();
		}
		@Override
		public boolean onServerTick() {

			return super.onServerTick();
		}
		@Override
		@SideOnly(Side.CLIENT)
		public Handler onClientRemove() {

			return super.onClientRemove();
		}
		@Override
		public Handler onServerRemove() {

			return super.onServerRemove();
		}
	};

	public ItemRoadhogWeapon() {
		super(30);
	}

	@Override
	public void onItemLeftClick(ItemStack stack, World world, EntityLivingBase player, EnumHand hand) { 
		// primary fire
		if (!world.isRemote && this.canUse(player, true, hand, false) && !TickHandler.hasHandler(player, Identifier.ROADHOG_HEALING)) {
			for (int i=0; i<25; ++i) {
				EntityRoadhogBullet projectile = new EntityRoadhogBullet(world, player, hand.ordinal());
				EntityHelper.setAim(projectile, player, player.rotationPitch, player.rotationYawHead, 60, 19F, hand, 10, 0);
				world.spawnEntity(projectile);
			}
			ModSoundEvents.ROADHOG_SHOOT_0.playSound(player, world.rand.nextFloat()+0.5F, world.rand.nextFloat()/3+0.8f);
			this.subtractFromCurrentAmmo(player, 1);
			if (world.rand.nextInt(25) == 0)
				player.getHeldItem(hand).damageItem(1, player);
			this.setCooldown(player, 26);
		}
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityLivingBase player, EnumHand hand) {
		// secondary fire
		if (!world.isRemote && this.canUse(player, true, hand, false) && !TickHandler.hasHandler(player, Identifier.ROADHOG_HEALING)) {
			EntityRoadhogScrap projectile = new EntityRoadhogScrap(world, player, hand.ordinal());
			EntityHelper.setAim(projectile, player, player.rotationPitch, player.rotationYawHead, 60, 0F, hand, 10, 0);
			world.spawnEntity(projectile);
			ModSoundEvents.ROADHOG_SHOOT_1.playSound(player, world.rand.nextFloat()+0.5F, world.rand.nextFloat()/3+0.8f);
			this.subtractFromCurrentAmmo(player, 1);
			if (world.rand.nextInt(25) == 0)
				player.getHeldItem(hand).damageItem(1, player);
			this.setCooldown(player, 26);
		}

		return super.onItemRightClick(world, player, hand);
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isSelected) {
		super.onUpdate(stack, world, entity, slot, isSelected);

		if (isSelected && entity instanceof EntityLivingBase && ((EntityLivingBase)entity).getHeldItemMainhand() == stack) {	
			EntityLivingBase player = (EntityLivingBase) entity;

			// hook
			if (!world.isRemote && hero.ability2.isSelected(player) && 
					this.canUse(player, true, EnumHand.MAIN_HAND, true)) { 
				EntityRoadhogHook projectile = new EntityRoadhogHook(world, player, EnumHand.OFF_HAND.ordinal());
				EntityHelper.setAim(projectile, player, player.rotationPitch, player.rotationYawHead, 60, 0F, EnumHand.OFF_HAND, 10, 0);
				world.spawnEntity(projectile);
				ModSoundEvents.ROADHOG_HOOK_THROW.playSound(player, world.rand.nextFloat()+0.5F, world.rand.nextFloat()/3+0.8f);
			}
			// health
			else if (!world.isRemote && (player.getHeldItemOffhand() == null || player.getHeldItemOffhand().isEmpty()) && 
					hero.ability1.isSelected(player) && 
					this.canUse(player, true, EnumHand.MAIN_HAND, false) && 
					!TickHandler.hasHandler(player, Identifier.ROADHOG_HEALING)) {
				ModSoundEvents.ROADHOG_HEAL_0.playFollowingSound(player, 1, 1, false);
				ModSoundEvents.ROADHOG_HEAL_1.playFollowingSound(player, 1, 1, false);
				ModSoundEvents.ROADHOG_HEAL_2.playFollowingSound(player, 1, 1, false);
				TickHandler.register(false, HEALING.setEntity(player).setTicks(40));
				Minewatch.network.sendToDimension(new SPacketSimple(74, player, true), world.provider.getDimension());
				player.setHeldItem(EnumHand.OFF_HAND, new ItemStack(ModItems.roadhog_health));
				player.setActiveHand(EnumHand.OFF_HAND);
			}

		}
	}	

}