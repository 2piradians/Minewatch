package twopiradians.minewatch.common.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.tickhandler.TickHandler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Identifier;

public class EntityMeiCrystal extends EntityMW {

	public EntityMeiCrystal(World worldIn) {
		this(worldIn, null);
	}

	public EntityMeiCrystal(World worldIn, EntityLivingBase throwerIn) {
		super(worldIn, throwerIn, -1);
		this.setSize(1.8f, 2.3f);
		this.lifetime = Integer.MAX_VALUE;
		if (this.getThrower() != null) {
			this.rotationYaw = this.getThrower().rotationYaw;
			this.rotationPitch = 0;
			this.prevRotationYaw = this.rotationYaw;
			this.prevRotationPitch = this.rotationPitch;
			this.setPositionAndUpdate(this.getThrower().posX, this.getThrower().posY, this.getThrower().posZ);
		}
	}
	
	@Override
	public void notifyDataManagerChange(DataParameter<?> key) {		
		super.notifyDataManagerChange(key);
		
		if (!this.isDead) {
			this.rotationYaw = this.getThrower().rotationYaw;
			this.rotationPitch = 0;
			this.prevRotationYaw = this.rotationYaw;
			this.prevRotationPitch = this.rotationPitch;
			this.setPosition(this.getThrower().posX, this.getThrower().posY, this.getThrower().posZ);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean isInRangeToRenderDist(double distance){
		return distance < 2000;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		
		if (!this.world.isRemote && (this.getThrower() == null || 
				!TickHandler.hasHandler(this.getThrower(), Identifier.MEI_CRYSTAL)))
			this.setDead();
		else if (this.getThrower() != null) {
			this.rotationPitch = 0;
			this.setPosition(this.getThrower().posX, this.getThrower().posY, this.getThrower().posZ);
		}
	}

	@Override
	protected void onImpact(RayTraceResult result) {}

}