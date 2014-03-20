package dbStats.Statistics;

import dbStats.API.Statistics.Statistic;

public class BlockItemStatistic extends Statistic {
	
	public final int itemId;
	public final int itemMeta;
	public int amount;
	public final String nbt;
	public final String action;

	public BlockItemStatistic(int hashCode, int priority, String table, String column, String player, int itemId, int itemMeta, int amount, String itemNbt, String action) {
		super(hashCode, priority, "dbstat_item", table, column, player, true, true, true, true, true);
		
		this.itemId = itemId;
		this.itemMeta = itemMeta;
		this.amount = amount;
		this.nbt = itemNbt;
		this.action = action.toLowerCase();
	}
	
	@Override
	public boolean Combine(Statistic stat)
	{
		if (stat instanceof BlockItemStatistic && this.StatisticsMatch(stat))
		{
			if (this.itemId == ((BlockItemStatistic)stat).itemId 
					&& this.itemMeta == ((BlockItemStatistic)stat).itemMeta
					&& this.nbt.equals(((BlockItemStatistic)stat).nbt)
					&& this.action.equals(((BlockItemStatistic)stat).action))
			{
				this.amount += ((BlockItemStatistic)stat).amount;
				return true;
			}
		}
		return false;
	}

	@Override
	public String GetValue() {
		return Integer.toString(this.amount);
	}

	@Override
	public String GetSQLValueForInsertion() {
		return "@pId,'" + this.itemId + "','" + this.itemMeta + "','" + this.nbt + "','" + this.action + "','" + this.amount + "'";
	}
}
