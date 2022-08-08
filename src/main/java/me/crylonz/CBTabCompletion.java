package me.crylonz;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CBTabCompletion implements TabCompleter {

    private List<String> list = new ArrayList<>();

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        list.clear();
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (cmd.getName().equalsIgnoreCase("cb") && player.hasPermission("cubeball.manage")) {

                if(args.length == 1) {
                    list.add("generate");
                    list.add("remove");
                    list.add("match");
                    list.add("start");
                    list.add("stop");
                    list.add("team");
                    list.add("pause");
                    list.add("resume");
                }
                if(args.length == 2) {
                    if(args[0].equalsIgnoreCase("team")) {
                        list.add("BLUE");
                        list.add("RED");
                        list.add("SPECTATOR");
                    }
                }
                if(args.length == 3) {
                    if(args[1].equalsIgnoreCase("BLUE") || args[1].equalsIgnoreCase("RED")) {
                        Bukkit.getOnlinePlayers().forEach( onlinePlayer -> {
                            list.add(onlinePlayer.getDisplayName());
                        });
                    }
                }
            }
        }
        return list;
    }
}