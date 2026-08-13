package dev.flomik.botania_ponder.ponder.scenes;

import dev.flomik.ponderlib.api.ParticleEmitter;
import dev.flomik.ponderlib.api.Pointing;
import dev.flomik.ponderlib.api.PonderPalette;
import dev.flomik.ponderlib.api.element.ElementLink;
import dev.flomik.ponderlib.api.element.EntityElement;
import dev.flomik.ponderlib.api.scene.CollisionMode;
import dev.flomik.ponderlib.api.scene.Easing;
import dev.flomik.ponderlib.api.scene.SceneBuilder;
import dev.flomik.ponderlib.api.scene.SceneBuildingUtil;
import dev.flomik.ponderlib.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import vazkii.botania.api.state.BotaniaStateProperties;
import vazkii.botania.api.state.enums.CraftyCratePattern;
import vazkii.botania.client.fx.SparkleParticleData;
import vazkii.botania.common.block.block_entity.CraftyCrateBlockEntity;
import vazkii.botania.common.block.block_entity.HoveringHourglassBlockEntity;
import vazkii.botania.common.block.block_entity.PlatformBlockEntity;
import vazkii.botania.common.block.block_entity.mana.ManaPoolBlockEntity;
import vazkii.botania.common.block.block_entity.mana.ManaPumpBlockEntity;
import vazkii.botania.common.entity.ManaPoolMinecartEntity;

import java.util.Objects;

/** Focused demonstrations for Botania automation blocks and the three platform variants. */
public final class AutomationScenes {

    private AutomationScenes() {
    }

    public static void craftyCrate(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos crate = util.grid().at(2, 2, 2);
        Vec3 top = util.vector().topOf(crate).add(0, 0.25, 0);
        Vec3 output = util.vector().centerOf(crate.below()).add(0, -0.35, 0);
        begin(scene, util, "crafty_crate_crafting", "Automatic Crafting", crate,
            "A Crafty Crate crafts as soon as every open slot in its pattern contains an item");

        scene.world().modifyBlock(crate,
            state -> state.setValue(BotaniaStateProperties.CRATE_PATTERN, CraftyCratePattern.CRAFTY_2_2), false);
        scene.overlay().showText(55, "This 2 by 2 pattern leaves four crafting slots open")
            .pointAt(util.vector().centerOf(crate)).placeNearTarget().attachKeyFrame();
        scene.idle(70);

        for (int slot : new int[] { 0, 1, 3, 4 }) {
            scene.overlay().showControls(top, Pointing.DOWN, 12).withItem(new ItemStack(Items.OAK_PLANKS));
            scene.world().modifyBlockEntity(crate, CraftyCrateBlockEntity.class,
                be -> be.getItemHandler().setItem(slot, new ItemStack(Items.OAK_PLANKS)));
            scene.idle(16);
        }
        scene.world().modifyBlockEntity(crate, CraftyCrateBlockEntity.class, be -> {
            for (int slot : new int[] { 0, 1, 3, 4 }) {
                be.getItemHandler().setItem(slot, ItemStack.EMPTY);
            }
        });
        scene.world().createItemEntity(output, new Vec3(0, -0.05, 0), new ItemStack(Items.CRAFTING_TABLE));
        greenSparkles(scene, util.vector().centerOf(crate), 18);
        finish(scene, util.select().position(crate),
            "The result is ejected through the open bottom, ready for a Hopper or another Crate");
    }

    public static void manaPump(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos pool = util.grid().at(1, 1, 2);
        BlockPos pump = util.grid().at(2, 1, 2);
        BlockPos rail = util.grid().at(3, 1, 2);
        Vec3 cartPos = util.vector().topOf(rail);
        begin(scene, util, "mana_pump_transfer", "Filling a Mana Pool Minecart", pump,
            "A Mana Pump transfers Mana between an adjacent Pool and a Mana Pool Minecart on rails");

        Selection system = util.select().position(pool).add(util.select().position(rail));
        reveal(scene, system);
        ElementLink<EntityElement> cart = scene.world().createEntity(level -> {
            ManaPoolMinecartEntity entity = new ManaPoolMinecartEntity(level, cartPos.x, cartPos.y, cartPos.z);
            entity.setMana(0);
            return entity;
        });
        scene.world().modifyBlockEntity(pool, ManaPoolBlockEntity.class,
            be -> be.receiveMana(ManaPoolBlockEntity.MAX_MANA * 3 / 4));
        scene.overlay().showText(55, "This orientation moves Mana from the stationary Pool into the cart")
            .pointAt(util.vector().centerOf(pump)).placeNearTarget().attachKeyFrame();
        scene.idle(70);

        for (int step = 1; step <= 8; step++) {
            int mana = ManaPoolBlockEntity.MAX_MANA * step / 16;
            scene.world().modifyEntity(cart, entity -> ((ManaPoolMinecartEntity) entity).setMana(mana));
            scene.world().modifyBlockEntity(pool, ManaPoolBlockEntity.class,
                be -> be.receiveMana(-ManaPoolBlockEntity.MAX_MANA / 16));
            scene.world().modifyBlockEntity(pump, ManaPumpBlockEntity.class, be -> {
                be.hasCart = true;
                be.hasCartOnTop = true;
                be.innerRingPos += 1F;
            });
            greenSparkles(scene, util.vector().centerOf(pump), 3);
            scene.idle(6);
        }
        finish(scene, util.select().position(pump),
            "Rotate the Pump to reverse the transfer; a redstone signal pauses it");
    }

