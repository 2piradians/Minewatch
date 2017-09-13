package twopiradians.minewatch.common.entity;

import java.util.List;
import java.util.UUID;

import com.google.common.base.Optional;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.client.particle.ParticleCircle;
import twopiradians.minewatch.common.hero.EnumHero;

public class EntityMercyBeam extends Entity {

	private static final DataParameter<Optional<UUID>> PLAYER = EntityDataManager.<Optional<UUID>>createKey(EntityMercyBeam.class, DataSerializers.OPTIONAL_UNIQUE_ID);
	public EntityPlayer player;
	public EntityLivingBase target;
	@SideOnly(Side.CLIENT)
	public ParticleCircle particleStaff;
	@SideOnly(Side.CLIENT)
	public ParticleCircle particleTarget;

	public EntityMercyBeam(World worldIn) {
		super(worldIn);
		this.setSize(0.1f, 0.1f);
		this.ignoreFrustumCheck = true;
	}

	public EntityMercyBeam(World worldIn, EntityPlayer player) {
		super(worldIn);
		this.setNoGravity(true);
		this.player = player;
		this.dataManager.set(PLAYER, Optional.of(player.getPersistentID()));
		this.position();
	}
	
	@Override
	@SideOnly(Side.CLIENT)
    public boolean isInRangeToRenderDist(double distance) {
       return true;
    }

	public void position() {
		if (player != null) {
			this.rotationPitch++;
			this.rotationYaw++;
			Vec3d look = player.getLookVec().scale(3);
			this.setPosition(player.posX+look.xCoord, player.posY+player.eyeHeight+look.yCoord, player.posZ+look.zCoord);
			
			List<Entity> list = this.world.getEntitiesWithinAABBExcludingEntity(player, player.getEntityBoundingBox().expandXyz(10));
			for (Entity entity : list)
				if (entity != this && entity != player && entity instanceof EntityLivingBase)
					target = (EntityLivingBase) entity;
			if (target != null) 
				this.setPosition(target.posX, target.posY+target.height/2, target.posZ);
		}
	}

	@Override
	public void onUpdate() {		
		super.onUpdate();

		// set player on client
		if (this.world.isRemote && player == null && this.dataManager.get(PLAYER).isPresent())
			player = this.world.getPlayerEntityByUUID(this.dataManager.get(PLAYER).get());

		// kill if player is null or not holding staff
		if (!this.world.isRemote && player == null/* || 
				(!this.world.isRemote && player != null && (!Minewatch.keys.rmb(player) && !Minewatch.keys.lmb(player)))*/ ||
				(((player.getHeldItemMainhand() == null || player.getHeldItemMainhand().getItem() != EnumHero.MERCY.weapon) &&
						(player.getHeldItemOffhand() == null || player.getHeldItemOffhand().getItem() != EnumHero.MERCY.weapon))))
			this.setDead();
		else 
			this.position();
		
		/*if (this.world.isRemote && player != null &&
				(((player.getHeldItemMainhand() != null && player.getHeldItemMainhand().getItem() == EnumHero.MERCY.weapon) ||
						(player.getHeldItemOffhand() != null && player.getHeldItemOffhand().getItem() == EnumHero.MERCY.weapon))) &&
				this.world.rand.nextInt(5) == 0) {
			EnumHand hand = player.getHeldItemOffhand() != null && player.getHeldItemOffhand().getItem() == EnumHero.MERCY.weapon 
					? EnumHand.OFF_HAND : EnumHand.MAIN_HAND;
			Vec3d vec = EntityMWThrowable.getShootingPos(player, player.rotationPitch, player.rotationYaw, hand);
			Minewatch.proxy.spawnParticlesSmoke(world, vec.xCoord, vec.yCoord, vec.zCoord, 0xCCC382, 0xFFFAE6, 1, 5);
		}*/
	}

	@Override
	protected void entityInit() {
		this.getDataManager().register(PLAYER, Optional.absent());
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) {}
}
