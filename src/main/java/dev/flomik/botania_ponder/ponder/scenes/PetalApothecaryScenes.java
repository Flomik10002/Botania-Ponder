package dev.flomik.botania_ponder.ponder.scenes;

import dev.flomik.ponderlib.api.ParticleEmitter;
import dev.flomik.ponderlib.api.Pointing;
import dev.flomik.ponderlib.api.PonderPalette;
import dev.flomik.ponderlib.api.element.ElementLink;
import dev.flomik.ponderlib.api.element.EntityElement;
import dev.flomik.ponderlib.api.scene.SceneBuilder;
import dev.flomik.ponderlib.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import vazkii.botania.api.block.PetalApothecary;
import vazkii.botania.client.fx.SparkleParticleData;
import vazkii.botania.common.block.PetalApothecaryBlock;
import vazkii.botania.common.block.block_entity.PetalApothecaryBlockEntity;

import java.util.List;
import java.util.Objects;

/** One focused scene: crafting a functional flower in the Petal Apothecary. */
public final class PetalApothecaryScenes {

    private PetalApothecaryScenes() {
    }

    public static void crafting(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("petal_apothecary_crafting", "Crafting Flowers in the Petal Apothecary");
        scene.configureBasePlate(0, 0, 5);

        BlockPos altarPos = util.grid().at(2, 1, 2);
        Vec3 altarCenter = util.vector().centerOf(altarPos);
        Vec3 fluidSurface = util.vector().topOf(altarPos).add(0, 0.03, 0);

        scene.showBasePlate();
        focusOn(scene, altarCenter);
        scene.idle(15);

        scene.world().showSection(util.select().position(altarPos), Direction.DOWN);
        focusOn(scene, altarCenter);
        scene.idle(10);
        scene.overlay().showOutlineWithText(util.select().position(altarPos), 70,
                "The Petal Apothecary crafts Botania's functional and generating flowers")
            .placeNearTarget()
            .attachKeyFrame();
        scene.idle(75);

        Vec3 controlPoint = util.vector().blockSurface(altarPos, Direction.EAST).add(0, 0.2, 0);
        scene.overlay().showControls(controlPoint, Pointing.LEFT, 25)
            .withItem(new ItemStack(Items.WATER_BUCKET))
            .rightClick();
        scene.idle(28);
        scene.world().modifyBlock(altarPos,
            state -> state.setValue(PetalApothecaryBlock.FLUID, PetalApothecary.State.WATER), false);
        scene.effects().emitSparks(fluidSurface, 0x557FA8, 8);
        scene.idle(8);
        scene.overlay().showText(55, "Begin every recipe by filling the Apothecary with water")
            .pointAt(fluidSurface)
            .placeNearTarget()
            .colored(PonderPalette.INPUT)
            .attachKeyFrame();
        scene.idle(60);

        List<ItemStack> petals = List.of(
            botaniaItem("brown_petal"),
            botaniaItem("brown_petal"),
            botaniaItem("red_petal"),
            botaniaItem("light_gray_petal")
        );
        int[] colors = { 0x6B4932, 0x6B4932, 0xA33A36, 0xA7A7A7 };
        scene.overlay().showText(88, "Drop the recipe's petals into the water")
            .pointAt(fluidSurface)
            .placeNearTarget()
            .colored(PonderPalette.INPUT);
        for (int slot = 0; slot < petals.size(); slot++) {
            ItemStack petal = petals.get(slot);
            dropIngredient(scene, altarPos, fluidSurface, petal, slot, colors[slot]);
        }
        scene.idle(6);
        scene.addKeyframe();

        scene.overlay().showControls(controlPoint, Pointing.LEFT, 22)
            .withItem(new ItemStack(Items.WHEAT_SEEDS))
            .drop();
        scene.idle(25);
        Vec3 seedStart = fluidSurface.add(0, 1.35, 0);
        ElementLink<EntityElement> seed = scene.world().createItemEntity(
            seedStart, Vec3.ZERO, new ItemStack(Items.WHEAT_SEEDS));
        scene.idle(20);
        scene.idle(3);
        scene.world().modifyEntity(seed, Entity::discard);

        scene.world().modifyBlockEntity(altarPos, PetalApothecaryBlockEntity.class, altar -> {
            altar.getItemHandler().clearContent();
        });
        scene.world().modifyBlock(altarPos,
            state -> state.setValue(PetalApothecaryBlock.FLUID, PetalApothecary.State.EMPTY), false);
        rainbowSparkle(scene, fluidSurface);
        scene.effects().indicateSuccess(altarPos);

        ItemStack result = botaniaItem("endoflame");
        scene.world().createItemEntity(fluidSurface.add(0, 0.35, 0), Vec3.ZERO, result);
        scene.idle(12);
        scene.overlay().showText(70, "A Seed completes the recipe and produces the flower")
            .pointAt(fluidSurface.add(0, 0.25, 0))
            .placeNearTarget()
            .colored(PonderPalette.OUTPUT)
            .attachKeyFrame();
        scene.idle(75);

        scene.markAsFinished();
    }

    private static void dropIngredient(SceneBuilder scene, BlockPos altarPos, Vec3 target,
                                       ItemStack ingredient, int slot, int color) {
        Vec3 start = target.add(0, 1.25, 0);
        ElementLink<EntityElement> entity = scene.world().createItemEntity(start, Vec3.ZERO, ingredient.copy());
        scene.idle(18);
        scene.idle(2);
        scene.world().modifyEntity(entity, Entity::discard);
        scene.world().modifyBlockEntity(altarPos, PetalApothecaryBlockEntity.class,
            altar -> altar.getItemHandler().setItem(slot, ingredient.copy()));
        scene.effects().emitSparks(target, color, 4);
        scene.idle(2);
    }

    private static void rainbowSparkle(SceneBuilder scene, Vec3 at) {
        int[] colors = { 0xD46A6A, 0xD6A85F, 0x72A86B, 0x668FB0, 0x9A75B5 };
        for (int color : colors) {
            float r = (color >> 16 & 0xFF) / 255F;
            float g = (color >> 8 & 0xFF) / 255F;
            float b = (color & 0xFF) / 255F;
            SparkleParticleData data = SparkleParticleData.sparkle(1.1F, r, g, b, 8);
            ParticleEmitter emitter = scene.effects().particleEmitterWithinBlockSpace(data, Vec3.ZERO);
            scene.effects().emitParticles(at, emitter, 3, 2);
        }
    }

    private static ItemStack botaniaItem(String path) {
        Item item = Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(
            ResourceLocation.fromNamespaceAndPath("botania", path)));
        return new ItemStack(item);
    }

    private static void focusOn(SceneBuilder scene, Vec3 subject) {
        scene.addInstruction(s -> s.setFocusPoint(subject));
    }
}
