package dev.flomik.botania_ponder.ponder.scenes;

import dev.flomik.ponderlib.api.Pointing;
import dev.flomik.ponderlib.api.PonderPalette;
import dev.flomik.ponderlib.api.scene.SceneBuilder;
import dev.flomik.ponderlib.api.scene.SceneBuildingUtil;
import dev.flomik.ponderlib.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import vazkii.botania.common.block.block_entity.corporea.CorporeaCrystalCubeBlockEntity;
import vazkii.botania.common.entity.BotaniaEntities;
import vazkii.botania.common.entity.CorporeaSparkEntity;

/** One-function scenes for the five core Corporea request blocks. */
public final class CorporeaScenes {

    private static final BlockPos MASTER = new BlockPos(1, 1, 3);
    private static final BlockPos STORAGE = new BlockPos(5, 1, 5);
    private static final Vec3 FOCUS = new Vec3(3.5, 1.7, 3.5);

    private CorporeaScenes() {
    }

    public static void index(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos indexPos = util.grid().at(3, 1, 2);
        begin(scene, util, "corporea_index_request", "Using the Corporea Index", indexPos,
            "The Corporea Index converts nearby chat messages into item requests");
        showNetwork(scene, util, indexPos, STORAGE);
        scene.world().modifyBlockEntity(STORAGE, ChestBlockEntity.class,
            chest -> chest.setItem(0, new ItemStack(Items.IRON_INGOT, 48)));

        scene.overlay().showText(55, "Say: 16 iron ingots")
            .pointAt(util.vector().centerOf(indexPos)).placeNearTarget()
            .colored(PonderPalette.INPUT).attachKeyFrame();
        scene.idle(60);

        scene.idle(12);
        scene.world().modifyBlockEntity(STORAGE, ChestBlockEntity.class,
            chest -> chest.setItem(0, new ItemStack(Items.IRON_INGOT, 32)));
        scene.world().createItemEntity(util.vector().centerOf(indexPos).add(0, 1.1, 0), Vec3.ZERO,
            new ItemStack(Items.IRON_INGOT, 16));
        scene.effects().indicateSuccess(indexPos);
        finish(scene, util.select().position(indexPos),
            "Up to the requested amount is extracted and dropped above the Index");
    }

    public static void funnel(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos funnelPos = util.grid().at(3, 2, 2);
        BlockPos outputPos = util.grid().at(3, 1, 2);
        begin(scene, util, "corporea_funnel_request", "Automating a Corporea Request", funnelPos,
            "A redstone pulse makes the Corporea Funnel request its framed item");
        showNetwork(scene, util, funnelPos, STORAGE);
        scene.world().showSection(util.select().position(outputPos), Direction.DOWN);
        focusOn(scene);
        scene.idle(10);
        scene.world().modifyBlockEntity(STORAGE, ChestBlockEntity.class,
            chest -> chest.setItem(0, new ItemStack(Items.OAK_PLANKS, 32)));

        scene.world().createEntity(level -> {
            ItemFrame frame = new ItemFrame(level, funnelPos.north(), Direction.NORTH);
            frame.setItem(new ItemStack(Items.OAK_PLANKS), false);
            return frame;
        });
        scene.overlay().showText(55,
                "The Item Frame chooses the item; its rotation chooses 1, 2, 4, 8, 16, 32, 48, or 64")
            .pointAt(util.vector().centerOf(funnelPos).add(0, 0, -0.55)).placeNearTarget()
            .colored(PonderPalette.INPUT).attachKeyFrame();
        scene.idle(60);

        scene.world().modifyBlock(funnelPos,
            state -> state.setValue(BlockStateProperties.POWERED, true), false);
        scene.effects().indicateRedstone(funnelPos);
        scene.idle(12);
        scene.world().modifyBlockEntity(outputPos, ChestBlockEntity.class,
            chest -> chest.setItem(0, new ItemStack(Items.OAK_PLANKS)));
        scene.effects().indicateSuccess(outputPos);
        finish(scene, util.select().position(outputPos),
            "The requested item is inserted into an inventory one or two blocks below");
    }

