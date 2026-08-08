package dev.flomik.botania_ponder.ponder.scenes;

import dev.flomik.ponderlib.api.ParticleEmitter;
import dev.flomik.ponderlib.api.PonderPalette;
import dev.flomik.ponderlib.api.scene.SceneBuilder;
import dev.flomik.ponderlib.api.scene.SceneBuildingUtil;
import dev.flomik.ponderlib.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import vazkii.botania.client.fx.SparkleParticleData;

import java.util.List;
import java.util.Objects;

/** The Pure Daisy's complete basic conversion cycle. */
public final class PureDaisyScenes {

    private PureDaisyScenes() {
    }

    public static void conversion(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("pure_daisy_conversion", "Purifying Blocks with a Pure Daisy");
        scene.configureBasePlate(0, 0, 5);

        BlockPos daisyPos = util.grid().at(2, 1, 2);
        Vec3 daisyCenter = util.vector().centerOf(daisyPos);
        Selection ring = util.select().fromTo(1, 1, 1, 3, 1, 3)
            .subtract(util.select().position(daisyPos));
        // Clockwise from the north-west corner: the first four are logs, the next four are stone.
        List<BlockPos> inputs = List.of(
            util.grid().at(1, 1, 1),
            util.grid().at(2, 1, 1),
            util.grid().at(3, 1, 1),
            util.grid().at(3, 1, 2),
            util.grid().at(3, 1, 3),
            util.grid().at(2, 1, 3),
            util.grid().at(1, 1, 3),
            util.grid().at(1, 1, 2)
        );

        scene.showBasePlate();
        focusOn(scene, daisyCenter);
        scene.idle(15);

        scene.world().showSection(util.select().position(daisyPos), Direction.DOWN);
        focusOn(scene, daisyCenter);
        scene.idle(10);
        scene.overlay().showOutlineWithText(util.select().position(daisyPos), 70,
                "A Pure Daisy slowly purifies compatible blocks placed directly around it")
            .placeNearTarget()
            .attachKeyFrame();
        scene.idle(75);

        for (BlockPos input : inputs) {
            scene.world().showSection(util.select().position(input), Direction.DOWN);
            // showSection focuses its own block; immediately restore the Daisy as the stable centre
            // so the staggered circular reveal does not pull the camera around eight times.
            focusOn(scene, daisyCenter);
            scene.idle(4);
        }
        scene.idle(10);
        scene.overlay().showOutlineWithText(ring, 75,
                "Logs and Stone can fill all eight spaces around the Daisy")
            .placeNearTarget()
            .attachKeyFrame();
        scene.idle(80);

        scene.overlay().showText(48, "Leave the blocks in place while the Daisy works")
            .pointAt(daisyCenter)
            .placeNearTarget()
            .colored(PonderPalette.INPUT);
        SparkleParticleData sparkle = SparkleParticleData.sparkle(0.8F, 1F, 1F, 1F, 5);
        ParticleEmitter emitter = scene.effects().particleEmitterWithinBlockSpace(sparkle, Vec3.ZERO);
        for (int tick = 0; tick < 48; tick++) {
            for (BlockPos input : inputs) {
                if ((tick + input.getX() + input.getZ()) % 4 == 0) {
                    scene.effects().emitParticles(util.vector().centerOf(input), emitter, 1, 1);
                }
            }
            scene.idle(1);
        }

        Block livingwood = botaniaBlock("livingwood_log");
        Block livingrock = botaniaBlock("livingrock");
        for (int i = 0; i < inputs.size(); i++) {
            BlockPos input = inputs.get(i);
            Block output = i < 4 ? livingwood : livingrock;
            scene.world().modifyBlock(input, ignored -> output.defaultBlockState(), true);
            scene.effects().indicateSuccess(input);
            scene.idle(4);
        }
        scene.idle(10);
        scene.overlay().showOutlineWithText(ring, 75,
                "Logs become Livingwood, while Stone becomes Livingrock")
            .placeNearTarget()
            .colored(PonderPalette.OUTPUT)
            .attachKeyFrame();
        scene.idle(80);

        scene.markAsFinished();
    }

    private static Block botaniaBlock(String path) {
        return Objects.requireNonNull(ForgeRegistries.BLOCKS.getValue(
            ResourceLocation.fromNamespaceAndPath("botania", path)));
    }

    private static void focusOn(SceneBuilder scene, Vec3 subject) {
        scene.addInstruction(s -> s.setFocusPoint(subject));
    }
}
