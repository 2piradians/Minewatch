package twopiradians.minewatch.packet;

import java.util.UUID;

import org.apache.commons.lang3.tuple.Triple;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import twopiradians.minewatch.client.gui.display.GuiDisplay;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.config.Config;
import twopiradians.minewatch.common.hero.Ability;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.item.weapon.ItemAnaRifle;
import twopiradians.minewatch.common.item.weapon.ItemGenjiShuriken;
import twopiradians.minewatch.common.item.weapon.ItemMWWeapon;
import twopiradians.minewatch.common.item.weapon.ItemMcCreeGun;
import twopiradians.minewatch.common.item.weapon.ItemReaperShotgun;
import twopiradians.minewatch.common.potion.ModPotions;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.tickhandler.Handlers;
import twopiradians.minewatch.common.tickhandler.TickHandler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Identifier;

public class SPacketSimple implements IMessage {

	private int type;
	private boolean bool;
	private UUID uuid;
	private double x;
	private double y;
	private double z;
	private int id;

	public SPacketSimple() { }

	public SPacketSimple(int type) {
		this(type, false, null, 0, 0, 0, null);
	}

	public SPacketSimple(int type, boolean bool) {
		this(type, bool, null, 0, 0, 0, null);
	}

	public SPacketSimple(int type, Entity entity, boolean bool) {
		this(type, bool, null, 0, 0, 0, entity);
	}

	public SPacketSimple(int type, Entity entity, boolean bool, double x, double y, double z) {
		this(type, bool, null, x, y, z, entity);
	}

	public SPacketSimple(int type, boolean bool, EntityPlayer player) {
		this(type, bool, player, 0, 0, 0, null);
	}

	public SPacketSimple(int type, boolean bool, EntityPlayer player, double x, double y, double z) {
		this(type, bool, player, x, y, z, null);
	}

	public SPacketSimple(int type, EntityPlayer player, double x, double y, double z) {
		this(type, false, player, x, y, z, null);
	}

	public SPacketSimple(int type, boolean bool, EntityPlayer player, double x, double y, double z, Entity entity) {
		this.type = type;
		this.bool = bool;
		this.uuid = player == null ? UUID.randomUUID() : player.getPersistentID();
		this.id = entity == null ? -1 : entity.getEntityId();
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.type = buf.readInt();
		this.bool = buf.readBoolean();
		this.uuid = UUID.fromString(ByteBufUtils.readUTF8String(buf));
		this.id = buf.readInt();
		this.x = buf.readDouble();
		this.y = buf.readDouble();
		this.z = buf.readDouble();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(this.type);
		buf.writeBoolean(this.bool);
		ByteBufUtils.writeUTF8String(buf, this.uuid.toString());
		buf.writeInt(this.id);
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
		player.moveEntity(vec.xCoord, vec.yCoord, vec.zCoord);
	}

