package com.chen1335.geneChip.config;

import java.util.HashMap;
import java.util.Map;

/**
 * 芯片参数的说明文案：生成配置注释时使用，直接说明「这个参数是干什么的」以及数值含义。
 * <p>
 * 键是 {@code registerConfigValue(...)} 里的参数 id；值与语言文件里的短标签不同，
 * 是写进 TOML 注释的作用说明（语言文件在配置构建时还没加载，所以这里用静态表）。
 */
public final class ChipParamDescriptions {
    private static final Map<String, String> DESCRIPTIONS = new HashMap<>();

    static {
        // 通用
        DESCRIPTIONS.put("weight", "抽卡权重：数值越大，抽到这枚芯片的概率越高。");
        DESCRIPTIONS.put("cooldown", "技能冷却时间（秒）。");
        DESCRIPTIONS.put("effect_time", "增益效果的持续时间（秒）。");
        DESCRIPTIONS.put("threshold", "触发阈值：生命值低于该比例时触发（0.3 = 低于 30%）。");
        DESCRIPTIONS.put("damage_mul", "芯片提供的伤害加成比例（0.25 = 伤害 +25%）。");
        DESCRIPTIONS.put("damage_boost", "被标记目标受到伤害的提升比例（0.2 = 受到伤害 +20%）。");
        DESCRIPTIONS.put("damage_reduction", "受到的伤害减免比例（0.5 = 减免 50%）。");
        DESCRIPTIONS.put("damage_multiplier", "命中时附加的近战伤害倍率（1.5 = 1.5 倍伤害）。");
        DESCRIPTIONS.put("impact_damage_multiplier", "目标撞上方块时的二次撞击伤害倍率。");
        DESCRIPTIONS.put("damage_reflect_ratio", "反击时附加的「已受伤伤害」比例（0.5 = 附加 50%）。");
        DESCRIPTIONS.put("melee_damage_bonus", "无甲状态下的近战伤害加成比例（0.2 = +20%）。");
        DESCRIPTIONS.put("max_damage_reduction", "坠落距离较近时的摔落伤害减免上限（0.8 = 减免 80%）。");
        DESCRIPTIONS.put("min_damage_reduction", "坠落距离很远时的摔落伤害减免下限（0.2 = 减免 20%）。");
        DESCRIPTIONS.put("reference_distance", "摔落减伤随距离衰减的参考距离（米）。");
        DESCRIPTIONS.put("fall_damage_reduction", "攀爬 / 贴墙时的摔落伤害减免比例（0.8 = 减免 80%）。");
        DESCRIPTIONS.put("duration_reduction", "负面效果持续时间的缩短比例（0.5 = 缩短 50%）。");
        DESCRIPTIONS.put("effect_extension", "正面效果持续时间的延长比例（0.5 = 延长 50%）。");
        DESCRIPTIONS.put("knockback_distance", "命中后的击退距离（格）。");
        DESCRIPTIONS.put("stun_duration", "眩晕目标的时间（秒）。");
        DESCRIPTIONS.put("heal", "进食时额外恢复的生命值（点）。");
        DESCRIPTIONS.put("heal_amount", "触发时恢复的生命值（点）。");
        DESCRIPTIONS.put("heal_chance", "击杀敌对生物时触发回血的概率（0.3 = 30%）。");
        DESCRIPTIONS.put("recovery_chance", "食用腐坏食物时触发回血的概率（0.1 = 10%）。");
        DESCRIPTIONS.put("recovery_amount", "触发回血时恢复的生命值（点）。");
        DESCRIPTIONS.put("speed_per_stack", "光合作用每层充能提供的移速加成（0.01 = 每层 +1%）。");
        DESCRIPTIONS.put("saturation_recovery", "光合作用每 30 秒恢复的饱和度（点）。");
        DESCRIPTIONS.put("sneak_armor_bonus", "蹲下时额外获得的护甲值（点）。");
        DESCRIPTIONS.put("extra_heal_ratio", "外伤急救额外恢复的比例（0.5 = 额外恢复原治疗量的 50%）。");
        DESCRIPTIONS.put("heal_on_kill", "击杀敌人时恢复的生命值（点）。");
        DESCRIPTIONS.put("health_restore", "黎明结算时恢复的生命值（点）。");
        DESCRIPTIONS.put("kill_chance", "暴击时直接击杀目标的概率（0.3 = 30%）。");
        DESCRIPTIONS.put("recycling_chance", "射击命中后回收弹药的概率（0.2 = 20%，霰弹按弹丸数分摊）。");
        DESCRIPTIONS.put("extra_loot_chance", "开启战利品容器时额外掉落战利品的概率（0.2 = 20%）。");
        DESCRIPTIONS.put("food_add", "进食时额外恢复的饥饿值（点）。");
        DESCRIPTIONS.put("food_cost", "触发效果时消耗的饱食度（点）。");
        DESCRIPTIONS.put("saturation_cost", "触发效果时消耗的饱和度（点）。");
        DESCRIPTIONS.put("hunger_cost", "标记实体时消耗的饥饿值（点）。");
        DESCRIPTIONS.put("immunity_cost", "装备时额外扣除的当前免疫力比例（0.05 = 扣除 5%）。");
        DESCRIPTIONS.put("mark_duration", "标记持续时间（秒）。");
        DESCRIPTIONS.put("max_time", "连击热潮的连续击杀判定窗口（秒）。");
        DESCRIPTIONS.put("explosion_radius", "死亡爆炸的半径（格，伤害与点燃都按此球形范围判定）。");
        DESCRIPTIONS.put("explosion_damage", "爆炸对半径内每个敌人造成的固定伤害（点，不随距离衰减）。");
        DESCRIPTIONS.put("burn_duration", "点燃敌人的持续时间（秒）。");
        DESCRIPTIONS.put("roll_distance", "战术翻滚的位移距离（格）。");
        DESCRIPTIONS.put("invincible_time", "翻滚期间的无敌时间（秒）。");
        DESCRIPTIONS.put("off_balance_time", "翻滚后失衡状态的持续时间（秒）。");
        DESCRIPTIONS.put("off_balance_slow", "翻滚后失衡状态的移速降低比例（0.7 = 降低 70%）。");
        DESCRIPTIONS.put("slide_distance", "战术滑铲的位移距离（格）。");
        DESCRIPTIONS.put("slide_time", "战术滑铲的持续时间（tick，20 = 1 秒）。");
        DESCRIPTIONS.put("climb_speed", "攀爬时向上的速度（方块/tick，0.3 ≈ 每秒 6 格）。");
        DESCRIPTIONS.put("cold_resistance", "寒冷环境下的体温保护强度（25 = 默认强度）。");
        DESCRIPTIONS.put("decrease_air_supply_mul", "水下氧气消耗的降低比例（0.5 = 消耗降低 50%）。");
        DESCRIPTIONS.put("exhaustion_reduce", "奔跑 / 跳跃时饥饿消耗的减少比例（0.5 = 降低 50%）。");
        DESCRIPTIONS.put("exhaustion_duration", "二段跳落地后缓慢效果的持续时间（秒）。");
        DESCRIPTIONS.put("drain_reduction", "感染区内免疫力流失速度的降低比例（0.3 = 降低 30%）。");
        DESCRIPTIONS.put("digestion_boost", "消化速度提升比例（0.1 = +10%）。");
        DESCRIPTIONS.put("stomach_bonus", "胃容量上限增加量（点）。");
        DESCRIPTIONS.put("difficulty_reduction", "开锁小游戏难度的降低比例（0.6 = 难度降低 60%）。");
        DESCRIPTIONS.put("quality_threshold", "判定为「完美开锁」的品质阈值（1.8 = 达到 1.8 倍品质）。");
        DESCRIPTIONS.put("reflect_window", "受到伤害后可以发动反击的有效窗口（秒）。");
    }

