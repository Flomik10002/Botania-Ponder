package dev.flomik.botania_ponder.ponder.scenes;

import dev.flomik.ponderlib.api.ParticleEmitter;
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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import vazkii.botania.api.mana.spark.SparkUpgradeType;
import vazkii.botania.api.state.BotaniaStateProperties;
import vazkii.botania.client.fx.SparkleParticleData;
import vazkii.botania.common.block.block_entity.EyeOfTheAncientsBlockEntity;
import vazkii.botania.common.block.block_entity.LifeImbuerBlockEntity;
import vazkii.botania.common.block.block_entity.SparkTinkererBlockEntity;
import vazkii.botania.common.block.block_entity.mana.BellowsBlockEntity;
import vazkii.botania.common.block.block_entity.mana.ManaPoolBlockEntity;
import vazkii.botania.common.block.block_entity.mana.ManaPrismBlockEntity;
import vazkii.botania.common.block.block_entity.mana.PowerGeneratorBlockEntity;
import vazkii.botania.common.entity.ManaSparkEntity;

import java.util.Objects;

/** Small, focused demonstrations for Botania's utility and Mana-control blocks. */
public final class UtilityBlockScenes {

    private UtilityBlockScenes() {
    }

    public static void enderOverseer(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos eye = util.grid().at(2, 1, 2);
        BlockPos lamp = util.grid().at(2, 1, 3);
        Vec3 eyeCenter = util.vector().centerOf(eye);
        begin(scene, util, "ender_overseer_gaze", "Detecting a Player's Gaze", eye,
            "The Ender Overseer produces a redstone signal while a player looks directly at it");

        reveal(scene, util.select().position(lamp));
        Vec3 gaze = util.vector().centerOf(util.grid().at(0, 2, 0));
        scene.overlay().showBigLine(PonderPalette.INPUT, gaze, eyeCenter, 55);
        scene.overlay().showText(55, "The line of sight must hit the Overseer without a block in the way")
            .pointAt(eyeCenter).placeNearTarget().attachKeyFrame();
        scene.idle(70);

        scene.world().modifyBlock(eye,
            state -> state.setValue(BlockStateProperties.POWERED, true), false);
        scene.world().modifyBlock(lamp,
            state -> state.setValue(BlockStateProperties.LIT, true), false);
        scene.effects().indicateRedstone(eye);
        scene.idle(35);
        scene.world().modifyBlock(eye,
            state -> state.setValue(BlockStateProperties.POWERED, false), false);
        scene.world().modifyBlock(lamp,
            state -> state.setValue(BlockStateProperties.LIT, false), false);
        finish(scene, util.select().position(eye),
            "Looking away removes the signal again; a Carved Pumpkin prevents detection");
    }

    public static void eyeOfTheAncients(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos eye = util.grid().at(2, 1, 3);
        BlockPos comparator = util.grid().at(2, 1, 2);
        BlockPos lamp = util.grid().at(2, 1, 1);
        begin(scene, util, "eye_of_the_ancients_count", "Counting Nearby Animals", eye,
            "The Eye of the Ancients measures how many animals are within six blocks");

        Selection output = util.select().position(comparator).add(util.select().position(lamp));
        reveal(scene, output);
        Vec3[] positions = {
            util.vector().topOf(util.grid().at(1, 0, 3)),
            util.vector().topOf(util.grid().at(3, 0, 3)),
            util.vector().topOf(util.grid().at(1, 0, 1)),
            util.vector().topOf(util.grid().at(3, 0, 1))
        };
        for (Vec3 position : positions) {
            scene.world().createEntity(level -> {
                Cow cow = new Cow(EntityType.COW, level);
                cow.setNoAi(true);
                cow.moveTo(position.x, position.y, position.z, -90, 0);
                return cow;
            });
            scene.idle(8);
        }
        scene.world().modifyBlockEntity(eye, EyeOfTheAncientsBlockEntity.class, be -> be.entities = 4);
        scene.world().modifyBlock(comparator,
            state -> state.setValue(BlockStateProperties.POWERED, true), false);
        scene.world().modifyBlock(lamp,
            state -> state.setValue(BlockStateProperties.LIT, true), false);
        scene.effects().indicateRedstone(comparator);
        scene.overlay().showText(55, "A Comparator reads one less than the animal count, up to signal strength 15")
            .pointAt(util.vector().centerOf(comparator)).placeNearTarget()
            .colored(PonderPalette.OUTPUT).attachKeyFrame();
        scene.idle(70);
        finish(scene, util.select().position(eye),
            "Use the analog output to control farms according to their current population");
    }

