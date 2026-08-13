package dev.flomik.botania_ponder.ponder.scenes;

import dev.flomik.ponderlib.api.ParticleEmitter;
import dev.flomik.ponderlib.api.PonderPalette;
import dev.flomik.ponderlib.api.element.ElementLink;
import dev.flomik.ponderlib.api.element.EntityElement;
import dev.flomik.ponderlib.api.scene.SceneBuilder;
import dev.flomik.ponderlib.api.scene.SceneBuildingUtil;
import dev.flomik.ponderlib.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import vazkii.botania.client.fx.SparkleParticleData;
import vazkii.botania.common.block.block_entity.mana.ManaPoolBlockEntity;

import java.util.List;

/** One-function scenes for Mana transport and small automation devices. */
public final class ManaDeviceScenes {

    private ManaDeviceScenes() {
    }

    public static void manaVoid(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos voidPos = util.grid().at(2, 1, 3);
        BlockPos spreaderPos = util.grid().at(2, 1, 0);
        Vec3 voidCenter = util.vector().centerOf(voidPos);
        Vec3 spreaderCenter = util.vector().centerOf(spreaderPos);
        begin(scene, util, 5, "mana_void_disposal", "Disposing of Mana",
            voidPos, "The Mana Void accepts an unlimited amount of Mana and permanently destroys it");

        scene.world().showSection(util.select().position(spreaderPos), Direction.DOWN);
        ManaSpreaderScenes.aimSpreader(scene, spreaderPos, voidPos);
        focusOn(scene, voidCenter);
        scene.idle(10);
        scene.overlay().showText(55, "Aim a Mana Spreader directly at the Void")
            .pointAt(spreaderCenter)
            .placeNearTarget()
            .attachKeyFrame();
        scene.idle(60);

        for (int burst = 0; burst < 3; burst++) {
            ManaSpreaderScenes.burstTrail(scene, spreaderCenter, voidCenter);
            darkSparkle(scene, voidCenter, 12);
            scene.idle(10);
        }
        finish(scene, util.select().position(voidPos),
            "Every burst disappears immediately; the Mana Void never fills up");
    }

    public static void manaDetector(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos detectorPos = util.grid().at(2, 1, 2);
        BlockPos spreaderPos = util.grid().at(2, 1, 0);
        BlockPos lampPos = util.grid().at(2, 1, 3);
        Vec3 detectorCenter = util.vector().centerOf(detectorPos);
        Vec3 spreaderCenter = util.vector().centerOf(spreaderPos);
        begin(scene, util, 5, "mana_detector_pulse", "Detecting Mana Bursts",
            detectorPos, "The Mana Detector emits a short redstone pulse when a Mana Burst passes through it");

        Selection support = util.select().position(spreaderPos).add(util.select().position(lampPos));
        scene.world().showSection(support, Direction.DOWN);
        ManaSpreaderScenes.aimSpreader(scene, spreaderPos, detectorPos);
        focusOn(scene, detectorCenter);
        scene.idle(10);
        scene.overlay().showText(60, "Mana Bursts pass through the Detector instead of being absorbed")
            .pointAt(detectorCenter)
            .placeNearTarget()
            .attachKeyFrame();
        scene.idle(65);

        ManaSpreaderScenes.burstTrail(scene, spreaderCenter, detectorCenter.add(0, 0, 2));
        scene.world().modifyBlock(detectorPos,
            state -> state.setValue(BlockStateProperties.POWERED, true), false);
        scene.world().modifyBlock(lampPos,
            state -> state.setValue(BlockStateProperties.LIT, true), false);
        scene.effects().indicateRedstone(detectorPos);
        scene.idle(16);
        scene.world().modifyBlock(detectorPos,
            state -> state.setValue(BlockStateProperties.POWERED, false), false);
        scene.world().modifyBlock(lampPos,
            state -> state.setValue(BlockStateProperties.LIT, false), false);
        scene.idle(8);
        finish(scene, util.select().position(detectorPos),
            "The signal lasts only briefly, making each burst a distinct redstone event");
    }

    public static void manaDistributor(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos distributorPos = util.grid().at(3, 1, 3);
        BlockPos spreaderPos = util.grid().at(3, 1, 0);
        List<BlockPos> pools = List.of(
            util.grid().at(2, 1, 3), util.grid().at(4, 1, 3), util.grid().at(3, 1, 4));
        Vec3 distributorCenter = util.vector().centerOf(distributorPos);
        Vec3 spreaderCenter = util.vector().centerOf(spreaderPos);
        begin(scene, util, 7, "mana_distributor_split", "Distributing Mana Evenly",
            distributorPos, "The Mana Distributor divides incoming Mana between adjacent Mana Pools");

        Selection poolSelection = util.select().position(pools.get(0))
            .add(util.select().position(pools.get(1)))
            .add(util.select().position(pools.get(2)));
        for (BlockPos pool : pools) {
            scene.world().showSection(util.select().position(pool), Direction.DOWN);
            focusOn(scene, distributorCenter);
            scene.idle(4);
        }
        scene.idle(8);
        scene.overlay().showOutlineWithText(poolSelection, 65,
                "Only non-full Pools touching a horizontal side participate")
            .placeNearTarget()
            .attachKeyFrame();
        scene.idle(70);

        scene.world().showSection(util.select().position(spreaderPos), Direction.DOWN);
        ManaSpreaderScenes.aimSpreader(scene, spreaderPos, distributorPos);
        focusOn(scene, distributorCenter);
        scene.idle(10);
        ManaSpreaderScenes.burstTrail(scene, spreaderCenter, distributorCenter);
        distributeMana(scene, util, distributorPos, pools);
        for (BlockPos pool : pools) {
            scene.effects().indicateSuccess(pool);
        }
        scene.idle(8);
        finish(scene, poolSelection, "Each available Pool receives an equal share of the incoming burst");
    }