    public static void crystalCube(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos cubePos = util.grid().at(3, 1, 2);
        begin(scene, util, "corporea_crystal_cube_count", "Monitoring a Corporea Item", cubePos,
            "The Corporea Crystal Cube displays the network's current stock of one item");
        showNetwork(scene, util, cubePos, STORAGE);
        scene.world().modifyBlockEntity(STORAGE, ChestBlockEntity.class,
            chest -> chest.setItem(0, new ItemStack(Items.DIAMOND, 48)));

        scene.overlay().showControls(util.vector().topOf(cubePos), Pointing.DOWN, 40)
            .withItem(new ItemStack(Items.DIAMOND)).rightClick();
        scene.overlay().showText(50, "Right-click with an item to select what the Cube monitors")
            .pointAt(util.vector().centerOf(cubePos)).placeNearTarget()
            .colored(PonderPalette.INPUT).attachKeyFrame();
        scene.idle(55);

        scene.world().modifyBlockEntityNBT(util.select().position(cubePos),
            CorporeaCrystalCubeBlockEntity.class, tag -> {
                CompoundTag target = new CompoundTag();
                new ItemStack(Items.DIAMOND).save(target);
                tag.put("requestTarget", target);
                tag.putInt("itemCount", 48);
            });
        scene.idle(20);
        scene.effects().indicateSuccess(cubePos);
        finish(scene, util.select().position(cubePos),
            "The displayed count refreshes from every inventory in the connected network");
    }

    public static void interceptor(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos interceptorPos = util.grid().at(3, 1, 2);
        BlockPos lampPos = util.grid().at(2, 1, 2);
        BlockPos requesterPos = util.grid().at(5, 2, 2);
        BlockPos outputPos = util.grid().at(5, 1, 2);
        begin(scene, util, "corporea_interceptor_failure", "Detecting a Failed Request", interceptorPos,
            "The Corporea Interceptor reacts when a network cannot fulfil a request");
        showFailureNetwork(scene, util, interceptorPos, requesterPos, outputPos, lampPos);

        scene.overlay().showText(50, "The Funnel requests Oak Planks, but the storage is empty")
            .pointAt(util.vector().centerOf(requesterPos)).placeNearTarget()
            .colored(PonderPalette.INPUT).attachKeyFrame();
        scene.idle(55);
        scene.idle(12);

        scene.world().modifyBlock(interceptorPos,
            state -> state.setValue(BlockStateProperties.POWERED, true), false);
        scene.world().modifyBlock(lampPos,
            state -> state.setValue(BlockStateProperties.LIT, true), false);
        scene.effects().indicateRedstone(interceptorPos);
        scene.idle(12);
        scene.world().modifyBlock(interceptorPos,
            state -> state.setValue(BlockStateProperties.POWERED, false), false);
        scene.world().modifyBlock(lampPos,
            state -> state.setValue(BlockStateProperties.LIT, false), false);
        finish(scene, util.select().position(interceptorPos),
            "An unfulfilled matching request produces a short redstone pulse");
    }

    public static void retainer(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos retainerPos = util.grid().at(3, 1, 2);
        BlockPos interceptorPos = util.grid().at(2, 1, 2);
        BlockPos requesterPos = util.grid().at(5, 2, 2);
        BlockPos outputPos = util.grid().at(5, 1, 2);
        begin(scene, util, "corporea_retainer_replay", "Replaying a Failed Request", retainerPos,
            "A Corporea Retainer beside an Interceptor remembers the last failed request");
        showFailureNetwork(scene, util, interceptorPos, requesterPos, outputPos, interceptorPos);

        scene.overlay().showText(50, "The first Oak Planks request fails and is stored by the Retainer")
            .pointAt(util.vector().centerOf(retainerPos)).placeNearTarget()
            .colored(PonderPalette.INPUT).attachKeyFrame();
        scene.idle(55);
        scene.idle(12);
        scene.effects().indicateSuccess(retainerPos);
        scene.idle(15);

        scene.world().modifyBlockEntity(STORAGE, ChestBlockEntity.class,
            chest -> chest.setItem(0, new ItemStack(Items.OAK_PLANKS, 16)));
        scene.overlay().showText(50, "After stock is restored, pulse the Retainer to replay that request")
            .pointAt(util.vector().centerOf(retainerPos)).placeNearTarget()
            .colored(PonderPalette.INPUT).attachKeyFrame();
        scene.idle(55);
        scene.world().modifyBlock(retainerPos,
            state -> state.setValue(BlockStateProperties.POWERED, true), false);
        scene.effects().indicateRedstone(retainerPos);
        scene.idle(12);
        scene.world().modifyBlockEntity(outputPos, ChestBlockEntity.class,
            chest -> chest.setItem(0, new ItemStack(Items.OAK_PLANKS)));
        finish(scene, util.select().position(outputPos),
            "The original requester runs again and receives the newly available item");
    }

