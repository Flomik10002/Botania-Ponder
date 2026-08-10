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
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.phys.Vec3;
import vazkii.botania.api.block_entity.GeneratingFlowerBlockEntity;
import vazkii.botania.client.fx.SparkleParticleData;
import vazkii.botania.common.block.BotaniaBlocks;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/** Focused, one-function scenes for common Generating Flora. */
public final class GeneratingFlowerScenes {

    private static final int FLUID_SOURCE_PREVIEW_TICKS = 40;

    private GeneratingFlowerScenes() {
    }

    public static void hydroangeas(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos flowerPos = util.grid().at(2, 1, 2);
        BlockPos waterPos = util.grid().at(2, 1, 3);
        Vec3 flowerCenter = util.vector().centerOf(flowerPos);
        begin(scene, util, "hydroangeas_generation", "Generating Mana with Hydroangeas",
            flowerPos, "Hydroangeas generates Mana by drinking nearby source blocks of water");

        scene.world().showSection(util.select().position(waterPos), Direction.DOWN);
        focusOn(scene, flowerCenter);
        // Leave the source unobstructed for over a second before explaining its placement.
        scene.idle(FLUID_SOURCE_PREVIEW_TICKS);
        scene.overlay().showOutlineWithText(util.select().position(waterPos), 55,
                "Place a water source in any adjacent space")
            .placeNearTarget()
            .colored(PonderPalette.INPUT)
            .attachKeyFrame();
        // 70, not 60: text windows fade in/out over 5 ticks each beyond their declared duration,
        // so a 55-tick text needs at least 65 before absorbFluid()'s own text opens.
        scene.idle(70);

        absorbFluid(scene, util, waterPos, flowerCenter,
            "The flower detects the source and slowly draws the water into itself", 0x536FBD);
        generateMana(scene, flowerPos, flowerCenter, 13, 24, 0x536FBD);
        scene.effects().indicateSuccess(flowerPos);
        scene.idle(8);
        finish(scene, util.select().position(flowerPos),
            "The source is consumed and converted into Mana over a short time");
    }

    public static void thermalily(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos flowerPos = util.grid().at(2, 1, 2);
        BlockPos lavaPos = util.grid().at(2, 1, 3);
        Vec3 flowerCenter = util.vector().centerOf(flowerPos);
        begin(scene, util, "thermalily_generation", "Generating Mana with a Thermalily",
            flowerPos, "The Thermalily converts nearby lava source blocks into Mana");

        scene.world().showSection(util.select().position(lavaPos), Direction.DOWN);
        focusOn(scene, flowerCenter);
        // Leave the source unobstructed for over a second before explaining its placement.
        scene.idle(FLUID_SOURCE_PREVIEW_TICKS);
        scene.overlay().showOutlineWithText(util.select().position(lavaPos), 55,
                "Place a lava source beside the flower")
            .placeNearTarget()
            .colored(PonderPalette.INPUT)
            .attachKeyFrame();
        // 70, not 60: text windows fade in/out over 5 ticks each beyond their declared duration,
        // so a 55-tick text needs at least 65 before absorbFluid()'s own text opens.
        scene.idle(70);

        absorbFluid(scene, util, lavaPos, flowerCenter,
            "The Thermalily absorbs the source before it begins generating Mana", 0xC55A32);
        generateMana(scene, flowerPos, flowerCenter, 750, 42, 0xC55A32);
        scene.effects().indicateSuccess(flowerPos);
        scene.idle(8);
        finish(scene, util.select().position(flowerPos),
            "A lava source produces a large amount of Mana, followed by a long cooldown");
    }

    public static void rosaArcana(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos flowerPos = util.grid().at(2, 1, 2);
        Vec3 flowerCenter = util.vector().centerOf(flowerPos);
        begin(scene, util, "rosa_arcana_generation", "Generating Mana with Rosa Arcana",
            flowerPos, "Rosa Arcana converts nearby experience into Mana");

        Vec3 orbStart = flowerCenter.add(-1.25, 1.1, 0);
        ElementLink<EntityElement> orb = scene.world().createEntity(level -> {
            return new ExperienceOrb(level, orbStart.x, orbStart.y, orbStart.z, 7);
        });
        scene.overlay().showText(48, "Nearby Experience Orbs are consumed one point at a time")
            .pointAt(orbStart)
            .placeNearTarget()
            .colored(PonderPalette.INPUT)
            .attachKeyFrame();
        scene.idle(36);
        scene.idle(4);
        scene.world().modifyEntity(orb, Entity::discard);
        scene.effects().emitParticles(flowerCenter,
            scene.effects().particleEmitterWithinBlockSpace(ParticleTypes.ENCHANT, Vec3.ZERO), 6, 2);
        generateMana(scene, flowerPos, flowerCenter, 350, 30, 0xC978C3);
        scene.effects().indicateSuccess(flowerPos);
        scene.idle(8);
        finish(scene, util.select().position(flowerPos),
            "Each experience point becomes 50 Mana in the flower's internal buffer");
    }

