package com.pzrank.spearattackfix;

import me.zed_0xff.zombie_buddy.Patch;
import zombie.AttackType;
import zombie.characters.IsoPlayer;
import zombie.inventory.types.HandWeapon;
import zombie.iso.IsoMovingObject;
import zombie.scripting.objects.WeaponCategory;

/**
 * v2.5.0: com lanca, o golpe por cima (OVERHEAD) nunca sai. O ataque e:
 *  - ESTOCADA (SPEAR_STAB) quando o alvo esta atras de cerca/janela
 *    (isTransparentWallTo) - a mecanica propria da lanca no jogo;
 *  - FATIADA (AttackType.DEFAULT, Bob_AttackSpear02_Hit) em todo o resto,
 *    inclusive contra zumbi a distancia, onde antes saia o golpe por cima.
 *
 * Por que funciona sem quebrar o acerto (diferente da v1.0.0): aqui o TIPO do
 * ataque muda, entao o jogo toca a animacao certa com a deteccao de acerto
 * dela. A v1.0.0 so trocava a animacao e o jogo continuava esperando o ataque
 * antigo - o som tocava mas o dano nunca saia.
 *
 * De onde vem cada valor (decompilado, B42.21, CombatManager.pressedAttack()):
 *  - SPEAR_STAB: setado quando o alvo mais proximo esta atras de parede
 *    transparente. Logo depois, o mesmo metodo setava OVERHEAD se o alvo
 *    estivesse a mais de 1.25 tiles e "isolado" (sem outro personagem a 1.7
 *    tiles), SOBRESCREVENDO a estocada - era o bug original (golpe lento sem
 *    dano perto de cercas altas), desde o B42.18.
 *  - SPEAR_STAB tambem e setado como efeito colateral por
 *    HandWeapon.canAttackPierceTransparentWall() (varredura de janelas
 *    barricadas). Por isso a regra abaixo olha o alvo real: sem zumbi atras de
 *    cerca/janela, vira fatiada - nada de estocada "fantasma" em campo aberto.
 *
 * Fica de fora: investida correndo (CHARGE) e ataque em zumbi caido
 * (SpearOnFloor/SpearStabOnFloor), que sao mecanismos separados.
 *
 * Historico: v2.0-v2.2 bloqueavam o OVERHEAD; v2.3.0 trocava OVERHEAD pela
 * estocada em qualquer distancia; v2.4.0 trocava OVERHEAD e estocada pela
 * fatiada sempre (a lanca virou "arma qualquer"). v2.5.0 devolve a estocada
 * so onde ela e da lanca: atraves de cerca e janela.
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
    //
    // O corpo do advice e copiado para dentro do metodo do jogo: so pode
    // usar classes do jogo, nunca outras classes deste mod.
    @Patch.OnEnter
    public static void enter(@Patch.This IsoPlayer player,
                             @Patch.Argument(value = 0, readOnly = false) AttackType attackType) {
        if (attackType != AttackType.OVERHEAD && attackType != AttackType.SPEAR_STAB) return;
        // So lanca: OVERHEAD tambem existe para armas de 1 mao, 2 maos e pesadas.
        HandWeapon weapon = player != null ? player.getUseHandWeapon() : null;
        if (weapon == null || !weapon.isOfWeaponCategory(WeaponCategory.SPEAR)) return;
        IsoMovingObject target = player.getHitInfoList().isEmpty()
            ? null : player.getHitInfoList().get(0).getObject();
        attackType = (target != null && player.isTransparentWallTo(target))
            ? AttackType.SPEAR_STAB : AttackType.DEFAULT;
    }
}