	public static class Handler implements IMessageHandler<SPacketSimple, IMessage> {
		@Override
		public IMessage onMessage(final SPacketSimple packet, final MessageContext ctx) {
			IThreadListener mainThread = Minecraft.getMinecraft();
			mainThread.addScheduledTask(new Runnable() {
				@Override
				public void run() {
					EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
					EntityPlayer packetPlayer = packet.uuid == null ? null : player.worldObj.getPlayerEntityByUUID(packet.uuid);
					Entity entity = packet.id == -1 ? null : player.worldObj.getEntityByID(packet.id);

					// Tracer's dash
					if (packet.type == 0) 
						move(player, 9, false);
					// Reaper's teleport
					else if (packet.type == 1 && packetPlayer != null) {
						TickHandler.register(true, ItemReaperShotgun.TPS.setEntity(packetPlayer).setTicks(70).setPosition(new Vec3d(packet.x, packet.y, packet.z)), 
								Ability.ABILITY_USING.setEntity(packetPlayer).setTicks(70));
						Minewatch.proxy.spawnParticlesReaperTeleport(packetPlayer.worldObj, packetPlayer, true, 0);
						if (player == packetPlayer)
							ItemReaperShotgun.tpThirdPersonView.put(packetPlayer, Minecraft.getMinecraft().gameSettings.thirdPersonView);
					}
					// McCree's roll
					else if (packet.type == 2 && packetPlayer != null) {
						if (packetPlayer == player) {
							player.onGround = true;
							player.movementInput.sneak = true;
						}
						if (packet.bool) {
							TickHandler.register(true, ItemMcCreeGun.ROLL.setEntity(packetPlayer).setTicks(10),
									Ability.ABILITY_USING.setEntity(packetPlayer).setTicks(10), 
									EnumHero.RenderManager.SNEAKING.setEntity(packetPlayer).setTicks(11));
						}
						if (packetPlayer == player)
							move(packetPlayer, 1.0d, false);
					}
					// Genji's strike
					else if (packet.type == 3 && packetPlayer != null) {
						TickHandler.register(true, ItemGenjiShuriken.STRIKE.setEntity(packetPlayer).setTicks(8),
								ItemGenjiShuriken.SWORD_CLIENT.setEntity(packetPlayer).setTicks(8),
								Ability.ABILITY_USING.setEntity(packetPlayer).setTicks(8), 
								EnumHero.RenderManager.SNEAKING.setEntity(packetPlayer).setTicks(9));
						if (packetPlayer == player) 
							move(packetPlayer, 1.8d, false);
					}
					// Genji's use sword
					else if (packet.type == 4 && packetPlayer != null) {
						TickHandler.register(true, ItemGenjiShuriken.SWORD_CLIENT.setEntity(packetPlayer).setTicks((int) packet.x));
						TickHandler.register(true, Ability.ABILITY_USING.setEntity(packetPlayer).setTicks((int) packet.x));
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
					// open display gui
					else if (packet.type == 7) {
						Minecraft.getMinecraft().displayGuiScreen(new GuiDisplay((int) packet.x));
					}
					// clear Frozen effect
					else if (packet.type == 8) {
						player.removePotionEffect(ModPotions.frozen);
					}
					// Mei's freeze / Reaper's tp
					else if (packet.type == 9 && entity instanceof EntityLivingBase) {
						if (packet.bool) 
							((EntityLivingBase) entity).addPotionEffect(new PotionEffect(ModPotions.frozen, (int) packet.x, 0, false, true));
						if (entity == player)
							TickHandler.register(true, Handlers.PREVENT_INPUT.setEntity(entity).setTicks((int) packet.x),
									Handlers.PREVENT_MOVEMENT.setEntity(entity).setTicks((int) packet.x), 
									Handlers.PREVENT_ROTATION.setEntity(entity).setTicks((int) packet.x));
					}
					// Reaper's wraith
					else if (packet.type == 10 && packetPlayer != null) {
						TickHandler.register(true, Ability.ABILITY_USING.setEntity(packetPlayer).setTicks(60),
								ItemReaperShotgun.WRAITH.setEntity(packetPlayer).setTicks(60));
						if (player == packetPlayer)
							ItemReaperShotgun.wraithViewBobbing.put(packetPlayer, Minecraft.getMinecraft().gameSettings.viewBobbing);
					}
					// wake up from Ana's sleep dart
					else if (packet.type == 11 && entity != null) {
						for (Identifier identifier : new Identifier[] {Identifier.ANA_SLEEP, 
								Identifier.PREVENT_INPUT, Identifier.PREVENT_MOVEMENT, Identifier.PREVENT_ROTATION}) {
							TickHandler.Handler handler = TickHandler.getHandler(entity, identifier);
							if (handler != null)
								handler.ticksLeft = 10;
						}
					}
					// Ana's sleep dart
					else if (packet.type == 12 && entity != null) {
						TickHandler.register(true, ItemAnaRifle.SLEEP.setEntity(entity).setTicks(120),
								Handlers.PREVENT_INPUT.setEntity(entity).setTicks(120),
								Handlers.PREVENT_MOVEMENT.setEntity(entity).setTicks(120),
								Handlers.PREVENT_ROTATION.setEntity(entity).setTicks(120));
						if (entity instanceof EntityLivingBase) 
							Handlers.rotations.put((EntityLivingBase) entity, Triple.of(0f, 0f, 0f));
					}
					// Genji's deflect
					else if (packet.type == 13 && packetPlayer != null) {
						// spawn sweep particle
						double d0 = (double)(-MathHelper.sin(packetPlayer.rotationYaw * 0.017453292F));
						double d1 = (double)MathHelper.cos(packetPlayer.rotationYaw * 0.017453292F);
						packetPlayer.worldObj.spawnParticle(EnumParticleTypes.SWEEP_ATTACK, packetPlayer.posX + d0, packetPlayer.posY + (double)packetPlayer.height * 0.8D, packetPlayer.posZ + d1, 0, d0, 0.0D, new int[0]);
						if (packetPlayer == player)
							TickHandler.register(true, Handlers.ACTIVE_HAND.setEntity(packetPlayer).setTicks(5));
					}
					// Kill/assist messages
					else if (packet.type == 14 && packetPlayer == player && entity != null && 
							(Config.trackKillsOption == 0 || (Config.trackKillsOption == 1 && entity instanceof EntityPlayer))) {
						String string = null;
						if (packet.x == -1)
							string = TextFormatting.BOLD + "" + TextFormatting.ITALIC+"YOU WERE ELIMINATED BY "+
									TextFormatting.DARK_RED + TextFormatting.BOLD + TextFormatting.ITALIC + TextFormatting.getTextWithoutFormattingCodes(entity.getName());
						else
							string = TextFormatting.BOLD + "" + TextFormatting.ITALIC+(packet.bool ? "ASSIST " : "ELIMINATED ") +
							TextFormatting.DARK_RED + TextFormatting.BOLD + TextFormatting.ITALIC + TextFormatting.getTextWithoutFormattingCodes(entity.getName()) +
							TextFormatting.RESET + TextFormatting.BOLD + TextFormatting.ITALIC + " " + (int)packet.x;
						TickHandler.register(true, EnumHero.RenderManager.MESSAGES.
								setString(new String(string).toUpperCase()).setBoolean(packet.bool).
								setEntity(player).setTicks(70+TickHandler.getHandlers(player, Identifier.HERO_MESSAGES).size()*1));
						if (packet.x != -1) {
							TickHandler.register(true, EnumHero.RenderManager.KILL_OVERLAY.setEntity(player).setTicks(10));
							player.playSound(ModSoundEvents.kill, 0.1f, 1.0f);
							TickHandler.Handler handler = TickHandler.getHandler(player, Identifier.HERO_MULTIKILL);
							if (handler == null)
								TickHandler.register(true, EnumHero.RenderManager.MULTIKILL.setEntity(player).setTicks(40).setNumber(1));
							else if (handler.number < 6) {
								handler.setTicks(40);
								handler.setNumber(handler.number+1);
								if (handler.number > 1 && handler.number < 7) {
									for (SoundEvent event : ModSoundEvents.multikill)
										Minewatch.proxy.stopSound(player, event, SoundCategory.PLAYERS);
									Minewatch.proxy.playFollowingSound(player, 
											ModSoundEvents.multikill[(int) (handler.number-2)], SoundCategory.PLAYERS, 1.0f, 1.0f);
								}
							}
						}

					}
					// Damage entity
					else if (packet.type == 15 && packetPlayer == player) {
						TickHandler.Handler handler = TickHandler.getHandler(player, Identifier.HIT_OVERLAY);
						if (handler == null || handler.ticksLeft < 11)
							TickHandler.register(true, EnumHero.RenderManager.HIT_OVERLAY.setEntity(player).setTicks(10).setNumber(packet.x));
						else 
							handler.setNumber(handler.number + packet.x/3d).setTicks(10);
						// play damage sound
						player.playSound(ModSoundEvents.hurt, (float) MathHelper.clamp_double(packet.x/18f, 0.1f, 0.4f), 1.0f);
					}
					// Interrupt
					else if (packet.type == 16 && entity != null) {
						TickHandler.interrupt(entity);
					}
				}
			});
			return null;
		}
	}
}