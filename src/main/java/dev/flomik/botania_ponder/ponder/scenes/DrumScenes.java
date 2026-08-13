package dev.flomik.botania_ponder.ponder.scenes;

import dev.flomik.ponderlib.api.PonderPalette;
import dev.flomik.ponderlib.api.element.ElementLink;
import dev.flomik.ponderlib.api.element.EntityElement;
import dev.flomik.ponderlib.api.scene.SceneBuilder;
import dev.flomik.ponderlib.api.scene.SceneBuildingUtil;
import dev.flomik.ponderlib.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import vazkii.botania.common.handler.BotaniaSounds;

import java.util.List;

/** One-function demonstrations for the complete Drum family. */
public final class DrumScenes {

    private static final BlockPos DRUM = new BlockPos(3, 1, 3);
    private static final BlockPos SPREADER = new BlockPos(3, 1, 0);
    private static final Vec3 FOCUS = new Vec3(3.5, 1.7, 3.5);

    private DrumScenes() {
    }

    public static void wild(SceneBuilder scene, SceneBuildingUtil util) {
        List<BlockPos> plants = List.of(
            util.grid().at(1, 1, 5), util.grid().at(2, 1, 5),
            util.grid().at(3, 1, 5), util.grid().at(4, 1, 5),
            util.grid().at(5, 1, 5));
        Selection plantSelection = positions(util, plants);
        begin(scene, util, "drum_wild_harvest", "Clearing Vegetation", DRUM,
            "A Mana Burst striking the Drum of the Wild destroys nearby vegetation");
        revealTrigger(scene, util, plantSelection,
            "The Drum does not store Mana; a Pulse Spreader can trigger it with one burst");

        hitDrum(scene, util);
        for (BlockPos plant : plants) {
            scene.world().destroyBlock(plant);
            scene.idle(2);
        }
        scene.effects().indicateSuccess(DRUM);
        finish(scene, plantSelection,
            "Crops, flowers, grass, and other Horn-harvestable vegetation are broken in range");
    }

    public static void canopy(SceneBuilder scene, SceneBuildingUtil util) {
        List<BlockPos> leaves = List.of(
            util.grid().at(2, 3, 5), util.grid().at(3, 3, 5), util.grid().at(4, 3, 5),
            util.grid().at(2, 4, 5), util.grid().at(3, 4, 5), util.grid().at(4, 4, 5));
        Selection leafSelection = positions(util, leaves);
        Selection wholeTree = util.select().fromTo(2, 1, 5, 4, 4, 5);
        begin(scene, util, "drum_canopy_harvest", "Removing a Tree Canopy", DRUM,
            "The Drum of the Canopy is the block form of the Horn of the Canopy");
        revealTrigger(scene, util, wholeTree,
            "A Mana Burst triggers the Drum while the tree is inside its working range");

        hitDrum(scene, util);
        for (BlockPos leaf : leaves) {
            scene.world().destroyBlock(leaf);
            scene.idle(2);
        }
        scene.effects().indicateSuccess(DRUM);
        finish(scene, util.select().fromTo(3, 1, 5, 3, 2, 5),
            "Leaves are broken while the logs remain standing");
    }

    public static void gatheringShear(SceneBuilder scene, SceneBuildingUtil util) {
        Vec3 sheepPos = util.vector().topOf(util.grid().at(3, 0, 5));
        begin(scene, util, "drum_gathering_shear", "Shearing Nearby Animals", DRUM,
            "The Drum of the Gathering shears adult animals with grown fleece");
        revealTrigger(scene, util, util.select().position(SPREADER),
            "A single Mana Burst activates every valid animal in range");

        ElementLink<EntityElement> sheep = scene.world().createEntity(level -> {
            Sheep entity = new Sheep(EntityType.SHEEP, level);
            entity.setNoAi(true);
            entity.moveTo(sheepPos.x, sheepPos.y, sheepPos.z, 180, 0);
            return entity;
        });
        scene.idle(25);
        hitDrum(scene, util);
        scene.world().modifyEntity(sheep, entity -> ((Sheep) entity).setSheared(true));
        scene.effects().playSound(SoundEvents.SHEEP_SHEAR, 1, 1);
        scene.world().createItemEntity(sheepPos.add(0.25, 0.4, 0), Vec3.ZERO,
            new ItemStack(Items.WHITE_WOOL, 2));
        scene.effects().indicateSuccess(DRUM);
        finish(scene, util.select().position(DRUM),
            "Up to five ready shearable mobs are processed by one drumbeat");
    }

