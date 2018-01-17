package twopiradians.minewatch.common.entity.ability;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.CommonProxy.EnumParticle;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.EntityMW;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.util.EntityHelper;
import twopiradians.minewatch.common.util.TickHandler;
import twopiradians.minewatch.common.util.TickHandler.Handler;
import twopiradians.minewatch.common.util.TickHandler.Identifier;
import twopiradians.minewatch.packet.SPacketSimple;

public class EntityWidowmakerHook extends EntityMW {

	public EntityWidowmakerHook(World worldIn) {
		this(worldIn, null, -1);
	}

	public EntityWidowmakerHook(World worldIn, EntityLivingBase throwerIn, int hand) {
		super(worldIn, throwerIn, hand);
		this.setSize(0.25f, 0.25f);
		this.lifetime = 1200;
		this.setNoGravity(true);
	}


	@Override
	public void onUpdate() {
		super.onUpdate();
	}
	
	@Override
	protected void onImpactMoveToHitPosition(RayTraceResult result) {
		super.onImpactMoveToHitPosition(result);
	}

	@Override
	public void onImpact(RayTraceResult result) {
		super.onImpact(result);

	}

}