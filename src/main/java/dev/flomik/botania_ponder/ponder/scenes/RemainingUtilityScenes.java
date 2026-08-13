package dev.flomik.botania_ponder.ponder.scenes;

import dev.flomik.ponderlib.api.Pointing;
import dev.flomik.ponderlib.api.PonderPalette;
import dev.flomik.ponderlib.api.element.ElementLink;
import dev.flomik.ponderlib.api.element.WorldSectionElement;
import dev.flomik.ponderlib.api.scene.SceneBuilder;
import dev.flomik.ponderlib.api.scene.SceneBuildingUtil;
import dev.flomik.ponderlib.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import vazkii.botania.client.fx.SparkleParticleData;
import vazkii.botania.common.block.block_entity.AnimatedTorchBlockEntity;
import vazkii.botania.common.block.block_entity.AvatarBlockEntity;
import vazkii.botania.common.block.block_entity.CacophoniumBlockEntity;
import vazkii.botania.common.block.block_entity.CocoonBlockEntity;
import vazkii.botania.common.block.block_entity.IncensePlateBlockEntity;
import vazkii.botania.common.block.block_entity.TinyPotatoBlockEntity;

import java.util.Objects;

/** The remaining small utility blocks in the current block-coverage milestone. */
public final class RemainingUtilityScenes {
    private RemainingUtilityScenes() {}

    public static void forceRelay(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos piston = util.grid().at(0, 1, 2), source = util.grid().at(1, 1, 2), remote = util.grid().at(3, 1, 2);
        Vec3 focus = util.vector().centerOf(util.grid().at(2, 1, 2));
        begin(scene, util, "force_relay_motion", "Relaying Piston Movement", source,
            "Bind a Force Relay to a distant block to transfer piston movement to that location");
        reveal(scene, util.select().position(piston).add(util.select().position(remote)), focus);
        scene.overlay().showControls(util.vector().topOf(source), Pointing.DOWN, 30)
            .withItem(botania("twig_wand")).rightClick();
        scene.overlay().showLine(PonderPalette.INPUT, util.vector().centerOf(source), util.vector().centerOf(remote), 45);
        scene.idle(55);
        scene.effects().indicateRedstone(piston);
        scene.world().modifyBlock(piston, state -> state.setValue(BlockStateProperties.EXTENDED, true), false);
        ElementLink<WorldSectionElement> left = scene.world().makeSectionIndependent(util.select().position(source));
        ElementLink<WorldSectionElement> right = scene.world().makeSectionIndependent(util.select().position(remote));
        scene.world().moveSection(left, new Vec3(1, 0, 0), 25);
        scene.world().moveSection(right, new Vec3(1, 0, 0), 25);
        scene.idle(30);
        finishAt(scene, util.vector().centerOf(remote.east()), "The bound block mirrors the relay's movement and remains at its new position");
    }

    public static void tinyPotato(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos potato = util.grid().at(2, 1, 2);
        begin(scene, util, "tiny_potato_name", "Naming a Tiny Potato", potato,
            "Use a Name Tag to give the Tiny Potato a name that remains visible above it");
        ItemStack tag = new ItemStack(Items.NAME_TAG); tag.setHoverName(Component.literal("Spud"));
        scene.overlay().showControls(util.vector().topOf(potato), Pointing.DOWN, 30).withItem(tag).rightClick();
        scene.world().modifyBlockEntity(potato, TinyPotatoBlockEntity.class, be -> {
            be.name = Component.literal("Spud"); be.jumpTicks = 10;
        });
        hearts(scene, util.vector().topOf(potato));
        finish(scene, util.select().position(potato), "Right-clicking also makes your named potato happily jump");
    }

    public static void incensePlate(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos plate = util.grid().at(2, 1, 2);
        begin(scene, util, "incense_plate_burning", "Burning Incense", plate,
            "An Incense Plate burns a brewed Incense Stick and spreads its effect across a wide area");
        ItemStack stick = botania("incense_stick");
        scene.overlay().showControls(util.vector().topOf(plate), Pointing.DOWN, 25).withItem(stick).rightClick();
        scene.world().modifyBlockEntity(plate, IncensePlateBlockEntity.class,
            be -> be.getItemHandler().setItem(0, stick.copy()));
        scene.idle(30);
        scene.overlay().showControls(util.vector().topOf(plate), Pointing.DOWN, 25)
            .withItem(new ItemStack(Items.FLINT_AND_STEEL)).rightClick();
        scene.world().modifyBlockEntity(plate, IncensePlateBlockEntity.class, be -> be.burning = true);
        scene.effects().emitParticles(util.vector().topOf(plate),
            scene.effects().simpleParticleEmitter(ParticleTypes.SMOKE, new Vec3(0, .05, 0)), 24, 2);
        finish(scene, util.select().position(plate), "Players within 32 blocks repeatedly receive the stick's brewed effect");
    }

