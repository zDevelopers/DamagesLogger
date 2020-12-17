package me.cassayre.florian.hawk;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.zcraft.quartzlib.components.commands.Commands;
import fr.zcraft.quartzlib.components.i18n.I18n;
import fr.zcraft.quartzlib.core.QuartzPlugin;
import me.cassayre.florian.hawk.commands.InfoCommand;
import me.cassayre.florian.hawk.commands.StartCommand;
import me.cassayre.florian.hawk.commands.StopCommand;
import me.cassayre.florian.hawk.report.Report;

public class Hawk extends QuartzPlugin {
    public static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private static Hawk instance = null;

    private ReportsManager manager = null;
    private Report report = null;

    /**
     * @return Hawk's plugin instance.
     */
    public static Hawk get() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        manager = loadComponent(ReportsManager.class);

        loadComponents(Commands.class, I18n.class);

        Commands.register("hawk", StartCommand.class, StopCommand.class, InfoCommand.class);
    }

    /**
     * Returns the current report when the plugin is used standalone through
     * its commands by users directly.
     * <p>
     * If you're integrating Hawk reports with your plugin, do not use this.
     * Create a report instance on your own ad use its methods.
     *
     * @return The current report.
     */
    public Report getReport() {
        return report;
    }

    /**
     * Sets the current report when the plugin is used standalone through its
     * commands by users directly.
     * <p>
     * If you're integrating Hawk reports with your plugin, do not use this.
     * Create a report instance on your own ad use its methods.
     *
     * @param report The report.
     */
    public void setReport(final Report report) {
        if (report != null) {
            manager.registerReport(report);
        } else {
            manager.unregisterReport(this.report);
        }

        this.report = report;
    }

    /**
     * The Hawk's reports manager allows you to register reports for damages'
     * auto-track, save and/or backup them, and of course publish them onto
     * the website.
     * <p>
     * You can also do all of this through Reports instances directly, so you
     * may never have to use this at all.
     *
     * @return Hawk's reports manager.
     * @see ReportsManager#get() Direct static access to the manager.
     */
    public ReportsManager getManager() {
        return manager;
    }
}
