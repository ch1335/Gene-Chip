package com.chen1335.geneChip.API.object;

import com.chen1335.geneChip.GeneChip;
import com.chen1335.geneChip.attachmentData.PlayerChipData;
import com.chen1335.geneChip.attachmentData.PlayerRunTimeData;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class GCAttachmentTypes {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, GeneChip.MODID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PlayerChipData>> PLAYER_CHIP_DATA = ATTACHMENT_TYPES.register("player_chip_data", () -> AttachmentType.serializable(o -> new PlayerChipData()).build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PlayerRunTimeData>> PLAYER_RUN_TIME_DATA = ATTACHMENT_TYPES.register("player_runt_time_data", () -> AttachmentType.builder(o -> new PlayerRunTimeData()).build());

}
