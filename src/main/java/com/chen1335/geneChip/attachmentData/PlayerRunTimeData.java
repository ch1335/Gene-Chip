package com.chen1335.geneChip.attachmentData;

import com.chen1335.geneChip.API.GeneChipAPI;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;

public class PlayerRunTimeData {
    private final ArrayList<Long> comboFeversTime = new ArrayList<>();

    public boolean thickSkinnedActive = false;

    public int photosynthesisStacks = 0;

    public int photosynthesisTimer = 0;

    public int oldImmunity = -1;

    public boolean slidingTackleActive = false;

    public int slidingTackleTimer = 0;

    public boolean canDoubleJump = true;

    public boolean isOnGround = true;

    public void recordKill(long time) {
        if (comboFeversTime.size() >= 3) {
            comboFeversTime.removeFirst();
        }
        comboFeversTime.add(time);
    }

    public boolean isComboFever(int timeRequire) {
        if (comboFeversTime.size() < 3) {
            return false;
        }
        return (comboFeversTime.getLast() - comboFeversTime.getFirst()) < timeRequire;
    }

    public void tick(Player entity) {
        int immunityValue = GeneChipAPI.getImmunityValue(entity);
        if (immunityValue != oldImmunity) {
            GeneChipAPI.onImmunityValueChanged(entity);
            oldImmunity = immunityValue;
        }

        if (slidingTackleActive) {
            slidingTackleTimer--;
            if (slidingTackleTimer<=0) {
                slidingTackleActive = false;
            }
        }
        
        // 更新地面状态
        isOnGround = entity.onGround();
        if (isOnGround) {
            canDoubleJump = true;
        }
    }

}
