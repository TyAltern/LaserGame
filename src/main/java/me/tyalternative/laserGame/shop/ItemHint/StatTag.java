package me.tyalternative.laserGame.shop.ItemHint;

public enum StatTag {
    MONEY_BONUS("Argent Bonus"),                                        // Argent Bonus
    SHOP_SLOT("Slot Shop"),                                             // Slot Shop
    CONSUMABLE_SLOT("Slot Consomable"),                                 // Slot Consommable
    AMMO_BONUS("Muni Bonus"),                                           // Muni Bonus
    FREE_SHOT("Tir Gratuit","Tirs Gratuits"),            // Tir Gratuit
    FIRE_RATE("Cadence de Tir"),                                        // Cadence de tir
    DAMAGE_BLOCKED("Degat Annule", "Degats Annules"),    // Degats Annules
    RELOAD_SPEED("Rechargement"),                                       // Rechargement
    EXTRA_LIFE("Vie", "Vies"),                           // Vie
    RANGE("Portee"),                                                    // Portee
    MAX_AMMO("Munition", "Munitions"),                   // Munitions
    DAMAGE_DEALT("Degat", "Degats"),                     // Degats
    DAMAGE_TAKEN("Degat Subit", "Degats SUbits"),        // Degat Subit
    MOVEMENT_SPEED("Vitesse"),                                          // Vitesse
    CROUCH_TIME("Tps Accroupi"),                                        // Tps Accroupis
    AMMO_REGEN("Muni Regen")                                            // Muni Regen
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
}