    public static void openCrate(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos cratePos = util.grid().at(2, 2, 2);
        Vec3 crateCenter = util.vector().centerOf(cratePos);
        begin(scene, util, 5, "open_crate_drop", "Dropping Items through an Open Crate",
            cratePos, "The Open Crate immediately ejects inserted items through its open bottom");

        ItemStack item = new ItemStack(Items.DIAMOND);
        Vec3 start = crateCenter.add(0, 1.5, 0);
        ElementLink<EntityElement> entity = scene.world().createItemEntity(
            start, new Vec3(0, -0.08, 0), item);
        scene.overlay().showText(56, "Items inserted from above do not remain stored inside")
            .pointAt(start)
            .placeNearTarget()
            .colored(PonderPalette.INPUT)
            .attachKeyFrame();
        scene.idle(54);
        scene.effects().indicateSuccess(cratePos);
        // 18, not 8: keeps this text's window from overlapping finish()'s.
        scene.idle(18);
        finish(scene, util.select().position(cratePos),
            "The item leaves directly below the Crate with no horizontal movement");
    }

    public static void spreaderTurntable(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos turntablePos = util.grid().at(2, 1, 2);
        BlockPos spreaderPos = util.grid().at(2, 2, 2);
        Vec3 turntableCenter = util.vector().centerOf(turntablePos);
        begin(scene, util, 5, "spreader_turntable_rotation", "Rotating a Mana Spreader",
            turntablePos, "A Spreader Turntable continuously rotates a Mana Spreader placed on top");

        scene.world().showSection(util.select().position(spreaderPos), Direction.DOWN);
        focusOn(scene, turntableCenter);
        scene.idle(10);
        scene.overlay().showOutlineWithText(util.select().position(spreaderPos), 60,
                "The Spreader's aim sweeps around while the Turntable runs")
            .placeNearTarget()
            .attachKeyFrame();
        // 75, not 65: text windows fade in/out over 5 ticks each beyond their declared duration,
        // so a 60-tick text needs at least 70 before the next one opens without overlapping it.
        scene.idle(75);

        scene.overlay().showText(60, "Rotation is continuous and changes the direction of future bursts")
            .pointAt(turntableCenter)
            .placeNearTarget()
            .colored(PonderPalette.OUTPUT);
        // Both shown block entities tick normally in Ponder. The Turntable's own ticker rotates
        // the Spreader by its configured one degree per tick; manually changing rotationX here
        // would stack with that real tick and make the model jump or run several times too fast.
        scene.idle(60);
        scene.effects().indicateSuccess(turntablePos);
        scene.idle(8);
        scene.markAsFinished();
    }

    private static void begin(SceneBuilder scene, SceneBuildingUtil util, int size,
                              String id, String title, BlockPos subjectPos, String description) {
        Vec3 center = util.vector().centerOf(subjectPos);
        scene.title(id, title);
        scene.configureBasePlate(0, 0, size);
        scene.showBasePlate();
        focusOn(scene, center);
        scene.idle(15);
        scene.world().showSection(util.select().position(subjectPos), Direction.DOWN);
        focusOn(scene, center);
        scene.idle(10);
        scene.overlay().showOutlineWithText(util.select().position(subjectPos), 65, description)
            .placeNearTarget()
            .attachKeyFrame();
        // 80, not 70: text windows fade in/out over 5 ticks each beyond their declared duration
        // (TextInstruction/FadeInOutInstruction), so a 65-tick text is on screen for 75 ticks - 80
        // leaves a small margin past that instead of landing the scene's first text on the boundary.
        scene.idle(80);
    }

    private static void distributeMana(SceneBuilder scene, SceneBuildingUtil util,
                                       BlockPos distributorPos, List<BlockPos> pools) {
        int duration = 36;
        Vec3 from = util.vector().centerOf(distributorPos);
        for (int tick = 1; tick <= duration; tick++) {
            double progress = tick / (double) duration;
            for (BlockPos poolPos : pools) {
                Vec3 to = util.vector().centerOf(poolPos);
                greenSparkle(scene, from.lerp(to, progress), 1);
                scene.world().modifyBlockEntity(poolPos, ManaPoolBlockEntity.class, pool -> {
                    int amount = ManaPoolBlockEntity.MAX_MANA / 2 / duration;
                    pool.receiveMana(amount);
                });
            }
            scene.idle(1);
        }
    }

    private static void darkSparkle(SceneBuilder scene, Vec3 at, int count) {
        SparkleParticleData data = SparkleParticleData.sparkle(1.1F, 0.2F, 0.2F, 0.2F, 6);
        ParticleEmitter emitter = scene.effects().particleEmitterWithinBlockSpace(data, Vec3.ZERO);
        scene.effects().emitParticles(at, emitter, count, 2);
    }

    private static void greenSparkle(SceneBuilder scene, Vec3 at, int count) {
        SparkleParticleData data = SparkleParticleData.sparkle(0.8F, 0.25F, 0.65F, 0.35F, 6);
        ParticleEmitter emitter = scene.effects().simpleParticleEmitter(data, Vec3.ZERO);
        scene.effects().emitParticles(at, emitter, count, 1);
    }

    private static void finish(SceneBuilder scene, Selection target, String text) {
        scene.overlay().showOutlineWithText(target, 65, text)
            .placeNearTarget()
            .colored(PonderPalette.OUTPUT)
            .attachKeyFrame();
        scene.idle(70);
        scene.markAsFinished();
    }

    private static void focusOn(SceneBuilder scene, Vec3 subject) {
        scene.addInstruction(s -> s.setFocusPoint(subject));
    }
}
