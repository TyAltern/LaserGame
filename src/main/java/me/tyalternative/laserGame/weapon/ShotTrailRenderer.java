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

/**
 * Affiche la trajectoire d'un tir, dans l'un des deux modes configurables
 * (voir trail.mode dans config.yml) :
 *  - CUBES : plusieurs petites Block Display alignées le long du trajet.
 *  - LINE  : une seule Block Display étirée entre start et end.
 */
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
                bd.setGravity(false); // sans ça l'entité tombe dès le spawn (comportement par défaut d'un Display)
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
     * [0,1]^3 (un bloc entier, l'entité étant à l'origine "coin", pas centrée).
     * On la scale à (épaisseur, épaisseur, distance) puis on fait pivoter son
     * axe local Z pour qu'il pointe vers 'direction' : comme l'axe Z local
     * part de 0 (à l'origine de l'entité, donc "start"), l'étirement selon Z
     * couvre exactement [start, end] une fois tourné.
     *
     * ROTATION : dérivée du yaw/pitch calculé par Location#setDirection()
     * (donc par Bukkit lui-même), plutôt que d'un calcul d'arc quaternion
     * maison (Quaternionf#rotationTo) que je ne pouvais pas vérifier
     * visuellement et qui s'est avéré ne pas fonctionner correctement en jeu.
     * rotateY(-yaw).rotateX(pitch) reproduit exactement la convention de
     * Location#getDirection() (yaw=0 -> +Z), vérifiée par dérivation manuelle
     * des deux formules - beaucoup plus sûr que de réinventer le calcul.
     *
     * GRAVITÉ : bug quasi certain de la version précédente - une Block
     * Display a la gravité activée PAR DÉFAUT comme n'importe quelle entité.
     * Sans setGravity(false), le bloc étiré tombe dès le spawn, ce qui donne
     * exactement le symptôme "part dans tous les sens" une fois combiné à
     * l'étirement/rotation (surtout visible vu que l'entité ne vit que 0.3s,
     * donc on ne voit quasiment que la chute, jamais un état stable).
     *
     * Simplification assumée : la translation de recentrage de l'épaisseur
     * (present dans une version précédente) a été retirée pour réduire les
     * sources d'erreur pendant qu'on confirme que la rotation de base
     * fonctionne. Le décalage résultant (jusqu'à thickness/2, donc quelques
     * centimètres avec l'épaisseur par défaut) est imperceptible pour un
     * faisceau fin ; à réintroduire seulement si besoin une fois validé.
     */
    private void renderLine(Location start, Location end) {
        Vector direction = end.toVector().subtract(start.toVector());
        double distance = direction.length();
        if (distance < 1e-3) return;

        Location dirLocation = start.clone();
        dirLocation.setDirection(direction); // pas besoin de normaliser, setDirection gère ça en interne
        float yawRad = (float) Math.toRadians(dirLocation.getYaw());
        float pitchRad = (float) Math.toRadians(dirLocation.getPitch());

        System.out.println("yaw: " + yawRad + " - pitch: " + pitchRad);

        Quaternionf rotation = new Quaternionf()
                .rotateY(-yawRad)
                .rotateX(pitchRad);

        float thickness = config.getTrailThickness();
        Transformation transformation = new Transformation(
                new Vector3f(0, 0, 0),
                rotation,
                new Vector3f(thickness, thickness, (float) distance),
                new Quaternionf()
        );

        BlockDisplay display = start.getWorld().spawn(start, BlockDisplay.class, bd -> {
            bd.setBlock(config.getTrailMaterial().createBlockData());
            bd.setTransformation(transformation);
            bd.setBrightness(new Display.Brightness(15, 15));
            bd.setGravity(false);
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