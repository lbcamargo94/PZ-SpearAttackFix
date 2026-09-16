# Spear Attack Fix

Mod para **Project Zomboid Build 42+** que corrige, na origem, um bug de combate com lanças: perto de certos obstáculos (cercas altas de metal, por exemplo), o jogo força um ataque "golpe por cima" bem mais lento, e o acerto frequentemente não registra — o som toca, mas o zumbi não recebe dano.

Bug relatado pela comunidade desde o Build 42.18, sem correção oficial da Indie Stone até o momento ([thread no fórum oficial](https://theindiestone.com/forums/topic/95691-bug-found-in-project-zomboid-build-4218-spear-attack-issue-in-a-specific-location/)).

---

## A causa raiz

Decompilando o jogo, encontramos a origem exata em `zombie.CombatManager.pressedAttack(IsoPlayer)`:

```java
if (!isoPlayer.getAttackVars().aimAtFloor && closestDist > 1.25f && weaponType == WeaponType.SPEAR
    && (closestToTarget == null || IsoUtils.DistanceTo(...) > 1.7f)) {
    isoPlayer.setAttackType(AttackType.OVERHEAD);
    ...
}
```

Essa é uma mecânica intencional do jogo (golpe por cima quando o alvo está "distante e isolado"), mas perto de obstáculos como cercas altas, o cálculo de distância/isolamento dispara incorretamente mesmo com o zumbi bem próximo, do outro lado do obstáculo — resultando no ataque lento e na falha de acerto.

## O que o mod faz

Usa um **patch de bytecode Java** (via [ZombieBuddy](https://steamcommunity.com/sharedfiles/filedetails/?id=3619862853)) que intercepta `IsoPlayer.setAttackType()` e bloqueia especificamente o valor `AttackType.OVERHEAD` — confirmado ser o **único lugar em todo o jogo** que define esse valor, então bloqueá-lo não afeta nenhum outro uso legítimo. Nenhuma animação, dano, alcance ou detecção de acerto é modificado — o mod impede que o gatilho bugado aconteça, em vez de tentar consertar o sintoma depois.

> **Nota técnica (histórico):** as versões v1.0.0 e v1.1.0 tentavam corrigir isso via arquivos de animação (`AnimSets`), trocando qual clipe tocava ou sua velocidade. Testes reais mostraram que isso não resolvia a detecção de acerto — a causa real não estava na animação, e sim no valor `AttackType` sendo forçado incorretamente pelo motor do jogo. A v2.0.0 corrige na origem.

### O que não é afetado (de propósito)

- **Ataque em zumbi caído no chão** (`SpearOnFloor`) — mecanismo separado, sem relação com o bug
- **Ataque de investida durante corrida** (`SpearCharge`) — independente, sem relação
- **Variação aleatória Stab vs Default** durante combate normal — comportamento vanilla intencional, não é o bug relatado

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
