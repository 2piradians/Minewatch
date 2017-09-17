package twopiradians.minewatch.common.item.weapon;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.IThrowableEntity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.EntityGenjiShuriken;
import twopiradians.minewatch.common.hero.Ability;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.tickhandler.TickHandler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Handler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Identifier;
import twopiradians.minewatch.packet.SPacketTriggerAbility;

public class ItemGenjiShuriken extends ItemMWWeapon {

	public static final Handler DEFLECT_SERVER = new Handler(Identifier.GENJI_DEFLECT) {
		@Override
		public boolean onServerTick() {
			AxisAlignedBB aabb = player.getEntityBoundingBox().expandXyz(4);
			List<Entity> list = player.worldObj.getEntitiesWithinAABBExcludingEntity(player, aabb);
			for (Entity entity : list) 
				if (!(entity instanceof EntityArrow))
					ItemGenjiShuriken.deflect(player, entity);
			return super.onServerTick();
		}

		@Override
		public void onRemove() {
			EnumHero.GENJI.ability1.keybind.setCooldown(player, 160, false);
		}
	};
	public static final Handler STRIKE_CLIENT = new Handler(Identifier.GENJI_STRIKE) {
		@Override
		@SideOnly(Side.CLIENT)
		public boolean onClientTick() {
			// block while striking
			if (player.getHeldItemMainhand() != null && player.getHeldItemMainhand().getItem() instanceof ItemGenjiShuriken &&
					player.getActiveItemStack() != player.getHeldItemMainhand()) 
				player.setActiveHand(EnumHand.MAIN_HAND);

			if (player == Minecraft.getMinecraft().thePlayer)
				SPacketTriggerAbility.move(player, 1.5f, true);

			if (player instanceof EntityPlayerSP)
				((EntityPlayerSP)player).movementInput.sneak = false;
			int numParticles = (int) ((Math.abs(player.chasingPosX-player.prevChasingPosX)+Math.abs(player.chasingPosY-player.prevChasingPosY)+Math.abs(player.chasingPosZ-player.prevChasingPosZ))*20d);
			for (int i=0; i<numParticles; ++i) {
				Minewatch.proxy.spawnParticlesTrail(player.worldObj, 
						player.prevChasingPosX+(player.chasingPosX-player.prevChasingPosX)*i/numParticles+(player.worldObj.rand.nextDouble()-0.5d)*0.1d-0.2d, 
						player.prevChasingPosY+(player.chasingPosY-player.prevChasingPosY)*i/numParticles+player.height/2+(player.worldObj.rand.nextDouble()-0.5d)*0.1d-0.2d, 
						player.prevChasingPosZ+(player.chasingPosZ-player.prevChasingPosZ)*i/numParticles+(player.worldObj.rand.nextDouble()-0.5d)*0.1d, 
						0, 0, 0, 0xAAB85A, 0xF4FCB6, 1, 7, 1);
				Minewatch.proxy.spawnParticlesTrail(player.worldObj, 
						player.prevChasingPosX+(player.chasingPosX-player.prevChasingPosX)*i/numParticles+(player.worldObj.rand.nextDouble()-0.5d)*0.1d+0.2d, 
						player.prevChasingPosY+(player.chasingPosY-player.prevChasingPosY)*i/numParticles+player.height/2+(player.worldObj.rand.nextDouble()-0.5d)*0.1d+0.2d, 
						player.prevChasingPosZ+(player.chasingPosZ-player.prevChasingPosZ)*i/numParticles+(player.worldObj.rand.nextDouble()-0.5d)*0.1d, 
						0, 0, 0, 0xAAB85A, 0xF4FCB6, 1, 7, 1);
				player.chasingPosX = player.prevPosX;
				player.chasingPosY = player.prevPosY;
				player.chasingPosZ = player.prevPosZ;
			}
			return super.onClientTick();
		}

		@Override
		public void onRemove() {
			player.resetActiveHand();
		}
	};
	public static final Handler STRIKE_SERVER = new Handler(Identifier.GENJI_STRIKE) {
		@Override
		public boolean onServerTick() {
			// block while striking
			if (player.getHeldItemMainhand() != null && player.getHeldItemMainhand().getItem() instanceof ItemGenjiShuriken &&
					player.getActiveItemStack() != player.getHeldItemMainhand()) 
				player.setActiveHand(EnumHand.MAIN_HAND);

			AxisAlignedBB aabb = player.getEntityBoundingBox().expandXyz(1.3d);
			List<Entity> list = player.worldObj.getEntitiesWithinAABBExcludingEntity(player, aabb);

			for (Entity entityCollided : list) 
				if (entityCollided instanceof EntityLivingBase) 
					if (((EntityLivingBase)entityCollided).attackEntityFrom(DamageSource.causePlayerDamage(player), 50F*ItemMWWeapon.damageScale))
						entityCollided.worldObj.playSound(null, entityCollided.getPosition(), ModSoundEvents.hurt, SoundCategory.PLAYERS, 0.3f, entityCollided.worldObj.rand.nextFloat()/2+0.75f);
			return super.onServerTick();
		}

		@Override
		public void onRemove() {
			player.resetActiveHand();
			EnumHero.GENJI.ability2.keybind.setCooldown(player, 160, false);
		}
	};
	public static final Handler SWORD_CLIENT = new Handler(Identifier.GENJI_SWORD) {};

