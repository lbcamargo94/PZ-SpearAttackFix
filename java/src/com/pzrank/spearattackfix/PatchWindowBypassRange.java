package com.pzrank.spearattackfix;

import me.zed_0xff.zombie_buddy.Patch;
import zombie.characters.IsoGameCharacter;
import zombie.iso.objects.IsoWindow;

/**
 * Causa raiz da estocada (AttackType.SPEAR_STAB) "fantasma" no ar: o jogo
 * varre TODAS as janelas da celula carregada (CombatManager.CalcHitListWindow,
 * chamado a cada frame via highlightMeleeTargets - mesmo sem o jogador estar
 * atacando de verdade) e, pra cada janela barricada, chama
 * IsoWindow.canAttackBypassIsoBarricade() -> IsoBarricade.canAttackBypassIsoBarricade()
 * -> HandWeapon.canAttackPierceTransparentWall(), que seta SPEAR_STAB como
 * efeito colateral - SEM NENHUMA checagem de distancia antes disso. Testado
 * ao vivo: uma janela barricada a 62 tiles de distancia disparava a estocada
 * no personagem, mesmo sem qualquer chance real de interacao.
 *
 * Esta classe intercepta IsoWindow.canAttackBypassIsoBarricade() (que tem
 * acesso tanto a posicao da janela - "this" - quanto do personagem - arg 0)
 * e pula a avaliacao inteira quando a janela esta fora de qualquer alcance
 * realista de arma corpo a corpo (o metodo original nem chega a rodar, entao
 * o efeito colateral perigoso em HandWeapon.canAttackPierceTransparentWall()
 * nunca acontece pra essas janelas distantes). O retorno default ao pular
 * (false = "nao pode ultrapassar") e inofensivo aqui: mais adiante em
 * CalcHitListWindow a janela e excluida do calculo de acerto de qualquer
 * jeito por estar fora do alcance real da arma (checagem de intersecao de
 * linha) - so a chamada perigosa que acontecia ANTES dessa checagem de
 * alcance e que precisava ser evitada. Perto o suficiente pra importar de
 * verdade (10 tiles), a chamada original roda normal - nao afeta o caso
 * legitimo de atacar atraves de janela/cerca de perto.
 *
 * Nota: tentar sobrescrever o valor de retorno via @Patch.Return(readOnly =
 * false) junto com skipOn nao funcionou (o advice simplesmente nunca era
 * chamado, sem erro nenhum no log) - confirmado via teste ao vivo. Usar so
 * skipOn, aceitando o retorno default, e o que realmente funciona.
 */
@Patch(className = "zombie.iso.objects.IsoWindow", methodName = "canAttackBypassIsoBarricade")
public class PatchWindowBypassRange {

    // 10 tiles - generoso o suficiente pra nao arriscar afetar nenhuma
    // interacao real (alcance de lanca/arma corpo a corpo e ~1.5-2 tiles),
    // mas bem abaixo dos 62 tiles confirmados no caso "fantasma" real.
    private static final float MAX_RELEVANT_DIST_SQ = 100.0f;

    @Patch.OnEnter(skipOn = true)
    public static boolean skipIfFar(@Patch.This IsoWindow window,
                                     @Patch.Argument(0) IsoGameCharacter chr) {
        if (chr == null) {
            return false;
        }
        float dx = window.getX() - chr.getX();
        float dy = window.getY() - chr.getY();
        return dx * dx + dy * dy > MAX_RELEVANT_DIST_SQ;
    }
}
