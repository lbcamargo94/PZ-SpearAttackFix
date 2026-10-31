package com.pzrank.spearattackfix;

import me.zed_0xff.zombie_buddy.Patch;
import zombie.AttackType;
import zombie.characters.IsoPlayer;
import zombie.characters.IsoZombie;
import zombie.inventory.types.HandWeapon;
import zombie.iso.IsoMovingObject;
import zombie.iso.IsoUtils;
import zombie.network.GameClient;
import zombie.scripting.objects.WeaponCategory;

/**
 * v2.5.0, ponto ideal da lanca (parte 1 de 2: critico). A fatiada da lanca so
 * pode dar critico quando o alvo esta na PONTA, a 1 tile ou mais. Colado no
 * personagem, o critico sorteado e cancelado - quem joga de lanca precisa
 * manter a distancia, como antes da v2.4.0.
 *
 * O critico corpo a corpo e sorteado em CombatManager.pressedAttack()
 * (Rand.Next(100) < calculateCritChance(alvo) + bonus), no inicio do golpe e
 * antes da animacao ser escolhida - por isso o patch roda na SAIDA desse metodo:
 * cancelado ali, o jogo toca a fatiada normal (SpearDefault) e nao a de morte
 * (SpearDefaultCrit). O jogo tambem acelera o golpe critico em 10%
 * (setCombatSpeed * 1.1); isso e desfeito junto.
 *
 * Continua valendo: abate furtivo por tras de zumbi que nao te viu (o jogo
 * forca o critico nesse caso), estocada atraves de cerca/janela, investida
 * correndo e ataque no chao.
 */
@Patch(className = "zombie.CombatManager", methodName = "pressedAttack")
public class PatchSweetSpotCritical {

    // Mesma medida que pressedAttack() usa para o "TargetDist" (centro a
    // centro). As lancas alcancam 1.40-1.55 tiles: 1.0 deixa o ultimo terco
    // do alcance como ponto ideal.
    private static final float SWEET_SPOT_MIN_DIST = 1.0f;

    @Patch.OnExit
    public static void exit(@Patch.Argument(0) IsoPlayer player) {
        if (player == null || !player.isCriticalHit()) return;
        if (GameClient.client && !player.isLocalPlayer()) return;
        if (player.getAttackType() != AttackType.DEFAULT || player.isAimAtFloor()) return;
        HandWeapon weapon = player.getUseHandWeapon();
        if (weapon == null || !weapon.isOfWeaponCategory(WeaponCategory.SPEAR)) return;
        if (player.getHitInfoList().isEmpty()) return;
        IsoMovingObject target = player.getHitInfoList().get(0).getObject();
        if (target == null) return;
        if (player.isAttackFromBehind() && player.getAttackVars().closeKill
            && target instanceof IsoZombie && ((IsoZombie) target).target == null) return;
        float dist = IsoUtils.DistanceTo(target.getX(), target.getY(), player.getX(), player.getY());
        if (dist >= SWEET_SPOT_MIN_DIST) return;
        player.setCriticalHit(false);
        player.setCombatSpeed(player.getCombatSpeed() / 1.1f);
    }
}
