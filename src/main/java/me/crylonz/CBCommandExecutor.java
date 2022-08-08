package me.crylonz;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static me.crylonz.CubeBall.*;

public class CBCommandExecutor implements CommandExecutor {

    public CBCommandExecutor() {
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player;
        if ((sender instanceof Player)) {
            player = (Player) sender;

            if (cmd.getName().equalsIgnoreCase("cb")) {

                if (args.length == 1) {
                    if (args[0].equalsIgnoreCase("generate") && player.hasPermission("cubeball.manage")) {
                        if (ball == null) {
                            generateBall(player.getLocation());
                            player.sendMessage("[Cubeball] " + ChatColor.GREEN + "Ball generated !");
                        } else {
                            player.sendMessage("[Cubeball] " + ChatColor.RED + "Ball already exists !");
                        }
                    } else if (args[0].equalsIgnoreCase("remove") && player.hasPermission("cubeball.manage")) {
                        if (ball != null) {
                            ball.remove();
                        }
                        ball = null;
                        player.sendMessage("[Cubeball] " + ChatColor.GREEN + "Ball removed !");
                    } else if (args[0].equalsIgnoreCase("match") && player.hasPermission("cubeball.manage")) {
                        if (ball != null) {
                            ball.remove();
                        }
                        ball = null;
                        match = new Match();
                        match.scanSpawn(player);
                    } else if (args[0].equalsIgnoreCase("start") && player.hasPermission("cubeball.manage")) {
                        if (match != null) {
                            match.start(player);
                        } else {
                            player.sendMessage("[Cubeball] " + ChatColor.RED + "You need te create a match before (/cb match) !");
                        }
                    } else if (args[0].equalsIgnoreCase("stop") && player.hasPermission("cubeball.manage")) {
                        if (match != null) {

                            if (ball != null) {
                                ball.remove();
                            }
                            match = null;
                            player.sendMessage("[Cubeball] " + ChatColor.GREEN + "Match cancelled ! To create a new match do /cb match");
                        } else {
                            player.sendMessage("[Cubeball] " + ChatColor.RED + "No match to stop");
                        }
                    } else if (args[0].equalsIgnoreCase("pause") && player.hasPermission("cubeball.manage")) {
                        if (match != null && match.pause()) {
                            for (Player p : match.getAllPlayer()) {
                                if (p != null) {
                                    p.sendMessage("[Cubeball] " + ChatColor.RED + "Match PAUSED by " + ChatColor.GOLD + player.getName());
                                }
                            }
                        } else {
                            player.sendMessage("[Cubeball] " + ChatColor.RED + "No match to pause");
                        }
                    } else if (args[0].equalsIgnoreCase("resume") && player.hasPermission("cubeball.manage")) {
                        if (match != null && match.resume()) {
                            for (Player p : match.getAllPlayer()) {
                                if (p != null) {
                                    p.sendMessage("[Cubeball] " + ChatColor.RED + "Match RESUMED by " + ChatColor.GOLD + player.getName());
                                }
                            }
                        } else {
                            player.sendMessage("[Cubeball] " + ChatColor.RED + "No match to resume");
                        }
                    }
                }
            }
            if (args.length == 3) {
                if (args[0].equalsIgnoreCase("team") && player.hasPermission("cubeball.manage")) {
                    if (match != null) {
                        try {
                            Team team = Team.valueOf(args[1].toUpperCase());
                            Player playerToAdd = Bukkit.getPlayer(args[2]);
                            if (match.addPlayerToTeam(playerToAdd, team)) {
                                player.sendMessage("[Cubeball] " + ChatColor.GREEN + "Player added to " + args[1].toUpperCase() + " team !");
                                playerToAdd.sendMessage("[Cubeball] " + ChatColor.GREEN + "Your are in the " + args[1].toUpperCase() + " team !");
                                match.displayTeams(player);
                            } else {
                                player.sendMessage("[Cubeball] " + ChatColor.RED + "Cannot find this player !");
                            }
                        } catch (IllegalArgumentException | NullPointerException e) {
                            player.sendMessage("[Cubeball] " + ChatColor.RED + "Team must be RED or BLUE or SPECTATOR");
                        }
                    } else {
                        player.sendMessage("[Cubeball] " + ChatColor.RED + "You need te create a match before (/cb match) !");
                    }
                }
            }
        }
        return true;
    }
}
