package twopiradians.minewatch.common.entity;

import java.util.UUID;

import com.google.common.base.Optional;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.client.particle.ParticleCircle;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.item.weapon.ItemMercyWeapon;

public class EntityMercyBeam extends Entity {

	private static final DataParameter<Optional<UUID>> PLAYER = EntityDataManager.<Optional<UUID>>createKey(EntityMercyBeam.class, DataSerializers.OPTIONAL_UNIQUE_ID);
	private static final DataParameter<Integer> TARGET = EntityDataManager.<Integer>createKey(EntityMercyBeam.class, DataSerializers.VARINT);
	private static final DataParameter<Boolean> HEAL = EntityDataManager.<Boolean>createKey(EntityMercyBeam.class, DataSerializers.BOOLEAN);
	public EntityPlayer player;
	public EntityLivingBase target;
	public boolean prevHeal;
	@SideOnly(Side.CLIENT)
	public ParticleCircle particleStaff;
	@SideOnly(Side.CLIENT)
	public ParticleCircle particleTarget;

	public EntityMercyBeam(World worldIn) {
		super(worldIn);
		this.setSize(0.1f, 0.1f);
		this.ignoreFrustumCheck = true;
	}

	public EntityMercyBeam(World worldIn, EntityPlayer player, EntityLivingBase target) {
		super(worldIn);
		this.setNoGravity(true);
		this.player = player;
		this.target = target;
		this.dataManager.set(PLAYER, Optional.of(player.getPersistentID()));
		this.dataManager.set(TARGET, target.getEntityId());
		this.dataManager.set(HEAL, !Minewatch.keys.rmb(player));
		this.setPosition(target.posX, target.posY+target.height/2, target.posZ);
		this.prevHeal = this.isHealing();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean isInRangeToRenderDist(double distance) {
		return true;
	}
	
	public boolean isHealing() {
		return this.dataManager.get(HEAL);
	}

	@Override
	public void onUpdate() {		
		super.onUpdate();

		// set player on client
		if (this.world.isRemote && player == null && this.dataManager.get(PLAYER).isPresent())
			player = this.world.getPlayerEntityByUUID(this.dataManager.get(PLAYER).get());
		// set target on client
		if (this.world.isRemote && target == null && this.dataManager.get(TARGET) != 0) {
			Entity entity = this.world.getEntityByID(this.dataManager.get(TARGET));
			if (entity instanceof EntityLivingBase)
				target = (EntityLivingBase) entity;
		}

		// kill if player/target is null/dead, player is not holding staff, target is more than 15 blocks from player, 
		// 	 player cannot see target, or player is not holding right/left click
		if (!this.world.isRemote && (player == null || player.isDead || target == null || target.isDead || 
				(!Minewatch.keys.rmb(player) && !Minewatch.keys.lmb(player)) ||
				((player.getHeldItemMainhand() == null || player.getHeldItemMainhand().getItem() != EnumHero.MERCY.weapon || 
				!ItemMercyWeapon.isStaff(player.getHeldItemMainhand())) &&
						(player.getHeldItemOffhand() == null || player.getHeldItemOffhand().getItem() != EnumHero.MERCY.weapon || 
						!ItemMercyWeapon.isStaff(player.getHeldItemOffhand()))) || 
				Math.sqrt(player.getDistanceSqToEntity(target)) > 15 || !player.canEntityBeSeen(target)))
			this.setDead();
		else if (target != null && player != null && !world.isRemote) {
			this.setPosition(target.posX, target.posY+target.height/2, target.posZ);
			this.prevHeal = this.isHealing();
			this.dataManager.set(HEAL, !Minewatch.keys.rmb(player));
		}
	}

	@Override
	protected void entityInit() {
		this.getDataManager().register(PLAYER, Optional.absent());
		this.getDataManager().register(TARGET, Integer.valueOf(0));
		this.getDataManager().register(HEAL, true);
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) {}
}
