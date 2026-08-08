package dev.flomik.botania_ponder;

import dev.flomik.botania_ponder.ponder.scenes.EndoflameScenes;
import dev.flomik.botania_ponder.ponder.scenes.BotanicalBreweryScenes;
import dev.flomik.botania_ponder.ponder.scenes.GeneratingFlowerScenes;
import dev.flomik.botania_ponder.ponder.scenes.FunctionalFlowerScenes;
import dev.flomik.botania_ponder.ponder.scenes.ManaDeviceScenes;
import dev.flomik.botania_ponder.ponder.scenes.ManaPoolScenes;
import dev.flomik.botania_ponder.ponder.scenes.ManaSpreaderScenes;
import dev.flomik.botania_ponder.ponder.scenes.PetalApothecaryScenes;
import dev.flomik.botania_ponder.ponder.scenes.PureDaisyScenes;
import dev.flomik.botania_ponder.ponder.scenes.RunicAltarScenes;
import dev.flomik.ponderlib.api.PonderColorScheme;
import dev.flomik.ponderlib.api.registration.PonderPlugin;
import dev.flomik.ponderlib.api.registration.PonderSceneRegistrationHelper;
import dev.flomik.ponderlib.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;

public class BotaniaPonderPlugin implements PonderPlugin {

    private static final ResourceLocation GENERATING_FLORA = tag("generating_flora");
    private static final ResourceLocation FUNCTIONAL_FLORA = tag("functional_flora");
    private static final ResourceLocation MANA_DEVICES = tag("mana_devices");
    private static final ResourceLocation CRAFTING = tag("crafting");

    private static final PonderColorScheme COLORS = PonderColorScheme.builder()
        .finishingFlash(0x79AD87)
        .basePlateShadow(0x0B160E)
        .tooltipBorder(0x315C3B, 0x548764, 0xA9C6AF)
        .frameBorder(0x806F9A79, 0x403D6347)
        .timelineFill(0xD05C936A, 0xA0336942)
        .keyframeTint(0xB9D0BF)
        .keyframeAlpha(0x80, 0xE0)
        .buttonHoverBorder(0xA076A681, 0x704B7957)
        .buttonBackground(0xE80A140D)
        .buttonIcon(0xFF789482, 0xFFC1D5C6)
        .build();

    @Override
    public String getModId() {
        return BotaniaPonder.MODID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper helper) {
        ResourceLocation manaPool = ResourceLocation.fromNamespaceAndPath("botania", "mana_pool");
        helper.addStoryBoard(manaPool, "mana_pool/filling", ManaPoolScenes::filling);
        helper.addStoryBoard(manaPool, "mana_pool/infusion", ManaPoolScenes::infusion);

        ResourceLocation manaSpreader = ResourceLocation.fromNamespaceAndPath("botania", "mana_spreader");
        helper.addStoryBoard(manaSpreader, "mana_spreader/spreader", ManaSpreaderScenes::spreader);

        ResourceLocation pureDaisy = ResourceLocation.fromNamespaceAndPath("botania", "pure_daisy");
        helper.addStoryBoard(pureDaisy, "pure_daisy/conversion", PureDaisyScenes::conversion);

        ResourceLocation endoflame = ResourceLocation.fromNamespaceAndPath("botania", "endoflame");
        helper.addStoryBoard(endoflame, "endoflame/generation", EndoflameScenes::generation);

        ResourceLocation petalApothecary = ResourceLocation.fromNamespaceAndPath("botania", "apothecary_default");
        helper.addStoryBoard(petalApothecary, "petal_apothecary/crafting", PetalApothecaryScenes::crafting);

        ResourceLocation runicAltar = ResourceLocation.fromNamespaceAndPath("botania", "runic_altar");
        helper.addStoryBoard(runicAltar, "runic_altar/crafting", RunicAltarScenes::crafting);

        ResourceLocation brewery = ResourceLocation.fromNamespaceAndPath("botania", "brewery");
        helper.addStoryBoard(brewery, "botanical_brewery/brewing", BotanicalBreweryScenes::brewing);

        helper.addStoryBoard(botania("hydroangeas"), "generating_flora/hydroangeas",
            GeneratingFlowerScenes::hydroangeas);
        helper.addStoryBoard(botania("thermalily"), "generating_flora/thermalily",
            GeneratingFlowerScenes::thermalily);
        helper.addStoryBoard(botania("rosa_arcana"), "generating_flora/rosa_arcana",
            GeneratingFlowerScenes::rosaArcana);
        helper.addStoryBoard(botania("gourmaryllis"), "generating_flora/gourmaryllis",
            GeneratingFlowerScenes::gourmaryllis);
        helper.addStoryBoard(botania("munchdew"), "generating_flora/munchdew",
            GeneratingFlowerScenes::munchdew);
        helper.addStoryBoard(botania("kekimurus"), "generating_flora/kekimurus",
            GeneratingFlowerScenes::kekimurus);
        helper.addStoryBoard(botania("spectrolus"), "generating_flora/spectrolus",
            GeneratingFlowerScenes::spectrolus);
        helper.addStoryBoard(botania("entropinnyum"), "generating_flora/entropinnyum",
            GeneratingFlowerScenes::entropinnyum);
        helper.addStoryBoard(botania("narslimmus"), "generating_flora/narslimmus",
            GeneratingFlowerScenes::narslimmus);
        helper.addStoryBoard(botania("rafflowsia"), "generating_flora/rafflowsia",
            GeneratingFlowerScenes::rafflowsia);
        helper.addStoryBoard(botania("dandelifeon"), "generating_flora/dandelifeon",
            GeneratingFlowerScenes::dandelifeon);
        helper.addStoryBoard(botania("shulk_me_not"), "generating_flora/shulk_me_not",
            GeneratingFlowerScenes::shulkMeNot);

        helper.addStoryBoard(botania("agricarnation"), "functional_flora/agricarnation",
            FunctionalFlowerScenes::agricarnation);
        helper.addStoryBoard(botania("clayconia"), "functional_flora/clayconia",
            FunctionalFlowerScenes::clayconia);
        helper.addStoryBoard(botania("orechid"), "functional_flora/orechid",
            FunctionalFlowerScenes::orechid);
        helper.addStoryBoard(botania("jaded_amaranthus"), "functional_flora/jaded_amaranthus",
            FunctionalFlowerScenes::jadedAmaranthus);
        helper.addStoryBoard(botania("hopperhock"), "functional_flora/hopperhock",
            FunctionalFlowerScenes::hopperhock);
        helper.addStoryBoard(botania("bellethorn"), "functional_flora/bellethorn",
            FunctionalFlowerScenes::bellethorn);
        helper.addStoryBoard(botania("daffomill"), "functional_flora/daffomill",
            FunctionalFlowerScenes::daffomill);
        helper.addStoryBoard(botania("exoflame"), "functional_flora/exoflame",
            FunctionalFlowerScenes::exoflame);

        helper.addStoryBoard(botania("mana_void"), "mana_devices/mana_void",
            ManaDeviceScenes::manaVoid);
        helper.addStoryBoard(botania("mana_detector"), "mana_devices/mana_detector",
            ManaDeviceScenes::manaDetector);
        helper.addStoryBoard(botania("mana_distributor"), "mana_devices/mana_distributor",
            ManaDeviceScenes::manaDistributor);
        helper.addStoryBoard(botania("open_crate"), "mana_devices/open_crate",
            ManaDeviceScenes::openCrate);
        helper.addStoryBoard(botania("turntable"), "mana_devices/spreader_turntable",
            ManaDeviceScenes::spreaderTurntable);
    }

