package me.cassayre.florian.hawk.listeners;

import me.cassayre.florian.hawk.ReportsManager;
import me.cassayre.florian.hawk.report.Report;
import me.cassayre.florian.hawk.report.ReportPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerConnectionListener implements Listener {
    private final ReportsManager manager;

    public PlayerConnectionListener(ReportsManager manager) {
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent e) {
        manager.getTrackedReportsFor(e.getPlayer(), true)
                .filter(Report::isAutoTrackingNewPlayers)
                .forEach(report -> report.registerPlayers(e.getPlayer()));

        manager.getTrackedReportsFor(e.getPlayer())
                .filter(Report::isAutoCollectingPreviousStatistics)
                .flatMap(report -> report.getPlayers().stream())
                .filter(player -> !player.hasPreviousStatistics())
                .forEach(ReportPlayer::collectPreviousStatistics);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent e) {
        manager.getTrackedReportsFor(e.getPlayer())
                .filter(Report::isStoppingTrackOnDisconnection)
                .forEach(report -> report.untrack(e.getPlayer()));
    }
}
