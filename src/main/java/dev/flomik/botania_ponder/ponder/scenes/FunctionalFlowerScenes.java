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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import vazkii.botania.api.block_entity.FunctionalFlowerBlockEntity;

import java.util.List;
import java.util.Objects;

/** Focused, one-function scenes for common Functional Flora. */
public final class FunctionalFlowerScenes {

    private FunctionalFlowerScenes() {
    }

    public static void agricarnation(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos flowerPos = util.grid().at(2, 1, 1);
        Vec3 flowerCenter = util.vector().centerOf(flowerPos);
        Selection crops = util.select().fromTo(1, 1, 3, 3, 1, 3);
        begin(scene, util, "agricarnation_growth", "Accelerating Crops with Agricarnation",
            flowerPos, "Agricarnation spends Mana to accelerate nearby plant growth");

        scene.world().showSection(crops, Direction.DOWN);
        focusOn(scene, flowerCenter);
        scene.idle(10);
        scene.overlay().showOutlineWithText(crops, 60,
                "Crops inside the flower's range receive additional growth ticks")
            .placeNearTarget()
            .colored(PonderPalette.INPUT)
            .attachKeyFrame();
        scene.idle(65);

        addMana(scene, flowerPos, 105);
        for (int age = 1; age <= 7; age++) {
            int nextAge = age;
            scene.world().modifyBlocks(crops,
                state -> state.setValue(BlockStateProperties.AGE_7, nextAge), false);
            scene.effects().emitSparks(crops.getCenter(), 0x78A85B, 3);
            scene.world().modifyBlockEntity(flowerPos, FunctionalFlowerBlockEntity.class,
                flower -> flower.addMana(-15));
            scene.idle(5);
        }
        scene.effects().indicateSuccess(flowerPos);
        scene.idle(8);
        finish(scene, crops, "The crops mature faster while Agricarnation has Mana available");
    }

    public static void clayconia(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos flowerPos = util.grid().at(2, 1, 1);
        Vec3 flowerCenter = util.vector().centerOf(flowerPos);
        Selection sand = util.select().fromTo(1, 1, 3, 3, 1, 3);
        begin(scene, util, "clayconia_conversion", "Producing Clay with Clayconia",
            flowerPos, "Clayconia consumes Mana to break nearby Sand into Clay Balls");

        revealRow(scene, util, flowerCenter, 3);
        scene.overlay().showOutlineWithText(sand, 60, "Sand within range is selected one block at a time")
            .placeNearTarget()
            .colored(PonderPalette.INPUT)
            .attachKeyFrame();
        scene.idle(65);

        addMana(scene, flowerPos, 240);
        for (int x = 1; x <= 3; x++) {
            BlockPos sandPos = util.grid().at(x, 1, 3);
            scene.world().destroyBlock(sandPos);
            scene.world().createItemEntity(util.vector().centerOf(sandPos).add(0, 0.25, 0),
                Vec3.ZERO, new ItemStack(Items.CLAY_BALL, 4));
            scene.world().modifyBlockEntity(flowerPos, FunctionalFlowerBlockEntity.class,
                flower -> flower.addMana(-80));
            scene.effects().emitSparks(util.vector().centerOf(sandPos), 0x7B8792, 5);
            scene.idle(12);
        }
        scene.effects().indicateSuccess(flowerPos);
        scene.idle(8);
        finish(scene, sand, "Each Sand block costs Mana and drops four Clay Balls");
    }

