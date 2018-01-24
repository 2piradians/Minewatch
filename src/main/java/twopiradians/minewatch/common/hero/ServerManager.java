package twopiradians.minewatch.common.hero;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

import twopiradians.minewatch.common.Minewatch;

public class ServerManager {

	private static URL url;
	public static ArrayList<Server> servers = new ArrayList<Server>();

	static {
		try {
			url = new URL("https://raw.githubusercontent.com/Furgl/Global-Mod-Info/master/Minewatch/servers.json");
		} catch (MalformedURLException e) {}
	}

	public static void lookUpServers() {
		if (url != null && servers.isEmpty()) {
			try {
				InputStream con = url.openStream();
				String data = new String(ByteStreams.toByteArray(con), "UTF-8");
				con.close();

				Map<String, Object> json = new Gson().fromJson(data, Map.class);
				for (String server : json.keySet()) 
					servers.add(new Server((LinkedTreeMap<String, String>) json.get(server)));
			}
			catch (Exception e) {
				Minewatch.logger.warn("Unable to look up servers.");
			}
		}
	}
	
	public static class Server {
		
		public String ip;
		public String status;
		public TreeMap<String, String> info = Maps.newTreeMap();

		public Server(LinkedTreeMap<String, String> map) {
			for (String category : map.keySet())
				if (category.equalsIgnoreCase("ip"))
					ip = map.get(category);
				else if (category.equalsIgnoreCase("status"))
					status = map.get(category);
				else
					info.put(category, map.get(category));
		}
		
	}

}