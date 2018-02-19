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
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.client.CPacketClientStatus;
import net.minecraft.network.play.client.CPacketSpectate;
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
import twopiradians.minewatch.client.key.Keys.KeyBind;
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

	/**entity = dead respawning entity, entityLiving = (client) spectating entity, @Nullable string = entity's team, bool = changed render view entity this tick*/
	public static final Handler DEAD = new Handler(Identifier.DEAD, false) {
		@Override
		@SideOnly(Side.CLIENT)
		public boolean onClientTick() {
			player.deathTime = -1;
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
							return player != entity && entity.getTeam() != null && entity.getTeam().getRegisteredName().equals(string) && !(entity instanceof EntityLivingBaseMW);
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
				Minewatch.logger.info("entity still dead, sending another packet");
				Minewatch.network.sendToServer(new CPacketSimple(12, true, player));
				this.ticksLeft = 10;
				return null;
			}
			Minecraft.getMinecraft().gameSettings.thirdPersonView = 0;
			return super.onClientRemove();
		}
		@Override
		public Handler onServerRemove() {
			if (player != null && player.getServer() != null)
				player.setGameType(player.getServer().getGameType());
			respawnEntity(entityLiving, entityLiving.world.getScoreboard().getTeam(string), false); 
			return super.onServerRemove();
		}
	};

	/**Respawn a player / mob*/
	public static void respawnEntity(EntityLivingBase entity, @Nullable Team team, boolean allowAlive) {
		if (entity == null || (entity.isEntityAlive() && !allowAlive))
			return;
		BlockPos teamSpawn = getTeamSpawn(entity, team);

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
				Minewatch.logger.info("respawn sending packet"); // TODO
				player.connection.processClientStatus(new CPacketClientStatus(CPacketClientStatus.State.PERFORM_RESPAWN));
				// move spawn back
				if (teamSpawn != null) 
					player.connection.playerEntity.setSpawnPoint(pos, spawnForced);
			}

			// rotate player
			if (teamSpawn != null) {
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
		// respawn with method if custom death screen disabled TODO
		if (!event.player.world.isRemote && !Config.customDeathScreen) {
			respawnEntity(event.player, event.player.getTeam(), true);
		}
	}

	@SubscribeEvent
	public static void registerMobs(LivingDeathEvent event) {
		// register dead mobs directly
		if (isRespawnableEntity(event.getEntityLiving()) &&
				!event.getEntityLiving().world.isRemote && !TickHandler.hasHandler(event.getEntityLiving(), Identifier.DEAD)) {
			TickHandler.register(false, DEAD.setEntity(event.getEntityLiving()).setTicks(Config.respawnTime).setString(event.getEntityLiving().getTeam().getRegisteredName()));
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void registerPlayers(GuiOpenEvent event) {
		// register dead players when they try to open GuiGameOver (in case they cancel respawn after death)
		if ((event.getGui() instanceof GuiGameOver) && Config.customDeathScreen) {
			event.setGui(null);
			EntityPlayer player = Minewatch.proxy.getClientPlayer();
			if (player != null && player.isDead && !TickHandler.hasHandler(player, Identifier.DEAD)) {
				Minewatch.logger.info("registering DEAD client");
				Minewatch.network.sendToServer(new CPacketSimple(12, false, player));
				Minewatch.logger.info("guiopenevent sending packet"); // TODO
				TickHandler.register(true, RespawnManager.DEAD.setEntity(player).setTicks(Config.respawnTime+3).setString(player.getTeam() != null ? player.getTeam().getRegisteredName() : null));
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
				!(!(entity instanceof EntityHero) && !Config.allowMobRespawn);
	}

	/**Can this player respawn with Team Spawn*/
	public static boolean isRespawnablePlayer(Entity entity) {
		return entity instanceof EntityPlayerMP && Config.allowPlayerRespawn && !(entity instanceof FakePlayer);
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
				GuiUtils.drawGradientRect(0, 0, 0, (int) width, 40, 0x6F000000, 0x6F000000);
				GuiUtils.drawGradientRect(0, 0, 40, (int) width, 42, 0xAAAAAAAA, 0xAAAAAAAA);
				GlStateManager.scale(1d/scale, 1d/scale, 0);
				scale = Config.guiScale*3.8d;
				GlStateManager.scale(scale, scale, 0);
				GlStateManager.enableBlend();
				GlStateManager.color(1, 1, 1, 1f);
				GuiUtils.drawTexturedModalRect((int) (width/scale-50), 3, 38, 1008, 21, 5, 0);
				GlStateManager.scale(1d/scale, 1d/scale, 0);
				scale = Config.guiScale*1d;

				// text
				String format = TextFormatting.BOLD+""+TextFormatting.GOLD+""+TextFormatting.ITALIC;
				String text = format+Minewatch.translate("overlay.respawn_in").toUpperCase()+": ";
				mc.fontRendererObj.drawString(text, (float) (width/scale-mc.fontRendererObj.getStringWidth(text)-30), 16, 0xFFFFFF, true);
				text = TextFormatting.BOLD+""+TextFormatting.ITALIC+handler.entityLiving.getName().toUpperCase();
				mc.fontRendererObj.drawString(text, 13, 23, 0xFFFFFF, true);
				text = format+Minewatch.translate("overlay.death_spectating").toUpperCase()+":";
				GlStateManager.scale(1d/scale, 1d/scale, 0);
				scale = Config.guiScale*1.2d;
				GlStateManager.scale(scale, scale, 0);
				mc.fontRendererObj.drawString(text, 10, 8, 0xFFFFFF, true);
				GlStateManager.scale(1d/scale, 1d/scale, 0);
				scale = Config.guiScale*1.5d;
				GlStateManager.scale(scale, scale, 0);
				text = String.valueOf(handler.ticksLeft / 20);
				mc.fontRendererObj.drawString(text, (float) (width/scale-mc.fontRendererObj.getStringWidth(text)/2d-18), 9, 0xFFFFFF, true);

				GlStateManager.popMatrix();
			}
		}
	}

}