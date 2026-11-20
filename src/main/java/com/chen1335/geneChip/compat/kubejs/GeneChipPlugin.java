package com.chen1335.geneChip.compat.kubejs;

import com.chen1335.geneChip.API.GeneChipAPI;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingRegistry;

public class GeneChipPlugin implements KubeJSPlugin {
    @Override
    public void registerBindings(BindingRegistry bindings) {
        bindings.add("GeneChipAPI", GeneChipAPI.class);
    }
}
