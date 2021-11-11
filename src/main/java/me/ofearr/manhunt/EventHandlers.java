package me.ofearr.manhunt;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;

public class EventHandlers implements Listener {

    private static Manhunt plugin;

    public EventHandlers(Manhunt manhunt){
        this.plugin = manhunt;
    }

    public static String TranslateColour(String text){

        String translated = ChatColor.translateAlternateColorCodes('&', text);

        return translated;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void EventPVP(EntityDamageByEntityEvent e){
        if(!plugin.activeListeners) return;
        if(!(e.getDamager() instanceof Player)) return;
        if(!(e.getEntity() instanceof Player)) return;
        Player damager = (Player) e.getDamager();
        if(plugin.activeGrace){
            e.setCancelled(true);
            damager.sendMessage((TranslateColour("&8[&b&lManhut&8] >> &You cannot attack other players while the grace period is active!")));
            return;
        }
        Player attacked = (Player) e.getEntity();

        if(Manhunt.playerRoles.get(damager.getUniqueId()) == "HUNTER" && Manhunt.playerRoles.get(attacked.getUniqueId()) == "HUNTER" && plugin.getConfig().getString("Settings.allow-team-pvp") == "false"){
            damager.sendMessage(TranslateColour(plugin.getConfig().getString("Settings.team-pvp-deny-message")));
            e.setCancelled(true);
        }

        if(Manhunt.playerRoles.get(damager.getUniqueId()) == "RUNNER" && Manhunt.playerRoles.get(attacked.getUniqueId()) == "RUNNER" && plugin.getConfig().getString("Settings.allow-team-pvp") == "false"){
            damager.sendMessage(TranslateColour(plugin.getConfig().getString("Settings.team-pvp-deny-message")));
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void EventPortalTravel(PlayerPortalEvent e){
        if(!plugin.activeListeners) return;
        Location entryPortal = e.getPlayer().getLocation();
        Player player = e.getPlayer();

        if(entryPortal.getWorld().getName().equalsIgnoreCase(plugin.getConfig().getString("Settings.over-world-name"))){
            plugin.playerPortalStatus.put(player.getUniqueId(), entryPortal);
        } else if(entryPortal.getWorld().getName().equalsIgnoreCase(plugin.getConfig().getString("Settings.nether-world-name"))){
            plugin.playerPortalStatus.remove(player.getUniqueId());
        }
    }

    //New death handling system, likely much cleaner than before
    @EventHandler(priority = EventPriority.HIGHEST)
    public void EventPlayerDeath(PlayerDeathEvent e){
        if(!plugin.activeListeners) return;
        Player player = e.getEntity();
        if(plugin.playerRoles.get(player.getUniqueId()) == "RUNNER"){
            for(Player p : Bukkit.getOnlinePlayers()){
                p.sendMessage(TranslateColour(plugin.getConfig().getString("Settings.runner-death-message")).replace("<player>", player.getName()));
            }
            e.setCancelled(true);
            player.setGameMode(GameMode.SPECTATOR);
            plugin.playerRoles.remove(player.getUniqueId());
            if(!(plugin.playerRoles.containsValue("RUNNER"))){
                for(Player p : Bukkit.getOnlinePlayers()){
                    p.sendTitle(TranslateColour("&c&lGame Over!"), TranslateColour("&aThere are no runners left!"));
                    p.sendMessage(TranslateColour("&8[&b&lManhut&8] >> &aThe game has ended since all the runners died!"));
                    plugin.playerRoles.clear();
                    plugin.runners.clear();
                    plugin.playerPortalStatus.clear();
                    plugin.deadHunters.clear();
                    plugin.activeGrace = false;
                }
            }
        }
        else if(plugin.playerRoles.get(player.getUniqueId()) == "HUNTER"){
            for(Player p : Bukkit.getOnlinePlayers()){
                p.sendMessage(TranslateColour(plugin.getConfig().getString("Settings.hunter-death-message")).replace("<player>", player.getName()));
            }
            player.getInventory().clear();
            e.setCancelled(true);
            player.setGameMode(GameMode.SPECTATOR);
            plugin.deadHunters.add(player.getUniqueId());

            new BukkitRunnable(){

                int respawnTimer = plugin.getConfig().getInt("Settings.hunter-respawn-cooldown");

                @Override
                public void run(){

                    if (respawnTimer == 0) {
                        this.cancel();

                        player.sendTitle(TranslateColour("&c&lRespawning!"), null);

                        String spawnCoords = plugin.getConfig().getString("Settings.spawn-location");
                        ArrayList<String> splitCoords = new ArrayList<>(Arrays.asList(spawnCoords.split(", ")));

                        double X = Double.valueOf(splitCoords.get(0));
                        double Y = Double.valueOf(splitCoords.get(1));
                        double Z = Double.valueOf(splitCoords.get(2));

                        World world = Bukkit.getWorld(plugin.getConfig().getString("Settings.over-world-name"));
                        Location spawnLoc = new Location(world, X, Y, Z);

                        player.teleport(spawnLoc);
                        player.setGameMode(GameMode.SURVIVAL);
                        plugin.deadHunters.remove(player.getUniqueId());

                        ItemStack trackingCompass = new ItemStack(Material.COMPASS);

                        ItemMeta compassMeta = trackingCompass.getItemMeta();
                        compassMeta.setDisplayName(TranslateColour("&c&lTracking Compass"));
                        ArrayList<String> lore = new ArrayList<>();

                        lore.add(" ");
                        lore.add(TranslateColour("&6Item Ability: Path Finder &c&lRIGHT CLICK"));
                        lore.add(TranslateColour("&7Points your compass to the"));
                        lore.add(TranslateColour("&7closest runner/portal entry."));
                        lore.add("");
                        lore.add(TranslateColour("&8Cooldown: " + plugin.getConfig().getInt("Settings.player-tracker-cooldown") + "s"));

                        compassMeta.setLore(lore);
                        trackingCompass.setItemMeta(compassMeta);

                        player.getInventory().addItem(trackingCompass);

                    } else {
                        player.sendTitle(TranslateColour("&c&lRespawning!"), TranslateColour( "&cRespawning in: " + respawnTimer + "s"));
                    }
                    respawnTimer--;
                }
            }.runTaskTimer(plugin, 0, 20L);
        }

    }

    //Prevents movement of dead hunters to prevent cheating
    @EventHandler(priority = EventPriority.HIGHEST)
    public void EventGhostHunterMove(PlayerMoveEvent e){
        if(!plugin.activeListeners) return;
        Player player = e.getPlayer();

        if(plugin.deadHunters.contains(player.getUniqueId())){
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void EventDragonKilled(EntityDeathEvent e){
        if(!plugin.activeListeners) return;
        Entity entity = e.getEntity();
        if(!(entity.getType() == EntityType.ENDER_DRAGON)) return;
        if(!(e.getEntity().getKiller() instanceof Player)) return;

        Player player = e.getEntity().getKiller();

        for(Player p : Bukkit.getOnlinePlayers()){
            p.sendTitle(TranslateColour("&c&lGame Over!"), TranslateColour("&aThe dragon was killed by " + player.getName() + "!"));
            plugin.playerRoles.clear();
            plugin.runners.clear();
            plugin.playerPortalStatus.clear();
            plugin.deadHunters.clear();
            plugin.activeGrace = false;

        }



    }

}
