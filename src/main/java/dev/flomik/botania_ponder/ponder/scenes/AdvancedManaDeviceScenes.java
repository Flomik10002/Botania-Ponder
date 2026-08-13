package dev.flomik.botania_ponder.ponder.scenes;

import dev.flomik.ponderlib.api.PonderPalette;
import dev.flomik.ponderlib.api.scene.SceneBuilder;
import dev.flomik.ponderlib.api.scene.SceneBuildingUtil;
import dev.flomik.ponderlib.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import vazkii.botania.client.fx.SparkleParticleData;
import vazkii.botania.common.block.block_entity.mana.ManaPoolBlockEntity;
import vazkii.botania.common.block.block_entity.mana.ManaSpreaderBlockEntity;

/** Focused scenes for advanced spreaders and the complete pylon family. */
public final class AdvancedManaDeviceScenes {

    private AdvancedManaDeviceScenes() {
    }

    public static void redstoneSpreader(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos spreader = util.grid().at(2, 1, 1);
        BlockPos lever = util.grid().at(1, 1, 1);
        BlockPos pool = util.grid().at(2, 1, 4);
        Vec3 focus = util.vector().centerOf(spreader);
        begin(scene, util, 5, "redstone_spreader_pulse", "Firing on a Redstone Pulse", spreader,
            "Unlike a normal Spreader, the Redstone Mana Spreader fires only on a rising redstone edge");

        reveal(scene, util.select().position(lever).add(util.select().position(pool)), focus);
        ManaSpreaderScenes.aimSpreader(scene, spreader, pool);
        scene.world().modifyBlockEntity(spreader, ManaSpreaderBlockEntity.class, be -> be.receiveMana(640));
        scene.overlay().showText(55, "Charging it does not fire a burst until the signal turns on")
            .pointAt(focus).placeNearTarget().attachKeyFrame();
        scene.idle(70);

        scene.world().modifyBlock(lever, state -> state.setValue(BlockStateProperties.POWERED, true), false);
        scene.effects().indicateRedstone(lever);
        ManaSpreaderScenes.burstTrail(scene, focus, util.vector().centerOf(pool));
        scene.world().modifyBlockEntity(pool, ManaPoolBlockEntity.class, be -> be.receiveMana(160));
        finish(scene, util.select().position(spreader),
            "Each off-to-on transition releases exactly one burst, enabling precise timing");
    }

    public static void elvenSpreader(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos spreader = util.grid().at(2, 1, 1);
        BlockPos pool = util.grid().at(2, 1, 4);
        Vec3 focus = util.vector().centerOf(spreader);
        begin(scene, util, 5, "elven_spreader_payload", "Sending Larger Mana Bursts", spreader,
            "The Elven Mana Spreader carries 240 Mana per burst instead of the basic Spreader's 160");

        reveal(scene, util.select().position(pool), focus);
        ManaSpreaderScenes.aimSpreader(scene, spreader, pool);
        scene.world().modifyBlockEntity(spreader, ManaSpreaderBlockEntity.class, be -> be.receiveMana(1000));
        ManaSpreaderScenes.burstTrail(scene, focus, util.vector().centerOf(pool));
        scene.world().modifyBlockEntity(pool, ManaPoolBlockEntity.class, be -> be.receiveMana(240));
        scene.effects().indicateSuccess(pool);
        finish(scene, util.select().position(spreader),
            "Its faster, larger payload improves throughput without changing how targets are bound");
    }

    public static void gaiaSpreader(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos spreader = util.grid().at(3, 1, 1);
        BlockPos pool = util.grid().at(3, 1, 6);
        Vec3 focus = util.vector().centerOf(util.grid().at(3, 1, 3));
        begin(scene, util, 7, "gaia_spreader_throughput", "Moving Mana at Maximum Throughput", spreader,
            "The Gaia Mana Spreader launches 640 Mana at twice the basic burst speed");

        reveal(scene, util.select().position(pool), focus);
        ManaSpreaderScenes.aimSpreader(scene, spreader, pool);
        scene.world().modifyBlockEntity(spreader, ManaSpreaderBlockEntity.class, be -> be.receiveMana(6400));
        fastBurst(scene, focus.add(0, 0, -2), util.vector().centerOf(pool));
        scene.world().modifyBlockEntity(pool, ManaPoolBlockEntity.class, be -> be.receiveMana(640));
        scene.effects().indicateSuccess(pool);
        finish(scene, util.select().position(spreader),
            "Its 6400 Mana buffer supports sustained high-volume transfers");
    }

    public static void manaPylon(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos pylon = util.grid().at(1, 1, 2);
        BlockPos second = util.grid().at(3, 1, 2);
        BlockPos table = util.grid().at(2, 1, 3);
        Vec3 focus = util.vector().centerOf(table);
        begin(scene, util, 5, "mana_pylon_enchanting", "Powering an Enchanting Table", pylon,
            "Mana Pylons act as extremely powerful bookshelves for a nearby Enchanting Table");

        reveal(scene, util.select().position(second).add(util.select().position(table)), focus);
        scene.overlay().showText(55, "Two correctly spaced Mana Pylons are enough to unlock level 30 enchantments")
            .pointAt(util.vector().centerOf(table)).placeNearTarget().attachKeyFrame();
        scene.idle(70);
        sparkle(scene, util.vector().centerOf(pylon), 14, 0.45F, 0.55F, 1F);
        sparkle(scene, util.vector().centerOf(second), 14, 0.45F, 0.55F, 1F);
        scene.overlay().showControls(util.vector().topOf(table), dev.flomik.ponderlib.api.Pointing.DOWN, 30)
            .withItem(new ItemStack(Items.DIAMOND_PICKAXE)).rightClick();
        finish(scene, util.select().position(pylon).add(util.select().position(second)),
            "Keep the same one-block air gap used for ordinary Bookshelves");
    }

