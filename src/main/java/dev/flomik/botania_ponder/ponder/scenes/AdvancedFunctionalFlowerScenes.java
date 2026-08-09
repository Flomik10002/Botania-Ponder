package dev.flomik.botania_ponder.ponder.scenes;

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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import vazkii.botania.api.block_entity.FunctionalFlowerBlockEntity;

import java.util.Objects;

/** The remaining standard Functional Flora, with one focused mechanic per scene. */
public final class AdvancedFunctionalFlowerScenes {

    private AdvancedFunctionalFlowerScenes() {
    }

    public static void bergamute(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos flowerPos = util.grid().at(2, 1, 2);
        BlockPos noteBlockPos = util.grid().at(2, 1, 4);
        Vec3 note = util.vector().centerOf(noteBlockPos).add(0, 0.75, 0);
        begin(scene, util, "bergamute_silence", "Silencing Sounds with Bergamute", flowerPos,
            "Bergamute suppresses sounds in a four-block radius without consuming Mana");

        scene.world().showSection(util.select().position(noteBlockPos), Direction.DOWN);
        focusOn(scene, center(util, flowerPos));
        scene.idle(12);
        scene.overlay().showText(55, "Sounds made inside the flower's range are intercepted")
            .pointAt(note).placeNearTarget().colored(PonderPalette.INPUT).attachKeyFrame();
        scene.effects().emitParticles(note,
            scene.effects().simpleParticleEmitter(ParticleTypes.NOTE, Vec3.ZERO), 5, 3);
        scene.idle(60);

        scene.effects().emitParticles(note,
            scene.effects().simpleParticleEmitter(ParticleTypes.SMOKE, Vec3.ZERO), 4, 2);
        scene.effects().emitSparks(center(util, flowerPos), 0x6D8672, 5);
        scene.idle(10);
        finish(scene, util.select().fromTo(flowerPos, noteBlockPos),
            "The sound is muted; a Redstone signal can temporarily disable the Bergamute");
    }

    public static void bubbell(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos flowerPos = util.grid().at(2, 1, 2);
        Selection water = util.select().fromTo(1, 1, 1, 3, 1, 3)
            .subtract(util.select().position(flowerPos));
        begin(scene, util, "bubbell_air", "Creating an Air Bubble with Bubbell", flowerPos,
            "Bubbell spends four Mana each tick to keep water out of a growing sphere");

        scene.world().showSection(water, Direction.DOWN);
        focusOn(scene, center(util, flowerPos));
        scene.idle(25);
        scene.overlay().showOutlineWithText(water, 55,
                "Water is left visible first so the affected volume is clear")
            .placeNearTarget().colored(PonderPalette.INPUT).attachKeyFrame();
        scene.idle(60);

        addMana(scene, flowerPos, 320);
        for (int radius = 0; radius < 2; radius++) {
            Selection ring = radius == 0
                ? util.select().fromTo(1, 1, 2, 3, 1, 2).subtract(util.select().position(flowerPos))
                : water;
            scene.world().modifyBlocks(ring, ignored -> botaniaBlock("fake_air").defaultBlockState(), false);
            scene.world().modifyBlockEntity(flowerPos, FunctionalFlowerBlockEntity.class,
                flower -> flower.addMana(-80));
            scene.effects().emitSparks(center(util, flowerPos), 0x668B83, 6);
            scene.idle(20);
        }
        scene.effects().indicateSuccess(flowerPos);
        scene.idle(8);
        finish(scene, water, "The resulting bubble contains breathable air while Mana remains available");
    }

