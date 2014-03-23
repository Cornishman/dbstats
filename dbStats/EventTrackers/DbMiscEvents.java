package dbStats.EventTrackers;

import dbStats.API.Statistics.EStatistic;
import dbStats.Statistics.MiscStat;
import dbStats.Util.ErrorUtil;
import dbStats.Util.Utilities;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.EventPriority;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

public class DbMiscEvents {

    @ForgeSubscribe(priority = EventPriority.LOWEST)
    public void playerArrowLoose(ArrowLooseEvent event)
    {
        if (!event.isCanceled() && Utilities.CanTrackPlayer(event.entityPlayer))
        {
            MinecraftForge.EVENT_BUS.post(new EStatistic(new MiscStat("misc", "", event.entityPlayer.username, "arrowsLoosed", "", 1)));
        }
    }

    @ForgeSubscribe(priority = EventPriority.LOWEST)
    public void playerFillBucket(FillBucketEvent event)
    {
        if (!event.isCanceled() && Utilities.CanTrackPlayer(event.entityPlayer))
        {
            ErrorUtil.LogMessage(event.entity.toString());
            ErrorUtil.LogMessage(event.entityLiving.toString());
            if (event.target != null)
            {
                int blockId = event.world.getBlockId(event.target.blockX, event.target.blockY, event.target.blockZ);
                Fluid f = FluidRegistry.lookupFluidForBlock(Block.blocksList[blockId]);
                if (f != null)
                {
                    if (f.getBlockID() != -1)
                    {
                        MinecraftForge.EVENT_BUS.post(new EStatistic(new MiscStat("misc", "", ((EntityPlayerMP) event.entity).username, "fillbucket", Integer.toString(f.getBlockID()), 1)));
                    }
                    else
                    {
                        MinecraftForge.EVENT_BUS.post(new EStatistic(new MiscStat("misc", "", ((EntityPlayerMP) event.entity).username, "fillbucket", f.getUnlocalizedName(), 1)));
                    }
                }

                ErrorUtil.LogMessage(event.target.toString());

                if (event.target.entityHit != null)
                {
                    ErrorUtil.LogMessage(event.target.entityHit.toString());
                }
            }
        }
    }

    @ForgeSubscribe(priority = EventPriority.LOWEST)
    public void playerStruckByLightning(EntityStruckByLightningEvent event)
    {
        if (event.entity instanceof EntityPlayerMP && Utilities.CanTrackPlayer((EntityPlayerMP) event.entity))
        {
            MinecraftForge.EVENT_BUS.post(new EStatistic(new MiscStat("misc", "", ((EntityPlayerMP) event.entity).username, "struckbylightning", "", 1)));
        }
    }
}
