package me.tyalternative.laserGame.shop.ItemHint;

import me.tyalternative.laserGame.utils.TextUtil;

public record StatHint(StatTag tag, String displayValue) {
    public String getLine() {
        boolean plural = false;

        if (displayValue.contains("%")) plural = true;
        else if (TextUtil.isNumeric(displayValue)) plural = Double.parseDouble(displayValue) > 1;

        return displayValue + " " + (plural ? tag.displayNamePlural : tag.displayName);
    }
}
