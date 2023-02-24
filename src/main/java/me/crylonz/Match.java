package me.crylonz;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

import static me.crylonz.CubeBall.*;
import static me.crylonz.MatchState.*;
import static org.bukkit.Bukkit.getServer;

enum Team {RED, BLUE, SPECTATOR}

enum MatchState {CREATED, READY, IN_PROGRESS, GOAL, PAUSED, OVERTIME, END}

public class Match {

    private static final Material ballSpawnBlock = Material.EMERALD_BLOCK;
    private static final Material blueTeamSpawnBlock = Material.BLUE_WOOL;
    private static final Material blueTeamGoalBlock = Material.BLUE_CONCRETE;
    private static final Material redTeamSpawnBlock = Material.RED_WOOL;
    private static final Material redTeamGoalBlock = Material.RED_CONCRETE;
    private final Random rand = new Random();
    private final ArrayList<Location> blueTeamGoalBlocks;
    private final ArrayList<Location> redTeamGoalBlocks;
    private final ArrayList<Player> blueTeam;
    private final ArrayList<Player> redTeam;
    private final ArrayList<Player> spectatorTeam;
    private MatchState matchState;
    private Location ballSpawn;
    private String lastTouchPlayer;
    private ArrayList<Location> blueTeamSpawns;
    private ArrayList<Location> redTeamSpawns;
    private int blueScore;
    private int redScore;

    public Match() {
        blueTeam = new ArrayList<>();
        redTeam = new ArrayList<>();
        spectatorTeam = new ArrayList<>();
        blueTeamGoalBlocks = new ArrayList<>();
        redTeamGoalBlocks = new ArrayList<>();
        blueScore = 0;
        redScore = 0;
        matchState = CREATED;
    }

    public void scanSpawn(Player p) {

        ballSpawn = null;
        blueTeamSpawns = new ArrayList<>();
        redTeamSpawns = new ArrayList<>();

        int radius = 75;
        final Block block = p.getLocation().getBlock();
        for (int x = -(radius); x <= radius; x++) {
            for (int y = -(radius); y <= radius; y++) {
                for (int z = -(radius); z <= radius; z++) {
                    if (block.getRelative(x, y, z).getType() == ballSpawnBlock) {
                        ballSpawn = block.getRelative(x, y + 3, z).getLocation().add(.5, 0, .5);
                    }
                    if (block.getRelative(x, y, z).getType() == blueTeamSpawnBlock) {
                        blueTeamSpawns.add(block.getRelative(x, y + 2, z).getLocation());
                    }
                    if (block.getRelative(x, y, z).getType() == redTeamSpawnBlock) {
                        redTeamSpawns.add(block.getRelative(x, y + 2, z).getLocation());
                    }
                    if (block.getRelative(x, y, z).getType() == blueTeamGoalBlock) {
                        blueTeamGoalBlocks.add(block.getRelative(x, y, z).getLocation());
                    }
                    if (block.getRelative(x, y, z).getType() == redTeamGoalBlock) {
                        redTeamGoalBlocks.add(block.getRelative(x, y, z).getLocation());
                    }
                }
            }
        }

        if (ballSpawn != null && blueTeamSpawns.size() > 0 && redTeamSpawns.size() > 0 &&
                blueTeamGoalBlocks.size() > 0 && redTeamGoalBlocks.size() > 0) {
            p.sendMessage("--- MATCH READY ---");
            matchState = READY;
        } else {
            p.sendMessage("--- ERROR ---");
        }
        p.sendMessage("Ball Spawn : " + (ballSpawn != null ? ChatColor.GREEN + "OK" : ChatColor.RED + "KO"));
        p.sendMessage("Blue Spawn : " + (blueTeamSpawns.size() != 0 ? ChatColor.GREEN + "OK" : ChatColor.RED + "KO") + " (" + blueTeamSpawns.size() + ")");
        p.sendMessage("Red Spawn  : " + (redTeamSpawns.size() != 0 ? ChatColor.GREEN + "OK" : ChatColor.RED + "KO") + " (" + redTeamSpawns.size() + ")");
        p.sendMessage("Blue Goal  : " + (blueTeamGoalBlocks.size() != 0 ? ChatColor.GREEN + "OK" : ChatColor.RED + "KO") + " (" + blueTeamGoalBlocks.size() + ")");
        p.sendMessage("Red Goal   : " + (redTeamGoalBlocks.size() != 0 ? ChatColor.GREEN + "OK" : ChatColor.RED + "KO") + " (" + redTeamGoalBlocks.size() + ")");
        p.sendMessage("------------------");
        p.sendMessage("Next step : Use /cb team to generate team");
    }