    public static void gourmaryllis(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos flowerPos = util.grid().at(2, 1, 2);
        Vec3 flowerCenter = util.vector().centerOf(flowerPos);
        begin(scene, util, "gourmaryllis_generation", "Generating Mana with Gourmaryllis",
            flowerPos, "Gourmaryllis digests edible items to generate Mana");

        ItemStack food = new ItemStack(Items.COOKED_BEEF);
        Vec3 controlPoint = util.vector().blockSurface(flowerPos, Direction.EAST).add(0, 0.15, 0);
        scene.overlay().showControls(controlPoint, Pointing.LEFT, 20)
            .withItem(food)
            .drop();
        scene.idle(23);
        Vec3 target = flowerCenter.add(-0.72, -0.35, 0);
        Vec3 start = target.add(0, 1.8, 0);
        ElementLink<EntityElement> foodEntity = scene.world().createItemEntity(start, Vec3.ZERO, food.copy());
        scene.idle(24);
        scene.idle(4);
        scene.world().modifyEntity(foodEntity, Entity::discard);

        ItemParticleOption crumbs = new ItemParticleOption(ParticleTypes.ITEM, food);
        scene.effects().emitParticles(flowerCenter,
            scene.effects().particleEmitterWithinBlockSpace(crumbs, Vec3.ZERO), 5, 3);
        generateMana(scene, flowerPos, flowerCenter, 4480, 40, 0xA4A631);
        scene.effects().indicateSuccess(flowerPos);
        scene.idle(8);
        finish(scene, util.select().position(flowerPos),
            "More nutritious food produces more Mana while it is digested");
    }

    public static void munchdew(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos flowerPos = util.grid().at(2, 1, 1);
        Vec3 flowerCenter = util.vector().centerOf(flowerPos);
        Selection leaves = util.select().fromTo(1, 1, 3, 3, 1, 3);
        begin(scene, util, "munchdew_generation", "Generating Mana with Munchdew",
            flowerPos, "Munchdew searches a large area for exposed leaf blocks");

        for (int x = 1; x <= 3; x++) {
            scene.world().showSection(util.select().position(util.grid().at(x, 1, 3)), Direction.DOWN);
            focusOn(scene, flowerCenter);
            scene.idle(4);
        }
        scene.idle(8);
        scene.overlay().showOutlineWithText(leaves, 60,
                "Exposed leaves are consumed one at a time")
            .placeNearTarget()
            .colored(PonderPalette.INPUT)
            .attachKeyFrame();
        scene.idle(65);

        for (int x = 1; x <= 3; x++) {
            BlockPos leafPos = util.grid().at(x, 1, 3);
            scene.world().destroyBlock(leafPos);
            flowToFlower(scene, util.vector().centerOf(leafPos), flowerCenter, 0x79A84B, 12);
            generateMana(scene, flowerPos, flowerCenter, 160, 8, 0x79A84B);
            scene.idle(5);
        }
        scene.effects().indicateSuccess(flowerPos);
        scene.idle(8);
        finish(scene, util.select().position(flowerPos),
            "Each leaf yields 160 Mana before the Munchdew pauses to digest");
    }

