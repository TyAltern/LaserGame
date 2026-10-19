package me.tyalternative.laserGame.effect;

public interface ConditionalSkillEffect extends SkillEffect {
    boolean canActivate(ActivationContext ctx);
}
