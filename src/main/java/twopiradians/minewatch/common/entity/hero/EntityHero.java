package twopiradians.minewatch.common.entity.hero;

import java.util.Arrays;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.config.Config;
import twopiradians.minewatch.common.entity.hero.ai.EntityHeroAIHurtByTarget;
import twopiradians.minewatch.common.entity.hero.ai.EntityHeroAIMoveToHealthPack;
import twopiradians.minewatch.common.entity.hero.ai.EntityHeroAINearestAttackableTarget;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.item.armor.ItemMWArmor;
import twopiradians.minewatch.common.item.weapon.ItemMWWeapon;
import twopiradians.minewatch.common.util.EntityHelper;
import twopiradians.minewatch.common.util.TickHandler;
import twopiradians.minewatch.common.util.TickHandler.Identifier;

public class EntityHero extends EntityMob {

	public static final DataParameter<Float> ABSORPTION = EntityDataManager.<Float>createKey(EntityHero.class, DataSerializers.FLOAT);
	public static final DataParameter<Integer> SKIN = EntityDataManager.<Integer>createKey(EntityHero.class, DataSerializers.VARINT);
	public EnumHero hero;
	@Nullable
	public EntityLivingBase healTarget;
	public boolean movingToHealthPack;
	public boolean onPack;

	public EntityHero(World worldIn) {
		this(worldIn, null);
	}

