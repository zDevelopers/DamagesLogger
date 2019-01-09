/*
 * Plugin UHCReloaded : Alliances
 *
 * Copyright ou © ou Copr. Amaury Carrade (2016)
 * Idées et réflexions : Alexandre Prokopowicz, Amaury Carrade, "Vayan".
 *
 * Ce logiciel est régi par la licence CeCILL soumise au droit français et
 * respectant les principes de diffusion des logiciels libres. Vous pouvez
 * utiliser, modifier et/ou redistribuer ce programme sous les conditions
 * de la licence CeCILL telle que diffusée par le CEA, le CNRS et l'INRIA
 * sur le site "http://www.cecill.info".
 *
 * En contrepartie de l'accessibilité au code source et des droits de copie,
 * de modification et de redistribution accordés par cette licence, il n'est
 * offert aux utilisateurs qu'une garantie limitée.  Pour les mêmes raisons,
 * seule une responsabilité restreinte pèse sur l'auteur du programme,  le
 * titulaire des droits patrimoniaux et les concédants successifs.
 *
 * A cet égard  l'attention de l'utilisateur est attirée sur les risques
 * associés au chargement,  à l'utilisation,  à la modification et/ou au
 * développement et à la reproduction du logiciel par l'utilisateur étant
 * donné sa spécificité de logiciel libre, qui peut le rendre complexe à
 * manipuler et qui le réserve donc à des développeurs et des professionnels
 * avertis possédant  des  connaissances  informatiques approfondies.  Les
 * utilisateurs sont donc invités à charger  et  tester  l'adéquation  du
 * logiciel à leurs besoins dans des conditions permettant d'assurer la
 * sécurité de leurs systèmes et ou de leurs données et, plus généralement,
 * à l'utiliser et l'exploiter dans les mêmes conditions de sécurité.
 *
 * Le fait que vous puissiez accéder à cet en-tête signifie que vous avez
 * pris connaissance de la licence CeCILL, et que vous en avez accepté les
 * termes.
 */
package me.cassayre.florian.damageslogger;

import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.core.ZLib;
import fr.zcraft.zlib.core.ZLibComponent;
import fr.zcraft.zlib.tools.Callback;
import fr.zcraft.zlib.tools.PluginLogger;
import fr.zcraft.zlib.tools.runners.RunTask;
import me.cassayre.florian.damageslogger.listeners.PlayerConnectionListener;
import me.cassayre.florian.damageslogger.listeners.PlayerDamagesListener;
import me.cassayre.florian.damageslogger.listeners.PlayerHealsListener;
import me.cassayre.florian.damageslogger.report.Report;
import me.cassayre.florian.damageslogger.report.ReportPlayer;
import me.cassayre.florian.damageslogger.report.record.HealRecord.HealingType;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * The reports manager: registers, saves, manages export & backups of, all reports.
 *
 * If you use this without the plugin, by shading the library, you must initialize the manager through zLib:
 *
 * <pre>
 *     final ReportsManager manager = ZLib.loadComponent(ReportsManager.class);
 * </pre>
 *
 * If you're not using zLib, your plugin must be slightly modified to use it. It is as simple as adding a Maven
 * dependency and updating the class your plugin's main class depends on ({@link fr.zcraft.zlib.core.ZPlugin}
 * instead of {@link org.bukkit.plugin.java.JavaPlugin}), and you'll also gain a lot of really useful and completely
 * optional goodies to work on Minecraft plugins without any change for your users (no other plugin to install)!
 * For more information, <a href="https://github.com/zDevelopers/zLib/wiki/Installation">check out the documentation</a>.
 */
public class ReportsManager extends ZLibComponent
{
    private static final Pattern NON_LATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

    private final Set<Report> reports = new HashSet<>();

    private boolean backup = true;
    private long backupInterval = 20 * 60;
    private boolean backupErrorWarningSent = false;
    private BukkitTask backupTask = null;

    private File saveDirectory = new File(ZLib.getPlugin().getDataFolder(), "reports");

    /**
     * As Bukkit does not expose the real heal cause (e.g. “golden apple”) in the regen event,
     * we need to keep track of it if auto-track is enabled.
     */
    private Map<UUID, HealingType> lastHealingType = new HashMap<>();


