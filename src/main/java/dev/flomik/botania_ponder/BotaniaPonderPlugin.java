package dev.flomik.botania_ponder;

import dev.flomik.botania_ponder.ponder.scenes.EndoflameScenes;
import dev.flomik.botania_ponder.ponder.scenes.BotanicalBreweryScenes;
import dev.flomik.botania_ponder.ponder.scenes.GeneratingFlowerScenes;
import dev.flomik.botania_ponder.ponder.scenes.FunctionalFlowerScenes;
import dev.flomik.botania_ponder.ponder.scenes.AdvancedFunctionalFlowerScenes;
import dev.flomik.botania_ponder.ponder.scenes.ManaDeviceScenes;
import dev.flomik.botania_ponder.ponder.scenes.ManaPoolScenes;
import dev.flomik.botania_ponder.ponder.scenes.ManaLensScenes;
import dev.flomik.botania_ponder.ponder.scenes.ManaSpreaderScenes;
import dev.flomik.botania_ponder.ponder.scenes.PetalApothecaryScenes;
import dev.flomik.botania_ponder.ponder.scenes.PureDaisyScenes;
import dev.flomik.botania_ponder.ponder.scenes.RunicAltarScenes;
import dev.flomik.botania_ponder.ponder.scenes.RedStringScenes;
import dev.flomik.ponderlib.api.PonderColorScheme;
import dev.flomik.ponderlib.api.registration.PonderPlugin;
import dev.flomik.ponderlib.api.registration.PonderSceneRegistrationHelper;
import dev.flomik.ponderlib.api.registration.PonderTagRegistrationHelper;
import dev.flomik.ponderlib.api.scene.PonderStoryBoard;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;
import java.util.Set;

public class BotaniaPonderPlugin implements PonderPlugin {

    private static final ResourceLocation GENERATING_FLORA = tag("generating_flora");
    private static final ResourceLocation FUNCTIONAL_FLORA = tag("functional_flora");
    private static final ResourceLocation MANA_DEVICES = tag("mana_devices");
    private static final ResourceLocation CRAFTING = tag("crafting");
    private static final ResourceLocation MANA_LENSES = tag("mana_lenses");
    private static final ResourceLocation RED_STRING = tag("red_string");

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
        addStoryBoard(helper, manaPool, "mana_pool/filling", ManaPoolScenes::filling, MANA_DEVICES);
        addStoryBoard(helper, manaPool, "mana_pool/infusion", ManaPoolScenes::infusion, MANA_DEVICES);

        ResourceLocation manaSpreader = ResourceLocation.fromNamespaceAndPath("botania", "mana_spreader");
        addStoryBoard(helper, manaSpreader, "mana_spreader/spreader", ManaSpreaderScenes::spreader, MANA_DEVICES);

        ResourceLocation pureDaisy = ResourceLocation.fromNamespaceAndPath("botania", "pure_daisy");
        addStoryBoard(helper, pureDaisy, "pure_daisy/conversion", PureDaisyScenes::conversion, CRAFTING);

        ResourceLocation endoflame = ResourceLocation.fromNamespaceAndPath("botania", "endoflame");
        addStoryBoard(helper, endoflame, "endoflame/generation", EndoflameScenes::generation, GENERATING_FLORA);

        ResourceLocation petalApothecary = ResourceLocation.fromNamespaceAndPath("botania", "apothecary_default");
        addStoryBoard(helper, petalApothecary, "petal_apothecary/crafting", PetalApothecaryScenes::crafting, CRAFTING);

        ResourceLocation runicAltar = ResourceLocation.fromNamespaceAndPath("botania", "runic_altar");
        addStoryBoard(helper, runicAltar, "runic_altar/crafting", RunicAltarScenes::crafting, CRAFTING);

        ResourceLocation brewery = ResourceLocation.fromNamespaceAndPath("botania", "brewery");
        addStoryBoard(helper, brewery, "botanical_brewery/brewing", BotanicalBreweryScenes::brewing, CRAFTING);

