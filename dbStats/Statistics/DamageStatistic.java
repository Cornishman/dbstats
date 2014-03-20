package dbStats.Statistics;

import dbStats.API.Statistics.Statistic;

public class DamageStatistic extends Statistic {
	
	public final String how;
	public final String what;
	public final int itemId;
	public final int itemMeta;
	public final String enchantments;
	public final String nbt;
	public final String action;
	public float amount;
	
	public DamageStatistic(int hashCode, int priority, String table, String column, String player, String how, String what, int itemId, int itemMeta, String enchantments, float amount, String nbt, String action)
	{
		super(hashCode, priority, "dbstat_deathkill", table, column, player, true, true, true, true, true);
		
		this.how = how;
		this.what = what;
		this.itemId = itemId;
		this.itemMeta = itemMeta;
		this.enchantments = enchantments;
		this.amount = amount;
		this.nbt = nbt;
		this.action = action;
	}

	@Override
	public boolean Combine(Statistic stat) {
		if (stat instanceof DamageStatistic && this.StatisticsMatch(stat))
		{
			DamageStatistic dks = (DamageStatistic)stat;
			if (this.how.equals(dks.how) 
					&& this.what.equals(dks.what) 
					&& this.itemId == dks.itemId 
					&& this.enchantments.equals(dks.enchantments)
					&& this.nbt.equals(dks.enchantments)
					&& this.action.equals(dks.action))
			{
				this.amount += dks.amount;
				return true;
			}
		}
		return false;
	}

	@Override
	public String GetValue() {
		return Float.toString(this.amount);
	}

	@Override
	public String GetSQLValueForInsertion() {
		return "@pId,'" + this.how + "','" + this.what + "','" + this.itemId + "','" + this.itemMeta + "','" + this.nbt + "','" + this.enchantments + "','" + this.action + "','" + this.amount + "'";
	}

}
