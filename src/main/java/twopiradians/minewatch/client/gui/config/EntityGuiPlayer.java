package twopiradians.minewatch.client.gui.config;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class EntityGuiPlayer extends AbstractClientPlayer
{
	private AbstractClientPlayer player;
	private static ScorePlayerTeam team;

	public EntityGuiPlayer(World worldIn, GameProfile playerProfile, AbstractClientPlayer player) 
	{
		super(worldIn, playerProfile);
		this.player = player;
		
		if (team == null) {
			team = Minecraft.getMinecraft().theWorld.getScoreboard().createTeam("");
			team.setNameTagVisibility(Team.EnumVisible.NEVER);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Team getTeam()
	{
		return team;
	}

	@Override
	public ResourceLocation getLocationSkin()
	{
		if (player != null) {
			NetworkPlayerInfo networkplayerinfo = this.getPlayerInfo();
			return networkplayerinfo == null ? DefaultPlayerSkin.getDefaultSkin(this.getUniqueID()) : networkplayerinfo.getLocationSkin();
		}
		else 
			return super.getLocationSkin();
	}
}