    @Override
    protected void onEnable()
    {
        ZLib.registerEvents(new PlayerConnectionListener(this));
        ZLib.registerEvents(new PlayerDamagesListener(this));
        ZLib.registerEvents(new PlayerHealsListener(this));

        // Boots the I/O worker
        ZLib.loadComponent(ReportsWorker.class);

        // Launches the backup task
        setBackup(true);
    }

    public void setBackup(boolean backup)
    {
        if (backupTask != null)
        {
            backupTask.cancel();
            backupTask = null;
        }

        if (backup)
        {
            backupTask = RunTask.timer(() -> reports.forEach(report -> backup(report, success -> backupErrorWarningSent = false, exception ->
                {
                    PluginLogger.error("Unable to backup report, is the reports backup directory writable?", exception);

                    if (!backupErrorWarningSent)
                    {
                        Bukkit.getOnlinePlayers().stream()
                                .filter(Permissible::isOp)
                                .forEach(player -> player.sendMessage(I.t("{darkred}Warning! {red}The backup file couldn't be saved, please see the console. {gray}This message won't be sent for further failure(s).")));

                        backupErrorWarningSent = true;
                    }
                })), backupInterval, backupInterval);
        }

        this.backup = backup;
    }

    public void setBackupInterval(long backupInterval)
    {
        final boolean restartBackup = backup && backupInterval != this.backupInterval;

        this.backupInterval = backupInterval;
        if (restartBackup) setBackup(true);
    }

    public void registerReport(final Report report)
    {
        reports.add(report);
    }

    public void unregisterReport(final Report report)
    {
        reports.remove(report);
    }

    public Stream<Report> getTrackedReportsFor(final OfflinePlayer player)
    {
        return getTrackedReportsFor(player, false);
    }

    public Stream<Report> getTrackedReportsFor(final OfflinePlayer player, boolean includeReportsWerePlayerIsNotTracked)
    {
        return reports.stream()
                .filter(Report::isAutoTracked)
                .filter(report -> includeReportsWerePlayerIsNotTracked || report.isTracked(player));
    }

    public void backup(final Report report, final Callback<File> callbackSuccess, final Callback<Throwable> callbackError)
    {
        report.getPlayers().forEach(ReportPlayer::collectStatistics);

        ReportsWorker.save(
                report,
                new File(saveDirectory, "backup/" + dateSlug(report.getStartDate()) + "-" + slug(report.getTitle()) + "-backup-" + dateSlug(System.currentTimeMillis()) + ".json"),
                callbackSuccess, callbackError
        );
    }

    public void save(final Report report, final Callback<File> callbackSuccess, final Callback<Throwable> callbackError)
    {
        report.getPlayers().forEach(ReportPlayer::collectStatistics);

        ReportsWorker.save(
                report,
                new File(saveDirectory, dateSlug(report.getStartDate()) + "-" + slug(report.getTitle()) + ".json"),
                callbackSuccess, callbackError
        );
    }

    public void publish(final Report report, final Callback<File> callbackSuccess, final Callback<String> callbackError)
    {
        report.getPlayers().forEach(ReportPlayer::collectStatistics);

        // TODO
    }


    /**
     * Saves the last healing type for the given player. Internal use but must be public.
     *
     * @param player The player.
     * @param healingType The healing type.
     */
    public void _setLastHealingType(final Player player, final HealingType healingType)
    {
        lastHealingType.put(player.getUniqueId(), healingType);
    }

    /**
     * Retrieves the last healing type for the given player. Internal use but must be public.
     *
     * @param player The player.
     * @return The latest recorded healing type with {@link #_setLastHealingType(Player, HealingType)},
     * or {@link HealingType#UNKNOWN} if nothing recorded.
     */
    public HealingType _getLastHealingType(final Player player)
    {
        return lastHealingType.getOrDefault(player.getUniqueId(), HealingType.UNKNOWN);
    }


    private static String slug(String input)
    {
        final String nowhitespace = WHITESPACE.matcher(input).replaceAll("-");
        final String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        final String slug = NON_LATIN.matcher(normalized).replaceAll("");

        return slug.toLowerCase(Locale.ENGLISH);
    }

    private static String dateSlug(long date)
    {
        return DATE_FORMAT.format(new Date(date));
    }
}
