package com.chen1335.geneChip.API.object;

import com.chen1335.geneChip.GeneChip;
import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.chips.combat.*;
import com.chen1335.geneChip.chip.chips.mutation.AdrenalGlandBurst;
import com.chen1335.geneChip.chip.chips.mutation.GrowingFervor;
import com.chen1335.geneChip.chip.chips.mutation.MutationAdaptation;
import com.chen1335.geneChip.chip.chips.mutation.PainBlockade;
import com.chen1335.geneChip.chip.chips.survival.Photosynthesis;
import com.chen1335.geneChip.chip.chips.survival.*;
import com.chen1335.geneChip.chip.chips.tactics.DoubleJump;
import com.chen1335.geneChip.chip.chips.tactics.SilentWalker;
import com.chen1335.geneChip.chip.chips.tactics.SlidingTackle;
import com.chen1335.geneChip.chip.chips.tactics.SpiderClimb;
import com.chen1335.geneChip.chip.chips.tactics.TacticalRoll;
import com.chen1335.geneChip.chip.chips.special.VengefulFlame;
import com.chen1335.geneChip.chip.chips.special.NightHunter;
import com.chen1335.geneChip.chip.chips.special.DawnAwakening;
import com.chen1335.geneChip.chip.chips.special.IronHeart;
import com.chen1335.geneChip.chip.chips.special.CounterStorm;
import com.chen1335.geneChip.chip.chips.special.LocksmithIntuition;
import com.chen1335.geneChip.chip.chips.special.HunterInstinct;
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

    public static final DeferredHolder<Chip, PainBlockade> PAIN_BLOCKADE = CHIPS.register("pain_blockade", PainBlockade::new);

    public static final DeferredHolder<Chip, AdrenalGlandBurst> ADRENAL_GLAND_BURST = CHIPS.register("adrenal_gland_burst", AdrenalGlandBurst::new);

    public static final DeferredHolder<Chip, GrowingFervor> GROWING_FERVOR = CHIPS.register("growing_fervor", GrowingFervor::new);

    public static final DeferredHolder<Chip, Endurance> ENDURANCE = CHIPS.register("endurance", Endurance::new);

    public static final DeferredHolder<Chip, BigEater> BIG_EATER = CHIPS.register("big_eater", BigEater::new);

    public static final DeferredHolder<Chip, Infected> INFECTED = CHIPS.register("infected", Infected::new);

    public static final DeferredHolder<Chip, ScrapCollector> SCRAP_COLLECTOR = CHIPS.register("scrap_collector", ScrapCollector::new);

    public static final DeferredHolder<Chip, WildHunter> WILD_HUNTER = CHIPS.register("wild_hunter", WildHunter::new);

    public static final DeferredHolder<Chip, SewagePurificationPack> SEWAGE_PURIFICATION_PACK = CHIPS.register("sewage_purification_pack", SewagePurificationPack::new);

    public static final DeferredHolder<Chip, NutrientExtraction> NUTRIENT_EXTRACTION = CHIPS.register("nutrient_extraction", NutrientExtraction::new);

    public static final DeferredHolder<Chip, RottenFleshTolerance> ROTTEN_FLESH_TOLERANCE = CHIPS.register("rotten_flesh_tolerance", RottenFleshTolerance::new);

    public static final DeferredHolder<Chip, TraumaFirstAid> TRAUMA_FIRST_AID = CHIPS.register("trauma_first_aid", TraumaFirstAid::new);

    public static final DeferredHolder<Chip, PermafrostWalkers> PERMAFROST_WALKERS = CHIPS.register("permafrost_walkers", PermafrostWalkers::new);

    public static final DeferredHolder<Chip, IronLung> IRON_LUNG = CHIPS.register("iron_lung", IronLung::new);

    public static final DeferredHolder<Chip, ThickSkinned> THICK_SKINNED = CHIPS.register("thick_skinned", ThickSkinned::new);

    public static final DeferredHolder<Chip, Photosynthesis> PHOTOSYNTHESIS = CHIPS.register("photosynthesis", Photosynthesis::new);

    public static final DeferredHolder<Chip, SlidingTackle> SLIDING_TACKLE = CHIPS.register("sliding_tackle", SlidingTackle::new);

    public static final DeferredHolder<Chip, SilentWalker> SILENT_WALKER = CHIPS.register("silent_walker", SilentWalker::new);

    public static final DeferredHolder<Chip, DoubleJump> DOUBLE_JUMP = CHIPS.register("double_jump", DoubleJump::new);

    public static final DeferredHolder<Chip, SpiderClimb> SPIDER_CLIMB = CHIPS.register("spider_climb", SpiderClimb::new);

    public static final DeferredHolder<Chip, TacticalRoll> TACTICAL_ROLL = CHIPS.register("tactical_roll", TacticalRoll::new);

    public static final DeferredHolder<Chip, VengefulFlame> VENGEFUL_FLAME = CHIPS.register("vengeful_flame", VengefulFlame::new);

    public static final DeferredHolder<Chip, NightHunter> NIGHT_HUNTER = CHIPS.register("night_hunter", NightHunter::new);

    public static final DeferredHolder<Chip, DawnAwakening> DAWN_AWAKENING = CHIPS.register("dawn_awakening", DawnAwakening::new);

    public static final DeferredHolder<Chip, IronHeart> IRON_HEART = CHIPS.register("iron_heart", IronHeart::new);

    public static final DeferredHolder<Chip, CounterStorm> COUNTER_STORM = CHIPS.register("counter_storm", CounterStorm::new);

    public static final DeferredHolder<Chip, LocksmithIntuition> LOCKSMITH_INTUITION = CHIPS.register("locksmith_intuition", LocksmithIntuition::new);

    public static final DeferredHolder<Chip, HunterInstinct> HUNTER_INSTINCT = CHIPS.register("hunter_instinct", HunterInstinct::new);
}
