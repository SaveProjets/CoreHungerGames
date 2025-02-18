package fr.edminecoreteam.hungergames.listeners.connection;

import fr.edminecoreteam.api.EdmineAPI;
import fr.edminecoreteam.hungergames.Core;
import fr.edminecoreteam.hungergames.State;
import fr.edminecoreteam.hungergames.utils.game.GameUtils;
import fr.edminecoreteam.hungergames.listeners.content.waiting.WaitingListeners;
import fr.edminecoreteam.hungergames.content.waiting.tasks.AutoStart;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;

public class JoinEvent implements Listener
{
    private final static Core core = Core.getInstance();

    private final GameUtils gameUtils = new GameUtils();

    @EventHandler
    public void event(PlayerJoinEvent e)
    {
        Player p = e.getPlayer();
        e.setJoinMessage(null);
        EdmineAPI.getInstance().getBossBarBuilder().putPlayer(p);
        if (core.isState(State.WAITING) || core.isState(State.STARTING))
        {
            if (core.getPlayersInGame().size() == core.getMaxplayers())
            {
                p.kickPlayer("§cPartie pleine...");
                return;
            }

            if (!core.getPlayersInGame().contains(p.getName()))
            {
                core.getPlayersInGame().add(p.getName());
                core.getPlayersToSpawn().add(p);
                core.getServer().broadcastMessage("§e" + p.getName() + "§7 a rejoint le jeu. §d" + core.getServer().getOnlinePlayers().size() + "§d/" + core.getMaxplayers());

                p.teleport(gameUtils.getSpawn());
                get(p);
                p.playSound(p.getLocation(), Sound.NOTE_PLING, 0.5f, 1.0f);

                if (core.getConfig().getString("type").equalsIgnoreCase("ranked"))
                {
                    if (core.getPlayersInGame().size() == core.getMaxplayers())
                    {
                        core.setState(State.STARTING);
                        AutoStart start = new AutoStart(core);
                        start.runTaskTimer((Plugin) core, 0L, 20L);
                    }
                }
                else if (core.getConfig().getString("type").equalsIgnoreCase("unranked"))
                {
                    if (core.getPlayersInGame().size() == core.getConfig().getInt("needtostart"))
                    {
                        core.setState(State.STARTING);
                        AutoStart start = new AutoStart(core);
                        start.runTaskTimer((Plugin) core, 0L, 20L);
                    }
                }
            }
        }
        if (core.isState(State.INGAME) || core.isState(State.PREPARATION) || core.isState(State.NOPVP))
        {
        }
    }

    private void get(Player p) {
        PlayerInventory pi = p.getInventory();
        WaitingListeners waitingListeners = new WaitingListeners(p);

        pi.setHelmet(null);
        pi.setChestplate(null);
        pi.setLeggings(null);
        pi.setBoots(null);
        p.setAllowFlight(false);
        p.setFlying(false);

        p.getInventory().clear();
        p.setLevel(0);
        p.setFoodLevel(20);
        p.setHealth(20.0);
        p.setGameMode(GameMode.ADVENTURE);
        waitingListeners.getWaitingItems();
        if (core.getConfig().getString("type").equalsIgnoreCase("ranked"))
        {
            p.sendTitle("§e§lHungerGames", "§7Mode §6Compétitif§8.");
        }
        if (core.getConfig().getString("type").equalsIgnoreCase("unranked"))
        {
            p.sendTitle("§e§lHungerGames", "§7Mode §fNormal§8.");
        }
    }
}
