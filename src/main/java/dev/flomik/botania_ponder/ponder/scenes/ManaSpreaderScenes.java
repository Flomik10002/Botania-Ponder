package dev.flomik.botania_ponder.ponder.scenes;

import dev.flomik.ponderlib.api.ParticleEmitter;
import dev.flomik.ponderlib.api.Pointing;
import dev.flomik.ponderlib.api.PonderPalette;
import dev.flomik.ponderlib.api.scene.SceneBuilder;
import dev.flomik.ponderlib.api.scene.SceneBuildingUtil;
import dev.flomik.ponderlib.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import vazkii.botania.client.fx.SparkleParticleData;
import vazkii.botania.client.fx.WispParticleData;
import vazkii.botania.common.block.block_entity.mana.ManaPoolBlockEntity;
import vazkii.botania.common.block.block_entity.mana.ManaSpreaderBlockEntity;

/** A single scene covering the Mana Spreader's complete flow: receive Mana, bind, and fire. */
public final class ManaSpreaderScenes {

    /** {@code ManaSpreaderBlock.Variant.MANA#color} - a plain Spreader's real burst colour. */
    private static final int BURST_COLOR = 0x20FF20;
    /** {@code EndoflameBlockEntity#getColor()}. */
    private static final int ENDOFLAME_COLOR = 0x785000;

    private ManaSpreaderScenes() {
    }

    /**
     * Schematic {@code mana_spreader/spreader}: three Endoflames at z=0 feed the Spreader at z=2,
     * which is aimed at the Mana Pool at z=4.
     */
    public static void spreader(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("mana_spreader", "Using a Mana Spreader");
        scene.configureBasePlate(0, 0, 5);
        BlockPos spreaderPos = util.grid().at(2, 1, 2);
        BlockPos targetPos = util.grid().at(2, 1, 4);
        Selection flowers = util.select().fromTo(1, 1, 0, 3, 1, 0);
        BlockPos[] flowerPositions = {
            util.grid().at(1, 1, 0), util.grid().at(2, 1, 0), util.grid().at(3, 1, 0)
        };
        Vec3 spreaderCenter = util.vector().centerOf(spreaderPos);
        Vec3 targetCenter = util.vector().centerOf(targetPos);
        scene.showBasePlate();
        focusOn(scene, spreaderCenter);
        scene.idle(15);

        scene.world().showSection(util.select().position(spreaderPos), Direction.DOWN);
        scene.idle(10);
        scene.overlay().showOutlineWithText(util.select().position(spreaderPos), 80,
                "A Mana Spreader holds a small internal buffer of Mana, and needs a source to fill it")
            .placeNearTarget()
            .attachKeyFrame();
        scene.idle(85);

        scene.world().showSection(flowers, Direction.DOWN);
        focusOn(scene, spreaderCenter);
        scene.idle(10);
        scene.overlay().showOutlineWithText(flowers, 90,
                "Generating Flora placed nearby auto-bind to the closest Spreader and feed it as they work")
            .placeNearTarget()
            .attachKeyFrame();
        scene.idle(95);
        fillSpreader(scene, spreaderPos, spreaderCenter, flowerPositions);
        scene.idle(10);

        scene.world().showSection(util.select().position(targetPos), Direction.DOWN);
        focusOn(scene, spreaderCenter);
        scene.idle(10);

        scene.overlay().showOutlineWithText(util.select().position(spreaderPos), 70,
                "The stored Mana is fired in bursts at the block the Spreader is bound to")
            .placeNearTarget()
            .attachKeyFrame();
        scene.idle(75);

        scene.overlay().showText(100,
                "With a Wand of the Forest in Bind Mode, sneak-right click the Spreader, then its target")
            .pointAt(spreaderCenter)
            .placeNearTarget();
        scene.idle(20);
        // One input hint is enough: the same Shift + right-click combination selects the Spreader,
        // then the moving outline shows where the second click goes.
        scene.overlay().showControls(util.vector().topOf(spreaderPos), Pointing.DOWN, 75)
            .withItem(twigWand())
            .rightClick()
            .whileSneaking();
        scene.overlay().showOutline(PonderPalette.OUTPUT, "binding_target",
            util.select().position(spreaderPos), 40);
        scene.idle(45);
        scene.overlay().showOutline(PonderPalette.OUTPUT, "binding_target",
            util.select().position(targetPos), 30);
        scene.idle(40);

        // Holding the wand makes a bound Spreader draw its aiming beam - real behaviour, see
        // botania.page.spreader3 ("nearby spreaders display aiming beams").
        scene.overlay().showLine(PonderPalette.OUTPUT, spreaderCenter, targetCenter, 60);
        scene.overlay().showText(60, "Once bound, the Spreader shows an aiming beam to its target")
            .pointAt(spreaderCenter)
            .placeNearTarget()
            .attachKeyFrame();
        scene.idle(65);

        scene.overlay().showText(70, "It then keeps firing as long as it has Mana and its target can accept more")
            .pointAt(spreaderCenter)
            .placeNearTarget()
            .attachKeyFrame();
        scene.idle(75);
        for (int burst = 1; burst <= 3; burst++) {
            burstTrail(scene, spreaderCenter, targetCenter);
            transferManaSmoothly(scene, spreaderPos, targetPos, burst == 3);
            scene.effects().indicateSuccess(targetPos);
            scene.idle(10);
        }
        scene.markAsFinished();
    }