    public static void heiseiDream(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos flowerPos = util.grid().at(2, 1, 2);
        Vec3 firstPos = util.vector().topOf(util.grid().at(1, 0, 1));
        Vec3 secondPos = util.vector().topOf(util.grid().at(4, 0, 1));
        begin(scene, util, "heisei_dream_conflict", "Turning Monsters with Heisei Dream", flowerPos,
            "Heisei Dream spends Mana to make nearby hostile mobs attack each other");

        ElementLink<EntityElement> first = zombie(scene, firstPos, -90);
        ElementLink<EntityElement> second = zombie(scene, secondPos, 90);
        scene.overlay().showText(55, "At least two hostile mobs must be inside the working range")
            .pointAt(firstPos.add(0, 1, 0)).placeNearTarget()
            .colored(PonderPalette.INPUT).attachKeyFrame();
        scene.idle(60);

        addMana(scene, flowerPos, 100);
        scene.world().moveEntity(first, new Vec3(2.15, 1, 1.5), 24, Easing.QUAD_IN, CollisionMode.RESPECT);
        scene.world().moveEntity(second, new Vec3(2.85, 1, 1.5), 24, Easing.QUAD_IN, CollisionMode.RESPECT);
        scene.idle(24);
        scene.world().modifyEntity(second, entity -> hurt((Zombie) entity, 4));
        scene.world().modifyEntity(first, entity -> ((Zombie) entity).swing(InteractionHand.MAIN_HAND));
        scene.world().modifyBlockEntity(flowerPos, FunctionalFlowerBlockEntity.class,
            flower -> flower.addMana(-100));
        scene.effects().emitParticles(new Vec3(2.5, 2, 1.5),
            scene.effects().simpleParticleEmitter(ParticleTypes.DAMAGE_INDICATOR, Vec3.ZERO), 6, 2);
        scene.idle(12);
        finish(scene, util.select().position(flowerPos),
            "One monster is brainwashed into choosing another hostile mob as its target");
    }

    public static void hyacidus(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos flowerPos = util.grid().at(2, 1, 2);
        Vec3 cowPos = util.vector().topOf(util.grid().at(1, 0, 2));
        begin(scene, util, "hyacidus_poison", "Poisoning Creatures with Hyacidus", flowerPos,
            "Hyacidus spends Mana to poison nearby non-player creatures");

        ElementLink<EntityElement> cow = cow(scene, cowPos);
        scene.overlay().showText(55, "A valid creature inside six blocks is selected")
            .pointAt(cowPos.add(0, 0.8, 0)).placeNearTarget()
            .colored(PonderPalette.INPUT).attachKeyFrame();
        scene.idle(60);

        addMana(scene, flowerPos, 20);
        scene.world().modifyEntity(cow, entity ->
            ((Cow) entity).addEffect(new MobEffectInstance(MobEffects.POISON, 60)));
        spendMana(scene, flowerPos, 20);
        scene.effects().emitParticles(cowPos.add(0, 0.8, 0),
            scene.effects().simpleParticleEmitter(ParticleTypes.ENTITY_EFFECT, Vec3.ZERO), 8, 3);
        scene.idle(35);
        finish(scene, util.select().position(flowerPos),
            "Each affected creature receives three seconds of Poison for 20 Mana");
    }

    public static void labellia(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos flowerPos = util.grid().at(2, 1, 2);
        Vec3 cowPos = util.vector().topOf(util.grid().at(3, 0, 2));
        Vec3 tagPos = center(util, flowerPos).add(0, 0.75, 0);
        begin(scene, util, "labellia_rename", "Renaming Creatures with Labellia", flowerPos,
            "Drop a renamed Name Tag directly onto Labellia to apply that name nearby");

        ElementLink<EntityElement> cow = cow(scene, cowPos);
        ItemStack tag = new ItemStack(Items.NAME_TAG);
        tag.setHoverName(Component.literal("Daisy"));
        ElementLink<EntityElement> droppedTag = scene.world().createItemEntity(tagPos, Vec3.ZERO, tag);
        scene.overlay().showText(55, "The custom name is read from the dropped Name Tag")
            .pointAt(tagPos).placeNearTarget().colored(PonderPalette.INPUT).attachKeyFrame();
        scene.idle(60);

        addMana(scene, flowerPos, 500);
        scene.world().modifyEntity(droppedTag, Entity::discard);
        scene.world().modifyEntity(cow, entity -> {
            entity.setCustomName(Component.literal("Daisy"));
            entity.setCustomNameVisible(true);
        });
        spendMana(scene, flowerPos, 500);
        scene.effects().emitSparks(cowPos.add(0, 0.8, 0), 0x76977B, 8);
        scene.overlay().showText(55, "Daisy")
            .pointAt(cowPos.add(0, 1.55, 0))
            .placeNearTarget()
            .colored(PonderPalette.OUTPUT)
            .attachKeyFrame();
        scene.idle(60);
        finish(scene, util.select().position(flowerPos),
            "The tag is consumed and nearby creatures receive its name for 500 Mana");
    }