    public static void orechid(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos flowerPos = util.grid().at(2, 1, 1);
        Vec3 flowerCenter = util.vector().centerOf(flowerPos);
        Selection stone = util.select().fromTo(1, 1, 3, 3, 1, 3);
        begin(scene, util, "orechid_transmutation", "Transmuting Stone with the Orechid",
            flowerPos, "The Orechid spends a large amount of Mana to transmute nearby Stone");

        revealRow(scene, util, flowerCenter, 3);
        scene.overlay().showOutlineWithText(stone, 60, "A random Stone block inside the working area is chosen")
            .placeNearTarget()
            .colored(PonderPalette.INPUT)
            .attachKeyFrame();
        scene.idle(65);

        addMana(scene, flowerPos, 17500);
        BlockPos chosen = util.grid().at(2, 1, 3);
        scene.overlay().showOutline(PonderPalette.OUTPUT, "orechid_target",
            util.select().position(chosen), 32);
        scene.idle(24);
        scene.world().modifyBlock(chosen, ignored -> Blocks.IRON_ORE.defaultBlockState(), true);
        scene.world().modifyBlockEntity(flowerPos, FunctionalFlowerBlockEntity.class,
            flower -> flower.addMana(-17500));
        scene.effects().indicateSuccess(chosen);
        scene.idle(10);
        finish(scene, util.select().position(chosen),
            "The selected Stone is replaced by an ore from the configured world pool");
    }

    public static void jadedAmaranthus(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos flowerPos = util.grid().at(2, 1, 2);
        Vec3 flowerCenter = util.vector().centerOf(flowerPos);
        begin(scene, util, "jaded_amaranthus_growth", "Growing Mystical Flowers",
            flowerPos, "Jaded Amaranthus spends Mana to grow Mystical Flowers on nearby grass");

        List<BlockPos> growthPositions = List.of(
            util.grid().at(1, 1, 1), util.grid().at(3, 1, 1), util.grid().at(2, 1, 3));
        List<Block> flowers = List.of(
            botaniaBlock("blue_mystical_flower"),
            botaniaBlock("red_mystical_flower"),
            botaniaBlock("yellow_mystical_flower"));
        scene.overlay().showText(66, "Empty grass positions in range can receive a random color")
            .pointAt(flowerCenter)
            .placeNearTarget()
            .colored(PonderPalette.INPUT)
            .attachKeyFrame();
        scene.idle(6);
        for (int i = 0; i < growthPositions.size(); i++) {
            BlockPos growthPos = growthPositions.get(i);
            addMana(scene, flowerPos, 100);
            scene.world().setBlock(growthPos, flowers.get(i).defaultBlockState());
            scene.world().showSection(util.select().position(growthPos), Direction.DOWN);
            focusOn(scene, flowerCenter);
            scene.world().modifyBlockEntity(flowerPos, FunctionalFlowerBlockEntity.class,
                flower -> flower.addMana(-100));
            scene.effects().emitSparks(util.vector().centerOf(growthPos), 0x95618D, 5);
            scene.idle(18);
        }
        scene.effects().indicateSuccess(flowerPos);
        scene.idle(8);
        Selection grown = util.select().position(growthPositions.get(0))
            .add(util.select().position(growthPositions.get(1)))
            .add(util.select().position(growthPositions.get(2)));
        finish(scene, grown, "Each operation creates one randomly colored Mystical Flower");
    }

    public static void hopperhock(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos flowerPos = util.grid().at(2, 1, 2);
        BlockPos chestPos = util.grid().at(2, 1, 3);
        Vec3 flowerCenter = util.vector().centerOf(flowerPos);
        begin(scene, util, "hopperhock_collection", "Collecting Items with Hopperhock",
            flowerPos, "Hopperhock collects loose items into adjacent inventories");

        scene.world().showSection(util.select().position(chestPos), Direction.DOWN);
        focusOn(scene, flowerCenter);
        scene.idle(10);
        scene.overlay().showOutlineWithText(util.select().position(chestPos), 55,
                "Place an inventory directly beside the flower")
            .placeNearTarget()
            .attachKeyFrame();
        scene.idle(60);

        ItemStack item = new ItemStack(Items.DIAMOND);
        Vec3 itemStart = flowerCenter.add(-1.5, 0.8, 0);
        ElementLink<EntityElement> looseItem = scene.world().createItemEntity(itemStart, Vec3.ZERO, item.copy());
        scene.overlay().showText(52, "A loose item inside the working range is detected")
            .pointAt(itemStart)
            .placeNearTarget()
            .colored(PonderPalette.INPUT)
            .attachKeyFrame();
        scene.idle(48);
        scene.idle(4);
        scene.world().modifyEntity(looseItem, Entity::discard);
        scene.world().modifyBlockEntity(chestPos, ChestBlockEntity.class,
            chest -> chest.setItem(0, item.copy()));
        scene.effects().indicateSuccess(chestPos);
        scene.idle(8);
        finish(scene, util.select().position(chestPos),
            "The item is inserted into the adjacent inventory automatically");
    }