    public static void gatheringMilk(SceneBuilder scene, SceneBuildingUtil util) {
        Vec3 cowPos = util.vector().topOf(util.grid().at(3, 0, 5));
        begin(scene, util, "drum_gathering_milk", "Milking Nearby Animals", DRUM,
            "The Drum of the Gathering fills empty buckets touching an adult milkable animal");
        revealTrigger(scene, util, util.select().position(SPREADER),
            "Drop an Empty Bucket at the animal before the Mana Burst arrives");

        scene.world().createEntity(level -> {
            Cow entity = new Cow(EntityType.COW, level);
            entity.setNoAi(true);
            entity.moveTo(cowPos.x, cowPos.y, cowPos.z, 180, 0);
            return entity;
        });
        ElementLink<EntityElement> bucket = scene.world().createItemEntity(
            cowPos.add(0, 0.1, 0), Vec3.ZERO, new ItemStack(Items.BUCKET));
        scene.idle(25);
        hitDrum(scene, util);
        scene.world().modifyEntity(bucket, Entity::discard);
        scene.world().createItemEntity(cowPos.add(0, 0.25, 0), Vec3.ZERO,
            new ItemStack(Items.MILK_BUCKET));
        scene.effects().indicateSuccess(DRUM);
        finish(scene, util.select().position(DRUM),
            "Each nearby Empty Bucket is replaced with a Milk Bucket");
    }

    public static void gatheringEgg(SceneBuilder scene, SceneBuildingUtil util) {
        Vec3 chickenPos = util.vector().topOf(util.grid().at(3, 0, 5));
        begin(scene, util, "drum_gathering_egg", "Advancing Egg Laying", DRUM,
            "The Drum of the Gathering moves adult chickens closer to laying their next egg");
        revealTrigger(scene, util, util.select().position(SPREADER),
            "A chicken whose egg is already close can lay it immediately after the drumbeat");

        scene.world().createEntity(level -> {
            Chicken entity = new Chicken(EntityType.CHICKEN, level);
            entity.setNoAi(true);
            entity.eggTime = 100;
            entity.moveTo(chickenPos.x, chickenPos.y, chickenPos.z, 180, 0);
            return entity;
        });
        scene.idle(25);
        hitDrum(scene, util);
        scene.idle(8);
        scene.world().createItemEntity(chickenPos.add(0, 0.2, 0), Vec3.ZERO,
            new ItemStack(Items.EGG));
        scene.effects().playSound(SoundEvents.CHICKEN_EGG, 1, 1);
        scene.effects().indicateSuccess(DRUM);
        finish(scene, util.select().position(DRUM),
            "The drum shortens the remaining egg timer; it does not create an egg every time");
    }

    private static void begin(SceneBuilder scene, SceneBuildingUtil util, String id, String title,
                              BlockPos subject, String description) {
        scene.title(id, title);
        scene.configureBasePlate(0, 0, 7);
        scene.showBasePlate();
        focusOn(scene);
        scene.idle(15);
        scene.world().showSection(util.select().position(subject), Direction.DOWN);
        focusOn(scene);
        scene.idle(10);
        scene.overlay().showOutlineWithText(util.select().position(subject), 60, description)
            .placeNearTarget().attachKeyFrame();
        scene.idle(65);
    }

    private static void revealTrigger(SceneBuilder scene, SceneBuildingUtil util,
                                      Selection targets, String text) {
        scene.world().showSection(util.select().position(SPREADER).add(targets), Direction.DOWN);
        ManaSpreaderScenes.aimSpreader(scene, SPREADER, DRUM);
        focusOn(scene);
        scene.idle(25);
        scene.overlay().showText(60, text)
            .pointAt(util.vector().centerOf(SPREADER)).placeNearTarget()
            .colored(PonderPalette.INPUT).attachKeyFrame();
        scene.idle(65);
    }

    private static void hitDrum(SceneBuilder scene, SceneBuildingUtil util) {
        ManaSpreaderScenes.burstTrail(scene, util.vector().centerOf(SPREADER),
            util.vector().centerOf(DRUM));
        scene.effects().playSound(BotaniaSounds.drum, 1, 1);
        scene.effects().emitParticles(util.vector().topOf(DRUM).add(0, 0.2, 0),
            scene.effects().simpleParticleEmitter(ParticleTypes.NOTE, new Vec3(1D / 24D, 0, 0)),
            1, 1);
        scene.idle(4);
    }

    private static Selection positions(SceneBuildingUtil util, List<BlockPos> positions) {
        Selection result = util.select().position(positions.get(0));
        for (int i = 1; i < positions.size(); i++) {
            result = result.add(util.select().position(positions.get(i)));
        }
        return result;
    }

    private static void finish(SceneBuilder scene, Selection target, String text) {
        scene.idle(12);
        scene.overlay().showOutlineWithText(target, 65, text)
            .placeNearTarget().colored(PonderPalette.OUTPUT).attachKeyFrame();
        scene.idle(70);
        scene.markAsFinished();
    }

    private static void focusOn(SceneBuilder scene) {
        scene.addInstruction(s -> s.setFocusPoint(FOCUS));
    }
}
