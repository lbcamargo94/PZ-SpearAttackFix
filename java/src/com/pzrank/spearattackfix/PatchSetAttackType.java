package com.pzrank.spearattackfix;

import me.zed_0xff.zombie_buddy.Patch;
import zombie.AttackType;

/**
 * Causa raiz do bug "ataque de lanca atraves de cerca": em
 * zombie.CombatManager.pressedAttack(IsoPlayer), quando o alvo mais
 * proximo esta a mais de 1.25 tiles de distancia E "isolado" (sem outro
 * personagem a menos de 1.7 tiles), o jogo forca AttackType.OVERHEAD
 * (a animacao lenta "golpe por cima"). Perto de obstaculos como cercas
 * altas, esse calculo de distancia/isolamento e disparado incorretamente
 * mesmo com o zumbi logo ali do outro lado, causando a "estocada" lenta
 * relatada por varios jogadores desde o B42.18 (sem fix oficial ainda).
 *
 * setAttackType(AttackType.OVERHEAD) so e chamado nesse UNICO lugar em
 * todo o jogo (confirmado via decompilacao). Em vez de so bloquear (o que
 * deixava o attackType como estivesse antes - normalmente DEFAULT, do
 * sorteio inicial em pressedAttack), SUBSTITUIMOS o valor por
 * AttackType.SPEAR_STAB antes do metodo original rodar. Isso resolve dois
 * problemas de uma vez:
 *
 * 1) O bug original (golpe lento sem dano) nunca mais acontece - SPEAR_STAB
 *    e uma animacao que sabemos, por teste real, que registra acerto
 *    corretamente perto de obstaculos.
 * 2) Como a condicao que dispara essa troca e exatamente "alvo a mais de
 *    1.25 tiles E isolado" - praticamente sempre verdadeiro quando ha uma
 *    cerca/parede separando o jogador do zumbi (a propria cerca garante
 *    mais de 1 tile de distancia) - a estocada passa a ser o ataque usado
 *    de forma consistente tanto atraves de obstaculos quanto a distancia,
 *    em vez do ataque padrao "vazar" pela cerca com a animacao errada.
 *
 * NOTA (testado ao vivo, revertido): uma versao intermediaria tentou usar
 * SPEAR_STAB so quando havia obstaculo de verdade (lendo se
 * canAttackPierceTransparentWall ja tinha rodado), caindo pra DEFAULT sem
 * obstaculo - a ideia era evitar o angulo meio "pra cima" da animacao de
 * estocada em combate aberto (relatado como visualmente estranho). Porem
 * isso fez a estocada sumir completamente do combate a distancia sem
 * obstaculo, que era exatamente o comportamento pedido originalmente -
 * revertido de volta pra substituicao incondicional.
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
    public static void enter(@Patch.Argument(value = 0, readOnly = false) AttackType attackType) {
        if (attackType == AttackType.OVERHEAD) {
            attackType = AttackType.SPEAR_STAB;
        }
    }
}