    public static void bellethorn(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos flowerPos = util.grid().at(2, 1, 2);
        Vec3 flowerCenter = util.vector().centerOf(flowerPos);
        Vec3 zombieStart = util.vector().topOf(util.grid().at(1, 0, 2));
        begin(scene, util, "bellethorn_damage", "Damaging Mobs with Bellethorn",
            flowerPos, "Bellethorn spends Mana to damage nearby living creatures other than players");

        ElementLink<EntityElement> zombie = scene.world().createEntity(level -> {
            Zombie entity = new Zombie(level);
            entity.setNoAi(true);
            entity.moveTo(zombieStart.x, zombieStart.y, zombieStart.z, -90, 0);
            return entity;
        });
        scene.overlay().showText(58, "Every five ticks, valid creatures inside the working range are struck")
            .pointAt(zombieStart.add(0, 1, 0))
            .placeNearTarget()
            .colored(PonderPalette.INPUT)
            .attachKeyFrame();
        scene.idle(62);

        addMana(scene, flowerPos, 72);
        for (int hit = 0; hit < 3; hit++) {
            scene.world().modifyEntity(zombie, entity -> {
                Zombie mob = (Zombie) entity;
                mob.setHealth(mob.getHealth() - 4);
                mob.hurtTime = 10;
                mob.hurtDuration = 10;
            });
            scene.world().modifyBlockEntity(flowerPos, FunctionalFlowerBlockEntity.class,
                flower -> flower.addMana(-24));
            scene.effects().emitParticles(zombieStart.add(0, 1, 0),
                scene.effects().simpleParticleEmitter(ParticleTypes.DAMAGE_INDICATOR, Vec3.ZERO), 4, 2);
            scene.idle(12);
        }
        scene.effects().indicateSuccess(flowerPos);
        scene.idle(8);
        finish(scene, util.select().position(flowerPos),
            "Each target takes four magic damage and costs the flower 24 Mana");
    }

    public static void daffomill(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos flowerPos = util.grid().at(2, 1, 3);
        Vec3 flowerCenter = util.vector().centerOf(flowerPos);
        Vec3 itemStart = util.vector().centerOf(util.grid().at(2, 1, 2)).add(0, 0.35, 0);
        begin(scene, util, "daffomill_wind", "Moving Items with Daffomill",
            flowerPos, "Daffomill creates a long directional stream of wind for loose items");

        ItemStack item = new ItemStack(Items.IRON_INGOT);
        ElementLink<EntityElement> looseItem = scene.world().createItemEntity(itemStart, Vec3.ZERO, item);
        scene.overlay().showText(62, "A tiny amount of Mana keeps the wind active for twenty ticks")
            .pointAt(itemStart)
            .placeNearTarget()
            .colored(PonderPalette.INPUT)
            .attachKeyFrame();
        scene.idle(66);

        addMana(scene, flowerPos, 1);
        for (int z = 2; z >= 0; z--) {
            scene.effects().emitParticles(util.vector().centerOf(util.grid().at(2, 1, z)).add(0, 0.3, 0),
                scene.effects().simpleParticleEmitter(ParticleTypes.CLOUD, new Vec3(0, 0, -0.06)), 3, 2);
        }
        for (int tick = 0; tick < 20; tick++) {
            scene.world().modifyEntity(looseItem, entity -> entity.setDeltaMovement(
                entity.getDeltaMovement().add(0, 0, -0.05)));
            scene.idle(1);
        }
        scene.idle(32);
        scene.world().modifyBlockEntity(flowerPos, FunctionalFlowerBlockEntity.class,
            flower -> flower.addMana(-1));
        scene.effects().indicateSuccess(flowerPos);
        scene.idle(8);
        finish(scene, util.select().fromTo(2, 1, 0, 2, 1, 3),
            "Loose items are pushed along the flower's orientation for up to sixteen blocks");
    }

