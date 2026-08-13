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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import vazkii.botania.api.state.BotaniaStateProperties;
import vazkii.botania.api.state.enums.AlfheimPortalState;
import vazkii.botania.client.fx.SparkleParticleData;
import vazkii.botania.common.block.block_entity.ManaEnchanterBlockEntity;
import vazkii.botania.common.block.block_entity.TerrestrialAgglomerationPlateBlockEntity;
import vazkii.botania.common.block.block_entity.mana.ManaPoolBlockEntity;
import vazkii.botania.common.handler.BotaniaSounds;

import java.util.List;
import java.util.Objects;

/** Focused scenes for Botania's advanced Mana crafting structures. */
public final class AdvancedCraftingScenes {

    private AdvancedCraftingScenes() {
    }

    public static void alchemyCatalyst(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos catalyst = util.grid().at(3, 1, 3);
        BlockPos pool = catalyst.above();
        Vec3 inside = util.vector().centerOf(pool).add(0, -0.35, 0);
        begin(scene, util, 7, "alchemy_catalyst_conversion", "Performing Mana Alchemy",
            catalyst, "An Alchemy Catalyst directly below a Mana Pool unlocks conversion recipes");
        reveal(scene, util, util.select().position(pool), pool, 60,
            "The Pool still performs ordinary Mana Infusion, but now checks Alchemy recipes too");
        fillPool(scene, pool, ManaPoolBlockEntity.MAX_MANA / 4);

        ElementLink<EntityElement> input = dropItem(scene, util, pool,
            new ItemStack(Items.ROTTEN_FLESH), inside);
        scene.world().modifyEntity(input, Entity::discard);
        scene.world().modifyBlockEntity(pool, ManaPoolBlockEntity.class, be -> be.receiveMana(-600));
        manaSparkle(scene, util.vector().centerOf(pool), 14);
        scene.world().createItemEntity(inside, Vec3.ZERO, new ItemStack(Items.LEATHER));
        finish(scene, util.select().position(pool),
            "This recipe converts Rotten Flesh into Leather and consumes 600 Mana");
    }

    public static void conjurationCatalyst(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos catalyst = util.grid().at(3, 1, 3);
        BlockPos pool = catalyst.above();
        Vec3 inside = util.vector().centerOf(pool).add(0, -0.35, 0);
        begin(scene, util, 7, "conjuration_catalyst_duplication", "Conjuring Simple Resources",
            catalyst, "A Conjuration Catalyst below a Mana Pool unlocks resource duplication recipes");
        reveal(scene, util, util.select().position(pool), pool, 60,
            "Conjuration spends a larger amount of Mana to create additional copies");
        fillPool(scene, pool, ManaPoolBlockEntity.MAX_MANA / 4);

        ElementLink<EntityElement> input = dropItem(scene, util, pool,
            new ItemStack(Items.REDSTONE), inside);
        scene.world().modifyEntity(input, Entity::discard);
        scene.world().modifyBlockEntity(pool, ManaPoolBlockEntity.class, be -> be.receiveMana(-5000));
        manaSparkle(scene, util.vector().centerOf(pool), 18);
        scene.world().createItemEntity(inside, Vec3.ZERO, new ItemStack(Items.REDSTONE, 2));
        finish(scene, util.select().position(pool),
            "One Redstone Dust becomes two at a cost of 5000 Mana");
    }

    public static void terraPlate(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos plate = util.grid().at(3, 1, 3);
        Vec3 plateTop = util.vector().topOf(plate).add(0, -0.3, 0);
        Selection platform = util.select().fromTo(2, 0, 2, 4, 0, 4);
        begin(scene, util, 7, "terra_plate_crafting", "Creating Terrasteel",
            plate, "The Terrestrial Agglomeration Plate crafts Terrasteel on a Livingrock and Lapis platform");
        reveal(scene, util, platform, plate, 65,
            "The complete 3x3 platform is required before the Plate can accept Mana");

        List<ItemStack> ingredients = List.of(
            botania("manasteel_ingot"), botania("mana_diamond"), botania("mana_pearl"));
        scene.overlay().showText(65, "Place one Manasteel Ingot, Mana Diamond, and Mana Pearl on the Plate")
            .pointAt(plateTop).placeNearTarget().colored(PonderPalette.INPUT).attachKeyFrame();
        for (ItemStack ingredient : ingredients) {
            scene.world().createItemEntity(plateTop.add(0, 0.2, 0), Vec3.ZERO, ingredient);
            scene.idle(14);
        }
        scene.idle(28);

        for (int tick = 0; tick < 40; tick++) {
            scene.world().modifyBlockEntity(plate, TerrestrialAgglomerationPlateBlockEntity.class,
                be -> be.receiveMana(ManaPoolBlockEntity.MAX_MANA / 2 / 40));
            scene.idle(1);
        }
        scene.world().modifyEntitiesInside(Entity.class, util.select().position(plate), Entity::discard);
        scene.effects().playSound(BotaniaSounds.terrasteelCraft, 1, 1);
        manaSparkle(scene, plateTop, 24);
        scene.world().createItemEntity(plateTop, Vec3.ZERO, botania("terrasteel_ingot"));
        finish(scene, util.select().position(plate),
            "Half a full Mana Pool is consumed to create one Terrasteel Ingot");
    }