        addStoryBoard(helper, botania("hydroangeas"), "generating_flora/hydroangeas",
            GeneratingFlowerScenes::hydroangeas, GENERATING_FLORA);
        addStoryBoard(helper, botania("thermalily"), "generating_flora/thermalily",
            GeneratingFlowerScenes::thermalily, GENERATING_FLORA);
        addStoryBoard(helper, botania("rosa_arcana"), "generating_flora/rosa_arcana",
            GeneratingFlowerScenes::rosaArcana, GENERATING_FLORA);
        addStoryBoard(helper, botania("gourmaryllis"), "generating_flora/gourmaryllis",
            GeneratingFlowerScenes::gourmaryllis, GENERATING_FLORA);
        addStoryBoard(helper, botania("munchdew"), "generating_flora/munchdew",
            GeneratingFlowerScenes::munchdew, GENERATING_FLORA);
        addStoryBoard(helper, botania("kekimurus"), "generating_flora/kekimurus",
            GeneratingFlowerScenes::kekimurus, GENERATING_FLORA);
        addStoryBoard(helper, botania("spectrolus"), "generating_flora/spectrolus",
            GeneratingFlowerScenes::spectrolus, GENERATING_FLORA);
        addStoryBoard(helper, botania("entropinnyum"), "generating_flora/entropinnyum",
            GeneratingFlowerScenes::entropinnyum, GENERATING_FLORA);
        addStoryBoard(helper, botania("narslimmus"), "generating_flora/narslimmus",
            GeneratingFlowerScenes::narslimmus, GENERATING_FLORA);
        addStoryBoard(helper, botania("rafflowsia"), "generating_flora/rafflowsia",
            GeneratingFlowerScenes::rafflowsia, GENERATING_FLORA);
        addStoryBoard(helper, botania("dandelifeon"), "generating_flora/dandelifeon",
            GeneratingFlowerScenes::dandelifeon, GENERATING_FLORA);
        addStoryBoard(helper, botania("shulk_me_not"), "generating_flora/shulk_me_not",
            GeneratingFlowerScenes::shulkMeNot, GENERATING_FLORA);

        addStoryBoard(helper, botania("agricarnation"), "functional_flora/agricarnation",
            FunctionalFlowerScenes::agricarnation, FUNCTIONAL_FLORA);
        addStoryBoard(helper, botania("clayconia"), "functional_flora/clayconia",
            FunctionalFlowerScenes::clayconia, FUNCTIONAL_FLORA);
        addStoryBoard(helper, botania("orechid"), "functional_flora/orechid",
            FunctionalFlowerScenes::orechid, FUNCTIONAL_FLORA);
        addStoryBoard(helper, botania("jaded_amaranthus"), "functional_flora/jaded_amaranthus",
            FunctionalFlowerScenes::jadedAmaranthus, FUNCTIONAL_FLORA);
        addStoryBoard(helper, botania("hopperhock"), "functional_flora/hopperhock",
            FunctionalFlowerScenes::hopperhock, FUNCTIONAL_FLORA);
        addStoryBoard(helper, botania("bellethorn"), "functional_flora/bellethorn",
            FunctionalFlowerScenes::bellethorn, FUNCTIONAL_FLORA);
        addStoryBoard(helper, botania("daffomill"), "functional_flora/daffomill",
            FunctionalFlowerScenes::daffomill, FUNCTIONAL_FLORA);
        addStoryBoard(helper, botania("exoflame"), "functional_flora/exoflame",
            FunctionalFlowerScenes::exoflame, FUNCTIONAL_FLORA);
        addStoryBoard(helper, botania("dreadthorn"), "functional_flora/dreadthorn",
            FunctionalFlowerScenes::dreadthorn, FUNCTIONAL_FLORA);
        addStoryBoard(helper, botania("pollidisiac"), "functional_flora/pollidisiac",
            FunctionalFlowerScenes::pollidisiac, FUNCTIONAL_FLORA);
        addStoryBoard(helper, botania("fallen_kanade"), "functional_flora/fallen_kanade",
            FunctionalFlowerScenes::fallenKanade, FUNCTIONAL_FLORA);
        addStoryBoard(helper, botania("tangleberrie"), "functional_flora/tangleberrie",
            FunctionalFlowerScenes::tangleberrie, FUNCTIONAL_FLORA);
        addStoryBoard(helper, botania("jiyuulia"), "functional_flora/jiyuulia",
            FunctionalFlowerScenes::jiyuulia, FUNCTIONAL_FLORA);
        addStoryBoard(helper, botania("bergamute"), "functional_flora/bergamute",
            AdvancedFunctionalFlowerScenes::bergamute, FUNCTIONAL_FLORA);
        addStoryBoard(helper, botania("bubbell"), "functional_flora/bubbell",
            AdvancedFunctionalFlowerScenes::bubbell, FUNCTIONAL_FLORA);
        addStoryBoard(helper, botania("heisei_dream"), "functional_flora/heisei_dream",
            AdvancedFunctionalFlowerScenes::heiseiDream, FUNCTIONAL_FLORA);
        addStoryBoard(helper, botania("hyacidus"), "functional_flora/hyacidus",
            AdvancedFunctionalFlowerScenes::hyacidus, FUNCTIONAL_FLORA);
        addStoryBoard(helper, botania("labellia"), "functional_flora/labellia",
            AdvancedFunctionalFlowerScenes::labellia, FUNCTIONAL_FLORA);
        addStoryBoard(helper, botania("loonium"), "functional_flora/loonium",
            AdvancedFunctionalFlowerScenes::loonium, FUNCTIONAL_FLORA);
        addStoryBoard(helper, botania("marimorphosis"), "functional_flora/marimorphosis",
            AdvancedFunctionalFlowerScenes::marimorphosis, FUNCTIONAL_FLORA);
        addStoryBoard(helper, botania("medumone"), "functional_flora/medumone",
            AdvancedFunctionalFlowerScenes::medumone, FUNCTIONAL_FLORA);
        addStoryBoard(helper, botania("orechid_ignem"), "functional_flora/orechid_ignem",
            AdvancedFunctionalFlowerScenes::orechidIgnem, FUNCTIONAL_FLORA);
        addStoryBoard(helper, botania("rannuncarpus"), "functional_flora/rannuncarpus",
            AdvancedFunctionalFlowerScenes::rannuncarpus, FUNCTIONAL_FLORA);
        addStoryBoard(helper, botania("solegnolia"), "functional_flora/solegnolia",
            AdvancedFunctionalFlowerScenes::solegnolia, FUNCTIONAL_FLORA);
        addStoryBoard(helper, botania("spectranthemum"), "functional_flora/spectranthemum",
            AdvancedFunctionalFlowerScenes::spectranthemum, FUNCTIONAL_FLORA);
        addStoryBoard(helper, botania("tigerseye"), "functional_flora/tigerseye",
            AdvancedFunctionalFlowerScenes::tigerseye, FUNCTIONAL_FLORA);
        addStoryBoard(helper, botania("vinculotus"), "functional_flora/vinculotus",
            AdvancedFunctionalFlowerScenes::vinculotus, FUNCTIONAL_FLORA);

