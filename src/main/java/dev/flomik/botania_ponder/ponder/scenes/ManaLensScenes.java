package dev.flomik.botania_ponder.ponder.scenes;

import dev.flomik.ponderlib.api.ParticleEmitter;
import dev.flomik.ponderlib.api.Pointing;
import dev.flomik.ponderlib.api.PonderPalette;
import dev.flomik.ponderlib.api.element.ElementLink;
import dev.flomik.ponderlib.api.element.EntityElement;
import dev.flomik.ponderlib.api.element.WorldSectionElement;
import dev.flomik.ponderlib.api.scene.CollisionMode;
import dev.flomik.ponderlib.api.scene.Easing;
import dev.flomik.ponderlib.api.scene.SceneBuilder;
import dev.flomik.ponderlib.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import vazkii.botania.client.fx.WispParticleData;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.block.block_entity.mana.ManaPoolBlockEntity;
import vazkii.botania.common.block.block_entity.mana.ManaSpreaderBlockEntity;

import java.util.Objects;

/** One focused scene for every standard Mana Lens. */
public final class ManaLensScenes {

    private static final int BURST_COLOR = 0x20FF20;

    private ManaLensScenes() {
    }

    public static void normal(SceneBuilder s, SceneBuildingUtil u) { lens(s, u, Kind.NORMAL); }
    public static void speed(SceneBuilder s, SceneBuildingUtil u) { lens(s, u, Kind.SPEED); }
    public static void power(SceneBuilder s, SceneBuildingUtil u) { lens(s, u, Kind.POWER); }
    public static void time(SceneBuilder s, SceneBuildingUtil u) { lens(s, u, Kind.TIME); }
    public static void efficiency(SceneBuilder s, SceneBuildingUtil u) { lens(s, u, Kind.EFFICIENCY); }
    public static void bounce(SceneBuilder s, SceneBuildingUtil u) { lens(s, u, Kind.BOUNCE); }
    public static void gravity(SceneBuilder s, SceneBuildingUtil u) { lens(s, u, Kind.GRAVITY); }
    public static void mine(SceneBuilder s, SceneBuildingUtil u) { lens(s, u, Kind.MINE); }
    public static void damage(SceneBuilder s, SceneBuildingUtil u) { lens(s, u, Kind.DAMAGE); }
    public static void phantom(SceneBuilder s, SceneBuildingUtil u) { lens(s, u, Kind.PHANTOM); }
    public static void magnet(SceneBuilder s, SceneBuildingUtil u) { lens(s, u, Kind.MAGNET); }
    public static void explosive(SceneBuilder s, SceneBuildingUtil u) { lens(s, u, Kind.EXPLOSIVE); }
    public static void influence(SceneBuilder s, SceneBuildingUtil u) { lens(s, u, Kind.INFLUENCE); }
    public static void weight(SceneBuilder s, SceneBuildingUtil u) { lens(s, u, Kind.WEIGHT); }
    public static void paint(SceneBuilder s, SceneBuildingUtil u) { lens(s, u, Kind.PAINT); }
    public static void fire(SceneBuilder s, SceneBuildingUtil u) { lens(s, u, Kind.FIRE); }
    public static void piston(SceneBuilder s, SceneBuildingUtil u) { lens(s, u, Kind.PISTON); }
    public static void light(SceneBuilder s, SceneBuildingUtil u) { lens(s, u, Kind.LIGHT); }
    public static void warp(SceneBuilder s, SceneBuildingUtil u) { lens(s, u, Kind.WARP); }
    public static void redirect(SceneBuilder s, SceneBuildingUtil u) { lens(s, u, Kind.REDIRECT); }
    public static void firework(SceneBuilder s, SceneBuildingUtil u) { lens(s, u, Kind.FIREWORK); }
    public static void flare(SceneBuilder s, SceneBuildingUtil u) { lens(s, u, Kind.FLARE); }
    public static void messenger(SceneBuilder s, SceneBuildingUtil u) { lens(s, u, Kind.MESSENGER); }
    public static void tripwire(SceneBuilder s, SceneBuildingUtil u) { lens(s, u, Kind.TRIPWIRE); }
    public static void storm(SceneBuilder s, SceneBuildingUtil u) { lens(s, u, Kind.STORM); }