    public static void manaEnchanter(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos enchanter = util.grid().at(5, 1, 5);
        Selection flowers = selectPositions(util, 1, new int[][] {
            { 1, 2 }, { 9, 2 }, { 4, 4 }, { 6, 4 }, { 0, 5 },
            { 10, 5 }, { 4, 6 }, { 6, 6 }, { 1, 8 }, { 9, 8 }
        });
        Selection pylons = selectPositions(util, 2, new int[][] {
            { 1, 2 }, { 9, 2 }, { 0, 5 }, { 10, 5 }, { 1, 8 }, { 9, 8 }
        });
        Selection structure = flowers.add(pylons);
        begin(scene, util, 11, "mana_enchanter_reusable_books", "Enchanting without Consuming Books",
            enchanter, "A formed Mana Enchanter applies enchantments while preserving the Enchanted Books");
        reveal(scene, util, structure, enchanter, 70,
            "Mana Pylons, Mystical Flowers, and the Obsidian ring must remain intact throughout the process");

        ItemStack tool = new ItemStack(Items.DIAMOND_PICKAXE);
        scene.overlay().showControls(util.vector().topOf(enchanter), Pointing.DOWN, 25)
            .withItem(tool).rightClick();
        scene.world().modifyBlockEntity(enchanter, ManaEnchanterBlockEntity.class,
            be -> be.itemToEnchant = tool.copy());
        ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
        EnchantedBookItem.addEnchantment(book, new EnchantmentInstance(Enchantments.BLOCK_EFFICIENCY, 4));
        Vec3 bookPos = util.vector().centerOf(enchanter).add(2.1, 0.3, 0);
        scene.world().createItemEntity(bookPos, Vec3.ZERO, book);
        scene.idle(35);
        scene.overlay().showText(60, "Drop the desired Enchanted Books inside the Obsidian ring, then use the Wand")
            .pointAt(bookPos).placeNearTarget().colored(PonderPalette.INPUT).attachKeyFrame();
        scene.idle(65);

        scene.world().modifyBlockEntity(enchanter, ManaEnchanterBlockEntity.class, be -> {
            be.stage = ManaEnchanterBlockEntity.State.GATHER_MANA;
            be.stageTicks = 20;
        });
        scene.effects().playSound(BotaniaSounds.enchanterForm, 1, 1);
        scene.idle(35);
        ItemStack enchanted = tool.copy();
        enchanted.enchant(Enchantments.BLOCK_EFFICIENCY, 4);
        scene.world().modifyBlockEntity(enchanter, ManaEnchanterBlockEntity.class, be -> {
            be.itemToEnchant = enchanted;
            be.stage = ManaEnchanterBlockEntity.State.DO_ENCHANT;
            be.stageTicks = 90;
        });
        scene.effects().playSound(BotaniaSounds.enchanterEnchant, 1, 1);
        manaSparkle(scene, util.vector().topOf(enchanter), 24);
        finish(scene, util.select().position(enchanter),
            "The item receives Efficiency IV while the Enchanted Book remains available for reuse");
    }

    public static void alfheimPortal(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos portal = util.grid().at(5, 1, 5);
        Selection frame = selectPositions(util, new int[][] {
            { 5, 1, 4 }, { 5, 1, 6 }, { 5, 2, 3 }, { 5, 2, 7 },
            { 5, 3, 3 }, { 5, 3, 7 }, { 5, 4, 3 }, { 5, 4, 7 },
            { 5, 5, 4 }, { 5, 5, 5 }, { 5, 5, 6 }
        });
        Selection power = selectPositions(util, new int[][] {
            { 3, 1, 2 }, { 3, 1, 8 }, { 3, 2, 2 }, { 3, 2, 8 }
        });
        begin(scene, util, 11, "alfheim_portal_trade", "Trading through the Alfheim Portal",
            portal, "The Elven Gateway Core opens a portal inside a completed Livingwood frame");
        reveal(scene, util, frame.add(power), portal, 70,
            "At least two Mana Pools topped with Natura Pylons pay the opening and trading costs");

        scene.overlay().showControls(util.vector().centerOf(portal), Pointing.DOWN, 25)
            .withItem(botania("twig_wand")).rightClick();
        scene.idle(28);
        scene.world().modifyBlock(portal,
            state -> state.setValue(BotaniaStateProperties.ALFPORTAL_STATE, AlfheimPortalState.ON_X), false);
        scene.idle(65);

        Vec3 opening = util.vector().centerOf(portal).add(0, 1.4, 0);
        ElementLink<EntityElement> diamond = scene.world().createItemEntity(
            opening.add(0, 0.8, 0), Vec3.ZERO, botania("mana_diamond"));
        scene.idle(25);
        scene.world().modifyEntity(diamond, Entity::discard);
        manaSparkle(scene, opening, 18);
        scene.world().createItemEntity(opening, Vec3.ZERO, botania("dragonstone"));
        finish(scene, frame,
            "A Mana Diamond sent through the active portal returns as a Dragonstone");
    }

