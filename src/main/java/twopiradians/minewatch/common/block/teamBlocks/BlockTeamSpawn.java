package twopiradians.minewatch.common.block.teamBlocks;


import java.util.ArrayList;
import java.util.Collections;

import javax.annotation.Nullable;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.client.CPacketClientStatus;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.CommonProxy.EnumGui;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.hero.EntityHero;
import twopiradians.minewatch.common.tileentity.TileEntityTeam;
import twopiradians.minewatch.common.tileentity.TileEntityTeamSpawn;
import twopiradians.minewatch.common.util.EntityHelper;
import twopiradians.minewatch.common.util.TickHandler;
import twopiradians.minewatch.common.util.TickHandler.Handler;
import twopiradians.minewatch.common.util.TickHandler.Identifier;
import twopiradians.minewatch.packet.CPacketSimple;
import twopiradians.minewatch.packet.SPacketSimple;

public class BlockTeamSpawn extends TeamBlock {

	public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
	
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
			/*			if (player instanceof EntityPlayerSP) {
				player.respawnPlayer();
				Minewatch.logger.info("respawning client");
			}*/
			return super.onClientRemove();
		}
		@Override
		public Handler onServerRemove() {
			BlockPos teamSpawn = getTeamSpawn(entityLiving);

			if (player instanceof EntityPlayerMP) {
				int dimension = 0;
				MinecraftServer mcServer = ((EntityPlayerMP) player).mcServer;
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
				((EntityPlayerMP) player).connection.processClientStatus(new CPacketClientStatus(CPacketClientStatus.State.PERFORM_RESPAWN));
				// move spawn back
				if (teamSpawn != null) {
					((EntityPlayerMP) player).connection.playerEntity.setSpawnPoint(pos, spawnForced);
					try {
						EnumFacing facing = player.world.getBlockState(teamSpawn).getValue(FACING);
						((EntityPlayerMP) player).connection.playerEntity.rotationYaw = facing.getHorizontalAngle();
						Minewatch.network.sendTo(new SPacketSimple(59, player, false, facing.getHorizontalAngle(), 0, 0), (EntityPlayerMP) player);
					}
					catch (Exception e) {}
				}
				Minewatch.logger.info("respawning server");
			}
			else if (entityLiving instanceof EntityHero && teamSpawn != null && this.string != null) {
				try {
					EnumFacing facing = entityLiving.world.getBlockState(teamSpawn).getValue(FACING);
					teamSpawn = spawnFuzz(teamSpawn, entityLiving.world);
					NBTTagCompound nbt = new NBTTagCompound();
					entityLiving.writeToNBT(nbt);
					nbt.removeTag("UUID");
					EntityHero heroMob = (EntityHero) ((EntityHero)entityLiving).hero.heroClass.getConstructor(World.class).newInstance(entityLiving.world);
					heroMob.readEntityFromNBT(nbt);
					heroMob.setHealth(heroMob.getMaxHealth());
					heroMob.setPositionAndRotation(teamSpawn.getX(), teamSpawn.getY(), teamSpawn.getZ(), facing.getHorizontalAngle(), 0);
					heroMob.rotationYawHead = facing.getHorizontalAngle();
					heroMob.prevRotationYawHead = facing.getHorizontalAngle();
					heroMob.rotationYaw = facing.getHorizontalAngle();
					heroMob.prevRotationYaw = facing.getHorizontalAngle();
					heroMob.setRenderYawOffset(facing.getHorizontalAngle());
					heroMob.prevRenderYawOffset = facing.getHorizontalAngle();
					entityLiving.world.getScoreboard().addPlayerToTeam(heroMob.getCachedUniqueIdString(), this.string);
					if (entityLiving.hasCustomName()) {
						heroMob.setCustomNameTag(entityLiving.getCustomNameTag());
						heroMob.setAlwaysRenderNameTag(entityLiving.getAlwaysRenderNameTag());
					}
					entityLiving.world.spawnEntity(heroMob);
					entityLiving.world.removeEntity(entityLiving);
				}
				catch (Exception e) {
					Minewatch.logger.warn("Unable to respawn hero ("+entityLiving+") at Team Spawn ("+teamSpawn+")");
				}
			}

			return super.onServerRemove();
		}
	};

	public BlockTeamSpawn() {
		super();
		this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(HAS_TEAM, false).withProperty(ACTIVATED, false));
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	@Nullable
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TileEntityTeamSpawn();
	}

	@Override
	public IBlockState withRotation(IBlockState state, Rotation rot) {
		return state.withProperty(FACING, rot.rotate((EnumFacing)state.getValue(FACING)));
	}

	@Override
	public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
		return state.withRotation(mirrorIn.toRotation((EnumFacing)state.getValue(FACING)));
	}

	@Override
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return super.getStateFromMeta(meta & 3).
				withProperty(FACING, EnumFacing.getHorizontal(meta >> 2 & 3));
	}

	/**00 (FACING) 0 (HAS_TEAM) 0 (ACTIVATED)*/
	@Override
	public int getMetaFromState(IBlockState state) {
		int meta = super.getMetaFromState(state);
		meta <<= 2;
		meta += state.getValue(FACING).getHorizontalIndex();
		return meta;
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] {FACING, HAS_TEAM, ACTIVATED});
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		boolean ret = super.onBlockActivated(world, pos, state, player, hand, facing, hitX, hitY, hitZ);

		// open gui if in creative mode
		if (!ret && player.world.isRemote && world.getTileEntity(pos) instanceof TileEntityTeam) {
			// warn if not in creative - set cooldown handler to prevent spam
			if (!player.isCreative()) {
				if (!TickHandler.hasHandler(player, Identifier.TEAM_BLOCK_WARNING_COOLDOWN)) {
					player.sendMessage(new TextComponentString(TextFormatting.RED+"You must be in creative mode to access this."));
					TickHandler.register(true, WARNING_COOLDOWN.setEntity(player).setTicks(50));
				}
			}
			else
				Minewatch.proxy.openGui(EnumGui.TEAM_SPAWN, ((TileEntityTeam)world.getTileEntity(pos)));
		}

		return player.isCreative();
	}

	/**Gets the position of an active team spawn that this entity can spawn at*/
	@Nullable
	public static BlockPos getTeamSpawn(EntityLivingBase entity) {
		for (BlockPos pos : TileEntityTeamSpawn.teamSpawnPositions.keySet())
			if (entity != null && pos != null && entity.world.getTileEntity(pos) instanceof TileEntityTeamSpawn)
				return pos;
		return null;
	}

	/**Applies spawn fuzz to a position (randomly positioning in the spawn radius)*/
	public static BlockPos spawnFuzz(BlockPos pos, World world) {
		if (pos != null && world.getTileEntity(pos) instanceof TileEntityTeamSpawn) {
			TileEntityTeamSpawn te = (TileEntityTeamSpawn) world.getTileEntity(pos);
			int radius = 5;
			ArrayList<BlockPos> positions = new ArrayList<BlockPos>();
			// add all possible xz positions
			for (int x=-radius/2; x<=radius/2; ++x) 
				for (int z=-radius/2; z<=radius/2; ++z)
					positions.add(pos.add(x, 0, z));
			Collections.shuffle(positions);
			for (BlockPos newPos : positions) {
				// while within radius - check for valid and move up or return
				while (newPos.getDistance(te.getPos().getX(), te.getPos().getY(), te.getPos().getZ()) < radius+1) {
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
	public void onEvent(LivingDeathEvent event) {
		if ((event.getEntityLiving() instanceof EntityHero && event.getEntityLiving().getTeam() != null) &&
				!event.getEntityLiving().world.isRemote) {
			TickHandler.register(false, DEAD.setEntity(event.getEntityLiving()).setTicks(20).setString(event.getEntityLiving().getTeam().getRegisteredName()));
			Minewatch.logger.info("livingdeathevent");
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onEvent(GuiOpenEvent event) {
		if (event.getGui() instanceof GuiGameOver) {
			event.setCanceled(true);
			if (Minecraft.getMinecraft().player.isDead) {
				Minewatch.network.sendToServer(new CPacketSimple(12, false, Minecraft.getMinecraft().player));
			}
		}
	}

}