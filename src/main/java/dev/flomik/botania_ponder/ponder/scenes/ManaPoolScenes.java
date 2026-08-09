package dev.flomik.botania_ponder.ponder.scenes;

import dev.flomik.ponderlib.api.Pointing;
import dev.flomik.ponderlib.api.PonderPalette;
import dev.flomik.ponderlib.api.ParticleEmitter;
import dev.flomik.ponderlib.api.element.ElementLink;
import dev.flomik.ponderlib.api.element.EntityElement;
import dev.flomik.ponderlib.api.scene.CollisionMode;
import dev.flomik.ponderlib.api.scene.Easing;
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
import vazkii.botania.common.block.block_entity.mana.ManaPoolBlockEntity;

/**
 * Scenes about the <b>Mana Pool itself</b> - what it does and what you do to it. Anything that only
 * happens to stand next to a Pool (a Spreader, a flower) appears only where the Pool's own behaviour
 * can't be shown without it, and never as scenery.
 * <p>
 * Split per behaviour rather than one "how mana works" scene, matching how Create registers several
 * narrow scenes per component (see {@code AllCreatePonderScenes}: {@code cog/small},
 * {@code cog/speedup}, {@code cog/encasing} are three separate scenes for one block).
 */
public final class ManaPoolScenes {

    private ManaPoolScenes() {
    }

    /**
     * Schematic {@code mana_pool/infusion}: 5x5 grass plate, Mana Pool centred at (2,1,2). Nothing
     * else - this scene is only about what the Pool does to an item dropped into it.
     */
    public static void infusion(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("mana_pool_infusion", "Mana Infusion");
        scene.configureBasePlate(0, 0, 5);
        BlockPos poolPos = util.grid().at(2, 1, 2);
        Vec3 poolCenter = util.vector().centerOf(poolPos);
        scene.showBasePlate();
        scene.addInstruction(s -> s.setFocusPoint(poolCenter));
        scene.idle(15);

        // A Mana Pool is a shallow basin - ManaPoolBlock's shape is box(0,0,0,16,8,16) with a
        // cutout from 2/16 up, so its rim is only half a block high and its inside floor sits at
        // +2/16. util.vector().topOf() returns the top of the full block cube, a long way above the
        // rim: dropping an item from there overshoots the basin. Aim at the basin itself instead.
        Vec3 poolRim = poolCenter.add(0, 0.05, 0);
        Vec3 poolInside = poolCenter.add(0, -0.35, 0);

        scene.world().showSection(util.select().position(poolPos), Direction.DOWN);
        scene.idle(10);

        // Actually fill the block entity, so the Pool renders its real mana surface rather than the
        // scene claiming it is full in text alone.
        scene.world().modifyBlockEntity(poolPos, ManaPoolBlockEntity.class,
            be -> be.receiveMana(ManaPoolBlockEntity.MAX_MANA));
        scene.idle(5);
        scene.overlay().showOutlineWithText(util.select().position(poolPos), 80,
                "A Mana Pool holding Mana can infuse items dropped into it")
            .placeNearTarget()
            .attachKeyFrame();
        scene.idle(85);
        scene.addKeyframe();

        // Toss a diamond in - the input, dropped and left to sink into the pool.
        // Keep the input hint beside the Pool, clear of the item's vertical drop path.
        Vec3 dropHint = util.vector().blockSurface(poolPos, Direction.WEST).add(0, 0.15, 0);
        scene.overlay().showControls(dropHint, Pointing.RIGHT, 20)
            .withItem(new ItemStack(Items.DIAMOND))
            .drop();
        scene.idle(25);
        Vec3 dropStart = poolCenter.add(0, 1.1, 0);
        ElementLink<EntityElement> diamond = scene.world()
            .createItemEntity(dropStart, Vec3.ZERO, new ItemStack(Items.DIAMOND));
        scene.world().moveEntity(diamond, poolInside, 24, Easing.QUAD_IN, CollisionMode.IGNORE);
        scene.idle(24);
        scene.idle(4);

        // Show the swap, don't just assert it: the input is removed, Botania's own sparkle plays,
        // and the infused output appears in its place.
        scene.world().modifyEntity(diamond, Entity::discard);
        manaSparkle(scene, poolRim, 20);
        scene.effects().indicateSuccess(poolPos);
        // Spawned at rest inside the basin, not dropped - same as Create's own outputs
        // (ProcessingScenes spawns results with a zero motion vector at their final position).
        scene.world().createItemEntity(poolInside, Vec3.ZERO, manaDiamond());
        scene.idle(15);

        scene.overlay().showText(80, "The item is consumed and replaced by its infused form - here, a Mana Diamond")
            .pointAt(poolRim)
            .placeNearTarget()
            .colored(PonderPalette.OUTPUT);
        scene.idle(85);

        scene.markAsFinished();
    }