    private static void lens(SceneBuilder scene, SceneBuildingUtil util, Kind kind) {
        BlockPos spreaderPos = util.grid().at(2, 1, 0);
        BlockPos targetPos = util.grid().at(2, 1, kind == Kind.PISTON ? 3 : 4);
        BlockPos resultPos = kind == Kind.PISTON ? targetPos.south() : targetPos;
        Vec3 spreader = util.vector().centerOf(spreaderPos);
        Vec3 target = util.vector().centerOf(targetPos);
        Vec3 result = util.vector().centerOf(resultPos);
        Vec3 focus = new Vec3(2.5, 1.5, 2.5);

        scene.title("mana_lens_" + kind.path, kind.title);
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        focusOn(scene, focus);
        scene.idle(15);
        scene.world().showSection(util.select().position(spreaderPos), Direction.DOWN);
        focusOn(scene, focus);
        scene.idle(10);

        scene.overlay().showControls(util.vector().topOf(spreaderPos), Pointing.DOWN, 45)
            .withItem(lensStack(kind.path)).rightClick();
        scene.overlay().showText(45, kind.description)
            .pointAt(spreader).placeNearTarget().colored(PonderPalette.INPUT).attachKeyFrame();
        scene.idle(50);
        scene.world().modifyBlockEntity(spreaderPos, ManaSpreaderBlockEntity.class,
            manaSpreader -> manaSpreader.getItemHandler().setItem(0, lensStack(kind.path)));
        scene.effects().indicateSuccess(spreaderPos);
        scene.idle(8);

        scene.world().showSection(util.select().position(targetPos), Direction.DOWN);
        focusOn(scene, focus);
        scene.idle(12);
        perform(scene, util, kind, spreaderPos, targetPos, spreader, target, focus);
        scene.idle(12);
        scene.overlay().showText(65, kind.result)
            .pointAt(kind == Kind.FLARE ? spreader : result)
            .placeNearTarget().colored(PonderPalette.OUTPUT).attachKeyFrame();
        scene.idle(70);
        scene.markAsFinished();
    }

