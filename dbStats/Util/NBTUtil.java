package dbStats.Util;

import java.util.ArrayList;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagEnd;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.nbt.NBTTagString;

public class NBTUtil {
	
	private static NBTBase GetTag(NBTBase currentTag, String tagName)
	{		
		try {
			@SuppressWarnings("unchecked")
			ArrayList<?> tags = new ArrayList<Object>(((NBTTagCompound) currentTag).getTags());
			for(Object tag : tags)
			{
				if (((NBTBase)tag).getName().equals(tagName))
				{
					return (NBTBase)tag;
				}
			}
		}
		catch (Exception e){
			ErrorUtil.LogException(e);
		}
		
		return currentTag;
	}
	
	private static String GetTagNamesAndValues(NBTBase tag, String prefix)
	{
		StringBuilder sb = new StringBuilder();
		
		try {
			switch(NBTBase.NBTTypes[tag.getId()])
			{
			case "BYTE":
				{
					NBTTagByte castTag = (NBTTagByte)tag;
					sb.append(prefix).append(castTag.getName()).append(" : ").append(castTag.data);
				}
				break;
			case "SHORT":
				{
					NBTTagShort castTag = (NBTTagShort)tag;
					sb.append(prefix).append(castTag.getName()).append(" : ").append(castTag.data);
				}
				break;
			case "INT":
				{
					NBTTagInt castTag = (NBTTagInt)tag;
					sb.append(prefix).append(castTag.getName()).append(" : ").append(castTag.data);
				}
				break;
			case "LONG":
				{
					NBTTagLong castTag = (NBTTagLong)tag;
					sb.append(prefix).append(castTag.getName()).append(" : ").append(castTag.data);
				}
				break;
			case "FLOAT":
				{
					NBTTagFloat castTag = (NBTTagFloat)tag;
					sb.append(prefix).append(castTag.getName()).append(" : ").append(castTag.data);
				}
				break;
			case "DOUBLE":
				{
					NBTTagDouble castTag = (NBTTagDouble)tag;
					sb.append(prefix).append(castTag.getName()).append(" : ").append(castTag.data);
				}
				break;
			case "STRING":
				{
					NBTTagString castTag = (NBTTagString)tag;
					sb.append(prefix).append(castTag.getName()).append(" : ").append(castTag.data);
				}
				break;
			case "BYTE[]":
				{
					NBTTagByteArray castTag = (NBTTagByteArray)tag;
					sb.append(prefix).append(castTag.getName()).append(" : ").append(castTag.byteArray.toString());
				}
				break;
			case "INT[]":
				{
					NBTTagIntArray castTag = (NBTTagIntArray)tag;
					sb.append(prefix).append(castTag.getName()).append(" : ").append(castTag.intArray.toString());
				}
				break;
			case "COMPOUND":
				{
					NBTTagCompound castTag = (NBTTagCompound)tag;
					if (!castTag.getName().isEmpty())
					{
						sb.append(prefix).append(castTag.getName()).append(" : ");
						sb.append("\n");
					}
					prefix += "-";
					String returnedValue = GetNBTTagNamesAndValues(castTag, prefix); 
//					if (!returnedValue.isEmpty() && !returnedValue.equals("\n"))
//					{
						sb.append(returnedValue);
//						sb.append("\n");
//					}
//					prefix = prefix.substring(0, prefix.length() - 1);
				}
				break;
			case "LIST":
				{
					NBTTagList castTag = (NBTTagList)tag;
					for(int i = 0; i < castTag.tagCount(); i++)
					{
						sb.append(prefix).append(castTag.getName());
						sb.append("[").append(i).append("]");
						String returnedValue = GetTagNamesAndValues(castTag.tagAt(i), prefix);
						if (!returnedValue.isEmpty() && !returnedValue.equals("\n"))
						{
							sb.append("\n");
							sb.append(returnedValue);
						}
					}
				}
				break;
			case "END":
				{
					NBTTagEnd castTag = (NBTTagEnd)tag;
					sb.append(prefix).append(castTag.getName()).append(" : END");
				}
				break;
			}
		} catch(Exception e)
		{
			ErrorUtil.LogException(e);
		}
		
		return sb.toString();
	}
	
	//{"END", "BYTE", "SHORT", "INT", "LONG", "FLOAT", "DOUBLE", "BYTE[]", "STRING", "LIST", "COMPOUND", "INT[]"};
	public static String GetNBTTagNamesAndValues(NBTTagCompound baseTag, String prefix)
	{
		if (baseTag == null)
			return "";
		
		StringBuilder sb = new StringBuilder();
		
		try {
			@SuppressWarnings("unchecked")
			ArrayList<?> tags = new ArrayList<Object>((baseTag).getTags());
			for(Object tag : tags)
			{
				String returnedValue = GetTagNamesAndValues((NBTBase)tag, prefix);
				if (!returnedValue.isEmpty() && !returnedValue.equals("\n"))
				{
					while(returnedValue.charAt(returnedValue.length() - 1) == '\n')
					{
						returnedValue = returnedValue.substring(0, returnedValue.length() - 1);
					}
					
					sb.append(returnedValue);
					sb.append("\n");
				}
			}
		}
		catch (Exception e){
			ErrorUtil.LogException(e);
		}
		
		return sb.toString();
	}
	