    /**
     * Schematic {@code mana_pool/filling}: 5x5 grass plate, Mana Pool centred at (2,1,2), a Mana
     * Spreader at (2,1,0) aimed along +z at it. The Spreader is here strictly as the Pool's mana
     * source - see {@code ManaSpreaderScenes} for what the Spreader itself does.
     */
    public static void filling(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("mana_pool_filling", "Filling a Mana Pool");
        scene.configureBasePlate(0, 0, 5);
        BlockPos poolPos = util.grid().at(2, 1, 2);
        BlockPos spreaderPos = util.grid().at(2, 1, 0);
        Vec3 poolCenter = util.vector().centerOf(poolPos);
        Vec3 spreaderCenter = util.vector().centerOf(spreaderPos);
        scene.showBasePlate();
        scene.addInstruction(s -> s.setFocusPoint(poolCenter));
        scene.idle(15);

        // Subject first, and it stays the focus for the whole scene.
        scene.world().showSection(util.select().position(poolPos), Direction.DOWN);
        scene.idle(10);
        scene.overlay().showOutlineWithText(util.select().position(poolPos), 70,
                "A Mana Pool stores Mana that other devices send to it")
            .placeNearTarget()
            .attachKeyFrame();
        scene.idle(75);

        // The source, revealed only now, and only because the Pool can't fill without one.
        scene.world().showSection(util.select().position(spreaderPos), Direction.DOWN);
        // Keep the Pool as the point of interest from the first reveal tick, avoiding a later snap.
        scene.addInstruction(s -> s.setFocusPoint(poolCenter));
        scene.idle(10);
        scene.overlay().showText(70, "A Mana Spreader aimed at the Pool sends Mana Bursts into it")
            .pointAt(spreaderCenter)
            .placeNearTarget();
        scene.idle(40);

        // Three bursts, each visibly raising the Pool's real fill level.
        for (int i = 0; i < 3; i++) {
            burstTrail(scene, spreaderCenter, poolCenter);
            fillPoolSmoothly(scene, poolPos, i == 2);
            scene.effects().indicateSuccess(poolPos);
            scene.idle(10);
        }

        scene.overlay().showOutlineWithText(util.select().position(poolPos), 70,
                "Each burst that lands raises the Pool's Mana level")
            .placeNearTarget()
            .attachKeyFrame();
        scene.idle(75);

        scene.markAsFinished();
    }

    /** Botania's own sparkle particle ({@code SparkleParticleData}), in Mana's own blue. */
    private static void manaSparkle(SceneBuilder scene, Vec3 at, int count) {
        SparkleParticleData data = SparkleParticleData.sparkle(1.6F, 0.4F, 0.4F, 1F, 5);
        ParticleEmitter emitter = scene.effects().particleEmitterWithinBlockSpace(data, Vec3.ZERO);
        scene.effects().emitParticles(at, emitter, count, 1);
    }

    private static void burstTrail(SceneBuilder scene, Vec3 from, Vec3 to) {
        ManaSpreaderScenes.burstTrail(scene, from, to);
    }

    private static void fillPoolSmoothly(SceneBuilder scene, BlockPos poolPos, boolean finalBurst) {
        int duration = 12;
        for (int tick = 1; tick <= duration; tick++) {
            int currentTick = tick;
            scene.world().modifyBlockEntity(poolPos, ManaPoolBlockEntity.class, pool -> {
                int amount = finalBurst && currentTick == duration
                    ? ManaPoolBlockEntity.MAX_MANA - pool.getCurrentMana()
                    : ManaPoolBlockEntity.MAX_MANA / 3 / duration;
                pool.receiveMana(amount);
            });
            scene.idle(1);
        }
    }

    private static ItemStack manaDiamond() {
        Item item = ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath("botania", "mana_diamond"));
        return new ItemStack(item);
    }
}
