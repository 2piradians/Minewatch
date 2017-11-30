package twopiradians.minewatch.common.item.weapon;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Rotations;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.IThrowableEntity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.EntityMW;
import twopiradians.minewatch.common.entity.ability.EntityJunkratMine;
import twopiradians.minewatch.common.entity.hero.EntityHero;
import twopiradians.minewatch.common.entity.projectile.EntityGenjiShuriken;
import twopiradians.minewatch.common.entity.projectile.EntityHanzoArrow;
import twopiradians.minewatch.common.hero.Ability;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.tickhandler.TickHandler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Handler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Identifier;
import twopiradians.minewatch.common.util.EntityHelper;
import twopiradians.minewatch.packet.SPacketSimple;

public class ItemGenjiShuriken extends ItemMWWeapon {

	public static final Handler DEFLECT = new Handler(Identifier.GENJI_DEFLECT, true) {
		@Override
		public boolean onServerTick() {
			AxisAlignedBB aabb = entityLiving.getEntityBoundingBox().expandXyz(4);
			List<Entity> list = entityLiving.world.getEntitiesWithinAABBExcludingEntity(entityLiving, aabb);
			for (Entity entity : list) 
				if (!(entity instanceof EntityArrow))
					ItemGenjiShuriken.deflect(entityLiving, entity);
			return super.onServerTick();
		}

		@Override
		public Handler onServerRemove() {
			EnumHero.GENJI.ability1.keybind.setCooldown(entityLiving, 160, false);
			return super.onServerRemove();
		}
	};
	/**bool represents if a mob was killed while striking - to prevent setting cooldown*/
	public static final Handler STRIKE = new Handler(Identifier.GENJI_STRIKE, true) {
		@Override
		@SideOnly(Side.CLIENT)
		public boolean onClientTick() {
			// block while striking
			if (entityLiving.getHeldItemMainhand() != null && entityLiving.getHeldItemMainhand().getItem() instanceof ItemGenjiShuriken &&
					entityLiving.getActiveItemStack() != entityLiving.getHeldItemMainhand()) 
				entityLiving.setActiveHand(EnumHand.MAIN_HAND);

			if (entityLiving == Minecraft.getMinecraft().player)
				SPacketSimple.move(entityLiving, 1.5f, true, true);

			if (entityLiving instanceof EntityPlayerSP)
				((EntityPlayerSP)entityLiving).movementInput.sneak = false;
			double x = entityLiving instanceof EntityPlayer ? ((EntityPlayer)entityLiving).chasingPosX : entityLiving.posX;
			double y = entityLiving instanceof EntityPlayer ? ((EntityPlayer)entityLiving).chasingPosY : entityLiving.posY;
			double z = entityLiving instanceof EntityPlayer ? ((EntityPlayer)entityLiving).chasingPosZ : entityLiving.posZ;
			double prevX = entityLiving instanceof EntityPlayer ? ((EntityPlayer)entityLiving).prevChasingPosX : entityLiving.prevPosX;
			double prevY = entityLiving instanceof EntityPlayer ? ((EntityPlayer)entityLiving).prevChasingPosY : entityLiving.prevPosY;
			double prevZ = entityLiving instanceof EntityPlayer ? ((EntityPlayer)entityLiving).prevChasingPosZ : entityLiving.prevPosZ;
			int numParticles = (int) ((Math.abs(x-prevX)+Math.abs(y-prevY)+Math.abs(z-prevZ))*20d);
			for (int i=0; i<numParticles; ++i) {
				Minewatch.proxy.spawnParticlesTrail(entityLiving.world, 
						prevX+(x-prevX)*i/numParticles+(entityLiving.world.rand.nextDouble()-0.5d)*0.1d-0.2d, 
						prevY+(y-prevY)*i/numParticles+entityLiving.height/2+(entityLiving.world.rand.nextDouble()-0.5d)*0.1d-0.2d, 
						prevZ+(z-prevZ)*i/numParticles+(entityLiving.world.rand.nextDouble()-0.5d)*0.1d, 
						0, 0, 0, 0xAAB85A, 0xF4FCB6, 1, 7, 0, 1);
				Minewatch.proxy.spawnParticlesTrail(entityLiving.world, 
						prevX+(x-prevX)*i/numParticles+(entityLiving.world.rand.nextDouble()-0.5d)*0.1d+0.2d, 
						prevY+(y-prevY)*i/numParticles+entityLiving.height/2+(entityLiving.world.rand.nextDouble()-0.5d)*0.1d+0.2d, 
						prevZ+(z-prevZ)*i/numParticles+(entityLiving.world.rand.nextDouble()-0.5d)*0.1d, 
						0, 0, 0, 0xAAB85A, 0xF4FCB6, 1, 7, 0, 1);
			}
			return super.onClientTick();
		}

		@Override
		public boolean onServerTick() {
			if (entityLiving instanceof EntityHero)
				SPacketSimple.move(entityLiving, 1.5f, true, true);
			
			// block while striking
			if (entityLiving.getHeldItemMainhand() != null && entityLiving.getHeldItemMainhand().getItem() instanceof ItemGenjiShuriken &&
					entityLiving.getActiveItemStack() != entityLiving.getHeldItemMainhand()) 
				entityLiving.setActiveHand(EnumHand.MAIN_HAND);

			AxisAlignedBB aabb = entityLiving.getEntityBoundingBox().expandXyz(1.3d);
			List<Entity> list = entityLiving.world.getEntitiesWithinAABBExcludingEntity(entityLiving, aabb);

			for (Entity entityCollided : list) 
				if (entityCollided instanceof EntityLivingBase)
					if (EntityHelper.attemptDamage(entityLiving, entityCollided, 50, false, false))
						ModSoundEvents.HURT.playSound(entityCollided, 0.3f, entityCollided.world.rand.nextFloat()/2+0.75f);
			return super.onServerTick();
		}
		@SideOnly(Side.CLIENT)
		@Override
		public Handler onClientRemove() {
			entityLiving.resetActiveHand();
			return super.onClientRemove();
		}
		@Override
		public Handler onServerRemove() {
			entityLiving.resetActiveHand();
			if (!this.bool)
				EnumHero.GENJI.ability2.keybind.setCooldown(entityLiving, 160, false);
			return super.onServerRemove();
		}
	};

