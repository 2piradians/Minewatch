package twopiradians.minewatch.common.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.CommonProxy.EnumParticle;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.tickhandler.TickHandler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Handler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Identifier;
import twopiradians.minewatch.common.util.EntityHelper;

public class EntityAnaBullet extends EntityMW {

	private static final DataParameter<Boolean> HEAL = EntityDataManager.<Boolean>createKey(EntityAnaBullet.class, DataSerializers.BOOLEAN);
	public static Handler DAMAGE = new Handler(Identifier.ANA_DAMAGE, false) {
		@Override
		@SideOnly(Side.CLIENT)
		public boolean onClientTick() {
			if (this.ticksLeft == 18)
				Minewatch.proxy.spawnParticlesCustom(EnumParticle.CIRCLE, this.entity.world, this.entity, 
						0xCA91DA, 0xB886A2, 1.0f, 18, (float)this.number, (float)this.number-1, 0, 0.1f);
			if (this.ticksLeft % 8 == 0)
				Minewatch.proxy.spawnParticlesCustom(EnumParticle.ANA_DAMAGE, this.entity.world, this.entity, 
						0xFFFFFF, 0xFFFFFF, 1.0f, 8, (float)this.number+8, (float)this.number-5, this.entity.world.rand.nextFloat(), 0);
			return --ticksLeft <= 0 || (entity != null && entity.isDead);
		}
		@Override
		public boolean onServerTick() {
			// damage
			if (this.ticksLeft % 4 == 0 && this.entity != null && this.entityLiving != null) {
				this.entity.hurtResistantTime = 0;
				EntityHelper.attemptDamage(this.entityLiving, this.entity, 15, true);
			}
			return --ticksLeft <= 0 || (entity != null && entity.isDead);
		}
	};

	public EntityAnaBullet(World worldIn) {
		this(worldIn, null, false);

	}

	public EntityAnaBullet(World worldIn, EntityLivingBase throwerIn, boolean heal) {
		super(worldIn, throwerIn);
		if (!worldIn.isRemote)
			this.getDataManager().set(HEAL, heal);
		this.setNoGravity(true);
		this.setSize(0.1f, 0.1f);
		this.lifetime = 10; 
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.getDataManager().register(HEAL, false);
	}

	@Override
	public void onUpdate() {
		this.isFriendly = this.getDataManager().get(HEAL);

		super.onUpdate();

		if (this.world.isRemote) 
			EntityHelper.spawnTrailParticles(this, 10, 0.05d, this.isFriendly ? 0xFFFCC7 : 0x9361D4, 
					this.isFriendly ? 0xEAE7B9 : 0xEBBCFF, 0.5f, 8, 1);
	}

	@Override
	protected void onImpact(RayTraceResult result) {
		super.onImpact(result);

		float size = result.entityHit == null ? 0 : Math.min(result.entityHit.height, result.entityHit.width)*8f;

		// heal
		if (this.isFriendly) {
			EntityHelper.attemptImpact(this, result.entityHit, -75, true);
			// particles / sounds
			if (this.world.isRemote && result.entityHit != null) {
				Minewatch.proxy.spawnParticlesCustom(EnumParticle.ANA_HEAL, world, result.entityHit, 0xFFFFFF, 0xFFFFFF, 0.8f, 
						30+world.rand.nextInt(10), size, size/1.5f, world.rand.nextFloat(), (world.rand.nextFloat()-0.5f)/5f);
				Minewatch.proxy.spawnParticlesCustom(EnumParticle.ANA_HEAL, world, result.entityHit, 0xFFFFFF, 0xFFFFFF, 0.7f, 
						30+world.rand.nextInt(10), size, size/1.5f, world.rand.nextFloat(), (world.rand.nextFloat()-0.5f)/5f);
				this.world.playSound((EntityPlayer) this.getThrower(), this.getThrower().getPosition(), ModSoundEvents.anaHeal, SoundCategory.PLAYERS, 
						0.3f, result.entityHit.world.rand.nextFloat()/2+1.5f);
				result.entityHit.playSound(ModSoundEvents.anaHeal, 0.2f, result.entityHit.world.rand.nextFloat()/2+1.5f);
			}
		}
		// damage
		else if (result.entityHit != null) {
			EntityHelper.moveToEntityHit(this, result.entityHit);
			EntityHelper.attemptImpact(this, result.entityHit, 0, false);
			if (!TickHandler.hasHandler(result.entityHit, Identifier.GENJI_DEFLECT)) {
				TickHandler.register(this.world.isRemote, DAMAGE.setTicks(18).setEntity(result.entityHit).setEntityLiving(this.getThrower()).setNumber(size));
				this.setDead();
			}
		}
	}
}
