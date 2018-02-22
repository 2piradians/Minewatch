package twopiradians.minewatch.common.tileentity;

import java.util.HashMap;

import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.client.gui.heroSelect.GuiHeroSelect;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.common.CommonProxy.EnumGui;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.EntityLivingBaseMW;
import twopiradians.minewatch.common.util.TickHandler;
import twopiradians.minewatch.common.util.TickHandler.Handler;
import twopiradians.minewatch.common.util.TickHandler.Identifier;

public class TileEntityTeamSpawn extends TileEntityTeam {

	public static Handler IN_RANGE = new Handler(Identifier.TEAM_SPAWN_IN_RANGE, false) {
		@Override
		@SideOnly(Side.CLIENT)
		public boolean onClientTick() {
			if (Minecraft.getMinecraft().currentScreen == null && 
					KeyBind.CHANGE_HERO.keyBind.isKeyDown())
				Minewatch.proxy.openGui(EnumGui.HERO_SELECT);
			return super.onClientTick();
		}
		@Override
		@SideOnly(Side.CLIENT)
		public Handler onClientRemove() {
			if (Minecraft.getMinecraft().currentScreen instanceof GuiHeroSelect)
				Minecraft.getMinecraft().displayGuiScreen(null);
			return super.onServerRemove();
		}
	};
	public static HashMap<BlockPos, String> teamSpawnPositions = Maps.newHashMap();
	public static final int MAX_SPAWN_RADIUS = 20;

	private int spawnRadius;
	public int ticksExisted;

	public TileEntityTeamSpawn() {
		super();
	}

	@Override
	public void update() {
		super.update();

		if (this.isActivated()) {

			// copied from MobSpawnerBaseLogic particles
			if (this.world.isRemote && this.world.rand.nextBoolean()) {
				double d3 = (double)((float)pos.getX() + 0.5f + (this.world.rand.nextFloat()-0.5f)*0.8f);
				double d4 = (double)((float)pos.getY() + 0.5f + (this.world.rand.nextFloat()-0.5f)*0.8f);
				double d5 = (double)((float)pos.getZ() + 0.5f + (this.world.rand.nextFloat()-0.5f)*0.8f);
				this.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d3, d4, d5, 0.0D, 0.0D, 0.0D, new int[0]);
				this.world.spawnParticle(EnumParticleTypes.FLAME, d3, d4, d5, 0.0D, 0.0D, 0.0D, new int[0]);
			}

			// regeneration
			if (!this.world.isRemote && this.ticksExisted % 10 == 0) 
				for (EntityLivingBase entity : world.getEntitiesWithinAABB(EntityLivingBase.class, 
						new AxisAlignedBB(pos.add(spawnRadius, spawnRadius, spawnRadius), 
								pos.add(-spawnRadius, -spawnRadius, -spawnRadius)))) 
					if (entity != null && entity.isEntityAlive() && !(entity instanceof EntityLivingBaseMW) &&
					(this.getTeam() == null || this.getTeam().isSameTeam(entity.getTeam())))
						entity.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 12, 5, false, false));

			// hero selection overlay TODO config
			if (this.world.isRemote && this.ticksExisted % 10 == 0) 
				for (EntityPlayer player : world.playerEntities)
					if (player == Minewatch.proxy.getClientPlayer() && player.isEntityAlive() && !player.isSpectator() &&
					(this.getTeam() == null || this.getTeam().isSameTeam(player.getTeam())) && 
					player.getDistance(pos.getX(), pos.getY(), pos.getZ()) <= spawnRadius) {
						Handler handler = TickHandler.getHandler(player, Identifier.TEAM_SPAWN_IN_RANGE);
						if (handler == null)
							TickHandler.register(true, IN_RANGE.setEntity(player).setTicks(12));
						else
							handler.ticksLeft = 12;
					}
		}

		++this.ticksExisted;
	}

	public int getSpawnRadius() {
		return spawnRadius;
	}

	public void setSpawnRadius(int spawnRadius) {
		if (!world.isRemote) {
			spawnRadius = MathHelper.clamp(spawnRadius, 0, MAX_SPAWN_RADIUS);
			this.spawnRadius = spawnRadius;
			this.needsToBeUpdated = true;
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt = super.writeToNBT(nbt);

		nbt.setInteger("spawnRadius", spawnRadius);

		return nbt;
	}

	/**Read extra info from nbt - like team and activated status*/
	protected void readExtraNBT(NBTTagCompound nbt) {
		super.readExtraNBT(nbt);

		if (nbt.hasKey("spawnRadius"))
			this.spawnRadius = MathHelper.clamp(nbt.getInteger("spawnRadius"), 0, MAX_SPAWN_RADIUS);
	}

	@Override
	public HashMap<BlockPos, String> getPositions() {
		return teamSpawnPositions;
	}

	@Override
	public void copyToNewTile(TileEntityTeam te) {
		super.copyToNewTile(te);

		if (te instanceof TileEntityTeamSpawn)
			((TileEntityTeamSpawn)te).spawnRadius = spawnRadius;
	}

}