	private static NBTBase GetTagFromTagList(NBTBase currentTag, int tagPos)
	{
		try {
			NBTTagList castTag = (NBTTagList)currentTag;
			return castTag.tagAt(tagPos);
		} catch(Exception e)
		{
			ErrorUtil.LogException(e);
		}
		
		return currentTag;
	}
	
	private static String GetNBTValueFromTag(NBTBase currrentTag, int tagPos, String valueName)
	{
		try {
			switch(NBTBase.NBTTypes[currrentTag.getId()])
			{
			case "BYTE":
			{
				NBTTagByte castTag = (NBTTagByte)currrentTag;
				return String.valueOf(castTag.data);
			}
			case "SHORT":
			{
				NBTTagShort castTag = (NBTTagShort)currrentTag;
				return String.valueOf(castTag.data);
			}
			case "INT":
			{
				NBTTagInt castTag = (NBTTagInt)currrentTag;
				return String.valueOf(castTag.data);
			}
			case "LONG":
			{
				NBTTagLong castTag = (NBTTagLong)currrentTag;
				return String.valueOf(castTag.data);
			}
			case "FLOAT":
			{
				NBTTagFloat castTag = (NBTTagFloat)currrentTag;
				return String.valueOf(castTag.data);
			}
			case "DOUBLE":
			{
				NBTTagDouble castTag = (NBTTagDouble)currrentTag;
				return String.valueOf(castTag.data);
			}
			case "STRING":
			{
				NBTTagString castTag = (NBTTagString)currrentTag;
				return castTag.data;
			}
			case "BYTE[]":
			{
				NBTTagByteArray castTag = (NBTTagByteArray)currrentTag;
				return String.valueOf(castTag.byteArray[tagPos]);
			}
			case "INT[]":
			{
				NBTTagIntArray castTag = (NBTTagIntArray)currrentTag;
				return String.valueOf(castTag.intArray[tagPos]);
			}
			case "COMPOUND":
			{
				NBTTagCompound castTag = (NBTTagCompound)currrentTag;
				return GetNBTValueFromTag(castTag.getTag(valueName), 0, "");
			}
			}
		} catch(Exception e)
		{
			ErrorUtil.LogException(e);
		}
		
		return "";
	}
	
	public static String GetNBTValueAtTag(String tagIdentString, NBTBase baseTag)
	{
		if (baseTag == null)
			return "";
		
		NBTBase currentTag = baseTag;
		
		String[] tagSearchOrder = tagIdentString.split("/");
		String valueTag = tagSearchOrder[tagSearchOrder.length - 1];
		tagSearchOrder[tagSearchOrder.length - 1] = "";
		for(String tagName : tagSearchOrder)
		{
			if (tagName.isEmpty())
				continue;
			
			String searchTagName = tagName;
			int arrayId = -1;
			if (tagName.endsWith("]"))
			{
				searchTagName = tagName.substring(0, tagName.indexOf("["));
				arrayId = Integer.parseInt(tagName.substring(tagName.indexOf("[") + 1, tagName.indexOf("]")));
			}
			
			currentTag = GetTag(currentTag, searchTagName);
			
			if (!currentTag.getName().equals(searchTagName))
			{
				ErrorUtil.LogWarning("Error in nbt search string - " + tagIdentString + " @ " + searchTagName);
				return "";
			}
			
			if (arrayId >= 0)
			{
				currentTag = GetTagFromTagList(currentTag, arrayId);
			}
		}
		
		int arrayId = -1;
		if (valueTag.endsWith("]"))
		{
			arrayId = Integer.parseInt(valueTag.substring(valueTag.indexOf("[") + 1, valueTag.indexOf("]")));
		}
		
		return GetNBTValueFromTag(currentTag, arrayId, valueTag);
	}
	
	public static String GetNBTDataForDebug(int itemID, int itemMeta, NBTTagCompound nbt)
	{
		if (nbt == null)
			return ChatFormat.RED + "No NBT Data found";
		
		StringBuilder sb = new StringBuilder();
		sb.append(ChatFormat.BLUE + "").append(itemID).append(":").append(itemMeta).append("\n").append(ChatFormat.YELLOW);
		
		sb.append(GetNBTTagNamesAndValues(nbt, ""));
		
		sb.deleteCharAt(sb.length() - 1);
		
		return sb.toString();
	}
}
