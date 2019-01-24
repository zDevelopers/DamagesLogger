package me.cassayre.florian.hawk.report.record;

import com.google.gson.JsonObject;
import me.cassayre.florian.hawk.report.record.core.LifeChangeRecord;
import org.bukkit.entity.Player;

public class HealRecord extends LifeChangeRecord
{
	public enum HealingType
    {
        NATURAL,
		GOLDEN_APPLE,
        NOTCH_APPLE,
        HEALING_POTION,
        COMMAND,
        UNKNOWN
	}

	private final HealingType healingType;

	public HealRecord(Player player, double points, HealingType healingType)
    {
        super(player, points);

        this.healingType = healingType;
	}

    public HealRecord(Player player, long startDate, long endDate, double points, HealingType healingType)
    {
        super(player, startDate, endDate, points, false);

        this.healingType = healingType;
    }

	public HealingType getHealingType()
    {
		return healingType;
	}

    @Override
    public boolean isLethal()
    {
        return false;
    }

    public boolean similarTo(final HealRecord other)
    {
        return this.healingType == other.healingType;
    }

    @Override
    public JsonObject toJSON()
    {
        final JsonObject json = super.toJSON();

        json.addProperty("cause", healingType.name());
        json.addProperty("heal", pointsNormalized());
        json.addProperty("healed", player.getUniqueId().toString());

        return json;
    }

    @Override
    public HealRecord clone()
    {
        try
        {
            return (HealRecord) super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            return null; // Unreachable as the Cloneable interface is implemented.
        }
    }

    @Override
    public String toString()
    {
        return "HealRecord{" + "healingType=" + healingType +
                ", points=" + points +
                ", isLethal=" + isLethal +
                ", player=" + player +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", updateDate=" + updateDate +
                '}';
    }
}
