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

package me.cassayre.florian.hawk.commands;

import fr.zcraft.quartzlib.components.commands.Command;
import fr.zcraft.quartzlib.components.commands.CommandException;
import fr.zcraft.quartzlib.components.commands.CommandInfo;
import fr.zcraft.quartzlib.components.i18n.I;
import fr.zcraft.quartzlib.components.rawtext.RawText;
import fr.zcraft.quartzlib.components.rawtext.RawTextPart;
import me.cassayre.florian.hawk.Hawk;
import me.cassayre.florian.hawk.report.Report;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@CommandInfo(name = "info")
public class InfoCommand extends Command {
    @Override
    protected void run() throws CommandException {
        final Report report = Hawk.get().getReport();

        if (report == null) {
            info(I.t("There are no reports being recorded."));
            return;
        }

        final int startedMinutesAgo = (int) ((System.currentTimeMillis() - report.getStartDate()) / 60000);

        final int damages = report.getDamages().size();
        final int heals = report.getHeals().size();
        final int events = report.getEvents().size();
        final int all = damages + heals + events;

        final Player player = sender instanceof Player ? playerSender() : null;

        final RawTextPart<?> infos = new RawText().color(ChatColor.GRAY)
            .then(report.getTitle())
                .style(ChatColor.GREEN, ChatColor.BOLD)
            .then("\n")
            .then(I.tln(player, "Started a minute ago", "Started {0} minutes ago", startedMinutesAgo))
                .color(ChatColor.DARK_GRAY)
            .then("\n\n")
            .then(I.tln(player, "{0} player", "{0} players", report.getPlayers().size()))
            .then(" - ")
            .then(I.tln(player, "{0} tracked", "{0} tracked", report.getTrackedPlayers().size()))
            .then("\n");

        if (report.getTeams().size() != 0) {
            infos.then(I.tln(player, "{0} team", "{0} teams", report.getTeams().size())).then("\n");
        }

        infos
            .then(I.tln(player, "{0} record", "{0} records", all))
            .then(" (")
            .then(I.tln(player, "{0} damage", "{0} damages", damages)).then(", ")
            .then(I.tln(player, "{0} heal", "{0} heals", heals)).then(", ")
            .then(I.tln(player, "{0} event", "{0} events", events))
            .then(")");

        if (report.isAutoTrackingNewPlayers() || report.isStoppingTrackOnDeath() || report.isStoppingTrackOnDisconnection()) {
            infos.then("\n\n");
        }

        if (report.isAutoTrackingNewPlayers()) {
            infos.then(I.tl(player, "Tracking new players if they join")).color(ChatColor.DARK_GRAY);
        }

        if (report.isStoppingTrackOnDeath()) {
            infos.then(I.tl(player, "Stopping track on death")).color(ChatColor.DARK_GRAY);
        }

        if (report.isStoppingTrackOnDisconnection()) {
            infos.then(I.tl(player, "Stopping track on disconnection")).color(ChatColor.DARK_GRAY);
        }

        if (sender instanceof Player) {
            send(
                new RawText(I.tl(player, "Hawk is currently recording a game session."))
                        .color(ChatColor.GREEN)
                        .hover(infos)
                    .then(" ")
                    .then(I.tl(player, "Hover this message for details."))
                        .color(ChatColor.GRAY)
                        .hover(infos)
                    .build()
            );
        } else {
            send(
                new RawText(I.tl(player, "Hawk is currently recording a game session."))
                    .color(ChatColor.GREEN)
                    .hover(infos)
            );

            send(infos.build());
        }

        send(
            new RawText(I.tl(player, "To stop and publish this session, run {cc}/hawk stop{gray}."))
                .color(ChatColor.GRAY)
                .suggest(StopCommand.class)
                .hover(I.tl(player, "Click here to pre-fill the command"))
        );
    }
}
