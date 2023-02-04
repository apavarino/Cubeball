package me.crylonz;

import org.bstats.bukkit.Metrics;
import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;
import java.util.logging.Logger;

import static java.lang.Math.abs;
import static me.crylonz.MatchState.IN_PROGRESS;
import static me.crylonz.MatchState.OVERTIME;

public class CubeBall extends JavaPlugin {

    public static File configFile;
    public static FileConfiguration customConfig = null;
    public static FallingBlock ball = null;
    public static Vector lastVelocity = null;
    public static int playerCollisionTick = 0;
    public static Match match;
    public static Plugin plugin;
    public static int matchTimer = 0;
    public final static Logger log = Logger.getLogger("Minecraft");

    // config
    public static Material cubeBallBlock = Material.IRON_BLOCK;
    public static int matchDuration = 300;
    public static int maxGoal = 0;

    public static void generateBall(Location location) {
        BlockData blockData = Bukkit.createBlockData(cubeBallBlock);
        ball = Objects.requireNonNull(location.getWorld()).spawnFallingBlock(location, blockData);
        ball.setGlowing(true);
        ball.setDropItem(false);
        ball.setInvulnerable(true);
        ball.playEffect(EntityEffect.ENTITY_POOF);

        if (lastVelocity != null) {
            lastVelocity.setX(0);
            lastVelocity.setZ(0);
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
        if (ball != null) {
            ball.remove();
        }
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
            if (ball != null) {
                ball.setTicksLived(1);

                ArrayList<Player> players = new ArrayList<>();
                if (match != null) {
                    players.addAll(match.getBlueTeam());
                    players.addAll(match.getRedTeam());
                } else {
                    players.addAll(Bukkit.getOnlinePlayers());
                }

                for (Player player : players) {
                    if (!player.isOnline() && !player.getWorld().equals(ball.getWorld())) {
                        break;
                    }

                    // if player is colliding the ball
                    if (player.getLocation().distance(ball.getLocation()) < 1 || (
                            player.getLocation().distance(ball.getLocation()) < 2 &&
                                    Math.floor(ball.getLocation().getX()) == Math.floor(player.getLocation().getX()) &&
                                    Math.floor(ball.getLocation().getZ()) == Math.floor(player.getLocation().getZ()))) {

                        // compute velocity to the ball
                        double yVeclocity = 0.15;

                        if (player.isSneaking()) {
                            yVeclocity = 0.5;
                        } else if (player.isSprinting()) {
                            yVeclocity = 0.25;
                        }

                        Vector velocity = ball.getVelocity();
                        velocity.setY(ball.getVelocity().getY() + yVeclocity + player.getVelocity().getY() / 2);
                        velocity.setX(ball.getVelocity().getX() + player.getLocation().getDirection().getX() / 2);
                        velocity.setZ(ball.getVelocity().getZ() + player.getLocation().getDirection().getZ() / 2);

                        // if player is not moving, create bouncing on it
                        if (abs(player.getVelocity().getX() + player.getVelocity().getY() + player.getVelocity().getZ()) == 0) {
                            velocity.setY(0);
                            velocity.setX(0);
                            velocity.setZ(0);
                        }

                        // apply ball trajectory
                        ball.setVelocity(velocity);
                        ball.setGravity(true);
                        ball.getWorld().playSound(ball.getLocation(), Sound.BLOCK_WOOL_HIT, 10, 1);
                        playerCollisionTick = 0;

                        if (match != null) {
                            match.setLastTouchPlayer(player.getDisplayName());
                        }
                        break;
                    }
                }

                //compute bouncing on other blocks
                if (playerCollisionTick > 3) {

                    boolean zBouncing = abs(lastVelocity.getZ()) - abs(ball.getVelocity().getZ()) > 0.2 && ball.getVelocity().getZ() == 0;
                    boolean xBouncing = abs(lastVelocity.getX()) - abs(ball.getVelocity().getX()) > 0.2 && ball.getVelocity().getX() == 0;
                    boolean yBouncing = abs(lastVelocity.getY()) - abs(ball.getVelocity().getY()) > 0.2 && ball.getVelocity().getY() == 0;

                    if (zBouncing) {
                        ball.setVelocity(ball.getVelocity().setZ(-lastVelocity.getZ()));
                        ball.getVelocity().setZ(-lastVelocity.getZ());
                        ball.getWorld().playSound(ball.getLocation(), Sound.BLOCK_WOOL_HIT, 10, 1);
                    }
                    if (xBouncing) {
                        ball.setVelocity(ball.getVelocity().setX(-lastVelocity.getX()));
                        ball.getVelocity().setX(-lastVelocity.getX());
                        ball.getWorld().playSound(ball.getLocation(), Sound.BLOCK_WOOL_HIT, 10, 1);
                    }
                    if (yBouncing) {
                        ball.setGravity(true);
                        ball.setVelocity(ball.getVelocity().setY(-lastVelocity.getY()));
                        ball.getWorld().playSound(ball.getLocation(), Sound.BLOCK_WOOL_HIT, 10, 1);
                    }
                }

                if (match != null) {
                    match.checkGoal(ball.getLocation());
                }

                lastVelocity = ball.getVelocity().clone();
                playerCollisionTick++;
            }
        }, 0, 2);
    }
}