    public static void cacophonium(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos block = util.grid().at(2, 1, 2), lever = util.grid().at(1, 1, 2);
        begin(scene, util, "cacophonium_replay", "Replaying a Creature Sound", block,
            "A Cacophonium Block stores a captured creature sound and replays it on a redstone pulse");
        reveal(scene, util.select().position(lever), util.vector().centerOf(block));
        scene.world().modifyBlock(lever, s -> s.setValue(BlockStateProperties.POWERED, true), false);
        scene.effects().indicateRedstone(lever);
        scene.effects().emitParticles(util.vector().topOf(block),
            scene.effects().simpleParticleEmitter(ParticleTypes.NOTE, Vec3.ZERO), 8, 2);
        finish(scene, util.select().position(block), "Every rising edge plays the same stored mob sound again");
    }

    public static void teruTeruBozu(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos teru = util.grid().at(2, 1, 2);
        begin(scene, util, "teru_teru_bozu_weather", "Clearing Rain", teru,
            "The Teru Teru Bozu periodically attempts to end rainy weather");
        Vec3 center = util.vector().centerOf(teru);
        scene.effects().emitParticles(center.add(0, 2, 0),
            scene.effects().simpleParticleEmitter(ParticleTypes.FALLING_WATER, new Vec3(0, -.1, 0)), 45, 3);
        scene.idle(30);
        sparkle(scene, center, 20);
        finish(scene, util.select().position(teru), "When its attempt succeeds, the rain timer is reset to clear weather");
    }

    public static void avatar(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos avatar = util.grid().at(2, 1, 2);
        begin(scene, util, "avatar_wielding", "Automating a Rod", avatar,
            "A Livingwood Avatar spends stored Mana to use a compatible rod in its facing direction");
        ItemStack rod = botania("water_rod");
        scene.overlay().showControls(util.vector().topOf(avatar), Pointing.DOWN, 25).withItem(rod).rightClick();
        scene.world().modifyBlockEntity(avatar, AvatarBlockEntity.class, be -> {
            be.getItemHandler().setItem(0, rod.copy()); be.receiveMana(3200);
        });
        BlockPos water = util.grid().at(2, 1, 3);
        scene.world().setBlock(water, Blocks.WATER.defaultBlockState(), false);
        scene.world().showSection(util.select().position(water), Direction.DOWN);
        pin(scene, util.vector().centerOf(avatar));
        scene.effects().indicateSuccess(avatar);
        finish(scene, util.select().position(avatar), "The Water Rod creates a source in front while the Avatar has Mana");
    }

    public static void animatedTorch(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos torch = util.grid().at(2, 1, 2), lamp = util.grid().at(3, 1, 2);
        begin(scene, util, "animated_torch_rotation", "Redirecting Redstone", torch,
            "The Animated Torch rotates to choose which neighboring side receives its redstone output");
        reveal(scene, util.select().position(lamp), util.vector().centerOf(torch));
        scene.overlay().showControls(util.vector().topOf(torch), Pointing.DOWN, 25).rightClick();
        scene.world().modifyBlockEntity(torch, AnimatedTorchBlockEntity.class, be -> {
            be.rotating = true; be.side = 1; be.rotation = 90;
        });
        scene.world().modifyBlock(lamp, s -> s.setValue(BlockStateProperties.LIT, true), false);
        scene.effects().indicateRedstone(torch);
        finish(scene, util.select().position(torch), "A Wand of the Forest changes how the next side is selected");
    }

    public static void cocoon(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos cocoon = util.grid().at(2, 1, 2);
        begin(scene, util, "cocoon_hatching", "Hatching an Animal", cocoon,
            "After two minutes, a Cocoon of Caprice hatches into a random passive animal");
        for (int step = 1; step <= 8; step++) {
            int time = CocoonBlockEntity.TOTAL_TIME * step / 8;
            scene.world().modifyBlockEntity(cocoon, CocoonBlockEntity.class, be -> be.timePassed = time);
            sparkle(scene, util.vector().centerOf(cocoon), 3); scene.idle(8);
        }
        scene.world().destroyBlock(cocoon);
        Vec3 spawn = util.vector().topOf(cocoon.below());
        scene.world().createEntity(level -> { Pig pig = new Pig(EntityType.PIG, level); pig.setNoAi(true); pig.moveTo(spawn.x, spawn.y, spawn.z, -90, 0); return pig; });
        finishAt(scene, spawn.add(0, .8, 0), "Feeding special items before hatching changes the possible result");
    }

