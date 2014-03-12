package dbStats.API;

import net.minecraftforge.event.Event;


public class Table extends Event {
	public final String tableName;
	public final Column[] columns;
	private final String columnToUpdate;
	
	public Table(String name, Column[] columns, String columnToUpdate)
	{
		this.tableName = name.toLowerCase();
		this.columns = columns;
		this.columnToUpdate = columnToUpdate.toLowerCase();
	}
	
	public String GetUpdateString()
	{
		return this.columnToUpdate;
	}
}