    /**
     * 经验项说明：键是 {@code 芯片id|经验项id}，值是「干什么获得的经验」。
     * 同一个经验项 id 在不同芯片里含义可能不同，所以按芯片区分。
     */
    private static final Map<String, String> EXP_SOURCES = new HashMap<>();

    static {
        EXP_SOURCES.put("head_shot_hunter|crit_exp", "暴击秒杀普通感染者时获得");
        EXP_SOURCES.put("make_living|recycle_exp", "射击命中并回收弹药时获得");
        EXP_SOURCES.put("precision_shooting|headshot_exp", "枪械爆头命中时获得");
        EXP_SOURCES.put("precision_shooting|hit_exp", "枪械普通命中（非爆头）时获得");
        EXP_SOURCES.put("bloodthirsty|heal_exp", "击杀敌对生物触发回血时获得");
        EXP_SOURCES.put("wild_hunter|animal_kill_exp", "击杀动物时获得");
        EXP_SOURCES.put("sewage_purification_pack|purify_exp", "饮用脏水被净化时获得");
        EXP_SOURCES.put("endurance|hunger_exp", "奔跑 / 跳跃消耗饥饿值（每 1 点）时获得");
        EXP_SOURCES.put("combo_fever|window_kill_exp", "连击窗口内击杀敌人时获得");
        EXP_SOURCES.put("melee_attack_master|hit_exp", "近战命中时获得");
        EXP_SOURCES.put("melee_attack_master|kill_exp", "近战击杀时获得");
        EXP_SOURCES.put("quick_adjustment|ranged_kill_exp", "远程命中敌人时获得");
        EXP_SOURCES.put("quick_adjustment|kill_exp", "击杀敌人时获得");
        EXP_SOURCES.put("desire_for_slaughter|kill_exp", "近战击杀时获得");
        EXP_SOURCES.put("mutation_adaptation|zone_passive_exp", "滞留感染区时获得（每分钟结算）");
        EXP_SOURCES.put("mutation_adaptation|zone_kill_exp", "在感染区击杀时获得");
        EXP_SOURCES.put("adrenal_gland_burst|burst_kill_exp", "肾上腺爆发期间击杀时获得");
        EXP_SOURCES.put("adrenal_gland_burst|burst_hurt_exp", "肾上腺爆发期间受伤时获得");
        EXP_SOURCES.put("growing_fervor|stage1_exp", "免疫值高于 75% 时滞留获得（每分钟结算）");
        EXP_SOURCES.put("growing_fervor|stage2_exp", "免疫值 50%~75% 时滞留获得（每分钟结算）");
        EXP_SOURCES.put("growing_fervor|stage3_exp", "免疫值 25%~50% 时滞留获得（每分钟结算）");
        EXP_SOURCES.put("growing_fervor|stage4_exp", "免疫值低于 25% 时滞留获得（每分钟结算）");
        EXP_SOURCES.put("pain_blockade|hit_exp", "无甲近战命中时获得（每 2 点伤害）");
        EXP_SOURCES.put("pain_blockade|kill_exp", "无甲近战击杀时获得");
        EXP_SOURCES.put("big_eater|eat_exp", "HRR 消化恢复饥饿值时获得（每 1 点饥饿值）");
        EXP_SOURCES.put("infected|passive_exp", "滞留感染区时获得（每分钟结算）");
        EXP_SOURCES.put("infected|buff_extend_exp", "延长正面效果时获得");
        EXP_SOURCES.put("scrap_collector|open_exp", "开启战利品容器时获得");
        EXP_SOURCES.put("scrap_collector|extra_loot_exp", "触发额外掉落时获得");
        EXP_SOURCES.put("nutrient_extraction|eat_exp", "进食时获得（每 1 点进入胃里的饥饿值）");
        EXP_SOURCES.put("nutrient_extraction|extra_exp", "额外恢复饱和度 / 生命值时获得");
        EXP_SOURCES.put("rotten_flesh_tolerance|eat_exp", "食用腐坏食物时获得");
        EXP_SOURCES.put("trauma_first_aid|heal_item_exp", "使用治疗物品时获得");
        EXP_SOURCES.put("permafrost_walkers|passive_exp", "处于低温环境时获得（每分钟结算）");
        EXP_SOURCES.put("permafrost_walkers|snow_walk_exp", "在细雪上行走时获得");
        EXP_SOURCES.put("iron_lung|breath_exp", "水下呼吸时获得");
        EXP_SOURCES.put("thick_skinned|hurt_exp", "受到伤害时获得（每 2 点伤害）");
        EXP_SOURCES.put("photosynthesis|stack_exp", "在阳光下获得光照层数时获得");
        EXP_SOURCES.put("photosynthesis|passive_exp", "白天光照下滞留时获得（每分钟结算）");
        EXP_SOURCES.put("sliding_tackle|slide_exp", "滑铲时获得");
        EXP_SOURCES.put("silent_walker|fall_exp", "坠落减伤生效时获得（每 3 格）");
        EXP_SOURCES.put("double_jump|jump_exp", "二段跳时获得");
        EXP_SOURCES.put("spider_climb|climb_exp", "攀爬时获得");
        EXP_SOURCES.put("tactical_roll|roll_exp", "翻滚时获得");
        EXP_SOURCES.put("tactical_roll|dodge_exp", "翻滚躲开伤害时获得");
        EXP_SOURCES.put("flying_kick|hit_exp", "飞踢命中目标时获得");
        EXP_SOURCES.put("flying_kick|impact_exp", "目标撞上方块时获得");
        EXP_SOURCES.put("flying_kick|stun_exp", "眩晕目标时获得");
        EXP_SOURCES.put("vengeful_flame|death_exp", "死亡触发爆炸时获得");
        EXP_SOURCES.put("vengeful_flame|burn_exp", "点燃敌人时获得（每个敌人）");
        EXP_SOURCES.put("vengeful_flame|revenge_exp", "受伤时获得（每 4 点伤害）");
        EXP_SOURCES.put("night_hunter|passive_exp", "夜间滞留时获得（每分钟结算）");
        EXP_SOURCES.put("night_hunter|kill_exp", "夜间击杀时获得");
        EXP_SOURCES.put("dawn_awakening|dawn_exp", "每日黎明结算时获得");
        EXP_SOURCES.put("dawn_awakening|cleared_exp", "黎明清除负面效果时获得");
        EXP_SOURCES.put("dawn_awakening|passive_exp", "白天滞留时获得（每分钟结算）");
        EXP_SOURCES.put("iron_heart|debuff_exp", "负面效果被缩短时获得");
        EXP_SOURCES.put("counter_storm|counter_exp", "发动反击时获得");
        EXP_SOURCES.put("counter_storm|window_exp", "受击后开启反击窗口时获得");
        EXP_SOURCES.put("locksmith_intuition|open_exp", "开锁时获得");
        EXP_SOURCES.put("locksmith_intuition|perfect_exp", "完美开锁时获得");
        EXP_SOURCES.put("hunter_instinct|mark_exp", "标记实体时获得");
        EXP_SOURCES.put("hunter_instinct|mark_death_exp", "被标记目标死亡时获得");
    }

    private ChipParamDescriptions() {
    }

    public static String describe(String parameterId) {
        if (parameterId.endsWith("_exp")) {
            return "芯片经验：触发对应行为时获得（JS 公式，变量 lvl = 芯片等级）。";
        }
        return DESCRIPTIONS.getOrDefault(parameterId,
                "芯片参数 " + parameterId + "（通过 JS 公式配置，变量 lvl = 芯片等级）。");
    }

    /** 带芯片上下文：经验项的注释写成「干什么获得的经验」。 */
    public static String describe(String chipId, String parameterId) {
        if (parameterId.endsWith("_exp")) {
            String source = EXP_SOURCES.getOrDefault(chipId + "|" + parameterId,
                    "触发「" + parameterId + "」对应行为时获得");
            return "芯片经验：" + source + "（JS 公式，变量 lvl = 芯片等级）。";
        }
        return describe(parameterId);
    }

    /** 经验项在配置界面里的标题：直接写「干什么获得的经验」。 */
    public static String expLabel(String chipId, String parameterId) {
        return EXP_SOURCES.getOrDefault(chipId + "|" + parameterId, "芯片经验（" + parameterId + "）");
    }
}
