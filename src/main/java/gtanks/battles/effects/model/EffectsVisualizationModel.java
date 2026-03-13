/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.battles.effects.model;

import gtanks.battles.BattlefieldModel;
import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.effects.Effect;
import gtanks.battles.spectator.SpectatorController;
import gtanks.commands.Type;
import java.util.ArrayList;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class EffectsVisualizationModel {
    private BattlefieldModel bfModel;

    public EffectsVisualizationModel(BattlefieldModel bfModel) {
        this.bfModel = bfModel;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void sendInitData(BattlefieldPlayerController player) {
        JSONObject _obj = new JSONObject();
        JSONArray array = new JSONArray();
        for (BattlefieldPlayerController _player : this.bfModel.players.values()) {
            ArrayList<Effect> arrayList;
            if (player == _player) continue;
            ArrayList<Effect> arrayList2 = arrayList = _player.tank.activeEffects;
            synchronized (arrayList2) {
                for (Effect effect : _player.tank.activeEffects) {
                    JSONObject obj = new JSONObject();
                    obj.put("userID", _player.getUser().getNickname());
                    obj.put("itemIndex", effect.getID());
                    obj.put("durationTime", 60000);
                    array.add(obj);
                }
            }
        }
        _obj.put("effects", array);
        player.send(Type.BATTLE, "init_effects", _obj.toJSONString());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void sendInitData(SpectatorController player) {
        JSONObject _obj = new JSONObject();
        JSONArray array = new JSONArray();
        for (BattlefieldPlayerController _player : this.bfModel.players.values()) {
            ArrayList<Effect> arrayList;
            ArrayList<Effect> arrayList2 = arrayList = _player.tank.activeEffects;
            synchronized (arrayList2) {
                for (Effect effect : _player.tank.activeEffects) {
                    JSONObject obj = new JSONObject();
                    obj.put("userID", _player.getUser().getNickname());
                    obj.put("itemIndex", effect.getID());
                    obj.put("durationTime", 60000);
                    array.add(obj);
                }
            }
        }
        _obj.put("effects", array);
        player.sendCommand(Type.BATTLE, "init_effects", _obj.toJSONString());
    }
}

