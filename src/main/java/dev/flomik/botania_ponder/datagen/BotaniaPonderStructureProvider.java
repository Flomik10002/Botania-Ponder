package dev.flomik.botania_ponder.datagen;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import dev.flomik.botania_ponder.BotaniaPonder;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiPredicate;

/**
 * Writes the Ponder schematics under {@code assets/botania_ponder/ponder/*.nbt} by hand, as a
 * vanilla structure-template NBT ({@code size}/{@code entities}/{@code blocks}/{@code palette}/
 * {@code DataVersion} tags) - PonderLib reads scenes' static geometry from a resource-pack asset
 * there, not {@code data/}, so it works on a client-only mod. There's no running world during
 * datagen to build these the normal way (structure blocks in-game, then export), so each scene gets
 * its own small write method here instead. Run via {@code ./gradlew runData}, then commit the
 * output under {@code src/generated/resources/}.
 */
public class BotaniaPonderStructureProvider implements DataProvider {

    private final PackOutput.PathProvider pathProvider;

    public BotaniaPonderStructureProvider(PackOutput output) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "ponder");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        return CompletableFuture.allOf(
            manaPoolInfusion(output),
            manaPoolFilling(output),
            manaSpreader(output),
            pureDaisy(output),
            endoflame(output),
            petalApothecary(output),
            runicAltar(output),
            botanicalBrewery(output),
            hydroangeas(output),
            thermalily(output),
            rosaArcana(output),
            gourmaryllis(output),
            munchdew(output),
            kekimurus(output),
            spectrolus(output),
            entropinnyum(output),
            narslimmus(output),
            rafflowsia(output),
            dandelifeon(output),
            shulkMeNot(output),
            agricarnation(output),
            clayconia(output),
            orechid(output),
            jadedAmaranthus(output),
            hopperhock(output),
            bellethorn(output),
            daffomill(output),
            exoflame(output),
            dreadthorn(output),
            pollidisiac(output),
            fallenKanade(output),
            tangleberrie(output),
            jiyuulia(output),
            bergamute(output),
            bubbell(output),
            heiseiDream(output),
            hyacidus(output),
            labellia(output),
            loonium(output),
            marimorphosis(output),
            medumone(output),
            orechidIgnem(output),
            rannuncarpus(output),
            solegnolia(output),
            spectranthemum(output),
            tigerseye(output),
            vinculotus(output),
            manaLenses(output),
            manaVoid(output),
            manaDetector(output),
            manaDistributor(output),
            openCrate(output),
            spreaderTurntable(output),
            redStringBlocks(output)
        );
    }

    /** {@code ManaPoolScenes#infusion}: nothing but the Pool - the scene is only about the Pool. */
    private CompletableFuture<?> manaPoolInfusion(CachedOutput output) {
        ListTag palette = new ListTag();
        ListTag blocks = new ListTag();
        checkerboardFloor(palette, blocks, 5);
        addBlock(blocks, 2, 1, 2, addPaletteEntry(palette, "botania:mana_pool"));
        return write(output, "mana_pool/infusion", root(palette, blocks, 5, 2, 5));
    }

    /** {@code ManaPoolScenes#filling}: Pool centred, one Spreader behind it as its mana source. */
    private CompletableFuture<?> manaPoolFilling(CachedOutput output) {
        ListTag palette = new ListTag();
        ListTag blocks = new ListTag();
        checkerboardFloor(palette, blocks, 5);
        addBlock(blocks, 2, 1, 2, addPaletteEntry(palette, "botania:mana_pool"));
        addBlock(blocks, 2, 1, 0, addPaletteEntry(palette, "botania:mana_spreader"));
        return write(output, "mana_pool/filling", root(palette, blocks, 5, 2, 5));
    }

    /**
     * {@code ManaSpreaderScenes#spreader}: three Endoflames feed the centred Spreader, which fires
     * at a Mana Pool behind it. Grass is placed only under the flowers.
     */
    private CompletableFuture<?> manaSpreader(CachedOutput output) {
        ListTag palette = new ListTag();
        ListTag blocks = new ListTag();
        checkerboardFloor(palette, blocks, 5, (x, z) -> z == 0 && x >= 1 && x <= 3);
        addBlock(blocks, 2, 1, 2, addPaletteEntry(palette, "botania:mana_spreader"));
        addBlock(blocks, 2, 1, 4, addPaletteEntry(palette, "botania:mana_pool"));
        int endoflame = addPaletteEntry(palette, "botania:endoflame");
        for (int x = 1; x <= 3; x++) {
            addBlock(blocks, x, 1, 0, endoflame);
        }
        return write(output, "mana_spreader/spreader", root(palette, blocks, 5, 2, 5));
    }

    /** Pure Daisy with four consecutive Oak Logs followed clockwise by four Stone blocks. */
    private CompletableFuture<?> pureDaisy(CachedOutput output) {
        ListTag palette = new ListTag();
        ListTag blocks = new ListTag();
        checkerboardFloor(palette, blocks, 5, (x, z) -> x == 2 && z == 2);
        addBlock(blocks, 2, 1, 2, addPaletteEntry(palette, "botania:pure_daisy"));
        int oakLog = addPaletteEntry(palette, "minecraft:oak_log");
        int stone = addPaletteEntry(palette, "minecraft:stone");
        for (int x = 1; x <= 3; x++) {
            for (int z = 1; z <= 3; z++) {
                if (x != 2 || z != 2) {
                    boolean firstHalfOfRing = z == 1 || x == 3 && z == 2;
                    addBlock(blocks, x, 1, z, firstHalfOfRing ? oakLog : stone);
                }
            }
        }
        return write(output, "pure_daisy/conversion", root(palette, blocks, 5, 2, 5));
    }

    /** Endoflame by itself: this scene is only about consuming fuel and generating Mana. */
    private CompletableFuture<?> endoflame(CachedOutput output) {
        ListTag palette = new ListTag();
        ListTag blocks = new ListTag();
        checkerboardFloor(palette, blocks, 5, (x, z) -> x == 2 && z == 2);
        addBlock(blocks, 2, 1, 2, addPaletteEntry(palette, "botania:endoflame"));
        return write(output, "endoflame/generation", root(palette, blocks, 5, 2, 5));
    }

    /** The default Petal Apothecary alone; its water and contents are added by the scene. */
    private CompletableFuture<?> petalApothecary(CachedOutput output) {
        ListTag palette = new ListTag();
        ListTag blocks = new ListTag();
        checkerboardFloor(palette, blocks, 5);
        addBlock(blocks, 2, 1, 2, addPaletteEntry(palette, "botania:apothecary_default"));
        return write(output, "petal_apothecary/crafting", root(palette, blocks, 5, 2, 5));
    }

    /** Runic Altar with the Mana Spreader required to power its recipe. */
    private CompletableFuture<?> runicAltar(CachedOutput output) {
        ListTag palette = new ListTag();
        ListTag blocks = new ListTag();
        checkerboardFloor(palette, blocks, 5);
        addBlock(blocks, 2, 1, 3, addPaletteEntry(palette, "botania:runic_altar"));
        addBlock(blocks, 2, 1, 0, addPaletteEntry(palette, "botania:mana_spreader"));
        return write(output, "runic_altar/crafting", root(palette, blocks, 5, 2, 5));
    }

    /** Botanical Brewery with the Mana Spreader required to power a brew. */
    private CompletableFuture<?> botanicalBrewery(CachedOutput output) {
        ListTag palette = new ListTag();
        ListTag blocks = new ListTag();
        checkerboardFloor(palette, blocks, 5);
        addBlock(blocks, 2, 1, 3, addPaletteEntry(palette, "botania:brewery"));
        addBlock(blocks, 2, 1, 0, addPaletteEntry(palette, "botania:mana_spreader"));
        return write(output, "botanical_brewery/brewing", root(palette, blocks, 5, 2, 5));
    }

    private CompletableFuture<?> hydroangeas(CachedOutput output) {
        return fluidFlower(output, "generating_flora/hydroangeas", "botania:hydroangeas", "minecraft:water");
    }

    private CompletableFuture<?> thermalily(CachedOutput output) {
        return fluidFlower(output, "generating_flora/thermalily", "botania:thermalily", "minecraft:lava");
    }

    private CompletableFuture<?> fluidFlower(CachedOutput output, String path, String flowerId, String fluidId) {
        ListTag palette = new ListTag();
        ListTag blocks = new ListTag();
        checkerboardFloor(palette, blocks, 5, (x, z) -> x == 2 && z == 2);
        addBlock(blocks, 2, 1, 2, addPaletteEntry(palette, flowerId));
        addBlock(blocks, 2, 1, 3, addPaletteEntry(palette, fluidId));
        return write(output, path, root(palette, blocks, 5, 2, 5));
    }

    private CompletableFuture<?> rosaArcana(CachedOutput output) {
        return solitaryFlower(output, "generating_flora/rosa_arcana", "botania:rosa_arcana", 2, 2);
    }

    private CompletableFuture<?> gourmaryllis(CachedOutput output) {
        return solitaryFlower(output, "generating_flora/gourmaryllis", "botania:gourmaryllis", 2, 2);
    }

    private CompletableFuture<?> solitaryFlower(CachedOutput output, String path, String flowerId,
                                                 int flowerX, int flowerZ) {
        ListTag palette = new ListTag();
        ListTag blocks = new ListTag();
        checkerboardFloor(palette, blocks, 5, (x, z) -> x == flowerX && z == flowerZ);
        addBlock(blocks, flowerX, 1, flowerZ, addPaletteEntry(palette, flowerId));
        return write(output, path, root(palette, blocks, 5, 2, 5));
    }

    private CompletableFuture<?> munchdew(CachedOutput output) {
        ListTag palette = new ListTag();
        ListTag blocks = new ListTag();
        checkerboardFloor(palette, blocks, 5, (x, z) -> x == 2 && z == 1);
        addBlock(blocks, 2, 1, 1, addPaletteEntry(palette, "botania:munchdew"));
        int leaves = addPaletteEntry(palette, "minecraft:oak_leaves");
        for (int x = 1; x <= 3; x++) {
            addBlock(blocks, x, 1, 3, leaves);
        }
        return write(output, "generating_flora/munchdew", root(palette, blocks, 5, 2, 5));
    }

    private CompletableFuture<?> kekimurus(CachedOutput output) {
        ListTag palette = new ListTag();
        ListTag blocks = new ListTag();
        checkerboardFloor(palette, blocks, 5, (x, z) -> x == 2 && z == 1);
        addBlock(blocks, 2, 1, 1, addPaletteEntry(palette, "botania:kekimurus"));
        addBlock(blocks, 2, 1, 3, addPaletteEntry(palette, "minecraft:cake"));
        return write(output, "generating_flora/kekimurus", root(palette, blocks, 5, 2, 5));
    }

    private CompletableFuture<?> spectrolus(CachedOutput output) {
        return solitaryFlower(output, "generating_flora/spectrolus", "botania:spectrolus", 2, 2);
    }

    private CompletableFuture<?> entropinnyum(CachedOutput output) {
        return solitaryFlower(output, "generating_flora/entropinnyum", "botania:entropinnyum", 2, 2);
    }

    private CompletableFuture<?> narslimmus(CachedOutput output) {
        return solitaryFlower(output, "generating_flora/narslimmus", "botania:narslimmus", 2, 2);
    }

    private CompletableFuture<?> rafflowsia(CachedOutput output) {
        ListTag palette = new ListTag();
        ListTag blocks = new ListTag();
        checkerboardFloor(palette, blocks, 5,
            (x, z) -> x == 2 && (z == 1 || z == 3));
        addBlock(blocks, 2, 1, 1, addPaletteEntry(palette, "botania:rafflowsia"));
        addBlock(blocks, 2, 1, 3, addPaletteEntry(palette, "botania:endoflame"));
        return write(output, "generating_flora/rafflowsia", root(palette, blocks, 5, 2, 5));
    }

    private CompletableFuture<?> dandelifeon(CachedOutput output) {
        return solitaryFlower(output, "generating_flora/dandelifeon", "botania:dandelifeon", 2, 2);
    }

    private CompletableFuture<?> shulkMeNot(CachedOutput output) {
        return solitaryFlower(output, "generating_flora/shulk_me_not", "botania:shulk_me_not", 2, 2);
    }

    private CompletableFuture<?> agricarnation(CachedOutput output) {
        ListTag palette = new ListTag();
        ListTag blocks = new ListTag();
        checkerboardFloor(palette, blocks, 5,
            (x, z) -> x == 2 && z == 1,
            (x, z) -> z == 3 && x >= 1 && x <= 3);
        addBlock(blocks, 2, 1, 1, addPaletteEntry(palette, "botania:agricarnation"));
        int wheat = addPaletteEntry(palette, "minecraft:wheat");
        for (int x = 1; x <= 3; x++) {
            addBlock(blocks, x, 1, 3, wheat);
        }
        return write(output, "functional_flora/agricarnation", root(palette, blocks, 5, 2, 5));
    }

    private CompletableFuture<?> clayconia(CachedOutput output) {
        return flowerWithInputRow(output, "functional_flora/clayconia", "botania:clayconia", "minecraft:sand");
    }

    private CompletableFuture<?> orechid(CachedOutput output) {
        return flowerWithInputRow(output, "functional_flora/orechid", "botania:orechid", "minecraft:stone");
    }

    private CompletableFuture<?> flowerWithInputRow(CachedOutput output, String path,
                                                     String flowerId, String inputId) {
        ListTag palette = new ListTag();
        ListTag blocks = new ListTag();
        checkerboardFloor(palette, blocks, 5, (x, z) -> x == 2 && z == 1);
        addBlock(blocks, 2, 1, 1, addPaletteEntry(palette, flowerId));
        int input = addPaletteEntry(palette, inputId);
        for (int x = 1; x <= 3; x++) {
            addBlock(blocks, x, 1, 3, input);
        }
        return write(output, path, root(palette, blocks, 5, 2, 5));
    }

    private CompletableFuture<?> jadedAmaranthus(CachedOutput output) {
        ListTag palette = new ListTag();
        ListTag blocks = new ListTag();
        checkerboardFloor(palette, blocks, 5,
            (x, z) -> x >= 1 && x <= 3 && z >= 1 && z <= 3);
        addBlock(blocks, 2, 1, 2, addPaletteEntry(palette, "botania:jaded_amaranthus"));
        return write(output, "functional_flora/jaded_amaranthus", root(palette, blocks, 5, 2, 5));
    }

    private CompletableFuture<?> hopperhock(CachedOutput output) {
        ListTag palette = new ListTag();
        ListTag blocks = new ListTag();
        checkerboardFloor(palette, blocks, 5, (x, z) -> x == 2 && z == 2);
        addBlock(blocks, 2, 1, 2, addPaletteEntry(palette, "botania:hopperhock"));
        addBlock(blocks, 2, 1, 3, addPaletteEntry(palette, "minecraft:chest"));
        return write(output, "functional_flora/hopperhock", root(palette, blocks, 5, 2, 5));
    }

    private CompletableFuture<?> bellethorn(CachedOutput output) {
        return solitaryFlower(output, "functional_flora/bellethorn", "botania:bellethorn", 2, 2);
    }

    private CompletableFuture<?> daffomill(CachedOutput output) {
        return solitaryFlower(output, "functional_flora/daffomill", "botania:daffomill", 2, 3);
    }

    private CompletableFuture<?> exoflame(CachedOutput output) {
        ListTag palette = new ListTag();
        ListTag blocks = new ListTag();
        checkerboardFloor(palette, blocks, 5, (x, z) -> x == 1 && z == 2);
        addBlock(blocks, 1, 1, 2, addPaletteEntry(palette, "botania:exoflame"));
        addBlock(blocks, 3, 1, 2,
            addPaletteEntry(palette, "minecraft:furnace", "facing", "south"));
        return write(output, "functional_flora/exoflame", root(palette, blocks, 5, 2, 5));
    }

    private CompletableFuture<?> dreadthorn(CachedOutput output) {
        return solitaryFlower(output, "functional_flora/dreadthorn", "botania:dreadthorn", 2, 2);
    }

    private CompletableFuture<?> pollidisiac(CachedOutput output) {
        return solitaryFlower(output, "functional_flora/pollidisiac", "botania:pollidisiac", 2, 2);
    }

    private CompletableFuture<?> fallenKanade(CachedOutput output) {
        return solitaryFlower(output, "functional_flora/fallen_kanade", "botania:fallen_kanade", 2, 2);
    }

    private CompletableFuture<?> tangleberrie(CachedOutput output) {
        return solitaryFlower(output, "functional_flora/tangleberrie", "botania:tangleberrie", 2, 2);
    }

    private CompletableFuture<?> jiyuulia(CachedOutput output) {
        return solitaryFlower(output, "functional_flora/jiyuulia", "botania:jiyuulia", 2, 2);
    }

    private CompletableFuture<?> bergamute(CachedOutput output) {
        ListTag palette = new ListTag();
        ListTag blocks = new ListTag();
        checkerboardFloor(palette, blocks, 5, (x, z) -> x == 2 && z == 2);
        addBlock(blocks, 2, 1, 2, addPaletteEntry(palette, "botania:bergamute"));
        addBlock(blocks, 2, 1, 4, addPaletteEntry(palette, "minecraft:note_block"));
        return write(output, "functional_flora/bergamute", root(palette, blocks, 5, 2, 5));
    }

    private CompletableFuture<?> bubbell(CachedOutput output) {
        ListTag palette = new ListTag();
        ListTag blocks = new ListTag();
        checkerboardFloor(palette, blocks, 5, (x, z) -> x == 2 && z == 2);
        addBlock(blocks, 2, 1, 2, addPaletteEntry(palette, "botania:bubbell"));
        int water = addPaletteEntry(palette, "minecraft:water", "level", "0");
        for (int x = 1; x <= 3; x++) {
            for (int z = 1; z <= 3; z++) {
                if (x != 2 || z != 2) {
                    addBlock(blocks, x, 1, z, water);
                }
            }
        }
        return write(output, "functional_flora/bubbell", root(palette, blocks, 5, 2, 5));
    }

    private CompletableFuture<?> heiseiDream(CachedOutput output) {
        return solitaryFlower(output, "functional_flora/heisei_dream", "botania:heisei_dream", 2, 2);
    }

    private CompletableFuture<?> hyacidus(CachedOutput output) {
        return solitaryFlower(output, "functional_flora/hyacidus", "botania:hyacidus", 2, 2);
    }

    private CompletableFuture<?> labellia(CachedOutput output) {
        return solitaryFlower(output, "functional_flora/labellia", "botania:labellia", 2, 2);
    }

    private CompletableFuture<?> loonium(CachedOutput output) {
        return solitaryFlower(output, "functional_flora/loonium", "botania:loonium", 2, 2);
    }

    private CompletableFuture<?> marimorphosis(CachedOutput output) {
        return flowerWithInputRow(output, "functional_flora/marimorphosis",
            "botania:marimorphosis", "minecraft:stone");
    }

    private CompletableFuture<?> medumone(CachedOutput output) {
        return solitaryFlower(output, "functional_flora/medumone", "botania:medumone", 2, 2);
    }

    private CompletableFuture<?> orechidIgnem(CachedOutput output) {
        return flowerWithInputRow(output, "functional_flora/orechid_ignem",
            "botania:orechid_ignem", "minecraft:netherrack");
    }

    private CompletableFuture<?> rannuncarpus(CachedOutput output) {
        ListTag palette = new ListTag();
        ListTag blocks = new ListTag();
        checkerboardFloor(palette, blocks, 5);
        int grass = addPaletteEntry(palette, "minecraft:grass_block");
        int whiteConcrete = addPaletteEntry(palette, "minecraft:white_concrete");
        addBlock(blocks, 2, 1, 2, grass);
        addBlock(blocks, 3, 1, 2, whiteConcrete);
        addBlock(blocks, 2, 2, 2, addPaletteEntry(palette, "botania:rannuncarpus"));
        return write(output, "functional_flora/rannuncarpus", root(palette, blocks, 5, 3, 5));
    }

    private CompletableFuture<?> solegnolia(CachedOutput output) {
        return solitaryFlower(output, "functional_flora/solegnolia", "botania:solegnolia", 2, 2);
    }

    private CompletableFuture<?> spectranthemum(CachedOutput output) {
        ListTag palette = new ListTag();
        ListTag blocks = new ListTag();
        checkerboardFloor(palette, blocks, 5, (x, z) -> x == 1 && z == 2);
        addBlock(blocks, 1, 1, 2, addPaletteEntry(palette, "botania:spectranthemum"));
        addBlock(blocks, 4, 1, 2, addPaletteEntry(palette, "minecraft:gold_block"));
        return write(output, "functional_flora/spectranthemum", root(palette, blocks, 5, 2, 5));
    }

    private CompletableFuture<?> tigerseye(CachedOutput output) {
        return solitaryFlower(output, "functional_flora/tigerseye", "botania:tigerseye", 2, 2);
    }

    private CompletableFuture<?> vinculotus(CachedOutput output) {
        return solitaryFlower(output, "functional_flora/vinculotus", "botania:vinculotus", 2, 2);
    }

    private CompletableFuture<?> manaLenses(CachedOutput output) {
        String[] paths = {
            "normal", "speed", "power", "time", "efficiency", "bounce", "gravity", "mine",
            "damage", "phantom", "magnet", "explosive", "influence", "weight", "paint", "fire",
            "piston", "light", "warp", "redirect", "firework", "flare", "messenger", "tripwire", "storm"
        };
        return CompletableFuture.allOf(java.util.Arrays.stream(paths)
            .map(path -> manaLens(output, path))
            .toArray(CompletableFuture[]::new));
    }

    private CompletableFuture<?> manaLens(CachedOutput output, String path) {
        ListTag palette = new ListTag();
        ListTag blocks = new ListTag();
        checkerboardFloor(palette, blocks, 5);
        addBlock(blocks, 2, 1, 0, addPaletteEntry(palette, "botania:mana_spreader"));
        String target = switch (path) {
            case "normal", "speed", "power", "time", "efficiency", "magnet", "messenger" -> "botania:mana_pool";
            case "paint" -> "minecraft:white_wool";
            case "fire" -> "minecraft:netherrack";
            case "warp" -> "botania:piston_relay";
            case "redirect" -> "botania:mana_spreader";
            default -> "minecraft:stone";
        };
        int targetZ = path.equals("piston") ? 3 : 4;
        addBlock(blocks, 2, 1, targetZ, addPaletteEntry(palette, target));
        return write(output, "mana_lenses/" + path, root(palette, blocks, 5, 4, 5));
    }

    private CompletableFuture<?> manaVoid(CachedOutput output) {
        ListTag palette = new ListTag();
        ListTag blocks = new ListTag();
        checkerboardFloor(palette, blocks, 5);
        addBlock(blocks, 2, 1, 0, addPaletteEntry(palette, "botania:mana_spreader"));
        addBlock(blocks, 2, 1, 3, addPaletteEntry(palette, "botania:mana_void"));
        return write(output, "mana_devices/mana_void", root(palette, blocks, 5, 2, 5));
    }

    private CompletableFuture<?> manaDetector(CachedOutput output) {
        ListTag palette = new ListTag();
        ListTag blocks = new ListTag();
        checkerboardFloor(palette, blocks, 5);
        addBlock(blocks, 2, 1, 0, addPaletteEntry(palette, "botania:mana_spreader"));
        addBlock(blocks, 2, 1, 2, addPaletteEntry(palette, "botania:mana_detector"));
        addBlock(blocks, 2, 1, 3, addPaletteEntry(palette, "minecraft:redstone_lamp"));
        return write(output, "mana_devices/mana_detector", root(palette, blocks, 5, 2, 5));
    }

    private CompletableFuture<?> manaDistributor(CachedOutput output) {
        ListTag palette = new ListTag();
        ListTag blocks = new ListTag();
        checkerboardFloor(palette, blocks, 7);
        addBlock(blocks, 3, 1, 0, addPaletteEntry(palette, "botania:mana_spreader"));
        addBlock(blocks, 3, 1, 3, addPaletteEntry(palette, "botania:mana_distributor"));
        int pool = addPaletteEntry(palette, "botania:mana_pool");
        addBlock(blocks, 2, 1, 3, pool);
        addBlock(blocks, 4, 1, 3, pool);
        addBlock(blocks, 3, 1, 4, pool);
        return write(output, "mana_devices/mana_distributor", root(palette, blocks, 7, 2, 7));
    }

    private CompletableFuture<?> openCrate(CachedOutput output) {
        ListTag palette = new ListTag();
        ListTag blocks = new ListTag();
        checkerboardFloor(palette, blocks, 5);
        addBlock(blocks, 2, 2, 2, addPaletteEntry(palette, "botania:open_crate"));
        return write(output, "mana_devices/open_crate", root(palette, blocks, 5, 3, 5));
    }

    private CompletableFuture<?> spreaderTurntable(CachedOutput output) {
        ListTag palette = new ListTag();
        ListTag blocks = new ListTag();
        checkerboardFloor(palette, blocks, 5);
        addBlock(blocks, 2, 1, 2, addPaletteEntry(palette, "botania:turntable"));
        addBlock(blocks, 2, 2, 2, addPaletteEntry(palette, "botania:mana_spreader"));
        return write(output, "mana_devices/spreader_turntable", root(palette, blocks, 5, 3, 5));
    }

    private CompletableFuture<?> redStringBlocks(CachedOutput output) {
        return CompletableFuture.allOf(
            redStringBlock(output, "container"),
            redStringBlock(output, "dispenser"),
            redStringBlock(output, "nutrifier"),
            redStringBlock(output, "comparator"),
            redStringBlock(output, "relay"),
            redStringBlock(output, "interceptor")
        );
    }

    /** Six related blocks sharing the same south-facing, four-block Red String link. */
    private CompletableFuture<?> redStringBlock(CachedOutput output, String path) {
        ListTag palette = new ListTag();
        ListTag blocks = new ListTag();
        BiPredicate<Integer, Integer> grass = path.equals("relay")
            ? (x, z) -> (x == 3 && z == 5) || (x == 2 && z == 5) || (x == 4 && z == 5)
                || (x == 3 && z == 4) || (x == 3 && z == 6)
            : (x, z) -> false;
        BiPredicate<Integer, Integer> farmland = path.equals("nutrifier")
            ? (x, z) -> x == 3 && z == 5
            : (x, z) -> false;
        checkerboardFloor(palette, blocks, 7, grass, farmland);

        String sourceId = switch (path) {
            case "container" -> "botania:red_string_container";
            case "dispenser" -> "botania:red_string_dispenser";
            case "nutrifier" -> "botania:red_string_fertilizer";
            case "comparator" -> "botania:red_string_comparator";
            case "relay" -> "botania:red_string_relay";
            case "interceptor" -> "botania:red_string_interceptor";
            default -> throw new IllegalArgumentException("Unknown Red String scene: " + path);
        };
        addBlock(blocks, 3, 1, 1, addPaletteEntry(palette, sourceId, "facing", "south"));

        String targetId = switch (path) {
            case "dispenser" -> "minecraft:dispenser";
            case "nutrifier" -> "minecraft:wheat";
            case "relay" -> "minecraft:dandelion";
            default -> "minecraft:chest";
        };
        int target = path.equals("dispenser")
            ? addPaletteEntry(palette, targetId, "facing", "south")
            : addPaletteEntry(palette, targetId);
        addBlock(blocks, 3, 1, 5, target);

        switch (path) {
            case "container" -> addBlock(blocks, 2, 1, 1,
                addPaletteEntry(palette, "minecraft:hopper", "facing", "east"));
            case "comparator" -> {
                addBlock(blocks, 2, 1, 1,
                    addPaletteEntry(palette, "minecraft:comparator", "facing", "west"));
                addBlock(blocks, 1, 1, 1, addPaletteEntry(palette, "minecraft:redstone_lamp"));
            }
            case "relay" -> addBlock(blocks, 3, 2, 1,
                addPaletteEntry(palette, "botania:jaded_amaranthus"));
            case "interceptor" -> addBlock(blocks, 2, 1, 1,
                addPaletteEntry(palette, "minecraft:redstone_lamp"));
            default -> {
            }
        }

        return write(output, "red_string/" + path, root(palette, blocks, 7, 3, 7));
    }

    private static CompoundTag root(ListTag palette, ListTag blocks, int sx, int sy, int sz) {
        CompoundTag root = new CompoundTag();
        root.put("size", intList(sx, sy, sz));
        root.put("entities", new ListTag());
        root.put("blocks", blocks);
        root.put("palette", palette);
        NbtUtils.addCurrentDataVersion(root);
        return root;
    }

    /**
     * The Ponder podium, in <b>PonderLib's own colours</b>: {@code white_concrete} alternating with
     * {@code light_gray_concrete}. That pairing is PonderLib's deliberate choice, not Create's -
     * see {@code PonderSchematicProvider#withCheckerboardFloor} in {@code references/ponderlib},
     * whose javadoc spells out that it uses {@code light_gray_concrete} "instead of
     * {@code snow_block}, by explicit choice rather than matching the original exactly". Match the
     * library we build on, not the library it was ported from.
     * <p>
     * It's a neutral stage that makes the subject read clearly and gives the viewer a grid to judge
     * distance by - not scenery, and not replaced wholesale just because a scene's blocks would sit
     * on grass in the real world.
     */
    private static void checkerboardFloor(ListTag palette, ListTag blocks, int size) {
        checkerboardFloor(palette, blocks, size, (x, z) -> false, (x, z) -> false);
    }

    /**
     * The same podium with individual cells swapped for grass - which is how Create handles a scene
     * whose content needs real ground: {@code rope_pulley/modes.nbt} is a plain checkerboard with a
     * single grass cell in the middle, {@code mechanical_saw/breaker.nbt} a small grass/dirt patch
     * where its trees grow. Botania flowers can only stand on grass/dirt, so a flower scene marks
     * just those positions here and leaves the rest of the podium neutral.
     */
    private static void checkerboardFloor(ListTag palette, ListTag blocks, int size, BiPredicate<Integer, Integer> grassCell) {
        checkerboardFloor(palette, blocks, size, grassCell, (x, z) -> false);
    }

    private static void checkerboardFloor(ListTag palette, ListTag blocks, int size,
                                          BiPredicate<Integer, Integer> grassCell,
                                          BiPredicate<Integer, Integer> farmlandCell) {
        int light = addPaletteEntry(palette, "minecraft:white_concrete");
        int dark = addPaletteEntry(palette, "minecraft:light_gray_concrete");
        int grass = -1;
        int farmland = -1;
        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                if (farmlandCell.test(x, z)) {
                    if (farmland < 0) {
                        farmland = addPaletteEntry(palette, "minecraft:farmland");
                    }
                    addBlock(blocks, x, 0, z, farmland);
                } else if (grassCell.test(x, z)) {
                    if (grass < 0) {
                        grass = addPaletteEntry(palette, "minecraft:grass_block");
                    }
                    addBlock(blocks, x, 0, z, grass);
                } else {
                    addBlock(blocks, x, 0, z, (x + z) % 2 == 0 ? light : dark);
                }
            }
        }
    }

    private static int addPaletteEntry(ListTag palette, String blockId) {
        CompoundTag entry = new CompoundTag();
        entry.putString("Name", blockId);
        palette.add(entry);
        return palette.size() - 1;
    }

    private static int addPaletteEntry(ListTag palette, String blockId,
                                       String propertyName, String propertyValue) {
        CompoundTag entry = new CompoundTag();
        entry.putString("Name", blockId);
        CompoundTag properties = new CompoundTag();
        properties.putString(propertyName, propertyValue);
        entry.put("Properties", properties);
        palette.add(entry);
        return palette.size() - 1;
    }

    private static void addBlock(ListTag blocks, int x, int y, int z, int paletteIndex) {
        CompoundTag block = new CompoundTag();
        block.put("pos", intList(x, y, z));
        block.putInt("state", paletteIndex);
        blocks.add(block);
    }

    private CompletableFuture<?> write(CachedOutput output, String name, CompoundTag root) {
        Path target = pathProvider.file(ResourceLocation.fromNamespaceAndPath(BotaniaPonder.MODID, name), "nbt");
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            NbtIo.writeCompressed(root, bytes);
            byte[] data = bytes.toByteArray();
            HashCode hash = Hashing.sha1().hashBytes(data);
            output.writeIfNeeded(target, data, hash);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return CompletableFuture.completedFuture(null);
    }

    private static ListTag intList(int... values) {
        ListTag tag = new ListTag();
        for (int value : values) {
            tag.add(IntTag.valueOf(value));
        }
        return tag;
    }

    @Override
    public String getName() {
        return "Botania Ponder Structures";
    }
}
