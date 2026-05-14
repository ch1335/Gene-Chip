package com.chen1335.geneChip.API.object;

import com.chen1335.geneChip.GeneChip;
import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.chips.combat.*;
import com.chen1335.geneChip.chip.chips.mutation.AdrenalGlandBurst;
import com.chen1335.geneChip.chip.chips.mutation.GrowingFervor;
import com.chen1335.geneChip.chip.chips.mutation.MutationAdaptation;
import com.chen1335.geneChip.chip.chips.survival.Photosynthesis;
import com.chen1335.geneChip.chip.chips.survival.*;
import com.chen1335.geneChip.chip.chips.tactics.SlidingTackle;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ChipTypes {
    public static final DeferredRegister<Chip> CHIPS = DeferredRegister.create(RegisterTypes.CHIP, GeneChip.MODID);

    public static final DeferredHolder<Chip, HeadShotHunter> HEAD_SHOT_HUNTER = CHIPS.register("head_shot_hunter", HeadShotHunter::new);

    public static final DeferredHolder<Chip, MakeLiving> MAKE_LIVING = CHIPS.register("make_living", MakeLiving::new);

    public static final DeferredHolder<Chip, PrecisionShooting> PRECISION_SHOOTING = CHIPS.register("precision_shooting", PrecisionShooting::new);
    public static final DeferredHolder<Chip, Bloodthirsty> BLOODTHIRSTY = CHIPS.register("bloodthirsty", Bloodthirsty::new);

    public static final DeferredHolder<Chip, ComboFever> COMBO_FEVER = CHIPS.register("combo_fever", ComboFever::new);

    public static final DeferredHolder<Chip, MeleeAttackMaster> MELEE_ATTACK_MASTER = CHIPS.register("melee_attack_master", MeleeAttackMaster::new);

    public static final DeferredHolder<Chip, QuickAdjustment> QUICK_ADJUSTMENT = CHIPS.register("quick_adjustment", QuickAdjustment::new);

    public static final DeferredHolder<Chip, DesireForSlaughter> DESIRE_FOR_SLAUGHTER = CHIPS.register("desire_for_slaughter", DesireForSlaughter::new);

    public static final DeferredHolder<Chip, MutationAdaptation> MUTATION_ADAPTATION = CHIPS.register("mutation_adaptation", MutationAdaptation::new);

    public static final DeferredHolder<Chip, AdrenalGlandBurst> ADRENAL_GLAND_BURST = CHIPS.register("adrenal_gland_burst", AdrenalGlandBurst::new);

    public static final DeferredHolder<Chip, GrowingFervor> GROWING_FERVOR = CHIPS.register("growing_fervor", GrowingFervor::new);

    public static final DeferredHolder<Chip, Endurance> ENDURANCE = CHIPS.register("endurance", Endurance::new);

    public static final DeferredHolder<Chip, WildHunter> WILD_HUNTER = CHIPS.register("wild_hunter", WildHunter::new);

    public static final DeferredHolder<Chip, SewagePurificationPack> SEWAGE_PURIFICATION_PACK = CHIPS.register("sewage_purification_pack", SewagePurificationPack::new);

    public static final DeferredHolder<Chip, NutrientExtraction> NUTRIENT_EXTRACTION = CHIPS.register("nutrient_extraction", NutrientExtraction::new);

    public static final DeferredHolder<Chip, PermafrostWalkers> PERMAFROST_WALKERS = CHIPS.register("permafrost_walkers", PermafrostWalkers::new);

    public static final DeferredHolder<Chip, IronLung> IRON_LUNG = CHIPS.register("iron_lung", IronLung::new);

    public static final DeferredHolder<Chip, ThickSkinned> THICK_SKINNED = CHIPS.register("thick_skinned", ThickSkinned::new);

    public static final DeferredHolder<Chip, Photosynthesis> PHOTOSYNTHESIS = CHIPS.register("photosynthesis", Photosynthesis::new);

    public static final DeferredHolder<Chip, SlidingTackle> SLIDING_TACKLE = CHIPS.register("sliding_tackle", SlidingTackle::new);
}
