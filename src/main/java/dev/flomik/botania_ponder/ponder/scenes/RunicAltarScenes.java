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
import vazkii.botania.client.fx.SparkleParticleData;
import vazkii.botania.common.block.block_entity.RunicAltarBlockEntity;

import java.util.List;
import java.util.Objects;

/** One focused scene: completing a recipe on the Runic Altar. */
public final class RunicAltarScenes {

    private static final int RUNE_OF_WATER_MANA = 5200;

    private RunicAltarScenes() {
    }

    public static void crafting(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("runic_altar_crafting", "Crafting Runes on the Runic Altar");
        scene.configureBasePlate(0, 0, 5);

        BlockPos altarPos = util.grid().at(2, 1, 3);
        BlockPos spreaderPos = util.grid().at(2, 1, 0);
        Vec3 altarCenter = util.vector().centerOf(altarPos);
        Vec3 altarTop = util.vector().topOf(altarPos).add(0, 0.03, 0);
        Vec3 spreaderCenter = util.vector().centerOf(spreaderPos);

        scene.showBasePlate();
        focusOn(scene, altarCenter);
        scene.idle(15);

        scene.world().showSection(util.select().position(altarPos), Direction.DOWN);
        focusOn(scene, altarCenter);
        scene.idle(10);
        scene.overlay().showOutlineWithText(util.select().position(altarPos), 70,
                "The Runic Altar combines items and Mana to create Runes")
            .placeNearTarget()
            .attachKeyFrame();
        scene.idle(75);

        List<ItemStack> ingredients = List.of(
            botaniaItem("mana_powder", 1),
            botaniaItem("manasteel_ingot", 1),
            new ItemStack(Items.BONE_MEAL),
            new ItemStack(Items.SUGAR_CANE),
            new ItemStack(Items.FISHING_ROD)
        );
        scene.overlay().showText(110, "Add every ingredient for the Rune of Water")
            .pointAt(altarTop)
            .placeNearTarget()
            .colored(PonderPalette.INPUT);
        for (int slot = 0; slot < ingredients.size(); slot++) {
            dropIngredient(scene, altarPos, altarTop, ingredients.get(slot), slot);
        }
        scene.idle(6);
        scene.addKeyframe();

        scene.world().modifyBlockEntity(altarPos, RunicAltarBlockEntity.class,
            altar -> altar.manaToGet = RUNE_OF_WATER_MANA);
        scene.world().showSection(util.select().position(spreaderPos), Direction.DOWN);
        focusOn(scene, altarCenter);
        scene.idle(10);
        scene.overlay().showText(60, "A Mana Spreader supplies the Mana required by the recipe")
            .pointAt(spreaderCenter)
            .placeNearTarget();
        scene.idle(65);

        for (int burst = 0; burst < 3; burst++) {
            ManaSpreaderScenes.burstTrail(scene, spreaderCenter, altarCenter);
            fillAltar(scene, altarPos, burst == 2);
            scene.effects().indicateSuccess(altarPos);
            scene.idle(8);
        }

        scene.overlay().showOutlineWithText(util.select().position(altarPos), 65,
                "When enough Mana arrives, the central indicator reaches full size")
            .placeNearTarget()
            .colored(PonderPalette.OUTPUT)
            .attachKeyFrame();
        scene.idle(70);

        Vec3 controlPoint = util.vector().blockSurface(altarPos, Direction.EAST).add(0, 0.2, 0);
        scene.overlay().showControls(controlPoint, Pointing.LEFT, 20)
            .withItem(botaniaItem("livingrock", 1))
            .drop();
        scene.idle(23);
        Vec3 rockStart = altarTop.add(0, 1.35, 0);
        ElementLink<EntityElement> livingrock = scene.world().createItemEntity(
            rockStart, Vec3.ZERO, botaniaItem("livingrock", 1));
        scene.idle(20);
        scene.idle(5);

        scene.overlay().showControls(controlPoint, Pointing.LEFT, 25)
            .withItem(botaniaItem("twig_wand", 1))
            .rightClick();
        scene.idle(28);
        completeRecipe(scene, altarPos, livingrock, altarTop);
        scene.idle(12);

        scene.overlay().showText(70,
                "Add Livingrock and right-click with the Wand of the Forest to complete the ritual")
            .pointAt(altarTop.add(0, 0.25, 0))
            .placeNearTarget()
            .colored(PonderPalette.OUTPUT)
            .attachKeyFrame();
        scene.idle(75);

        scene.markAsFinished();
    }

    private static void dropIngredient(SceneBuilder scene, BlockPos altarPos, Vec3 target,
                                       ItemStack ingredient, int slot) {
        Vec3 start = target.add(0, 1.25, 0);
        ElementLink<EntityElement> entity = scene.world().createItemEntity(start, Vec3.ZERO, ingredient.copy());
        scene.idle(18);
        scene.idle(2);
        scene.world().modifyEntity(entity, Entity::discard);
        scene.world().modifyBlockEntity(altarPos, RunicAltarBlockEntity.class,
            altar -> altar.getItemHandler().setItem(slot, ingredient.copy()));
        scene.effects().emitSparks(target, 0x4F968D, 4);
        scene.idle(2);
    }

    private static void fillAltar(SceneBuilder scene, BlockPos altarPos, boolean finalBurst) {
        int duration = 12;
        for (int tick = 1; tick <= duration; tick++) {
            int currentTick = tick;
            scene.world().modifyBlockEntity(altarPos, RunicAltarBlockEntity.class, altar -> {
                int amount = finalBurst && currentTick == duration
                    ? altar.getTargetMana() - altar.getCurrentMana()
                    : RUNE_OF_WATER_MANA / 3 / duration;
                altar.receiveMana(amount);
            });
            scene.idle(1);
        }
    }

    private static void completeRecipe(SceneBuilder scene, BlockPos altarPos,
                                       ElementLink<EntityElement> livingrock, Vec3 altarTop) {
        scene.world().modifyEntity(livingrock, Entity::discard);
        scene.world().modifyBlockEntity(altarPos, RunicAltarBlockEntity.class, altar -> {
            altar.getItemHandler().clearContent();
            altar.receiveMana(-altar.getCurrentMana());
            altar.manaToGet = 0;
        });

        int[] colors = { 0x4F968D, 0x73B9B0, 0xA6D4CE };
        for (int color : colors) {
            float r = (color >> 16 & 0xFF) / 255F;
            float g = (color >> 8 & 0xFF) / 255F;
            float b = (color & 0xFF) / 255F;
            SparkleParticleData data = SparkleParticleData.sparkle(1.2F, r, g, b, 8);
            ParticleEmitter emitter = scene.effects().particleEmitterWithinBlockSpace(data, Vec3.ZERO);
            scene.effects().emitParticles(altarTop, emitter, 4, 2);
        }
        scene.effects().indicateSuccess(altarPos);
        scene.world().createItemEntity(altarTop.add(0, 0.35, 0), Vec3.ZERO,
            botaniaItem("rune_water", 2));
    }

    private static ItemStack botaniaItem(String path, int count) {
        Item item = Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(
            ResourceLocation.fromNamespaceAndPath("botania", path)));
        return new ItemStack(item, count);
    }

    private static void focusOn(SceneBuilder scene, Vec3 subject) {
        scene.addInstruction(s -> s.setFocusPoint(subject));
    }
}
