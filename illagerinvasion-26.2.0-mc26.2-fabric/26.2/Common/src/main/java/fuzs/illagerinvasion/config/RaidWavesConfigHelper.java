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

    // 파쇄병 - 방패를 휘두르며 플레이어에게 돌진하는 불량배
    public static int[] getBasherRaidWaves() 
    {
        return new int[]{0, 2, 3, 4, 5, 6, 7, 8};
    }

    // 도발자 - 인챈트된 활로 공격
    public static int[] getProvokerRaidWaves() 
    {
        return new int[]{0, 0, 1, 2, 3, 4, 5, 6};
    }

    // 강령술사 - 마법을 이용해, 좀비, 스켈레톤 부활
    public static int[] getNecromancerRaidWaves() 
    {
        return new int[]{0, 1, 2, 3, 4, 4, 5, 5};
    }

    // 주술사 - 마법 보라색 불꽃공격 (땅에서 쏟아나는 화염)
    public static int[] getSorcererRaidWaves() 
    {
        return new int[]{0, 0, 1, 1, 2, 2, 3, 4};
    }

    // 환영술사 - 활공격, 분신술
    public static int[] getIllusionerRaidWaves() 
    {
        return new int[]{0, 0, 0, 1, 1, 2, 2, 3};
    }

    // 기록관 - 주변 아군에 마법효과 부여
    public static int[] getArchivistRaidWaves() 
    {
        return new int[]{0, 1, 1, 2, 2, 3, 3, 4};
    }

    // 약탈자 - 치명적인 도끼를 던지는 원거리 공격형
    public static int[] getMarauderRaidWaves() 
    {
        return new int[]{0, 1, 2, 3, 3, 4, 5, 6};
    }

    // 심문관(대장일리저) - 방패를 파괴해야만 잡을 수 있는 전사
    public static int[] getInquisitorRaidWaves() 
    {
        return new int[]{0, 0, 1, 2, 2, 3, 3, 4};
    }

    // 연금술사 - 활공격, 물약던지기(상태이상폭격)
    public static int[] getAlchemistRaidWaves() 
    {
        return new int[]{0, 0, 0, 1, 2, 3, 4, 5};
    }

    // 화염술사 - 마그마블록을 던짐(화염구)
    public static int[] getFirecallerRaidWaves() 
    {
        return new int[]{0, 1, 1, 1, 2, 2, 3, 4};
    }

    // 찬란한 기원자(보스) - 준보스 (매우쌤)
    public static int[] getInvokerRaidWaves() 
    {
        return new int[]{0, 0, 0, 0, 1, 1, 2, 3};
    }
}
