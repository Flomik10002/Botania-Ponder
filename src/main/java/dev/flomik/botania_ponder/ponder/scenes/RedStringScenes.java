package dev.flomik.botania_ponder.ponder.scenes;

import dev.flomik.ponderlib.api.Pointing;
import dev.flomik.ponderlib.api.PonderPalette;
import dev.flomik.ponderlib.api.element.ElementLink;
import dev.flomik.ponderlib.api.element.EntityElement;
import dev.flomik.ponderlib.api.scene.SceneBuilder;
import dev.flomik.ponderlib.api.scene.SceneBuildingUtil;
import dev.flomik.ponderlib.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Objects;

/** Focused scenes for all six Red String blocks. */
public final class RedStringScenes {

    private static final BlockPos SOURCE = new BlockPos(3, 1, 1);
    private static final BlockPos TARGET = new BlockPos(3, 1, 5);

    private RedStringScenes() {
    }

    public static void container(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos hopperPos = util.grid().at(2, 1, 1);
        begin(scene, util, "red_string_container_transfer", "Accessing a Remote Inventory",
            "The Red Stringed Container exposes the inventory of the block it finds", TARGET);

        scene.world().showSection(util.select().position(hopperPos), Direction.DOWN);
        focusOn(scene, midpoint(util));
        scene.idle(10);
        scene.overlay().showControls(util.vector().topOf(hopperPos), Pointing.DOWN, 35)
            .withItem(new ItemStack(Items.GOLD_INGOT));
        scene.overlay().showText(45, "Automation inserts the item into the Red Stringed Container")
            .pointAt(util.vector().centerOf(SOURCE)).placeNearTarget()
            .colored(PonderPalette.INPUT).attachKeyFrame();
        scene.idle(50);

        pulse(scene, util, 24);
        scene.world().modifyBlockEntity(TARGET, ChestBlockEntity.class,
            chest -> chest.setItem(0, new ItemStack(Items.GOLD_INGOT)));
        scene.effects().indicateSuccess(TARGET);
        finish(scene, util.select().position(TARGET),
            "The item enters the bound Chest from the same side used at the Container");
    }

    public static void dispenser(SceneBuilder scene, SceneBuildingUtil util) {
        begin(scene, util, "red_string_dispenser_trigger", "Triggering a Remote Dispenser",
            "The Red Stringed Dispenser binds only to a Dispenser or Dropper", TARGET);

        scene.world().modifyBlockEntity(TARGET, DispenserBlockEntity.class,
            dispenser -> dispenser.setItem(0, new ItemStack(Items.SNOWBALL)));
        scene.overlay().showText(45, "Apply a redstone pulse to the Red Stringed Dispenser")
            .pointAt(util.vector().centerOf(SOURCE)).placeNearTarget()
            .colored(PonderPalette.INPUT).attachKeyFrame();
        scene.idle(50);

        scene.world().modifyBlock(SOURCE,
            state -> state.setValue(BlockStateProperties.POWERED, true), false);
        scene.effects().indicateRedstone(SOURCE);
        pulse(scene, util, 16);
        Vec3 muzzle = util.vector().centerOf(TARGET).add(0, 0, 0.65);
        ElementLink<EntityElement> snowball = scene.world().createEntity(level -> {
            Snowball projectile = new Snowball(level, muzzle.x, muzzle.y, muzzle.z);
            projectile.setDeltaMovement(0, 0, 0.22);
            return projectile;
        });
        scene.idle(24);
        scene.removeElement(snowball);
        scene.world().modifyBlock(SOURCE,
            state -> state.setValue(BlockStateProperties.POWERED, false), false);
        finish(scene, util.select().position(TARGET),
            "The redstone pulse makes the distant Dispenser fire its own contents");
    }

