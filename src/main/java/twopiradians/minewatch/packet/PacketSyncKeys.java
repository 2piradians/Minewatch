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

public class PacketSyncKeys implements IMessage
{
	private boolean isKeyPressed;
	private UUID player;
	private String keyName;

	public PacketSyncKeys() {}

	public PacketSyncKeys(String keyName, boolean isKeyPressed, UUID player) {
		this.keyName = keyName;
		this.isKeyPressed = isKeyPressed;
		this.player = player;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.keyName = ByteBufUtils.readUTF8String(buf);
		this.isKeyPressed = buf.readBoolean();
		this.player = UUID.fromString(ByteBufUtils.readUTF8String(buf));
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, keyName);
		buf.writeBoolean(this.isKeyPressed);
		ByteBufUtils.writeUTF8String(buf, player.toString());
	}

	public static class Handler implements IMessageHandler<PacketSyncKeys, IMessage> {
		@Override
		public IMessage onMessage(final PacketSyncKeys packet, final MessageContext ctx) {
			IThreadListener mainThread = (WorldServer) ctx.getServerHandler().playerEntity.world;
			mainThread.addScheduledTask(new Runnable() {

				@Override
				public void run() {
					EntityPlayer player = ctx.getServerHandler().playerEntity;

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
					else if (packet.keyName.equals("Alt Weapon")) {
						ItemStack main = player.getHeldItemMainhand();
						if (main != null && main.getItem() instanceof ItemMWWeapon) {
							EnumHero hero = ((ItemMWWeapon)main.getItem()).hero;
							hero.playersUsingAlt.put(packet.player, packet.isKeyPressed);
						}
					}
					else if (packet.keyName.equals("LMB"))
						Minewatch.keys.lmb.put(packet.player, packet.isKeyPressed);
					else if (packet.keyName.equals("RMB"))
						Minewatch.keys.rmb.put(packet.player, packet.isKeyPressed);
					else if (packet.keyName.equals("Toggle Ability 1")) {
						EnumHero hero = ItemMWArmor.SetManager.playersWearingSets.get(packet.player);
						if (hero != null)
							for (Ability ability : new Ability[] {hero.ability1, hero.ability2, hero.ability3})
								if (ability.isToggleable && ability.keybind == KeyBind.ABILITY_1 && 
								ability.keybind.getCooldown(player) == 0) 
									hero.weapon.toggle(player, ability, packet.isKeyPressed);
					}
					else if (packet.keyName.equals("Toggle Ability 2")) {
						EnumHero hero = ItemMWArmor.SetManager.playersWearingSets.get(packet.player);
						if (hero != null)
							for (Ability ability : new Ability[] {hero.ability1, hero.ability2, hero.ability3})
								if (ability.isToggleable && ability.keybind == KeyBind.ABILITY_2 && 
								ability.keybind.getCooldown(player) == 0) 
									hero.weapon.toggle(player, ability, packet.isKeyPressed);
					}
				}
			});
			return null;
		}
	}
}
