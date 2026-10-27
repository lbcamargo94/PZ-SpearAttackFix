package com.pzrank.spearattackfix;

import me.zed_0xff.zombie_buddy.Patch;
import zombie.AttackType;
import zombie.characters.IsoPlayer;
import zombie.inventory.types.HandWeapon;
import zombie.scripting.objects.WeaponCategory;

/**
 * v2.4.0: a lanca SEMPRE usa o ataque padrao (a "fatiada", AttackType.DEFAULT,
 * animacao Bob_AttackSpear02_Hit) - o golpe por cima (OVERHEAD) e a estocada
 * (SPEAR_STAB) sao trocados por ele em qualquer situacao: perto de cercas e
 * janelas, contra zumbi a distancia e em campo aberto. Dano e critico passam
 * a ser os da fatiada (SpearDefault / SpearDefaultCrit).
 *
 * Por que funciona sem quebrar o acerto (diferente da v1.0.0): aqui o TIPO do
 * ataque muda, entao o jogo toca a animacao da fatiada com a deteccao de acerto
 * dela. A v1.0.0 so trocava a animacao e o jogo continuava esperando o ataque
 * antigo - o som tocava mas o dano nunca saia.
 *
 * De onde vem cada valor (decompilado, B42.21):
 *  - OVERHEAD: CombatManager.pressedAttack(), so com arma do tipo SPEAR, quando
 *    o alvo esta a mais de 1.25 tiles e "isolado" (sem outro personagem a 1.7
 *    tiles). Perto de cercas altas esse calculo dispara errado - era o bug
 *    original (golpe lento sem dano), desde o B42.18.
 *  - SPEAR_STAB: CombatManager (alvo atras de parede transparente) e
 *    HandWeapon.canAttackPierceTransparentWall(). A PERMISSAO de atravessar a
 *    cerca/janela nao depende do tipo de ataque - continua valendo com a fatiada.
 *
 * Fica de fora: investida correndo (CHARGE) e ataque em zumbi caido
 * (SpearOnFloor/SpearStabOnFloor), que sao mecanismos separados.
 *
 * Historico: v2.0-v2.2 bloqueavam o OVERHEAD; v2.3.0 trocava OVERHEAD pela
 * estocada. Testado ao vivo e revertido na v2.3: usar estocada so com obstaculo
 * real fazia ela sumir do combate a distancia.
 */
@Patch(className = "zombie.characters.IsoPlayer", methodName = "setAttackType")
public class PatchSetAttackType {

    // IMPORTANTE: esta classe precisa estar EXATAMENTE no pacote declarado em
    // javaPkgName no mod.info (com.pzrank.spearattackfix) -
    // PatchEngine.applyPatches() do ZombieBuddy exige igualdade exata de
    // pacote (nao aceita subpacote) pra reconhecer uma classe como patch. Um
    // "com.pzrank.spearattackfix.patches" (com subpacote a mais) fazia o
    // scanner ignorar essa classe silenciosamente ("no patches in
    // package..." no log) - o patch nunca foi aplicado antes disso ser
    // corrigido (confirmado via log que ficou assim desde a v2.0.0).
    @Patch.OnEnter
    public static void enter(@Patch.This IsoPlayer player,
                             @Patch.Argument(value = 0, readOnly = false) AttackType attackType) {
        if (attackType != AttackType.OVERHEAD && attackType != AttackType.SPEAR_STAB) return;
        // So lanca: OVERHEAD tambem existe para armas de 1 mao, 2 maos e pesadas.
        HandWeapon weapon = player != null ? player.getUseHandWeapon() : null;
        if (weapon == null || !weapon.isOfWeaponCategory(WeaponCategory.SPEAR)) return;
        attackType = AttackType.DEFAULT;
    }
}