	public static final Handler SWORD_CLIENT = new Handler(Identifier.GENJI_SWORD, true) {};

	public ItemGenjiShuriken() {
		super(40);
		this.saveEntityToNBT = true;
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
	public void onItemLeftClick(ItemStack stack, World world, EntityLivingBase player, EnumHand hand) { 
		// throw single shuriken TODO make triple w/ delay
		if (!player.world.isRemote && this.canUse(player, true, hand, false) && player.ticksExisted % 3 == 0) {
			EntityGenjiShuriken shuriken = new EntityGenjiShuriken(player.world, player, hand.ordinal());
			EntityHelper.setAim(shuriken, player, player.rotationPitch, player.rotationYawHead, 60, 1, hand, 15, 0.6f);
			player.world.spawnEntity(shuriken);
			ModSoundEvents.GENJI_SHOOT.playSound(player, world.rand.nextFloat()+0.5F, player.world.rand.nextFloat()/2+0.75f);
			this.subtractFromCurrentAmmo(player, 1, hand);
			if (this.getCurrentAmmo(player) % 3 == 0 &&	this.getCurrentAmmo(player) != this.getMaxAmmo(player))
				this.setCooldown(player, 15);
			if (player.world.rand.nextInt(24) == 0)
				player.getHeldItem(hand).damageItem(1, player);
		}
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityLivingBase player, EnumHand hand) {
		// throw triple shuriken
		if (!player.world.isRemote && this.canUse(player, true, hand, false)) {
			for (int i = 0; i < Math.min(3, this.getCurrentAmmo(player)); i++) {
				EntityGenjiShuriken shuriken = new EntityGenjiShuriken(player.world, player, hand.ordinal());
				EntityHelper.setAim(shuriken, player, player.rotationPitch, player.rotationYawHead + (1 - i)*8, 60, 1, hand, 15, 0.6f);
				player.world.spawnEntity(shuriken);
			}
			ModSoundEvents.GENJI_SHOOT.playSound(player, 1, player.world.rand.nextFloat()/2+0.75f);
			this.subtractFromCurrentAmmo(player, 3, hand);
			if (world.rand.nextInt(8) == 0)
				player.getHeldItem(hand).damageItem(1, player);
			this.setCooldown(player, 15);
		}

		return new ActionResult(EnumActionResult.PASS, player.getHeldItem(hand));
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isSelected) {
		super.onUpdate(stack, world, entity, slot, isSelected);

		if (entity instanceof EntityLivingBase) {
			EntityLivingBase player = (EntityLivingBase)entity;

			// deflect
			if (isSelected && !world.isRemote && hero.ability1.isSelected(player) && 
					this.canUse(player, true, EnumHand.MAIN_HAND, true)) {
				if (player instanceof EntityHero)
					SPacketSimple.move(player, 1.8d, false, true);
				Minewatch.network.sendToDimension(new SPacketSimple(4, player, true, 40, 0, 0), world.provider.getDimension());
				TickHandler.register(false, DEFLECT.setEntity(player).setTicks(40));
				TickHandler.register(false, Ability.ABILITY_USING.setEntity(player).setTicks(40).setAbility(hero.ability1));
				ModSoundEvents.GENJI_DEFLECT.playFollowingSound(entity, 1, 1, false);
			}

			// strike
			if (isSelected && !world.isRemote && hero.ability2.isSelected(player) &&
					this.canUse(player, true, EnumHand.MAIN_HAND, true)) {
				TickHandler.register(false, STRIKE.setEntity(player).setTicks(8));
				TickHandler.register(false, Ability.ABILITY_USING.setEntity(player).setTicks(8).setAbility(hero.ability2));
				Minewatch.network.sendToDimension(new SPacketSimple(3, (EntityLivingBase) entity, true), world.provider.getDimension());
				ModSoundEvents.GENJI_STRIKE.playFollowingSound(entity, 1, 1, false);
			}
		}
	}	

	private static boolean deflect(EntityLivingBase player, Entity entity) {
		if (canDeflect(player, entity)) {
			double velScale = Math.sqrt(entity.motionX*entity.motionX + 
					entity.motionY*entity.motionY + entity.motionZ*entity.motionZ)*1.2d;
			entity.motionX = player.getLookVec().xCoord*velScale;	
			entity.motionY = player.getLookVec().yCoord*velScale;	
			entity.motionZ = player.getLookVec().zCoord*velScale;		

			if (entity instanceof EntityArrow && !(entity instanceof EntityHanzoArrow)) { 
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
			if (entity instanceof EntityMW)
				((EntityMW)entity).lifetime *= 2;
			if (entity instanceof EntityJunkratMine) {
				((EntityJunkratMine)entity).ignoreImpacts.remove(RayTraceResult.Type.ENTITY);
				((EntityJunkratMine)entity).deflectTimer = 40;
			}
			DataParameter<Rotations> data = EntityHelper.getVelocityParameter(entity);
			if (data != null) 
				entity.getDataManager().set(data, new Rotations((float)entity.motionX, (float)entity.motionY, (float)entity.motionZ));
			else
				entity.velocityChanged = true;
			ModSoundEvents.GENJI_DEFLECT_HIT.playSound(player, 0.6f, player.world.rand.nextFloat()/6f+0.9f);
			Minewatch.network.sendToDimension(new SPacketSimple(13, player, false), player.world.provider.getDimension());
			return true;
		}
		return false;
	}
	
	public static boolean canDeflect(EntityLivingBase player, Entity entity) {
		return entity != null && !entity.isDead && (entity instanceof EntityArrow || entity instanceof EntityThrowable || 
				entity instanceof IThrowableEntity ||entity instanceof EntityFireball ||
				entity instanceof EntityTNTPrimed) && !entity.onGround &&
				player.getLookVec().dotProduct(new Vec3d(entity.motionX, entity.motionY, entity.motionZ).normalize()) < -0.1d &&
				!(entity instanceof EntityMW && ((((EntityMW)entity).notDeflectible) || 
						!EntityHelper.shouldHit(((EntityMW)entity).getThrower(), player, false)));
	}

	@SubscribeEvent
	public void deflectAttack(LivingAttackEvent event) {
		if (event.getEntity() instanceof EntityLivingBase && !event.getEntity().world.isRemote && 
				TickHandler.hasHandler(event.getEntity(), Identifier.GENJI_DEFLECT)) {
			if (deflect((EntityLivingBase) event.getEntity(), event.getSource().getSourceOfDamage())) 
				event.setCanceled(true);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ArrayList<String> getAllModelLocations(ArrayList<String> locs) {
		locs.add("_sword");
		return super.getAllModelLocations(locs);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public String getModelLocation(ItemStack stack, @Nullable EntityLivingBase entity) {
		return TickHandler.hasHandler(entity, Identifier.GENJI_SWORD) ? "_sword" : "";
	}

}