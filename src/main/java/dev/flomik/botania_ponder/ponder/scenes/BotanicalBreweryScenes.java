package dev.flomik.botania_ponder.ponder.scenes;

import dev.flomik.ponderlib.api.ParticleEmitter;
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import vazkii.botania.client.fx.SparkleParticleData;
import vazkii.botania.common.block.block_entity.BreweryBlockEntity;
import vazkii.botania.common.crafting.BotaniaRecipeTypes;
import vazkii.botania.common.item.brew.BaseBrewItem;

import java.util.List;
import java.util.Objects;

/** One focused scene: brewing one infusion in the Botanical Brewery. */
public final class BotanicalBreweryScenes {

    private static final int SPEED_BREW_MANA = 4000;
    private static final int SPEED_BREW_COLOR = 0x59B7FF;

    private BotanicalBreweryScenes() {
    }

    public static void brewing(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("botanical_brewery_brewing", "Brewing Botanical Infusions");
        scene.configureBasePlate(0, 0, 5);

        BlockPos breweryPos = util.grid().at(2, 1, 3);
        BlockPos spreaderPos = util.grid().at(2, 1, 0);
        Vec3 breweryCenter = util.vector().centerOf(breweryPos);
        Vec3 breweryTop = util.vector().topOf(breweryPos).add(0, 0.03, 0);
        Vec3 spreaderCenter = util.vector().centerOf(spreaderPos);

        scene.showBasePlate();
        focusOn(scene, breweryCenter);
        scene.idle(15);

        scene.world().showSection(util.select().position(breweryPos), Direction.DOWN);
        focusOn(scene, breweryCenter);
        scene.idle(10);
        scene.overlay().showOutlineWithText(util.select().position(breweryPos), 70,
                "The Botanical Brewery infuses special containers with powerful Brews")
            .placeNearTarget()
            .attachKeyFrame();
        scene.idle(75);

        List<ItemStack> ingredients = List.of(
            botaniaItem("vial"),
            new ItemStack(Items.NETHER_WART),
            new ItemStack(Items.SUGAR),
            new ItemStack(Items.REDSTONE)
        );
        scene.overlay().showText(88, "Add a Managlass Vial, then the ingredients for a Brew of Speed")
            .pointAt(breweryTop)
            .placeNearTarget()
            .colored(PonderPalette.INPUT);
        for (int slot = 0; slot < ingredients.size(); slot++) {
            dropIngredient(scene, breweryPos, breweryTop, ingredients.get(slot), slot);
        }
        scene.idle(6);

        scene.world().modifyBlockEntity(breweryPos, BreweryBlockEntity.class, brewery -> {
            brewery.recipe = brewery.getLevel().getRecipeManager()
                .getRecipeFor(BotaniaRecipeTypes.BREW_TYPE, brewery.getItemHandler(), brewery.getLevel())
                .orElse(null);
        });
        scene.world().modifyBlock(breweryPos,
            state -> state.setValue(BlockStateProperties.POWERED, true), false);
        scene.addKeyframe();

        scene.world().showSection(util.select().position(spreaderPos), Direction.DOWN);
        focusOn(scene, breweryCenter);
        scene.idle(10);
        scene.overlay().showText(60, "The Brewery consumes Mana only after a valid recipe is assembled")
            .pointAt(spreaderCenter)
            .placeNearTarget();
        scene.idle(65);

        for (int burst = 0; burst < 3; burst++) {
            ManaSpreaderScenes.burstTrail(scene, spreaderCenter, breweryCenter);
            fillBrewery(scene, breweryPos, burst == 2);
            coloredWisps(scene, breweryTop, 5);
            scene.idle(8);
        }

        completeBrew(scene, breweryPos, breweryTop);
        scene.idle(12);
        scene.overlay().showText(70, "Once enough Mana is supplied, the finished Brew is released")
            .pointAt(breweryTop.add(0, 0.25, 0))
            .placeNearTarget()
            .colored(PonderPalette.OUTPUT)
            .attachKeyFrame();
        scene.idle(75);

        scene.markAsFinished();
    }

    private static void dropIngredient(SceneBuilder scene, BlockPos breweryPos, Vec3 target,
                                       ItemStack ingredient, int slot) {
        Vec3 start = target.add(0, 1.25, 0);
        ElementLink<EntityElement> entity = scene.world().createItemEntity(start, Vec3.ZERO, ingredient.copy());
        scene.idle(18);
        scene.idle(2);
        scene.world().modifyEntity(entity, Entity::discard);
        scene.world().modifyBlockEntity(breweryPos, BreweryBlockEntity.class,
            brewery -> brewery.getItemHandler().setItem(slot, ingredient.copy()));
        scene.effects().emitSparks(target, 0x6D9077, 4);
        scene.idle(2);
    }

    private static void fillBrewery(SceneBuilder scene, BlockPos breweryPos, boolean finalBurst) {
        int duration = 12;
        for (int tick = 1; tick <= duration; tick++) {
            int currentTick = tick;
            scene.world().modifyBlockEntity(breweryPos, BreweryBlockEntity.class, brewery -> {
                int amount = finalBurst && currentTick == duration
                    ? brewery.getManaCost() - brewery.getCurrentMana()
                    : SPEED_BREW_MANA / 3 / duration;
                brewery.receiveMana(amount);
            });
            scene.idle(1);
        }
    }

    private static void completeBrew(SceneBuilder scene, BlockPos breweryPos, Vec3 breweryTop) {
        scene.world().modifyBlockEntity(breweryPos, BreweryBlockEntity.class, brewery -> {
            brewery.receiveMana(-brewery.getCurrentMana());
            brewery.getItemHandler().clearContent();
            brewery.recipe = null;
        });
        scene.world().modifyBlock(breweryPos,
            state -> state.setValue(BlockStateProperties.POWERED, false), false);
        coloredWisps(scene, breweryTop, 16);
        scene.effects().indicateSuccess(breweryPos);
        scene.world().createItemEntity(breweryTop.add(0, 0.35, 0), Vec3.ZERO, speedBrew());
    }

    private static void coloredWisps(SceneBuilder scene, Vec3 at, int count) {
        float r = (SPEED_BREW_COLOR >> 16 & 0xFF) / 255F;
        float g = (SPEED_BREW_COLOR >> 8 & 0xFF) / 255F;
        float b = (SPEED_BREW_COLOR & 0xFF) / 255F;
        SparkleParticleData data = SparkleParticleData.sparkle(1.1F, r, g, b, 8);
        ParticleEmitter emitter = scene.effects().particleEmitterWithinBlockSpace(data, Vec3.ZERO);
        scene.effects().emitParticles(at, emitter, count, 2);
    }

    private static ItemStack speedBrew() {
        ItemStack result = botaniaItem("brew_vial");
        BaseBrewItem.setBrew(result, ResourceLocation.fromNamespaceAndPath("botania", "speed"));
        return result;
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
