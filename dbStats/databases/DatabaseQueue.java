package dbStats.databases;

import dbStats.API.Statistics.EStatistic;
import dbStats.API.Statistics.Statistic;
import dbStats.DbStats;
import dbStats.Util.ErrorUtil;
import net.minecraftforge.event.ForgeSubscribe;

import java.util.ArrayList;

public class DatabaseQueue {
	
	final ArrayList<Statistic> statsQueue;
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
				copiedStatsQueue = GroupQueueStatstics(copiedStatsQueue, true);
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
            boolean match = false;
            for(Statistic cStat : statsQueue)
            {
                //Compare the uidHash against the current queue, if a match is found override the statistic with the higher priority
                if (cStat.uidHash == stat.uidHash)
                {
                    match = true;
                    if (stat.priority > cStat.priority)
                    {
                        cStat = stat;
                    }
                    else { match = false; }
                    break;
                }
            }

            if (!match) { statsQueue.add(stat);}

			if (DbStats.config.forceQueueUpdate(statsQueue.size()))
			{
				GroupQueueStatstics(statsQueue, false);
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
	
	private ArrayList<Statistic> GroupQueueStatstics(ArrayList<Statistic> stats, Boolean returnGroup)
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
		}

        if (!returnGroup)
        {
            stats.addAll(groupedStats);
            return null;
        }
		
		return groupedStats;
	}
}