    public static void naturaPylon(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos pylon = util.grid().at(2, 2, 1);
        BlockPos pool = pylon.below();
        BlockPos portal = util.grid().at(2, 1, 4);
        Vec3 focus = util.vector().centerOf(util.grid().at(2, 1, 2));
        begin(scene, util, 5, "natura_pylon_portal", "Stabilizing the Alfheim Portal", pylon,
            "A Natura Pylon above a Mana Pool supplies one side of an Alfheim Portal");

        reveal(scene, util.select().position(pool).add(util.select().position(portal)), focus);
        scene.world().modifyBlockEntity(pool, ManaPoolBlockEntity.class,
            be -> be.receiveMana(ManaPoolBlockEntity.MAX_MANA / 2));
        trail(scene, util.vector().centerOf(pylon), util.vector().centerOf(portal), 30,
            0.35F, 0.9F, 0.45F);
        finish(scene, util.select().position(pylon),
            "The complete portal requires two Mana Pool and Natura Pylon pairs");
    }

    public static void gaiaPylon(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos beacon = util.grid().at(4, 1, 4);
        BlockPos[] pylons = {
            util.grid().at(0, 2, 0), util.grid().at(0, 2, 8),
            util.grid().at(8, 2, 0), util.grid().at(8, 2, 8)
        };
        Vec3 focus = util.vector().centerOf(beacon);
        begin(scene, util, 9, "gaia_pylon_ritual", "Forming the Gaia Guardian Arena", pylons[0],
            "Four Gaia Pylons at the corners are mandatory for the Gaia Guardian ritual");

        Selection ritual = util.select().position(beacon);
        for (BlockPos pylon : pylons) {
            ritual = ritual.add(util.select().position(pylon));
            ritual = ritual.add(util.select().position(pylon.below()));
        }
        reveal(scene, ritual, focus);
        for (BlockPos pylon : pylons) {
            trail(scene, util.vector().centerOf(pylon), focus.add(0, 1, 0), 12,
                0.75F, 0.25F, 0.8F);
        }
        finish(scene, select(scene, util, pylons),
            "Each Pylon sits four blocks away on both horizontal axes from the central Beacon");
    }

    private static void begin(SceneBuilder scene, SceneBuildingUtil util, int size, String id,
                              String title, BlockPos subject, String description) {
        Vec3 focus = util.vector().centerOf(subject);
        if (size >= 7) {
            focus = util.vector().centerOf(util.grid().at(size / 2, 1, size / 2));
        }
        scene.title(id, title);
        scene.configureBasePlate(0, 0, size);
        if (size >= 9) {
            scene.scaleSceneView(0.72F);
        }
        scene.showBasePlate();
        pin(scene, focus);
        scene.idle(15);
        scene.world().showSection(util.select().position(subject), Direction.DOWN);
        pin(scene, focus);
        scene.idle(10);
        scene.overlay().showOutlineWithText(util.select().position(subject), 65, description)
            .placeNearTarget().attachKeyFrame();
        scene.idle(80);
    }

    private static void reveal(SceneBuilder scene, Selection selection, Vec3 focus) {
        scene.world().showSection(selection, Direction.DOWN);
        pin(scene, focus);
        scene.idle(20);
    }

    private static void finish(SceneBuilder scene, Selection target, String text) {
        scene.overlay().showOutlineWithText(target, 65, text)
            .placeNearTarget().colored(PonderPalette.OUTPUT).attachKeyFrame();
        scene.idle(70);
        scene.markAsFinished();
    }

    private static void fastBurst(SceneBuilder scene, Vec3 from, Vec3 to) {
        trail(scene, from, to, 12, 0.3F, 0.9F, 0.35F);
    }

    private static void trail(SceneBuilder scene, Vec3 from, Vec3 to, int ticks,
                              float red, float green, float blue) {
        var emitter = scene.effects().simpleParticleEmitter(
            SparkleParticleData.sparkle(0.8F, red, green, blue, 6), Vec3.ZERO);
        for (int tick = 1; tick <= ticks; tick++) {
            scene.effects().emitParticles(from.lerp(to, tick / (double) ticks), emitter, 2, 1);
            scene.idle(1);
        }
    }

    private static void sparkle(SceneBuilder scene, Vec3 at, int count,
                                float red, float green, float blue) {
        scene.effects().emitParticles(at, scene.effects().simpleParticleEmitter(
            SparkleParticleData.sparkle(0.9F, red, green, blue, 6), Vec3.ZERO), count, 2);
    }

    private static Selection select(SceneBuilder scene, SceneBuildingUtil util, BlockPos[] positions) {
        Selection selection = util.select().position(positions[0]);
        for (int index = 1; index < positions.length; index++) {
            selection = selection.add(util.select().position(positions[index]));
        }
        return selection;
    }

    private static void pin(SceneBuilder scene, Vec3 focus) {
        scene.addInstruction(s -> s.setFocusPoint(focus));
    }
}