    public static void kekimurus(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos flowerPos = util.grid().at(2, 1, 1);
        BlockPos cakePos = util.grid().at(2, 1, 3);
        Vec3 flowerCenter = util.vector().centerOf(flowerPos);
        begin(scene, util, "kekimurus_generation", "Generating Mana with Kekimurus",
            flowerPos, "Kekimurus searches its surroundings for placed cakes");

        scene.world().showSection(util.select().position(cakePos), Direction.DOWN);
        focusOn(scene, flowerCenter);
        scene.idle(10);
        scene.overlay().showOutlineWithText(util.select().position(cakePos), 60,
                "Every 80 ticks, the flower eats one slice of a nearby cake")
            .placeNearTarget()
            .colored(PonderPalette.INPUT)
            .attachKeyFrame();
        scene.idle(65);

        scene.world().modifyBlock(cakePos,
            state -> state.setValue(CakeBlock.BITES, state.getValue(CakeBlock.BITES) + 1), true);
        ItemStack cake = new ItemStack(Items.CAKE);
        scene.effects().emitParticles(util.vector().centerOf(cakePos),
            scene.effects().particleEmitterWithinBlockSpace(
                new ItemParticleOption(ParticleTypes.ITEM, cake), Vec3.ZERO), 8, 3);
        flowToFlower(scene, util.vector().centerOf(cakePos), flowerCenter, 0x935D28, 22);
        generateMana(scene, flowerPos, flowerCenter, 1800, 28, 0x935D28);
        scene.effects().indicateSuccess(flowerPos);
        scene.idle(8);
        finish(scene, util.select().position(flowerPos),
            "One slice produces 1800 Mana; the cake remains for the next cycle");
    }

    public static void spectrolus(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos flowerPos = util.grid().at(2, 1, 2);
        Vec3 flowerCenter = util.vector().centerOf(flowerPos);
        begin(scene, util, "spectrolus_generation", "Generating Mana with Spectrolus",
            flowerPos, "Spectrolus consumes wool in the sixteen-color dye order");

        scene.overlay().showText(70, "The cycle starts with white, then orange, then magenta")
            .pointAt(flowerCenter.add(0, 0.25, 0))
            .placeNearTarget()
            .colored(PonderPalette.INPUT)
            .attachKeyFrame();
        scene.idle(75);

        ItemStack[] wool = {
            new ItemStack(Items.WHITE_WOOL),
            new ItemStack(Items.ORANGE_WOOL),
            new ItemStack(Items.MAGENTA_WOOL)
        };
        int[] colors = { 0xE9ECEC, 0xD87F33, 0xB24CD8 };
        for (int i = 0; i < wool.length; i++) {
            consumeItem(scene, flowerCenter.add(-1.25, 0.9, 0), flowerCenter.add(0, 0.1, 0),
                wool[i], 22, colors[i]);
            generateMana(scene, flowerPos, flowerCenter, 1200, 16, colors[i]);
            scene.idle(6);
        }
        scene.effects().indicateSuccess(flowerPos);
        scene.idle(8);
        finish(scene, util.select().position(flowerPos),
            "Each correct wool block yields 1200 Mana and advances the expected color");
    }

    public static void entropinnyum(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos flowerPos = util.grid().at(2, 1, 2);
        Vec3 flowerCenter = util.vector().centerOf(flowerPos);
        Vec3 tntPos = flowerCenter.add(-1.4, 0.15, 0);
        begin(scene, util, "entropinnyum_generation", "Generating Mana with Entropinnyum",
            flowerPos, "Entropinnyum watches for primed TNT within its range");

        scene.overlay().showText(60, "A normally ignited TNT must reach the final tick of its fuse")
            .pointAt(tntPos.add(0, 0.4, 0))
            .placeNearTarget()
            .colored(PonderPalette.INPUT)
            .attachKeyFrame();
        scene.idle(65);
        ElementLink<EntityElement> tnt = scene.world().createEntity(level -> {
            PrimedTnt entity = new PrimedTnt(level, tntPos.x, tntPos.y, tntPos.z, null);
            entity.setFuse(42);
            entity.setDeltaMovement(0.04, 0, 0);
            return entity;
        });
        scene.idle(32);
        scene.idle(5);
        scene.world().modifyEntity(tnt, Entity::discard);
        scene.effects().emitParticles(flowerCenter.add(-0.35, 0.25, 0),
            scene.effects().simpleParticleEmitter(ParticleTypes.EXPLOSION, Vec3.ZERO), 2, 1);
        generateMana(scene, flowerPos, flowerCenter, 6500, 38, 0xCB3D3D);
        scene.effects().indicateSuccess(flowerPos);
        scene.idle(8);
        finish(scene, util.select().position(flowerPos),
            "The explosion is cancelled and fills the empty flower with 6500 Mana");
    }