    public void start(Player p) {
        if (matchState.equals(READY)) {
            if (!blueTeam.isEmpty() || !redTeam.isEmpty()) {
                startDelayedRound();
                matchTimer = matchDuration;
                matchState = IN_PROGRESS;

                p.sendMessage("[CubeBall] " + ChatColor.GREEN + "Match starting !");
                getAllPlayer().forEach(player -> {
                    player.sendMessage("[CubeBall] " + ChatColor.GREEN + "Match started ! Duration : " + ChatColor.GOLD + (matchTimer / 60) + ":" + (matchTimer - ((matchTimer / 60) * 60)));
                    player.sendMessage("[CubeBall] " + ChatColor.GREEN + "Max goals : " + (maxGoal == 0 ? "UNLIMITED" : maxGoal));
                });
            } else {
                p.sendMessage("[CubeBall] " + ChatColor.RED + "You need to add players to team (/cb team blue|red) <Player>");
            }
        } else {
            p.sendMessage("[CubeBall] " + ChatColor.RED + "Match is not ready or already started");
        }
    }

    private void startDelayedRound() {
        blueTeam.forEach(player -> {
            int randomIndex = rand.nextInt(blueTeamSpawns.size());
            player.teleport(blueTeamSpawns.get(randomIndex));
        });

        redTeam.forEach(player -> {
            int randomIndex = rand.nextInt(redTeamSpawns.size());
            player.teleport(redTeamSpawns.get(randomIndex));
        });

        getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> sendMessageToAllPlayer("3", "", 1), 20);
        getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> sendMessageToAllPlayer("2", "", 1), 40);
        getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> sendMessageToAllPlayer("1", "", 1), 60);
        getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            sendMessageToAllPlayer("GO !", "", 1);
            startRound();
        }, 80);
    }

    private void startRound() {
        matchState = matchTimer > 0 ? IN_PROGRESS : OVERTIME;
        removeBall();
        CubeBall.generateBall(BALL_MATCH_ID, ballSpawn, null);
    }

    public boolean addPlayerToTeam(Player p, Team team) {
        if (p != null) {
            if (team.equals(Team.BLUE)) {
                if (!blueTeam.contains(p)) {
                    blueTeam.add(p);
                }
                redTeam.remove(p);
                spectatorTeam.remove(p);
            } else if (team.equals(Team.RED)) {
                if (!redTeam.contains(p)) {
                    redTeam.add(p);
                }
                blueTeam.remove(p);
                spectatorTeam.remove(p);
            } else {
                if (!spectatorTeam.contains(p)) {
                    spectatorTeam.add(p);
                }
                blueTeam.remove(p);
                redTeam.remove(p);
            }
            return true;
        }
        return false;
    }

    public void checkGoal(Location ballLocation) {
        if (matchState.equals(IN_PROGRESS) || matchState.equals(OVERTIME)) {

            for (Location blockLocation : blueTeamGoalBlocks) {
                if (ballLocation.getBlockX() == blockLocation.getBlockX() &&
                        ballLocation.getBlockZ() == blockLocation.getBlockZ()) {
                    goal(Team.RED);
                    return;
                }
            }

            for (Location blockLocation : redTeamGoalBlocks) {
                if (ballLocation.getBlockX() == blockLocation.getBlockX() &&
                        ballLocation.getBlockZ() == blockLocation.getBlockZ()) {
                    goal(Team.BLUE);
                    return;
                }
            }
        }
    }

    private void goal(Team team) {
        removeBall();
        if (Team.BLUE.equals(team)) {
            blueScore++;
            triggerGoalAnimation(Team.BLUE);

        } else {
            redScore++;
            triggerGoalAnimation(Team.RED);
        }

        if (matchState.equals(IN_PROGRESS) && (maxGoal == 0 || (blueScore != maxGoal && redScore != maxGoal))) {
            sendScoreToPlayer();
            matchState = GOAL;
            getServer().getScheduler().scheduleSyncDelayedTask(plugin, this::startDelayedRound, 30 * 2);
        } else {
            matchState = GOAL;
            endMatch();
        }
    }

    public void endMatch() {
        String title;
        if (getBlueScore() > getRedScore()) {
            title = ChatColor.BLUE + "BLUE" + ChatColor.GOLD + " TEAM WIN !";
        } else if (getBlueScore() < getRedScore()) {
            title = ChatColor.RED + "RED" + ChatColor.GOLD + " TEAM WIN !";
        } else {
            title = ChatColor.GOLD + "OVERTIME !";
            setMatchState(OVERTIME);
        }

        String score = ChatColor.BLUE.toString() + getBlueScore() + ChatColor.WHITE + " - " + ChatColor.RED + getRedScore();
        sendMessageToAllPlayer(title, score, 3);
        if (!getMatchState().equals(OVERTIME)) {
            matchState = END;
            removeBall();
        }
    }

    public void sendScoreToPlayer() {
        String title = ChatColor.BLUE.toString() + blueScore + ChatColor.WHITE + " - " + ChatColor.RED + redScore;
        String subtitle = ChatColor.BOLD.toString() + ChatColor.GOLD + lastTouchPlayer.toUpperCase() + ChatColor.RESET + " GOALS ! "
                + "(" + computeSpeedGoal() + " km/h)";
        sendMessageToAllPlayer(title, subtitle, 3);
    }

    public double computeSpeedGoal() {

        Ball ball = balls.get(BALL_MATCH_ID);

        if (ball != null && ball.getBall() != null) {
            return Math.round((Math.abs((ball.getLastVelocity().getX())) + Math.abs((ball.getLastVelocity().getY())) + Math.abs((ball.getLastVelocity().getZ()))) * 100);
        }
        return 0;
    }

    public void sendMessageToAllPlayer(String title, String subtitle, int duration) {
        blueTeam.forEach(player -> {
            if (player != null) {
                player.sendTitle(title, subtitle, 1, duration * 20, 1);
                player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1, 3);
            }
        });

        redTeam.forEach(player -> {
            if (player != null) {
                player.sendTitle(title, subtitle, 1, duration * 20, 1);
                player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1, 3);
            }
        });

        spectatorTeam.forEach(player -> {
            if (player != null) {
                player.sendTitle(title, subtitle, 1, duration * 20, 1);
                player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1, 3);
            }
        });
    }

    public void triggerGoalAnimation(Team team) {
        if (team.equals(Team.BLUE)) {
            redTeamGoalBlocks.forEach(block -> {
                Objects.requireNonNull(block.getWorld()).spawnEntity(block.getBlock().getLocation(), EntityType.FIREWORK);
                Objects.requireNonNull(block.getWorld()).playEffect(block.getBlock().getLocation(), Effect.VILLAGER_PLANT_GROW, 3);
            });
        } else {
            blueTeamGoalBlocks.forEach(block -> {
                Objects.requireNonNull(block.getWorld()).spawnEntity(block.getBlock().getLocation(), EntityType.FIREWORK);
                Objects.requireNonNull(block.getWorld()).playEffect(block.getBlock().getLocation(), Effect.VILLAGER_PLANT_GROW, 3);
            });

        }
    }

    public void displayTeams(Player p) {
        p.sendMessage("BLUE TEAM : " + this.blueTeam.size() + " player(s)");
        this.blueTeam.forEach(player -> {
            if (player != null) {
                p.sendMessage("-" + ChatColor.BLUE + player.getDisplayName());
            }
        });

        p.sendMessage("RED TEAM : " + this.redTeam.size() + " player(s)");
        this.redTeam.forEach(player -> {
            if (player != null) {
                p.sendMessage("-" + ChatColor.RED + player.getDisplayName());
            }
        });

        p.sendMessage("SPECTATOR TEAM : " + this.spectatorTeam.size() + " player(s)");
        this.spectatorTeam.forEach(player -> {
            if (player != null) {
                p.sendMessage("-" + ChatColor.GREEN + player.getDisplayName());
            }
        });
    }

    public void setLastTouchPlayer(String lastTouchPlayer) {
        this.lastTouchPlayer = lastTouchPlayer;
    }

    public ArrayList<Player> getBlueTeam() {
        return blueTeam;
    }

    public ArrayList<Player> getRedTeam() {
        return redTeam;
    }

    public ArrayList<Player> getSpectatorTeam() {
        return spectatorTeam;
    }

    public ArrayList<Player> getAllPlayer() {
        ArrayList<Player> team = new ArrayList<>();
        team.addAll(getRedTeam());
        team.addAll(getBlueTeam());
        team.addAll(getSpectatorTeam());
        return team;
    }

    public int getBlueScore() {
        return blueScore;
    }

    public int getRedScore() {
        return redScore;
    }

    public MatchState getMatchState() {
        return matchState;
    }

    public void setMatchState(MatchState matchState) {
        this.matchState = matchState;
    }

    public void removeBall() {
        destroyBall(BALL_MATCH_ID);
    }

    public boolean pause() {
        if (matchState.equals(IN_PROGRESS) || matchState.equals(OVERTIME)) {
            matchState = PAUSED;
            removeBall();
            return true;
        }
        return false;
    }

    public boolean resume() {
        if (matchState.equals(PAUSED)) {
            startDelayedRound();
            return true;
        }
        return false;
    }
}
