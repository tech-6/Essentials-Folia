package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.utils.DateUtil;
import org.bukkit.Chunk;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public class Commandgc extends EssentialsCommand {
    public Commandgc() {
        super("gc");
    }

    @Override
    protected void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
        // RitaSister start
        // removed message about tps because Folia is not have implemented API for return TPS value
        /*final double tps = 20d;
        final ChatColor color;
        if (tps >= 18.0) {
            color = ChatColor.GREEN;
        } else if (tps >= 15.0) {
            color = ChatColor.YELLOW;
        } else {
            color = ChatColor.RED;
        }*/

        sender.sendTl("uptime", DateUtil.formatDateDiff(ManagementFactory.getRuntimeMXBean().getStartTime()));
        //sender.sendTl("tps", "" + color + NumberUtil.formatDouble(tps));
        //RitaSister end
        sender.sendTl("gcmax", Runtime.getRuntime().maxMemory() / 1024 / 1024);
        sender.sendTl("gctotal", Runtime.getRuntime().totalMemory() / 1024 / 1024);
        sender.sendTl("gcfree", Runtime.getRuntime().freeMemory() / 1024 / 1024);

        ess.scheduleGlobalDelayedTask(() -> {
            final List<World> worlds = server.getWorlds();
            for (final World w : worlds) {
                String worldType = "World";
                switch (w.getEnvironment()) {
                    case NETHER:
                        worldType = "Nether";
                        break;
                    case THE_END:
                        worldType = "The End";
                        break;
                }

                final AtomicInteger tileEntities = new AtomicInteger();
                tileEntities.set(0);

                final AtomicInteger entitySize = new AtomicInteger();
                entitySize.set(0);

                try {
                    for (final Chunk chunk : w.getLoadedChunks()) {
                        ess.scheduleLocationDelayedTask(chunk, () -> tileEntities.getAndAdd(chunk.getTileEntities().length));
                    }
                } catch (final ClassCastException ex) {
                    ess.getLogger().log(Level.SEVERE, "Corrupted chunk data on world " + w, ex);
                }

                for(Entity entity : w.getEntities()) {
                    ess.scheduleEntityDelayedTask(entity, () -> entitySize.getAndAdd(entity.getWorld().getEntities().size()));
                }

                sender.sendTl("gcWorld", worldType, w.getName(), w.getLoadedChunks().length, entitySize, tileEntities);
            }
        });
    }
}
