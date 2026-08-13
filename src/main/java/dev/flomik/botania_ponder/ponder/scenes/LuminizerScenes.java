package dev.flomik.botania_ponder.ponder.scenes;

import dev.flomik.ponderlib.api.PonderPalette;
import dev.flomik.ponderlib.api.element.ElementLink;
import dev.flomik.ponderlib.api.element.EntityElement;
import dev.flomik.ponderlib.api.scene.CollisionMode;
import dev.flomik.ponderlib.api.scene.Easing;
import dev.flomik.ponderlib.api.scene.SceneBuilder;
import dev.flomik.ponderlib.api.scene.SceneBuildingUtil;
import dev.flomik.ponderlib.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import vazkii.botania.client.fx.SparkleParticleData;

/** One-function scenes for Spectral Rails and every Luminizer variant. */
public final class LuminizerScenes {
    private LuminizerScenes() {}

    public static void spectralRail(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos rail = util.grid().at(1, 1, 2), wall = util.grid().at(3, 1, 2);
        Vec3 focus = util.vector().centerOf(util.grid().at(2, 1, 2));
        begin(scene, util, "spectral_rail_launch", "Launching a Spectral Minecart", rail,
            "A minecart leaving a Spectral Rail briefly floats and can pass through solid blocks");
        reveal(scene, util.select().position(wall), focus);
        Vec3 start = util.vector().topOf(rail);
        ElementLink<EntityElement> cart = scene.world().createEntity(level -> {
            Minecart minecart = new Minecart(level, start.x, start.y, start.z);
            minecart.setNoGravity(true);
            minecart.setYRot(-90);
            return minecart;
        });
        scene.world().moveEntity(cart, util.vector().centerOf(wall).add(1.5, .5, 0), 45,
            Easing.QUAD_OUT, CollisionMode.IGNORE);
        trail(scene, util.vector().topOf(rail), util.vector().centerOf(wall).add(1.5, .5, 0), 30);
        finish(scene, util.select().position(rail), "The effect ends when the cart lands on another rail or hits Dreamwood");
    }

    public static void luminizer(SceneBuilder scene, SceneBuildingUtil util) {
        route(scene, util, "luminizer_transport", "Travelling Through a Luminizer Network",
            "Right-click a bound Luminizer to ride its light path to the linked destination", false, false);
    }

    public static void fork(SceneBuilder scene, SceneBuildingUtil util) {
        route(scene, util, "fork_luminizer_branch", "Choosing a Route at a Fork",
            "A Fork Luminizer lets the travelling entity choose between two outgoing paths", true, false);
    }

    public static void toggle(SceneBuilder scene, SceneBuildingUtil util) {
        route(scene, util, "toggle_luminizer_signal", "Switching a Route with Redstone",
            "A Toggle Luminizer changes which outgoing path is used according to redstone power", true, true);
    }

    public static void detector(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos start = util.grid().at(1, 2, 2), detector = util.grid().at(3, 2, 2), lamp = util.grid().at(3, 1, 3);
        Vec3 focus = util.vector().centerOf(util.grid().at(2, 2, 2));
        begin(scene, util, "detector_luminizer_output", "Detecting a Traveller", detector,
            "A Detector Luminizer emits a short redstone pulse when an entity passes through it");
        Selection route = util.select().position(start).add(util.select().position(lamp)); reveal(scene, route, focus);
        ElementLink<EntityElement> item = scene.world().createItemEntity(util.vector().centerOf(start), Vec3.ZERO, new ItemStack(Items.ENDER_PEARL));
        scene.world().moveEntity(item, util.vector().centerOf(detector), 30, Easing.QUAD_IN_OUT, CollisionMode.IGNORE); scene.idle(32);
        scene.world().modifyBlock(detector, s -> s.setValue(BlockStateProperties.POWERED, true), false);
        scene.world().modifyBlock(lamp, s -> s.setValue(BlockStateProperties.LIT, true), false);
        scene.effects().indicateRedstone(detector); scene.idle(10);
        scene.world().modifyBlock(detector, s -> s.setValue(BlockStateProperties.POWERED, false), false);
        scene.world().modifyBlock(lamp, s -> s.setValue(BlockStateProperties.LIT, false), false);
        finish(scene, util.select().position(detector), "The pulse can trigger machinery at an intermediate network point");
    }

