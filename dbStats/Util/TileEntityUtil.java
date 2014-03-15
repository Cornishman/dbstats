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

        try {
            for(Field field : tileEntity.getClass().getDeclaredFields())
            {
                if (!field.isAccessible())
                {
                    field.setAccessible(true);
                }

                if (field.get(tileEntity) instanceof TileEntity)
                {
                    TileEntity te = (TileEntity)field.get(tileEntity);
                    block.ID = te.getBlockType() != null ? te.getBlockType().blockID : 0;
                    block.ID = block.ID <= 0 ? te.worldObj.getBlockId(te.xCoord, te.yCoord, te.zCoord) : block.ID;
                    block.Meta = te.blockMetadata <= 0 ? te.worldObj.getBlockMetadata(te.xCoord, te.yCoord, te.zCoord) : te.blockMetadata;

                    return block;
                }
            }
        } catch(Exception ex){
            return new Block();
        }

        return block;
    }
}