	public ItemGenjiShuriken() {
		super(40);
		this.savePlayerToNBT = true;
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		return EnumAction.BLOCK;
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 10;
	}

	@Override
	public void onItemLeftClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand) { 
		// throw single shuriken
		if (!player.worldObj.isRemote && this.canUse(player, true, hand) && player.ticksExisted % 3 == 0) {
			EntityGenjiShuriken shuriken = new EntityGenjiShuriken(player.worldObj, player);
			shuriken.setAim(player, player.rotationPitch, player.rotationYaw, 3F, 1.0F, 1F, hand, false);
			player.worldObj.spawnEntityInWorld(shuriken);
			player.worldObj.playSound(null, player.posX, player.posY, player.posZ, 
					ModSoundEvents.genjiShoot, SoundCategory.PLAYERS, world.rand.nextFloat()+0.5F, 
					player.worldObj.rand.nextFloat()/2+0.75f);	
			this.subtractFromCurrentAmmo(player, 1, hand);
			if (!player.getCooldownTracker().hasCooldown(this) && this.getCurrentAmmo(player) % 3 == 0 &&
					this.getCurrentAmmo(player) != this.getMaxAmmo(player))
				player.getCooldownTracker().setCooldown(this, 15);
			if (player.worldObj.rand.nextInt(24) == 0)
				player.getHeldItem(hand).damageItem(1, player);
		}
	}

	@Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand hand) {
		// throw triple shuriken
		if (!playerIn.worldObj.isRemote && this.canUse(playerIn, true, hand)) {
			for (int i = 0; i < Math.min(3, this.getCurrentAmmo(playerIn)); i++) {
				EntityGenjiShuriken shuriken = new EntityGenjiShuriken(playerIn.worldObj, playerIn);
				shuriken.setAim(playerIn, playerIn.rotationPitch, playerIn.rotationYaw + (1 - i)*8, 3F, 1.0F, 0F, hand, false);
				playerIn.worldObj.spawnEntityInWorld(shuriken);
			}
			playerIn.worldObj.playSound(null, playerIn.posX, playerIn.posY, playerIn.posZ, 
					ModSoundEvents.genjiShoot, SoundCategory.PLAYERS, 1.0f, playerIn.worldObj.rand.nextFloat()/2+0.75f);
			this.subtractFromCurrentAmmo(playerIn, 3, hand);
			if (worldIn.rand.nextInt(8) == 0)
				playerIn.getHeldItem(hand).damageItem(1, playerIn);
			if (!playerIn.getCooldownTracker().hasCooldown(this))
				playerIn.getCooldownTracker().setCooldown(this, 15);
		}

		return new ActionResult(EnumActionResult.PASS, playerIn.getHeldItem(hand));
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isSelected) {
		super.onUpdate(stack, world, entity, slot, isSelected);

		if (entity instanceof EntityPlayer && this.canUse((EntityPlayer) entity, false, null)) {
			EntityPlayer player = (EntityPlayer)entity;

			// deflect
			if (isSelected && !world.isRemote && hero.ability1.isSelected((EntityPlayer) entity) &&
					entity instanceof EntityPlayerMP) {
				Minewatch.network.sendToAll(new SPacketTriggerAbility(4, (EntityPlayer) entity, 40, 0, 0));
				TickHandler.register(false, DEFLECT_SERVER.setEntity(player).setTicks(40));
				TickHandler.register(false, Ability.ABILITY_USING.setEntity(player).setTicks(40));
				world.playSound(null, entity.getPosition(), ModSoundEvents.genjiDeflect, SoundCategory.PLAYERS, 1.0f, 1.0f);
			}

			// strike
			if (isSelected && !world.isRemote && hero.ability2.isSelected((EntityPlayer) entity) &&
					entity instanceof EntityPlayerMP) {
				TickHandler.register(false, STRIKE_SERVER.setEntity(player).setTicks(8));
				TickHandler.register(false, Ability.ABILITY_USING.setEntity(player).setTicks(8));
				Minewatch.network.sendToAll(new SPacketTriggerAbility(3, true, (EntityPlayer) entity));
				world.playSound(null, entity.getPosition(), ModSoundEvents.genjiStrike, SoundCategory.PLAYERS, 2f, 1.0f);
			}
		}
	}	

	private static boolean deflect(EntityPlayer player, Entity entity) {
		if ((entity instanceof EntityArrow || entity instanceof EntityThrowable || 
				entity instanceof IThrowableEntity ||entity instanceof EntityFireball ||
				entity instanceof EntityTNTPrimed) &&
				player.getLookVec().dotProduct(new Vec3d(entity.motionX, entity.motionY, entity.motionZ)) < -0.1d) {
			double velScale = Math.sqrt(entity.motionX*entity.motionX + 
					entity.motionY*entity.motionY + entity.motionZ*entity.motionZ)*1.2d;
			entity.motionX = player.getLookVec().xCoord*velScale;	
			entity.motionY = player.getLookVec().yCoord*velScale;	
			entity.motionZ = player.getLookVec().zCoord*velScale;		
			entity.velocityChanged = true;

			if (entity instanceof EntityArrow) { 
				EntityArrow ent = (EntityArrow) entity;
				ent.shootingEntity = player;
				// undo motion slowing done after event in EntityArrow#onHit
				ent.motionX *= -1/0.10000000149011612D;
				ent.motionY *= -1/0.10000000149011612D;
				ent.motionZ *= -1/0.10000000149011612D;
			}
			if (entity instanceof EntityFireball) {
				EntityFireball ent = (EntityFireball) entity;
				ent.accelerationX = ent.motionX * 0.05d;
				ent.accelerationY = ent.motionY * 0.05d;
				ent.accelerationZ = ent.motionZ * 0.05d;
				ent.shootingEntity = player;
			}
			if (entity instanceof IThrowableEntity)
				((IThrowableEntity) entity).setThrower(player);

			player.worldObj.playSound(null, player.getPosition(), ModSoundEvents.genjiDeflectHit, SoundCategory.PLAYERS, 0.6f, player.worldObj.rand.nextFloat()/6f+0.9f);
			player.setActiveHand(EnumHand.MAIN_HAND);
			return true;
		}
		return false;
	}

	@SubscribeEvent
	public void deflectAttack(LivingAttackEvent event) {
		if (event.getEntity() instanceof EntityPlayer && !event.getEntity().worldObj.isRemote && 
				TickHandler.getHandler(event.getEntity(), Identifier.GENJI_DEFLECT) != null) {
			if (deflect((EntityPlayer) event.getEntity(), event.getSource().getSourceOfDamage()))
				event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void onKill(LivingDeathEvent event) {
		// remove strike cooldown if killed by Genji
		if (event.getEntityLiving() != null && !event.getEntityLiving().worldObj.isRemote && 
				event.getSource().getEntity() instanceof EntityPlayer) 
			hero.ability2.keybind.setCooldown((EntityPlayer) event.getSource().getEntity(), 0, false);
	}

}
