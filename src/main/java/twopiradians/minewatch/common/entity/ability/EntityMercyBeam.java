package twopiradians.minewatch.common.entity.ability;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.client.particle.ParticleCustom;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.item.weapon.ItemMercyWeapon;

public class EntityMercyBeam extends Entity {

	private static final DataParameter<Integer> PLAYER = EntityDataManager.<Integer>createKey(EntityMercyBeam.class, DataSerializers.VARINT);
	private static final DataParameter<Integer> TARGET = EntityDataManager.<Integer>createKey(EntityMercyBeam.class, DataSerializers.VARINT);
	private static final DataParameter<Boolean> HEAL = EntityDataManager.<Boolean>createKey(EntityMercyBeam.class, DataSerializers.BOOLEAN);
	public EntityLivingBase player;
	public EntityLivingBase target;
	public boolean prevHeal;
	@SideOnly(Side.CLIENT)
	public ParticleCustom particleStaff;
	@SideOnly(Side.CLIENT)
	public ParticleCustom particleTarget;

	public EntityMercyBeam(World worldIn) {
		this(worldIn, null, null);
	}

	public EntityMercyBeam(World worldIn, EntityLivingBase entity, EntityLivingBase target) {
		super(worldIn);
		this.setSize(0.1f, 0.1f);
		this.ignoreFrustumCheck = true;
		this.setNoGravity(true);
		this.player = entity;
		this.target = target;
		this.prevHeal = this.isHealing();
		if (entity != null && target != null) {
			this.setPosition(target.posX, target.posY+target.height/2, target.posZ);
			this.dataManager.set(PLAYER, entity.getEntityId());
			this.dataManager.set(TARGET, target.getEntityId());
			this.dataManager.set(HEAL, !KeyBind.RMB.isKeyDown(entity));
		}
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
		if (this.world.isRemote && player == null && this.dataManager.get(PLAYER) != -1) {
			Entity entity = this.world.getEntityByID(this.dataManager.get(PLAYER));
			if (entity instanceof EntityLivingBase)
				player = (EntityLivingBase) entity;
		}
		// set target on client
		if (this.world.isRemote && target == null && this.dataManager.get(TARGET) != -1) {
			Entity entity = this.world.getEntityByID(this.dataManager.get(TARGET));
			if (entity instanceof EntityLivingBase)
				target = (EntityLivingBase) entity;
		}

		// kill if player/target is null/dead, player is not holding staff, target is more than 15 blocks from player, 
		// 	 player cannot see target, or player is not holding right/left click
		if (!this.world.isRemote && (player == null || !player.isEntityAlive() || target == null || !target.isEntityAlive() || 
				(!KeyBind.RMB.isKeyDown(player) && !KeyBind.LMB.isKeyDown(player)) ||
				((player.getHeldItemMainhand() == null || player.getHeldItemMainhand().getItem() != EnumHero.MERCY.weapon || 
				!ItemMercyWeapon.isStaff(player.getHeldItemMainhand())) &&
						(player.getHeldItemOffhand() == null || player.getHeldItemOffhand().getItem() != EnumHero.MERCY.weapon || 
						!ItemMercyWeapon.isStaff(player.getHeldItemOffhand()))) || 
				Math.sqrt(player.getDistanceSqToEntity(target)) > 15 || !player.canEntityBeSeen(target)))
			this.setDead();
		else if (target != null && player != null) {
			this.setPosition(target.posX, target.posY+target.height/2, target.posZ);
			this.prevHeal = this.isHealing();
			if (!this.world.isRemote)
				this.dataManager.set(HEAL, !KeyBind.RMB.isKeyDown(player));
		}
	}

	@Override
	protected void entityInit() {
		this.getDataManager().register(PLAYER, -1);
		this.getDataManager().register(TARGET, -1);
		this.getDataManager().register(HEAL, true);
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) {}
}
