# Spear Attack Fix

Mod para **Project Zomboid Build 42+** que corrige, na origem, dois bugs de combate com lanças:

1. Perto de certos obstáculos (cercas altas de metal, por exemplo), o jogo força um ataque "golpe por cima" bem mais lento, e o acerto frequentemente não registra — o som toca, mas o zumbi não recebe dano.
2. A animação de estocada pode disparar "no ar", sem zumbi nem obstáculo visível por perto — causada por uma janela barricada em qualquer lugar do mapa carregado, mesmo a dezenas de tiles de distância.

Bug 1 relatado pela comunidade desde o Build 42.18, sem correção oficial da Indie Stone até o momento ([thread no fórum oficial](https://theindiestone.com/forums/topic/95691-bug-found-in-project-zomboid-build-4218-spear-attack-issue-in-a-specific-location/)).

---

## A causa raiz

### Bug 1 — golpe por cima forçado

Decompilando o jogo, encontramos a origem exata em `zombie.CombatManager.pressedAttack(IsoPlayer)`:

```java
if (!isoPlayer.getAttackVars().aimAtFloor && closestDist > 1.25f && weaponType == WeaponType.SPEAR
    && (closestToTarget == null || IsoUtils.DistanceTo(...) > 1.7f)) {
    isoPlayer.setAttackType(AttackType.OVERHEAD);
    ...
}
```

Essa é uma mecânica intencional do jogo (golpe por cima quando o alvo está "distante e isolado"), mas perto de obstáculos como cercas altas, o cálculo de distância/isolamento dispara incorretamente mesmo com o zumbi bem próximo, do outro lado do obstáculo — resultando no ataque lento e na falha de acerto.

### Bug 2 — estocada "fantasma" perto de janelas barricadas distantes

`CombatManager.highlightMeleeTargets()` roda a cada frame (mesmo sem o jogador atacar de verdade, só pra destacar alvos na tela) e chama `CalcHitListWindow()`, que varre **todas as janelas do mapa atualmente carregado** — sem nenhuma checagem de distância. Para cada janela barricada, o jogo pergunta se a lança consegue ultrapassar a barricada (`HandWeapon.canAttackPierceTransparentWall()`), e essa checagem já seta `AttackType.SPEAR_STAB` como efeito colateral, mesmo que a janela esteja a dezenas de tiles de distância, sem qualquer chance real de interação. Confirmado ao vivo: uma janela barricada a **62 tiles** de distância disparava a estocada continuamente.

## O que o mod faz

Usa **patches de bytecode Java** (via [ZombieBuddy](https://steamcommunity.com/sharedfiles/filedetails/?id=3619862853)):

- Intercepta `IsoPlayer.setAttackType()` e **substitui** o valor `AttackType.OVERHEAD` por `AttackType.SPEAR_STAB` — confirmado ser o **único lugar em todo o jogo** que define OVERHEAD, então essa troca não afeta nenhum outro uso legítimo. Como a condição que dispara essa troca (`alvo a mais de 1.25 tiles E isolado`) é praticamente sempre verdadeira quando há um obstáculo separando o jogador do zumbi, a estocada passa a ser o ataque usado de forma consistente tanto atravessando obstáculos quanto a distância — em vez do golpe padrão "vazar" através de cercas/janelas com a animação errada.
- Intercepta `IsoWindow.canAttackBypassIsoBarricade()` e pula a avaliação (sem chegar a chamar o efeito colateral perigoso) quando a janela está fora de qualquer alcance realista de arma corpo a corpo (10 tiles). Perto o suficiente pra importar de verdade, a checagem roda normal — não afeta o caso legítimo de atacar através de janela/cerca de perto.

Nenhum dano, alcance ou detecção de acerto é modificado — o mod corrige o tipo de ataque usado nos gatilhos bugados, em vez de tentar consertar o sintoma depois.

> **Nota técnica (histórico):** as versões v1.0.0 e v1.1.0 tentavam corrigir o bug 1 via arquivos de animação (`AnimSets`), trocando qual clipe tocava ou sua velocidade. Testes reais mostraram que isso não resolvia a detecção de acerto — a causa real não estava na animação, e sim no valor `AttackType` sendo forçado incorretamente pelo motor do jogo. A v2.0.0 corrige na origem.
>
> **Nota técnica (v2.1.0):** a v2.0.0 declarava a classe do patch Java num pacote (`com.pzrank.spearattackfix.patches`) diferente do `javaPkgName` do `mod.info` (`com.pzrank.spearattackfix`). O ZombieBuddy exige igualdade exata de pacote para reconhecer uma classe como patch — por isso o bloqueio nunca chegou a ser aplicado de verdade, mesmo com o mod "carregando" sem erro nenhum. A v2.1.0 corrige movendo a classe pro pacote certo.
>
> **Nota técnica (v2.2.0):** identificado e corrigido o bug 2 (estocada fantasma), descrito acima. Confirmado ao vivo via logging de diagnóstico: antes do fix, a estocada disparava continuamente por uma janela barricada a 62 tiles; depois do fix, zero disparos, sem afetar o combate normal.
>
> **Nota técnica (v2.3.0):** a v2.2.0 apenas bloqueava OVERHEAD (deixando `attackType` como estivesse antes - normalmente o golpe padrão, do sorteio inicial do jogo), o que ainda deixava o golpe padrão "vazando" através de cercas/janelas com a animação errada, e a estocada não saindo de forma consistente a distância. A v2.3.0 substitui OVERHEAD por SPEAR_STAB em vez de só bloquear, resolvendo os dois casos de uma vez. Testado ao vivo: estocada consistente tanto atravessando cerca com zumbi do outro lado (acertando e causando dano) quanto contra zumbi distante em campo aberto.

### O que não é afetado (de propósito)

- **Ataque em zumbi caído no chão** (`SpearOnFloor`) — mecanismo separado, sem relação com os bugs
- **Ataque de investida durante corrida** (`SpearCharge`) — independente, sem relação
- **Ataque corpo a corpo normal** (alvo próximo e não isolado) — continua com a variação padrão do jogo entre golpe normal e estocada

## Requisitos

- **[ZombieBuddy](https://steamcommunity.com/sharedfiles/filedetails/?id=3619862853)** — obrigatório. É o framework que permite o patch Java. Instale e ative antes deste mod.

## Compatibilidade

| Build | Suporte |
|-------|---------|
| 42.20.x | ✔ Compatível (Single Player e Multiplayer) |

## IDs

- **Workshop ID:** `3801423705`
- **Mod ID:** `SpearAttackFix`

## Oficina Steam

https://steamcommunity.com/sharedfiles/filedetails/?id=3801423705
