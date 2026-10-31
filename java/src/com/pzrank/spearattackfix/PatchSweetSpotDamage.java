package com.pzrank.spearattackfix;

import me.zed_0xff.zombie_buddy.Patch;
import zombie.AttackType;
import zombie.characters.IsoGameCharacter;
import zombie.characters.IsoPlayer;
import zombie.characters.IsoZombie;
import zombie.inventory.types.HandWeapon;
import zombie.iso.IsoUtils;
import zombie.scripting.objects.WeaponCategory;

/**
 * v2.5.0, ponto ideal da lanca (parte 2 de 2: dano). A fatiada da lanca que
 * acerta um zumbi a MENOS de 1 tile causa 40% menos dano - a lanca rende na
 * ponta, nao colada no corpo. Na ponta (1 tile ou mais) o dano e o normal.
 *
 * Ponto de entrada: IsoZombie.Hit(weapon, wielder, damageSplit, ...), chamado
 * para cada zumbi atingido pelo golpe. damageSplit e o dano base ja dividido
 * entre os alvos; IsoGameCharacter.processHitDamage() multiplica ele depois
 * (critico, nivel da arma, por tras...), entao reduzir aqui reduz o total.
 *
 * Nao mexe na estocada (atraves de cerca/janela), na investida correndo, no
 * ataque no chao nem no empurrao.
 */
@Patch(className = "zombie.characters.IsoZombie", methodName = "Hit")
public class PatchSweetSpotDamage {

    // Mesma distancia da PatchSweetSpotCritical. Constantes primitivas sao
    // copiadas pelo javac para o codigo, entao funcionam dentro do advice.
    private static final float SWEET_SPOT_MIN_DIST = 1.0f;
    private static final float CLOSE_DAMAGE_MULT = 0.6f;

    @Patch.OnEnter
    public static void enter(@Patch.This IsoZombie zombie,
                             @Patch.Argument(0) HandWeapon weapon,
                             @Patch.Argument(1) IsoGameCharacter wielder,
                             @Patch.Argument(value = 2, readOnly = false) float damageSplit,
                             @Patch.Argument(3) boolean bIgnoreDamage) {
        if (bIgnoreDamage || weapon == null || !(wielder instanceof IsoPlayer)) return;
        if (!weapon.isOfWeaponCategory(WeaponCategory.SPEAR)) return;
        IsoPlayer player = (IsoPlayer) wielder;
        if (player.getAttackType() != AttackType.DEFAULT || player.isAimAtFloor() || player.isDoShove()) return;
        float dist = IsoUtils.DistanceTo(zombie.getX(), zombie.getY(), player.getX(), player.getY());
        if (dist < SWEET_SPOT_MIN_DIST) damageSplit *= CLOSE_DAMAGE_MULT;
    }
}