    /** Continuously fills the real buffer while the flowers' sparkles travel into the Spreader. */
    private static void fillSpreader(SceneBuilder scene, BlockPos spreaderPos, Vec3 spreaderCenter,
                                     BlockPos[] flowerPositions) {
        float r = (ENDOFLAME_COLOR >> 16 & 0xFF) / 255F;
        float g = (ENDOFLAME_COLOR >> 8 & 0xFF) / 255F;
        float b = (ENDOFLAME_COLOR & 0xFF) / 255F;
        SparkleParticleData data = SparkleParticleData.sparkle(1.2F, r, g, b, 5);
        ParticleEmitter emitter = scene.effects().simpleParticleEmitter(data, Vec3.ZERO);

        int fillDuration = 24;
        scene.overlay().showText(fillDuration, "Mana flows into the Spreader's internal buffer")
            .pointAt(spreaderCenter)
            .placeNearTarget()
            .colored(PonderPalette.INPUT);
        for (int tick = 1; tick <= fillDuration; tick++) {
            double progress = tick / (double) fillDuration;
            for (BlockPos pos : flowerPositions) {
                Vec3 flowerCenter = new Vec3(pos.getX() + 0.5, pos.getY() + 0.4, pos.getZ() + 0.5);
                // The final tick lands at progress 1, exactly at the Spreader's centre.
                scene.effects().emitParticles(flowerCenter.lerp(spreaderCenter, progress), emitter, 1, 1);
            }

            int currentTick = tick;
            scene.world().modifyBlockEntity(spreaderPos, ManaSpreaderBlockEntity.class, spreader -> {
                int amount = currentTick == fillDuration
                    ? spreader.getMaxMana()
                    : Math.max(1, spreader.getMaxMana() / fillDuration);
                spreader.receiveMana(amount);
            });
            scene.idle(1);
        }
        scene.idle(5);
        scene.effects().indicateSuccess(spreaderPos);
        scene.overlay().showText(28, "The Mana buffer is full")
            .pointAt(spreaderCenter)
            .placeNearTarget()
            .colored(PonderPalette.OUTPUT);
        scene.idle(32);
    }

    /**
     * A Mana Burst's real particle trail. {@code ManaBurstEntity#particles} draws a burst purely as
     * {@code WispParticleData} in the Spreader's own colour - a burst has no model at all - so this
     * emits that same particle along the path instead of standing anything else in for it.
     */
    static void burstTrail(SceneBuilder scene, Vec3 from, Vec3 to) {
        float r = (BURST_COLOR >> 16 & 0xFF) / 255F;
        float g = (BURST_COLOR >> 8 & 0xFF) / 255F;
        float b = (BURST_COLOR & 0xFF) / 255F;
        WispParticleData data = WispParticleData.wisp(0.4F, r, g, b);
        ParticleEmitter emitter = scene.effects().simpleParticleEmitter(data, Vec3.ZERO);

        Vec3 path = to.subtract(from);
        int steps = 24;
        for (int i = 1; i <= steps; i++) {
            scene.effects().emitParticles(from.add(path.scale((double) i / steps)), emitter, 2, 1);
            scene.idle(1);
        }
    }

    private static void transferManaSmoothly(SceneBuilder scene, BlockPos spreaderPos,
                                             BlockPos poolPos, boolean finalBurst) {
        int duration = 12;
        for (int tick = 1; tick <= duration; tick++) {
            int currentTick = tick;
            scene.world().modifyBlockEntity(spreaderPos, ManaSpreaderBlockEntity.class, spreader -> {
                int amount = finalBurst && currentTick == duration
                    ? spreader.getCurrentMana()
                    : spreader.getMaxMana() / 3 / duration;
                spreader.receiveMana(-Math.min(amount, spreader.getCurrentMana()));
            });
            scene.world().modifyBlockEntity(poolPos, ManaPoolBlockEntity.class, pool -> {
                int amount = finalBurst && currentTick == duration
                    ? ManaPoolBlockEntity.MAX_MANA - pool.getCurrentMana()
                    : ManaPoolBlockEntity.MAX_MANA / 3 / duration;
                pool.receiveMana(amount);
            });
            scene.idle(1);
        }
    }

    /**
     * Puts the camera back on the scene's subject.
     * <p>
     * Every {@code showSection} silently moves the camera: {@code RevealSectionInstruction} calls
     * {@code scene.setFocusPoint(selection.getCenter())} on the section it reveals. So revealing a
     * supporting block drags the camera off the block the scene is actually about, and it stays
     * there unless it is put back - which is what this does.
     */
    private static void focusOn(SceneBuilder scene, Vec3 subject) {
        scene.addInstruction(s -> s.setFocusPoint(subject));
    }

    private static ItemStack twigWand() {
        Item item = ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath("botania", "twig_wand"));
        return new ItemStack(item);
    }
}
