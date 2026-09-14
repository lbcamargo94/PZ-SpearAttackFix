# Spear Attack Fix

Mod para **Project Zomboid Build 42+** que corrige um bug de animação no combate com lanças: em determinados locais do mapa (perto de cercas altas de metal, por exemplo), o jogo troca automaticamente o ataque normal por uma "estocada" bem mais lenta, sem nenhuma ação do jogador.

---

## O bug

O combate de lança do B42 escolhe entre 3 animações de ataque dependendo da situação: **Default** (golpe normal, rápido), **Stab** (estocada mais lenta) e **Overhead** (golpe por cima). Em certos pontos do mapa o jogo ativa a variante Stab por engano.

## O que o mod faz

Unifica as três variantes (Default, Stab e Overhead) para sempre reproduzir a mesma animação rápida do ataque padrão, independente da situação. Como o timing de acerto (`AttackCollisionCheck`) já era herdado do Default via `x_extends`, o resultado não é só visual — o acerto fica tecnicamente mais preciso também.

O mod só troca qual animação é reproduzida. Não altera dano, alcance, chance de crítico ou qualquer outro valor de balanceamento — nenhum arquivo de item foi modificado.

### O que não é afetado (de propósito)

- **Ataque em zumbi caído no chão** (`SpearOnFloor`) — mecanismo separado, sem relação com o bug relatado
- **Ataque de investida durante corrida** (`SpearCharge`) — definição independente, com timing de colisão próprio; alterá-la sem recalibrar o timing arriscaria descolar o acerto da animação

## Compatibilidade

| Build | Suporte |
|-------|---------|
| 42.20.x | ✔ Compatível (Single Player e Multiplayer) |

Apenas substitui 2 arquivos de animação (`AnimSets`) — seguro para adicionar ou remover de uma run em andamento, não afeta saves existentes.

## IDs

- **Workshop ID:** `3801423705`
- **Mod ID:** `SpearAttackFix`

## Oficina Steam

https://steamcommunity.com/sharedfiles/filedetails/?id=3801423705
