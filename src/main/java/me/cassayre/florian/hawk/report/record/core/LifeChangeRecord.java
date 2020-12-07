package me.cassayre.florian.hawk.report.record.core;

import org.bukkit.entity.Player;

public abstract class LifeChangeRecord extends Record implements Cloneable {
    private static final int POINTS_MAX = (int) Math.pow(2, 16) - 1;

    protected double points;
    protected boolean isLethal;

    public LifeChangeRecord(Player player, double points) {
        this(player, points, false);
    }

    public LifeChangeRecord(Player player, double points, boolean isLethal) {
        super(player);

        this.points = points;
        this.isLethal = isLethal;

        if (isLethal) {
            endDate = System.currentTimeMillis();
            updateDate = endDate;
        }
    }

    public LifeChangeRecord(Player player, long startDate, long endDate, double points, boolean isLethal) {
        super(player, startDate, endDate);

        this.points = points;
        this.isLethal = isLethal;
    }

    public double getPoints() {
        return points;
    }

    public void addPoints(double points) {
        addPoints(points, false);
    }

    public void addPoints(double points, boolean isLethal) {
        this.points += points;
        this.isLethal = isLethal;

        if (isLethal) {
            setEndDateNow();
        } else {
            update();
        }
    }

    protected int pointsNormalized() {
        return (int) Math.min(Math.abs(points), POINTS_MAX);
    }

    public boolean isLethal() {
        return isLethal;
    }
}
