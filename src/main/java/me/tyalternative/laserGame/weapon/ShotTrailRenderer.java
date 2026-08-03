package me.tyalternative.laserGame.weapon;

import me.tyalternative.laserGame.config.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class ShotTrailRenderer {

    private final Plugin plugin;
    private final ConfigManager config;

    public ShotTrailRenderer(Plugin plugin, ConfigManager config) {
        this.plugin = plugin;
        this.config = config;
    }

    public void render(Location start, Location end) {
        if (config.getTrailMode() == TrailMode.LINE) {
            renderLine(start, end);
        } else {
            renderCubes(start, end);
        }
    }

    private void renderCubes(Location start, Location end) {
        Vector direction = end.toVector().subtract(start.toVector());
        double distance = direction.length();
        if (distance < 1e-3) return;
        direction.normalize();

        double step = config.getTrailStep();
        float scale = config.getTrailThickness();
        List<BlockDisplay> spawned = new ArrayList<>();

        for (double d = 0; d < distance; d += step) {
            Location point = start.clone().add(direction.clone().multiply(d));
            BlockDisplay display = start.getWorld().spawn(point, BlockDisplay.class, bd -> {
                bd.setBlock(config.getTrailMaterial().createBlockData());
                bd.setTransformation(new Transformation(
                        new Vector3f(0, 0, 0),
                        new Quaternionf(),
                        new Vector3f(scale, scale, scale),
                        new Quaternionf()
                ));
                bd.setBrightness(new Display.Brightness(15, 15));
                bd.setPersistent(false);
            });
            spawned.add(display);
        }

        scheduleRemoval(spawned.stream().map(e -> (org.bukkit.entity.Entity) e).toList());
    }

    /**
     * Une seule Block Display étirée entre start et end.
     *
     * Principe : une Block Display non transformée occupe l'espace local
     * [0,1]^3 (un bloc entier). On la scale à (épaisseur, épaisseur, distance)
     * puis on fait pivoter son axe local Z pour qu'il pointe vers 'direction' :
     * comme l'axe Z local part de 0 (à l'origine de l'entité, donc "start"),
     * l'étirement selon Z couvre exactement [start, end] une fois tourné.
     *
     * La translation est utilisée pour recentrer le faisceau fin (épaisseur en
     * X/Y) sur l'axe du tir plutôt que d'avoir la bordure du bloc collée sur
     * l'axe. Comme la translation d'une Transformation est appliquée telle
     * quelle (non re-tournée), on tourne nous-même le vecteur de décalage avec
     * le même quaternion avant de le passer en translation, pour qu'il suive
     * l'orientation du faisceau.
     */
    private void renderLine(Location start, Location end) {
        Vector direction = end.toVector().subtract(start.toVector());
        double distance = direction.length();
        if (distance < 1e-3) return;
        Vector3f dirNormalized = new Vector3f(
                (float) (direction.getX() / distance),
                (float) (direction.getY() / distance),
                (float) (direction.getZ() / distance)
        );

        float thickness = config.getTrailThickness();
        Quaternionf rotation = new Quaternionf().rotationTo(new Vector3f(0, 0, 1), dirNormalized);

        Vector3f translation = new Vector3f(-thickness / 2f, -thickness / 2f, 0f);
        rotation.transform(translation); // recentre le faisceau selon l'orientation réelle du tir

        Transformation transformation = new Transformation(
                translation,
                rotation,
                new Vector3f(thickness, thickness, (float) distance),
                new Quaternionf()
        );

        BlockDisplay display = start.getWorld().spawn(start, BlockDisplay.class, bd -> {
            bd.setBlock(config.getTrailMaterial().createBlockData());
            bd.setTransformation(transformation);
            bd.setBrightness(new Display.Brightness(15, 15));
            bd.setPersistent(false);
        });

        scheduleRemoval(List.of(display));
    }

    private void scheduleRemoval(List<org.bukkit.entity.Entity> entities) {
        long lifetime = config.getTrailLifetimeTicks();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (org.bukkit.entity.Entity e : entities) {
                if (!e.isDead()) e.remove();
            }
        }, lifetime);
    }
}
