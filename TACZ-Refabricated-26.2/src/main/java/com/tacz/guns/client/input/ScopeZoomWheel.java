package com.tacz.guns.client.input;

import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.api.item.nbt.AttachmentItemDataAccessor;
import com.tacz.guns.client.resource.index.ClientAttachmentIndex;
import com.tacz.guns.network.message.ClientMessagePlayerZoomLevel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

import static com.tacz.guns.util.InputExtraCheck.isInGame;

/**
 * 배율 조준경(부착형·일체형)의 조준 방식과 마우스 휠 배율 조정.
 *
 * <p>레드닷·홀로그래픽처럼 배율이 없는 조준경은 배율 조준경으로 보지 않는다. 배율 단계가 둘 이상인 부착 조준경만
 * 휠로 배율을 바꾸며, 고정 배율 조준경과 일체형 조준경은 휠을 쓰지 않는다.
 */
@Environment(EnvType.CLIENT)
public final class ScopeZoomWheel {
    private ScopeZoomWheel() {
    }

    /** 총에 배율 조준경이 달렸는지. 부착 조준경이 없으면 일체형 조준경을 본다. */
    public static boolean hasMagnifiedScope(ItemStack gun) {
        IGun iGun = IGun.getIGunOrNull(gun);
        if (iGun == null) {
            return false;
        }
        Identifier scopeId = iGun.getAttachmentId(gun, AttachmentType.SCOPE);
        if (DefaultAssets.isEmptyAttachmentId(scopeId)) {
            scopeId = iGun.getBuiltInAttachmentId(gun, AttachmentType.SCOPE);
        }
        if (DefaultAssets.isEmptyAttachmentId(scopeId)) {
            return false;
        }
        return TimelessAPI.getClientAttachmentIndex(scopeId).map(ClientAttachmentIndex::isScope).orElse(false);
    }

    /**
     * 조준 중인 가변 배율 조준경이면 휠 한 칸마다 배율 단계를 하나 옮긴다. 위로 굴리면 확대한다.
     *
     * @return 휠 입력을 배율 조정에 썼으면 true. 이때는 핫바 칸을 바꾸지 않는다.
     */
    public static boolean onScroll(double scrollY) {
        if (!isInGame() || scrollY == 0) {
            return false;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || player.isSpectator()) {
            return false;
        }
        if (!IClientPlayerGunOperator.fromLocalPlayer(player).isAim()) {
            return false;
        }
        ItemStack gun = player.getMainHandItem();
        IGun iGun = IGun.getIGunOrNull(gun);
        if (iGun == null) {
            return false;
        }
        Optional<float[]> levels = variableZoomLevels(iGun, gun);
        if (levels.isEmpty()) {
            return false;
        }

        float[] zooms = levels.get();
        int current = AttachmentItemDataAccessor.getZoomNumberFromTag(iGun.getAttachmentTag(gun, AttachmentType.SCOPE))
                % zooms.length;
        boolean zoomIn = scrollY > 0;
        int target = nextLevel(zooms, current, zoomIn);
        if (target != current) {
            ClientPlayNetworking.send(new ClientMessagePlayerZoomLevel(target));
        }
        return true;
    }

    /** 부착한 배율 조준경의 배율 단계 목록. 단계가 하나뿐이거나 일체형이면 빈 값 */
    private static Optional<float[]> variableZoomLevels(IGun iGun, ItemStack gun) {
        Identifier scopeId = iGun.getAttachmentId(gun, AttachmentType.SCOPE);
        if (DefaultAssets.isEmptyAttachmentId(scopeId)) {
            return Optional.empty();
        }
        return TimelessAPI.getClientAttachmentIndex(scopeId)
                .filter(ClientAttachmentIndex::isScope)
                .map(ClientAttachmentIndex::getZoom)
                .filter(zooms -> zooms.length > 1);
    }

    /**
     * 현재 배율 바로 위(확대) 또는 바로 아래(축소)의 단계. 끝에 닿으면 넘어가지 않고 현재 단계를 돌려준다.
     * 데이터의 단계 순서와 관계없이 배율 값으로 이웃 단계를 고른다.
     */
    private static int nextLevel(float[] zooms, int current, boolean zoomIn) {
        int next = current;
        for (int i = 0; i < zooms.length; i++) {
            boolean onRightSide = zoomIn ? zooms[i] > zooms[current] : zooms[i] < zooms[current];
            if (!onRightSide) {
                continue;
            }
            boolean closer = next == current || (zoomIn ? zooms[i] < zooms[next] : zooms[i] > zooms[next]);
            if (closer) {
                next = i;
            }
        }
        return next;
    }
}
