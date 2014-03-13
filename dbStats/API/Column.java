package dbStats.API;

public class Column {
	
	public enum ColumnType {
		INT, VARCHAR, TIMESTAMP, FLOAT, BOOLEAN
    }
	
	public final boolean allowNull;
	public final ColumnType type;
	public final String name;
	public final String defaultValue;
	public final boolean primaryKey;
	public final boolean uniqueKey;
	public final boolean autoIncrement;
	public final int dataSize;

	public Column(String name, ColumnType type, int length, boolean allowNull, String defaultValue, boolean primary, boolean unique, boolean autoIncrement)
	{
		this.name = name.toLowerCase();
		this.type = type;
		this.dataSize = length;
		this.allowNull = allowNull;
		this.defaultValue = defaultValue;
		this.primaryKey = primary;
		this.uniqueKey = unique;
		this.autoIncrement = autoIncrement;
	}
}
