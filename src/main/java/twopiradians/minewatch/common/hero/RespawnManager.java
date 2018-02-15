package twopiradians.minewatch.common.hero;

import java.util.ArrayList;
import java.util.Collections;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.client.CPacketClientStatus;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.block.teamBlocks.BlockTeamSpawn;
import twopiradians.minewatch.common.entity.hero.EntityHero;
import twopiradians.minewatch.common.tileentity.TileEntityTeamSpawn;
import twopiradians.minewatch.common.util.EntityHelper;
import twopiradians.minewatch.common.util.TickHandler;
import twopiradians.minewatch.common.util.TickHandler.Handler;
import twopiradians.minewatch.common.util.TickHandler.Identifier;
import twopiradians.minewatch.packet.CPacketSimple;
import twopiradians.minewatch.packet.SPacketSimple;

@Mod.EventBusSubscriber
public class RespawnManager {

	public static final Handler DEAD = new Handler(Identifier.DEAD, false) {
		@Override
		@SideOnly(Side.CLIENT)
		public boolean onClientTick() {
			return --ticksLeft <= 0 || (entity != null && !entity.isEntityAlive() && this.identifier != Identifier.DEAD);
		}
		@Override
		public boolean onServerTick() {
			return --ticksLeft <= 0 || (entity != null && !entity.isEntityAlive() && this.identifier != Identifier.DEAD);
		}
		@Override
		@SideOnly(Side.CLIENT)
		public Handler onClientRemove() {
			return super.onClientRemove();
		}
		@Override
		public Handler onServerRemove() {
			respawnHero(entityLiving, entityLiving.world.getScoreboard().getTeam(string));
			return super.onServerRemove();
		}
	};
	
	/**Respawn a player / hero mob*/
	public static void respawnHero(EntityLivingBase entity, @Nullable Team team) {
		if (entity == null || entity.isEntityAlive())
			return;
		
		BlockPos teamSpawn = getTeamSpawn(entity, team);
		
		if (entity instanceof EntityPlayerMP) {
			EntityPlayerMP player = (EntityPlayerMP) entity;
			int dimension = 0;
			MinecraftServer mcServer = player.mcServer;
			World world = mcServer.worldServerForDimension(dimension);
			if (world == null)
				dimension = player.getSpawnDimension();
			else if (!world.provider.canRespawnHere())
				dimension = world.provider.getRespawnDimension((EntityPlayerMP) player);
			if (mcServer.worldServerForDimension(dimension) == null) 
				dimension = 0;
			boolean spawnForced = player.isSpawnForced(dimension);
			BlockPos pos = player.getBedLocation(dimension);
			if (pos == null) 
				pos = player.world.getSpawnPoint();

			// move spawn to team spawn if possible
			if (teamSpawn != null) 
				player.setSpawnPoint(spawnFuzz(teamSpawn, player.world), true);
			player.connection.processClientStatus(new CPacketClientStatus(CPacketClientStatus.State.PERFORM_RESPAWN));
			// move spawn back
			if (teamSpawn != null) {
				player.connection.playerEntity.setSpawnPoint(pos, spawnForced);
				try {
					EnumFacing facing = player.world.getBlockState(teamSpawn).getValue(BlockTeamSpawn.FACING);
					player.connection.playerEntity.rotationYaw = facing.getHorizontalAngle();
					Minewatch.network.sendTo(new SPacketSimple(59, player, false, facing.getHorizontalAngle(), 0, 0), (EntityPlayerMP) player);
				}
				catch (Exception e) {}
			}
		}
		else if (entity instanceof EntityHero && teamSpawn != null && team != null) {
			try {
				EnumFacing facing = entity.world.getBlockState(teamSpawn).getValue(BlockTeamSpawn.FACING);
				teamSpawn = spawnFuzz(teamSpawn, entity.world);
				NBTTagCompound nbt = new NBTTagCompound();
				entity.writeToNBT(nbt);
				nbt.removeTag("UUID");
				EntityHero heroMob = (EntityHero) ((EntityHero)entity).hero.heroClass.getConstructor(World.class).newInstance(entity.world);
				heroMob.readEntityFromNBT(nbt);
				heroMob.setHealth(heroMob.getMaxHealth());
				heroMob.setPositionAndRotation(teamSpawn.getX(), teamSpawn.getY(), teamSpawn.getZ(), facing.getHorizontalAngle(), 0);
				heroMob.rotationYawHead = facing.getHorizontalAngle();
				heroMob.prevRotationYawHead = facing.getHorizontalAngle();
				heroMob.rotationYaw = facing.getHorizontalAngle();
				heroMob.prevRotationYaw = facing.getHorizontalAngle();
				heroMob.setRenderYawOffset(facing.getHorizontalAngle());
				heroMob.prevRenderYawOffset = facing.getHorizontalAngle();
				entity.world.getScoreboard().addPlayerToTeam(heroMob.getCachedUniqueIdString(), team.getRegisteredName());
				if (entity.hasCustomName()) {
					heroMob.setCustomNameTag(entity.getCustomNameTag());
					heroMob.setAlwaysRenderNameTag(entity.getAlwaysRenderNameTag());
				}
				entity.world.spawnEntity(heroMob);
				entity.world.removeEntity(entity);
			}
			catch (Exception e) {
				Minewatch.logger.warn("Unable to respawn hero ("+entity+") at Team Spawn ("+teamSpawn+")");
				entity.world.removeEntity(entity);
			}
		}
	}