    public static void hoveringHourglass(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos hourglass = util.grid().at(2, 1, 2);
        BlockPos lamp = util.grid().at(2, 1, 3);
        begin(scene, util, "hovering_hourglass_timer", "Timing Redstone Pulses", hourglass,
            "The Hovering Hourglass waits for the duration represented by the items inside it");

        reveal(scene, util.select().position(lamp));
        scene.overlay().showControls(util.vector().topOf(hourglass), Pointing.DOWN, 30)
            .withItem(new ItemStack(Items.SAND, 3)).rightClick();
        scene.world().modifyBlockEntity(hourglass, HoveringHourglassBlockEntity.class,
            be -> be.getItemHandler().setItem(0, new ItemStack(Items.SAND, 3)));
        scene.overlay().showText(55, "Three Sand make a 60 tick timer: three seconds")
            .pointAt(util.vector().centerOf(hourglass)).placeNearTarget().attachKeyFrame();
        scene.idle(70);

        for (int step = 1; step <= 10; step++) {
            float fraction = step / 10F;
            scene.world().modifyBlockEntity(hourglass, HoveringHourglassBlockEntity.class, be -> {
                be.lastFraction = Math.max(0, fraction - 0.1F);
                be.timeFraction = fraction;
            });
            scene.idle(6);
        }
        scene.world().modifyBlockEntity(hourglass, HoveringHourglassBlockEntity.class, be -> {
            be.flip = !be.flip;
            be.flipTicks = 4;
            be.lastFraction = 0;
            be.timeFraction = 0;
        });
        scene.world().modifyBlock(lamp, state -> state.setValue(BlockStateProperties.LIT, true), false);
        scene.effects().indicateRedstone(hourglass);
        scene.idle(4);
        scene.world().modifyBlock(lamp, state -> state.setValue(BlockStateProperties.LIT, false), false);
        finish(scene, util.select().position(hourglass),
            "At the end of every cycle it flips and emits a four tick redstone pulse");
    }

    public static void abstrusePlatform(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos platform = util.grid().at(2, 2, 2);
        Vec3 above = util.vector().topOf(platform).add(0, 1.2, 0);
        beginStable(scene, util, "abstruse_platform_collision", "A One-Way Platform", platform,
            "The Abstruse Platform catches entities falling from above but permits travel upward");

        ElementLink<EntityElement> falling = scene.world().createItemEntity(above, Vec3.ZERO,
            new ItemStack(Items.APPLE));
        scene.world().moveEntity(falling, util.vector().topOf(platform).add(0, 0.15, 0), 25,
            Easing.QUAD_IN, CollisionMode.RESPECT);
        scene.idle(35);
        ElementLink<EntityElement> rising = scene.world().createItemEntity(
            util.vector().centerOf(platform.below()).add(0, -0.25, 0), Vec3.ZERO, new ItemStack(Items.FEATHER));
        scene.world().moveEntity(rising, above, 30, Easing.QUAD_OUT, CollisionMode.IGNORE);
        scene.idle(35);
        finish(scene, util.select().position(platform),
            "Players and items can rise through it, then stand safely on its upper face");
    }

    public static void spectralPlatform(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos platform = util.grid().at(2, 2, 2);
        BlockPos torch = platform.above();
        Vec3 stableFocus = util.vector().centerOf(platform);
        beginStable(scene, util, "spectral_platform_support", "Support Without Collision", platform,
            "The Spectral Platform has no entity collision, but still supports blocks placed on it");

        revealStable(scene, util.select().position(torch), stableFocus);
        ElementLink<EntityElement> item = scene.world().createItemEntity(
            util.vector().topOf(torch).add(0, 1, 0), Vec3.ZERO, new ItemStack(Items.ENDER_PEARL));
        scene.world().moveEntity(item, util.vector().centerOf(platform.below()).add(0, -0.2, 0), 35,
            Easing.QUAD_IN, CollisionMode.IGNORE);
        scene.idle(40);
        finish(scene, util.select().position(platform),
            "Entities pass through the platform while the block above remains supported");
    }

