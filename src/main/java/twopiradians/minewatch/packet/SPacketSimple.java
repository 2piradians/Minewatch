package twopiradians.minewatch.packet;

import java.util.UUID;

import org.apache.commons.lang3.tuple.Triple;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.registry.IThrowableEntity;
import twopiradians.minewatch.client.gui.display.GuiDisplay;
import twopiradians.minewatch.client.gui.tab.GuiTab;
import twopiradians.minewatch.common.CommonProxy.EnumParticle;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.config.Config;
import twopiradians.minewatch.common.entity.EntityLivingBaseMW;
import twopiradians.minewatch.common.entity.EntityMW;
import twopiradians.minewatch.common.entity.ability.EntityAnaGrenade;
import twopiradians.minewatch.common.entity.ability.EntityJunkratMine;
import twopiradians.minewatch.common.entity.ability.EntityJunkratTrap;
import twopiradians.minewatch.common.entity.ability.EntityWidowmakerHook;
import twopiradians.minewatch.common.entity.ability.EntityWidowmakerMine;
import twopiradians.minewatch.common.entity.hero.EntityHero;
import twopiradians.minewatch.common.entity.projectile.EntityJunkratGrenade;
import twopiradians.minewatch.common.hero.Ability;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.hero.RankManager;
import twopiradians.minewatch.common.hero.RankManager.Rank;
import twopiradians.minewatch.common.hero.RenderManager;
import twopiradians.minewatch.common.hero.SetManager;
import twopiradians.minewatch.common.item.weapon.ItemAnaRifle;
import twopiradians.minewatch.common.item.weapon.ItemBastionGun;
import twopiradians.minewatch.common.item.weapon.ItemGenjiShuriken;
import twopiradians.minewatch.common.item.weapon.ItemLucioSoundAmplifier;
import twopiradians.minewatch.common.item.weapon.ItemMWWeapon;
import twopiradians.minewatch.common.item.weapon.ItemMcCreeGun;
import twopiradians.minewatch.common.item.weapon.ItemMeiBlaster;
import twopiradians.minewatch.common.item.weapon.ItemMercyWeapon;
import twopiradians.minewatch.common.item.weapon.ItemMoiraWeapon;
import twopiradians.minewatch.common.item.weapon.ItemReaperShotgun;
import twopiradians.minewatch.common.item.weapon.ItemReinhardtHammer;
import twopiradians.minewatch.common.item.weapon.ItemSombraMachinePistol;
import twopiradians.minewatch.common.item.weapon.ItemTracerPistol;
import twopiradians.minewatch.common.item.weapon.ItemWidowmakerRifle;
import twopiradians.minewatch.common.item.weapon.ItemZenyattaWeapon;
import twopiradians.minewatch.common.potion.ModPotions;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.util.EntityHelper;
import twopiradians.minewatch.common.util.Handlers;
import twopiradians.minewatch.common.util.TickHandler;
import twopiradians.minewatch.common.util.TickHandler.Identifier;

public class SPacketSimple implements IMessage {

	private int type;
	private boolean bool;
	private UUID uuid;
	private double x;
	private double y;
	private double z;
	private double x2;
	private double y2;
	private double z2;
	private double x3;
	private double y3;
	private double z3;
	private double x4;
	private double y4;
	private double z4;
	private int id;
	private int id2;
	private String string;

	public SPacketSimple() { }

	public SPacketSimple(int type) {
		this(type, false, null, 0, 0, 0, null, null);
	}

	public SPacketSimple(int type, boolean bool) {
		this(type, bool, null, 0, 0, 0, null, null);
	}

	public SPacketSimple(int type, Entity entity, boolean bool) {
		this(type, bool, null, 0, 0, 0, entity, null);
	}

	public SPacketSimple(int type, Entity entity, String string) {
		this(type, false, UUID.randomUUID(), 0, 0, 0, entity == null ? -1 : entity.getEntityId(), -1, string);
	}

	public SPacketSimple(int type, UUID uuid, boolean bool) {
		this(type, bool, uuid, 0, 0, 0, -1, -1, null);
	}

	public SPacketSimple(int type, Entity entity, boolean bool, Entity entity2) {
		this(type, bool, null, 0, 0, 0, entity, entity2);
	}

	public SPacketSimple(int type, Entity entity, boolean bool, double x, double y, double z) {
		this(type, bool, null, x, y, z, entity, null);
	}

	public SPacketSimple(int type, boolean bool, EntityPlayer player) {
		this(type, bool, player, 0, 0, 0, null, null);
	}

	public SPacketSimple(int type, boolean bool, EntityPlayer player, double x, double y, double z) {
		this(type, bool, player, x, y, z, null, null);
	}

	public SPacketSimple(int type, boolean bool, EntityPlayer player, double x, double y, double z, Entity entity) {
		this(type, bool, player, x, y, z, entity, null);
	}

	public SPacketSimple(int type, EntityPlayer player, double x, double y, double z) {
		this(type, false, player, x, y, z, null, null);
	}

	public SPacketSimple(int type, EntityPlayer player, double x, double y, double z, Entity entity) {
		this(type, false, player, x, y, z, entity, null);
	}

	public SPacketSimple(int type, boolean bool, EntityPlayer player, double x, double y, double z, Entity entity, Entity entity2) {
		this(type, bool, player == null ? UUID.randomUUID() : player.getPersistentID(), x, y, z, 
				entity == null ? -1 : entity.getEntityId(), entity2 == null ? -1 : entity2.getEntityId(), null);
	}

	public SPacketSimple(int type, boolean bool, UUID playerUUID, double x, double y, double z, int entityID, int entityID2, String string) {
		this.type = type;
		this.bool = bool;
		this.uuid = playerUUID;
		this.id = entityID;
		this.id2 = entityID2;
		this.x = x;
		this.y = y;
		this.z = z;
		this.string = string == null ? "" : string;
	}

