package twopiradians.minewatch.common.item.weapon;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.EntityBastionBullet;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.tickhandler.TickHandler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Handler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Identifier;
import twopiradians.minewatch.common.util.EntityHelper;
import twopiradians.minewatch.common.util.Handlers;
import twopiradians.minewatch.packet.SPacketSimple;

public class ItemBastionGun extends ItemMWWeapon {

	public static final Handler TURRET = new Handler(Identifier.BASTION_TURRET, true) {
		@SideOnly(Side.CLIENT)
		@Override
		public boolean onClientTick() {
			if (entityLiving != null && entityLiving.getHeldItemMainhand() != null && 
					entityLiving.getHeldItemMainhand().getItem() == EnumHero.BASTION.weapon && 
					EnumHero.BASTION.playersUsingAlt.contains(entityLiving.getPersistentID())) {
				// prevent movement
				Handler handler = TickHandler.getHandler(entityLiving, Identifier.PREVENT_MOVEMENT);
				if (handler == null)
					TickHandler.register(true, Handlers.PREVENT_MOVEMENT.setEntity(entityLiving).setTicks(ticksLeft));
				else
					handler.ticksLeft = this.ticksLeft;

				return false;
			}
			else if (EnumHero.BASTION.playersUsingAlt.contains(entityLiving.getPersistentID())) {
				EnumHero.BASTION.playersUsingAlt.remove(entityLiving.getPersistentID());
				EnumHero.BASTION.reloadSound = ModSoundEvents.bastionReload;
			}

			return super.onClientTick();
		}
		@Override
		public boolean onServerTick() {
			if (entityLiving != null && entityLiving.getHeldItemMainhand() != null && 
					entityLiving.getHeldItemMainhand().getItem() == EnumHero.BASTION.weapon && 
					EnumHero.BASTION.playersUsingAlt.contains(entityLiving.getPersistentID())) {
				// prevent movement
				Handler handler = TickHandler.getHandler(entityLiving, Identifier.PREVENT_MOVEMENT);
				if (handler == null)
					TickHandler.register(false, Handlers.PREVENT_MOVEMENT.setEntity(entityLiving).setTicks(ticksLeft));
				else
					handler.ticksLeft = this.ticksLeft;

				return false;
			}
			else if (EnumHero.BASTION.playersUsingAlt.contains(entityLiving.getPersistentID())) {
				EnumHero.BASTION.playersUsingAlt.remove(entityLiving.getPersistentID());
				EnumHero.BASTION.reloadSound = ModSoundEvents.bastionReload;
			}

			return super.onServerTick();
		}
	};

	public ItemBastionGun() {
		super(40);
		this.savePlayerToNBT = true;
		MinecraftForge.EVENT_BUS.register(this);//TODO
		/*this.addPropertyOverride(new ResourceLocation("pull"), new IItemPropertyGetter() {
			@SideOnly(Side.CLIENT)
			public float apply(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn) {
				return entityIn == null ? 0.0F : (!(entityIn.getActiveItemStack().getItem() instanceof ItemHanzoBow) ? 0.0F :
					(float)(stack.getMaxItemUseDuration() - entityIn.getItemInUseCount()) / 10.0F);
			}
		});*/
	}

	@Override
	public void onItemLeftClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand) { 
		// shoot
		if (this.canUse(player, true, hand, false)) {
			boolean turret = hero.playersUsingAlt.contains(player.getPersistentID());
			if (!world.isRemote) {
				EntityBastionBullet bullet = new EntityBastionBullet(world, player, turret ? 2 : hand.ordinal());
				if (turret)
					EntityHelper.setAim(bullet, player, player.rotationPitch, player.rotationYaw, -1, 1.5F, null, 20, 0);
				else
					EntityHelper.setAim(bullet, player, player.rotationPitch, player.rotationYaw, -1, 0.6F, hand, 12, 0.43f);
				world.spawnEntity(bullet);
				world.playSound(null, player.posX, player.posY, player.posZ, 
						turret ? ModSoundEvents.bastionShoot1 : ModSoundEvents.bastionShoot0, SoundCategory.PLAYERS, world.rand.nextFloat()+0.5F, 
						world.rand.nextFloat()/3+0.8f);	
				this.subtractFromCurrentAmmo(player, 1);
				if (world.rand.nextInt(25) == 0)
					player.getHeldItem(hand).damageItem(1, player);
				if (!turret && !player.getCooldownTracker().hasCooldown(this))
					player.getCooldownTracker().setCooldown(this, 3);
			}
		}
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isSelected) {
		super.onUpdate(stack, world, entity, slot, isSelected);

		if (isSelected && entity instanceof EntityPlayer) {	
			EntityPlayer player = (EntityPlayer) entity;

			// reconfigure
			if (!world.isRemote && hero.ability2.isSelected(player) && 
					this.canUse(player, true, EnumHand.MAIN_HAND, true)) {
				boolean turret = false;
				if (hero.playersUsingAlt.contains(player.getPersistentID())) {
					hero.playersUsingAlt.remove(player.getPersistentID());
					hero.reloadSound = ModSoundEvents.bastionReload;
				}
				else {
					hero.playersUsingAlt.add(player.getPersistentID());
					hero.reloadSound = ModSoundEvents.bastionTurretReload;
					turret = true;
				}
				if (turret) 
					TickHandler.register(false, TURRET.setEntity(player).setTicks(10));
				Minewatch.network.sendToAll(new SPacketSimple(31, turret, player));
				player.getCooldownTracker().setCooldown(this, turret ? 20 : 10);
				hero.ability2.keybind.setCooldown(player, turret ? 20 : 10, true);
				this.setCurrentAmmo(player, this.getMaxAmmo(player), EnumHand.MAIN_HAND);
			}

		}
	}	

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void hideOffhand(RenderSpecificHandEvent event) {
		if (event.getHand() == EnumHand.OFF_HAND && 
				EnumHero.BASTION.playersUsingAlt.contains(Minewatch.proxy.getClientUUID()) &&
				Minecraft.getMinecraft().player.getHeldItemMainhand() != null && 
				Minecraft.getMinecraft().player.getHeldItemMainhand().getItem() == EnumHero.BASTION.weapon)
			event.setCanceled(true);
	}

}