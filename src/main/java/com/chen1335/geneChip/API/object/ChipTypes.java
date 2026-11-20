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
import com.chen1335.geneChip.chip.chips.tactics.FlyingKick;
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

import java.util.List;
import java.util.function.Supplier;

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

    public static final DeferredHolder<Chip, FlyingKick> FLYING_KICK = CHIPS.register("flying_kick", FlyingKick::new);

    public static final DeferredHolder<Chip, VengefulFlame> VENGEFUL_FLAME = CHIPS.register("vengeful_flame", VengefulFlame::new);

    public static final DeferredHolder<Chip, NightHunter> NIGHT_HUNTER = CHIPS.register("night_hunter", NightHunter::new);

    public static final DeferredHolder<Chip, DawnAwakening> DAWN_AWAKENING = CHIPS.register("dawn_awakening", DawnAwakening::new);

    public static final DeferredHolder<Chip, IronHeart> IRON_HEART = CHIPS.register("iron_heart", IronHeart::new);

    public static final DeferredHolder<Chip, CounterStorm> COUNTER_STORM = CHIPS.register("counter_storm", CounterStorm::new);

    public static final DeferredHolder<Chip, LocksmithIntuition> LOCKSMITH_INTUITION = CHIPS.register("locksmith_intuition", LocksmithIntuition::new);

    public static final DeferredHolder<Chip, HunterInstinct> HUNTER_INSTINCT = CHIPS.register("hunter_instinct", HunterInstinct::new);

    /**
     * 芯片清单：供配置系统在注册表填充前枚举每颗芯片的参数（生成 TOML 分段与注释）。
     * <p>
     * 这里的 id / 工厂必须与上面的 {@code CHIPS.register(...)} 保持一致；
     * {@code ChipConfig} 在加载时会做一次一致性校验，不一致会打日志。
     */
    public record ChipEntry(String id, String chineseName, Supplier<Chip> factory) {
    }

    public static final List<ChipEntry> CHIP_ENTRIES = List.of(
            new ChipEntry("head_shot_hunter", "爆头猎手", HeadShotHunter::new),
            new ChipEntry("make_living", "维持生计", MakeLiving::new),
            new ChipEntry("precision_shooting", "精准射击", PrecisionShooting::new),
            new ChipEntry("bloodthirsty", "嗜血", Bloodthirsty::new),
            new ChipEntry("wild_hunter", "野外猎手", WildHunter::new),
            new ChipEntry("sewage_purification_pack", "污水净化包", SewagePurificationPack::new),
            new ChipEntry("endurance", "持久耐力", Endurance::new),
            new ChipEntry("combo_fever", "连击热潮", ComboFever::new),
            new ChipEntry("melee_attack_master", "近战专家", MeleeAttackMaster::new),
            new ChipEntry("quick_adjustment", "快速调整", QuickAdjustment::new),
            new ChipEntry("desire_for_slaughter", "杀戮渴望", DesireForSlaughter::new),
            new ChipEntry("mutation_adaptation", "突变适应", MutationAdaptation::new),
            new ChipEntry("adrenal_gland_burst", "肾上腺爆发", AdrenalGlandBurst::new),
            new ChipEntry("growing_fervor", "愈发狂热", GrowingFervor::new),
            new ChipEntry("pain_blockade", "痛觉封锁", PainBlockade::new),
            new ChipEntry("big_eater", "大胃王", BigEater::new),
            new ChipEntry("infected", "感染者", Infected::new),
            new ChipEntry("scrap_collector", "废品收集者", ScrapCollector::new),
            new ChipEntry("nutrient_extraction", "养分萃取", NutrientExtraction::new),
            new ChipEntry("rotten_flesh_tolerance", "腐肉耐受", RottenFleshTolerance::new),
            new ChipEntry("trauma_first_aid", "外伤急救", TraumaFirstAid::new),
            new ChipEntry("permafrost_walkers", "冻土行者", () -> new PermafrostWalkers(GeneChip.id("permafrost_walkers"))),
            new ChipEntry("iron_lung", "铁肺", IronLung::new),
            new ChipEntry("thick_skinned", "皮糙肉厚", ThickSkinned::new),
            new ChipEntry("photosynthesis", "光合作用", Photosynthesis::new),
            new ChipEntry("sliding_tackle", "战术滑铲", SlidingTackle::new),
            new ChipEntry("silent_walker", "静步者", SilentWalker::new),
            new ChipEntry("double_jump", "二段跳", DoubleJump::new),
            new ChipEntry("spider_climb", "蛛行", SpiderClimb::new),
            new ChipEntry("tactical_roll", "战术翻滚", TacticalRoll::new),
            new ChipEntry("flying_kick", "飞身踢", FlyingKick::new),
            new ChipEntry("vengeful_flame", "复仇火焰", VengefulFlame::new),
            new ChipEntry("night_hunter", "暗夜猎手", NightHunter::new),
            new ChipEntry("dawn_awakening", "黎明觉醒", DawnAwakening::new),
            new ChipEntry("iron_heart", "钢铁之心", IronHeart::new),
            new ChipEntry("counter_storm", "反击风暴", CounterStorm::new),
            new ChipEntry("locksmith_intuition", "锁匠直觉", LocksmithIntuition::new),
            new ChipEntry("hunter_instinct", "猎手本能", HunterInstinct::new)
    );
}