    public static void narslimmus(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos flowerPos = util.grid().at(2, 1, 2);
        Vec3 flowerCenter = util.vector().centerOf(flowerPos);
        Vec3 slimeStart = util.vector().topOf(util.grid().at(1, 0, 2));
        begin(scene, util, "narslimmus_generation", "Generating Mana with Narslimmus",
            flowerPos, "Narslimmus consumes naturally spawned slimes from slime chunks");

        ElementLink<EntityElement> slime = scene.world().createEntity(level -> {
            Slime entity = new Slime(EntityType.SLIME, level);
            entity.setSize(2, true);
            entity.setNoAi(true);
            entity.moveTo(slimeStart.x, slimeStart.y, slimeStart.z, -90, 0);
            return entity;
        });
        scene.overlay().showText(58, "Larger natural slimes produce exponentially more Mana")
            .pointAt(slimeStart.add(0, 0.6, 0))
            .placeNearTarget()
            .colored(PonderPalette.INPUT)
            .attachKeyFrame();
        for (int hop = 0; hop < 3; hop++) {
            scene.world().modifyEntity(slime, entity -> entity.setDeltaMovement(0.12, 0.25, 0));
            scene.idle(14);
        }
        scene.idle(18);
        scene.world().modifyEntity(slime, Entity::discard);
        scene.effects().emitParticles(flowerCenter.add(-0.2, 0.25, 0),
            scene.effects().particleEmitterWithinBlockSpace(ParticleTypes.ITEM_SLIME, Vec3.ZERO), 16, 3);
        generateMana(scene, flowerPos, flowerCenter, 4800, 36, 0x71A873);
        scene.effects().indicateSuccess(flowerPos);
        scene.idle(8);
        finish(scene, util.select().position(flowerPos),
            "This size-two slime produces 4800 Mana without splitting into smaller slimes");
    }

    public static void rafflowsia(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos flowerPos = util.grid().at(2, 1, 1);
        BlockPos foodPos = util.grid().at(2, 1, 3);
        Vec3 flowerCenter = util.vector().centerOf(flowerPos);
        begin(scene, util, "rafflowsia_generation", "Generating Mana with Rafflowsia",
            flowerPos, "Rafflowsia consumes other special flowers to generate Mana");

        scene.world().showSection(util.select().position(foodPos), Direction.DOWN);
        focusOn(scene, flowerCenter);
        scene.idle(10);
        scene.overlay().showOutlineWithText(util.select().position(foodPos), 65,
                "Feeding different flower types builds a much more valuable streak")
            .placeNearTarget()
            .colored(PonderPalette.INPUT)
            .attachKeyFrame();
        scene.idle(70);

        scene.world().destroyBlock(foodPos);
        flowToFlower(scene, util.vector().centerOf(foodPos), flowerCenter, 0x6E4A82, 28);
        generateMana(scene, flowerPos, flowerCenter, 2000, 32, 0x6E4A82);
        scene.effects().indicateSuccess(flowerPos);
        scene.idle(8);
        finish(scene, util.select().position(flowerPos),
            "Repeating the same flower lowers the reward; variety keeps increasing it");
    }

    public static void dandelifeon(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos flowerPos = util.grid().at(2, 1, 2);
        Vec3 flowerCenter = util.vector().centerOf(flowerPos);
        begin(scene, util, "dandelifeon_generation", "Generating Mana with Dandelifeon",
            flowerPos, "A powered Dandelifeon runs Conway's Game of Life with Cellular Blocks");

        List<BlockPos> first = List.of(
            util.grid().at(1, 1, 1), util.grid().at(2, 1, 1), util.grid().at(3, 1, 1),
            util.grid().at(3, 1, 2), util.grid().at(2, 1, 3));
        showCells(scene, util, flowerCenter, first);
        scene.overlay().showText(65, "Every ten ticks, the cells survive, appear, or die by the usual Life rules")
            .pointAt(util.vector().centerOf(util.grid().at(2, 1, 1)))
            .placeNearTarget()
            .colored(PonderPalette.INPUT)
            .attachKeyFrame();
        scene.idle(70);

        List<BlockPos> second = List.of(
            util.grid().at(2, 1, 1), util.grid().at(3, 1, 1),
            util.grid().at(3, 1, 2), util.grid().at(3, 1, 3));
        replaceCells(scene, first, second);
        scene.idle(20);
        List<BlockPos> third = List.of(
            util.grid().at(2, 1, 1), util.grid().at(3, 1, 2),
            util.grid().at(3, 1, 3), util.grid().at(2, 1, 3));
        replaceCells(scene, second, third);
        scene.idle(20);

        scene.overlay().showText(60, "When a mature cell reaches the central area, the board is consumed")
            .pointAt(flowerCenter.add(0, 0.2, 0))
            .placeNearTarget()
            .colored(PonderPalette.OUTPUT)
            .attachKeyFrame();
        for (BlockPos cell : third) {
            scene.world().destroyBlock(cell);
            flowToFlower(scene, util.vector().centerOf(cell), flowerCenter, 0x8B487C, 8);
        }
        generateMana(scene, flowerPos, flowerCenter, 240, 28, 0x8B487C);
        scene.idle(4);
        scene.effects().indicateSuccess(flowerPos);
        // 16, not 8: keeps this text's window (60-tick text, fading out 5 ticks past that) from
        // overlapping finish()'s.
        scene.idle(16);
        finish(scene, util.select().position(flowerPos),
            "Mana gained equals the consumed cell's generation multiplied by 60");
    }

