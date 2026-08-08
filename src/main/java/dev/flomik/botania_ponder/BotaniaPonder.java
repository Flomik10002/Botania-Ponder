package dev.flomik.botania_ponder;

import com.mojang.logging.LogUtils;
import dev.flomik.botania_ponder.datagen.BotaniaPonderStructureProvider;
import dev.flomik.ponderlib.foundation.PonderIndex;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(BotaniaPonder.MODID)
public class BotaniaPonder {

    public static final String MODID = "botania_ponder";
    private static final Logger LOGGER = LogUtils.getLogger();

    public BotaniaPonder(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::gatherData);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        PonderIndex.addPlugin(new BotaniaPonderPlugin());
        LOGGER.info("Registered Botania Ponder scenes with PonderLib");
    }

    private void gatherData(final GatherDataEvent event) {
        event.getGenerator().addProvider(event.includeClient(), new BotaniaPonderStructureProvider(event.getGenerator().getPackOutput()));
    }
}
