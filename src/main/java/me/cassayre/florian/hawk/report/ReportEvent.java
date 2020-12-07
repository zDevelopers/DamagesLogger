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

package me.cassayre.florian.hawk.report;

import com.google.gson.JsonObject;
import java.time.Instant;
import java.util.TimeZone;
import org.bukkit.OfflinePlayer;

/**
 * An event displayed in the game timeline of the report.
 */
public class ReportEvent {
    private final long date;
    private final EventType type;

    private final String title;
    private final String description;

    private final EventIcon icon;
    private final String iconAttribute;

    public ReportEvent(final String title, final String description, final EventIcon icon, final String iconAttribute) {
        this(EventType.BLUE, title, description, icon, iconAttribute);
    }

    public ReportEvent(final EventType type, final String title, final String description, final EventIcon icon,
                       final String iconAttribute) {
        this(System.currentTimeMillis(), type, title, description, icon, iconAttribute);
    }

    public ReportEvent(final long date, final EventType type, final String title, final String description,
                       final EventIcon icon, final String iconAttribute) {
        this.date = date;
        this.type = type;
        this.title = title;
        this.description = description;
        this.icon = icon;
        this.iconAttribute = iconAttribute;
    }

    public static ReportEvent withPlayer(final EventType type, final String title, final String description,
                                         final OfflinePlayer player) {
        return new ReportEvent(type, title, description, EventIcon.PLAYER, player.getUniqueId().toString());
    }

    public static ReportEvent withPlayer(final EventType type, final String title, final OfflinePlayer player) {
        return new ReportEvent(type, title, null, EventIcon.PLAYER, player.getUniqueId().toString());
    }

    public static ReportEvent withPlayer(final String title, final String description, final OfflinePlayer player) {
        return new ReportEvent(title, description, EventIcon.PLAYER, player.getUniqueId().toString());
    }

    public static ReportEvent withPlayer(final String title, final OfflinePlayer player) {
        return new ReportEvent(title, null, EventIcon.PLAYER, player.getUniqueId().toString());
    }

    public static ReportEvent withIcon(final EventType type, final String title, final String description,
                                       final String iconID) {
        return new ReportEvent(type, title, description, EventIcon.ICON, iconID);
    }

    public static ReportEvent withIcon(final EventType type, final String title, final String iconID) {
        return new ReportEvent(type, title, null, EventIcon.ICON, iconID);
    }

    public static ReportEvent withIcon(final String title, final String description, final String iconID) {
        return new ReportEvent(title, description, EventIcon.ICON, iconID);
    }

    public static ReportEvent withIcon(final String title, final String iconID) {
        return new ReportEvent(title, null, EventIcon.ICON, iconID);
    }

    public static ReportEvent withURL(final EventType type, final String title, final String description,
                                      final String iconURL) {
        return new ReportEvent(type, title, description, EventIcon.URL, iconURL);
    }

    public static ReportEvent withURL(final EventType type, final String title, final String iconURL) {
        return new ReportEvent(type, title, null, EventIcon.URL, iconURL);
    }

    public static ReportEvent withURL(final String title, final String description, final String iconURL) {
        return new ReportEvent(title, description, EventIcon.URL, iconURL);
    }

    public static ReportEvent withURL(final String title, final String iconURL) {
        return new ReportEvent(title, null, EventIcon.URL, iconURL);
    }

    public JsonObject toJSON() {
        final JsonObject json = new JsonObject();

        json.addProperty("date",
                Instant.ofEpochMilli(date).atZone(TimeZone.getDefault().toZoneId()).toOffsetDateTime().toString());
        json.addProperty("type", type.name());
        json.addProperty("title", title);
        json.addProperty("description", description);

        final JsonObject jsonIcon = new JsonObject();

        jsonIcon.addProperty("type", icon.name().toLowerCase());
        jsonIcon.addProperty(icon.attributeKey, iconAttribute);

        json.add("icon", jsonIcon);

        return json;
    }

    public enum EventType {
        BLUE,
        GOLD,
        GREEN,
        RED,
    }

    public enum EventIcon {
        PLAYER("uuid"),
        ICON("icon_id"),
        URL("url"),

        ;

        private final String attributeKey;

        EventIcon(final String attributeKey) {
            this.attributeKey = attributeKey;
        }
    }
}
