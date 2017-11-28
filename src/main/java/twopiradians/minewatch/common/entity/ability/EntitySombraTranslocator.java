package twopiradians.minewatch.common.entity.ability;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.CommonProxy.EnumParticle;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.EntityMW;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.util.EntityHelper;

public class EntitySombraTranslocator extends EntityMW {

	private boolean prevOnGround;
	private boolean playedSound;

	public EntitySombraTranslocator(World worldIn) {
		this(worldIn, null);
	}

	public EntitySombraTranslocator(World worldIn, EntityLivingBase throwerIn) {
		super(worldIn, throwerIn, -1);
		this.setSize(0.3f, 0.3f);
		this.lifetime = 1200;
		this.notDeflectible = true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean isInRangeToRenderDist(double distance){
		return distance < 2000;
	}

	@Override
	public void onUpdate() {
		if (this.onGround) {
			this.rotationPitch = 0;
			this.motionX = 0;
			this.motionZ = 0;
		}

		// particles
		if (this.world.isRemote) {
			int color1 = EntityHelper.shouldHit(getThrower(), Minewatch.proxy.getClientPlayer(), false) ? 
					0xFFA84F : 0x9F62E5;
			int color2 = EntityHelper.shouldHit(getThrower(), Minewatch.proxy.getClientPlayer(), false) ? 
					0xAD2512 : 0x8E77BC;
			if (this.onGround)
				Minewatch.proxy.spawnParticlesCustom(EnumParticle.CIRCLE, world, posX, posY, posZ, 0, 0.2f, 0, color1, color2, 1f, 10, 2, 1.5f, 0, 0);
			else 
				EntityHelper.spawnTrailParticles(this, 5, 0, color1, color2, 0.8f, 5, 0.8f);
		}
		
		// prevOnGround and normal particle
		if (prevOnGround != onGround && onGround) {
			if (this.world.isRemote && this.getThrower() == Minewatch.proxy.getClientPlayer())
				ModSoundEvents.SOMBRA_TRANSLOCATOR_LAND.playFollowingSound(this.getThrower(), 1, 1, false);
			if (!this.playedSound) {
				if (this.world.isRemote && this.getThrower() == Minewatch.proxy.getClientPlayer())
					ModSoundEvents.SOMBRA_TRANSLOCATOR_DURING.playFollowingSound(this.getThrower(), 0.8f, 1, false);
				this.playedSound = true;
				if (!this.world.isRemote) 
					this.lifetime = this.ticksExisted + 235;
			}
			if (world.isRemote && this.getThrower() instanceof EntityPlayer && 
					this.getThrower().getPersistentID().equals(Minewatch.proxy.getClientUUID()))
				Minewatch.proxy.spawnParticlesCustom(EnumParticle.SOMBRA_TRANSPOSER, world, this, 0xFFFFFF, 0xFFFFFF, 1, Integer.MAX_VALUE, 1, 1, 0, 0);
		}
		this.prevOnGround = this.onGround;

		// gravity
		this.motionY -= 0.05D;

		// set cooldown when expiring
		if (!this.world.isRemote && this.ticksExisted > lifetime &&
				this.getThrower() != null) 
			EnumHero.SOMBRA.ability2.keybind.setCooldown(this.getThrower(), 80, false);

		super.onUpdate();
	}

	@Override
	protected void onImpact(RayTraceResult result) {
		if (result.typeOfHit == RayTraceResult.Type.BLOCK)
			this.onGround = true;
	}

	protected boolean isValidImpact(RayTraceResult result) {
		return result.typeOfHit == RayTraceResult.Type.BLOCK && result.sideHit == EnumFacing.DOWN;
	}

}