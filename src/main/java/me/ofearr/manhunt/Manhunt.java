package me.ofearr.manhunt;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public final class Manhunt extends JavaPlugin {

    public static String TranslateColour(String text){

        String translated = ChatColor.translateAlternateColorCodes('&', text);

        return translated;
    }
    public static boolean activeGrace;
    public static boolean activeListeners;

    public static HashMap<UUID, String> playerRoles = new HashMap<>();
    public static ArrayList<UUID> runners = new ArrayList<>();
    public static HashMap<UUID, Location> playerPortalStatus = new HashMap<>();
    public static ArrayList<UUID> deadHunters = new ArrayList<>();

    @Override
    public void onEnable() {
        activeGrace = false;
        activeListeners = false;
        loadConfig();

        Bukkit.getPluginManager().registerEvents(new EventHandlers(this), this);
        Bukkit.getPluginManager().registerEvents(new CompassHandler(this), this);
    }

    @Override
    public void onDisable() {
        playerRoles.clear();
        runners.clear();
        playerPortalStatus.clear();
        deadHunters.clear();
    }

    public void loadConfig(){
        saveDefaultConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().toLowerCase().equals("setrunner")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (!(player.hasPermission("manhunt.assign"))) {
                    player.sendMessage(ChatColor.RED + "Insufficient permissions!");
                } else {
                    if(args.length == 0){
                        player.sendMessage(TranslateColour("&cYou must provide a player name to set as a runner!"));
                    } else{
                        if(!(Bukkit.getServer().getPlayer(args[0]) == null) && args[0] != null){
                            Player target = Bukkit.getPlayer(args[0]);
                            playerRoles.put(target.getUniqueId(), "RUNNER");
                            runners.add(target.getUniqueId());
                            player.sendMessage(TranslateColour("&aSuccessfully set " + target.getName() + " as a runner!"));
                        } else if(Bukkit.getServer().getPlayer(args[0])  == null) {
                            player.sendMessage(TranslateColour("&cThat player is not online!"));
                        }
                    }
                }
            }
        }

        if (command.getName().toLowerCase().equals("startgame")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (!(player.hasPermission("manhunt.start"))) {
                    player.sendMessage("&cInsufficent permissions!");
                } else {
                    for(Player p : Bukkit.getOnlinePlayers()){
                        if(!(playerRoles.get(p.getUniqueId()) == "RUNNER")){
                            playerRoles.put(p.getUniqueId(), "HUNTER");
                        }
                    }
                    for(Player p : Bukkit.getOnlinePlayers()){
                        if(playerRoles.get(p.getUniqueId()) == "RUNNER"){
                            p.sendTitle(TranslateColour("&b&lRunner"), TranslateColour("&aYou're a Runner!"));
                        } else {
                            p.sendTitle(TranslateColour("&c&lHunter"), TranslateColour("&aYou're a Hunter!"));

                            ItemStack trackingCompass = new ItemStack(Material.COMPASS);

                            ItemMeta compassMeta = trackingCompass.getItemMeta();
                            compassMeta.setDisplayName(TranslateColour("&c&lTracking Compass"));
                            ArrayList<String> lore = new ArrayList<>();

                            lore.add(" ");
                            lore.add(TranslateColour("&6Item Ability: Path Finder &c&lRIGHT CLICK"));
                            lore.add(TranslateColour("&7Points your compass to the"));
                            lore.add(TranslateColour("&7closest runner/portal entry."));
                            lore.add("");
                            lore.add(TranslateColour("&8Cooldown: " + getConfig().getInt("Settings.player-tracker-cooldown") + "s"));

                            compassMeta.setLore(lore);
                            trackingCompass.setItemMeta(compassMeta);
                            p.getInventory().addItem(trackingCompass);


                        }

                        p.sendMessage(TranslateColour("&8[&b&lManhunt&8] >> &aThe game has been started by " + sender.getName() + "!"));
                    }

                    activeGrace = true;
                    activeListeners = true;

                    new BukkitRunnable(){

                        int graceTimer = getConfig().getInt("Settings.grace-period");

                        @Override
                        public void run(){

                            if(graceTimer == 600){
                                for(Player p : Bukkit.getOnlinePlayers()){
                                    p.sendTitle(TranslateColour("&6&lGrace Period!"), TranslateColour("&aThe grace period will end in 10 minutes!"));
                                    p.sendMessage(TranslateColour("&8[&b&lManhunt&8] >> &aThe grace period will end in 10 minutes!"));
                                }
                            }

                            if(graceTimer == 300){
                                for(Player p : Bukkit.getOnlinePlayers()){
                                    p.sendTitle(TranslateColour("&6&lGrace Period!"), TranslateColour("&aThe grace period will end in 5 minutes!"));
                                    p.sendMessage(TranslateColour("&8[&b&lManhunt&8] >> &aThe grace period will end in 5 minutes!"));
                                }
                            }

                            if(graceTimer == 60){
                                for(Player p : Bukkit.getOnlinePlayers()){
                                    p.sendTitle(TranslateColour("&6&lGrace Period!"), TranslateColour("&aThe grace period will end in 1 minute!"));
                                    p.sendMessage(TranslateColour("&8[&b&lManhunt&8] >> &aThe grace period will end in 1 minute!"));
                                }
                            }

                            if(graceTimer == 10){
                                for(Player p : Bukkit.getOnlinePlayers()){
                                    p.sendTitle(TranslateColour("&6&lGrace Period!"), TranslateColour("&aThe grace period will end in 10 seconds!"));
                                    p.sendMessage(TranslateColour("&8[&b&lManhunt&8] >> &aThe grace period will end in 10 seconds!"));
                                }
                            }

                            if(graceTimer == 5){
                                for(Player p : Bukkit.getOnlinePlayers()){
                                    p.sendTitle(TranslateColour("&6&lGrace Period!"), TranslateColour("&aThe grace period will end in 5 seconds!"));
                                    p.sendMessage(TranslateColour("&8[&b&lManhunt&8] >> &aThe grace period will end in 5 seconds!"));
                                }
                            }

                            if(graceTimer == 4){
                                for(Player p : Bukkit.getOnlinePlayers()){
                                    p.sendTitle(TranslateColour("&6&lGrace Period!"), TranslateColour("&aThe grace period will end in 4 seconds!"));
                                    p.sendMessage(TranslateColour("&8[&b&lManhunt&8] >> &aThe grace period will end in 4 seconds!"));
                                }
                            }

                            if(graceTimer == 3){
                                for(Player p : Bukkit.getOnlinePlayers()){
                                    p.sendTitle(TranslateColour("&6&lGrace Period!"), TranslateColour("&aThe grace period will end in 3 seconds!"));
                                    p.sendMessage(TranslateColour("&8[&b&lManhunt&8] >> &aThe grace period will end in 3 seconds!"));
                                }
                            }

                            if(graceTimer == 2){
                                for(Player p : Bukkit.getOnlinePlayers()){
                                    p.sendTitle(TranslateColour("&6&lGrace Period!"), TranslateColour("&aThe grace period will end in 2 seconds!"));
                                    p.sendMessage(TranslateColour("&8[&b&lManhunt&8] >> &aThe grace period will end in 2 seconds!"));
                                }
                            }

                            if(graceTimer == 1){
                                for(Player p : Bukkit.getOnlinePlayers()){
                                    p.sendTitle(TranslateColour("&6&lGrace Period!"), TranslateColour("&aThe grace period will end in 1 second!"));
                                    p.sendMessage(TranslateColour("&8[&b&lManhunt&8] >> &aThe grace period will end in 1 second!"));
                                }
                            }

                            if (graceTimer <= 0) {
                                for(Player p : Bukkit.getOnlinePlayers()){
                                    p.sendTitle(TranslateColour("&6&lGrace Ended!"), TranslateColour("&aThe grace period has ended!"));
                                    p.sendMessage(TranslateColour("&8[&b&lManhunt&8] >> &aThe grace period has ended! Be careful!"));
                                    this.cancel();
                                }
                                activeGrace = false;
                            }
                            graceTimer--;
                        }
                    }.runTaskTimer(this, 0, 20L);

                }
            }
        }

        if (command.getName().toLowerCase().equals("endgame")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (!(player.hasPermission("manhunt.end"))) {
                    player.sendMessage(ChatColor.RED + "Insufficient permissions!");
                } else {
                    for(Player p : Bukkit.getOnlinePlayers()){
                        p.sendMessage(TranslateColour("&8[&b&lManhunt&8] >> &aThe game has been ended by " + sender.getName() + "!"));
                        playerRoles.clear();
                        runners.clear();
                        playerPortalStatus.clear();
                        deadHunters.clear();
                        activeGrace = false;
                    }
                }
            }
        }
        return false;
    }

}