        addStoryBoard(helper, botania("lens_normal"), "mana_lenses/normal", ManaLensScenes::normal, MANA_LENSES);
        addStoryBoard(helper, botania("lens_speed"), "mana_lenses/speed", ManaLensScenes::speed, MANA_LENSES);
        addStoryBoard(helper, botania("lens_power"), "mana_lenses/power", ManaLensScenes::power, MANA_LENSES);
        addStoryBoard(helper, botania("lens_time"), "mana_lenses/time", ManaLensScenes::time, MANA_LENSES);
        addStoryBoard(helper, botania("lens_efficiency"), "mana_lenses/efficiency", ManaLensScenes::efficiency, MANA_LENSES);
        addStoryBoard(helper, botania("lens_bounce"), "mana_lenses/bounce", ManaLensScenes::bounce, MANA_LENSES);
        addStoryBoard(helper, botania("lens_gravity"), "mana_lenses/gravity", ManaLensScenes::gravity, MANA_LENSES);
        addStoryBoard(helper, botania("lens_mine"), "mana_lenses/mine", ManaLensScenes::mine, MANA_LENSES);
        addStoryBoard(helper, botania("lens_damage"), "mana_lenses/damage", ManaLensScenes::damage, MANA_LENSES);
        addStoryBoard(helper, botania("lens_phantom"), "mana_lenses/phantom", ManaLensScenes::phantom, MANA_LENSES);
        addStoryBoard(helper, botania("lens_magnet"), "mana_lenses/magnet", ManaLensScenes::magnet, MANA_LENSES);
        addStoryBoard(helper, botania("lens_explosive"), "mana_lenses/explosive", ManaLensScenes::explosive, MANA_LENSES);
        addStoryBoard(helper, botania("lens_influence"), "mana_lenses/influence", ManaLensScenes::influence, MANA_LENSES);
        addStoryBoard(helper, botania("lens_weight"), "mana_lenses/weight", ManaLensScenes::weight, MANA_LENSES);
        addStoryBoard(helper, botania("lens_paint"), "mana_lenses/paint", ManaLensScenes::paint, MANA_LENSES);
        addStoryBoard(helper, botania("lens_fire"), "mana_lenses/fire", ManaLensScenes::fire, MANA_LENSES);
        addStoryBoard(helper, botania("lens_piston"), "mana_lenses/piston", ManaLensScenes::piston, MANA_LENSES);
        addStoryBoard(helper, botania("lens_light"), "mana_lenses/light", ManaLensScenes::light, MANA_LENSES);
        addStoryBoard(helper, botania("lens_warp"), "mana_lenses/warp", ManaLensScenes::warp, MANA_LENSES);
        addStoryBoard(helper, botania("lens_redirect"), "mana_lenses/redirect", ManaLensScenes::redirect, MANA_LENSES);
        addStoryBoard(helper, botania("lens_firework"), "mana_lenses/firework", ManaLensScenes::firework, MANA_LENSES);
        addStoryBoard(helper, botania("lens_flare"), "mana_lenses/flare", ManaLensScenes::flare, MANA_LENSES);
        addStoryBoard(helper, botania("lens_messenger"), "mana_lenses/messenger", ManaLensScenes::messenger, MANA_LENSES);
        addStoryBoard(helper, botania("lens_tripwire"), "mana_lenses/tripwire", ManaLensScenes::tripwire, MANA_LENSES);
        addStoryBoard(helper, botania("lens_storm"), "mana_lenses/storm", ManaLensScenes::storm, MANA_LENSES);

