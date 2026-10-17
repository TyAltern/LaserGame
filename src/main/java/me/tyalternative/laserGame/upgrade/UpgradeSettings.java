package me.tyalternative.laserGame.upgrade;

import java.util.Map;

public final class UpgradeSettings {

    private final String upgradeId;
    private final Map<String, Object> params;

    public UpgradeSettings(String upgradeId, Map<String, Object> params) {
        this.upgradeId = upgradeId;
        this.params = params == null ? Map.of() : params;
    }

    public String upgradeId() { return upgradeId; }

    public double getDouble(String key, double def) {
        Object value = params.get(key);
        if (value instanceof Number number) return number.doubleValue();
        if (value != null) {
            try {
                return Double.parseDouble(String.valueOf(value).trim());
            } catch (NumberFormatException ignored) { }
        }
        return def;
    }

    public int getInt(String key, int def) {
        return (int) Math.round(getDouble(key, def));
    }

    public long getLong(String key, int def) {
        return Math.round(getDouble(key, def));
    }

    public boolean getBoolean(String key, boolean def) {
        Object value = params.get(key);
        if (value instanceof Boolean bool) return bool;
        if (value != null) return Boolean.parseBoolean(String.valueOf(value).trim());
        return def;
    }

    public String getString(String key, String def) {
        Object value = params.get(key);
        return value == null ? def : String.valueOf(value);
    }
}
