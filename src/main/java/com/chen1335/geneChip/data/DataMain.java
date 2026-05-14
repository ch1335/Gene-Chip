package com.chen1335.geneChip.data;

import com.chen1335.geneChip.GeneChip;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.DataGenerator;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Map;
import java.util.Set;

@EventBusSubscriber(modid = GeneChip.MODID)
public class DataMain {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();

        DatapackBuiltinEntriesProvider builtinEntriesProvider = generator.addProvider(event.includeServer(), new DatapackBuiltinEntriesProvider(
                generator.getPackOutput(),
                event.getLookupProvider(),
                new RegistrySetBuilder()

                ,
                Map.of(),
                Set.of(GeneChip.MODID)
        ));


        generator.addProvider(event.includeServer(), new GCEntityTypeTagsProvider(generator.getPackOutput(), event.getLookupProvider(), event.getExistingFileHelper()));
    }

}
