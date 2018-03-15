package twopiradians.minewatch.common.hero;

import java.util.ArrayList;
import java.util.Collections;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntityShulker;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.client.CPacketClientStatus;
import net.minecraft.potion.PotionEffect;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.client.config.GuiUtils;
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

	/**entity = dead respawning entity, entityLiving = (client) spectating entity, @Nullable string = entity's team, bool = changed render view entity this tick, number = gamemode to set to afterwards, obj = TileEntityTeamSpawn to spawn at (or null)*/
	public static final Handler DEAD = new Handler(Identifier.DEAD, false) {
		@Override
		@SideOnly(Side.CLIENT) 
		public boolean onClientTick() {
			// prevent screen shake / stutter
			player.deathTime = 0;
			player.setHealth(0.000001f);
			// update fov once in a while bc it doesn't do it properly while spectating
			if (this.ticksLeft % 20 == 0)
				Minewatch.proxy.updateFOV();
			Minecraft.getMinecraft().gameSettings.thirdPersonView = 1;

			// copy positions of spectating entity
			player.setPosition(entityLiving.posX, entityLiving.posY, entityLiving.posZ);
			player.prevPosX = entityLiving.prevPosX;
			player.prevPosY = entityLiving.prevPosY;
			player.prevPosZ = entityLiving.prevPosZ;
			player.lastTickPosX = entityLiving.lastTickPosX;
			player.lastTickPosY = entityLiving.lastTickPosY;
			player.lastTickPosZ = entityLiving.lastTickPosZ;

			if ((Minecraft.getMinecraft().gameSettings.keyBindUseItem.isKeyDown() || 
					Minecraft.getMinecraft().gameSettings.keyBindAttack.isKeyDown())) {
				if (string != null && !this.bool) {
					ArrayList<EntityLivingBase> teamEntities = new ArrayList<EntityLivingBase>(player.world.getEntities(EntityLivingBase.class, new Predicate<EntityLivingBase>() {
						@Override
						public boolean apply(EntityLivingBase entity) {
							return player != entity && entity.getTeam() != null && entity.getTeam().getName().equals(string) && !(entity instanceof EntityLivingBaseMW);
						}}));

					// get index of next/prev team entity
					int index = teamEntities.indexOf(this.entityLiving)+(Minecraft.getMinecraft().gameSettings.keyBindUseItem.isKeyDown() ? -1 : 1);
					if (index < 0)
						index = teamEntities.size()-1;
					else if (index >= teamEntities.size())
						index = 0;

					EntityLivingBase entityToSpectate = teamEntities.isEmpty() ? player : teamEntities.get(index);
					this.entityLiving = entityToSpectate;
					this.bool = true;
				}
			}
			else 
				this.bool = false;

			return --ticksLeft <= 0 || entity == null || entity.isEntityAlive() || entity != Minewatch.proxy.getClientPlayer();
		}
		@Override
		public boolean onServerTick() {
			if (player instanceof EntityPlayerMP) {
				if (((EntityPlayerMP)player).getSpectatingEntity() != player)
					((EntityPlayerMP)player).setSpectatingEntity(player);
				player.isDead = false; // needed to keep entities from spazzing out (in SP at least)
				player.setGameType(GameType.SPECTATOR);
			}
			return --ticksLeft <= 0 || entity == null || entity.isEntityAlive();
		}
		@Override
		@SideOnly(Side.CLIENT)
		public Handler onClientRemove() {
			// if still alive, send another packet to server and wait
			if (entity == Minewatch.proxy.getClientPlayer()) {
				Minewatch.network.sendToServer(new CPacketSimple(12, true, player));
				this.ticksLeft = 10;
				return null;
			}
			Minecraft.getMinecraft().gameSettings.thirdPersonView = 0;
			return super.onClientRemove();
		}
		@Override
		public Handler onServerRemove() {
			if (player != null && player.getServer() != null) {
				GameType type = player.getServer().getGameType();
				if (number >= 0 && number < GameType.values().length && (int)number != GameType.SPECTATOR.ordinal())
					type = GameType.values()[(int) number];
				player.setGameType(type);
			}
			respawnEntity(entityLiving, entityLiving.world.getScoreboard().getTeam(string), false, this.obj instanceof TileEntityTeamSpawn ? ((TileEntityTeamSpawn)obj).getPos() : null); 
			return super.onServerRemove();
		}
	};

	/**Respawn a player / mob*/
	public static void respawnEntity(EntityLivingBase entity, @Nullable Team team, boolean allowAlive, @Nullable BlockPos teamSpawn) {
		if (entity == null || (entity.isEntityAlive() && !allowAlive) ||
				(entity instanceof EntityPlayerMP && ((EntityPlayerMP)entity).hasDisconnected()))
			return;

		if (teamSpawn == null || !isValidTeamSpawn(entity, team, teamSpawn))
			teamSpawn = getTeamSpawn(entity, team);

		if (entity instanceof EntityPlayerMP) {
			EntityPlayerMP player = (EntityPlayerMP) entity;

			// just move player if still alive
			if (allowAlive && player.isEntityAlive()) {
				if (teamSpawn != null) {
					BlockPos fuzz = spawnFuzz(teamSpawn, player.world);
					player.setPosition(fuzz.getX()+0.5d, fuzz.getY(), fuzz.getZ()+0.5d);
				}
			}
			// move spawn point, spawn, move back spawn point
			else {
				int dimension = 0;
				MinecraftServer mcServer = player.mcServer;
				World world = mcServer.getWorld(dimension);
				if (world == null)
					dimension = player.getSpawnDimension();
				else if (!world.provider.canRespawnHere())
					dimension = world.provider.getRespawnDimension((EntityPlayerMP) player);
				if (mcServer.getWorld(dimension) == null) 
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
				if (teamSpawn != null) 
					player.connection.player.setSpawnPoint(pos, spawnForced);
			}

			// rotate player
			if (teamSpawn != null) {
				try {
					EnumFacing facing = player.world.getBlockState(teamSpawn).getValue(BlockTeamSpawn.FACING);
					player.connection.player.rotationYaw = facing.getHorizontalAngle();
					Minewatch.network.sendTo(new SPacketSimple(59, player, false, facing.getHorizontalAngle(), 0, 0), (EntityPlayerMP) player);
				}
				catch (Exception e) {}
			}

			// heal to full
			player.connection.player.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 40, 31, false, false));
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
				respawnEntity.motionX = 0;
				respawnEntity.motionY = 0;
				respawnEntity.motionZ = 0;
				entity.world.getScoreboard().addPlayerToTeam(respawnEntity.getCachedUniqueIdString(), team.getName());
				if (entity.hasCustomName()) {
					respawnEntity.setCustomNameTag(entity.getCustomNameTag());
					respawnEntity.setAlwaysRenderNameTag(entity.getAlwaysRenderNameTag());
				}
				respawnEntity.clearActivePotions();
				respawnEntity.extinguish();
				respawnEntity.deathTime = 0;
				if (respawnEntity instanceof EntityShulker)
					((EntityShulker)respawnEntity).setAttachmentPos(teamSpawn);
				respawnEntity.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 40, 31, false, false));
				entity.world.spawnEntity(respawnEntity);
				if (Config.mobRespawnRandomHero && respawnEntity instanceof EntityHero) 
					((EntityHero)respawnEntity).spawnRandomHero();
			}
			catch (Exception e) {
				Minewatch.logger.warn("Unable to respawn entity ("+entity+") at Team Spawn ("+teamSpawn+")", e);
			}
		}
	}
	
	public static boolean isValidTeamSpawn(EntityLivingBase entity, @Nullable Team team, BlockPos teamSpawn) {
		ArrayList<BlockPos> spawns = getTeamSpawns(entity, team);
		return spawns.contains(teamSpawn);
	}

	/**Gets the position of an active team spawn that this entity can spawn at (randomly pick one if multiple available)*/
	@Nullable
	public static BlockPos getTeamSpawn(EntityLivingBase entity, @Nullable Team team) {
		ArrayList<BlockPos> spawns = getTeamSpawns(entity, team);
		return spawns.isEmpty() ? null : spawns.get(entity.world.rand.nextInt(spawns.size()));
	}
	
	/**Gets the position of an active team spawn that this entity can spawn at (randomly pick one if multiple available)*/
	@Nullable
	public static ArrayList<BlockPos> getTeamSpawns(EntityLivingBase entity, @Nullable Team team) {
		ArrayList<BlockPos> spawns = new ArrayList<BlockPos>();
		// check for same team first
		for (BlockPos pos : TileEntityTeamSpawn.teamSpawnPositions.keySet())
			if (entity != null && pos != null && entity.world.getTileEntity(pos) instanceof TileEntityTeamSpawn) {
				TileEntityTeamSpawn te = ((TileEntityTeamSpawn)entity.world.getTileEntity(pos));
				if (te.isActivated() && te.getTeam() != null && te.getTeam().isSameTeam(team) && !te.isInvalid())
					spawns.add(pos);
			}
		// check for no team second
		if (spawns.isEmpty())
			for (BlockPos pos : TileEntityTeamSpawn.teamSpawnPositions.keySet())
				if (entity != null && pos != null && entity.world.getTileEntity(pos) instanceof TileEntityTeamSpawn) {
					TileEntityTeamSpawn te = ((TileEntityTeamSpawn)entity.world.getTileEntity(pos));
					if (te.isActivated() && te.getTeam() == null && !te.isInvalid())
						spawns.add(pos);
				}
		return spawns;
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
		if (!event.player.world.isRemote && !Config.customDeathScreen) {
			respawnEntity(event.player, event.player.getTeam(), true, null);
		}
	}

	@SubscribeEvent
	public static void registerMobs(LivingDeathEvent event) {
		// put team in customEntityData because entity removed from team on death
		if (event.getEntityLiving() != null && event.getEntityLiving().getTeam() != null)
			event.getEntityLiving().getEntityData().setString("minewatch:team", event.getEntityLiving().getTeam().getName());

		// register dead mobs directly
		if (isRespawnableEntity(event.getEntityLiving()) &&
				!event.getEntityLiving().world.isRemote && !TickHandler.hasHandler(event.getEntityLiving(), Identifier.DEAD)) {
			TickHandler.register(false, DEAD.setEntity(event.getEntityLiving()).setTicks(Config.respawnTime).setString(event.getEntityLiving().getTeam().getName()));
		}

		// put players in 3rd person
		if (event.getEntityLiving() instanceof EntityPlayerMP && Config.customDeathScreen)
			Minewatch.network.sendTo(new SPacketSimple(75, false, (EntityPlayer) event.getEntityLiving()), (EntityPlayerMP) event.getEntityLiving());
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void registerPlayers(GuiOpenEvent event) {
		// register dead players when they try to open GuiGameOver (in case they cancel respawn after death)
		if ((event.getGui() instanceof GuiGameOver) && Config.customDeathScreen) {
			event.setGui(null);
			EntityPlayer player = Minewatch.proxy.getClientPlayer();
			if (player != null && player.isDead && !TickHandler.hasHandler(player, Identifier.DEAD)) {
				String team = player.getTeam() != null ? player.getTeam().getName() : null;
				BlockPos teamSpawn = getTeamSpawn(player, player.getTeam());
				TileEntityTeamSpawn te = teamSpawn != null && player.world.getTileEntity(teamSpawn) instanceof TileEntityTeamSpawn ? ((TileEntityTeamSpawn)player.world.getTileEntity(teamSpawn)) : null;
				Minewatch.network.sendToServer(new CPacketSimple(12, false, player, teamSpawn == null ? 0 : teamSpawn.getX(), teamSpawn == null ? 0 : teamSpawn.getY(), teamSpawn == null ? 0 : teamSpawn.getZ()));
				TickHandler.register(true, RespawnManager.DEAD.setEntity(player).setTicks(Config.respawnTime+3).setString(team).setObject(te));
				if (te != null && te.isActivated() && te.getChangeHero())
					TickHandler.register(true, TileEntityTeamSpawn.IN_RANGE.setEntity(player).setTicks(Config.respawnTime+3).setAllowDead(true));
			}
		}
	}

	/**Can this non-player entity respawn with Team Spawn*/
	public static boolean isRespawnableEntity(Entity entity) {
		return entity instanceof EntityLivingBase && !(entity instanceof EntityLivingBaseMW) && 
				!(entity instanceof EntityPlayer) && 
				!(entity instanceof EntityArmorStand) && entity.getTeam() != null && 
				!(entity instanceof EntitySlime && !((EntitySlime)entity).isSmallSlime()) &&  // only allow small slimes to respawn
				entity.isNonBoss() && 
				!(entity instanceof EntityHero && !Config.allowHeroRespawn) && 
				!(!(entity instanceof EntityHero) && !Config.allowMobRespawn) && 
				getTeamSpawn((EntityLivingBase) entity, entity.getTeam()) != null;
	}

	/**Can this player respawn with Team Spawn*/
	public static boolean isRespawnablePlayer(Entity entity) {
		return entity instanceof EntityPlayerMP && Config.allowPlayerRespawn && !(entity instanceof FakePlayer) && 
				getTeamSpawn((EntityLivingBase) entity, entity.getTeam()) != null;
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public static void renderDeathOverlay(RenderGameOverlayEvent.Post event) {
		if (Minecraft.getMinecraft().player != null && !Minecraft.getMinecraft().player.isEntityAlive() && 
				event.getType() == ElementType.ALL) {
			Minecraft mc = Minecraft.getMinecraft();
			EntityPlayer player = mc.player;
			Handler handler = TickHandler.getHandler(player, Identifier.DEAD);
			if (handler != null) {
				double width = event.getResolution().getScaledWidth_double();

				GlStateManager.pushMatrix();
				GlStateManager.enableBlend();
				Minecraft.getMinecraft().getTextureManager().bindTexture(RenderManager.ABILITY_OVERLAY);

				// top
				double scale = Config.guiScale*1d;
				GlStateManager.scale(scale, scale, 0);
				GuiUtils.drawGradientRect(0, 0, 0, (int) (width/scale), (int) (40/scale), 0x6F000000, 0x6F000000);
				GuiUtils.drawGradientRect(0, 0, (int) (40/scale), (int) (width/scale), (int) (42/scale), 0xAAAAAAAA, 0xAAAAAAAA);
				GlStateManager.scale(1d/scale, 1d/scale, 0);
				scale = Config.guiScale*3.8d;
				GlStateManager.scale(scale, scale, 0);
				GlStateManager.enableBlend();
				GlStateManager.color(1, 1, 1, 1f);
				GuiUtils.drawTexturedModalRect((int) (width/scale-250/scale), 3, 38, 1008, 21, 5, 0);
				GlStateManager.scale(1d/scale, 1d/scale, 0);

				// text
				scale = Config.guiScale*1d;
				GlStateManager.scale(scale, scale, 0);
				String format = TextFormatting.BOLD+""+TextFormatting.GOLD+""+TextFormatting.ITALIC;
				String text = format+Minewatch.translate("overlay.respawn_in").toUpperCase()+": ";
				mc.fontRenderer.drawString(text, (float) (width/scale-mc.fontRenderer.getStringWidth(text)-30f/scale), (float) (16f/scale), 0xFFFFFF, true);
				text = TextFormatting.BOLD+""+TextFormatting.ITALIC+handler.entityLiving.getName().toUpperCase();
				mc.fontRenderer.drawString(text, (float) (13f/scale), (float) (23f/scale), 0xFFFFFF, true);
				text = format+Minewatch.translate("overlay.death_spectating").toUpperCase()+":";
				GlStateManager.scale(1d/scale, 1d/scale, 0);
				scale = Config.guiScale*1.4d;
				GlStateManager.scale(scale, scale, 0);
				mc.fontRenderer.drawString(text, (float) (10f/scale), (float) (8f/scale), 0xFFFFFF, true);
				GlStateManager.scale(1d/scale, 1d/scale, 0);
				scale = Config.guiScale*1.5d;
				GlStateManager.scale(scale, scale, 0);
				text = String.valueOf(handler.ticksLeft / 20);
				mc.fontRenderer.drawString(text, (float) (width/scale-mc.fontRenderer.getStringWidth(text)/2d-28f/scale), (float) (15f/scale), 0xFFFFFF, true);

				GlStateManager.popMatrix();
			}
		}
	}

}