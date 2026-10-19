package me.tyalternative.laserGame.shop.ItemHint;

import me.tyalternative.laserGame.lang.Lang;

public enum StatTag {
    MONEY_BONUS("Argent Bonus"),                                        // Argent Bonus
    SHOP_SLOT("Slot Shop"),                                             // Slot Shop
    CONSUMABLE_SLOT("Slot Consommable"),                                 // Slot Consommable
    AMMO_BONUS("Muni Bonus"),                                           // Muni Bonus
    FREE_SHOT("Tir Gratuit","Tirs Gratuits"),            // Tir Gratuit
    FIRE_RATE("Cadence de Tir"),                                        // Cadence de tir
    HIT_RADIUS("Taille Faisceau"),                                        // Cadence de tir
    DAMAGE_BLOCKED("Degat Annule", "Degats Annules"),    // Degats Annules
    RELOAD_SPEED("Rechargement"),                                       // Rechargement
    ABILITY_COOLDOWN("de Cooldown"),                                       // Rechargement
    EXTRA_LIFE("Vie", "Vies"),                           // Vie
    RANGE("Portee"),                                                    // Portee
    MAX_AMMO("Munition", "Munitions"),                   // Munitions
    DAMAGE_DEALT("Degat Infligé", "Degats Infligés"),                     // Degats
    CRITICAL_DAMAGE_DEALT("Degat Critique", "Degats Critiques"),                     // Degats
    DAMAGE_TAKEN("Degat Subit", "Degats Subits"),        // Degat Subit
    MOVEMENT_SPEED("Vitesse"),                                          // Vitesse
    CROUCH_TIME("Tps Accroupi"),                                        // Tps Accroupis
    INVISIBILITY("Invisibilite"),                                        // Tps Accroupis
    DURATION("Duree"),                                                  // Tps Accroupis
    RADIUS("Blocs de Distance"),                                                  // Tps Accroupis
    PIERCING_SHOT("Tir perforant", "Tirs Perforants"),  // Tire Perforant                                      // Tps Accroupis
    AMMO_REGEN("Muni Regen"),                                           // Muni Regen
    RADAR("Radar"),
    DUPLICATE_ITEM("Doublon", "Doublons"),
    SHOP_DISCOUNT("Reduction Shop"),
    FREE_ITEM("Item Gratuit", "Items Gratuits"),
    VICTORY_POINT("Point de Victoire", "Points de Victoire"),
    EMPTY("")
    ;
    final String displayName;
    final String displayNamePlural;
    StatTag(String displayName) {
        this.displayName = displayName;
        this.displayNamePlural = displayName;
    }
    StatTag(String displayName, String displayNamePlural) {
        this.displayName = displayName;
        this.displayNamePlural = displayNamePlural;
    }
    public String label(boolean plural) {
        String key = "stat." + name().toLowerCase() + (plural ? ".other" : ".one");
        if (Lang.has(key)) return Lang.get(key);
        return plural ? displayNamePlural : displayName;
    }
}