    public static void shulkMeNot(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos flowerPos = util.grid().at(2, 1, 2);
        Vec3 flowerCenter = util.vector().centerOf(flowerPos);
        Vec3 shulkerPos = util.vector().topOf(util.grid().at(1, 0, 2));
        Vec3 zombieStart = util.vector().topOf(util.grid().at(3, 0, 2));
        begin(scene, util, "shulk_me_not_generation", "Generating Mana with Shulk Me Not",
            flowerPos, "Shulk Me Not observes nearby Shulkers fighting other hostile mobs");

        AtomicReference<Shulker> shulkerRef = new AtomicReference<>();
        AtomicReference<Zombie> zombieRef = new AtomicReference<>();
        ElementLink<EntityElement> shulker = scene.world().createEntity(level -> {
            Shulker entity = new Shulker(EntityType.SHULKER, level);
            entity.setNoAi(true);
            entity.moveTo(shulkerPos.x, shulkerPos.y, shulkerPos.z, -90, 0);
            shulkerRef.set(entity);
            return entity;
        });
        ElementLink<EntityElement> zombie = scene.world().createEntity(level -> {
            Zombie entity = new Zombie(level);
            entity.setNoAi(true);
            entity.moveTo(zombieStart.x, zombieStart.y, zombieStart.z, 90, 0);
            zombieRef.set(entity);
            return entity;
        });
        scene.overlay().showText(65, "A Shulker projectile must hit another hostile mob and give it Levitation")
            .pointAt(zombieStart.add(0, 1, 0))
            .placeNearTarget()
            .colored(PonderPalette.INPUT)
            .attachKeyFrame();
        scene.idle(12);
        ElementLink<EntityElement> bullet = scene.world().createEntity(level ->
            new ShulkerBullet(level, shulkerRef.get(), zombieRef.get(), null));
        scene.idle(42);
        scene.idle(4);

        scene.world().modifyEntity(bullet, Entity::discard);
        scene.world().modifyEntity(shulker, Entity::discard);
        scene.world().modifyEntity(zombie, Entity::discard);
        scene.effects().emitParticles(shulkerPos.add(0, 0.5, 0),
            scene.effects().simpleParticleEmitter(ParticleTypes.EXPLOSION, Vec3.ZERO), 5, 2);
        scene.effects().emitParticles(zombieStart.add(0, 1.1, 0),
            scene.effects().simpleParticleEmitter(ParticleTypes.EXPLOSION, Vec3.ZERO), 5, 2);
        flowToFlower(scene, shulkerPos.add(0, 0.5, 0), flowerCenter, 0x76568A, 18);
        generateMana(scene, flowerPos, flowerCenter, 75000, 50, 0x76568A);
        scene.effects().indicateSuccess(flowerPos);
        scene.idle(8);
        finish(scene, util.select().position(flowerPos),
            "Both mobs are consumed and the empty flower receives 75000 Mana");
    }

    private static void showCells(SceneBuilder scene, SceneBuildingUtil util, Vec3 focus,
                                  List<BlockPos> cells) {
        for (BlockPos cell : cells) {
            scene.world().setBlock(cell, BotaniaBlocks.cellBlock.defaultBlockState());
            scene.world().showSection(util.select().position(cell), Direction.DOWN);
            focusOn(scene, focus);
            scene.idle(5);
        }
        scene.idle(8);
    }

    private static void replaceCells(SceneBuilder scene, List<BlockPos> oldCells, List<BlockPos> newCells) {
        for (BlockPos cell : oldCells) {
            if (!newCells.contains(cell)) {
                scene.world().modifyBlock(cell, ignored -> Blocks.AIR.defaultBlockState(), true);
            }
        }
        for (BlockPos cell : newCells) {
            if (!oldCells.contains(cell)) {
                scene.world().setBlock(cell, BotaniaBlocks.cellBlock.defaultBlockState());
            }
        }
    }

