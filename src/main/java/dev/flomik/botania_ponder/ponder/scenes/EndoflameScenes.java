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
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import vazkii.botania.api.block_entity.GeneratingFlowerBlockEntity;

/** One focused scene: fuel consumption and Mana generation inside an Endoflame. */
public final class EndoflameScenes {

    private EndoflameScenes() {
    }

    public static void generation(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("endoflame_generation", "Generating Mana with an Endoflame");
        scene.configureBasePlate(0, 0, 5);

        BlockPos flowerPos = util.grid().at(2, 1, 2);
        Vec3 flowerCenter = util.vector().centerOf(flowerPos);

        scene.showBasePlate();
        focusOn(scene, flowerCenter);
        scene.idle(15);

        scene.world().showSection(util.select().position(flowerPos), Direction.DOWN);
        focusOn(scene, flowerCenter);
        scene.idle(10);
        scene.overlay().showOutlineWithText(util.select().position(flowerPos), 75,
                "The Endoflame turns ordinary furnace fuel into Mana")
            .placeNearTarget()
            .attachKeyFrame();
        scene.idle(80);

        Vec3 fuelTarget = flowerCenter.add(-0.72, -0.35, 0);
        Vec3 fuelStart = fuelTarget.add(0, 1.8, 0);
        Vec3 inputHint = util.vector().blockSurface(flowerPos, Direction.EAST).add(0, 0.15, 0);
        scene.overlay().showControls(inputHint, Pointing.LEFT, 22)
            .withItem(new ItemStack(Items.COAL))
            .drop();
        scene.idle(25);

        ElementLink<EntityElement> coal = scene.world().createItemEntity(
            fuelStart, Vec3.ZERO, new ItemStack(Items.COAL));
        scene.idle(24);
        scene.idle(5);
        scene.world().modifyEntity(coal, Entity::discard);
        scene.effects().emitParticles(fuelTarget,
            scene.effects().particleEmitterWithinBlockSpace(ParticleTypes.LARGE_SMOKE, Vec3.ZERO), 3, 2);
        scene.effects().emitParticles(flowerCenter,
            scene.effects().particleEmitterWithinBlockSpace(ParticleTypes.FLAME, Vec3.ZERO), 2, 5);
        scene.idle(8);

        scene.overlay().showText(60, "One fuel item is consumed, then burns over time")
            .pointAt(flowerCenter)
            .placeNearTarget()
            .colored(PonderPalette.INPUT)
            .attachKeyFrame();
        ParticleEmitter flame = scene.effects().particleEmitterWithinBlockSpace(ParticleTypes.FLAME, Vec3.ZERO);
        for (int tick = 0; tick < 60; tick++) {
            if (tick % 3 == 0) {
                scene.effects().emitParticles(flowerCenter.add(0, 0.25, 0), flame, 1, 1);
            }
            scene.world().modifyBlockEntity(flowerPos, GeneratingFlowerBlockEntity.class,
                flower -> flower.addMana(5));
            scene.idle(1);
        }

        scene.effects().indicateSuccess(flowerPos);

        scene.idle(15);
        scene.overlay().showOutlineWithText(util.select().position(flowerPos), 65,
                "While the fuel burns, the Endoflame steadily fills its internal Mana buffer")
            .placeNearTarget()
            .colored(PonderPalette.OUTPUT)
            .attachKeyFrame();
        scene.idle(70);

        scene.markAsFinished();
    }

    private static void focusOn(SceneBuilder scene, Vec3 subject) {
        scene.addInstruction(s -> s.setFocusPoint(subject));
    }
}