    public static void launcher(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos relay = util.grid().at(2, 2, 2), launcher = util.grid().at(2, 1, 4);
        Vec3 focus = util.vector().centerOf(util.grid().at(2, 2, 3));
        begin(scene, util, "launcher_luminizer_exit", "Launching Out of the Network", launcher,
            "A Luminizer Launcher converts arrival momentum into a physical launch out of the network");
        reveal(scene, util.select().position(relay), focus);
        ElementLink<EntityElement> item = scene.world().createItemEntity(util.vector().centerOf(relay), Vec3.ZERO, new ItemStack(Items.ENDER_PEARL));
        Vec3 exit = util.vector().centerOf(launcher).add(0, 2.3, 1.2);
        scene.world().moveEntity(item, util.vector().centerOf(launcher).add(0, .5, 0), 25, Easing.QUAD_IN, CollisionMode.IGNORE);
        scene.idle(27); scene.world().moveEntity(item, exit, 28, Easing.QUAD_OUT, CollisionMode.IGNORE);
        trail(scene, util.vector().centerOf(launcher), exit, 20);
        finish(scene, util.select().position(launcher), "Place it at the endpoint when the traveller should continue through open air");
    }

    private static void route(SceneBuilder scene, SceneBuildingUtil util, String id, String title,
                              String description, boolean branch, boolean powered) {
        BlockPos start = util.grid().at(1, 2, 2), first = util.grid().at(3, 2, 2);
        BlockPos second = util.grid().at(3, 2, branch ? 4 : 2), lever = util.grid().at(2, 1, 2);
        Vec3 focus = util.vector().centerOf(util.grid().at(2, 2, 2));
        begin(scene, util, id, title, start, description);
        Selection rest = util.select().position(first);
        if (branch) rest = rest.add(util.select().position(second));
        if (powered) rest = rest.add(util.select().position(lever));
        reveal(scene, rest, focus);
        if (powered) { scene.world().modifyBlock(lever, s -> s.setValue(BlockStateProperties.POWERED, true), false); scene.effects().indicateRedstone(lever); }
        ElementLink<EntityElement> item = scene.world().createItemEntity(util.vector().centerOf(start), Vec3.ZERO, new ItemStack(Items.ENDER_PEARL));
        Vec3 destination = util.vector().centerOf(branch ? second : first);
        scene.world().moveEntity(item, destination, 42, Easing.QUAD_IN_OUT, CollisionMode.IGNORE);
        trail(scene, util.vector().centerOf(start), destination, 32);
        finish(scene, util.select().position(start), branch ? "The selected branch determines the next bound Luminizer" : "Items and living entities can both use the bound route");
    }

    private static void begin(SceneBuilder scene, SceneBuildingUtil util, String id, String title, BlockPos subject, String text) {
        Vec3 focus = util.vector().centerOf(util.grid().at(2, 2, 2)); scene.title(id, title); scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate(); pin(scene, focus); scene.idle(15); scene.world().showSection(util.select().position(subject), Direction.DOWN); pin(scene, focus);
        scene.idle(10); scene.overlay().showOutlineWithText(util.select().position(subject), 65, text).placeNearTarget().attachKeyFrame(); scene.idle(80);
    }
    private static void reveal(SceneBuilder scene, Selection selection, Vec3 focus) { scene.world().showSection(selection, Direction.DOWN); pin(scene, focus); scene.idle(20); }
    private static void finish(SceneBuilder scene, Selection target, String text) { scene.overlay().showOutlineWithText(target, 65, text).placeNearTarget().colored(PonderPalette.OUTPUT).attachKeyFrame(); scene.idle(70); scene.markAsFinished(); }
    private static void trail(SceneBuilder scene, Vec3 from, Vec3 to, int ticks) { var emitter = scene.effects().simpleParticleEmitter(SparkleParticleData.sparkle(.75F, .45F, .75F, .9F, 6), Vec3.ZERO); for (int i=1;i<=ticks;i++){scene.effects().emitParticles(from.lerp(to,i/(double)ticks),emitter,2,1);scene.idle(1);} }
    private static void pin(SceneBuilder scene, Vec3 focus) { scene.addInstruction(s -> s.setFocusPoint(focus)); }
}
