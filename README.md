# Spear Attack Fix

Mod para **Project Zomboid Build 42+** que corrige um bug de animação no combate com lanças: em determinados locais do mapa (perto de cercas altas de metal, por exemplo), o jogo troca automaticamente o ataque normal por uma "estocada" bem mais lenta, sem nenhuma ação do jogador.

---

## O bug

O combate de lança do B42 escolhe entre 3 animações de ataque dependendo da situação: **Default** (golpe normal, rápido), **Stab** (estocada mais lenta) e **Overhead** (golpe por cima). Em certos pontos do mapa o jogo ativa a variante Stab por engano.

## O que o mod faz

Acelera as animações **Stab** e **Overhead** (via `m_SpeedScale`) para o mesmo ritmo do ataque **Default**, sem trocar qual clipe de animação é reproduzido. Nenhum outro dado é alterado — dano, alcance, chance de crítico, e a detecção de acerto (`AttackCollisionCheck`) continuam exatamente como no vanilla, já que a animação em si não muda, só a velocidade de reprodução.

> **Nota técnica (v1.0.0 → v1.1.0):** a v1.0.0 tentava trocar qual animação tocava (reaproveitando o clipe do Default nos três casos), mas isso quebrava a detecção de acerto — o ataque tocava o som mas não registrava dano. A v1.1.0 usa uma abordagem mais segura: mantém as animações originais (e o comportamento de acerto original, comprovadamente funcional) e só ajusta a velocidade.

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
