package twopiradians.minewatch.packet;

import java.util.UUID;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.item.weapon.ItemGenjiShuriken;
import twopiradians.minewatch.common.item.weapon.ItemMWWeapon;
import twopiradians.minewatch.common.item.weapon.ItemMcCreeGun;
import twopiradians.minewatch.common.item.weapon.ItemReaperShotgun;

public class SPacketTriggerAbility implements IMessage {

	private int type;
	private boolean bool;
	private UUID uuid;
	private double x;
	private double y;
	private double z;

	public SPacketTriggerAbility() { }

	public SPacketTriggerAbility(int type) {
		this(type, null, 0, 0, 0);
	}

	public SPacketTriggerAbility(int type, boolean bool) {
		this(type, bool, null, 0, 0, 0);
	}

	public SPacketTriggerAbility(int type, boolean bool, EntityPlayer player) {
		this(type, bool, player, 0, 0, 0);
	}

	public SPacketTriggerAbility(int type, EntityPlayer player, double x, double y, double z) {
		this(type, false, player, x, y, z);
	}

	public SPacketTriggerAbility(int type, boolean bool, EntityPlayer player, double x, double y, double z) {
		this.type = type;
		this.bool = bool;
		this.uuid = player == null ? UUID.randomUUID() : player.getPersistentID();
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.type = buf.readInt();
		this.bool = buf.readBoolean();
		this.uuid = UUID.fromString(ByteBufUtils.readUTF8String(buf));
		this.x = buf.readDouble();
		this.y = buf.readDouble();
		this.z = buf.readDouble();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(this.type);
		buf.writeBoolean(this.bool);
		ByteBufUtils.writeUTF8String(buf, this.uuid.toString());
		buf.writeDouble(this.x);
		buf.writeDouble(this.y);
		buf.writeDouble(this.z);
	}

	public static void move(EntityPlayer player, double scale, boolean useLook) {
		Vec3d vec = new Vec3d(player.motionX, 0, player.motionZ);
		if (vec.xCoord == 0 && vec.zCoord == 0) 
			vec = new Vec3d(player.getLookVec().xCoord, 0, player.getLookVec().zCoord);
		if (useLook)
			vec = new Vec3d(player.getLookVec().xCoord, player.getLookVec().yCoord, player.getLookVec().zCoord);
		if (!player.onGround) {
			player.motionY = 0.24d;
			player.velocityChanged = true;
		}
		vec = vec.normalize().scale(scale);
		player.move(MoverType.SELF, vec.xCoord, vec.yCoord, vec.zCoord);
	}

	public static class Handler implements IMessageHandler<SPacketTriggerAbility, IMessage> {
		@Override
		public IMessage onMessage(final SPacketTriggerAbility packet, final MessageContext ctx) {
			IThreadListener mainThread = Minecraft.getMinecraft();
			mainThread.addScheduledTask(new Runnable() {
				@Override
				public void run() {
					EntityPlayerSP player = Minecraft.getMinecraft().player;
					EntityPlayer packetPlayer = packet.uuid == null ? null : Minecraft.getMinecraft().world.getPlayerEntityByUUID(packet.uuid);

					// Tracer's dash
					if (packet.type == 0) 
						move(player, 9, false);
					// Reaper's teleport
					else if (packet.type == 1 && packetPlayer != null) {
						ItemReaperShotgun.clientTps.put(packetPlayer, new Tuple(70, new Vec3d(packet.x, packet.y, packet.z)));
						Minewatch.proxy.spawnParticlesReaperTeleport(packetPlayer.world, packetPlayer, true, 0);
					}
					// McCree's roll
					else if (packet.type == 2 && packetPlayer != null) {//TODO test that particles are server side
						if (packetPlayer == player) {
							player.onGround = true;
							player.movementInput.sneak = true;
						}
						if (packet.bool) {
							ItemMcCreeGun.clientRolling.put(packetPlayer, 10);
							EnumHero.RenderManager.playersSneaking.put(packetPlayer, 11);
						}
						if (packetPlayer == player)
							move(packetPlayer, 1.0d, false);
					}
					// Genji's strike
					else if (packet.type == 3 && packetPlayer != null) {
						ItemGenjiShuriken.clientStriking.put(packetPlayer, 8);
						ItemGenjiShuriken.useSword.put(packetPlayer, 8);
						EnumHero.RenderManager.playersSneaking.put(packetPlayer, 9);
						if (packetPlayer == player) 
							move(packetPlayer, 1.8d, false);
					}
					// Genji's use sword
					else if (packet.type == 4 && packetPlayer != null) {
						ItemGenjiShuriken.useSword.put(packetPlayer, (int) packet.x);
					}
					// Reinhardt's hammer swing
					else if (packet.type == 5) {
						Minewatch.proxy.mouseClick();
					}
					// Sync playersUsingAlt
					else if (packet.type == 6 && packetPlayer != null) {
						ItemStack main = packetPlayer.getHeldItemMainhand();
						if (main != null && main.getItem() instanceof ItemMWWeapon) {
							EnumHero hero = ((ItemMWWeapon)main.getItem()).hero;
							hero.playersUsingAlt.put(packet.uuid, packet.bool);
							if (ItemStack.areItemsEqualIgnoreDurability(main, packetPlayer.getHeldItemOffhand()))
								((ItemMWWeapon)main.getItem()).setCurrentAmmo(packetPlayer, 
										((ItemMWWeapon)main.getItem()).getCurrentAmmo(packetPlayer), 
										EnumHand.MAIN_HAND, EnumHand.OFF_HAND);
							else
								((ItemMWWeapon)main.getItem()).setCurrentAmmo(packetPlayer, 
										((ItemMWWeapon)main.getItem()).getCurrentAmmo(packetPlayer), 
										EnumHand.MAIN_HAND);
						}
					}
				}
			});
			return null;
		}
	}
}