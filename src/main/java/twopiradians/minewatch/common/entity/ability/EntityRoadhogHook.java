package twopiradians.minewatch.common.entity.ability;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Event;
import twopiradians.minewatch.common.entity.EntityMW;

public class EntityRoadhogHook extends EntityMW {

	public EntityRoadhogHook(World worldIn) {
		this(worldIn, null, -1);
	}

	public EntityRoadhogHook(World worldIn, EntityLivingBase throwerIn, int hand) {
		super(worldIn, throwerIn, hand);
		this.setSize(0.9f, 0.9f);
		this.lifetime = 10;
		this.setNoGravity(true);
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
	}

	@Override
	public void onImpact(RayTraceResult result) {
		super.onImpact(result);


	}

}