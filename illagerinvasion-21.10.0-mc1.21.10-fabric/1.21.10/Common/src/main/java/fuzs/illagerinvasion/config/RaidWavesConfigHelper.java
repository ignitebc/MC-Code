package fuzs.illagerinvasion.config;

public class RaidWavesConfigHelper {
    public static final int[] BASHER_RAID_WAVES = getBasherRaidWaves();
    public static final int[] PROVOKER_RAID_WAVES = getProvokerRaidWaves();
    public static final int[] NECROMANCER_RAID_WAVES = getNecromancerRaidWaves();
    public static final int[] SORCERER_RAID_WAVES = getSorcererRaidWaves();
    public static final int[] ILLUSIONER_RAID_WAVES = getIllusionerRaidWaves();
    public static final int[] ARCHIVIST_RAID_WAVES = getArchivistRaidWaves();
    public static final int[] MARAUDER_RAID_WAVES = getMarauderRaidWaves();
    public static final int[] INQUISITOR_RAID_WAVES = getInquisitorRaidWaves();
    public static final int[] ALCHEMIST_RAID_WAVES = getAlchemistRaidWaves();
    public static final int[] INVOKER_RAID_WAVES = getInvokerRaidWaves();
    public static final int[] FIRECALLER_RAID_WAVES = getFirecallerRaidWaves();
    
    // public static int[] getEmptyRaidWaves() {
    //     return new int[]{0, 0, 0, 0, 0, 0, 0, 0};
    // }
    
    // //전선 붕괴 탱커
    // public static int[] getBasherRaidWaves() {
    //     return new int[]{0, 1, 2, 1, 2, 3, 2, 3};
    // }
    // //위치·어그로 교란
    // public static int[] getProvokerRaidWaves() {
    //     return new int[]{0, 1, 1, 0, 2, 1, 3, 2};
    // }
    // //소환 / 장기전 유발
    // public static int[] getNecromancerRaidWaves() {
    //     return new int[]{0, 0, 0, 0, 1, 1, 1, 1};
    // }
    // //범위 마법 딜러
    // public static int[] getSorcererRaidWaves() {
    //     return new int[]{0, 0, 0, 0, 0, 1, 1, 1};
    // }
    // //혼란 / 시야 교란
    // public static int[] getIllusionerRaidWaves() {
    //     return new int[]{0, 0, 0, 1, 0, 1, 0, 1};
    // }
    // //버프·난이도 증폭
    // public static int[] getArchivistRaidWaves() {
    //     return new int[]{0, 1, 0, 1, 2, 1, 2, 3};
    // }
    // //고화력 돌격
    // public static int[] getMarauderRaidWaves() {
    //     return new int[]{0, 1, 1, 1, 2, 2, 3, 3};
    // }
    // //플레이어 제압
    // public static int[] getInquisitorRaidWaves() {
    //     return new int[]{0, 0, 0, 1, 0, 1, 0, 2};
    // }
    // //상태이상 폭격
    // public static int[] getAlchemistRaidWaves() {
    //     return new int[]{0, 0, 0, 1, 2, 1, 2, 2};
    // }
    // //보스급 압박
    // public static int[] getInvokerRaidWaves() {
    //     return new int[]{0, 0, 0, 0, 0, 1, 0, 1};
    // }

    public static int[] getEmptyRaidWaves() 
    {
        return new int[]{0, 0, 0, 0, 0, 0, 0, 0};
    }

    // 전선 붕괴 탱커-파쇄병 (Basher) : 매 웨이브 전선 압박 유지
    public static int[] getBasherRaidWaves() 
    {
        return new int[]{0, 2, 3, 4, 5, 6, 7, 8};
    }

    // 위치·어그로 교란-도발자 (Provoker) : 2웨이브부터 지속, 후반 강화
    public static int[] getProvokerRaidWaves() 
    {
        return new int[]{0, 0, 1, 2, 3, 4, 5, 6};
    }

    // 소환 / 장기전 유발-강령술사 (Necromancer) : 3웨이브부터 등장, 후반 2마리 유지
    public static int[] getNecromancerRaidWaves() 
    {
        return new int[]{0, 0, 0, 1, 2, 3, 3, 3};
    }

    // 범위 마법 딜러-주술사 (Sorcerer) : 4웨이브부터, 후반 2마리로 확실한 압박
    public static int[] getSorcererRaidWaves() 
    {
        return new int[]{0, 0, 0, 0, 1, 2, 3, 4};
    }

    // 혼란 / 시야 교란 -환영술사(Illusioner) : 중후반에 고정 투입
    public static int[] getIllusionerRaidWaves() 
    {
        return new int[]{0, 0, 0, 1, 1, 2, 2, 3};
    }

    // 버프·난이도 증폭-기록관 (Archivist) : 초반부터 한 마리, 후반 2~3 유지
    public static int[] getArchivistRaidWaves() 
    {
        return new int[]{0, 1, 1, 2, 2, 3, 3, 4};
    }

    // 고화력 돌격-약탈자 (Marauder) : 초반부터 딜체크, 후반 4~5로 킬각 생성
    public static int[] getMarauderRaidWaves() 
    {
        return new int[]{0, 1, 2, 3, 3, 4, 5, 6};
    }

    // 플레이어 제압 -심문관(Inquisitor) : 3웨이브부터 꾸준히 투입, 후반 2로 강화
    public static int[] getInquisitorRaidWaves() 
    {
        return new int[]{0, 0, 0, 1, 1, 1, 2, 2};
    }

    // 상태이상 폭격 -연금술사 (Alchemist) : 3~4부터 투입, 후반 3으로 중첩 압박
    public static int[] getAlchemistRaidWaves() 
    {
        return new int[]{0, 0, 0, 1, 2, 2, 3, 3};
    }

     // 범위 화염 압박 -화염술사 (Firecaller) : 4부터 시작, 후반 강화
    public static int[] getFirecallerRaidWaves() 
    {
        return new int[]{0, 1, 1, 1, 2, 2, 3, 4};
    }

    // 보스급 압박 -찬란한 기원자 (Invoker) : 4부터 시작, 7~8에서 2마리로 보스전화
    public static int[] getInvokerRaidWaves() 
    {
        return new int[]{0, 0, 0, 0, 1, 1, 2, 3};
    }
}