    public static void manaFluxfield(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos generator = util.grid().at(2, 1, 3);
        BlockPos spreader = util.grid().at(2, 1, 0);
        Vec3 generatorCenter = util.vector().centerOf(generator);
        Vec3 spreaderCenter = util.vector().centerOf(spreader);
        begin(scene, util, "mana_fluxfield_conversion", "Converting Mana into Energy", generator,
            "The Mana Fluxfield accepts Mana Bursts and converts their Mana into Forge Energy");

        reveal(scene, util.select().position(spreader));
        ManaSpreaderScenes.aimSpreader(scene, spreader, generator);
        scene.overlay().showText(55, "Aim a Mana Spreader at the Fluxfield to charge its internal energy buffer")
            .pointAt(spreaderCenter).placeNearTarget().attachKeyFrame();
        scene.idle(70);
        ManaSpreaderScenes.burstTrail(scene, spreaderCenter, generatorCenter);
        scene.world().modifyBlockEntity(generator, PowerGeneratorBlockEntity.class,
            be -> be.receiveMana(128));
        greenSparkles(scene, generatorCenter, 18);
        scene.effects().indicateSuccess(generator);
        scene.idle(20);
        finish(scene, util.select().position(generator),
            "On Forge, each Mana becomes 10 FE which is pushed into adjacent energy receivers");
    }

    public static void lifeImbuer(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos spawner = util.grid().at(2, 1, 3);
        BlockPos imbuer = spawner.above();
        BlockPos spreader = util.grid().at(2, 1, 0);
        Vec3 imbuerCenter = util.vector().centerOf(imbuer);
        begin(scene, util, "life_imbuer_spawning", "Empowering a Monster Spawner", imbuer,
            "A Life Imbuer directly above a Monster Spawner lets it work without a nearby player");

        Selection support = util.select().position(spawner).add(util.select().position(spreader));
        reveal(scene, support);
        ManaSpreaderScenes.aimSpreader(scene, spreader, imbuer);
        ManaSpreaderScenes.burstTrail(scene, util.vector().centerOf(spreader), imbuerCenter);
        scene.world().modifyBlockEntity(imbuer, LifeImbuerBlockEntity.class, be -> be.receiveMana(120));
        greenSparkles(scene, imbuerCenter, 16);
        scene.overlay().showText(55, "Stored Mana substitutes for the normal nearby-player requirement")
            .pointAt(imbuerCenter).placeNearTarget().attachKeyFrame();
        scene.idle(70);

        Vec3 spawnAt = util.vector().topOf(util.grid().at(3, 0, 3));
        scene.world().createEntity(level -> {
            Zombie zombie = new Zombie(EntityType.ZOMBIE, level);
            zombie.setNoAi(true);
            zombie.moveTo(spawnAt.x, spawnAt.y, spawnAt.z, -90, 0);
            return zombie;
        });
        scene.effects().emitParticles(spawnAt.add(0, 0.7, 0),
            scene.effects().simpleParticleEmitter(ParticleTypes.PORTAL, Vec3.ZERO), 18, 3);
        scene.idle(25);
        finish(scene, util.select().position(imbuer),
            "Each forced spawner check consumes 6 Mana from the Life Imbuer");
    }

    public static void manaPrism(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos prism = util.grid().at(2, 1, 2);
        BlockPos spreader = util.grid().at(2, 1, 0);
        BlockPos pool = util.grid().at(2, 1, 4);
        Vec3 prismCenter = util.vector().centerOf(prism);
        ItemStack lens = botania("lens_speed");
        begin(scene, util, "mana_prism_lens", "Changing a Burst in Flight", prism,
            "A Mana Prism applies its installed lens to every Mana Burst that passes through it");

        Selection path = util.select().position(spreader).add(util.select().position(pool));
        reveal(scene, path);
        ManaSpreaderScenes.aimSpreader(scene, spreader, prism);
        scene.overlay().showControls(util.vector().topOf(prism), Pointing.DOWN, 30)
            .withItem(lens).rightClick();
        scene.world().modifyBlockEntity(prism, ManaPrismBlockEntity.class,
            be -> be.getItemHandler().setItem(0, lens.copy()));
        scene.world().modifyBlock(prism,
            state -> state.setValue(BotaniaStateProperties.HAS_LENS, true), false);
        scene.idle(35);
        scene.overlay().showText(55, "The Prism replaces the burst's current lens effect with the installed one")
            .pointAt(prismCenter).placeNearTarget().attachKeyFrame();
        scene.idle(70);

        ManaSpreaderScenes.burstTrail(scene, util.vector().centerOf(spreader), prismCenter);
        fastTrail(scene, prismCenter, util.vector().centerOf(pool), 14);
        scene.world().modifyBlockEntity(pool, ManaPoolBlockEntity.class,
            be -> be.receiveMana(ManaPoolBlockEntity.MAX_MANA / 3));
        scene.effects().indicateSuccess(pool);
        scene.idle(20);
        finish(scene, util.select().position(prism),
            "The Speed Lens accelerates the outgoing burst while preserving its remaining Mana");
    }