	public EntityHero(World worldIn, @Nullable EnumHero hero) {
		super(worldIn);
		if (hero != null) {
			this.hero = hero;
			if (Config.mobRandomSkins && !worldIn.isRemote)
				this.getDataManager().set(SKIN, this.rand.nextInt(this.hero.skinInfo.length));
		}
		Arrays.fill(this.inventoryArmorDropChances, (float) Config.mobEquipmentDropRate);
		Arrays.fill(this.inventoryHandsDropChances, (float) Config.mobEquipmentDropRate);
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.32D);
		this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(32d);
	}

	@Override
	protected void initEntityAI() {
		this.tasks.addTask(1, new EntityAISwimming(this));
		this.tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, 1D));
		this.tasks.addTask(7, new EntityAIWanderAvoidWater(this, 1.0D, 0.0F));
		this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
		this.tasks.addTask(8, new EntityAILookIdle(this));

		this.tasks.addTask(1, new EntityHeroAIMoveToHealthPack(this));
		this.targetTasks.addTask(1, new EntityHeroAIHurtByTarget(this, true, new Class[0]));
		this.targetTasks.addTask(2, new EntityHeroAINearestAttackableTarget(this, EntityLivingBase.class, true));
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataManager.register(SKIN, -1);
		this.dataManager.register(ABSORPTION, 0f);

		for (KeyBind key : KeyBind.values())
			this.dataManager.register(key.datamanager, false);
	}

	@Override
	public void notifyDataManagerChange(DataParameter<?> key) {
		super.notifyDataManagerChange(key);
		for (KeyBind keybind : KeyBind.values())
			if (key.getId() == keybind.datamanager.getId()) 
				keybind.setKeyDown(this, this.dataManager.get(keybind.datamanager));
	}

	@Override
	public void onUpdate() {
		super.onUpdate(); 

		// random hero
		if (this.hero == null && !this.world.isRemote)
			this.spawnRandomHero();

		// set drop chances
		if (this.inventoryArmorDropChances[0] != Config.mobEquipmentDropRate) {
			Arrays.fill(this.inventoryArmorDropChances, (float) Config.mobEquipmentDropRate);
			Arrays.fill(this.inventoryHandsDropChances, (float) Config.mobEquipmentDropRate);
		}

		// stop doing things when dead
		if (!this.isEntityAlive() || hero == null)
			return;

		// make body follow head
		if (this.getHeldItemMainhand() != null && 
				this.getHeldItemMainhand().getItem() instanceof ItemMWWeapon &&
				(KeyBind.LMB.isKeyDown(this) || KeyBind.RMB.isKeyDown(this))) {
			this.renderYawOffset = this.rotationYawHead;
		}

		// clear dead/invalid target
		if (!this.world.isRemote && this.getAttackTarget() != null && 
				(!this.getAttackTarget().isEntityAlive() || !EntityHelper.shouldHit(this, this.getAttackTarget(), false)))
			this.setAttackTarget(null);

		// update items and armor
		this.setLeftHanded(false);
		for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
			ItemStack stack = this.getItemStackFromSlot(slot);
			if ((stack == null || stack.isEmpty()) && hero.getEquipment(slot) != null) {
				stack = new ItemStack(hero.getEquipment(slot));
				this.setItemStackToSlot(slot, stack);
			}

			if (stack != null && stack.getItem() instanceof ItemMWArmor)
				((ItemMWArmor)stack.getItem()).onArmorTick(world, this, stack);
			if (stack != null)
				stack.getItem().onUpdate(stack, world, this, 0, stack == this.getHeldItemMainhand());
		}
	}

	/**Kill this and replace it with a random hero*/
	public void spawnRandomHero() {
		try {
			EnumHero hero = EnumHero.values()[this.rand.nextInt(EnumHero.values().length)];
			EntityHero heroMob = (EntityHero) hero.heroClass.getConstructor(World.class).newInstance(this.world);
			NBTTagCompound nbt = this.serializeNBT();
			nbt.removeTag("UUID");
			nbt.removeTag("UUIDMost");
			nbt.removeTag("UUIDLeast");
			nbt.removeTag("skin");
			heroMob.readFromNBT(nbt);
			if (this.getTeam() != null)
				world.getScoreboard().addPlayerToTeam(heroMob.getCachedUniqueIdString(), this.getTeam().getName());
			for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) 
				heroMob.setItemStackToSlot(slot, ItemStack.EMPTY);
			heroMob.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
			heroMob.setNoAI(this.isAIDisabled());
			if (this.hasCustomName()) {
				heroMob.setCustomNameTag(this.getCustomNameTag());
				heroMob.setAlwaysRenderNameTag(this.getAlwaysRenderNameTag());
			}
			this.world.spawnEntity(heroMob);
		}
		catch (Exception e) {
			Minewatch.logger.error("Minewatch was unable to spawn a random hero, please report this to the authors: ");
			e.printStackTrace();
		}
		this.setDead();
	}
	
	@Override
    @Nullable
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
		this.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 40, 31, false, false));
		
    	return super.onInitialSpawn(difficulty, livingdata);
    }

	/**Overridden to sync absorption with data manager (like players)*/
	@Override
	public void setAbsorptionAmount(float amount) {
		super.setAbsorptionAmount(amount);

		if (amount < 0.0F)
			amount = 0.0F;

		this.getDataManager().set(ABSORPTION, Float.valueOf(amount));
	}

	/**Overridden to sync absorption with data manager (like players)*/
	@Override
	public float getAbsorptionAmount() {
		return ((Float)this.getDataManager().get(ABSORPTION)).floatValue();
	}

	@Override
	protected boolean isValidLightLevel() {
		return this.rand.nextInt((world.isDaytime() && !super.isValidLightLevel()) ? 600 : 300) <= Config.mobSpawnFreq*3 && (Config.mobSpawn == 1 ? super.isValidLightLevel() : true);
	}

	@Override
	public float getBlockPathWeight(BlockPos pos) {
		return Config.mobSpawn == 1 ? super.getBlockPathWeight(pos) : 0;
	}

	@Override
	public int getMaxSpawnedInChunk() {
		return 1;
	}

	/**May be used in the future*/
	public boolean shouldUseAbility() {
		return this.getRNG().nextInt(25) == 0;
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		// still trigger event on client (for health manager)
		if (world.isRemote) {
			net.minecraftforge.common.ForgeHooks.onLivingAttack(this, source, amount);
			return false;
		}

		// prevent same team damage (for non-mw attacks)
		if (source != null && 
				((source.getTrueSource() != null && source.getTrueSource().isOnSameTeam(this)) ||
						(source.getTrueSource() != null && source.getTrueSource().isOnSameTeam(this))))
			return false;
		else
			return super.attackEntityFrom(source, amount);
	}

	@Override
	protected boolean canDespawn() {
		return Config.heroMobsDespawn || this.getTeam() == null;
	}

	/**Overridden to make public for ItemMWArmor genji double jump*/
	@Override
	public void jump() {
		super.jump();
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);

		compound.setInteger("skin", this.getDataManager().get(SKIN));
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);

		if (compound.hasKey("skin") && compound.getInteger("skin") >= 0)
			this.getDataManager().set(SKIN, compound.getInteger("skin"));
	}

	public void lookAtTarget(EntityLivingBase target, float lookYOffset) {
		lookAtTarget(new Vec3d(target.prevPosX, target.prevPosY+target.getEyeHeight()+lookYOffset, target.prevPosZ));
	}

	public void lookAtTarget(Vec3d target) {
		if (!TickHandler.hasHandler(this, Identifier.GENJI_STRIKE) && !TickHandler.hasHandler(this, Identifier.PREVENT_ROTATION)) 
			this.getLookHelper().setLookPosition(target.x, target.y, target.z, 360, 360);
		this.rotationYaw = this.rotationYawHead;
	}

}