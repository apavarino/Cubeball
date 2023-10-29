package me.crylonz;

import org.bstats.bukkit.Metrics;
import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;

import static java.lang.Math.abs;
import static me.crylonz.MatchState.IN_PROGRESS;
import static me.crylonz.MatchState.OVERTIME;

public class CubeBall extends JavaPlugin {

    public static File configFile;
    public static FileConfiguration customConfig = null;
    public static HashMap<String, Ball> balls = new HashMap<>();

    public static Match match;

    public static String BALL_MATCH_ID = "BALL_MATCH_ID_DONT_USE_IT";
    public static Plugin plugin;
    public static int matchTimer = 0;
    public final static Logger log = Logger.getLogger("Minecraft");

    // config
    public static Material cubeBallBlock = Material.IRON_BLOCK;
    public static int matchDuration = 300;
    public static int maxGoal = 0;

    public static void generateBall(String id, Location location, Vector lastVelocity) {

        if (balls.get(id) != null) {
            throw new IllegalStateException("Same ID cannot be put on the same ball");
        }

        BlockData blockData = Bukkit.createBlockData(cubeBallBlock);
        FallingBlock block = Objects.requireNonNull(location.getWorld()).spawnFallingBlock(location, blockData);
        block.setMetadata("ballID", new FixedMetadataValue(plugin, id));
        block.setGlowing(true);
        block.setDropItem(false);
        block.setInvulnerable(true);
        block.playEffect(EntityEffect.ENTITY_POOF);

        Ball ball = new Ball();
        ball.setId(id);
        ball.setBall(block);

        if (lastVelocity != null) {
            ball.getLastVelocity().setX(0);
            ball.getLastVelocity().setZ(0);
        }

        ball.setPlayerCollisionTick(0);
        balls.put(id, ball);
    }

    public static void destroyBall(String id) {
        if (balls.get(id) != null && balls.get(id).getBall() != null) {
            balls.get(id).getBall().remove();
            balls.remove(id);
        }
    }

    public void onEnable() {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new CubeBallListener(), this);

        plugin = this;

        Metrics metrics = new Metrics(this, 17634);

        launchRepeatingTask();

        Objects.requireNonNull(this.getCommand("cb"), "Command cb not found")
                .setExecutor(new CBCommandExecutor());

        Objects.requireNonNull(getCommand("cb")).setTabCompleter(new CBTabCompletion());

        configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            saveDefaultConfig();
        } else {
            cubeBallBlock = Material.valueOf((String) getConfig().get("cubeBallBlock"));
            matchDuration = getConfig().getInt("matchDuration");
            maxGoal = getConfig().getInt("maxGoal");
        }
    }

    public void onDisable() {
        balls.forEach((key, value) -> {
            if (value.getBall() != null) {
                value.getBall().remove();
            }
        });
    }

    private void launchRepeatingTask() {

        getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            if (match != null && match.getMatchState().equals(IN_PROGRESS)) {
                matchTimer--;

                if (matchTimer % 60 == 0 && matchTimer > 0) {
                    match.getAllPlayer().forEach(player -> {
                        if (player != null) {
                            player.sendMessage("[CubeBall] " + ChatColor.GOLD + matchTimer / 60 + " min left !");
                        }

                    });
                }
                if (matchTimer == 30 || matchTimer == 15 || matchTimer <= 10 && matchTimer > 0) {
                    match.getAllPlayer().forEach(player -> {
                        if (player != null) {
                            player.sendMessage("[CubeBall] " + ChatColor.GOLD + matchTimer + " sec left !");
                        }
                    });
                }
                if (matchTimer <= 0) {
                    match.endMatch();
                    if (match.getMatchState() != OVERTIME) {
                        match = null;
                    }
                }
            }
        }, 0, 20);


        getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {

            for (Iterator<Map.Entry<String, Ball>> iterator = balls.entrySet().iterator(); iterator.hasNext(); ) {
                Map.Entry<String, Ball> entry = iterator.next();
                String id = entry.getKey();
                Ball ballData = entry.getValue();
                if (ballData.getBall() != null) {
                    ballData.getBall().setTicksLived(1);

                    ArrayList<Player> players = new ArrayList<>();
                    if (match != null) {
                        players.addAll(match.getBlueTeam());
                        players.addAll(match.getRedTeam());
                    } else {
                        players.addAll(Bukkit.getOnlinePlayers());
                    }

                    ballData.getBall().getNearbyEntities(10, 10, 10)
                            .stream().filter(entity -> entity instanceof Player)
                            .forEach(p -> {
                                Player player = (Player) p;
                                // if player is colliding the ball
                                if (player.getLocation().distance(ballData.getBall().getLocation()) < 1 || (
                                        player.getLocation().distance(ballData.getBall().getLocation()) < 2 &&
                                                Math.floor(ballData.getBall().getLocation().getX()) == Math.floor(player.getLocation().getX()) &&
                                                Math.floor(ballData.getBall().getLocation().getZ()) == Math.floor(player.getLocation().getZ()))) {

                                    // compute velocity to the ball
                                    double yVeclocity = 0.15;

                                    if (player.isSneaking()) {
                                        yVeclocity = 0.5;
                                    } else if (player.isSprinting()) {
                                        yVeclocity = 0.25;
                                    }

                                    Vector velocity = ballData.getBall().getVelocity();
                                    velocity.setY(ballData.getBall().getVelocity().getY() + yVeclocity + player.getVelocity().getY() / 2);
                                    velocity.setX(ballData.getBall().getVelocity().getX() + player.getLocation().getDirection().getX() / 2);
                                    velocity.setZ(ballData.getBall().getVelocity().getZ() + player.getLocation().getDirection().getZ() / 2);

                                    // if player is not moving, create bouncing on it
                                    if (abs(player.getVelocity().getX() + player.getVelocity().getY() + player.getVelocity().getZ()) == 0) {
                                        velocity.setY(0);
                                        velocity.setX(0);
                                        velocity.setZ(0);
                                    }

                                    // apply ball trajectory
                                    ballData.getBall().setVelocity(velocity);
                                    ballData.getBall().setGravity(true);
                                    ballData.getBall().getWorld().playSound(ballData.getBall().getLocation(), Sound.BLOCK_WOOL_HIT, 10, 1);
                                    ballData.setPlayerCollisionTick(0);

                                    if (match != null) {
                                        match.setLastTouchPlayer(player.getDisplayName());
                                    }
                                }
                            });

                    //compute bouncing on other blocks
                    if (ballData.getPlayerCollisionTick() > 3) {

                        boolean zBouncing = abs(ballData.getLastVelocity().getZ()) - abs(ballData.getBall().getVelocity().getZ()) > 0.2 && ballData.getBall().getVelocity().getZ() == 0;
                        boolean xBouncing = abs(ballData.getLastVelocity().getX()) - abs(ballData.getBall().getVelocity().getX()) > 0.2 && ballData.getBall().getVelocity().getX() == 0;
                        boolean yBouncing = abs(ballData.getLastVelocity().getY()) - abs(ballData.getBall().getVelocity().getY()) > 0.2 && ballData.getBall().getVelocity().getY() == 0;

                        if (zBouncing) {
                            ballData.getBall().setVelocity(ballData.getBall().getVelocity().setZ(-ballData.getLastVelocity().getZ()));
                            ballData.getBall().getVelocity().setZ(-ballData.getLastVelocity().getZ());
                            ballData.getBall().getWorld().playSound(ballData.getBall().getLocation(), Sound.BLOCK_WOOL_HIT, 10, 1);
                        }
                        if (xBouncing) {
                            ballData.getBall().setVelocity(ballData.getBall().getVelocity().setX(-ballData.getLastVelocity().getX()));
                            ballData.getBall().getVelocity().setX(-ballData.getLastVelocity().getX());
                            ballData.getBall().getWorld().playSound(ballData.getBall().getLocation(), Sound.BLOCK_WOOL_HIT, 10, 1);
                        }
                        if (yBouncing) {
                            ballData.getBall().setGravity(true);
                            ballData.getBall().setVelocity(ballData.getBall().getVelocity().setY(-ballData.getLastVelocity().getY()));
                            ballData.getBall().getWorld().playSound(ballData.getBall().getLocation(), Sound.BLOCK_WOOL_HIT, 10, 1);
                        }
                    }

                    if (match != null && ballData.getId().equals(BALL_MATCH_ID)) {
                        match.checkGoal(ballData.getBall().getLocation());
                    }

                    ballData.setLastVelocity(ballData.getBall().getVelocity().clone());
                    ballData.setPlayerCollisionTick(ballData.getPlayerCollisionTick() + 1);
                }
            }
        }, 0, 2);
    }
}


