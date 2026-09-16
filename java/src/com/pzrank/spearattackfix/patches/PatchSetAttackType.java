package com.pzrank.spearattackfix.patches;

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
 * todo o jogo (confirmado via decompilacao) - bloquear especificamente
 * esse valor aqui e seguro, sem afetar nenhum outro uso legitimo.
 */
@Patch(className = "zombie.characters.IsoPlayer", methodName = "setAttackType")
public class PatchSetAttackType {

    @Patch.OnEnter(skipOn = true)
    public static boolean enter(@Patch.Argument(0) AttackType attackType) {
        // Retorna true (skip) apenas para OVERHEAD - todos os outros
        // AttackType (default, spearstab aleatorio, charge, etc.)
        // continuam funcionando exatamente como no vanilla.
        return attackType == AttackType.OVERHEAD;
    }
}