    @Override
    public PonderColorScheme colors() {
        return COLORS;
    }

    @Override
    public void registerTags(PonderTagRegistrationHelper helper) {
        helper.registerTag(GENERATING_FLORA)
            .title("Generating Flora")
            .description("Flowers that convert resources into Mana")
            .icon(botaniaBlock("endoflame"))
            .addToIndex()
            .register();
        helper.addToTag(GENERATING_FLORA,
            botania("endoflame"), botania("hydroangeas"), botania("thermalily"),
            botania("rosa_arcana"), botania("gourmaryllis"), botania("munchdew"),
            botania("kekimurus"), botania("spectrolus"), botania("entropinnyum"),
            botania("narslimmus"), botania("rafflowsia"), botania("dandelifeon"),
            botania("shulk_me_not"));

        helper.registerTag(FUNCTIONAL_FLORA)
            .title("Functional Flora")
            .description("Flowers that spend Mana to interact with the world")
            .icon(botaniaBlock("agricarnation"))
            .addToIndex()
            .register();
        helper.addToTag(FUNCTIONAL_FLORA,
            botania("agricarnation"), botania("clayconia"), botania("orechid"),
            botania("jaded_amaranthus"), botania("hopperhock"), botania("bellethorn"),
            botania("daffomill"), botania("exoflame"));

        helper.registerTag(MANA_DEVICES)
            .title("Mana Devices")
            .description("Blocks that store, move, detect, or control Mana")
            .icon(botaniaBlock("mana_pool"))
            .addToIndex()
            .register();
        helper.addToTag(MANA_DEVICES,
            botania("mana_pool"), botania("mana_spreader"), botania("mana_void"),
            botania("mana_detector"), botania("mana_distributor"), botania("open_crate"),
            botania("turntable"));

        helper.registerTag(CRAFTING)
            .title("Botanical Crafting")
            .description("Botania's world-based crafting and transformation devices")
            .icon(botaniaBlock("apothecary_default"))
            .addToIndex()
            .register();
        helper.addToTag(CRAFTING,
            botania("pure_daisy"), botania("apothecary_default"), botania("runic_altar"),
            botania("brewery"));
    }

    private static ResourceLocation botania(String path) {
        return ResourceLocation.fromNamespaceAndPath("botania", path);
    }

    private static ResourceLocation tag(String path) {
        return ResourceLocation.fromNamespaceAndPath(BotaniaPonder.MODID, path);
    }

    private static Block botaniaBlock(String path) {
        return Objects.requireNonNull(ForgeRegistries.BLOCKS.getValue(botania(path)));
    }
}