    public static void exoflame(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos flowerPos = util.grid().at(1, 1, 2);
        BlockPos furnacePos = util.grid().at(3, 1, 2);
        Vec3 flowerCenter = util.vector().centerOf(flowerPos);
        begin(scene, util, "exoflame_heating", "Heating Furnaces with Exoflame",
            flowerPos, "Exoflame spends Mana to fuel and accelerate nearby furnaces");

        scene.world().modifyBlockEntity(furnacePos, FurnaceBlockEntity.class,
            furnace -> furnace.setItem(0, new ItemStack(Items.IRON_ORE)));
        scene.world().showSection(util.select().position(furnacePos), Direction.DOWN);
        focusOn(scene, flowerCenter);
        scene.idle(12);
        scene.overlay().showOutlineWithText(util.select().position(furnacePos), 65,
                "A furnace with a valid input can run without placing fuel inside it")
            .placeNearTarget()
            .colored(PonderPalette.INPUT)
            .attachKeyFrame();
        scene.idle(70);

        addMana(scene, flowerPos, 300);
        scene.world().modifyBlock(furnacePos,
            state -> state.setValue(BlockStateProperties.LIT, true), false);
        scene.world().modifyBlockEntity(flowerPos, FunctionalFlowerBlockEntity.class,
            flower -> flower.addMana(-300));
        for (int tick = 0; tick < 48; tick++) {
            if (tick % 4 == 0) {
                scene.effects().emitParticles(util.vector().centerOf(furnacePos).add(0, 0.15, 0),
                    scene.effects().simpleParticleEmitter(ParticleTypes.FLAME, Vec3.ZERO), 2, 1);
                scene.effects().emitSparks(flowerCenter.add(0, 0.2, 0), 0x8A4D32, 1);
            }
            scene.idle(1);
        }
        scene.world().modifyBlockEntity(furnacePos, FurnaceBlockEntity.class, furnace -> {
            furnace.setItem(0, ItemStack.EMPTY);
            furnace.setItem(2, new ItemStack(Items.IRON_INGOT));
        });
        scene.world().modifyBlock(furnacePos,
            state -> state.setValue(BlockStateProperties.LIT, false), false);
        scene.effects().indicateSuccess(furnacePos);
        scene.idle(8);
        finish(scene, util.select().position(furnacePos),
            "Exoflame supplies 200 burn ticks and adds extra cooking progress every two ticks");
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
        scene.idle(70);
    }

    private static void revealRow(SceneBuilder scene, SceneBuildingUtil util, Vec3 focus, int z) {
        for (int x = 1; x <= 3; x++) {
            scene.world().showSection(util.select().position(util.grid().at(x, 1, z)), Direction.DOWN);
            focusOn(scene, focus);
            scene.idle(4);
        }
        scene.idle(8);
    }

    private static void addMana(SceneBuilder scene, BlockPos flowerPos, int amount) {
        scene.world().modifyBlockEntity(flowerPos, FunctionalFlowerBlockEntity.class,
            flower -> flower.addMana(amount));
    }

    private static void finish(SceneBuilder scene, Selection target, String text) {
        scene.overlay().showOutlineWithText(target, 65, text)
            .placeNearTarget()
            .colored(PonderPalette.OUTPUT)
            .attachKeyFrame();
        scene.idle(70);
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
