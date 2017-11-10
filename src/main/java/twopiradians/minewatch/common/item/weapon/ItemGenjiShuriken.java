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
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
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
import twopiradians.minewatch.common.config.Config;
import twopiradians.minewatch.common.entity.EntityGenjiShuriken;
import twopiradians.minewatch.common.entity.EntityHanzoArrow;
import twopiradians.minewatch.common.entity.EntityJunkratMine;
import twopiradians.minewatch.common.entity.EntityMW;
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
			AxisAlignedBB aabb = player.getEntityBoundingBox().grow(4);
			List<Entity> list = player.world.getEntitiesWithinAABBExcludingEntity(player, aabb);
			for (Entity entity : list) 
				if (!(entity instanceof EntityArrow))
					ItemGenjiShuriken.deflect(player, entity);
			return super.onServerTick();
		}

		@Override
		public Handler onServerRemove() {
			EnumHero.GENJI.ability1.keybind.setCooldown(player, 160, false);
			return super.onServerRemove();
		}
	};
	/**bool represents if a mob was killed while striking - to prevent setting cooldown*/
	public static final Handler STRIKE = new Handler(Identifier.GENJI_STRIKE, true) {
		@Override
		@SideOnly(Side.CLIENT)
		public boolean onClientTick() {
			// block while striking
			if (player.getHeldItemMainhand() != null && player.getHeldItemMainhand().getItem() instanceof ItemGenjiShuriken &&
					player.getActiveItemStack() != player.getHeldItemMainhand()) 
				player.setActiveHand(EnumHand.MAIN_HAND);

			if (player == Minecraft.getMinecraft().player)
				SPacketSimple.move(player, 1.5f, true);

			if (player instanceof EntityPlayerSP)
				((EntityPlayerSP)player).movementInput.sneak = false;
			int numParticles = (int) ((Math.abs(player.chasingPosX-player.prevChasingPosX)+Math.abs(player.chasingPosY-player.prevChasingPosY)+Math.abs(player.chasingPosZ-player.prevChasingPosZ))*20d);
			for (int i=0; i<numParticles; ++i) {
				Minewatch.proxy.spawnParticlesTrail(player.world, 
						player.prevChasingPosX+(player.chasingPosX-player.prevChasingPosX)*i/numParticles+(player.world.rand.nextDouble()-0.5d)*0.1d-0.2d, 
						player.prevChasingPosY+(player.chasingPosY-player.prevChasingPosY)*i/numParticles+player.height/2+(player.world.rand.nextDouble()-0.5d)*0.1d-0.2d, 
						player.prevChasingPosZ+(player.chasingPosZ-player.prevChasingPosZ)*i/numParticles+(player.world.rand.nextDouble()-0.5d)*0.1d, 
						0, 0, 0, 0xAAB85A, 0xF4FCB6, 1, 7, 0, 1);
				Minewatch.proxy.spawnParticlesTrail(player.world, 
						player.prevChasingPosX+(player.chasingPosX-player.prevChasingPosX)*i/numParticles+(player.world.rand.nextDouble()-0.5d)*0.1d+0.2d, 
						player.prevChasingPosY+(player.chasingPosY-player.prevChasingPosY)*i/numParticles+player.height/2+(player.world.rand.nextDouble()-0.5d)*0.1d+0.2d, 
						player.prevChasingPosZ+(player.chasingPosZ-player.prevChasingPosZ)*i/numParticles+(player.world.rand.nextDouble()-0.5d)*0.1d, 
						0, 0, 0, 0xAAB85A, 0xF4FCB6, 1, 7, 0, 1);
				player.chasingPosX = player.prevPosX;
				player.chasingPosY = player.prevPosY;
				player.chasingPosZ = player.prevPosZ;
			}
			return super.onClientTick();
		}

		@Override
		public boolean onServerTick() {
			// block while striking
			if (player.getHeldItemMainhand() != null && player.getHeldItemMainhand().getItem() instanceof ItemGenjiShuriken &&
					player.getActiveItemStack() != player.getHeldItemMainhand()) 
				player.setActiveHand(EnumHand.MAIN_HAND);

			AxisAlignedBB aabb = player.getEntityBoundingBox().grow(1.3d);
			List<Entity> list = player.world.getEntitiesWithinAABBExcludingEntity(player, aabb);

			for (Entity entityCollided : list) 
				if (entityCollided instanceof EntityLivingBase)
					if (((EntityLivingBase)entityCollided).attackEntityFrom(DamageSource.causePlayerDamage(player), 50F*Config.damageScale))
						entityCollided.world.playSound(null, entityCollided.getPosition(), ModSoundEvents.hurt, SoundCategory.PLAYERS, 0.3f, entityCollided.world.rand.nextFloat()/2+0.75f);
			return super.onServerTick();
		}
		@SideOnly(Side.CLIENT)
		@Override
		public Handler onClientRemove() {
			player.resetActiveHand();
			return super.onClientRemove();
		}
		@Override
		public Handler onServerRemove() {
			player.resetActiveHand();
			if (!this.bool)
				EnumHero.GENJI.ability2.keybind.setCooldown(player, 160, false);
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
	public void onItemLeftClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand) { 
		// throw single shuriken TODO make triple w/ delay
		if (!player.world.isRemote && this.canUse(player, true, hand, false) && player.ticksExisted % 3 == 0) {
			EntityGenjiShuriken shuriken = new EntityGenjiShuriken(player.world, player, hand.ordinal());
			EntityHelper.setAim(shuriken, player, player.rotationPitch, player.rotationYaw, 60, 1, hand, 15, 0.6f);
			player.world.spawnEntity(shuriken);
			player.world.playSound(null, player.posX, player.posY, player.posZ, 
					ModSoundEvents.genjiShoot, SoundCategory.PLAYERS, world.rand.nextFloat()+0.5F, 
					player.world.rand.nextFloat()/2+0.75f);	
			this.subtractFromCurrentAmmo(player, 1, hand);
			if (!player.getCooldownTracker().hasCooldown(this) && this.getCurrentAmmo(player) % 3 == 0 &&
					this.getCurrentAmmo(player) != this.getMaxAmmo(player))
				player.getCooldownTracker().setCooldown(this, 15);
			if (player.world.rand.nextInt(24) == 0)
				player.getHeldItem(hand).damageItem(1, player);
		}
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		// throw triple shuriken
		if (!player.world.isRemote && this.canUse(player, true, hand, false)) {
			for (int i = 0; i < Math.min(3, this.getCurrentAmmo(player)); i++) {
				EntityGenjiShuriken shuriken = new EntityGenjiShuriken(player.world, player, hand.ordinal());
				EntityHelper.setAim(shuriken, player, player.rotationPitch, player.rotationYaw + (1 - i)*8, 60, 1, hand, 15, 0.6f);
				player.world.spawnEntity(shuriken);
			}
			player.world.playSound(null, player.posX, player.posY, player.posZ, 
					ModSoundEvents.genjiShoot, SoundCategory.PLAYERS, 1.0f, player.world.rand.nextFloat()/2+0.75f);
			this.subtractFromCurrentAmmo(player, 3, hand);
			if (world.rand.nextInt(8) == 0)
				player.getHeldItem(hand).damageItem(1, player);
			if (!player.getCooldownTracker().hasCooldown(this))
				player.getCooldownTracker().setCooldown(this, 15);
		}

		return new ActionResult(EnumActionResult.PASS, player.getHeldItem(hand));
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isSelected) {
		super.onUpdate(stack, world, entity, slot, isSelected);

		if (entity instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer)entity;

			// deflect
			if (isSelected && !world.isRemote && hero.ability1.isSelected((EntityPlayer) entity) &&
					entity instanceof EntityPlayerMP && this.canUse((EntityPlayer) entity, true, EnumHand.MAIN_HAND, true)) {
				Minewatch.network.sendToDimension(new SPacketSimple(4, true, (EntityPlayer) entity, 40, 0, 0), world.provider.getDimension());
				TickHandler.register(false, DEFLECT.setEntity(player).setTicks(40));
				TickHandler.register(false, Ability.ABILITY_USING.setEntity(player).setTicks(40).setAbility(hero.ability1));
				Minewatch.proxy.playFollowingSound(entity, ModSoundEvents.genjiDeflect, SoundCategory.PLAYERS, 1.0f, 1.0f, false);
			}

			// strike
			if (isSelected && !world.isRemote && hero.ability2.isSelected((EntityPlayer) entity) &&
					entity instanceof EntityPlayerMP && this.canUse((EntityPlayer) entity, true, EnumHand.MAIN_HAND, true)) {
				TickHandler.register(false, STRIKE.setEntity(player).setTicks(8));
				TickHandler.register(false, Ability.ABILITY_USING.setEntity(player).setTicks(8).setAbility(hero.ability2));
				Minewatch.network.sendToDimension(new SPacketSimple(3, true, (EntityPlayer) entity), world.provider.getDimension());
				Minewatch.proxy.playFollowingSound(entity, ModSoundEvents.genjiStrike, SoundCategory.PLAYERS, 1.0f, 1.0f, false);
			}
		}
	}	

	private static boolean deflect(EntityPlayer player, Entity entity) {
		if (entity != null && !entity.isDead && (entity instanceof EntityArrow || entity instanceof EntityThrowable || 
				entity instanceof IThrowableEntity ||entity instanceof EntityFireball ||
				entity instanceof EntityTNTPrimed) &&
				player.getLookVec().dotProduct(new Vec3d(entity.motionX, entity.motionY, entity.motionZ).normalize()) < -0.4d &&
				!(entity instanceof EntityMW && ((((EntityMW)entity).notDeflectible) || 
						!EntityHelper.shouldHit(((EntityMW)entity).getThrower(), player, false)))) {
			double velScale = Math.sqrt(entity.motionX*entity.motionX + 
					entity.motionY*entity.motionY + entity.motionZ*entity.motionZ)*1.2d;
			entity.motionX = player.getLookVec().x*velScale;	
			entity.motionY = player.getLookVec().y*velScale;	
			entity.motionZ = player.getLookVec().z*velScale;		

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
			player.world.playSound(null, player.getPosition(), ModSoundEvents.genjiDeflectHit, SoundCategory.PLAYERS, 0.6f, player.world.rand.nextFloat()/6f+0.9f);
			Minewatch.network.sendToDimension(new SPacketSimple(13, false, player), player.world.provider.getDimension());
			return true;
		}
		return false;
	}

	@SubscribeEvent
	public void deflectAttack(LivingAttackEvent event) {
		if (event.getEntity() instanceof EntityPlayer && !event.getEntity().world.isRemote && 
				TickHandler.hasHandler(event.getEntity(), Identifier.GENJI_DEFLECT)) {
			if (deflect((EntityPlayer) event.getEntity(), event.getSource().getTrueSource())) 
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