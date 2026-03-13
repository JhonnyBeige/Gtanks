/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.battles;

import gtanks.battles.BattlefieldModel;
import gtanks.battles.ctf.flags.FlagServer;
import gtanks.battles.inventory.InventoryController;
import gtanks.battles.tanks.Tank;
import gtanks.battles.tanks.colormaps.ColormapsFactory;
import gtanks.battles.tanks.loaders.HullsFactory;
import gtanks.battles.tanks.loaders.WeaponsFactory;
import gtanks.battles.tanks.math.Vector3;
import gtanks.battles.tanks.statistic.PlayerStatistic;
import gtanks.commands.Command;
import gtanks.json.JSONUtils;
import gtanks.lobby.LobbyManager;
import gtanks.logger.Logger;
import gtanks.logger.Type;
import gtanks.main.params.OnlineStats;
import gtanks.network.listeners.IDisconnectListener;
import gtanks.services.AutoEntryServices;
import gtanks.services.LobbysServices;
import gtanks.services.annotations.ServicesInject;
import gtanks.users.User;
import gtanks.users.garage.Garage;
import gtanks.users.locations.UserLocation;
import gtanks.utils.StringUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class BattlefieldPlayerController
implements IDisconnectListener,
Comparable<BattlefieldPlayerController> {
    public LobbyManager parentLobby;
    public BattlefieldModel battle;
    public Tank tank;
    public PlayerStatistic statistic;
    public String playerTeamType;
    public FlagServer flag;
    public InventoryController inventory;
    public long timer;
    public boolean userInited = false;
    @ServicesInject(target=LobbysServices.class)
    private final LobbysServices lobbysServices = LobbysServices.getInstance();
    @ServicesInject(target=AutoEntryServices.class)
    private final AutoEntryServices autoEntryServices = AutoEntryServices.instance();

    public BattlefieldPlayerController(LobbyManager parent, BattlefieldModel battle, String playerTeamType) {
        this.parentLobby = parent;
        this.battle = battle;
        this.playerTeamType = playerTeamType;
        this.tank = new Tank(null);
        this.tank.setHull(HullsFactory.getHull(this.getGarage().mountHull.getId()));
        this.tank.setWeapon(WeaponsFactory.getWeapon(this.getGarage().mountTurret.getId(), this, battle));
        this.tank.setColormap(ColormapsFactory.getColormap(this.getGarage().mountColormap.getId()));
        this.statistic = new PlayerStatistic(0, 0, 0);
        this.inventory = new InventoryController(this);
        battle.addPlayer(this);
        this.sendShotsData();
        OnlineStats.addInBattleOnline();
    }

    public User getUser() {
        return this.parentLobby.getLocalUser();
    }

    public Garage getGarage() {
        return this.parentLobby.getLocalUser().getGarage();
    }

    public void executeCommand(Command cmd) {
        try {
            switch (cmd.type) {
                case BATTLE: {
                    if (cmd.args[0].equals("get_init_data_local_tank")) {
                        this.battle.initLocalTank(this);
                        break;
                    }
                    if (cmd.args[0].equals("idle_kick")) {
                        this.parentLobby.kick();
                        break;
                    }
                    if (cmd.args[0].equals("activate_tank")) {
                        this.battle.activateTank(this);
                        break;
                    }
                    if (cmd.args[0].equals("suicide")) {
                        if (this.tank.state.equals("active")) {
                            this.battle.respawnPlayer(this, true);
                        }
                        break;
                    }
                    if (cmd.args[0].equals("move")) {
                        this.parseAndMove(cmd.args);
                        break;
                    }
                    if (cmd.args[0].equals("chat")) {
                        this.battle.chatModel.onMessage(this, cmd.args[1], Boolean.valueOf(cmd.args[2]));
                        break;
                    }
                    if (cmd.args[0].equals("attempt_to_take_bonus")) {
                        this.battle.onTakeBonus(this, cmd.args[1]);
                        break;
                    }
                    if (cmd.args[0].equals("start_fire")) {
                        if (this.tank.state.equals("active")) {
                            this.tank.getWeapon().startFire(cmd.args.length >= 2 ? cmd.args[1] : "");
                        }
                        break;
                    }
                    if (cmd.args[0].equals("fire")) {
                        if (this.tank.state.equals("active")) {
                            this.tank.getWeapon().fire(cmd.args[1]);
                        }
                        break;
                    }
                    if (cmd.args[0].equals("quick_shot_shaft")) {
                        if (this.tank.state.equals("active")) {
                            this.tank.getWeapon().quickFire(cmd.args[1]);
                        }
                        break;
                    }
                    if (cmd.args[0].equals("i_exit_from_battle")) {
                        this.parentLobby.onExitFromBattle();
                        break;
                    }
                    if (cmd.args[0].equals("stop_fire")) {
                        this.tank.getWeapon().stopFire();
                        break;
                    }
                    if (cmd.args[0].equals("exit_from_statistic")) {
                        this.parentLobby.onExitFromStatistic();
                        break;
                    }
                    if (cmd.args[0].equals("attempt_to_take_flag")) {
                        this.battle.ctfModel.attemptToTakeFlag(this, cmd.args[1]);
                        break;
                    }
                    if (cmd.args[0].equals("tank_capturing_point")) {
                        Vector3 tankPos;
                        try {
                            tankPos = new Vector3(Float.parseFloat(cmd.args[2]), Float.parseFloat(cmd.args[3]), Float.parseFloat(cmd.args[4]));
                        } catch (Exception var4) {
                            tankPos = new Vector3(0.0f, 0.0f, 0.0f);
                        }
                        this.battle.domModel.tankCapturingPoint(this, cmd.args[1], tankPos);
                    }
                    if (cmd.args[0].equals("tank_leave_capturing_point")) {
                        this.battle.domModel.tankLeaveCapturingPoint(this, cmd.args[1]);
                    }
                    if (cmd.args[0].equals("flag_drop")) {
                        this.parseAndDropFlag(cmd.args[1]);
                        break;
                    }
                    if (cmd.args[0].equals("speedhack_detected")) {
                        this.battle.cheatDetected(this, this.getClass());
                        break;
                    }
                    if (cmd.args[0].equals("activate_item")) {
                        Vector3 _tankPos;
                        try {
                            _tankPos = new Vector3(Float.parseFloat(cmd.args[2]), Float.parseFloat(cmd.args[3]), Float.parseFloat(cmd.args[4]));
                        } catch (Exception ex) {
                            _tankPos = new Vector3(0.0f, 0.0f, 0.0f);
                        }
                        this.inventory.activateItem(cmd.args[1], _tankPos);
                        break;
                    }
                    if (cmd.args[0].equals("mine_hit")) {
                        this.battle.battleMinesModel.hitMine(this, cmd.args[1]);
                        break;
                    }
                    if (cmd.args[0].equals("check_md5_map")) {
                        this.battle.mapChecksumModel.check(this, cmd.args[1]);
                    }
                    break;
                }
                default: {
                    Logger.log(Type.ERROR, "User " + this.parentLobby.getLocalUser().getNickname() + "[" + this.parentLobby.networker.toString() + "] send unknowed request!");
                    break;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void parseAndDropFlag(String json) {
        try {
            JSONObject _json = (JSONObject)new JSONParser().parse(json);
            this.battle.ctfModel.dropFlag(this, new Vector3((float)((Double)_json.get("x")).doubleValue(), (float)((Double)_json.get("y")).doubleValue(), (float)((Double)_json.get("z")).doubleValue()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void sendShotsData() {
        this.send(gtanks.commands.Type.BATTLE, "init_shots_data;{\"weapons\":[{\"has_wwd\":true,\"reload\":1250,\"max_damage_radius\":15.0,\"min_damage_percent\":0.0,\"min_damage_radius\":20.0,\"num_rays_up\":0,\"id\":\"flamethrower_m2\",\"auto_aiming_down\":0.0,\"num_rays_down\":0,\"auto_aiming_up\":0.0,\"special_entity\":{\"cooling_speed\":80,\"heating_speed\":200,\"target_detection_interval\":500,\"heat_limit\":1000,\"cone_angle\":0.349066,\"range\":23.494118}},{\"has_wwd\":true,\"reload\":1000,\"max_damage_radius\":15.0,\"min_damage_percent\":0.0,\"min_damage_radius\":20.0,\"num_rays_up\":0,\"id\":\"flamethrower_m3\",\"auto_aiming_down\":0.0,\"num_rays_down\":0,\"auto_aiming_up\":0.0,\"special_entity\":{\"cooling_speed\":100,\"heating_speed\":200,\"target_detection_interval\":500,\"heat_limit\":1000,\"cone_angle\":0.349066,\"range\":23.494118}},{\"has_wwd\":true,\"reload\":2000,\"max_damage_radius\":15.0,\"min_damage_percent\":0.0,\"min_damage_radius\":20.0,\"num_rays_up\":0,\"id\":\"flamethrower_m0\",\"auto_aiming_down\":0.0,\"num_rays_down\":0,\"auto_aiming_up\":0.0,\"special_entity\":{\"cooling_speed\":50,\"heating_speed\":200,\"target_detection_interval\":500,\"heat_limit\":1000,\"cone_angle\":0.349066,\"range\":23.494118}},{\"has_wwd\":true,\"reload\":1786,\"max_damage_radius\":15.0,\"min_damage_percent\":0.0,\"min_damage_radius\":20.0,\"num_rays_up\":0,\"id\":\"flamethrower_m1\",\"auto_aiming_down\":0.0,\"num_rays_down\":0,\"auto_aiming_up\":0.0,\"special_entity\":{\"cooling_speed\":5999,\"heating_speed\":20000,\"target_detection_interval\":500,\"heat_limit\":100000,\"cone_angle\":0.349066,\"range\":23.494118}},{\"has_wwd\":true,\"reload\":1000,\"max_damage_radius\":22.0,\"min_damage_percent\":0.0,\"min_damage_radius\":27.0,\"num_rays_up\":0,\"id\":\"frezeeny_m0\",\"auto_aiming_down\":0.0,\"num_rays_down\":0,\"auto_aiming_up\":0.0,\"special_entity\":{\"energyDischargeSpeed\":115,\"damageAreaRange\":27.0,\"damageAreaConeAngle\":0.349066,\"weaponTickMsec\":500,\"energyCapacity\":1000,\"energyRechargeSpeed\":100}},{\"has_wwd\":true,\"reload\":1900,\"max_damage_radius\":50.0,\"min_damage_percent\":20.0,\"min_damage_radius\":100.0,\"num_rays_up\":73,\"id\":\"smoky_m1\",\"auto_aiming_down\":0.20944,\"num_rays_down\":97,\"auto_aiming_up\":0.15708,\"special_entity\":{}},{\"has_wwd\":true,\"reload\":1750,\"max_damage_radius\":50.0,\"min_damage_percent\":20.0,\"min_damage_radius\":100.0,\"num_rays_up\":73,\"id\":\"smoky_m2\",\"auto_aiming_down\":0.20944,\"num_rays_down\":97,\"auto_aiming_up\":0.15708,\"special_entity\":{}},{\"has_wwd\":true,\"reload\":1600,\"max_damage_radius\":50.0,\"min_damage_percent\":20.0,\"min_damage_radius\":100.0,\"num_rays_up\":73,\"id\":\"smoky_m3\",\"auto_aiming_down\":0.20944,\"num_rays_down\":97,\"auto_aiming_up\":0.15708,\"special_entity\":{}},{\"has_wwd\":true,\"reload\":2100,\"max_damage_radius\":60.0,\"min_damage_percent\":50.0,\"min_damage_radius\":120.0,\"num_rays_up\":73,\"id\":\"hwthunder_m0\",\"auto_aiming_down\":0.20944,\"num_rays_down\":97,\"auto_aiming_up\":0.15708,\"special_entity\":{\"minSplashDamageRadius\":10.0,\"impactForce\":2.0,\"maxSplashDamageRadius\":5.0,\"minSplashDamagePercent\":25.0}},{\"has_wwd\":true,\"reload\":1500,\"max_damage_radius\":100.0,\"min_damage_percent\":20.0,\"min_damage_radius\":50.0,\"num_rays_up\":73,\"id\":\"smokyxt_m0\",\"auto_aiming_down\":0.20944,\"num_rays_down\":97,\"auto_aiming_up\":0.15708,\"special_entity\":{}},{\"has_wwd\":true,\"reload\":2000,\"max_damage_radius\":50.0,\"min_damage_percent\":20.0,\"min_damage_radius\":100.0,\"num_rays_up\":73,\"id\":\"smoky_m0\",\"auto_aiming_down\":0.20944,\"num_rays_down\":97,\"auto_aiming_up\":0.15708,\"special_entity\":{}},{\"has_wwd\":true,\"reload\":2000,\"max_damage_radius\":15.0,\"min_damage_percent\":0.0,\"min_damage_radius\":20.0,\"num_rays_up\":0,\"id\":\"flamethrowerhw_m0\",\"auto_aiming_down\":0.0,\"num_rays_down\":0,\"auto_aiming_up\":0.0,\"special_entity\":{\"cooling_speed\":125,\"heating_speed\":200,\"target_detection_interval\":500,\"heat_limit\":1000,\"cone_angle\":0.349066,\"range\":23.494118}},{\"has_wwd\":true,\"reload\":680,\"max_damage_radius\":100.0,\"min_damage_percent\":20.0,\"min_damage_radius\":55.0,\"num_rays_up\":88,\"id\":\"snowman_m0\",\"auto_aiming_down\":0.244346,\"num_rays_down\":112,\"auto_aiming_up\":0.191986,\"special_entity\":{\"shot_radius\":1.03,\"shot_speed\":35.0,\"shot_range\":55.0}},{\"has_wwd\":true,\"reload\":640,\"max_damage_radius\":100.0,\"min_damage_percent\":20.0,\"min_damage_radius\":60.0,\"num_rays_up\":88,\"id\":\"snowman_m1\",\"auto_aiming_down\":0.244346,\"num_rays_down\":112,\"auto_aiming_up\":0.191986,\"special_entity\":{\"shot_radius\":1.03,\"shot_speed\":38.0,\"shot_range\":60.0}},{\"has_wwd\":true,\"reload\":600,\"max_damage_radius\":100.0,\"min_damage_percent\":20.0,\"min_damage_radius\":65.0,\"num_rays_up\":88,\"id\":\"snowman_m2\",\"auto_aiming_down\":0.244346,\"num_rays_down\":112,\"auto_aiming_up\":0.191986,\"special_entity\":{\"shot_radius\":1.03,\"shot_speed\":39.0,\"shot_range\":65.0}},{\"has_wwd\":true,\"reload\":560,\"max_damage_radius\":100.0,\"min_damage_percent\":20.0,\"min_damage_radius\":70.0,\"num_rays_up\":88,\"id\":\"snowman_m3\",\"auto_aiming_down\":0.244346,\"num_rays_down\":112,\"auto_aiming_up\":0.191986,\"special_entity\":{\"shot_radius\":1.06,\"shot_speed\":44.0,\"shot_range\":70.0}},{\"has_wwd\":true,\"reload\":500,\"max_damage_radius\":60.0,\"min_damage_percent\":0.0,\"min_damage_radius\":80.0,\"num_rays_up\":134,\"id\":\"ricochet_m3\",\"auto_aiming_down\":0.344346,\"num_rays_down\":204,\"auto_aiming_up\":0.291986,\"special_entity\":{\"shotDistance\":80.0,\"energyPerShot\":2361,\"shotSpeed\":60.0,\"energyCapacity\":20000,\"energyRechargeSpeed\":2500.0,\"shotRadius\":1.0}},{\"has_wwd\":true,\"reload\":580,\"max_damage_radius\":60.0,\"min_damage_percent\":0.0,\"min_damage_radius\":80.0,\"num_rays_up\":134,\"id\":\"ricochet_m1\",\"auto_aiming_down\":0.444346,\"num_rays_down\":204,\"auto_aiming_up\":0.291986,\"special_entity\":{\"shotDistance\":80.0,\"energyPerShot\":2492,\"shotSpeed\":60.0,\"energyCapacity\":20000,\"energyRechargeSpeed\":2000.0,\"shotRadius\":1.0}},{\"has_wwd\":true,\"reload\":550,\"max_damage_radius\":60.0,\"min_damage_percent\":0.0,\"min_damage_radius\":80.0,\"num_rays_up\":134,\"id\":\"ricochet_m2\",\"auto_aiming_down\":0.444346,\"num_rays_down\":204,\"auto_aiming_up\":0.291986,\"special_entity\":{\"shotDistance\":80.0,\"energyPerShot\":2472,\"shotSpeed\":60.0,\"energyCapacity\":20000,\"energyRechargeSpeed\":2222.0,\"shotRadius\":1.0}},{\"has_wwd\":true,\"reload\":600,\"max_damage_radius\":60.0,\"min_damage_percent\":0.0,\"min_damage_radius\":80.0,\"num_rays_up\":134,\"id\":\"ricochet_m0\",\"auto_aiming_down\":0.444346,\"num_rays_down\":204,\"auto_aiming_up\":0.291986,\"special_entity\":{\"shotDistance\":80.0,\"energyPerShot\":2569,\"shotSpeed\":60.0,\"energyCapacity\":20000,\"energyRechargeSpeed\":1901.0,\"shotRadius\":1.0}},{\"has_wwd\":true,\"reload\":2000,\"max_damage_radius\":100.0,\"min_damage_percent\":100.0,\"min_damage_radius\":100.0,\"num_rays_up\":73,\"id\":\"shaft_m0\",\"auto_aiming_down\":0.20944,\"num_rays_down\":97,\"auto_aiming_up\":0.15708,\"special_entity\":{\"impact_quick_shot\":1.2,\"minimum_fov\":0.698132,\"inital_fov\":0.349066,\"max_energy\":1000.0,\"elevation_angle_up\":0.20944,\"shrubs_hiding_radius_max\":8000.0,\"discharge_rate\":113.63636,\"shrubs_hiding_radius_min\":2000.0,\"elevation_angle_down\":-0.261799,\"horizontal_targeting_speed\":0.244346,\"charge_rate\":294.11765,\"vertical_targeting_speed\":0.244346}},{\"has_wwd\":true,\"reload\":1900,\"max_damage_radius\":100.0,\"min_damage_percent\":100.0,\"min_damage_radius\":100.0,\"num_rays_up\":73,\"id\":\"shaft_m1\",\"auto_aiming_down\":0.20944,\"num_rays_down\":97,\"auto_aiming_up\":0.15708,\"special_entity\":{\"impact_quick_shot\":1.35,\"minimum_fov\":0.698132,\"inital_fov\":0.349066,\"max_energy\":1000.0,\"elevation_angle_up\":0.20944,\"shrubs_hiding_radius_max\":8000.0,\"discharge_rate\":116.27907,\"shrubs_hiding_radius_min\":2000.0,\"elevation_angle_down\":-0.261799,\"horizontal_targeting_speed\":0.261799,\"charge_rate\":312.5,\"vertical_targeting_speed\":0.261799}},{\"has_wwd\":false,\"reload\":5300,\"num_rays_up\":56,\"id\":\"railgunxt_m0\",\"auto_aiming_down\":0.174533,\"num_rays_down\":81,\"auto_aiming_up\":0.122173,\"special_entity\":{}},{\"has_wwd\":true,\"reload\":1750,\"max_damage_radius\":100.0,\"min_damage_percent\":100.0,\"min_damage_radius\":100.0,\"num_rays_up\":73,\"id\":\"shaft_m2\",\"auto_aiming_down\":0.20944,\"num_rays_down\":97,\"auto_aiming_up\":0.15708,\"special_entity\":{\"impact_quick_shot\":1.5,\"minimum_fov\":0.698132,\"inital_fov\":0.349066,\"max_energy\":1000.0,\"elevation_angle_up\":0.20944,\"shrubs_hiding_radius_max\":8000.0,\"discharge_rate\":119.04762,\"shrubs_hiding_radius_min\":2000.0,\"elevation_angle_down\":-0.261799,\"horizontal_targeting_speed\":0.296706,\"charge_rate\":333.33334,\"vertical_targeting_speed\":0.296706}},{\"has_wwd\":true,\"reload\":1600,\"max_damage_radius\":100.0,\"min_damage_percent\":100.0,\"min_damage_radius\":100.0,\"num_rays_up\":73,\"id\":\"shaft_m3\",\"auto_aiming_down\":0.20944,\"num_rays_down\":97,\"auto_aiming_up\":0.15708,\"special_entity\":{\"impact_quick_shot\":1.65,\"minimum_fov\":0.698132,\"inital_fov\":0.349066,\"max_energy\":1000.0,\"elevation_angle_up\":0.20944,\"shrubs_hiding_radius_max\":8000.0,\"discharge_rate\":121.95122,\"shrubs_hiding_radius_min\":2000.0,\"elevation_angle_down\":-0.261799,\"horizontal_targeting_speed\":0.349066,\"charge_rate\":357.14285,\"vertical_targeting_speed\":0.349066}},{\"has_wwd\":true,\"reload\":1000,\"max_damage_radius\":15.0,\"min_damage_percent\":0.0,\"min_damage_radius\":20.0,\"num_rays_up\":0,\"id\":\"frezee_m3\",\"auto_aiming_down\":0.0,\"num_rays_down\":0,\"auto_aiming_up\":0.0,\"special_entity\":{\"energyDischargeSpeed\":120,\"damageAreaRange\":20.0,\"damageAreaConeAngle\":0.349066,\"weaponTickMsec\":500,\"energyCapacity\":1000,\"energyRechargeSpeed\":100}},{\"has_wwd\":false,\"reload\":1000,\"num_rays_up\":0,\"id\":\"isidahw_m0\",\"auto_aiming_down\":0.0,\"num_rays_down\":0,\"auto_aiming_up\":0.0,\"special_entity\":{\"chargeRate\":25,\"coneAngle\":450.0,\"angle\":45.0,\"radius\":25.0,\"tickPeriod\":500,\"capacity\":100,\"dischargeRate\":25}},{\"has_wwd\":true,\"reload\":3500,\"max_damage_radius\":60.0,\"min_damage_percent\":50.0,\"min_damage_radius\":120.0,\"num_rays_up\":73,\"id\":\"thunder_m0\",\"auto_aiming_down\":0.20944,\"num_rays_down\":97,\"auto_aiming_up\":0.15708,\"special_entity\":{\"minSplashDamageRadius\":10.0,\"impactForce\":2.0,\"maxSplashDamageRadius\":5.0,\"minSplashDamagePercent\":25.0}},{\"has_wwd\":true,\"reload\":3160,\"max_damage_radius\":60.0,\"min_damage_percent\":50.0,\"min_damage_radius\":120.0,\"num_rays_up\":73,\"id\":\"thunder_m1\",\"auto_aiming_down\":0.20944,\"num_rays_down\":97,\"auto_aiming_up\":0.15708,\"special_entity\":{\"minSplashDamageRadius\":10.0,\"impactForce\":2.0,\"maxSplashDamageRadius\":5.0,\"minSplashDamagePercent\":25.0}},{\"has_wwd\":true,\"reload\":1000,\"max_damage_radius\":15.0,\"min_damage_percent\":0.0,\"min_damage_radius\":20.0,\"num_rays_up\":0,\"id\":\"frezee_m0\",\"auto_aiming_down\":0.0,\"num_rays_down\":0,\"auto_aiming_up\":0.0,\"special_entity\":{\"energyDischargeSpeed\":120,\"damageAreaRange\":20.0,\"damageAreaConeAngle\":0.349066,\"weaponTickMsec\":500,\"energyCapacity\":1000,\"energyRechargeSpeed\":50}},{\"has_wwd\":true,\"reload\":1000,\"max_damage_radius\":15.0,\"min_damage_percent\":0.0,\"min_damage_radius\":20.0,\"num_rays_up\":0,\"id\":\"frezee_m2\",\"auto_aiming_down\":0.0,\"num_rays_down\":0,\"auto_aiming_up\":0.0,\"special_entity\":{\"energyDischargeSpeed\":120,\"damageAreaRange\":20.0,\"damageAreaConeAngle\":0.349066,\"weaponTickMsec\":500,\"energyCapacity\":1000,\"energyRechargeSpeed\":80}},{\"has_wwd\":true,\"reload\":1000,\"max_damage_radius\":15.0,\"min_damage_percent\":0.0,\"min_damage_radius\":20.0,\"num_rays_up\":0,\"id\":\"frezee_m1\",\"auto_aiming_down\":0.0,\"num_rays_down\":0,\"auto_aiming_up\":0.0,\"special_entity\":{\"energyDischargeSpeed\":120,\"damageAreaRange\":20.0,\"damageAreaConeAngle\":0.349066,\"weaponTickMsec\":500,\"energyCapacity\":1000,\"energyRechargeSpeed\":60}},{\"has_wwd\":false,\"reload\":6000,\"num_rays_up\":56,\"id\":\"railgun_m2\",\"auto_aiming_down\":0.174533,\"num_rays_down\":81,\"auto_aiming_up\":0.122173,\"special_entity\":{}},{\"has_wwd\":false,\"reload\":5800,\"num_rays_up\":56,\"id\":\"railgun_m3\",\"auto_aiming_down\":0.174533,\"num_rays_down\":81,\"auto_aiming_up\":0.122173,\"special_entity\":{}},{\"has_wwd\":false,\"reload\":6400,\"num_rays_up\":56,\"id\":\"railgun_m0\",\"auto_aiming_down\":0.174533,\"num_rays_down\":81,\"auto_aiming_up\":0.122173,\"special_entity\":{}},{\"has_wwd\":false,\"reload\":4300,\"num_rays_up\":56,\"id\":\"railgunxt_m0\",\"auto_aiming_down\":0.174533,\"num_rays_down\":81,\"auto_aiming_up\":0.122173,\"special_entity\":{}},{\"has_wwd\":false,\"reload\":6200,\"num_rays_up\":56,\"id\":\"railgun_m1\",\"auto_aiming_down\":0.174533,\"num_rays_down\":81,\"auto_aiming_up\":0.122173,\"special_entity\":{}},{\"has_wwd\":true,\"reload\":500,\"max_damage_radius\":60.0,\"min_damage_percent\":0.0,\"min_damage_radius\":80.0,\"num_rays_up\":134,\"id\":\"pumpkingun_m3\",\"auto_aiming_down\":0.344346,\"num_rays_down\":204,\"auto_aiming_up\":0.291986,\"special_entity\":{\"shotDistance\":80.0,\"energyPerShot\":228,\"shotSpeed\":60.0,\"energyCapacity\":2000,\"energyRechargeSpeed\":240.0,\"shotRadius\":1.0}},{\"has_wwd\":true,\"reload\":550,\"max_damage_radius\":60.0,\"min_damage_percent\":0.0,\"min_damage_radius\":80.0,\"num_rays_up\":134,\"id\":\"pumpkingun_m2\",\"auto_aiming_down\":0.444346,\"num_rays_down\":204,\"auto_aiming_up\":0.291986,\"special_entity\":{\"shotDistance\":80.0,\"energyPerShot\":240,\"shotSpeed\":60.0,\"energyCapacity\":2000,\"energyRechargeSpeed\":222.2,\"shotRadius\":1.0}},{\"has_wwd\":false,\"reload\":1786,\"num_rays_up\":0,\"id\":\"isida_m1\",\"auto_aiming_down\":0.0,\"num_rays_down\":0,\"auto_aiming_up\":0.0,\"special_entity\":{\"chargeRate\":5599,\"coneAngle\":450.0,\"angle\":45.0,\"radius\":15.0,\"tickPeriod\":500,\"capacity\":100000,\"dischargeRate\":20000}},{\"has_wwd\":true,\"reload\":580,\"max_damage_radius\":60.0,\"min_damage_percent\":0.0,\"min_damage_radius\":80.0,\"num_rays_up\":134,\"id\":\"pumpkingun_m1\",\"auto_aiming_down\":0.444346,\"num_rays_down\":204,\"auto_aiming_up\":0.291986,\"special_entity\":{\"shotDistance\":80.0,\"energyPerShot\":255,\"shotSpeed\":60.0,\"energyCapacity\":2000,\"energyRechargeSpeed\":200.0,\"shotRadius\":1.0}},{\"has_wwd\":true,\"reload\":330,\"max_damage_radius\":40.0,\"min_damage_percent\":0.0,\"min_damage_radius\":60.0,\"num_rays_up\":88,\"id\":\"twins_m3\",\"auto_aiming_down\":0.244346,\"num_rays_down\":112,\"auto_aiming_up\":0.191986,\"special_entity\":{\"shot_radius\":1.03,\"shot_speed\":90.0,\"shot_range\":60.0}},{\"has_wwd\":false,\"reload\":2000,\"num_rays_up\":0,\"id\":\"isida_m0\",\"auto_aiming_down\":0.0,\"num_rays_down\":0,\"auto_aiming_up\":0.0,\"special_entity\":{\"chargeRate\":50,\"coneAngle\":450.0,\"angle\":45.0,\"radius\":15.0,\"tickPeriod\":500,\"capacity\":1000,\"dischargeRate\":200}},{\"has_wwd\":true,\"reload\":600,\"max_damage_radius\":60.0,\"min_damage_percent\":0.0,\"min_damage_radius\":80.0,\"num_rays_up\":134,\"id\":\"pumpkingun_m0\",\"auto_aiming_down\":0.444346,\"num_rays_down\":204,\"auto_aiming_up\":0.291986,\"special_entity\":{\"shotDistance\":80.0,\"energyPerShot\":170,\"shotSpeed\":60.0,\"energyCapacity\":1300,\"energyRechargeSpeed\":120.0,\"shotRadius\":1.0}},{\"has_wwd\":true,\"reload\":400,\"max_damage_radius\":40.0,\"min_damage_percent\":0.0,\"min_damage_radius\":60.0,\"num_rays_up\":88,\"id\":\"twins_m1\",\"auto_aiming_down\":0.244346,\"num_rays_down\":112,\"auto_aiming_up\":0.191986,\"special_entity\":{\"shot_radius\":1.03,\"shot_speed\":90.0,\"shot_range\":60.0}},{\"has_wwd\":true,\"reload\":370,\"max_damage_radius\":40.0,\"min_damage_percent\":0.0,\"min_damage_radius\":60.0,\"num_rays_up\":88,\"id\":\"twins_m2\",\"auto_aiming_down\":0.244346,\"num_rays_down\":112,\"auto_aiming_up\":0.191986,\"special_entity\":{\"shot_radius\":1.03,\"shot_speed\":90.0,\"shot_range\":60.0}},{\"has_wwd\":true,\"reload\":370,\"max_damage_radius\":40.0,\"min_damage_percent\":0.0,\"min_damage_radius\":60.0,\"num_rays_up\":88,\"id\":\"twins_m0\",\"auto_aiming_down\":0.244346,\"num_rays_down\":112,\"auto_aiming_up\":0.191986,\"special_entity\":{\"shot_radius\":1.03,\"shot_speed\":90.0,\"shot_range\":60.0}},{\"has_wwd\":false,\"reload\":1000,\"num_rays_up\":0,\"id\":\"isida_m3\",\"auto_aiming_down\":0.0,\"num_rays_down\":0,\"auto_aiming_up\":0.0,\"special_entity\":{\"chargeRate\":100,\"coneAngle\":450.0,\"angle\":45.0,\"radius\":15.0,\"tickPeriod\":500,\"capacity\":1000,\"dischargeRate\":200}},{\"has_wwd\":false,\"reload\":1250,\"num_rays_up\":0,\"id\":\"isida_m2\",\"auto_aiming_down\":0.0,\"num_rays_down\":0,\"auto_aiming_up\":0.0,\"special_entity\":{\"chargeRate\":80,\"coneAngle\":450.0,\"angle\":45.0,\"radius\":15.0,\"tickPeriod\":500,\"capacity\":1000,\"dischargeRate\":200}},{\"has_wwd\":true,\"reload\":2700,\"max_damage_radius\":60.0,\"min_damage_percent\":50.0,\"min_damage_radius\":120.0,\"num_rays_up\":73,\"id\":\"thunder_m2\",\"auto_aiming_down\":0.20944,\"num_rays_down\":97,\"auto_aiming_up\":0.15708,\"special_entity\":{\"minSplashDamageRadius\":10.0,\"impactForce\":2.0,\"maxSplashDamageRadius\":5.0,\"minSplashDamagePercent\":25.0}},{\"has_wwd\":true,\"reload\":2250,\"max_damage_radius\":60.0,\"min_damage_percent\":50.0,\"min_damage_radius\":120.0,\"num_rays_up\":73,\"id\":\"thunder_m3\",\"auto_aiming_down\":0.20944,\"num_rays_down\":97,\"auto_aiming_up\":0.15708,\"special_entity\":{\"minSplashDamageRadius\":10.0,\"impactForce\":2.0,\"maxSplashDamageRadius\":5.0,\"minSplashDamagePercent\":25.0}},{\"has_wwd\":true,\"reload\":330,\"max_damage_radius\":40.0,\"min_damage_percent\":0.0,\"min_damage_radius\":60.0,\"num_rays_up\":88,\"id\":\"twinsxt_m0\",\"auto_aiming_down\":0.244346,\"num_rays_down\":112,\"auto_aiming_up\":0.191986,\"special_entity\":{\"shot_radius\":1.03,\"shot_speed\":90.0,\"shot_range\":60.0}},{\"has_wwd\":true,\"reload\":2250,\"max_damage_radius\":60.0,\"min_damage_percent\":50.0,\"min_damage_radius\":120.0,\"num_rays_up\":73,\"id\":\"bfg_m0\",\"auto_aiming_down\":0.20944,\"num_rays_down\":97,\"auto_aiming_up\":0.15708,\"special_entity\":{\"minSplashDamageRadius\":20.0,\"impactForce\":10.0,\"maxSplashDamageRadius\":10.0,\"minSplashDamagePercent\":25.0}}]}");
    }

    public void parseAndMove(String[] args) {
        try {
            Vector3 pos = new Vector3(0.0f, 0.0f, 0.0f);
            Vector3 orient = new Vector3(0.0f, 0.0f, 0.0f);
            Vector3 line = new Vector3(0.0f, 0.0f, 0.0f);
            Vector3 ange = new Vector3(0.0f, 0.0f, 0.0f);
            float turretDir = 0.0f;
            int bits = 0;
            String[] temp = args[1].split("@");
            pos.x = Float.parseFloat(temp[0]);
            pos.y = Float.parseFloat(temp[1]);
            pos.z = Float.parseFloat(temp[2]);
            orient.x = Float.parseFloat(temp[3]);
            orient.y = Float.parseFloat(temp[4]);
            orient.z = Float.parseFloat(temp[5]);
            line.x = Float.parseFloat(temp[6]);
            line.y = Float.parseFloat(temp[7]);
            line.z = Float.parseFloat(temp[8]);
            ange.x = Float.parseFloat(temp[9]);
            ange.y = Float.parseFloat(temp[10]);
            ange.z = Float.parseFloat(temp[11]);
            turretDir = Float.parseFloat(args[2]);
            bits = Integer.parseInt(args[3]);
            if (this.tank.position == null) {
                this.tank.position = new Vector3(0.0f, 0.0f, 0.0f);
            }
            if (args[1].startsWith("000") || args[1].startsWith("00") || args[1].startsWith("-00") || args[1].startsWith("-000")) {
                System.out.println("invisible detected OR megakill");
                this.battle.cheatDetected(this, this.getClass());
                return;
            }
            this.tank.position = pos;
            this.tank.orientation = orient;
            this.tank.linVel = line;
            this.tank.angVel = ange;
            this.tank.turretDir = turretDir;
            this.tank.controllBits = bits;
            this.battle.moveTank(this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void clearEffects() {
        while (this.tank.activeEffects.size() > 0) {
            this.tank.activeEffects.get(0).deactivate();
        }
    }

    public void destroy(boolean cache) {
        this.battle.removeUser(this, cache);
        if (!cache) {
            this.lobbysServices.sendCommandToAllUsers(gtanks.commands.Type.LOBBY, UserLocation.BATTLESELECT, "remove_player_from_battle", JSONUtils.parseRemovePlayerComand(this));
            if (!this.battle.battleInfo.team) {
                this.lobbysServices.sendCommandToAllUsers(gtanks.commands.Type.LOBBY, UserLocation.BATTLESELECT, StringUtils.concatStrings("update_count_users_in_dm_battle", ";", this.battle.battleInfo.battleId, ";", String.valueOf(this.battle.players.size())));
            } else {
                this.lobbysServices.sendCommandToAllUsers(gtanks.commands.Type.LOBBY, UserLocation.BATTLESELECT, "update_count_users_in_team_battle", JSONUtils.parseUpdateCoundPeoplesCommand(this.battle.battleInfo));
            }
        }
        this.parentLobby = null;
        this.battle = null;
        this.tank = null;
    }

    public void send(gtanks.commands.Type type, String ... args) {
        if (this.parentLobby != null) {
            this.parentLobby.send(type, args);
        }
    }

    @Override
    public void onDisconnect() {
        this.autoEntryServices.userExit(this);
        this.destroy(true);
        OnlineStats.removeInBattleOnline();
    }

    @Override
    public int compareTo(BattlefieldPlayerController o) {
        return (int)(o.statistic.getScore() - this.statistic.getScore());
    }
}

