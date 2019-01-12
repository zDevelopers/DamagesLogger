package me.cassayre.florian.damageslogger;

import fr.zcraft.zlib.components.commands.Commands;
import fr.zcraft.zlib.components.i18n.I18n;
import fr.zcraft.zlib.core.ZPlugin;
import me.cassayre.florian.damageslogger.commands.InfoCommand;
import me.cassayre.florian.damageslogger.commands.StartCommand;
import me.cassayre.florian.damageslogger.commands.StopCommand;
import me.cassayre.florian.damageslogger.report.Report;

public class DamagesLogger extends ZPlugin
{
    private static DamagesLogger instance = null;

    private ReportsManager manager = null;
    private Report report = null;

    @Override
    public void onEnable()
    {
        instance = this;
        manager = loadComponent(ReportsManager.class);

        loadComponents(Commands.class, I18n.class);

        Commands.register("damageslogger", StartCommand.class, StopCommand.class, InfoCommand.class);
    }

    public static DamagesLogger get()
    {
        return instance;
    }

    public Report getReport()
    {
        return report;
    }

    public void setReport(final Report report)
    {
        if (report != null) manager.registerReport(report);
        else manager.unregisterReport(this.report);

        this.report = report;
    }

    public ReportsManager getManager()
    {
        return manager;
    }
}