    public static void infrangiblePlatform(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos platform = util.grid().at(2, 1, 2);
        beginStable(scene, util, "infrangible_platform_barrier", "An Indestructible Platform", platform,
            "The Infrangible Platform is a permanently solid, indestructible barrier");

        scene.overlay().showControls(util.vector().centerOf(platform), Pointing.RIGHT, 35)
            .withItem(new ItemStack(Items.NETHERITE_PICKAXE)).leftClick();
        for (int hit = 0; hit < 3; hit++) {
            scene.effects().emitParticles(util.vector().centerOf(platform),
                scene.effects().simpleParticleEmitter(ParticleTypes.SMOKE, Vec3.ZERO), 5, 1);
            scene.idle(12);
        }
        scene.overlay().showOutline(PonderPalette.RED, "unbroken", util.select().position(platform), 45);
        scene.idle(50);
        finish(scene, util.select().position(platform),
            "It never breaks and always collides, making it suitable for permanent boundaries");
    }

    public static void manastormCharge(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos charge = util.grid().at(2, 1, 3);
        BlockPos spreader = util.grid().at(2, 1, 0);
        Vec3 chargeCenter = util.vector().centerOf(charge);
        begin(scene, util, "manastorm_charge_activation", "Releasing a Manastorm", charge,
            "A Mana Burst striking a Manastorm Charge releases a violent storm of Mana Bursts");

        reveal(scene, util.select().position(spreader));
        ManaSpreaderScenes.aimSpreader(scene, spreader, charge);
        ManaSpreaderScenes.burstTrail(scene, util.vector().centerOf(spreader), chargeCenter);
        scene.world().destroyBlock(charge);
        for (int ring = 0; ring < 18; ring++) {
            double angle = ring * Math.PI * 2 / 18;
            Vec3 end = chargeCenter.add(Math.cos(angle) * 2.2, (ring % 3 - 1) * 0.45, Math.sin(angle) * 2.2);
            fastTrail(scene, chargeCenter, end, 3);
        }
        scene.effects().emitParticles(chargeCenter,
            scene.effects().simpleParticleEmitter(ParticleTypes.EXPLOSION, Vec3.ZERO), 2, 1);
        scene.overlay().showText(65,
                "The storm fires hundreds of random bursts before ending in a powerful explosion")
            .pointAt(chargeCenter).placeNearTarget().colored(PonderPalette.OUTPUT).attachKeyFrame();
        scene.idle(70);
        scene.markAsFinished();
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

    /** Keeps RevealSectionInstruction from recentering the camera as platform sections appear. */
    private static void beginStable(SceneBuilder scene, SceneBuildingUtil util, String id, String title,
                                    BlockPos subject, String description) {
        Vec3 stableFocus = util.vector().centerOf(subject);
        scene.title(id, title);
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        focus(scene, stableFocus);
        scene.idle(15);
        revealStable(scene, util.select().position(subject), stableFocus);
        scene.idle(10);
        scene.overlay().showOutlineWithText(util.select().position(subject), 65, description)
            .placeNearTarget().attachKeyFrame();
        scene.idle(80);
    }

    private static void reveal(SceneBuilder scene, Selection selection) {
        scene.world().showSection(selection, Direction.DOWN);
        scene.idle(20);
    }

    private static void revealStable(SceneBuilder scene, Selection selection, Vec3 stableFocus) {
        scene.world().showSection(selection, Direction.DOWN);
        focus(scene, stableFocus);
        scene.idle(20);
    }

    private static void finish(SceneBuilder scene, Selection target, String text) {
        scene.overlay().showOutlineWithText(target, 65, text)
            .placeNearTarget().colored(PonderPalette.OUTPUT).attachKeyFrame();
        scene.idle(70);
        scene.markAsFinished();
    }

    private static void fastTrail(SceneBuilder scene, Vec3 from, Vec3 to, int ticks) {
        ParticleEmitter emitter = greenEmitter(scene, 0.65F);
        for (int tick = 1; tick <= ticks; tick++) {
            scene.effects().emitParticles(from.lerp(to, tick / (double) ticks), emitter, 2, 1);
            scene.idle(1);
        }
    }

    private static void greenSparkles(SceneBuilder scene, Vec3 at, int count) {
        scene.effects().emitParticles(at, greenEmitter(scene, 0.8F), count, 2);
    }

    private static ParticleEmitter greenEmitter(SceneBuilder scene, float size) {
        SparkleParticleData data = SparkleParticleData.sparkle(size, 0.25F, 0.65F, 0.35F, 6);
        return scene.effects().simpleParticleEmitter(data, Vec3.ZERO);
    }

    private static void focus(SceneBuilder scene, Vec3 point) {
        scene.addInstruction(s -> s.setFocusPoint(point));
    }
}