	/**Gets the position of an active team spawn that this entity can spawn at*/
	@Nullable
	public static BlockPos getTeamSpawn(EntityLivingBase entity, @Nullable Team team) {
		// check for same team first
		for (BlockPos pos : TileEntityTeamSpawn.teamSpawnPositions.keySet())
			if (entity != null && pos != null && entity.world.getTileEntity(pos) instanceof TileEntityTeamSpawn) {
				TileEntityTeamSpawn te = ((TileEntityTeamSpawn)entity.world.getTileEntity(pos));
				if (te.isActivated() && te.getTeam() != null && te.getTeam().isSameTeam(team))
					return pos;
			}
		// check for no team second
		for (BlockPos pos : TileEntityTeamSpawn.teamSpawnPositions.keySet())
			if (entity != null && pos != null && entity.world.getTileEntity(pos) instanceof TileEntityTeamSpawn) {
				TileEntityTeamSpawn te = ((TileEntityTeamSpawn)entity.world.getTileEntity(pos));
				if (te.isActivated() && te.getTeam() == null)
					return pos;
			}
		return null;
	}

	/**Applies spawn fuzz to a position (randomly positioning in the spawn radius)*/
	public static BlockPos spawnFuzz(BlockPos pos, World world) {
		if (pos != null && world.getTileEntity(pos) instanceof TileEntityTeamSpawn) {
			TileEntityTeamSpawn te = (TileEntityTeamSpawn) world.getTileEntity(pos);
			int radius = te.getSpawnRadius();
			ArrayList<BlockPos> positions = new ArrayList<BlockPos>();
			// add all possible xz positions
			for (int x=-radius; x<=radius; ++x) 
				for (int z=-radius; z<=radius; ++z)
					positions.add(pos.add(x, 0, z));
			Collections.shuffle(positions);
			for (BlockPos newPos : positions) {
				// while within radius - check for valid and move up or return
				while (newPos.getDistance(te.getPos().getX(), te.getPos().getY(), te.getPos().getZ()) <= radius+1) {
					if (EntityHelper.isValidTeleportLocation(newPos, world)) // valid spot found
						return newPos;
					newPos = newPos.up();
				}
			}
		}
		Minewatch.logger.warn("Unable to spawn at Team Spawn ("+pos+")");
		return pos == null ? BlockPos.ORIGIN : pos.up();
	}
	
	@SubscribeEvent
	public static void registerHeroMobs(LivingDeathEvent event) {
		// register dead heroes directly
		if (isRespawnableHero(event.getEntityLiving()) &&
				!event.getEntityLiving().world.isRemote && !TickHandler.hasHandler(event.getEntityLiving(), Identifier.DEAD)) {
			TickHandler.register(false, DEAD.setEntity(event.getEntityLiving()).setTicks(20).setString(event.getEntityLiving().getTeam().getRegisteredName()));
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void registerPlayers(GuiOpenEvent event) {
		// register dead players when they try to open GuiGameOver (in case they cancel respawn after death)
		if (event.getGui() instanceof GuiGameOver) {
			event.setCanceled(true);
			if (Minecraft.getMinecraft().player.isDead) {
				Minewatch.network.sendToServer(new CPacketSimple(12, false, Minecraft.getMinecraft().player));
			}
		}
	}

	/**Can this hero respawn with Team Spawn*/
	public static boolean isRespawnableHero(Entity entity) {
		return entity instanceof EntityHero && entity.getTeam() != null;
	}

	/**Can this player respawn with Team Spawn*/
	public static boolean isRespawnablePlayer(Entity entity) {
		return entity instanceof EntityPlayerMP;
	}
	
}