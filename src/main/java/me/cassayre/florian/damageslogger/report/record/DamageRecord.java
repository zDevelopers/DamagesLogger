package me.cassayre.florian.damageslogger.report.record;

import com.google.gson.JsonObject;
import me.cassayre.florian.damageslogger.ReportsUtils;
import me.cassayre.florian.damageslogger.report.record.core.LifeChangeRecord;
import org.apache.commons.lang.Validate;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DamageRecord extends LifeChangeRecord
{
    private final DamageType damageType;
    private final Weapon weapon;
    private final Map<Enchantment, Integer> weaponEnchantments;
    private final OfflinePlayer damager;

    public DamageRecord(Player player, double points, Weapon weapon, Player damager)
    {
        this(player, points, weapon, damager, false);
    }

    public DamageRecord(Player player, double points, Weapon weapon, Player damager, boolean isLethal)
    {
        this(player, points, weapon, null, damager, isLethal);
    }

    public DamageRecord(Player player, double points, Weapon weapon, Map<Enchantment, Integer> weaponEnchantments, Player damager, boolean isLethal)
    {
        super(player, points, isLethal);

        this.weapon = weapon;
        this.weaponEnchantments = weaponEnchantments != null ? new HashMap<>(weaponEnchantments) : Collections.emptyMap();
        this.damager = damager;
        this.damageType = DamageType.PLAYER;
    }

    public DamageRecord(Player player, long startDate, long endDate, double points, Weapon weapon, Player damager, boolean isLethal)
    {
        this(player, startDate, endDate, points, weapon, null, damager, isLethal);
    }

    public DamageRecord(Player player, long startDate, long endDate, double points, Weapon weapon, Map<Enchantment, Integer> weaponEnchantments, Player damager, boolean isLethal)
    {
        super(player, startDate, endDate, points, isLethal);

        this.weapon = weapon;
        this.weaponEnchantments = weaponEnchantments != null ? new HashMap<>(weaponEnchantments) : Collections.emptyMap();
        this.damager = damager;
        this.damageType = DamageType.PLAYER;
    }

    public DamageRecord(Player player, double points, Weapon weapon, DamageType damageType)
    {
        this(player, points, weapon, damageType, false);
    }

    public DamageRecord(Player player, double points, Weapon weapon, DamageType damageType, boolean isLethal)
    {
        this(player, points, weapon, null, damageType, isLethal);
    }

    public DamageRecord(Player player, double points, Weapon weapon, Map<Enchantment, Integer> weaponEnchantments, DamageType damageType, boolean isLethal)
    {
        super(player, points, isLethal);

        Validate.isTrue(damageType != DamageType.PLAYER, "To create a player damage, use the constructors accepting a Player argument.");

        this.weapon = weapon;
        this.weaponEnchantments = weaponEnchantments != null ? new HashMap<>(weaponEnchantments) : Collections.emptyMap();
        this.damager = null;
        this.damageType = damageType;
    }

    public DamageRecord(Player player, long startDate, long endDate, double points, Weapon weapon, DamageType damageType, boolean isLethal)
    {
        this(player, startDate, endDate, points, weapon, null, damageType, isLethal);
    }

    public DamageRecord(Player player, long startDate, long endDate, double points, Weapon weapon, Map<Enchantment, Integer> weaponEnchantments, DamageType damageType, boolean isLethal)
    {
        super(player, startDate, endDate, points, isLethal);

        Validate.isTrue(damageType != DamageType.PLAYER, "To create a player damage, use the constructors accepting a Player argument.");

        this.weapon = weapon;
        this.weaponEnchantments = weaponEnchantments != null ? new HashMap<>(weaponEnchantments) : Collections.emptyMap();
        this.damager = null;
        this.damageType = damageType;
    }

    public Weapon getWeapon()
    {
        return weapon;
    }

    public OfflinePlayer getDamager()
    {
        return damager;
    }

    public DamageType getDamageType()
    {
        return damageType;
    }

    public boolean similarTo(final DamageRecord other)
    {
        return Objects.equals(this.damager, other.damager)
                && this.damageType == other.damageType
                && this.weapon == other.weapon;
    }

    @Override
    public JsonObject toJSON()
    {
        final JsonObject json = super.toJSON();

        json.addProperty("cause", damageType.name());
        json.addProperty("weapon", weapon != null ? weapon.name() : null);

        if (!weaponEnchantments.isEmpty())
        {
            final JsonObject enchantments = new JsonObject();
            weaponEnchantments.forEach((enchantment, level) -> enchantments.addProperty(ReportsUtils.getEnchantmentID(enchantment), level));

            json.add("weapon_enchantments", enchantments);
        }

        if (damager != null) json.addProperty("damager", damager.getUniqueId().toString());
        json.addProperty("damagee", player.getUniqueId().toString());

        json.addProperty("damage", (int) Math.abs(points));
        json.addProperty("lethal", isLethal);

        return json;
    }

    @Override
    public DamageRecord clone()
    {
        try
        {
            return (DamageRecord) super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            return null; // Unreachable as the Cloneable interface is implemented.
        }
    }

    @Override
    public String toString()
    {
        return "DamageRecord{" + "damageType=" + damageType +
                ", weapon=" + weapon +
                ", weaponEnchantments=" + weaponEnchantments +
                ", damager=" + damager +
                ", points=" + points +
                ", isLethal=" + isLethal +
                ", player=" + player +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", updateDate=" + updateDate +
                '}';
    }

    public enum DamageType
    {
        PLAYER,

        ZOMBIE,
        SKELETON,
        PIGMAN,
        WITCH,
        SPIDER,
        CAVE_SPIDER,
        CREEPER,
        ENDERMAN,
        SLIME,
        GHAST,
        MAGMA_CUBE,
        BLAZE,
        WOLF,
        ANGRY_WOLF,
        SILVERFISH,
        IRON_GOLEM,
        ZOMBIE_VILLAGER,
        ENDER_DRAGON,
        WITHER,
        WITHER_SKELETON,

        FIRE,
        LAVA,
        THUNDERBOLT,
        CACTUS,
        TNT,
        FALL,
        SUFFOCATION,
        DROWNING,
        STARVATION,

        COMMAND,
        UNKNOWN
    }

    public enum Weapon
    {
        FISTS,

        SWORD_WOOD,
        SWORD_GOLD,
        SWORD_STONE,
        SWORD_IRON,
        SWORD_DIAMOND,

        AXE_WOOD,
        AXE_GOLD,
        AXE_STONE,
        AXE_IRON,
        AXE_DIAMOND,

        BOW,

        MAGIC,
        THORNS,

        UNKNOWN
    }
}
