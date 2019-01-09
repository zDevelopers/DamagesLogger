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
package me.cassayre.florian.damageslogger.commands;

import fr.zcraft.zlib.components.commands.*;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.core.ZLib;
import fr.zcraft.zlib.tools.PluginLogger;
import me.cassayre.florian.damageslogger.DamagesLogger;
import me.cassayre.florian.damageslogger.report.Report;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.util.List;

@CommandInfo (name = "stop", usageParameters = "[--no-publish]")
@WithFlags ({"no-publish"})
public class StopCommand extends Command
{
    @Override
    protected void run() throws CommandException
    {
        final Report report = DamagesLogger.get().getReport();
        final CommandSender sender = this.sender;

        if (report == null)
        {
            error(I.t("There is no running recorder. Use {0} to start recording a new report.", Commands.getCommandInfo(StartCommand.class).build()));
            return;
        }

        info(I.t("Compiling and saving report..."));

        DamagesLogger.get().getManager().save(report, file ->
        {
            sender.sendMessage(I.t("{green}The report was successively stopped and saved."));
            sender.sendMessage(I.t("{gray}Saved to {0}", file.getAbsolutePath().replace(ZLib.getPlugin().getDataFolder().getAbsolutePath() + File.separator, "")));
            DamagesLogger.get().setReport(null);
        }, error ->
        {
            sender.sendMessage(I.t("{red}Unable to save the report."));
            sender.sendMessage(I.t("{gray}See console for details. The record was not stopped."));
            PluginLogger.error("Unable to save record to file.", error);
        });

        if (!hasFlag("no-publish"))
        {
            DamagesLogger.get().getManager().publish(report, url ->
            {
                sender.sendMessage("");
                sender.sendMessage(I.t("{darkgreen}{bold}The report was successfully published!"));
                sender.sendMessage(I.t("{green}View it online: {0}", url));
                sender.sendMessage("");
            }, error ->
            {
                sender.sendMessage("");
                sender.sendMessage(I.t("{red}There were an error while publishing your report :("));
                sender.sendMessage(I.t("{gray}The server said: {0}", error));
                sender.sendMessage("");
            });
        }
    }

    @Override
    protected List<String> complete()
    {
        if (args.length == 1) return getMatchingSubset(args[0], "--no-publish");
        else return null;
    }
}
