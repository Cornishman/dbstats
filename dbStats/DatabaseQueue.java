package dbStats;

import java.util.ArrayList;

import net.minecraftforge.event.ForgeSubscribe;
import dbStats.API.Statistics.EStatistic;
import dbStats.API.Statistics.Statistic;

public class DatabaseQueue {
	
	ArrayList<Statistic> statsQueue;
	QueueHandler queueHandler;
	
	private class QueueHandler implements Runnable {
		
		Thread runner;
		private boolean active;
		
		public QueueHandler() {
			active = true;
			runner = new Thread(this);
			runner.start();
		}

		@Override
		public void run() {
			while (active)
			{
				try {
					Thread.sleep(DbStats.config.queryDelay());
				} catch (InterruptedException ex) { }
				
				ArrayList<Statistic> copiedStatsQueue;
				
				synchronized (statsQueue) {
					copiedStatsQueue = new ArrayList<Statistic>(statsQueue);
					statsQueue.clear();
				}
				
				//Now do what we will with copiedStatsQueue
				copiedStatsQueue = GroupQueueStatstics(copiedStatsQueue);
				copiedStatsQueue = DbStats.database.GroupStatsIntoSingleQueriesAndExecute(copiedStatsQueue);
				
				for(Statistic stat : copiedStatsQueue)
				{
					if (!DbStats.database.upsertStatistic(stat))
						ErrorUtil.LogStatisticError(stat);
				}
			}
		}
	}

	public DatabaseQueue() {
		statsQueue = new ArrayList<Statistic>();
		queueHandler = new QueueHandler();
	}
	
	public void StopQueueHandler() {
		queueHandler.active = false;
		queueHandler.runner.interrupt();
		try {
			queueHandler.runner.join();
		} catch (InterruptedException e) { /*Do Nothing*/}
	}
	
	private void AddStatisticToQueue(Statistic stat)
	{
		synchronized(statsQueue) {
			statsQueue.add(stat);
			if (DbStats.config.forceQueueUpdate(statsQueue.size()))
			{
				statsQueue = GroupQueueStatstics(statsQueue);
				if (DbStats.config.forceQueueUpdate(statsQueue.size()))
				{
					queueHandler.runner.interrupt();
				}
			}
		}
	}
	
	@ForgeSubscribe
	public void onStatistic(EStatistic event){
		AddStatisticToQueue(event.statistic);
	}
	
	private ArrayList<Statistic> GroupQueueStatstics(ArrayList<Statistic> stats)
	{
		ArrayList<Statistic> groupedStats = new ArrayList<Statistic>();
		
		while (!stats.isEmpty()) {
			ArrayList<Statistic> statsToRemove = new ArrayList<Statistic>();
			Statistic currentStat = null;
			//Searches for all repeated stats and combines them all to one stat
			for(Statistic stat : stats)
			{
				if (currentStat == null)
				{
					currentStat = stat;
					statsToRemove.add(stat);
					continue;
				}
				
				if (stat.key.equals(currentStat.key))
				{
					if (currentStat.Combine(stat))
					{
						statsToRemove.add(stat);
					}
				}
			}
			
			stats.removeAll(statsToRemove);
			groupedStats.add(currentStat);
			currentStat = null;
		}
		
		return groupedStats;
	}
}
