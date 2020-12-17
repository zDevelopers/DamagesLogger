package me.cassayre.florian.hawk.report.record;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import fr.zcraft.quartzlib.components.nbt.NBT;
import fr.zcraft.quartzlib.tools.reflection.NMSException;
import java.util.Map;
import me.cassayre.florian.hawk.Hawk;
import me.cassayre.florian.hawk.report.record.core.LifeChangeRecord;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

public class DamageRecord extends LifeChangeRecord {
    /**
     * The cause of this damage event.
     */
    private final DamageCause damageCause;

    /**
     * The weapon, already converted to JSON so it's frozen in time.
     */
    private final JsonElement weapon;

    /**
     * The player damager, if any. If this damage is not from a player, this will be null.
     */
    private final OfflinePlayer playerDamager;

    /**
     * The entity damager, if any. If this damage is not from an entity, this will be null.
     */
    private final EntityType entityDamager;

    public DamageRecord(Player player, double points, ItemStack weapon, Player playerDamager) {
        this(player, points, weapon, playerDamager, false);
    }

    public DamageRecord(Player player, double points, ItemStack weapon, Player playerDamager, boolean isLethal) {
        super(player, points, isLethal);

        this.weapon = snap(weapon);
        this.playerDamager = playerDamager;
        this.entityDamager = null;
        this.damageCause = DamageCause.PLAYER;
    }

    public DamageRecord(Player player, long startDate, long endDate, double points, ItemStack weapon, Player playerDamager, boolean isLethal) {
        super(player, startDate, endDate, points, isLethal);

        this.weapon = snap(weapon);
        this.playerDamager = playerDamager;
        this.entityDamager = null;
        this.damageCause = DamageCause.PLAYER;
    }

    public DamageRecord(Player player, double points, ItemStack weapon, EntityType entityDamager) {
        this(player, points, weapon, entityDamager, false);
    }

    public DamageRecord(Player player, double points, ItemStack weapon, EntityType entityDamager, boolean isLethal) {
        super(player, points, isLethal);

        this.weapon = snap(weapon);
        this.playerDamager = null;
        this.entityDamager = entityDamager;
        this.damageCause = DamageCause.ENTITY;
    }

    public DamageRecord(Player player, long startDate, long endDate, double points, ItemStack weapon, EntityType entityDamager, boolean isLethal) {
        super(player, startDate, endDate, points, isLethal);

        this.weapon = snap(weapon);
        this.playerDamager = null;
        this.entityDamager = entityDamager;
        this.damageCause = DamageCause.ENTITY;
    }

    public DamageRecord(Player player, double points, ItemStack weapon, DamageCause damageCause) {
        this(player, points, weapon, damageCause, false);
    }

    public DamageRecord(Player player, double points, ItemStack weapon, DamageCause damageCause, boolean isLethal) {
        super(player, points, isLethal);

        Validate.isTrue(damageCause != DamageCause.PLAYER, "To create a player damage, use the constructors accepting a Player argument.");
        Validate.isTrue(damageCause != DamageCause.ENTITY, "To create an entity damage, use the constructors accepting an EntityType argument.");

        this.weapon = snap(weapon);
        this.playerDamager = null;
        this.entityDamager = null;
        this.damageCause = damageCause;
    }

    public DamageRecord(Player player, long startDate, long endDate, double points, ItemStack weapon, DamageCause damageCause, boolean isLethal) {
        super(player, startDate, endDate, points, isLethal);

        Validate.isTrue(damageCause != DamageCause.PLAYER, "To create a player damage, use the constructors accepting a Player argument.");
        Validate.isTrue(damageCause != DamageCause.ENTITY, "To create an entity damage, use the constructors accepting an EntityType argument.");

        this.weapon = snap(weapon);
        this.playerDamager = null;
        this.entityDamager = null;
        this.damageCause = damageCause;
    }

    public JsonElement getWeapon() {
        return weapon;
    }

    /**
     * Converts an item stack to a JSON representation (matching item's NBT), effectively snapping it.
     *
     * @param weapon The item stack to convert.
     * @return The item's JSON representation.
     */
    private JsonElement snap(final ItemStack weapon) {
        if (weapon == null || weapon.getType() == Material.AIR) {
            return JsonNull.INSTANCE;
        }

        final JsonObject jsonItem = new JsonObject();
        jsonItem.addProperty("id", weapon.getType().getKey().toString());

        try {
            final Map<String, Object> weaponNBT = NBT.fromItemStack(weapon).toHashMap();
            if (!weaponNBT.isEmpty()) {
                jsonItem.add("tag", Hawk.GSON.toJsonTree(weaponNBT));
            }
        } catch (NMSException ignored) {
            // No tag sorry
        }

        return jsonItem;
    }

    public OfflinePlayer getPlayerDamager() {
        return playerDamager;
    }

    public EntityType getEntityDamager() {
        return entityDamager;
    }

    public DamageCause getDamageCause() {
        return damageCause;
    }

    @Override
    public JsonObject toJSON() {
        final JsonObject json = super.toJSON();

        final JsonObject jsonCause = new JsonObject();
        jsonCause.addProperty("type", damageCause.name());
        jsonCause.add("weapon", weapon);

        if (damageCause == DamageCause.ENTITY && entityDamager != null) {
            jsonCause.addProperty("entity", entityDamager.getKey().toString());
        }

        if (damageCause == DamageCause.PLAYER && playerDamager != null) {
            jsonCause.addProperty("player", playerDamager.getUniqueId().toString());
        }

        json.add("cause", jsonCause);
        json.addProperty("damagee", player.getUniqueId().toString());

        json.addProperty("damage", pointsNormalized());
        json.addProperty("lethal", isLethal);

        return json;
    }

    @Override
    public DamageRecord clone() {
        try {
            return (DamageRecord) super.clone();
        } catch (CloneNotSupportedException e) {
            return null; // Unreachable as the Cloneable interface is implemented.
        }
    }

    /**
     * Clone this damage record with a different number of points and a different lethality.
     * @param points    The damage points to set to the cloned record.
     * @param isLethal  {@code true} if the new record is a lethal damage.
     * @return A cloned and modified record.
     */
    public DamageRecord cloneWithDamage(final double points, final boolean isLethal) {
        final DamageRecord cloned = this.clone();
        cloned.points = points;
        cloned.isLethal = isLethal;
        return cloned;
    }

    public enum DamageCause {
        PLAYER,
        ENTITY,

        BLOCK_EXPLOSION,
        CONTACT,
        CRAMMING,
        DRAGON_BREATH,
        DROWNING,
        DRYOUT,
        FALL,
        FALLING_BLOCK,
        FIRE,
        FIRE_TICK,
        FLY_INTO_WALL,
        HOT_FLOOR,
        LAVA,
        LIGHTNING,
        MAGIC,
        MELTING,
        POISON,
        PROJECTILE,
        STARVATION,
        SUFFOCATION,
        SUICIDE,
        THORNS,
        VOID,
        WITHER,

        COMMAND,
        UNKNOWN;

        public static DamageCause fromBukkit(EntityDamageEvent.DamageCause cause) {
            switch (cause) {
                case ENTITY_ATTACK:
                case ENTITY_EXPLOSION:
                case ENTITY_SWEEP_ATTACK:
                    return ENTITY;

                case CUSTOM:
                    return UNKNOWN;

                default:
                    try {
                        return valueOf(cause.name());
                    } catch (IllegalArgumentException e) {
                        return UNKNOWN;
                    }
            }
        }
    }
}
