package pw.biome.hideandseek.objects;

import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.contexts.ContextResolver;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import pw.biome.hideandseek.HideAndSeek;
import pw.biome.hideandseek.util.GameManager;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class HSPlayer {

    @Getter
    private static final ConcurrentHashMap<UUID, HSPlayer> hsPlayerMap = new ConcurrentHashMap<>();

    @Getter
    private final UUID uuid;

    @Getter
    private final String name;

    @Getter
    private HSTeam currentTeam;

    @Getter
    private boolean exempt;

    @Getter
    private int leaveTaskId;

    public HSPlayer(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;

        if (HideAndSeek.getInstance().getGameManager().getExemptPlayers().contains(uuid)) {
            this.exempt = true;
        }

        hsPlayerMap.put(uuid, this);
    }

    public static ContextResolver<HSPlayer, BukkitCommandExecutionContext> getContextResolver() {
        return (c) -> {
            Player player = c.getPlayer();
            return HSPlayer.getOrCreate(player.getUniqueId(), player.getDisplayName());
        };
    }

    public void setExempt(boolean exempt) {
        this.exempt = exempt;

        getCurrentTeam().removePlayer(this);

        GameManager gameManager = HideAndSeek.getInstance().getGameManager();

        if (exempt) {
            gameManager.getExemptPlayers().add(uuid);
            setCurrentTeam(null, false);
        } else {
            gameManager.getExemptPlayers().remove(uuid);
            setCurrentTeam(gameManager.getSeekers(), false);
        }
    }

    public void setCurrentTeam(HSTeam newTeam, boolean triggerUpdate) {
        if (currentTeam != null) currentTeam.removePlayer(this);

        if (newTeam == null) {
            this.currentTeam = null;
            return;
        }

        newTeam.addPlayer(this);

        this.currentTeam = newTeam;

        Bukkit.broadcastMessage(ChatColor.AQUA + name + " is now a " + ChatColor.GOLD + newTeam.getName());

        if (triggerUpdate) {
            // Calculate winner after every team change
            HideAndSeek.getInstance().getGameManager().calculateWinner();
        }
    }

    public static HSPlayer getOrCreate(UUID uuid, String displayName) {
        if (hsPlayerMap.containsKey(uuid)) {
            return hsPlayerMap.get(uuid);
        }
        return new HSPlayer(uuid, displayName);
    }

    public static HSPlayer getExact(UUID uuid) {
        return hsPlayerMap.get(uuid);
    }

    public void startLeaveTask() {
        leaveTaskId = Bukkit.getScheduler().runTaskLater(HideAndSeek.getInstance(), () -> {
            setCurrentTeam(HideAndSeek.getInstance().getGameManager().getSeekers(), true); // Add to seeker if they have been logged out for >10min
        }, 10 * (60 * 20)).getTaskId();
    }

    public void cancelLeaveTask() {
        if (leaveTaskId != 0) {
            Bukkit.getScheduler().cancelTask(leaveTaskId);
        }
    }
}
