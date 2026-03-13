/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.battles.bonuses.model;

import gtanks.battles.BattlefieldModel;
import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.bonuses.Bonus;
import gtanks.battles.effects.Effect;
import gtanks.battles.effects.EffectType;
import gtanks.battles.effects.impl.ArmorEffect;
import gtanks.battles.effects.impl.DamageEffect;
import gtanks.battles.effects.impl.HealthEffect;
import gtanks.battles.effects.impl.NitroEffect;
import gtanks.commands.Type;
import gtanks.main.database.DatabaseManager;
import gtanks.main.database.impl.DatabaseManagerImpl;
import gtanks.services.annotations.ServicesInject;
import gtanks.users.garage.GarageItemsLoader;
import gtanks.users.garage.items.Item;

public class BonusTakeModel {
    private BattlefieldModel bfModel;
    @ServicesInject(target=DatabaseManagerImpl.class)
    private DatabaseManager database = DatabaseManagerImpl.instance();

    public BonusTakeModel(BattlefieldModel bfModel) {
        this.bfModel = bfModel;
    }

    public boolean onTakeBonus(Bonus bonus, BattlefieldPlayerController player) {
        switch (bonus.type) {
            case CRYSTALL: {
                player.parentLobby.getLocalUser().addCrystall(1);
                player.send(Type.BATTLE, "set_cry", String.valueOf(player.parentLobby.getLocalUser().getCrystall()));
                this.database.update(player.getUser());
                break;
            }
            case GOLD: {
                this.bfModel.sendUserLogMessage(player.parentLobby.getLocalUser().getNickname(), "\u0432\u0437\u044f\u043b \u0437\u043e\u043b\u043e\u0442\u043e\u0439 \u044f\u0449\u0438\u043a");
                player.parentLobby.getLocalUser().addCrystall(100);
                player.send(Type.BATTLE, "set_cry", String.valueOf(player.parentLobby.getLocalUser().getCrystall()));
                this.database.update(player.getUser());
                break;
            }
            case GIFT: {
                Item bonusItem = player.getGarage().getItemById("gift");
                if (bonusItem == null) {
                    bonusItem = GarageItemsLoader.items.get("gift").clone();
                    player.getGarage().items.add(bonusItem);
                }
                ++bonusItem.count;
                player.parentLobby.getLocalUser().getGarage().parseJSONData();
                this.database.update(player.parentLobby.getLocalUser().getGarage());
                break;
            }
            case ARMOR: {
                this.activateDrop(new ArmorEffect(), player);
                break;
            }
            case DAMAGE: {
                this.activateDrop(new DamageEffect(), player);
                break;
            }
            case HEALTH: {
                this.activateDrop(new HealthEffect(), player);
                break;
            }
            case NITRO: {
                this.activateDrop(new NitroEffect(), player);
            }
        }
        return true;
    }

    private void activateDrop(Effect effect, BattlefieldPlayerController player) {
        if (!player.tank.isUsedEffect(effect.getEffectType())) {
            effect.activate(player, false, player.tank.position);
            player.battle.sendToAllPlayers(Type.BATTLE, "enable_effect", player.getUser().getNickname(), String.valueOf(effect.getID()), effect.getEffectType() == EffectType.HEALTH ? String.valueOf(10000) : String.valueOf(40000));
        }
    }
}