    private static void begin(SceneBuilder scene, SceneBuildingUtil util, int size,
                              String id, String title, BlockPos subject, String text) {
        scene.title(id, title);
        scene.configureBasePlate(0, 0, size);
        if (size >= 11) {
            scene.scaleSceneView(0.65F);
            scene.setSceneOffsetY(-1F);
        }
        scene.showBasePlate();
        focus(scene, util.vector().centerOf(subject));
        scene.idle(15);
        scene.world().showSection(util.select().position(subject), Direction.DOWN);
        scene.idle(10);
        scene.overlay().showOutlineWithText(util.select().position(subject), 65, text)
            .placeNearTarget().attachKeyFrame();
        scene.idle(70);
    }

    private static void reveal(SceneBuilder scene, SceneBuildingUtil util, Selection selection,
                               BlockPos focus, int duration, String text) {
        scene.world().showSection(selection, Direction.DOWN);
        scene.idle(20);
        scene.overlay().showText(duration, text)
            .pointAt(util.vector().centerOf(focus)).placeNearTarget().attachKeyFrame();
        scene.idle(duration);
        scene.idle(5);
    }

    private static ElementLink<EntityElement> dropItem(SceneBuilder scene, SceneBuildingUtil util,
                                                        BlockPos pool, ItemStack stack, Vec3 target) {
        Vec3 start = util.vector().centerOf(pool).add(0, 1.1, 0);
        ElementLink<EntityElement> item = scene.world().createItemEntity(start, Vec3.ZERO, stack);
        scene.world().moveEntity(item, target, 24, Easing.QUAD_IN, CollisionMode.IGNORE);
        scene.idle(28);
        return item;
    }

    private static void fillPool(SceneBuilder scene, BlockPos pool, int mana) {
        scene.world().modifyBlockEntity(pool, ManaPoolBlockEntity.class, be -> be.receiveMana(mana));
        scene.idle(12);
    }

    private static void manaSparkle(SceneBuilder scene, Vec3 at, int count) {
        SparkleParticleData data = SparkleParticleData.sparkle(1.2F, 0.4F, 0.4F, 1F, 6);
        ParticleEmitter emitter = scene.effects().particleEmitterWithinBlockSpace(data, Vec3.ZERO);
        scene.effects().emitParticles(at, emitter, count, 2);
    }

    private static Selection selectPositions(SceneBuildingUtil util, int y, int[][] xz) {
        Selection result = util.select().position(util.grid().at(xz[0][0], y, xz[0][1]));
        for (int i = 1; i < xz.length; i++) {
            result = result.add(util.select().position(util.grid().at(xz[i][0], y, xz[i][1])));
        }
        return result;
    }

    private static Selection selectPositions(SceneBuildingUtil util, int[][] xyz) {
        Selection result = util.select().position(util.grid().at(xyz[0][0], xyz[0][1], xyz[0][2]));
        for (int i = 1; i < xyz.length; i++) {
            result = result.add(util.select().position(util.grid().at(xyz[i][0], xyz[i][1], xyz[i][2])));
        }
        return result;
    }

    private static ItemStack botania(String path) {
        Item item = Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(
            ResourceLocation.fromNamespaceAndPath("botania", path)));
        return new ItemStack(item);
    }

    private static void finish(SceneBuilder scene, Selection target, String text) {
        scene.idle(15);
        scene.overlay().showOutlineWithText(target, 70, text)
            .placeNearTarget().colored(PonderPalette.OUTPUT).attachKeyFrame();
        scene.idle(75);
        scene.markAsFinished();
    }

    private static void focus(SceneBuilder scene, Vec3 point) {
        scene.addInstruction(s -> s.setFocusPoint(point));
    }
}