    public static void nutrifier(SceneBuilder scene, SceneBuildingUtil util) {
        begin(scene, util, "red_string_nutrifier_growth", "Fertilizing at a Distance",
            "The Red Stringed Nutrifier binds to the first block that accepts Bone Meal", TARGET);

        scene.overlay().showControls(util.vector().topOf(SOURCE), Pointing.DOWN, 35)
            .withItem(new ItemStack(Items.BONE_MEAL)).rightClick();
        scene.overlay().showText(45, "Use Bone Meal on the Nutrifier instead of on the crop")
            .pointAt(util.vector().centerOf(SOURCE)).placeNearTarget()
            .colored(PonderPalette.INPUT).attachKeyFrame();
        scene.idle(50);

        pulse(scene, util, 20);
        scene.world().modifyBlock(TARGET,
            state -> state.setValue(BlockStateProperties.AGE_7, 7), false);
        scene.effects().emitParticles(util.vector().centerOf(TARGET).add(0, 0.4, 0),
            scene.effects().particleEmitterWithinBlockSpace(ParticleTypes.HAPPY_VILLAGER, Vec3.ZERO), 12, 3);
        scene.effects().indicateSuccess(TARGET);
        finish(scene, util.select().position(TARGET),
            "The Bone Meal effect is applied to the bound crop four blocks away");
    }

    public static void comparator(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos comparatorPos = util.grid().at(2, 1, 1);
        BlockPos lampPos = util.grid().at(1, 1, 1);
        begin(scene, util, "red_string_comparator_signal", "Reading a Remote Comparator Signal",
            "The Red Stringed Comparator binds to blocks with an analog comparator output", TARGET);

        scene.world().showSection(util.select().position(comparatorPos), Direction.DOWN);
        focusOn(scene, midpoint(util));
        scene.idle(5);
        scene.world().showSection(util.select().position(lampPos), Direction.DOWN);
        focusOn(scene, midpoint(util));
        scene.idle(10);
        scene.overlay().showText(50, "A normal Comparator reads from the Red Stringed block")
            .pointAt(util.vector().centerOf(comparatorPos)).placeNearTarget()
            .colored(PonderPalette.INPUT).attachKeyFrame();
        scene.idle(55);

        scene.world().modifyBlockEntity(TARGET, ChestBlockEntity.class, chest -> {
            for (int slot = 0; slot < chest.getContainerSize(); slot++) {
                chest.setItem(slot, new ItemStack(Items.COBBLESTONE, 48));
            }
        });
        pulse(scene, util, 20);
        scene.world().modifyBlock(comparatorPos,
            state -> state.setValue(BlockStateProperties.POWERED, true), false);
        scene.world().modifyBlock(lampPos,
            state -> state.setValue(BlockStateProperties.LIT, true), false);
        scene.effects().indicateRedstone(comparatorPos);
        finish(scene, util.select().position(comparatorPos).add(util.select().position(lampPos)),
            "The output strength matches the signal that the distant Chest would produce");
    }

    public static void relay(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos flowerPos = util.grid().at(3, 2, 1);
        List<BlockPos> growth = List.of(
            util.grid().at(2, 1, 5), util.grid().at(4, 1, 5),
            util.grid().at(3, 1, 4), util.grid().at(3, 1, 6));
        begin(scene, util, "red_string_relay_flower", "Moving a Flower's Effective Position",
            "The Red Stringed Spoofer binds to an ordinary flower or mushroom", TARGET);

        scene.world().showSection(util.select().position(flowerPos), Direction.DOWN);
        focusOn(scene, midpoint(util));
        scene.idle(10);
        scene.overlay().showOutlineWithText(util.select().position(flowerPos), 55,
                "The Botania flower on the Spoofer becomes inert at its real position")
            .placeNearTarget().colored(PonderPalette.INPUT).attachKeyFrame();
        scene.idle(60);

        pulse(scene, util, 22);
        List<Block> flowers = List.of(
            botaniaBlock("white_mystical_flower"), botaniaBlock("orange_mystical_flower"),
            botaniaBlock("yellow_mystical_flower"), botaniaBlock("lime_mystical_flower"));
        for (int index = 0; index < growth.size(); index++) {
            BlockPos pos = growth.get(index);
            scene.world().setBlock(pos, flowers.get(index).defaultBlockState());
            scene.effects().emitSparks(util.vector().centerOf(pos), 0x79AD87, 5);
            scene.idle(7);
        }
        finish(scene, util.select().position(TARGET),
            "The bound plant becomes the flower's effective position and its effect happens there");
    }

