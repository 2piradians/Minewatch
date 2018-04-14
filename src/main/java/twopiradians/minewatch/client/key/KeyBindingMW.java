package twopiradians.minewatch.client.key;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.hero.SetManager;
import twopiradians.minewatch.common.item.weapon.ItemMWWeapon;

@SideOnly(Side.CLIENT)
public class KeyBindingMW extends KeyBinding {

	public boolean requireSet;
	public boolean requireWeapon;

	public KeyBindingMW(String description, int keyCode, String category, boolean requireSet, boolean requireWeapon) {
		super(description, keyCode, category);
		this.requireSet = requireSet;
		this.requireWeapon = requireWeapon;
	}

	@Override
	public boolean isActiveAndMatches(int keyCode) {
		EntityPlayerSP player = Minecraft.getMinecraft().player;
		return super.isActiveAndMatches(keyCode) && player != null && 
				(!requireSet || SetManager.getWornSet(player) != null) && 
				(!requireWeapon || (player.getHeldItemMainhand() != null &&
				player.getHeldItemMainhand().getItem() instanceof ItemMWWeapon));
	}

}