    private static void consumeItem(SceneBuilder scene, Vec3 start, Vec3 target,
                                    ItemStack stack, int duration, int color) {
        ElementLink<EntityElement> item = scene.world().createItemEntity(start, Vec3.ZERO, stack.copy());
        scene.idle(duration);
        scene.world().modifyEntity(item, Entity::discard);
        scene.effects().emitParticles(target,
            scene.effects().particleEmitterWithinBlockSpace(
                new ItemParticleOption(ParticleTypes.ITEM, stack), Vec3.ZERO), 7, 2);
        manaSparkle(scene, target, color, 4);
    }

    private static void begin(SceneBuilder scene, SceneBuildingUtil util, String id, String title,
                              BlockPos flowerPos, String description) {
        Vec3 center = util.vector().centerOf(flowerPos);
        scene.title(id, title);
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        focusOn(scene, center);
        scene.idle(15);
        scene.world().showSection(util.select().position(flowerPos), Direction.DOWN);
        focusOn(scene, center);
        scene.idle(10);
        scene.overlay().showOutlineWithText(util.select().position(flowerPos), 65, description)
            .placeNearTarget()
            .attachKeyFrame();
        // 80, not 70: text windows fade in/out over 5 ticks each beyond their declared duration
        // (TextInstruction/FadeInOutInstruction), so a 65-tick text is on screen for 75 ticks - 80
        // leaves a small margin past that instead of landing the scene's first text on the boundary.
        scene.idle(80);
    }

    private static void finish(SceneBuilder scene, Selection flower, String text) {
        scene.overlay().showOutlineWithText(flower, 65, text)
            .placeNearTarget()
            .colored(PonderPalette.OUTPUT)
            .attachKeyFrame();
        scene.idle(70);
        scene.markAsFinished();
    }

    private static void generateMana(SceneBuilder scene, BlockPos flowerPos, Vec3 flowerCenter,
                                     int totalMana, int duration, int color) {
        int amountPerTick = totalMana / duration;
        for (int tick = 1; tick <= duration; tick++) {
            int amount = tick == duration
                ? totalMana - amountPerTick * (duration - 1)
                : amountPerTick;
            scene.world().modifyBlockEntity(flowerPos, GeneratingFlowerBlockEntity.class,
                flower -> flower.addMana(amount));
            if (tick % 3 == 0) {
                manaSparkle(scene, flowerCenter.add(0, 0.15, 0), color, 1);
            }
            scene.idle(1);
        }
    }

    private static void flowToFlower(SceneBuilder scene, Vec3 from, Vec3 to, int color, int duration) {
        Vec3 path = to.subtract(from);
        for (int tick = 1; tick <= duration; tick++) {
            Vec3 at = from.add(path.scale(tick / (double) duration));
            manaSparkle(scene, at, color, 2);
            scene.idle(1);
        }
    }

    private static void absorbFluid(SceneBuilder scene, SceneBuildingUtil util, BlockPos fluidPos,
                                    Vec3 flowerCenter, String text, int color) {
        Selection fluid = util.select().position(fluidPos);
        Vec3 fluidCenter = util.vector().centerOf(fluidPos);
        scene.overlay().showOutlineWithText(fluid, 70, text)
            .placeNearTarget()
            .colored(PonderPalette.INPUT)
            .attachKeyFrame();
        flowToFlower(scene, fluidCenter, flowerCenter, color, 44);
        scene.idle(12);
        scene.world().hideSection(fluid, Direction.UP);
        scene.idle(14);
    }

    private static void manaSparkle(SceneBuilder scene, Vec3 at, int color, int count) {
        float r = (color >> 16 & 0xFF) / 255F;
        float g = (color >> 8 & 0xFF) / 255F;
        float b = (color & 0xFF) / 255F;
        SparkleParticleData data = SparkleParticleData.sparkle(0.8F, r, g, b, 6);
        ParticleEmitter emitter = scene.effects().simpleParticleEmitter(data, Vec3.ZERO);
        scene.effects().emitParticles(at, emitter, count, 1);
    }

    private static void focusOn(SceneBuilder scene, Vec3 subject) {
        scene.addInstruction(s -> s.setFocusPoint(subject));
    }
}
