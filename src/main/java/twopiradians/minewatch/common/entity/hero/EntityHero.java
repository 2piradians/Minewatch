package twopiradians.minewatch.common.entity.hero;

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
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.common.entity.hero.ai.EntityHeroAIHurtByTarget;
import twopiradians.minewatch.common.entity.hero.ai.EntityHeroAINearestAttackableTarget;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.item.armor.ItemMWArmor;

public class EntityHero extends EntityMob {

	public EnumHero hero;

	public EntityHero(World worldIn) {
		this(worldIn, null);
	}

	public EntityHero(World worldIn, @Nullable EnumHero hero) {
		super(worldIn);
		if (hero != null)
			this.hero = hero;
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.32D);
	}

	@Override
	protected void initEntityAI() {
		this.tasks.addTask(1, new EntityAISwimming(this));
		this.tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, 1.0D));
		this.tasks.addTask(7, new EntityAIWanderAvoidWater(this, 1.0D, 0.0F));
		this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
		this.tasks.addTask(8, new EntityAILookIdle(this));
		this.targetTasks.addTask(1, new EntityHeroAIHurtByTarget(this, true, new Class[0]));
		this.targetTasks.addTask(2, new EntityHeroAINearestAttackableTarget(this, EntityLivingBase.class, true));
	}

	@Override
	protected void entityInit() {
		super.entityInit();

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
		
		//if (this.getAttackTarget() != null) 
			//this.getLookHelper().setLookPosition(this.getAttackTarget().posX, this.getAttackTarget().posY+this.getAttackTarget().getEyeHeight(), this.getAttackTarget().posZ, 30, 30);

		// update items and armor
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
	protected boolean canDropLoot() {
		return true;
	}

	/**Overridden to make public*/
	@Override
	public void jump() {
		super.jump();
	}

}