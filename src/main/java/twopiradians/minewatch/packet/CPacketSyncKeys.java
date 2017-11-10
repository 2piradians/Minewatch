package twopiradians.minewatch.packet;

import java.util.UUID;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.hero.Ability;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.item.armor.ItemMWArmor;
import twopiradians.minewatch.common.item.weapon.ItemMWWeapon;

public class CPacketSyncKeys implements IMessage
{
	private boolean isKeyPressed;
	private float fov;
	private UUID player;
	private String keyName;

	public CPacketSyncKeys() {}

	public CPacketSyncKeys(String keyName, boolean isKeyPressed, UUID player) {
		this(keyName, isKeyPressed, 70f, player);
	}

	public CPacketSyncKeys(String keyName, float fov, UUID player) {
		this(keyName, false, fov, player);
	}

	public CPacketSyncKeys(String keyName, boolean isKeyPressed, float fov, UUID player) {
		this.keyName = keyName;
		this.isKeyPressed = isKeyPressed;
		this.fov = fov;
		this.player = player;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.keyName = ByteBufUtils.readUTF8String(buf);
		this.isKeyPressed = buf.readBoolean();
		this.fov = buf.readFloat();
		this.player = UUID.fromString(ByteBufUtils.readUTF8String(buf));
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, keyName);
		buf.writeBoolean(this.isKeyPressed);
		buf.writeFloat(fov);
		ByteBufUtils.writeUTF8String(buf, player.toString());
	}

	public static class Handler implements IMessageHandler<CPacketSyncKeys, IMessage> {
		@Override
		public IMessage onMessage(final CPacketSyncKeys packet, final MessageContext ctx) {
			IThreadListener mainThread = (WorldServer) ctx.getServerHandler().player.world;
			mainThread.addScheduledTask(new Runnable() {

				@Override
				public void run() {
					EntityPlayer player = ctx.getServerHandler().player;

					if (packet.keyName.equals("Hero Information"))
						Minewatch.keys.heroInformation.put(packet.player, packet.isKeyPressed);
					else if (packet.keyName.equals("Reload"))
						Minewatch.keys.reload.put(packet.player, packet.isKeyPressed);
					else if (packet.keyName.equals("Ability 1"))
						Minewatch.keys.ability1.put(packet.player, packet.isKeyPressed);
					else if (packet.keyName.equals("Ability 2"))
						Minewatch.keys.ability2.put(packet.player, packet.isKeyPressed);
					else if (packet.keyName.equals("Ultimate"))
						Minewatch.keys.ultimate.put(packet.player, packet.isKeyPressed);
					else if (packet.keyName.equals("Jump"))
						Minewatch.keys.jump.put(packet.player, packet.isKeyPressed);
					else if (packet.keyName.equals("Fov"))
						Minewatch.keys.fov.put(packet.player, packet.fov);
					else if (packet.keyName.equals("Alt Weapon")) {
						ItemStack main = player.getHeldItemMainhand();
						if (main != null && main.getItem() instanceof ItemMWWeapon) {
							EnumHero hero = ((ItemMWWeapon)main.getItem()).hero;
							if (!packet.isKeyPressed)
								hero.playersUsingAlt.remove(packet.player);
							else if (!hero.playersUsingAlt.contains(packet.player))
								hero.playersUsingAlt.add(packet.player);
							Minewatch.network.sendToAll(new SPacketSimple(6, packet.isKeyPressed, player));
						}
					}
					else if (packet.keyName.equals("LMB"))
						Minewatch.keys.lmb.put(packet.player, packet.isKeyPressed);
					else if (packet.keyName.equals("RMB"))
						Minewatch.keys.rmb.put(packet.player, packet.isKeyPressed);
					else if (packet.keyName.equals("Toggle Ability 1")) {
						EnumHero hero = ItemMWArmor.SetManager.entitiesWearingSets.get(packet.player);
						if (hero != null)
							for (Ability ability : new Ability[] {hero.ability1, hero.ability2, hero.ability3})
								if (ability.isToggleable && ability.keybind == KeyBind.ABILITY_1 && 
								ability.keybind.getCooldown(player) == 0) 
									ability.toggle(player, packet.isKeyPressed);
					}
					else if (packet.keyName.equals("Toggle Ability 2")) {
						EnumHero hero = ItemMWArmor.SetManager.entitiesWearingSets.get(packet.player);
						if (hero != null)
							for (Ability ability : new Ability[] {hero.ability1, hero.ability2, hero.ability3})
								if (ability.isToggleable && ability.keybind == KeyBind.ABILITY_2 && 
								ability.keybind.getCooldown(player) == 0) 
									ability.toggle(player, packet.isKeyPressed);
					}
				}
			});
			return null;
		}
	}
}
