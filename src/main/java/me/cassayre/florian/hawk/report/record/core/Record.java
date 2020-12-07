package me.cassayre.florian.hawk.report.record.core;

import com.google.gson.JsonObject;
import java.time.Instant;
import java.util.TimeZone;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public abstract class Record {
    protected final OfflinePlayer player;
    protected long startDate, endDate, updateDate;

    public Record(Player player) {
        this.player = player;

        this.startDate = System.currentTimeMillis();
        updateDate = startDate;
    }

    public Record(Player player, long startDate, long endDate) {
        this.player = player;

        this.startDate = startDate;
        this.endDate = endDate;
    }

    public OfflinePlayer getPlayer() {
        return player;
    }

    public long getStartDate() {
        return startDate;
    }

    public long getEndDate() {
        if (endDate == 0) {
            throw new IllegalStateException("setEndDate() was not called!");
        }

        return endDate;
    }


    public boolean isEnded() {
        return endDate != 0;
    }

    public void setEndDateNow() {
        if (isEnded()) {
            throw new IllegalStateException("setEndDate() as already been called!");
        }

        endDate = System.currentTimeMillis();
        updateDate = endDate;
    }

    public void setEndDateLastUpdate() {
        if (isEnded()) {
            throw new IllegalStateException("setEndDate() as already been called!");
        }

        endDate = updateDate;
    }

    public long getUpdateDate() {
        return updateDate;
    }

    protected void update() {
        if (isEnded()) {
            throw new IllegalStateException("setEndDate() as already been called!");
        }

        updateDate = System.currentTimeMillis();
    }

    public boolean inCooldown(final long cooldown) {
        return System.currentTimeMillis() - updateDate <= cooldown;
    }

    public JsonObject toJSON() {
        final JsonObject json = new JsonObject();
        json.addProperty("date",
                Instant.ofEpochMilli(startDate).atZone(TimeZone.getDefault().toZoneId()).toOffsetDateTime().toString());

        return json;
    }
}
