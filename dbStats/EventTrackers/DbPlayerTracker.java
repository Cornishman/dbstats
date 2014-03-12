package dbStats.EventTrackers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.IPlayerTracker;
import dbStats.DbStats;
import dbStats.API.Statistics.EStatistic;
import dbStats.Statistics.DistanceStatistic;
import dbStats.Statistics.PlayerStatistic;
import dbStats.Util.Utilities;

public class DbPlayerTracker implements IPlayerTracker {
	
	public ArrayList<PlaytimeUpdate> playerPlaytimeUpdaters;
	
	private class PlaytimeUpdate implements Runnable {
		private final String playerName;
		public Thread runner;
		private boolean keepAlive;
		private Date prevUpdateTime;
		
		private HashMap<String, Double> distances = new HashMap<String, Double>(); 
		
		PlaytimeUpdate(String player)
		{
			this.playerName = player;
			this.keepAlive = true;
			this.prevUpdateTime = new Date(System.currentTimeMillis());
			this.runner = new Thread(this);
			this.runner.start();
		}
		
		@Override
		public void run() {
			while (keepAlive)
			{
				try {
					Thread.sleep(DbStats.config.playerUpdateDelay());
				} catch (InterruptedException ex) { /*Do Nothing*/ }
				
				Date now = new Date(System.currentTimeMillis());
				long seconds = (now.getTime() - prevUpdateTime.getTime()) / 1000;
				prevUpdateTime = now;
				
				if (!Utilities.CanTrackPlayer(playerName))
					continue;
					
				MinecraftForge.EVENT_BUS.post(new EStatistic(new PlayerStatistic("players", "Playtime", playerName, (int)seconds, true)));
				
				synchronized (this) {
					Iterator<Entry<String, Double>> it = distances.entrySet().iterator();
					while(it.hasNext())
					{
						Map.Entry<String, Double> distance = (Map.Entry<String, Double>)it.next();
						upsertDistanceStatistic(distance.getKey(), distance.getValue(), playerName);
						it.remove();
					}
					distances.clear();
				}
			}
			
			playerPlaytimeUpdaters.remove(this);
		}
	}
	
	private static double upsertDistanceStatistic(String movementMethod, double distance, String player)
	{
		if (distance == 0)
			return distance;
		
		distance = Utilities.ConvertToMeters(distance);
		MinecraftForge.EVENT_BUS.post(new EStatistic(new DistanceStatistic("distances", "total", movementMethod, player, distance)));
		
		return 0;
	}
	
	public DbPlayerTracker() {
		playerPlaytimeUpdaters = new ArrayList<PlaytimeUpdate>();
	}
	
	private void removePlayerPlaytimeUpdate(String player)
	{
		//Find the matching player playTime updater and disable it
		for(PlaytimeUpdate update : playerPlaytimeUpdaters)
		{
			if (update.playerName.equals(player))
			{
				update.keepAlive = false;
				update.runner.interrupt();	//Interrupt the update to get accurate seconds
				try {
					update.runner.join();
				} catch (InterruptedException e) { }
			}
			
			break;
		}
	}
	
	//Called when the server is stopping!
	public void stopAllPlayerTrackerEvents()
	{
		for(WorldServer worldServer : DbStats.server.worldServers)
		{
			for(Object player : worldServer.playerEntities.toArray())
			{
				EntityPlayer p = (EntityPlayer)player;
				onPlayerLogout(p); //Make the game think the player logged out
			}
		}
	}
	
	public void addDistanceStat(String player, double distance, String movementMethod)
	{
		for(PlaytimeUpdate update : playerPlaytimeUpdaters)
		{
			if (update.playerName.equals(player))
			{
				synchronized (update) {
					update.distances.put("total", distance + (update.distances.get("total") != null ? update.distances.get("total") : 0));
					update.distances.put(movementMethod, distance + (update.distances.get(movementMethod) != null ? update.distances.get(movementMethod) : 0));
				}
				
				break;
			}
		}
	}

	@Override
	public void onPlayerLogin(EntityPlayer player) {
		
		if (!DbStats.database.checkForPlayer(player.username))
		{
			//Insert if new player
			DbStats.database.insertNewPlayer(player.username);	//Explicit insert call, to make sure they exist (Hopefully before the next queue executes)
		}
		else
		{
			//Increase number of logins if not
			MinecraftForge.EVENT_BUS.post(new EStatistic(new PlayerStatistic("players", "Logins", player.username, 1, true)));
		}
		
		//Update LastLogin
		MinecraftForge.EVENT_BUS.post(new EStatistic(new PlayerStatistic("players", "LastLogin", player.username, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis()), false)));
		
		//Add a player playtime tracker 
		playerPlaytimeUpdaters.add(new PlaytimeUpdate(player.username));
	}

	@Override
	public void onPlayerLogout(EntityPlayer player) {
		//Update Last seen
		MinecraftForge.EVENT_BUS.post(new EStatistic(new PlayerStatistic("players", "LastSeen", player.username, new SimpleDateFormat("yyy-MM-dd HH:mm:ss").format(System.currentTimeMillis()), false)));
		
		//Remove the player playtime tracker
		removePlayerPlaytimeUpdate(player.username);
	}

	@Override
	public void onPlayerChangedDimension(EntityPlayer player) {
		if (Utilities.CanTrackPlayer((EntityPlayer) player))
		{
			MinecraftForge.EVENT_BUS.post(new EStatistic(new PlayerStatistic("players", "DimensionTeleports", player.username, 1, true)));
		}
	}

	@Override
	public void onPlayerRespawn(EntityPlayer player) {
		//Nothing
	}
}
