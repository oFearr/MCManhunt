package me.ofearr.manhunt;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class CompassHandler implements Listener {

    public static String TranslateColour(String text){

        String translated = ChatColor.translateAlternateColorCodes('&', text);

        return translated;
    }

    private Manhunt plugin = Manhunt.plugin;

    private int cooldownTime = plugin.getConfig().getInt("Settings.player-tracker-cooldown");
    private HashMap<UUID, Long> cooldownTracker = new HashMap<>();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void EventCompassUse(PlayerInteractEvent e){
        if(plugin.activeListeners == false) return;
        if(e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR){
            Player player = e.getPlayer();

            ItemStack heldItem = e.getPlayer().getInventory().getItemInMainHand();

            if(heldItem == null){
                return;
            }
            if(!(heldItem.hasItemMeta())){
                return;
            }
            if(!(heldItem.getItemMeta().hasLore())){
                return;
            }

            if(heldItem.getItemMeta().getDisplayName().equalsIgnoreCase(TranslateColour("&c&lTracking Compass"))){

                if(cooldownTracker.containsKey(player.getUniqueId())){
                    Long timeLeft = ((cooldownTracker.get(player.getUniqueId()) / 100) + cooldownTime) - (System.currentTimeMillis() / 1000);
                    if(timeLeft > 0){
                        player.sendMessage(TranslateColour("&cThis item is currently on cooldown for " + timeLeft + "s!"));
                    }
                } else {
                    cooldownTracker.put(player.getUniqueId(), System.currentTimeMillis());
                }

                ArrayList<Location> playerLocations = new ArrayList<>();

                for (int i = 0; i < plugin.runners.size(); i++){
                    Player target = Bukkit.getPlayer(plugin.runners.get(i));
                    if(target.getWorld().getName().equalsIgnoreCase(plugin.getConfig().getString("Settings.over-world-name"))){
                        Location loc = target.getLocation();
                        playerLocations.add(loc);
                    } else if(target.getWorld().getName().equalsIgnoreCase(plugin.getConfig().getString("Settings.nether-world-name"))){
                        Location loc = plugin.playerPortalStatus.get(target.getUniqueId());
                        playerLocations.add(loc);

                    }

                }


                Location closestLoc = player.getLocation();
                Double currentLocationDistance = 99999.0;

                for(int i = 0; i < playerLocations.size(); i++){
                    Location currentLoc = player.getLocation();
                    Location targetLoc = playerLocations.get(i);

                    if(currentLoc.distance(targetLoc) < currentLocationDistance){
                        closestLoc = targetLoc;
                        currentLocationDistance = currentLoc.distance(targetLoc);
                    }

                }

                if(closestLoc == null) player.sendMessage(TranslateColour("&8[&b&lManhut&8] >> &cFailed to find a player location!"));

                player.setCompassTarget(closestLoc);

            }
        }
    }
}