    public static void sparkTinkerer(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos tinkerer = util.grid().at(2, 1, 2);
        BlockPos pool = util.grid().at(3, 1, 2);
        ItemStack replacement = botania("spark_upgrade_recessive");
        ItemStack previous = botania("spark_upgrade_dominant");
        Vec3 sparkPos = util.vector().topOf(pool).add(0, 0.2, 0);
        begin(scene, util, "spark_tinkerer_swap", "Automating Spark Augments", tinkerer,
            "The Spark Tinkerer swaps an augment with one Spark on a horizontally adjacent block");

        reveal(scene, util.select().position(pool));
        ElementLink<EntityElement> spark = scene.world().createEntity(level -> {
            ManaSparkEntity entity = new ManaSparkEntity(level);
            entity.setPos(sparkPos.x, sparkPos.y, sparkPos.z);
            entity.setUpgrade(SparkUpgradeType.DOMINANT);
            return entity;
        });
        scene.overlay().showControls(util.vector().topOf(tinkerer), Pointing.DOWN, 30)
            .withItem(replacement).rightClick();
        scene.world().modifyBlockEntity(tinkerer, SparkTinkererBlockEntity.class,
            be -> be.getItemHandler().setItem(0, replacement.copy()));
        scene.idle(35);
        scene.overlay().showText(55, "A rising redstone edge performs one swap with a neighboring Spark")
            .pointAt(util.vector().centerOf(tinkerer)).placeNearTarget().attachKeyFrame();
        scene.idle(70);

        scene.world().modifyBlock(tinkerer,
            state -> state.setValue(BlockStateProperties.POWERED, true), false);
        scene.world().modifyEntity(spark,
            entity -> ((ManaSparkEntity) entity).setUpgrade(SparkUpgradeType.RECESSIVE));
        scene.world().modifyBlockEntity(tinkerer, SparkTinkererBlockEntity.class,
            be -> be.getItemHandler().setItem(0, previous.copy()));
        scene.effects().indicateRedstone(tinkerer);
        greenSparkles(scene, sparkPos, 14);
        scene.idle(25);
        finish(scene, util.select().position(tinkerer),
            "The removed Dominant Augment returns to the Tinkerer for the next pulse");
    }

    public static void bellows(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos bellows = util.grid().at(2, 1, 2);
        BlockPos furnace = util.grid().at(2, 1, 3);
        Vec3 bellowsCenter = util.vector().centerOf(bellows);
        begin(scene, util, "bellows_furnace", "Accelerating a Furnace", bellows,
            "Bellows facing a burning Furnace advance its cooking progress when compressed");

        reveal(scene, util.select().position(furnace));
        scene.world().modifyBlockEntity(furnace, FurnaceBlockEntity.class, be -> {
            be.setItem(0, new ItemStack(Items.IRON_ORE, 3));
            be.setItem(1, new ItemStack(Items.COAL));
        });
        scene.world().modifyBlock(furnace,
            state -> state.setValue(BlockStateProperties.LIT, true), false);
        scene.overlay().showControls(util.vector().topOf(bellows), Pointing.DOWN, 30).rightClick();
        scene.overlay().showText(55, "Right-click the Bellows to compress it once")
            .pointAt(bellowsCenter).placeNearTarget().attachKeyFrame();
        scene.idle(70);

        for (int pulse = 0; pulse < 3; pulse++) {
            scene.world().modifyBlockEntity(bellows, BellowsBlockEntity.class, be -> {
                be.active = true;
                be.movePos = 0;
                be.moving = 0;
            });
            scene.effects().emitParticles(util.vector().centerOf(furnace),
                scene.effects().simpleParticleEmitter(ParticleTypes.SMOKE, Vec3.ZERO), 4, 2);
            scene.idle(30);
        }
        scene.world().modifyBlockEntity(furnace, FurnaceBlockEntity.class, be -> {
            be.setItem(0, new ItemStack(Items.IRON_ORE, 2));
            be.setItem(2, new ItemStack(Items.IRON_INGOT));
        });
        scene.effects().indicateSuccess(furnace);
        finish(scene, util.select().position(bellows),
            "While compressing, the Bellows advances cooking repeatedly in 20-tick steps");
    }

