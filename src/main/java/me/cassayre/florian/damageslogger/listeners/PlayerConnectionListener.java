package me.cassayre.florian.damageslogger.listeners;

import me.cassayre.florian.damageslogger.ReportsManager;
import me.cassayre.florian.damageslogger.report.Report;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerConnectionListener implements Listener
{
    private final ReportsManager manager;

    public PlayerConnectionListener(ReportsManager manager)
    {
        this.manager = manager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e)
    {
        manager.getTrackedReportsFor(e.getPlayer())
                .filter(Report::isAutoTrackingNewPlayers)
                .forEach(report -> report.registerPlayers(e.getPlayer()));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e)
    {
        manager.getTrackedReportsFor(e.getPlayer())
                .filter(Report::isStopingTrackOnDisconnection)
                .forEach(report -> report.untrackPlayer(e.getPlayer()));
    }
}
