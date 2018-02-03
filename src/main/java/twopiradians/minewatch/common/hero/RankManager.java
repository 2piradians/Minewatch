package twopiradians.minewatch.common.hero;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.packet.SPacketSimple;

@Mod.EventBusSubscriber
public class RankManager {

	public enum Rank {
		NONE(0), HELPER(1, "server-owners", "map-makers", "translators", "skin-makers"), PATREON(2, "patreons"), MOD(3, "mods"), DEV(3);

		public ArrayList<String> categories = new ArrayList<String>();
		public final ResourceLocation iconLoc;

		private Rank(int icon, String... categories) {
			if (categories != null)
				this.categories = Lists.newArrayList(categories);
			iconLoc = new ResourceLocation(Minewatch.MODID, "textures/gui/icon_background_"+icon+".png");
		}
	}

	private static URL url;
	public static HashMap<UUID, ArrayList<Rank>> serverRanks = Maps.newHashMap();
	public static ArrayList<Rank> clientRanks = new ArrayList<Rank>();

	static {
		try {
			url = new URL("https://raw.githubusercontent.com/Furgl/Global-Mod-Info/master/Minewatch/ranks.json");
		} catch (MalformedURLException e) {}
	}

	public static void lookUpRanks() {
		if (url != null && serverRanks.isEmpty()) {
			try {
				// add devs manually (because paranoid and never changing)
				serverRanks.put(UUID.fromString("f08951bc-e379-4f19-a113-7728b0367647"), Lists.newArrayList(Rank.DEV)); // Furgl
				serverRanks.put(UUID.fromString("93d28330-e1e2-447b-b552-00cb13e9afbd"), Lists.newArrayList(Rank.DEV)); // 2piradians

				InputStream con = url.openStream();
				String data = new String(ByteStreams.toByteArray(con), "UTF-8");
				con.close();

				Map<String, Object> json = new Gson().fromJson(data, Map.class);
				for (Rank rank : Rank.values())
					for (String category : rank.categories)
						if (json.containsKey(category) && json.get(category) instanceof ArrayList<?>)
							for (String uuidString : (ArrayList<String>) json.get(category)) {
								UUID uuid = UUID.fromString(uuidString);
								ArrayList<Rank> ranks = serverRanks.get(uuid);
								if (ranks == null)
									ranks = new ArrayList<Rank>();
								if (!ranks.contains(rank))
									ranks.add(rank);
								serverRanks.put(uuid, ranks);
							}
			}
			catch (Exception e) {
				Minewatch.logger.warn("Unable to look up ranks.");
			}
		}
	}

	@SubscribeEvent
	public static void onLoginSendRanks(PlayerLoggedInEvent event) {
		// send packet with player's rank(s) on login
		if (!event.player.world.isRemote && event.player instanceof EntityPlayerMP) {
			ArrayList<Rank> ranks = getRanks(event.player);
			double num = 0;
			for (Rank rank : Rank.values())
				if (ranks.contains(rank))
					num += 1 << rank.ordinal();
			Minewatch.network.sendTo(new SPacketSimple(60, false, event.player, num, 0, 0), (EntityPlayerMP) event.player);
		}
	}

	public static Rank getHighestRank(Entity player) {
		return player == null ? Rank.NONE : getHighestRank(player.getPersistentID(), player.world.isRemote);
	}

	public static Rank getHighestRank(UUID uuid, boolean isRemote) {
		if (uuid != null) {
			ArrayList<Rank> ranks = getRanks(uuid, isRemote);
			Rank highestRank = Rank.NONE;
			for (Rank rank : ranks)
				if (rank.ordinal() > highestRank.ordinal())
					highestRank = rank;
			return highestRank;
		}
		return Rank.NONE;
	}

	public static ArrayList<Rank> getRanks(Entity player) {
		return player == null ? new ArrayList<Rank>() : getRanks(player.getPersistentID(), player.world.isRemote);
	}

	public static ArrayList<Rank> getRanks(UUID uuid, boolean isRemote) {
		if (uuid != null)
			if (isRemote)
				return clientRanks;
			else if (!isRemote && serverRanks.containsKey(uuid))
				return serverRanks.get(uuid);
		return new ArrayList<Rank>();
	}

}