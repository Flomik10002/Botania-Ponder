package dev.flomik.botania_ponder.ponder;

import dev.flomik.ponderlib.api.element.ElementLink;
import dev.flomik.ponderlib.api.element.EntityElement;
import dev.flomik.ponderlib.api.scene.SceneBuilder;
import net.minecraft.world.phys.Vec3;

/**
 * One targeted animation that deliberately cannot use normal entity physics.
 * TODO(PONDERLIB-FIX): Recheck after native collision-free/keyframed entity movement lands.
 */
public final class PonderSceneUtil {

    private PonderSceneUtil() {
    }

    /**
     * Moves an entity manually along an accelerating drop curve. Gravity and collisions are
     * disabled so decorative block collision shapes cannot stop the entity before {@code target}.
     * The final instruction places it exactly at {@code target}.
     */
    public static void dropEntity(SceneBuilder scene, ElementLink<EntityElement> entityLink,
                                  Vec3 start, Vec3 target, int duration) {
        if (duration <= 0) {
            throw new IllegalArgumentException("Drop duration must be positive");
        }

        scene.world().modifyEntity(entityLink, entity -> {
            entity.setNoGravity(true);
            entity.noPhysics = true;
            entity.setDeltaMovement(Vec3.ZERO);
            entity.setPos(start);
        });

        for (int tick = 1; tick <= duration; tick++) {
            double progress = tick / (double) duration;
            double fallingProgress = progress * progress;
            Vec3 position = start.lerp(target, fallingProgress);
            scene.world().modifyEntity(entityLink, entity -> entity.setPos(position));
            scene.idle(1);
        }
    }

}
