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
import me.cassayre.florian.damageslogger.report.record.DamageRecord.DamageType;
import me.cassayre.florian.damageslogger.report.record.DamageRecord.Weapon;
import me.cassayre.florian.damageslogger.report.record.HealRecord.HealingType;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * The reports manager: registers, saves, manages export & backups of,
 * all reports.
 *
 * If you use this without the plugin, by shading the library, you must
 * initialize the manager first:
 *
 * <pre>
 *     ReportsManager.init(yourPluginInstance);
 *     final ReportsManager manager = ReportsManager.get();
 * </pre>
 */
public class ReportsManager extends ZLibComponent
{
    private static final Pattern NON_LATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

    private static ReportsManager instance = null;

    private final Set<Report> reports = new HashSet<>();

    private boolean backup = true;
    private long backupInterval = 20 * 60;
    private boolean backupErrorWarningSent = false;
    private BukkitTask backupTask = null;

    private File saveDirectory = new File(ZLib.getPlugin().getDataFolder(), "reports");

    /*
     * As Bukkit does not expose the real heal cause (e.g. “golden apple”) in the regen event,
     * we need to keep track of it if auto-track is enabled.
     * Same idea for damages sources to identify witches, withers…
     */
    private Map<UUID, HealingType> lastHealingType = new HashMap<>();
    private Map<UUID, DamageType> lastDamageType = new HashMap<>();
    private Map<UUID, Player> lastMagicDamager = new HashMap<>();
    private Map<UUID, Weapon> lastWeapon = new HashMap<>();

    /**
     * Initialize the reports manager for use as a shaded library.
     *
     * If you're depending on this by requiring the DamagesLogger plugin to
     * be installed on the server, you don't need this. Use
     * {@link DamagesLogger#getManager() this method} to get the manager.
     *
     * @param plugin Your plugin's instance.
     */
    public static void init(final JavaPlugin plugin)
    {
        // This constructor is used by external plugins using
        // this as a shaded library. As such, there is no plugin
        // so the zLib will not be initialized automatically.
        // We have to initialize manually the zLib.
        // We check for existing initialization just to be sure.
        if (!ZLib.isInitialized())
        {
            ZLib.init(plugin);
        }

        // If shaded
        if (DamagesLogger.get() == null || DamagesLogger.get().getManager() == null)
        {
            instance = ZLib.loadComponent(ReportsManager.class);
        }
    }

    /**
     * Retrieves the Reports Manager instance.
     */
    public static ReportsManager get()
    {
        return DamagesLogger.get() != null && DamagesLogger.get().getManager() != null ? DamagesLogger.get().getManager() : instance;
    }

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

    /**
     * Enables or disables reports backups.
     *
     * If enabled, every minute by default, all running reports will be
     * saved into a backup directory, this directory being {@code reports/backup}
     * under your plugin's data directory (or DamagesLogger's own data directory,
     * if used not shaded).
     *
     * @param backup {@code true} to enable backups.
     * @see #setBackupInterval(long) to change the backup interval.
     */
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

    /**
     * Changes the backups interval. By default, a backup is made every minute.
     *
     * @param backupInterval The backup interval, in ticks.
     */
    public void setBackupInterval(long backupInterval)
    {
        final boolean restartBackup = backup && backupInterval != this.backupInterval;

        this.backupInterval = backupInterval;
        if (restartBackup) setBackup(true);
    }

    /**
     * Registers a report into this manager. If you use auto-track, you _must_
     * register the report for damages, heals and event to be saved. Also,
     * only registered reports will have backups.
     *
     * @param report The report to register.
     * @return The registered report (so you can reuse it in chains).
     */
    public Report registerReport(final Report report)
    {
        reports.add(report);
        return report;
    }

    /**
     * Unregisters a report.
     *
     * This implies that auto-track will no longer be active, and backups will
     * stop for this report. You'll still be able to save, publish, or event backup
     * manually the report using the methods in this class.
     *
     * @param report The report to unregister.
     * @return The registered report (so you can reuse it in chains).
     */
    public Report unregisterReport(final Report report)
    {
        reports.remove(report);
        return report;
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
        save(report, new File(saveDirectory, dateSlug(report.getStartDate()) + "-" + slug(report.getTitle()) + ".json"), callbackSuccess, callbackError);
    }

    public void save(final Report report, File location, final Callback<File> callbackSuccess, final Callback<Throwable> callbackError)
    {
        report.getPlayers().forEach(ReportPlayer::collectStatistics);
        ReportsWorker.save(report, location, callbackSuccess, callbackError);
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

    /**
     * Saves the last damage type for the given player. Internal use but must be public.
     *
     * @param player The player.
     * @param damageType The damage type.
     */
    public void _setLastDamageType(final Player player, final DamageType damageType)
    {
        lastDamageType.put(player.getUniqueId(), damageType);
    }

    /**
     * Retrieves the last damage type for the given player. Internal use but must be public.
     *
     * @param player The player.
     * @return The latest recorded damage type with {@link #_setLastDamageType(Player, DamageType)},
     * or {@link DamageType#UNKNOWN} if nothing recorded.
     */
    public DamageType _getLastDamageType(final Player player)
    {
        return lastDamageType.getOrDefault(player.getUniqueId(), DamageType.UNKNOWN);
    }

    /**
     * Saves the last magic damage type for the given player. Internal use but must be public.
     *
     * @param player The player.
     * @param damager The damager.
     */
    public void _setLastMagicDamager(final Player player, final Player damager)
    {
        lastMagicDamager.put(player.getUniqueId(), damager);
    }

    /**
     * Retrieves the last magic damage type for the given player. Internal use but must be public.
     *
     * @param player The player.
     * @return The latest recorded damage type with {@link #_setLastMagicDamager(Player, Player)},
     * or {@link DamageType#UNKNOWN} if nothing recorded.
     */
    public Player _getLastMagicDamager(final Player player)
    {
        return lastMagicDamager.get(player.getUniqueId());
    }

    /**
     * Saves the last magic damage type for the given player. Internal use but must be public.
     *
     * @param player The player.
     * @param weapon The weapon.
     */
    public void _setLastWeapon(final Player player, final Weapon weapon)
    {
        lastWeapon.put(player.getUniqueId(), weapon);
    }

    /**
     * Retrieves the last magic damage type for the given player. Internal use but must be public.
     *
     * @param player The player.
     * @return The latest recorded damage type with {@link #_setLastMagicDamager(Player, Player)},
     * or {@link DamageType#UNKNOWN} if nothing recorded.
     */
    public Weapon _getLastWeapon(final Player player)
    {
        return lastWeapon.getOrDefault(player.getUniqueId(), Weapon.UNKNOWN);
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