    public static void interceptor(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos lampPos = util.grid().at(2, 1, 1);
        begin(scene, util, "red_string_interceptor_pulse", "Detecting Remote Interaction",
            "The Red Stringed Interceptor binds to a complex block such as a Chest", TARGET);

        scene.world().showSection(util.select().position(lampPos), Direction.DOWN);
        focusOn(scene, midpoint(util));
        scene.idle(10);
        scene.overlay().showControls(util.vector().topOf(TARGET), Pointing.DOWN, 35).rightClick();
        scene.overlay().showText(45, "Right-click the bound block at its actual position")
            .pointAt(util.vector().centerOf(TARGET)).placeNearTarget()
            .colored(PonderPalette.INPUT).attachKeyFrame();
        scene.idle(50);

        pulse(scene, util, 16);
        scene.world().modifyBlock(SOURCE,
            state -> state.setValue(BlockStateProperties.POWERED, true), false);
        scene.world().modifyBlock(lampPos,
            state -> state.setValue(BlockStateProperties.LIT, true), false);
        scene.effects().indicateRedstone(SOURCE);
        scene.idle(12);
        scene.world().modifyBlock(SOURCE,
            state -> state.setValue(BlockStateProperties.POWERED, false), false);
        scene.world().modifyBlock(lampPos,
            state -> state.setValue(BlockStateProperties.LIT, false), false);
        finish(scene, util.select().position(SOURCE),
            "The Interceptor emits a short redstone pulse for every player interaction");
    }

    private static void begin(SceneBuilder scene, SceneBuildingUtil util, String id, String title,
                              String description, BlockPos targetPos) {
        Vec3 focus = midpoint(util);
        scene.title(id, title);
        scene.configureBasePlate(0, 0, 7);
        scene.showBasePlate();
        focusOn(scene, focus);
        scene.idle(15);
        scene.world().showSection(util.select().position(SOURCE), Direction.DOWN);
        focusOn(scene, focus);
        scene.idle(10);
        scene.overlay().showOutlineWithText(util.select().position(SOURCE), 55, description)
            .placeNearTarget().attachKeyFrame();
        // 65, not 60: text windows fade in/out over 5 ticks each beyond their declared duration
        // (TextInstruction/FadeInOutInstruction), so a 55-tick text is on screen for 65 ticks.
        scene.idle(65);

        scene.world().showSection(util.select().position(targetPos), Direction.DOWN);
        focusOn(scene, focus);
        scene.idle(10);
        scene.overlay().showControls(util.vector().topOf(SOURCE), Pointing.DOWN, 50)
            .withItem(botaniaItem("twig_wand"));
        scene.overlay().showBigLine(PonderPalette.RED,
            util.vector().centerOf(SOURCE), util.vector().centerOf(targetPos), 50);
        scene.overlay().showText(50,
                "The red face finds the nearest compatible block in a straight line, up to eight blocks away")
            .pointAt(util.vector().centerOf(targetPos)).placeNearTarget().attachKeyFrame();
        // 65, not 55: text windows fade in/out over 5 ticks each beyond their declared duration,
        // so a 50-tick text needs at least 60 before the next scene's own text opens.
        scene.idle(65);
    }

    private static void pulse(SceneBuilder scene, SceneBuildingUtil util, int duration) {
        scene.overlay().showBigLine(PonderPalette.RED,
            util.vector().centerOf(SOURCE), util.vector().centerOf(TARGET), duration);
        scene.effects().emitSparks(util.vector().centerOf(SOURCE), 0xB34A4A, 6);
        scene.idle(duration);
    }

    private static void finish(SceneBuilder scene, Selection target, String text) {
        scene.idle(8);
        scene.overlay().showOutlineWithText(target, 65, text)
            .placeNearTarget().colored(PonderPalette.OUTPUT).attachKeyFrame();
        scene.idle(70);
        scene.markAsFinished();
    }

    private static Vec3 midpoint(SceneBuildingUtil util) {
        return new Vec3(3.5, 1.5, 3.5);
    }

    private static void focusOn(SceneBuilder scene, Vec3 focus) {
        scene.addInstruction(s -> s.setFocusPoint(focus));
    }

    private static ItemStack botaniaItem(String path) {
        Item item = Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(
            ResourceLocation.fromNamespaceAndPath("botania", path)));
        return new ItemStack(item);
    }

    private static Block botaniaBlock(String path) {
        return Objects.requireNonNull(ForgeRegistries.BLOCKS.getValue(
            ResourceLocation.fromNamespaceAndPath("botania", path)));
    }
}