    public static void felPumpkin(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos pumpkin = util.grid().at(2, 3, 2);
        Selection construct = util.select().position(pumpkin.below()).add(util.select().position(pumpkin.below(2)));
        begin(scene, util, "fel_pumpkin_blaze", "Summoning a Blaze", pumpkin,
            "Place a Fel Pumpkin on two Iron Bars to assemble a Blaze without a spawner");
        reveal(scene, construct, util.vector().centerOf(pumpkin.below()));
        scene.world().destroyBlock(pumpkin); scene.world().destroyBlock(pumpkin.below()); scene.world().destroyBlock(pumpkin.below(2));
        Vec3 spawn = util.vector().topOf(pumpkin.below(3));
        scene.world().createEntity(level -> { Blaze blaze = new Blaze(EntityType.BLAZE, level); blaze.setNoAi(true); blaze.moveTo(spawn.x, spawn.y, spawn.z, -90, 0); return blaze; });
        sparkle(scene, spawn.add(0, 1, 0), 24);
        finishAt(scene, spawn.add(0, 1, 0), "The three construction blocks are consumed when the Blaze appears");
    }

    public static void starfield(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos starfield = util.grid().at(2, 1, 2);
        begin(scene, util, "starfield_illusion", "Projecting a Starfield", starfield,
            "The Starfield Creator fills the surrounding view with a decorative moving star field");
        Vec3 center = util.vector().centerOf(starfield);
        var emitter = scene.effects().simpleParticleEmitter(
            SparkleParticleData.sparkle(.7F, .55F, .35F, .75F, 8), Vec3.ZERO);
        for (int ring = 0; ring < 24; ring++) {
            double angle = ring * Math.PI * 2 / 24;
            scene.effects().emitParticles(center.add(Math.cos(angle) * 2.2, (ring % 5) * .45, Math.sin(angle) * 2.2), emitter, 2, 1);
            scene.idle(2);
        }
        finish(scene, util.select().position(starfield), "The effect is visual only and follows the creator's animated sky pattern");
    }

    private static void begin(SceneBuilder scene, SceneBuildingUtil util, String id, String title, BlockPos subject, String text) {
        Vec3 focus = util.vector().centerOf(subject); scene.title(id, title); scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate(); pin(scene, focus); scene.idle(15); scene.world().showSection(util.select().position(subject), Direction.DOWN);
        pin(scene, focus); scene.idle(10); scene.overlay().showOutlineWithText(util.select().position(subject), 65, text).placeNearTarget().attachKeyFrame(); scene.idle(80);
    }
    private static void reveal(SceneBuilder scene, Selection selection, Vec3 focus) { scene.world().showSection(selection, Direction.DOWN); pin(scene, focus); scene.idle(20); }
    private static void finish(SceneBuilder scene, Selection target, String text) { scene.overlay().showOutlineWithText(target, 65, text).placeNearTarget().colored(PonderPalette.OUTPUT).attachKeyFrame(); scene.idle(70); scene.markAsFinished(); }
    private static void finishAt(SceneBuilder scene, Vec3 target, String text) { scene.overlay().showText(65, text).pointAt(target).placeNearTarget().colored(PonderPalette.OUTPUT).attachKeyFrame(); scene.idle(70); scene.markAsFinished(); }
    private static void sparkle(SceneBuilder scene, Vec3 at, int count) { scene.effects().emitParticles(at, scene.effects().simpleParticleEmitter(SparkleParticleData.sparkle(.8F, .3F, .7F, .4F, 6), Vec3.ZERO), count, 2); }
    private static void hearts(SceneBuilder scene, Vec3 at) { scene.effects().emitParticles(at, scene.effects().simpleParticleEmitter(ParticleTypes.HEART, new Vec3(0, .04, 0)), 8, 2); }
    private static ItemStack botania(String id) { Item item = Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath("botania", id))); return new ItemStack(item); }
    private static void pin(SceneBuilder scene, Vec3 focus) { scene.addInstruction(s -> s.setFocusPoint(focus)); }
}
