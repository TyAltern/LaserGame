package me.tyalternative.laserGame.shop.ItemHint;

import me.tyalternative.laserGame.utils.TextUtil;

public record StatHint(StatTag tag, String displayValue) {
    public String getLine() {
        boolean plural = false;
        String signless = displayValue.substring(1);

        if (displayValue.contains("%")) plural = true;
        else if (TextUtil.isNumeric(displayValue)) plural = Double.parseDouble(displayValue) > 1;
        else if (TextUtil.isNumeric(signless)) plural = Double.parseDouble(signless) > 1;

        return displayValue + " " + tag.label(plural);
    }
}
