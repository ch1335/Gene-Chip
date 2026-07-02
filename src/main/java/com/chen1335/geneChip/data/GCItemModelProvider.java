package com.chen1335.geneChip.data;

import com.chen1335.geneChip.API.object.GCItems;
import com.chen1335.geneChip.GeneChip;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class GCItemModelProvider extends ItemModelProvider {
    public GCItemModelProvider(PackOutput output,  ExistingFileHelper existingFileHelper) {
        super(output, GeneChip.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {

    }
}