    public static void loonium(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos flowerPos = util.grid().at(2, 1, 2);
        Vec3 spawnPos = util.vector().topOf(util.grid().at(4, 0, 2));
        begin(scene, util, "loonium_spawn", "Summoning Loot Mobs with Loonium", flowerPos,
            "Loonium consumes a large Mana burst to summon a configured hostile mob");

        addMana(scene, flowerPos, 35000);
        scene.overlay().showText(55, "The chosen structure and loot table depend on the flower's configuration")
            .pointAt(center(util, flowerPos)).placeNearTarget()
            .colored(PonderPalette.INPUT).attachKeyFrame();
        scene.idle(60);

        scene.effects().emitParticles(spawnPos.add(0, 0.8, 0),
            scene.effects().simpleParticleEmitter(ParticleTypes.WITCH, Vec3.ZERO), 12, 4);
        scene.world().createEntity(level -> {
            Zombie mob = new Husk(EntityType.HUSK, level);
            mob.setNoAi(true);
            mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.GOLD_INGOT));
            mob.moveTo(spawnPos.x, spawnPos.y, spawnPos.z, 90, 0);
            return mob;
        });
        spendMana(scene, flowerPos, 35000);
        scene.idle(20);
        finish(scene, util.select().position(flowerPos),
            "The special loot is awarded only when a player kills the summoned mob");
    }

    public static void marimorphosis(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos flowerPos = util.grid().at(2, 1, 1);
        BlockPos chosen = util.grid().at(2, 1, 3);
        Selection stone = util.select().fromTo(1, 1, 3, 3, 1, 3);
        begin(scene, util, "marimorphosis_stone", "Forming Metamorphic Stone", flowerPos,
            "Marimorphosis spends Mana to convert nearby Stone into a biome-themed variant");

        revealRow(scene, util, center(util, flowerPos), 3);
        scene.overlay().showOutlineWithText(stone, 55,
                "The output variant is selected from the biome at the flower")
            .placeNearTarget().colored(PonderPalette.INPUT).attachKeyFrame();
        scene.idle(60);

        addMana(scene, flowerPos, 12);
        scene.world().modifyBlock(chosen,
            ignored -> botaniaBlock("metamorphic_forest_stone").defaultBlockState(), true);
        spendMana(scene, flowerPos, 12);
        scene.effects().indicateSuccess(chosen);
        scene.idle(12);
        finish(scene, util.select().position(chosen),
            "One Stone block becomes the metamorphic stone associated with the current biome");
    }

    public static void medumone(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos flowerPos = util.grid().at(2, 1, 2);
        Vec3 zombiePos = util.vector().topOf(util.grid().at(1, 0, 2));
        begin(scene, util, "medumone_stasis", "Stopping Creatures with Medumone", flowerPos,
            "Medumone continuously spends Mana to hold nearby non-player creatures still");

        ElementLink<EntityElement> zombie = zombie(scene, zombiePos, -90);
        scene.overlay().showText(55, "The flower refreshes an extreme Slowness effect every tick")
            .pointAt(zombiePos.add(0, 1, 0)).placeNearTarget()
            .colored(PonderPalette.INPUT).attachKeyFrame();
        scene.idle(60);

        addMana(scene, flowerPos, 40);
        scene.world().modifyEntity(zombie, entity ->
            ((Zombie) entity).addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 100)));
        for (int tick = 0; tick < 30; tick++) {
            spendMana(scene, flowerPos, 1);
            if (tick % 6 == 0) {
                scene.effects().emitSparks(zombiePos.add(0, 1, 0), 0x6A8091, 2);
            }
            scene.idle(1);
        }
        finish(scene, util.select().position(flowerPos),
            "Each held creature costs one Mana per tick until it leaves the six-block range");
    }

    public static void orechidIgnem(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos flowerPos = util.grid().at(2, 1, 1);
        BlockPos chosen = util.grid().at(2, 1, 3);
        Selection netherrack = util.select().fromTo(1, 1, 3, 3, 1, 3);
        begin(scene, util, "orechid_ignem_ore", "Transmuting Netherrack with Orechid Ignem", flowerPos,
            "Orechid Ignem spends 20,000 Mana to transform nearby Netherrack");

        revealRow(scene, util, center(util, flowerPos), 3);
        scene.overlay().showOutlineWithText(netherrack, 55,
                "This flower only operates in dimensions with a ceiling, such as the Nether")
            .placeNearTarget().colored(PonderPalette.INPUT).attachKeyFrame();
        scene.idle(60);

        addMana(scene, flowerPos, 20000);
        scene.world().modifyBlock(chosen, ignored -> Blocks.NETHER_QUARTZ_ORE.defaultBlockState(), true);
        spendMana(scene, flowerPos, 20000);
        scene.effects().indicateSuccess(chosen);
        scene.idle(12);
        finish(scene, util.select().position(chosen),
            "The selected Netherrack is replaced by an ore from the configured Nether pool");
    }

    public static void rannuncarpus(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos flowerPos = util.grid().at(2, 2, 2);
        BlockPos target = util.grid().at(3, 2, 2);
        Vec3 itemPos = center(util, flowerPos).add(-0.8, 0.35, 0);
        begin(scene, util, "rannuncarpus_placement", "Placing Blocks with Rannuncarpus", flowerPos,
            "Rannuncarpus takes dropped Block Items and places them on matching surfaces");

        ElementLink<EntityElement> item = scene.world().createItemEntity(itemPos, Vec3.ZERO,
            new ItemStack(Items.COBBLESTONE));
        scene.overlay().showText(55, "The hidden block beneath the flower acts as its placement filter")
            .pointAt(center(util, util.grid().at(2, 0, 2))).placeNearTarget()
            .colored(PonderPalette.INPUT).attachKeyFrame();
        scene.idle(60);

        addMana(scene, flowerPos, 1);
        scene.world().modifyEntity(item, Entity::discard);
        scene.world().setBlock(target, Blocks.COBBLESTONE.defaultBlockState());
        scene.world().showSection(util.select().position(target), Direction.DOWN);
        focusOn(scene, center(util, flowerPos));
        spendMana(scene, flowerPos, 1);
        scene.effects().indicateSuccess(target);
        scene.idle(12);
        finish(scene, util.select().position(target),
            "With Mana, the placement range grows from six to eight blocks and each block costs one Mana");
    }

    public static void solegnolia(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos flowerPos = util.grid().at(2, 1, 2);
        Vec3 itemPos = util.vector().topOf(util.grid().at(1, 0, 2)).add(0, 0.15, 0);
        begin(scene, util, "solegnolia_magnet", "Suppressing Magnets with Solegnolia", flowerPos,
            "Solegnolia disables Botania's item magnetization within five blocks");

        scene.world().createItemEntity(itemPos, Vec3.ZERO, new ItemStack(Items.DIAMOND));
        scene.overlay().showText(55, "Loose items inside the field are no longer pulled toward a magnet wearer")
            .pointAt(itemPos).placeNearTarget().colored(PonderPalette.INPUT).attachKeyFrame();
        scene.idle(60);
        scene.effects().emitParticles(itemPos,
            scene.effects().simpleParticleEmitter(ParticleTypes.SMOKE, Vec3.ZERO), 5, 2);
        scene.effects().emitSparks(center(util, flowerPos), 0x7D8F83, 6);
        scene.idle(12);
        finish(scene, util.select().position(flowerPos),
            "The suppression field consumes no Mana and can be disabled with Redstone");
    }

    public static void spectranthemum(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos flowerPos = util.grid().at(1, 1, 2);
        BlockPos targetPos = util.grid().at(4, 1, 2);
        Vec3 itemStart = center(util, flowerPos).add(0.8, 0.45, 0);
        Vec3 itemEnd = center(util, targetPos).add(0, 0.8, 0);
        begin(scene, util, "spectranthemum_teleport", "Teleporting Items with Spectranthemum", flowerPos,
            "A bound Spectranthemum teleports nearby loose items to its target position");

        scene.world().showSection(util.select().position(targetPos), Direction.DOWN);
        focusOn(scene, center(util, flowerPos));
        scene.idle(12);
        ElementLink<EntityElement> item = scene.world().createItemEntity(itemStart, Vec3.ZERO,
            new ItemStack(Items.ENDER_PEARL));
        scene.overlay().showText(55, "Bind the flower to the destination with the Wand of the Forest")
            .pointAt(itemEnd).placeNearTarget().colored(PonderPalette.INPUT).attachKeyFrame();
        scene.idle(60);

        addMana(scene, flowerPos, 12);
        scene.effects().emitParticles(itemStart,
            scene.effects().simpleParticleEmitter(ParticleTypes.PORTAL, Vec3.ZERO), 10, 3);
        scene.world().moveEntity(item, itemEnd, 1, Easing.LINEAR, CollisionMode.IGNORE);
        scene.idle(1);
        spendMana(scene, flowerPos, 12);
        scene.effects().emitParticles(itemEnd,
            scene.effects().simpleParticleEmitter(ParticleTypes.PORTAL, Vec3.ZERO), 10, 3);
        scene.idle(12);
        finish(scene, util.select().position(targetPos),
            "Mana cost scales with item count and the distance to the bound destination");
    }

    public static void tigerseye(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos flowerPos = util.grid().at(2, 1, 2);
        Vec3 creeperPos = util.vector().topOf(util.grid().at(1, 0, 1));
        Vec3 retreat = util.vector().topOf(util.grid().at(4, 0, 1));
        begin(scene, util, "tigerseye_creeper", "Repelling Creepers with Tigerseye", flowerPos,
            "Tigerseye spends Mana to pacify nearby Creepers before they can explode");

        ElementLink<EntityElement> creeper = scene.world().createEntity(level -> {
            Creeper mob = new Creeper(EntityType.CREEPER, level);
            mob.setNoAi(true);
            mob.moveTo(creeperPos.x, creeperPos.y, creeperPos.z, -90, 0);
            return mob;
        });
        scene.overlay().showText(55, "An ignited Creeper inside ten blocks is immediately pacified")
            .pointAt(creeperPos.add(0, 1, 0)).placeNearTarget()
            .colored(PonderPalette.INPUT).attachKeyFrame();
        scene.world().modifyEntity(creeper, entity -> ((Creeper) entity).setSwellDir(1));
        scene.effects().emitParticles(creeperPos.add(0, 0.8, 0),
            scene.effects().simpleParticleEmitter(ParticleTypes.SMOKE, Vec3.ZERO), 5, 2);
        scene.idle(12);

        addMana(scene, flowerPos, 70);
        scene.world().modifyEntity(creeper, entity -> ((Creeper) entity).setSwellDir(-1));
        scene.effects().emitSparks(center(util, flowerPos), 0x987A55, 8);
        scene.world().moveEntity(creeper, retreat, 30, Easing.QUAD_OUT, CollisionMode.RESPECT);
        scene.idle(30);
        spendMana(scene, flowerPos, 70);
        finish(scene, util.select().position(flowerPos),
            "The Creeper stops swelling and flees from players at a cost of 70 Mana");
    }

    public static void vinculotus(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos flowerPos = util.grid().at(2, 1, 2);
        Vec3 start = util.vector().topOf(util.grid().at(0, 0, 2));
        Vec3 destination = util.vector().topOf(util.grid().at(3, 0, 2));
        begin(scene, util, "vinculotus_teleport", "Redirecting Endermen with Vinculotus", flowerPos,
            "Vinculotus intercepts Enderman teleports and redirects them beside the flower");

        ElementLink<EntityElement> enderman = scene.world().createEntity(level -> {
            EnderMan mob = new EnderMan(EntityType.ENDERMAN, level);
            mob.setNoAi(true);
            mob.moveTo(start.x, start.y, start.z, -90, 0);
            return mob;
        });
        scene.overlay().showText(55, "Teleport attempts can be intercepted from as far as 64 blocks away")
            .pointAt(start.add(0, 1.5, 0)).placeNearTarget()
            .colored(PonderPalette.INPUT).attachKeyFrame();
        scene.idle(60);

        addMana(scene, flowerPos, 50);
        scene.effects().emitParticles(start.add(0, 1, 0),
            scene.effects().simpleParticleEmitter(ParticleTypes.PORTAL, Vec3.ZERO), 14, 3);
        scene.world().moveEntity(enderman, destination, 1, Easing.LINEAR, CollisionMode.IGNORE);
        scene.idle(1);
        spendMana(scene, flowerPos, 50);
        scene.effects().emitParticles(destination.add(0, 1, 0),
            scene.effects().simpleParticleEmitter(ParticleTypes.PORTAL, Vec3.ZERO), 14, 3);
        scene.idle(12);
        finish(scene, util.select().position(flowerPos),
            "The Enderman arrives within two blocks of the flower for 50 Mana");
    }

    private static void begin(SceneBuilder scene, SceneBuildingUtil util, String id, String title,
                              BlockPos flowerPos, String description) {
        scene.title(id, title);
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        focusOn(scene, center(util, flowerPos));
        scene.idle(15);
        Selection flowerColumn = flowerPos.getY() > 1
            ? util.select().fromTo(flowerPos.below(), flowerPos)
            : util.select().position(flowerPos);
        scene.world().showSection(flowerColumn, Direction.DOWN);
        focusOn(scene, center(util, flowerPos));
        scene.idle(10);
        scene.overlay().showOutlineWithText(util.select().position(flowerPos), 65, description)
            .placeNearTarget().attachKeyFrame();
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

    private static ElementLink<EntityElement> zombie(SceneBuilder scene, Vec3 position, float yaw) {
        return scene.world().createEntity(level -> {
            Zombie mob = new Husk(EntityType.HUSK, level);
            mob.setNoAi(true);
            mob.moveTo(position.x, position.y, position.z, yaw, 0);
            return mob;
        });
    }

    private static ElementLink<EntityElement> cow(SceneBuilder scene, Vec3 position) {
        return scene.world().createEntity(level -> {
            Cow animal = new Cow(EntityType.COW, level);
            animal.setNoAi(true);
            animal.moveTo(position.x, position.y, position.z, -90, 0);
            return animal;
        });
    }

    private static void hurt(Zombie zombie, float amount) {
        zombie.setHealth(zombie.getHealth() - amount);
        zombie.hurtTime = 10;
        zombie.hurtDuration = 10;
    }

    private static void addMana(SceneBuilder scene, BlockPos flowerPos, int amount) {
        scene.world().modifyBlockEntity(flowerPos, FunctionalFlowerBlockEntity.class,
            flower -> flower.addMana(amount));
    }

    private static void spendMana(SceneBuilder scene, BlockPos flowerPos, int amount) {
        scene.world().modifyBlockEntity(flowerPos, FunctionalFlowerBlockEntity.class,
            flower -> flower.addMana(-amount));
    }

    private static void finish(SceneBuilder scene, Selection target, String text) {
        scene.overlay().showOutlineWithText(target, 65, text)
            .placeNearTarget().colored(PonderPalette.OUTPUT).attachKeyFrame();
        scene.idle(70);
        scene.markAsFinished();
    }

    private static Block botaniaBlock(String path) {
        return Objects.requireNonNull(ForgeRegistries.BLOCKS.getValue(
            ResourceLocation.fromNamespaceAndPath("botania", path)));
    }

    private static Vec3 center(SceneBuildingUtil util, BlockPos pos) {
        return util.vector().centerOf(pos);
    }

    private static void focusOn(SceneBuilder scene, Vec3 subject) {
        scene.addInstruction(s -> s.setFocusPoint(subject));
    }
}
