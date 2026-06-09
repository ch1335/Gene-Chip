package com.chen1335.geneChip.mixins.clavis;

import com.chen1335.geneChip.API.ILockExtension;
import it.hurts.shatterbyte.clavis.common.data.Lock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Lock.class)
public class LockMixin implements ILockExtension {

    @Shadow
    float difficulty;

    @Unique
    public void GC$setDifficulty(float difficulty) {
        this.difficulty = difficulty;
    }

    @Unique
    public float GC$getDifficulty() {
       return this.difficulty;
    }
}