	public SPacketSimple(int type, Entity entity, RayTraceResult result) {
		this.type = type;
		this.uuid = UUID.randomUUID();
		this.id = entity == null ? -1 : entity.getEntityId();
		this.id2 = result.entityHit == null ? -1 : result.entityHit.getEntityId();
		this.x = result.hitVec.xCoord;
		this.y = result.hitVec.yCoord;
		this.z = result.hitVec.zCoord;
		this.x2 = result.typeOfHit == null ? -1 : result.typeOfHit.ordinal();
		this.y2 = result.sideHit == null ? -1 : result.sideHit.ordinal();
		this.x3 = entity.posX;
		this.y3 = entity.posY;
		this.z3 = entity.posZ;
		this.x4 = entity.prevPosX;
		this.y4 = entity.prevPosY;
		this.z4 = entity.prevPosZ;
		this.string = "";
		this.bool = entity.isDead;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.type = buf.readInt();
		this.bool = buf.readBoolean();
		this.uuid = UUID.fromString(ByteBufUtils.readUTF8String(buf));
		this.id = buf.readInt();
		this.id2 = buf.readInt();
		this.x = buf.readDouble();
		this.y = buf.readDouble();
		this.z = buf.readDouble();
		this.x2 = buf.readDouble();
		this.y2 = buf.readDouble();
		this.z2 = buf.readDouble();
		this.x3 = buf.readDouble();
		this.y3 = buf.readDouble();
		this.z3 = buf.readDouble();
		this.x4 = buf.readDouble();
		this.y4 = buf.readDouble();
		this.z4 = buf.readDouble();
		this.string = ByteBufUtils.readUTF8String(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(this.type);
		buf.writeBoolean(this.bool);
		ByteBufUtils.writeUTF8String(buf, this.uuid.toString());
		buf.writeInt(this.id);
		buf.writeInt(this.id2);
		buf.writeDouble(this.x);
		buf.writeDouble(this.y);
		buf.writeDouble(this.z);
		buf.writeDouble(this.x2);
		buf.writeDouble(this.y2);
		buf.writeDouble(this.z2);
		buf.writeDouble(this.x3);
		buf.writeDouble(this.y3);
		buf.writeDouble(this.z3);
		buf.writeDouble(this.x4);
		buf.writeDouble(this.y4);
		buf.writeDouble(this.z4);
		ByteBufUtils.writeUTF8String(buf, this.string);
	}

	public static void move(EntityLivingBase player, double scale, boolean useLook, boolean giveMotionY) {
		Vec3d vec = new Vec3d(player.motionX, 0, player.motionZ);
		if (vec.xCoord == 0 && vec.zCoord == 0) 
			vec = new Vec3d(player.getLookVec().xCoord, 0, player.getLookVec().zCoord);
		if (useLook)
			vec = new Vec3d(player.getLookVec().xCoord, player.getLookVec().yCoord, player.getLookVec().zCoord);
		if (!player.onGround && (player instanceof EntityPlayer || giveMotionY)) {
			player.motionY = 0.24d;
			player.velocityChanged = true;
		}
		vec = vec.normalize().scale(scale);
		player.move(MoverType.SELF, vec.xCoord, vec.yCoord, vec.zCoord);
	}

	public static class Handler implements IMessageHandler<SPacketSimple, IMessage> {
		@Override
		public IMessage onMessage(final SPacketSimple packet, final MessageContext ctx) {
			IThreadListener mainThread = Minecraft.getMinecraft();
			mainThread.addScheduledTask(new Runnable() {
				@Override
				public void run() {
					EntityPlayerSP player = Minecraft.getMinecraft().player;
					EntityPlayer packetPlayer = packet.uuid == null ? null : player.world.getPlayerEntityByUUID(packet.uuid);
					Entity entity = packet.id == -1 ? null : player.world.getEntityByID(packet.id);
					Entity entity2 = packet.id2 == -1 ? null : player.world.getEntityByID(packet.id2);

					// Tracer's dash
					if (packet.type == 0 && entity != null) {
						if (entity == player) {
							player.chasingPosX = player.posX;
							player.chasingPosY = player.posY;
							player.chasingPosZ = player.posZ;
							player.setSneaking(false);
							move(player, 9, false, true);
						}
						TickHandler.register(true, ItemTracerPistol.RECOLOR.setEntity(entity).setTicks(20));
					}
					// Reaper's teleport
					else if (packet.type == 1 && entity instanceof EntityLivingBase) {
						entity.rotationPitch = 0;
						TickHandler.register(true, ItemReaperShotgun.TPS.setEntity(entity).setTicks(70).setPosition(new Vec3d(packet.x, packet.y, packet.z)), 
								Ability.ABILITY_USING.setEntity(entity).setTicks(70).setAbility(EnumHero.REAPER.ability1));
						Minewatch.proxy.spawnParticlesReaperTeleport(entity.world, (EntityLivingBase) entity, true, 0);
						Minewatch.proxy.spawnParticlesReaperTeleport(entity.world, (EntityLivingBase) entity, false, 0);
						if (player == entity)
							ItemReaperShotgun.tpThirdPersonView.put(player, Minecraft.getMinecraft().gameSettings.thirdPersonView);
					}
					// McCree's roll
					else if (packet.type == 2 && entity instanceof EntityLivingBase) {
						if (entity == player) {
							player.onGround = true;
							player.movementInput.sneak = true;
						}
						if (packet.bool) {
							TickHandler.register(true, ItemMcCreeGun.ROLL.setEntity(entity).setTicks(10),
									Ability.ABILITY_USING.setEntity(entity).setTicks(10).setAbility(EnumHero.MCCREE.ability2), 
									RenderManager.SNEAKING.setEntity(entity).setTicks(11));
						}
						if (entity == player)
							move((EntityLivingBase) entity, 0.6d, false, false);
					}
					// Genji's strike
					else if (packet.type == 3 && entity != null) {
						TickHandler.register(true, ItemGenjiShuriken.STRIKE.setEntity(entity).setTicks(8),
								ItemGenjiShuriken.SWORD_CLIENT.setEntity(entity).setTicks(8),
								Ability.ABILITY_USING.setEntity(entity).setTicks(8).setAbility(EnumHero.GENJI.ability2), 
								RenderManager.SNEAKING.setEntity(entity).setTicks(9));
						if (entity == player) 
							move(player, 1.8d, false, true);
					}
					// Genji's use sword
					else if (packet.type == 4 && entity != null) {
						if (packet.bool)
							TickHandler.register(true, ItemGenjiShuriken.DEFLECT.setEntity(entity).setTicks((int) packet.x));
						TickHandler.register(true, ItemGenjiShuriken.SWORD_CLIENT.setEntity(entity).setTicks((int) packet.x));
						TickHandler.register(true, Ability.ABILITY_USING.setEntity(entity).setTicks((int) packet.x).
								setAbility(packet.bool ? EnumHero.GENJI.ability1 : null));
					}
					// Reinhardt's hammer swing
					else if (packet.type == 5) {
						Minewatch.proxy.mouseClick();
					}
					// Sync playersUsingAlt
					else if (packet.type == 6 && packetPlayer != null) {
						ItemStack stack = packetPlayer.getHeldItemMainhand();
						//ItemMWWeapon.setAlternate(stack, packet.bool);
						// cause reequip animation if player
						if (stack != null && stack.getItem() instanceof ItemMWWeapon)
							((ItemMWWeapon)stack.getItem()).reequipAnimation(stack);
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
					else if (packet.type == 10 && entity != null) {
						TickHandler.register(true, Ability.ABILITY_USING.setEntity(entity).setTicks(60).setAbility(EnumHero.REAPER.ability2),
								ItemReaperShotgun.WRAITH.setEntity(entity).setTicks(60), Handlers.INVULNERABLE.setEntity(entity).setTicks(60));
						if (player == entity)
							ItemReaperShotgun.wraithViewBobbing.put(player, Minecraft.getMinecraft().gameSettings.viewBobbing);
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
					else if (packet.type == 13 && entity != null) {
						// spawn sweep particle
						double d0 = (double)(-MathHelper.sin(entity.rotationYaw * 0.017453292F));
						double d1 = (double)MathHelper.cos(entity.rotationYaw * 0.017453292F);
						entity.world.spawnParticle(EnumParticleTypes.SWEEP_ATTACK, entity.posX + d0, entity.posY + (double)entity.height * 0.8D, entity.posZ + d1, 0, d0, 0.0D, new int[0]);
						if (entity == player)
							TickHandler.register(true, Handlers.ACTIVE_HAND.setEntity(entity).setTicks(5));
						if (entity2 instanceof IThrowableEntity)
							((IThrowableEntity)entity2).setThrower(entity);
					}
					// Kill/assist messages
					else if (packet.type == 14 && packetPlayer == player && entity != null && 
							(Config.trackKillsOption == 0 || (Config.trackKillsOption == 1 && entity instanceof EntityPlayer))) {
						String string = null;
						String name = entity.getName().equalsIgnoreCase("entity.zombie.name") ? "Zombie Villager" : entity.getName();
						if (packet.x == -1)
							string = TextFormatting.BOLD + "" + TextFormatting.ITALIC+"YOU WERE ELIMINATED BY "+
									TextFormatting.DARK_RED + TextFormatting.BOLD + TextFormatting.ITALIC + TextFormatting.getTextWithoutFormattingCodes(name);
						else
							string = TextFormatting.BOLD + "" + TextFormatting.ITALIC+(packet.bool ? "ASSIST " : "ELIMINATED ") +
							TextFormatting.DARK_RED + TextFormatting.BOLD + TextFormatting.ITALIC + TextFormatting.getTextWithoutFormattingCodes(name) +
							TextFormatting.RESET + TextFormatting.BOLD + TextFormatting.ITALIC + " " + (int)packet.x;
						TickHandler.register(true, RenderManager.MESSAGES.
								setString(new String(string).toUpperCase()).setBoolean(packet.bool).
								setEntity(player).setTicks(70+TickHandler.getHandlers(player, Identifier.HERO_MESSAGES).size()*1));
						if (packet.x != -1) {
							TickHandler.register(true, RenderManager.KILL_OVERLAY.setEntity(player).setTicks(10));
							ModSoundEvents.KILL.playSound(player, 0.1f, 1, true);
							if (!(entity instanceof EntityLivingBaseMW)) {
								TickHandler.Handler handler = TickHandler.getHandler(player, Identifier.HERO_MULTIKILL);
								if (handler == null)
									TickHandler.register(true, RenderManager.MULTIKILL.setEntity(player).setTicks(40).setNumber(1));
								else if (handler.number < 6) {
									handler.setTicks(40);
									handler.setNumber(handler.number+1);
									if (handler.number > 1 && handler.number < 7) {
										ModSoundEvents.MULTIKILL_2.stopSound(player);
										ModSoundEvents.MULTIKILL_3.stopSound(player);
										ModSoundEvents.MULTIKILL_4.stopSound(player);
										ModSoundEvents.MULTIKILL_5.stopSound(player);
										ModSoundEvents.MULTIKILL_6.stopSound(player);
										ModSoundEvents.valueOf("MULTIKILL_"+(int)handler.number).playFollowingSound(player, 1, 1, false);
									}
								}
							}
						}

					}
					// Damage entity
					else if (packet.type == 15 && packetPlayer == player) {
						TickHandler.Handler handler = TickHandler.getHandler(player, Identifier.HIT_OVERLAY);
						if (handler == null || handler.ticksLeft < 11)
							TickHandler.register(true, RenderManager.HIT_OVERLAY.setEntity(player).setTicks(10).setNumber(packet.x));
						else 
							handler.setNumber(handler.number + packet.x/3d).setTicks(10);
						// play damage sound
						ModSoundEvents.HURT.playSound(player, (float) MathHelper.clamp(packet.x/18f, 0.1f, 0.4f), 1.0f, true);
					}
					// Interrupt
					else if (packet.type == 16 && entity != null) {
						TickHandler.interrupt(entity);
					}
					// sync config
					else if (packet.type == 17) {
						Minewatch.network.sendToServer(new PacketSyncConfig());
					}
					// add opped button to tab
					else if (packet.type == 18) {
						GuiTab.addOppedButtons();
					}
					// Mercy's Angel
					else if (packet.type == 19 && entity != null) {
						if (packet.bool)
							ModSoundEvents.MERCY_ANGEL_VOICE.playFollowingSound(entity, 1, 1, false);
						ModSoundEvents.MERCY_ANGEL.playFollowingSound(entity, 1, 1, false);
						TickHandler.register(true, ItemMercyWeapon.ANGEL.setPosition(new Vec3d(packet.x, packet.y, packet.z)).setTicks(75).setEntity(entity),
								Ability.ABILITY_USING.setTicks(75).setEntity(entity).setAbility(EnumHero.MERCY.ability3));
					}
					// Junkrat's grenade bounce
					else if (packet.type == 20 && entity instanceof EntityJunkratGrenade) {
						// direct hit
						if (packet.bool && entity2 instanceof Entity) {
							((EntityJunkratGrenade)entity).explode(null);
							ModSoundEvents.JUNKRAT_GRENADE_EXPLODE.playSound(entity, 1, 1);
						}
						// bounce
						else {
							((EntityJunkratGrenade)entity).bounces = (int) packet.x;
							if (packet.x == 0) 
								ModSoundEvents.JUNKRAT_GRENADE_TICK_0.playFollowingSound(entity, 1, 1, true);
							else if (packet.x == 1) 
								ModSoundEvents.JUNKRAT_GRENADE_TICK_1.playFollowingSound(entity, 1, 1, true);
							else if (packet.x == 2) 
								ModSoundEvents.JUNKRAT_GRENADE_TICK_2.playFollowingSound(entity, 1, 1, true);
							else
								ModSoundEvents.JUNKRAT_GRENADE_TICK_3.playSound(entity, 1, 1);
							Minewatch.proxy.spawnParticlesCustom(EnumParticle.SPARK, entity.world, entity.posX, entity.posY, entity.posZ,
									0, 0, 0, 0xFCCD75, 0xFFDA93, 0.7f, 2, 3, 2.5f, entity.world.rand.nextFloat(), 0.01f);
							ModSoundEvents.JUNKRAT_GRENADE_BOUNCE.playSound(entity, 0.7f, 1);
						}
					}
					// Shoot Ana's sleep dart
					else if (packet.type == 21 && packetPlayer == player && player != null) {
						TickHandler.register(true, Ability.ABILITY_USING.setEntity(player).setTicks((int)packet.x).setAbility(EnumHero.ANA.ability2));
					}
					// Unused
					else if (packet.type == 22 && packetPlayer != null) {}
					// Frozen particles
					else if (packet.type == 23 && entity != null) {
						for (int i=0; i<3; ++i)
							Minewatch.proxy.spawnParticlesCustom(EnumParticle.CIRCLE, entity.world, 
									packet.x+entity.world.rand.nextDouble()-0.5d, 
									packet.y+entity.world.rand.nextDouble()-0.5d, 
									packet.z+entity.world.rand.nextDouble()-0.5d, 
									0, 0.01f, 0, 0x5BC8E0, 0xAED4FF,
									entity.world.rand.nextFloat(), 5, 20f, 25f, 0, 0);
					}
					// Junkrat death grenades
					else if (packet.type == 24 && entity instanceof EntityJunkratGrenade) {
						((EntityJunkratGrenade)entity).explodeTimer = (int) packet.x;
						((EntityJunkratGrenade)entity).isDeathGrenade = true;
					}
					// Junkrat trap
					else if (packet.type == 25 && entity instanceof EntityJunkratTrap && entity2 instanceof EntityLivingBase) {
						if (packet.bool) {
							((EntityJunkratTrap)entity).trappedEntity = (EntityLivingBase) entity2;
							TickHandler.register(true, Handlers.PREVENT_MOVEMENT.setTicks(70).setEntity(entity2),
									EntityJunkratTrap.TRAPPED.setTicks(70).setEntity(entity2));
							if (((EntityJunkratTrap)entity).getThrower() == player) {
								ModSoundEvents.JUNKRAT_TRAP_PLACED_VOICE.stopSound(player);
								ModSoundEvents.JUNKRAT_TRAP_TRIGGER_OWNER.playFollowingSound(player, 1, 1, false);
								ModSoundEvents.JUNKRAT_TRAP_TRIGGER_VOICE.playFollowingSound(player, 1, 1, false);
							}
						}
						if (packetPlayer == player && player != null)
							Minewatch.proxy.spawnParticlesCustom(EnumParticle.JUNKRAT_TRAP_TRIGGERED, player.world, entity.posX, entity.posY+1.5d, entity.posZ, 0, 0, 0, 0xFFFFFF, 0xFFFFFF, 1, 80, 5, 5, 0, 0);
					}
					// Junkrat trap destroyed
					else if (packet.type == 26 && entity instanceof EntityJunkratTrap) {
						if (packet.bool)
							for (int i=0; i<30; ++i)
								entity.world.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, 
										entity.posX+(entity.world.rand.nextDouble()-0.5d)*1d, entity.posY+entity.world.rand.nextDouble()*1d, entity.posZ+(entity.world.rand.nextDouble()-0.5d)*1d, 0, 0, 0, new int[0]);
						else
							TickHandler.unregister(true, 
									TickHandler.getHandler(((EntityJunkratTrap)entity).trappedEntity, Identifier.PREVENT_MOVEMENT),
									TickHandler.getHandler(((EntityJunkratTrap)entity).trappedEntity, Identifier.JUNKRAT_TRAP));
						entity.setDead();
					}
					// Sombra's invisibility
					else if (packet.type == 27 && entity != null) {
						if (packet.bool) {
							TickHandler.register(true, ItemSombraMachinePistol.INVISIBLE.setEntity(entity).setTicks(130),
									Ability.ABILITY_USING.setEntity(entity).setTicks(120).setAbility(EnumHero.SOMBRA.ability3));
							if (entity == player)
								ModSoundEvents.SOMBRA_INVISIBLE_START.playFollowingSound(entity, 1, 1, false);
						}
						else if (entity instanceof EntityLivingBase)
							ItemSombraMachinePistol.cancelInvisibility((EntityLivingBase) entity);
					}
					// Widowmaker's venom trap
					else if (packet.type == 28 && entity != null && entity2 instanceof EntityLivingBase) {
						TickHandler.register(true, EntityWidowmakerMine.POISONED.setTicks(100).setEntity(entity).setEntityLiving((EntityLivingBase) entity2));
						if (entity2 == player && player != null) 
							Minewatch.proxy.spawnParticlesCustom(EnumParticle.WIDOWMAKER_MINE_TRIGGERED, entity2.world, packet.x, packet.y+1, packet.z, 0, 0, 0, 0xFFFFFF, 0xFFFFFF, 1, 80, 5, 5, 0, 0);
					}
					// Sombra's teleport
					else if (packet.type == 29 && entity != null) {
						TickHandler.register(true, ItemSombraMachinePistol.TELEPORT.setEntity(entity).setTicks(10).
								setPosition(new Vec3d(packet.x, packet.y, packet.z)));
					}
					// Junkrat's mine explosion
					else if (packet.type == 30 && entity instanceof EntityJunkratMine) {
						((EntityJunkratMine)entity).explode();
					}
					// Bastion's reconfigure
					else if (packet.type == 31 && entity instanceof EntityLivingBase) {
						ItemMWWeapon.setAlternate(((EntityLivingBase)entity).getHeldItemMainhand(), packet.bool);
						if (packet.bool) {
							TickHandler.register(true, ItemBastionGun.TURRET.setEntity(entity).setTicks(10));
							ModSoundEvents.BASTION_RECONFIGURE_1.playFollowingSound(entity, 1, 1, false);
						}
						else
							ModSoundEvents.BASTION_RECONFIGURE_0.playFollowingSound(entity, 1, 1, false);
					}
					// Mei's cryo-freeze
					else if (packet.type == 32 && entity != null) {
						if (packet.bool) {
							if (entity == player) {
								ItemMeiBlaster.thirdPersonView = Minecraft.getMinecraft().gameSettings.thirdPersonView;
								TickHandler.register(true, Ability.ABILITY_USING.setEntity(entity).setTicks(80).setAbility(EnumHero.MEI.ability2));
							}
							TickHandler.register(true, ItemMeiBlaster.CRYSTAL.setEntity(entity).setTicks(80),
									Handlers.PREVENT_MOVEMENT.setEntity(entity).setTicks(80),
									Handlers.PREVENT_INPUT.setEntity(entity).setTicks(80),
									Handlers.PREVENT_ROTATION.setEntity(entity).setTicks(80));
							ModSoundEvents.MEI_CRYSTAL_START.playFollowingSound(entity, 1, 1, false);
						}
						else 
							TickHandler.unregister(true, TickHandler.getHandler(entity, Identifier.MEI_CRYSTAL),
									TickHandler.getHandler(entity, Identifier.PREVENT_MOVEMENT),
									TickHandler.getHandler(entity, Identifier.PREVENT_INPUT),
									TickHandler.getHandler(entity, Identifier.PREVENT_ROTATION),
									TickHandler.getHandler(entity, Identifier.ABILITY_USING));
					}
					// Reinhardt's fire strike
					else if (packet.type == 33 && entity != null) {
						TickHandler.register(true, ItemReinhardtHammer.STRIKE.setEntity(entity).setTicks(13));
						if (entity == player)
							TickHandler.register(true, Ability.ABILITY_USING.setEntity(player).setTicks(13).setAbility(EnumHero.REINHARDT.ability2));
						ModSoundEvents.REINHARDT_STRIKE_THROW.playFollowingSound(entity, 1, 1, false);
					}
					// Set entity's position
					else if (packet.type == 34 && entity != null) {
						entity.setPosition(packet.x, packet.y, packet.z);
					}
					// Sombra's translocator
					else if (packet.type == 35 && entity instanceof EntityLivingBase && entity2 != null) {
						TickHandler.register(true, Ability.ABILITY_USING.setAbility(EnumHero.SOMBRA.ability2).setTicks(10).setEntity(entity));
						if (EnumHero.SOMBRA.ability2.entities.get(entity) == null || 
								EnumHero.SOMBRA.ability2.entities.get(entity).getEntityId() != entity2.getEntityId())
							EnumHero.SOMBRA.ability2.entities.put((EntityLivingBase) entity, entity2);
					}
					// EntityHero item cooldown handler
					else if (packet.type == 36 && entity instanceof EntityHero) {
						ItemStack main = ((EntityHero)entity).getHeldItemMainhand();
						ItemStack off = ((EntityHero)entity).getHeldItemOffhand();
						if (main != null && main.getItem() instanceof ItemMWWeapon)
							((ItemMWWeapon)main.getItem()).setCooldown(entity, (int) packet.x);
						else if (off != null && off.getItem() instanceof ItemMWWeapon)
							((ItemMWWeapon)off.getItem()).setCooldown(entity, (int) packet.x);
					}
					// Lucio's amp
					else if (packet.type == 37) {
						TickHandler.register(true, 
								Ability.ABILITY_USING.setEntity(player).setTicks(60).setAbility(EnumHero.LUCIO.ability2));
					}
					// Lucio's soundwave
					else if (packet.type == 38 && entity instanceof EntityLivingBase) {
						Minewatch.proxy.spawnParticlesMuzzle(EnumParticle.CIRCLE, entity.world, (EntityLivingBase) entity, 
								0xE2EA7D, 0xABBF78, 1, 13, 1, 10, 0, 0, EnumHand.MAIN_HAND, 17, 0.65f);
						Minewatch.proxy.spawnParticlesMuzzle(EnumParticle.CIRCLE, entity.world, (EntityLivingBase) entity, 
								0xE2EA7D, 0xABBF78, 0.9f, 13, 6, 10, 0, 0, EnumHand.MAIN_HAND, 17, 0.65f);
						Minewatch.proxy.spawnParticlesMuzzle(EnumParticle.HOLLOW_CIRCLE, entity.world, (EntityLivingBase) entity, 
								0xDFED8B, 0xABBF78, 0.7f, 15, 5+(entity.world.rand.nextFloat()-0.5f)*2f, 7+(entity.world.rand.nextFloat()-0.5f)*2f, entity.world.rand.nextFloat(), entity.world.rand.nextFloat()/10f, EnumHand.MAIN_HAND, 17, 0.65f);
						Minewatch.proxy.spawnParticlesMuzzle(EnumParticle.HOLLOW_CIRCLE, entity.world, (EntityLivingBase) entity, 
								0xDFED8B, 0xABBF78, 0.7f, 14, 2+(entity.world.rand.nextFloat()-0.5f)*2f, 6+(entity.world.rand.nextFloat()-0.5f)*2f, entity.world.rand.nextFloat(), entity.world.rand.nextFloat()/10f, EnumHand.MAIN_HAND, 17, 0.65f);
						Minewatch.proxy.spawnParticlesMuzzle(EnumParticle.HOLLOW_CIRCLE, entity.world, (EntityLivingBase) entity, 
								0xDFED8B, 0xABBF78, 0.7f, 13, 3+(entity.world.rand.nextFloat()-0.5f)*2f, 6+(entity.world.rand.nextFloat()-0.5f)*2f, entity.world.rand.nextFloat(), entity.world.rand.nextFloat()/10f, EnumHand.MAIN_HAND, 17, 0.65f);
						ItemLucioSoundAmplifier.soundwave((EntityLivingBase) entity, entity.motionX, entity.motionY, entity.motionZ);							
						ModSoundEvents.LUCIO_SOUNDWAVE.playFollowingSound(entity, 1, 1, false);
					}
					// Lucio's soundwave (player causing it)
					else if (packet.type == 39 && packetPlayer != null) {
						Minewatch.network.sendToServer(new CPacketSimple(4, false, packetPlayer, packetPlayer.motionX, packetPlayer.motionY, packetPlayer.motionZ));
					}
					// Hurt sound for Mercy's power beam
					else if (packet.type == 40 && entity != null) {
						ModSoundEvents.HURT.playSound(entity, 0.3f, entity.world.rand.nextFloat()/2+0.75f, true);
					}
					// Entity collision raytraceresult onImpact
					else if (packet.type == 41 && entity instanceof EntityMW) {
						if (packet.bool)
							entity.setDead();
						entity.setPosition(packet.x3, packet.y3, packet.z3);
						if (entity.ticksExisted == 0) {
							entity.prevPosX = packet.x4;
							entity.prevPosY = packet.y4;
							entity.prevPosZ = packet.z4;
						}
						Vec3d hitVec = new Vec3d(packet.x, packet.y, packet.z);
						RayTraceResult.Type typeOfHit = packet.x2 >= 0 && packet.x2 < RayTraceResult.Type.values().length ? 
								RayTraceResult.Type.values()[(int) packet.x2] : null;
								EnumFacing sideHit = packet.y2 >= 0 && packet.y2 < EnumFacing.values().length ? 
										EnumFacing.values()[(int) packet.y2] : null;
										if (typeOfHit == RayTraceResult.Type.BLOCK && sideHit != null)
											((EntityMW)entity).onImpact(new RayTraceResult(hitVec, sideHit, new BlockPos(hitVec)));
										else if (typeOfHit == RayTraceResult.Type.ENTITY && entity2 != null)
											((EntityMW)entity).onImpact(new RayTraceResult(entity2, hitVec));
					}
					// Zenyatta's Harmony (entity is zen, entity2 is target)
					else if (packet.type == 42 && entity != null && entity2 instanceof EntityLivingBase) {
						// refresh / start
						if (packet.bool) {
							// remove discord by same player
							TickHandler.Handler discord = TickHandler.getHandler(entity, Identifier.ZENYATTA_DISCORD);
							if (discord != null && discord.entityLiving == entity2)
								TickHandler.unregister(false, discord);
							// apply harmony
							TickHandler.Handler handler = TickHandler.getHandler(entity, Identifier.ZENYATTA_HARMONY);
							if (handler != null) 
								handler.setTicks(60).setEntityLiving((EntityLivingBase) entity2);
							else {
								TickHandler.register(true, ItemZenyattaWeapon.HARMONY.setTicks(60).setEntity(entity).setEntityLiving((EntityLivingBase) entity2));
								Minewatch.proxy.spawnParticlesCustom(EnumParticle.ZENYATTA_HARMONY_ORB, entity.world, entity2, 0xFFFFFF, 0xFFFFFF, 1.0f, Integer.MAX_VALUE, 3, 3, 0, 0);
								if (entity == player && EntityHelper.isHoldingItem(player, ItemZenyattaWeapon.class, EnumHand.MAIN_HAND)) {
									ItemZenyattaWeapon.animatingHarmony = player.getHeldItemMainhand();
									ItemZenyattaWeapon.animatingDiscord = null;
									ItemZenyattaWeapon.animatingTime = player.ticksExisted + ItemZenyattaWeapon.ANIMATION_TIME;
								}
							}
							if (entity == player) {
								EnumHero.ZENYATTA.ability1.entities.put((EntityLivingBase) entity, entity2);
								Minewatch.proxy.spawnParticlesCustom(EnumParticle.ZENYATTA_HARMONY, entity.world, entity2, 0xFFFFFF, 0xFFFFFF, 1.0f, Integer.MAX_VALUE, 1, 1, 0, 0);
							}
						}
						// end
						else {
							EnumHero.ZENYATTA.ability1.entities.remove(entity);
							TickHandler.Handler handler = TickHandler.getHandler(entity, Identifier.ZENYATTA_HARMONY);
							if (handler != null) {
								TickHandler.unregister(true, handler);
								if (entity == player && packet.x <= 0)
									ModSoundEvents.ZENYATTA_HEAL_RETURN.playFollowingSound(entity, 1.0f, 1.0f, false);
							}
						}
					}
					// Zenyatta's Discord (entity is zen, entity2 is target)
					else if (packet.type == 43 && entity instanceof EntityLivingBase && entity2 instanceof EntityLivingBase) {
						// refresh / start
						if (packet.bool) {
							// remove harmony by same player
							TickHandler.Handler harmony = TickHandler.getHandler(entity, Identifier.ZENYATTA_HARMONY);
							if (harmony != null && harmony.entityLiving == entity2)
								TickHandler.unregister(false, harmony);
							// apply discord
							TickHandler.Handler handler = TickHandler.getHandler(entity, Identifier.ZENYATTA_DISCORD);
							if (handler != null) 
								handler.setTicks(60).setEntityLiving((EntityLivingBase) entity2);
							else {
								TickHandler.register(true, ItemZenyattaWeapon.DISCORD.setTicks(60).setEntity(entity).setEntityLiving((EntityLivingBase) entity2));
								Minewatch.proxy.spawnParticlesCustom(EnumParticle.ZENYATTA_DISCORD_ORB, entity.world, entity2, 0xFFFFFF, 0xFFFFFF, 1.0f, Integer.MAX_VALUE, 3, 3, 0, 0);
								if (entity == player && EntityHelper.isHoldingItem(player, ItemZenyattaWeapon.class, EnumHand.OFF_HAND)) {
									ItemZenyattaWeapon.animatingDiscord = player.getHeldItemOffhand();
									ItemZenyattaWeapon.animatingHarmony = null;
									ItemZenyattaWeapon.animatingTime = player.ticksExisted + ItemZenyattaWeapon.ANIMATION_TIME;
								}							
							}
							if (entity == player) {
								EnumHero.ZENYATTA.ability2.entities.put((EntityLivingBase) entity, entity2);
								Minewatch.proxy.spawnParticlesCustom(EnumParticle.ZENYATTA_DISCORD, entity.world, entity2, 0xFFFFFF, 0xFFFFFF, 1.0f, Integer.MAX_VALUE, 1, 1, 0, 0);
							}
						}
						// end
						else {
							EnumHero.ZENYATTA.ability2.entities.remove(entity);
							TickHandler.Handler handler = TickHandler.getHandler(entity, Identifier.ZENYATTA_DISCORD);
							if (handler != null) {
								TickHandler.unregister(true, handler);
								if (entity == player && packet.x <= 0)
									ModSoundEvents.ZENYATTA_DAMAGE_RETURN.playFollowingSound(entity, 1.0f, 1.0f, false);
							}
						}
					}
					// health plus particles
					else if (packet.type == 44 && entity != null) {
						EntityHelper.spawnHealParticles(entity);
					}

					// Team Selector send message
					else if (packet.type == 45 && packet.string != null) {
						ITextComponent component = new TextComponentString(TextFormatting.GREEN+"[Team Stick] "+TextFormatting.RESET+packet.string);
						Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(component, 92);
					}
					// sync weapon charge
					else if (packet.type == 46) {
						EnumHero hero = SetManager.getWornSet(player);
						if (hero != null)
							hero.weapon.setCurrentCharge(player, (float) packet.x, false);
					}
					// Moira's Fade
					else if (packet.type == 47 && entity != null) {
						TickHandler.register(true, Ability.ABILITY_USING.setEntity(entity).setTicks(16).setAbility(EnumHero.MOIRA.ability3),
								ItemMoiraWeapon.FADE.setEntity(entity).setTicks(16), Handlers.INVULNERABLE.setEntity(entity).setTicks(16));
						TickHandler.unregister(true, TickHandler.getHandler(entity, Identifier.MOIRA_DAMAGE));
						if (player == entity)
							ItemMoiraWeapon.fadeViewBobbing.put(player, Minecraft.getMinecraft().gameSettings.viewBobbing);
					}
					// Moira's damage
					else if (packet.type == 48 && entity != null) {
						if (packet.bool)
							TickHandler.register(true, ItemMoiraWeapon.DAMAGE.setEntity(entity).setEntityLiving(entity2 instanceof EntityLivingBase ? (EntityLivingBase) entity2 : null).setTicks(10));
						else
							TickHandler.unregister(true, TickHandler.getHandler(entity, Identifier.MOIRA_DAMAGE));
					}
					// Moira's orb select
					else if (packet.type == 49 && entity != null) {
						if (packet.bool) {
							TickHandler.register(true, ItemMoiraWeapon.ORB_SELECT.setEntity(entity).setTicks(12000));
							Minewatch.proxy.spawnParticlesCustom(EnumParticle.MOIRA_ORB, entity.world, entity, 0xFF50FF, 0xFF50FF, 0.99f, 12000, 2, 2, 0, 0.05f);
							Minewatch.proxy.spawnParticlesCustom(EnumParticle.MOIRA_ORB, entity.world, entity, 0xFBF235, 0xFBF235, 1, 12000, 2, 2, 0, -0.05f);
							Minewatch.proxy.spawnParticlesCustom(EnumParticle.MOIRA_ORB, entity.world, entity, 0x251A60, 0x251A60, 0.99f, 12000, 2, 2, 0, 0.05f);
							Minewatch.proxy.spawnParticlesCustom(EnumParticle.MOIRA_ORB, entity.world, entity, 0xFBF235, 0xFBF235, 1, 12000, 2, 2, 0, -0.05f);
						}
						else
							TickHandler.unregister(true, TickHandler.getHandler(entity, Identifier.MOIRA_ORB_SELECT));
					}
					// Select hero voice line
					else if (packet.type == 50 && packetPlayer == player && packet.x >= 0 && packet.x < EnumHero.values().length) {
						EnumHero hero = EnumHero.values()[(int) packet.x];
						if (hero != null && hero.selectSound != null)
							hero.selectSound.playFollowingSound(player, 0.5f, 1.0f, false);
					}
					// McCree's fan the hammer
					else if (packet.type == 51 && entity != null) {
						if (packet.bool)
							TickHandler.register(true, ItemMcCreeGun.FAN.setEntity(entity).setTicks(5));
						else
							TickHandler.unregister(true, TickHandler.getHandler(entity, Identifier.MCCREE_FAN));
					}
					// McCree's flashbang
					else if (packet.type == 52 && entity != null) {
						float size = Math.min(entity.height, entity.width)*9f;
						Minewatch.proxy.spawnParticlesCustom(EnumParticle.STUN, entity.world, entity, 0xFFFFFF, 0xFFFFFF, 0.9f, 14, size, size, 0, 0);
						TickHandler.register(true, Handlers.PREVENT_INPUT.setEntity(entity).setTicks(17),
								Handlers.PREVENT_MOVEMENT.setEntity(entity).setTicks(17),
								Handlers.PREVENT_ROTATION.setEntity(entity).setTicks(17));
					}
					// Ana's grenade
					else if (packet.type == 53 && entity != null) {
						if (packet.bool) {
							Minewatch.proxy.spawnParticlesCustom(EnumParticle.ANA_GRENADE_HEAL, entity.world, entity, 0xFFFFFF, 0xFFFFFF, 1, 80, 2, 2, 0, 0);
							TickHandler.register(true, EntityAnaGrenade.HEAL.setEntity(entity).setTicks(80));
							if (entity == player)
								ModSoundEvents.ANA_GRENADE_HEAL.playFollowingSound(entity, 0.2f, 1, false);
						}
						else {
							Minewatch.proxy.spawnParticlesCustom(EnumParticle.ANA_GRENADE_DAMAGE, entity.world, entity, 0xFFFFFF, 0xFFFFFF, 1, 80, 2, 2, 0, 0);
							TickHandler.register(true, EntityAnaGrenade.DAMAGE.setEntity(entity).setTicks(80).setNumber(packet.x));
							if (entity == player)
								ModSoundEvents.ANA_GRENADE_DAMAGE.playFollowingSound(entity, 1, 1, false);
						}
					}
					// Tracer's recall
					else if (packet.type == 54 && entity instanceof EntityLivingBase) {
						Minewatch.proxy.spawnParticlesCustom(EnumParticle.HOLLOW_CIRCLE_3, entity.world, entity.posX, entity.posY+entity.height/2f, entity.posZ, 0, 0, 0, 0x63B8E8, 0x4478AD, 1, 7, 20, 0, 0, 0.5f);
						Minewatch.proxy.spawnParticlesCustom(EnumParticle.HOLLOW_CIRCLE_2, entity.world, entity.posX, entity.posY+entity.height/2f, entity.posZ, 0, 0, 0, 0x63B8E8, 0x4478AD, 1, 7, 20, 0, 0, 0.5f);
						Minewatch.proxy.spawnParticlesCustom(EnumParticle.SPARK, entity.world, entity.posX, entity.posY+entity.height/2f, entity.posZ, 0, 0, 0, 0x63B8E8, 0x63B8E8, 1, 7, 15, 5, 0, 0.5f);
						Minewatch.proxy.spawnParticlesCustom(EnumParticle.SPARK, entity.world, entity.posX, entity.posY+entity.height/2f, entity.posZ, 0, 0, 0, 0xD2FFFF, 0xEAFFFF, 1, 7, 10, 5, 0, 0.8f);
						Minewatch.proxy.spawnParticlesCustom(EnumParticle.SPARK, entity.world, entity.posX, entity.posY+entity.height/2f, entity.posZ, 0, 0, 0, 0xD2FFFF, 0xEAFFFF, 1, 15, 0, 10, 0, 0.1f);
						((EntityLivingBase)entity).addPotionEffect(new PotionEffect(MobEffects.INVISIBILITY, 30, 0, false, false));
						entity.extinguish();
						TickHandler.unregister(true, TickHandler.getHandler(entity, Identifier.ANA_GRENADE_DAMAGE));
						TickHandler.register(true, ItemTracerPistol.RECALL.setEntity(entity).setTicks(35), 
								Handlers.PREVENT_INPUT.setEntity(entity).setTicks(30),
								Handlers.PREVENT_MOVEMENT.setEntity(entity).setTicks(30),
								Ability.ABILITY_USING.setEntity(entity).setTicks(30).setAbility(EnumHero.TRACER.ability1),
								Handlers.INVULNERABLE.setEntity(entity).setTicks(30));		
					}
					// Widowmaker's hook
					else if (packet.type == 55 && entity instanceof EntityLivingBase && entity2 instanceof EntityWidowmakerHook) {
						TickHandler.register(true, ItemWidowmakerRifle.HOOK.setEntity(entity2).setEntityLiving((EntityLivingBase) entity).setTicks(100),
								Ability.ABILITY_USING.setEntity(entity).setTicks(100).setAbility(EnumHero.WIDOWMAKER.ability2));
					}
					// Reinhardt's charge
					else if (packet.type == 56 && entity instanceof EntityLivingBase) {
						TickHandler.Handler handler = TickHandler.getHandler(entity, Identifier.REINHARDT_CHARGE);
						// register charge / updated pinned entity
						if (packet.bool && (entity2 == null || entity2 instanceof EntityLivingBase)) {
							if (handler == null) {
								((EntityLivingBase)entity).rotationYaw = (float) packet.x;
								ModSoundEvents.REINHARDT_CHARGE.playFollowingSound(entity, 1, 1, false);
								TickHandler.register(true, ItemReinhardtHammer.CHARGE.setEntity(entity).setEntityLiving((EntityLivingBase) entity2).setTicks(80),
										Handlers.ACTIVE_HAND.setEntity(entity).setTicks(80),
										Handlers.PREVENT_ROTATION.setEntity(entity).setTicks(80), 
										Handlers.PREVENT_MOVEMENT.setEntity(entity).setTicks(80).setBoolean(true),
										RenderManager.SNEAKING.setEntity(entity).setTicks(80));
								if (entity == player)
									TickHandler.register(true, Ability.ABILITY_USING.setEntity(entity).setTicks(80).setAbility(EnumHero.REINHARDT.ability3),
											Handlers.FORCE_VIEW.setEntity(entity).setTicks(80).setNumber(3));
							}
							else {
								TickHandler.interrupt(entity2);
								handler.entityLiving = (EntityLivingBase) entity2;
								handler.entityLiving.rotationPitch = 0;
								handler.entityLiving.rotationYaw = MathHelper.wrapDegrees(entity.rotationYaw+180f);
								handler.entityLiving.prevRotationYaw = handler.entityLiving.rotationYaw;
								handler.entityLiving.prevRotationPitch = handler.entityLiving.rotationPitch;
								TickHandler.register(true, Handlers.PREVENT_INPUT.setEntity(entity2).setTicks(handler.ticksLeft),
										Handlers.PREVENT_ROTATION.setEntity(entity2).setTicks(handler.ticksLeft), 
										Handlers.PREVENT_MOVEMENT.setEntity(entity2).setTicks(handler.ticksLeft),
										Handlers.FORCE_VIEW.setEntity(entity2).setTicks(handler.ticksLeft).setNumber(3));
							}
						}
						// unregister charge
						else if (!packet.bool) {
							// stop sound
							ModSoundEvents.REINHARDT_CHARGE.stopFollowingSound(entity);
							// spawn particle
							RayTraceResult result = EntityHelper.getMouseOverBlock((EntityLivingBase) entity, 3, 0, entity.getRotationYawHead());
							if (result != null) {
								double x = result.hitVec.xCoord; 
								double y = result.hitVec.yCoord;
								double z = result.hitVec.zCoord;
								if (result.sideHit == EnumFacing.SOUTH)
									z = Math.ceil(z);
								else if (result.sideHit == EnumFacing.EAST)
									x = Math.ceil(x);
								else if (result.sideHit == EnumFacing.UP)
									y = Math.ceil(y);
								Vec3d pos = new Vec3d(x, y, z);
								Minewatch.proxy.spawnParticlesCustom(EnumParticle.REINHARDT_CHARGE, entity.world, pos.xCoord, pos.yCoord, pos.zCoord, 0, 0, 0, 0xFFFFFF, 0xFFFFFF, 1.0f, 300, 10, 9, entity.world.rand.nextFloat(), 0, result.sideHit, true);
							}
							// unregister
							TickHandler.unregister(true, handler, TickHandler.getHandler(entity, Identifier.ACTIVE_HAND),
									TickHandler.getHandler(entity, Identifier.PREVENT_ROTATION),
									TickHandler.getHandler(entity, Identifier.PREVENT_MOVEMENT),
									TickHandler.getHandler(entity, Identifier.HERO_SNEAKING));
							if (entity2 != null) {
								TickHandler.unregister(true, TickHandler.getHandler(entity2, Identifier.PREVENT_INPUT),
										TickHandler.getHandler(entity2, Identifier.PREVENT_ROTATION),
										TickHandler.getHandler(entity2, Identifier.PREVENT_MOVEMENT),
										TickHandler.getHandler(entity2, Identifier.FORCE_VIEW));
								if (entity2 == player)
									Minewatch.proxy.updateFOV();
							}
							if (entity == player)
								TickHandler.unregister(true, TickHandler.getHandler(entity, Identifier.ABILITY_USING),
										TickHandler.getHandler(entity, Identifier.FORCE_VIEW));
						}
					}
					// stop following sound
					else if (packet.type == 57 && entity != null && packet.string != null) {
						Minewatch.proxy.stopFollowingSound(entity, packet.string);
					}
					// Two Reinhardt Charges colliding
					else if (packet.type == 58 && entity != null && entity2 != null) {
						TickHandler.unregister(true, TickHandler.getHandler(entity, Identifier.HERO_SNEAKING));
						TickHandler.register(true, Handlers.PREVENT_INPUT.setEntity(entity).setTicks(20),
								Handlers.PREVENT_ROTATION.setEntity(entity).setTicks(20), 
								Handlers.PREVENT_MOVEMENT.setEntity(entity).setTicks(20));
						TickHandler.register(true, Handlers.PREVENT_INPUT.setEntity(entity2).setTicks(20),
								Handlers.PREVENT_ROTATION.setEntity(entity2).setTicks(20), 
								Handlers.PREVENT_MOVEMENT.setEntity(entity2).setTicks(20));
					}
					// Set player's rotations
					else if (packet.type == 59 && entity == player) {
						player.prevRotationYaw = player.rotationYaw;
						player.prevRotationPitch = player.rotationPitch;
						player.rotationYaw = (float) MathHelper.wrapDegrees(MathHelper.clamp(MathHelper.wrapDegrees(packet.x), MathHelper.wrapDegrees(player.rotationYaw-1), MathHelper.wrapDegrees(player.rotationYaw+1)));
						player.rotationPitch = (float) MathHelper.wrapDegrees(MathHelper.clamp(MathHelper.wrapDegrees(packet.y), MathHelper.wrapDegrees(player.rotationPitch-1), MathHelper.wrapDegrees(player.rotationPitch+1)));
					}
					// Ranks
					else if (packet.type == 60) {
						RankManager.clientRanks.clear();
						for (Rank rank : Rank.values())
							if ((((int)packet.x) >> rank.ordinal() & 1) == 1)
								RankManager.clientRanks.add(rank);
					}
				}
			});
			return null;
		}
	}
}