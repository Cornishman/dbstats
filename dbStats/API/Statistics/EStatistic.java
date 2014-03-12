package dbStats.API.Statistics;

import net.minecraftforge.event.Event;

public class EStatistic extends Event {
	public final Statistic statistic;

	public EStatistic(Statistic stat) {
		this.statistic = stat;
	}
}