    private static void begin(SceneBuilder scene, SceneBuildingUtil util, String id, String title,
                              BlockPos subject, String description) {
        scene.title(id, title);
        scene.configureBasePlate(0, 0, 7);
        scene.showBasePlate();
        focusOn(scene);
        scene.idle(15);
        scene.world().showSection(util.select().position(subject), Direction.DOWN);
        focusOn(scene);
        scene.idle(10);
        scene.overlay().showOutlineWithText(util.select().position(subject), 55, description)
            .placeNearTarget().attachKeyFrame();
        scene.idle(60);
    }

    private static void showNetwork(SceneBuilder scene, SceneBuildingUtil util,
                                    BlockPos requestor, BlockPos storage) {
        scene.world().showSection(util.select().position(MASTER), Direction.DOWN);
        focusOn(scene);
        scene.idle(6);
        scene.world().showSection(util.select().position(storage), Direction.DOWN);
        focusOn(scene);
        scene.idle(10);
        spark(scene, MASTER, true);
        spark(scene, storage, false);
        spark(scene, requestor, false);
        scene.overlay().showText(50,
                "A Corporea Spark connects the block to a network managed by one Master Spark")
            .pointAt(util.vector().centerOf(requestor).add(0, 0.8, 0)).placeNearTarget()
            .attachKeyFrame();
        scene.idle(55);
    }

    private static void showFailureNetwork(SceneBuilder scene, SceneBuildingUtil util,
                                           BlockPos interceptor, BlockPos requester,
                                           BlockPos output, BlockPos extra) {
        Selection support = util.select().position(MASTER)
            .add(util.select().position(STORAGE))
            .add(util.select().position(requester))
            .add(util.select().position(output))
            .add(util.select().position(extra));
        scene.world().showSection(support, Direction.DOWN);
        focusOn(scene);
        scene.idle(12);
        spark(scene, MASTER, true);
        spark(scene, STORAGE, false);
        spark(scene, requester, false);
        spark(scene, interceptor, false);
        scene.overlay().showText(45, "All request blocks participate through the same Spark network")
            .pointAt(util.vector().centerOf(MASTER).add(0, 0.8, 0)).placeNearTarget()
            .attachKeyFrame();
        scene.idle(50);
    }

    private static void spark(SceneBuilder scene, BlockPos block, boolean master) {
        scene.world().createEntity(level -> {
            CorporeaSparkEntity spark = new CorporeaSparkEntity(BotaniaEntities.CORPOREA_SPARK, level);
            spark.setMaster(master);
            spark.setPos(block.getX() + 0.5, block.getY() + 1.25, block.getZ() + 0.5);
            return spark;
        });
    }

    private static void finish(SceneBuilder scene, Selection target, String text) {
        scene.idle(8);
        scene.overlay().showOutlineWithText(target, 65, text)
            .placeNearTarget().colored(PonderPalette.OUTPUT).attachKeyFrame();
        scene.idle(70);
        scene.markAsFinished();
    }

    private static void focusOn(SceneBuilder scene) {
        scene.addInstruction(s -> s.setFocusPoint(FOCUS));
    }
}