    private static void perform(SceneBuilder scene, SceneBuildingUtil util, Kind kind,
                                BlockPos spreaderPos, BlockPos targetPos,
                                Vec3 spreader, Vec3 target, Vec3 focus) {
        switch (kind) {
            case NORMAL -> {
                trail(scene, spreader, target, 24);
                fillPool(scene, targetPos, ManaPoolBlockEntity.MAX_MANA / 5);
            }
            case SPEED -> {
                trail(scene, spreader, target, 12);
                fillPool(scene, targetPos, ManaPoolBlockEntity.MAX_MANA / 8);
            }
            case POWER -> {
                bigTrail(scene, spreader, target, 28);
                fillPool(scene, targetPos, ManaPoolBlockEntity.MAX_MANA / 3);
            }
            case TIME -> {
                trail(scene, spreader, target, 42);
                fillPool(scene, targetPos, ManaPoolBlockEntity.MAX_MANA / 6);
            }
            case EFFICIENCY -> {
                trail(scene, spreader, target, 30);
                fillPool(scene, targetPos, ManaPoolBlockEntity.MAX_MANA / 5);
            }
            case MESSENGER -> trail(scene, spreader, target, 8);
            case BOUNCE -> {
                trail(scene, spreader, target, 20);
                trail(scene, target, new Vec3(4.5, 1.5, 3.5), 18);
            }
            case GRAVITY -> curvedTrail(scene, spreader.add(0, 1.0, 0), target, 32, -0.1);
            case MINE -> {
                trail(scene, spreader, target, 24);
                scene.world().destroyBlock(targetPos);
                scene.world().createItemEntity(target.add(0, 0.2, 0), Vec3.ZERO,
                    new ItemStack(Items.COBBLESTONE));
            }
            case DAMAGE -> {
                Vec3 mobPos = util.vector().topOf(util.grid().at(2, 0, 3));
                ElementLink<EntityElement> cow = cow(scene, mobPos);
                scene.idle(8);
                trail(scene, spreader, mobPos.add(0, 0.7, 0), 20);
                scene.world().modifyEntity(cow, entity -> {
                    Cow animal = (Cow) entity;
                    animal.setHealth(animal.getHealth() - 8);
                    animal.hurtTime = 10;
                    animal.hurtDuration = 10;
                });
                scene.effects().emitParticles(mobPos.add(0, 0.8, 0),
                    scene.effects().simpleParticleEmitter(ParticleTypes.DAMAGE_INDICATOR, Vec3.ZERO), 6, 2);
            }
            case PHANTOM -> {
                trail(scene, spreader, target, 20);
                trail(scene, target, target.add(0, 0, 1.5), 12);
            }
            case MAGNET -> {
                Vec3 offCourse = new Vec3(1.5, 1.5, 2.5);
                trail(scene, spreader, offCourse, 14);
                curvedTrail(scene, offCourse, target, 22, 0.35);
            }
            case EXPLOSIVE, STORM -> {
                trail(scene, spreader, target, kind == Kind.STORM ? 16 : 24);
                scene.world().destroyBlock(targetPos);
                scene.effects().emitParticles(target,
                    scene.effects().particleEmitterWithinBlockSpace(ParticleTypes.EXPLOSION, Vec3.ZERO),
                    kind == Kind.STORM ? 10 : 5, 2);
            }
            case INFLUENCE -> {
                Vec3 crossing = new Vec3(2.5, 1.5, 2.5);
                ElementLink<EntityElement> item = scene.world().createItemEntity(
                    new Vec3(1.5, 1.2, 2.5), Vec3.ZERO, new ItemStack(Items.GOLD_INGOT));
                trail(scene, spreader, crossing, 14);
                scene.world().moveEntity(item, new Vec3(1.5, 1.2, 4.25), 18,
                    Easing.LINEAR, CollisionMode.IGNORE);
                trail(scene, crossing, target, 18);
                scene.idle(18);
            }
            case WEIGHT -> {
                BlockPos high = util.grid().at(2, 3, 4);
                scene.world().setBlock(high, Blocks.SAND.defaultBlockState());
                scene.world().showSection(util.select().position(high), Direction.DOWN);
                focusOn(scene, focus);
                scene.idle(12);
                trail(scene, spreader, util.vector().centerOf(high), 22);
                scene.world().setBlock(high, Blocks.AIR.defaultBlockState());
                scene.world().setBlock(targetPos.above(), Blocks.SAND.defaultBlockState());
                scene.effects().emitParticles(target.add(0, 1, 0),
                    scene.effects().simpleParticleEmitter(
                        new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SAND.defaultBlockState()),
                        new Vec3(0, -0.08, 0)), 6, 3);
            }
            case PAINT -> {
                trail(scene, spreader, target, 24);
                scene.world().setBlock(targetPos, Blocks.PINK_WOOL.defaultBlockState());
            }
            case FIRE -> {
                trail(scene, spreader, target, 24);
                // TODO(PONDERLIB-FIX): fire blocks currently render incorrectly inside captured
                // schematic sections. Recheck Kindle after the next PonderLib rendering update.
                scene.world().setBlock(targetPos.above(), Blocks.FIRE.defaultBlockState());
                scene.effects().emitParticles(target.add(0, 1, 0),
                    scene.effects().simpleParticleEmitter(ParticleTypes.FLAME, Vec3.ZERO), 7, 3);
                scene.overlay().showOutlineWithText(util.select().position(targetPos.above()), 45,
                        "The struck face is ignited")
                    .placeNearTarget().colored(PonderPalette.OUTPUT).attachKeyFrame();
                scene.idle(50);
            }
            case PISTON -> {
                scene.addKeyframe();
                trail(scene, spreader, target, 24);
                BlockPos pushedPos = targetPos.south();
                Vec3 pushedCenter = util.vector().centerOf(pushedPos);
                ElementLink<WorldSectionElement> pushed = scene.world().makeSectionIndependent(
                    util.select().position(targetPos));
                scene.world().moveSection(pushed, new Vec3(0, 0, 1), 20);
                scene.idle(20);

                // The moving section is only the animation. Commit the result to the virtual
                // world afterwards so timeline seeking and every later instruction see the block
                // at its new position instead of retaining the stale source coordinates.
                scene.addInstruction(s -> {
                    var pushedState = s.getLevel().getBlockState(targetPos);
                    s.setBlockState(targetPos, Blocks.AIR.defaultBlockState());
                    s.setBlockState(pushedPos, pushedState);
                });
                scene.world().showIndependentSectionImmediately(util.select().position(pushedPos));
                scene.effects().emitSparks(pushedCenter, 0x66836A, 6);
                scene.idle(15);
            }
            case LIGHT -> {
                trail(scene, spreader, target, 24);
                // TODO(PONDERLIB-FIX): Mana Flame has the same captured-section rendering issue
                // as vanilla fire. Recheck Flash after the next PonderLib rendering update.
                scene.world().setBlock(targetPos.above(), BotaniaBlocks.manaFlame.defaultBlockState());
                scene.effects().emitSparks(target.add(0, 1, 0), 0x80FFB0, 8);
                scene.overlay().showOutlineWithText(util.select().position(targetPos.above()), 45,
                        "A colored Mana Flame is placed on the hit face")
                    .placeNearTarget().colored(PonderPalette.OUTPUT).attachKeyFrame();
                scene.idle(50);
            }
            case WARP -> {
                Vec3 first = new Vec3(2.5, 1.5, 2.0);
                Vec3 exit = new Vec3(2.5, 1.5, 3.5);
                trail(scene, spreader, first, 12);
                scene.effects().emitParticles(first,
                    scene.effects().simpleParticleEmitter(ParticleTypes.PORTAL, Vec3.ZERO), 8, 2);
                scene.effects().emitParticles(exit,
                    scene.effects().simpleParticleEmitter(ParticleTypes.PORTAL, Vec3.ZERO), 8, 2);
                trail(scene, exit, target, 10);
            }
            case REDIRECT -> {
                Vec3 oldAim = target.add(-2, 0, 0);
                scene.overlay().showLine(PonderPalette.INPUT, target, oldAim, 28);
                scene.idle(28);
                trail(scene, spreader, target, 24);
                scene.world().modifyBlockEntity(targetPos, ManaSpreaderBlockEntity.class, other -> {
                    other.rotationX = (other.rotationX + 90F) % 360F;
                });
                scene.overlay().showBigLine(PonderPalette.OUTPUT, target, spreader, 45);
                scene.effects().emitSparks(target, 0x60D080, 8);
                scene.idle(45);
            }
            case FIREWORK -> {
                trail(scene, spreader, target, 24);
                scene.effects().emitParticles(target.add(0, 1, 0),
                    scene.effects().particleEmitterWithinBlockSpace(ParticleTypes.FIREWORK, Vec3.ZERO), 12, 5);
            }
            case FLARE -> {
                for (int tick = 0; tick < 32; tick++) {
                    scene.effects().emitParticles(spreader.add(0, 0.3, 0),
                        scene.effects().simpleParticleEmitter(ParticleTypes.END_ROD,
                            new Vec3(0, 0.04, 0.12)), 2, 1);
                    scene.idle(1);
                }
            }
            case TRIPWIRE -> {
                Vec3 left = new Vec3(1.25, 1, 2.5);
                Vec3 right = new Vec3(3.75, 1, 2.5);
                ElementLink<EntityElement> cow = cow(scene, left);
                scene.overlay().showLine(PonderPalette.INPUT, spreader, target, 50);
                scene.world().moveEntity(cow, right, 30, Easing.LINEAR, CollisionMode.RESPECT);
                scene.idle(30);
                trail(scene, spreader, target, 18);
            }
        }
        BlockPos resultPos = kind == Kind.PISTON ? targetPos.south() : targetPos;
        scene.effects().indicateSuccess(kind == Kind.FLARE ? spreaderPos : resultPos);
    }

    private static void fillPool(SceneBuilder scene, BlockPos target, int amount) {
        scene.world().modifyBlockEntity(target, ManaPoolBlockEntity.class, pool -> pool.receiveMana(amount));
    }

    private static ElementLink<EntityElement> cow(SceneBuilder scene, Vec3 position) {
        return scene.world().createEntity(level -> {
            Cow cow = new Cow(EntityType.COW, level);
            cow.setNoAi(true);
            cow.moveTo(position.x, position.y, position.z, -90, 0);
            return cow;
        });
    }

    private static void trail(SceneBuilder scene, Vec3 from, Vec3 to, int ticks) {
        ParticleEmitter emitter = burstEmitter(scene, 0.4F);
        for (int tick = 1; tick <= ticks; tick++) {
            scene.effects().emitParticles(from.lerp(to, tick / (double) ticks), emitter, 2, 1);
            scene.idle(1);
        }
    }

    private static void bigTrail(SceneBuilder scene, Vec3 from, Vec3 to, int ticks) {
        ParticleEmitter emitter = burstEmitter(scene, 0.75F);
        for (int tick = 1; tick <= ticks; tick++) {
            scene.effects().emitParticles(from.lerp(to, tick / (double) ticks), emitter, 4, 1);
            scene.idle(1);
        }
    }

    private static void curvedTrail(SceneBuilder scene, Vec3 from, Vec3 to, int ticks, double bend) {
        ParticleEmitter emitter = burstEmitter(scene, 0.4F);
        for (int tick = 1; tick <= ticks; tick++) {
            double progress = tick / (double) ticks;
            Vec3 point = from.lerp(to, progress).add(0, bend * progress * progress, 0);
            scene.effects().emitParticles(point, emitter, 2, 1);
            scene.idle(1);
        }
    }

    private static ParticleEmitter burstEmitter(SceneBuilder scene, float size) {
        float r = (BURST_COLOR >> 16 & 0xFF) / 255F;
        float g = (BURST_COLOR >> 8 & 0xFF) / 255F;
        float b = (BURST_COLOR & 0xFF) / 255F;
        return scene.effects().simpleParticleEmitter(WispParticleData.wisp(size, r, g, b), Vec3.ZERO);
    }

    private static ItemStack lensStack(String path) {
        Item item = Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(
            ResourceLocation.fromNamespaceAndPath("botania", "lens_" + path)));
        return new ItemStack(item);
    }

    private static void focusOn(SceneBuilder scene, Vec3 focus) {
        scene.addInstruction(s -> s.setFocusPoint(focus));
    }

    private enum Kind {
        NORMAL("normal", "Using a Basic Mana Lens", "A Mana Lens is installed directly onto a Mana Spreader",
            "The basic lens preserves the burst's normal speed, capacity, and Mana loss"),
        SPEED("speed", "Accelerating Bursts", "The Velocity Lens makes Mana Bursts travel twice as fast",
            "Higher speed reduces burst capacity and causes Mana to decay sooner"),
        POWER("power", "Increasing Burst Capacity", "The Potency Lens doubles the maximum Mana in each burst",
            "The stronger burst travels slightly slower and loses Mana more quickly"),
        TIME("time", "Delaying Mana Loss", "The Resistance Lens greatly delays when a burst begins losing Mana",
            "The longer-lived burst travels more slowly in exchange for its extended range"),
        EFFICIENCY("efficiency", "Reducing Mana Loss", "The Efficiency Lens cuts ongoing Mana loss to one fifth",
            "Bursts retain substantially more Mana over long journeys"),
        BOUNCE("bounce", "Bouncing Mana Bursts", "The Bounce Lens reflects a burst from solid surfaces",
            "A reflected burst continues travelling instead of disappearing on impact"),
        GRAVITY("gravity", "Curving Bursts with Gravity", "The Gravity Lens adds a downward pull to every burst",
            "The burst follows a falling arc rather than a perfectly straight line"),
        MINE("mine", "Mining Blocks with Mana", "The Bore Lens spends burst Mana to break the block it hits",
            "The mined block drops normally when the burst has enough Mana and harvest level"),
        DAMAGE("damage", "Damaging Creatures", "The Damaging Lens converts burst Mana into damage on contact",
            "Living creatures struck by the burst take magic damage"),
        PHANTOM("phantom", "Passing through Blocks", "The Phantom Lens lets bursts continue through ordinary blocks",
            "Mana receivers still stop and accept the otherwise intangible burst"),
        MAGNET("magnet", "Homing Mana Bursts", "The Magnetizing Lens searches for nearby non-full Mana receivers",
            "The burst curves toward a valid receiver within three blocks of its path"),
        EXPLOSIVE("explosive", "Exploding on Impact", "The Entropic Lens turns remaining burst Mana into an explosion",
            "The burst is consumed and detonates when it strikes a solid block"),
        INFLUENCE("influence", "Influencing Moving Objects", "The Influence Lens shares its motion with nearby movable entities",
            "Items, arrows, falling blocks, primed TNT, and ordinary bursts copy its direction"),
        WEIGHT("weight", "Making Blocks Fall", "The Weight Lens applies gravity to supported blocks it strikes",
            "Affected blocks fall until they reach a solid surface"),
        PAINT("paint", "Painting Blocks", "A dyed Paintslinger Lens recolors compatible blocks on impact",
            "The struck block adopts the lens's stored dye color"),
        FIRE("fire", "Igniting with Mana", "The Kindle Lens sets creatures and suitable block faces on fire",
            "The impact creates fire without requiring Flint and Steel"),
        PISTON("piston", "Pushing Blocks", "The Force Lens pushes a movable block from the impact face",
            "The affected block moves as if driven by a piston"),
        LIGHT("light", "Placing Mana Flames", "The Flash Lens creates a colored Mana Flame on impact",
            "The Mana Flame provides light and keeps the burst's color"),
        WARP("warp", "Warping Mana Bursts", "The Warp Lens teleports a burst through a linked Force Relay",
            "The burst reappears at the paired Relay and resumes its original trajectory"),
        REDIRECT("redirect", "Redirecting Spreaders", "The Redirective Lens turns the Spreader that it strikes",
            "The struck Spreader aims back toward the source of the burst"),
        FIREWORK("firework", "Launching Fireworks", "The Celebratory Lens releases a Firework Rocket on impact",
            "The burst is consumed and its configured firework effect is launched"),
        FLARE("flare", "Emitting Decorative Flares", "The Flare Lens replaces Mana Bursts with a stream of colored particles",
            "The controlled Spreader becomes a reusable decorative particle emitter"),
        MESSENGER("messenger", "Sending Fast Signals", "The Messenger Lens triples burst speed and lifetime before decay",
            "It carries very little Mana, making it best suited to triggers and signals"),
        TRIPWIRE("tripwire", "Detecting Creatures", "The Tripwire Lens simulates its path and waits for a creature to cross it",
            "The Spreader fires only after the projected beam detects a living creature"),
        STORM("storm", "Unleashing a Mana Storm", "The Storm Lens creates an exceptionally powerful impact explosion",
            "This rare lens consumes the burst in a large destructive blast");

        final String path;
        final String title;
        final String description;
        final String result;

        Kind(String path, String title, String description, String result) {
            this.path = path;
            this.title = title;
            this.description = description;
            this.result = result;
        }
    }
}
