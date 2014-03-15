package dbStats.Util;

import net.minecraft.tileentity.TileEntity;

import java.lang.reflect.Field;

public class TileEntityUtil {

    public static class Block{
        public int ID;
        public int Meta;

        Block()
        {
            ID = 0;
            Meta = 0;
        }
    };

    public static Block GetModBlock(Object tileEntity)
    {
        Block block = new Block();

        if (tileEntity instanceof TileEntity)
        {
            TileEntity te = (TileEntity) tileEntity;
            block.ID = te.blockType != null ? te.blockType.blockID : 0;
            block.ID = block.ID <= 0 ? te.worldObj.getBlockId(te.xCoord, te.yCoord, te.zCoord) : block.ID;
            block.Meta = te.blockMetadata <= 0 ? te.worldObj.getBlockMetadata(te.xCoord, te.yCoord, te.zCoord) : te.blockMetadata;
            return block;
        }

        //Specific mod cases
        try {
            //Forestry Mod
            if (tileEntity.getClass().toString().equals("class forestry.core.utils.TileInventoryAdapter"))
            {
                return readModBlock(tileEntity, "tile");
            }

            //Applied Energistics
            if (tileEntity.getClass().toString().equals("class appeng.common.AppEngInternalInventory"))
            {
                return readModBlock(tileEntity, "te");
            }
        }
        catch (Exception ex)
        {
            //Do nothing
        }

        return block;
    }

    private static Block readModBlock(Object obj, String fieldName)
            throws NoSuchFieldException, IllegalAccessException
    {
        Field modTileEntity = obj.getClass().getDeclaredField(fieldName);
        modTileEntity.setAccessible(true);

        Block block = new Block();

        if (modTileEntity != null)
        {
            TileEntity te = (TileEntity)modTileEntity.get(obj);
            block.ID = te.getBlockType() != null ? te.getBlockType().blockID : 0;
            block.ID = block.ID <= 0 ? te.worldObj.getBlockId(te.xCoord, te.yCoord, te.zCoord) : block.ID;
            block.Meta = te.blockMetadata <= 0 ? te.worldObj.getBlockMetadata(te.xCoord, te.yCoord, te.zCoord) : te.blockMetadata;
        }

        return block;
    }
}
