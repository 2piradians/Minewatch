package twopiradians.minewatch.common.hero;

import java.util.ArrayList;
import java.util.Collections;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntityShulker;
import net.minecraft.entity.monster.EntitySlime;
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
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.block.teamBlocks.BlockTeamSpawn;
import twopiradians.minewatch.common.config.Config;
import twopiradians.minewatch.common.entity.EntityLivingBaseMW;
import twopiradians.minewatch.common.tileentity.TileEntityTeamSpawn;
import twopiradians.minewatch.common.util.EntityHelper;
import twopiradians.minewatch.common.util.TickHandler;
import twopiradians.minewatch.common.util.TickHandler.Handler;
import twopiradians.minewatch.common.util.TickHandler.Identifier;
import twopiradians.minewatch.packet.CPacketSimple;
import twopiradians.minewatch.packet.SPacketSimple;

@Mod.EventBusSubscriber
public class RespawnManager {

	/**entity = dead respawning entity, @Nullable string = entity's team*/
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
			respawnEntity(entityLiving, entityLiving.world.getScoreboard().getTeam(string), false);
			return super.onServerRemove();
		}
	};
	
	/**Respawn a player / mob*/
	public static void respawnEntity(EntityLivingBase entity, @Nullable Team team, boolean allowAlive) {
		if (entity == null || (entity.isEntityAlive() && !allowAlive))
			return;
		Minewatch.logger.info("respawn: "+entity.ticksExisted); // TODO
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
		else if (entity instanceof EntityLivingBase && teamSpawn != null && team != null) {
			try {
				entity.world.removeEntity(entity);
				EnumFacing facing = entity.world.getBlockState(teamSpawn).getValue(BlockTeamSpawn.FACING);
				teamSpawn = spawnFuzz(teamSpawn, entity.world);
				NBTTagCompound nbt = entity.serializeNBT();
				nbt.removeTag("UUID");
				nbt.removeTag("UUIDMost");
				nbt.removeTag("UUIDLeast");
				EntityLivingBase respawnEntity = (EntityLivingBase) EntityList.createEntityFromNBT(nbt, entity.world);
				respawnEntity.setHealth(respawnEntity.getMaxHealth());
				respawnEntity.setPositionAndRotation(teamSpawn.getX()+0.5d, teamSpawn.getY(), teamSpawn.getZ()+0.5d, facing.getHorizontalAngle(), 0);
				respawnEntity.rotationYawHead = facing.getHorizontalAngle();
				respawnEntity.prevRotationYawHead = facing.getHorizontalAngle();
				respawnEntity.rotationYaw = facing.getHorizontalAngle();
				respawnEntity.prevRotationYaw = facing.getHorizontalAngle();
				respawnEntity.setRenderYawOffset(facing.getHorizontalAngle());
				respawnEntity.prevRenderYawOffset = facing.getHorizontalAngle();
				entity.world.getScoreboard().addPlayerToTeam(respawnEntity.getCachedUniqueIdString(), team.getRegisteredName());
				if (entity.hasCustomName()) {
					respawnEntity.setCustomNameTag(entity.getCustomNameTag());
					respawnEntity.setAlwaysRenderNameTag(entity.getAlwaysRenderNameTag());
				}
				respawnEntity.clearActivePotions();
				respawnEntity.extinguish();
				if (respawnEntity instanceof EntityShulker)
					((EntityShulker)respawnEntity).setAttachmentPos(teamSpawn);
				entity.world.spawnEntity(respawnEntity);
			}
			catch (Exception e) {
				Minewatch.logger.warn("Unable to respawn entity ("+entity+") at Team Spawn ("+teamSpawn+")", e);
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
	public static void respawnPlayers(PlayerRespawnEvent event) {
		// respawn with method if custom death screen disabled
		if (!event.player.world.isRemote && !Config.customDeathScreen && 
				!TickHandler.hasHandler(event.player, Identifier.DEAD)) {
			event.player.addTag(Minewatch.MODID+": respawned");
			event.player.setHealth(0);Minewatch.logger.info("respawnevent: "+event.player.ticksExisted); // TODO
			TickHandler.register(false, RespawnManager.DEAD.setEntity(event.player).setTicks(0).setString(event.player.getTeam() != null ? event.player.getTeam().getRegisteredName() : null).setBoolean(true));
			TickHandler.unregister(false, TickHandler.getHandler(event.player, Identifier.DEAD));
		}
	}
	
	@SubscribeEvent
	public static void registerMobs(LivingDeathEvent event) {
		// register dead mobs directly
		if (isRespawnableEntity(event.getEntityLiving()) &&
				!event.getEntityLiving().world.isRemote && !TickHandler.hasHandler(event.getEntityLiving(), Identifier.DEAD)) {
			TickHandler.register(false, DEAD.setEntity(event.getEntityLiving()).setTicks(0).setString(event.getEntityLiving().getTeam().getRegisteredName()));
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void registerPlayers(GuiOpenEvent event) {
		// register dead players when they try to open GuiGameOver (in case they cancel respawn after death)
		if (event.getGui() instanceof GuiGameOver && Config.customDeathScreen) {
			event.setCanceled(true);
			if (Minecraft.getMinecraft().player.isDead) {
				Minewatch.network.sendToServer(new CPacketSimple(12, false, Minecraft.getMinecraft().player));
			}
		}
	}

	/**Can this non-player entity respawn with Team Spawn*/
	public static boolean isRespawnableEntity(Entity entity) {
		return entity instanceof EntityLivingBase && !(entity instanceof EntityLivingBaseMW) && 
				!(entity instanceof EntityArmorStand) && entity.getTeam() != null && 
				!(entity instanceof EntitySlime && !((EntitySlime)entity).isSmallSlime()) &&  // only allow small slimes to respawn
				entity.isNonBoss();
	}

	/**Can this player respawn with Team Spawn*/
	public static boolean isRespawnablePlayer(Entity entity) {
		return entity instanceof EntityPlayerMP;
	}
	
}