package twopiradians.minewatch.common.entity.hero;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.common.config.Config;
import twopiradians.minewatch.common.entity.hero.ai.EntityHeroAIHurtByTarget;
import twopiradians.minewatch.common.entity.hero.ai.EntityHeroAINearestAttackableTarget;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.item.armor.ItemMWArmor;
import twopiradians.minewatch.common.item.weapon.ItemMWWeapon;

public class EntityHero extends EntityMob {

	public static final DataParameter<Integer> SKIN = EntityDataManager.<Integer>createKey(EntityHero.class, DataSerializers.VARINT);
	public EnumHero hero;
	@Nullable
	public EntityLivingBase healTarget;

	public EntityHero(World worldIn) {
		this(worldIn, null);
	}

	public EntityHero(World worldIn, @Nullable EnumHero hero) {
		super(worldIn);
		if (hero != null) {
			this.hero = hero;
			if (Config.mobRandomSkins)
				this.getDataManager().set(SKIN, this.rand.nextInt(this.hero.skinInfo.length));
		}
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
		this.targetTasks.addTask(1, new EntityHeroAIHurtByTarget(this, true, new Class[0]));
		this.targetTasks.addTask(2, new EntityHeroAINearestAttackableTarget(this, EntityLivingBase.class, true));
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataManager.register(SKIN, 0);

		for (KeyBind key : KeyBind.values())
			this.dataManager.register(key.datamanager, false);
	}

	@Override
	public void notifyDataManagerChange(DataParameter<?> key) {
		super.notifyDataManagerChange(key);
		for (KeyBind keybind : KeyBind.values())
			if (key.getId() == keybind.datamanager.getId()) {
				keybind.setKeyDown(this, this.dataManager.get(keybind.datamanager));
				//System.out.println("Updating "+keybind+" to "+this.dataManager.get(keybind.datamanager)); // TODO
			}
	}

	@Override
	public void onUpdate() {
		super.onUpdate(); this.setGlowing(true); // FIXME

		if (!this.isEntityAlive())
			return;

		// reset to default skin if random skins disabled
		if (!world.isRemote && !Config.mobRandomSkins && this.getDataManager().get(SKIN) != 0)
			this.getDataManager().set(SKIN, 0);

		// make body follow head
		if (this.getHeldItemMainhand() != null && 
				this.getHeldItemMainhand().getItem() instanceof ItemMWWeapon &&
				(KeyBind.LMB.isKeyDown(this) || KeyBind.RMB.isKeyDown(this))) {
			this.renderYawOffset = this.rotationYawHead;
		}

		// clear dead target
		if (this.getAttackTarget() != null && !this.getAttackTarget().isEntityAlive())
			this.setAttackTarget(null);

		// update items and armor
		this.setLeftHanded(false);
		for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
			ItemStack stack = this.getItemStackFromSlot(slot);
			if (stack == null || stack.isEmpty()) {
				stack = new ItemStack(hero.getEquipment(slot));
				this.setItemStackToSlot(slot, stack);
			}

			if (stack != null && stack.getItem() instanceof ItemMWArmor)
				((ItemMWArmor)stack.getItem()).onArmorTick(world, this, stack);
			if (stack != null)
				stack.getItem().onUpdate(stack, world, this, 0, stack == this.getHeldItemMainhand());
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		return compound;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
	}
	
	@Override
	public boolean getCanSpawnHere() {
        return super.getCanSpawnHere();
    }

	@Override
	protected boolean isValidLightLevel() {
		return Config.mobSpawn == 1 ? super.isValidLightLevel() : this.rand.nextInt(30) == 0;
	}

	@Override
	public float getBlockPathWeight(BlockPos pos) {
		return Config.mobSpawn == 1 ? super.getBlockPathWeight(pos) : 0;
	}

	@Override
    public int getMaxSpawnedInChunk() {
        return 1;
    }
	
	@Override
	protected boolean canDropLoot() {
		return true;
	}

	/**Overridden to make public*/
	@Override
	public void jump() {
		super.jump();
	}

}