    public static void tinyPlanet(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos planet = util.grid().at(2, 1, 2);
        BlockPos spreader = util.grid().at(2, 1, 0);
        BlockPos pool = util.grid().at(4, 1, 2);
        Vec3 center = util.vector().centerOf(planet);
        begin(scene, util, "tiny_planet_orbit", "Bending Mana Burst Paths", planet,
            "The Tiny Planet curves nearby Mana Bursts around itself instead of letting them fly straight");

        Selection path = util.select().position(spreader).add(util.select().position(pool));
        reveal(scene, path);
        ManaSpreaderScenes.aimSpreader(scene, spreader, planet);
        scene.overlay().showText(55, "Place the Tiny Planet near a burst route to pull the trajectory sideways")
            .pointAt(center).placeNearTarget().attachKeyFrame();
        scene.idle(70);

        orbitTrail(scene, center, 2.0, -Math.PI / 2, 0, 36);
        scene.world().modifyBlockEntity(pool, ManaPoolBlockEntity.class,
            be -> be.receiveMana(ManaPoolBlockEntity.MAX_MANA / 4));
        scene.effects().indicateSuccess(pool);
        scene.idle(20);
        finish(scene, util.select().position(planet),
            "The curved burst reaches a receiver that was not on the Spreader's original straight line");
    }

    private static void begin(SceneBuilder scene, SceneBuildingUtil util, String id, String title,
                              BlockPos subject, String description) {
        scene.title(id, title);
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        focus(scene, util.vector().centerOf(subject));
        scene.idle(15);
        scene.world().showSection(util.select().position(subject), Direction.DOWN);
        scene.idle(10);
        scene.overlay().showOutlineWithText(util.select().position(subject), 65, description)
            .placeNearTarget().attachKeyFrame();
        scene.idle(80);
    }

    private static void reveal(SceneBuilder scene, Selection selection) {
        scene.world().showSection(selection, Direction.DOWN);
        scene.idle(20);
    }

    private static void finish(SceneBuilder scene, Selection target, String text) {
        scene.overlay().showOutlineWithText(target, 65, text)
            .placeNearTarget().colored(PonderPalette.OUTPUT).attachKeyFrame();
        scene.idle(70);
        scene.markAsFinished();
    }

    private static void fastTrail(SceneBuilder scene, Vec3 from, Vec3 to, int ticks) {
        ParticleEmitter emitter = greenEmitter(scene, 0.8F);
        for (int tick = 1; tick <= ticks; tick++) {
            scene.effects().emitParticles(from.lerp(to, tick / (double) ticks), emitter, 2, 1);
            scene.idle(1);
        }
    }

    private static void orbitTrail(SceneBuilder scene, Vec3 center, double radius,
                                   double startAngle, double endAngle, int ticks) {
        ParticleEmitter emitter = greenEmitter(scene, 0.65F);
        for (int tick = 0; tick <= ticks; tick++) {
            double angle = startAngle + (endAngle - startAngle) * tick / ticks;
            Vec3 point = center.add(Math.cos(angle) * radius, 0, Math.sin(angle) * radius);
            scene.effects().emitParticles(point, emitter, 2, 1);
            scene.idle(1);
        }
    }

    private static void greenSparkles(SceneBuilder scene, Vec3 at, int count) {
        scene.effects().emitParticles(at, greenEmitter(scene, 0.9F), count, 2);
    }

    private static ParticleEmitter greenEmitter(SceneBuilder scene, float size) {
        SparkleParticleData data = SparkleParticleData.sparkle(size, 0.25F, 0.65F, 0.35F, 6);
        return scene.effects().simpleParticleEmitter(data, Vec3.ZERO);
    }

    private static ItemStack botania(String path) {
        Item item = Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(
            ResourceLocation.fromNamespaceAndPath("botania", path)));
        return new ItemStack(item);
    }

    private static void focus(SceneBuilder scene, Vec3 point) {
        scene.addInstruction(s -> s.setFocusPoint(point));
    }
}