        addStoryBoard(helper, botania("mana_void"), "mana_devices/mana_void",
            ManaDeviceScenes::manaVoid, MANA_DEVICES);
        addStoryBoard(helper, botania("mana_detector"), "mana_devices/mana_detector",
            ManaDeviceScenes::manaDetector, MANA_DEVICES);
        addStoryBoard(helper, botania("mana_distributor"), "mana_devices/mana_distributor",
            ManaDeviceScenes::manaDistributor, MANA_DEVICES);
        addStoryBoard(helper, botania("open_crate"), "mana_devices/open_crate",
            ManaDeviceScenes::openCrate, MANA_DEVICES);
        addStoryBoard(helper, botania("turntable"), "mana_devices/spreader_turntable",
            ManaDeviceScenes::spreaderTurntable, MANA_DEVICES);

        addStoryBoard(helper, botania("red_string_container"), "red_string/container",
            RedStringScenes::container, RED_STRING);
        addStoryBoard(helper, botania("red_string_dispenser"), "red_string/dispenser",
            RedStringScenes::dispenser, RED_STRING);
        addStoryBoard(helper, botania("red_string_fertilizer"), "red_string/nutrifier",
            RedStringScenes::nutrifier, RED_STRING);
        addStoryBoard(helper, botania("red_string_comparator"), "red_string/comparator",
            RedStringScenes::comparator, RED_STRING);
        addStoryBoard(helper, botania("red_string_relay"), "red_string/relay",
            RedStringScenes::relay, RED_STRING);
        addStoryBoard(helper, botania("red_string_interceptor"), "red_string/interceptor",
            RedStringScenes::interceptor, RED_STRING);
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
            botania("daffomill"), botania("exoflame"), botania("dreadthorn"),
            botania("pollidisiac"), botania("fallen_kanade"), botania("tangleberrie"),
            botania("jiyuulia"), botania("bergamute"), botania("bubbell"),
            botania("heisei_dream"), botania("hyacidus"), botania("labellia"),
            botania("loonium"), botania("marimorphosis"), botania("medumone"),
            botania("orechid_ignem"), botania("rannuncarpus"), botania("solegnolia"),
            botania("spectranthemum"), botania("tigerseye"), botania("vinculotus"));

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

        helper.registerTag(MANA_LENSES)
            .title("Mana Lenses")
            .description("Lenses that modify Mana Burst movement, capacity, and impact behavior")
            .icon(botaniaItem("lens_normal"))
            .addToIndex()
            .register();
        helper.addToTag(MANA_LENSES,
            botania("lens_normal"), botania("lens_speed"), botania("lens_power"),
            botania("lens_time"), botania("lens_efficiency"), botania("lens_bounce"),
            botania("lens_gravity"), botania("lens_mine"), botania("lens_damage"),
            botania("lens_phantom"), botania("lens_magnet"), botania("lens_explosive"),
            botania("lens_influence"), botania("lens_weight"), botania("lens_paint"),
            botania("lens_fire"), botania("lens_piston"), botania("lens_light"),
            botania("lens_warp"), botania("lens_redirect"), botania("lens_firework"),
            botania("lens_flare"), botania("lens_messenger"), botania("lens_tripwire"),
            botania("lens_storm"));

        helper.registerTag(RED_STRING)
            .title("Red String")
            .description("Blocks that link their behavior to a distant compatible target")
            .icon(botaniaBlock("red_string_container"))
            .addToIndex()
            .register();
        helper.addToTag(RED_STRING,
            botania("red_string_container"), botania("red_string_dispenser"),
            botania("red_string_fertilizer"), botania("red_string_comparator"),
            botania("red_string_relay"), botania("red_string_interceptor"));
    }

    private static ResourceLocation botania(String path) {
        return ResourceLocation.fromNamespaceAndPath("botania", path);
    }

    private static void addStoryBoard(PonderSceneRegistrationHelper helper,
                                      ResourceLocation component, String schematicPath,
                                      PonderStoryBoard storyBoard, ResourceLocation tag) {
        helper.addStoryBoard(component, schematicPath, storyBoard,
            Set.of(tag), Set.of(), Set.of());
    }

    private static ResourceLocation tag(String path) {
        return ResourceLocation.fromNamespaceAndPath(BotaniaPonder.MODID, path);
    }

    private static Block botaniaBlock(String path) {
        return Objects.requireNonNull(ForgeRegistries.BLOCKS.getValue(botania(path)));
    }

    private static Item botaniaItem(String path) {
        return Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(botania(path)));
    }
}
