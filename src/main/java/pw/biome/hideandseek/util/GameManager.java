package pw.biome.hideandseek.util;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import pro.husk.ichat.iChat;
import pro.husk.ichat.obj.PlayerCache;
import pw.biome.hideandseek.HideAndSeek;
import pw.biome.hideandseek.objects.HSPlayer;
import pw.biome.hideandseek.objects.HSTeam;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class GameManager {

    @Getter
    private final ThreadLocalRandom random = ThreadLocalRandom.current();

    @Getter
    private HSTeam hiders;

    @Getter
    private HSTeam seekers;

    @Getter
    @Setter
    private boolean canHiderJoin;

    @Getter
    private boolean gameRunning;

    @Getter
    private int gameLength;

    @Getter
    private final List<BukkitTask> taskList = new ArrayList<>();

    @Getter
    private final List<UUID> exemptPlayers = new ArrayList<>();

    public void setupGame() {
        this.hiders = new HSTeam("Hider", TeamType.HIDER);
        this.seekers = new HSTeam("Seeker", TeamType.SEEKER);

        gameLength = HideAndSeek.getInstance().getConfig().getInt("game-length");

        // Load exempt users
        HideAndSeek.getInstance().getConfig().getStringList("exempt-players").forEach(uuidString -> {
            UUID uuid = UUID.fromString(uuidString);
            exemptPlayers.add(uuid);
        });
    }

    public void createGame() {
        if (gameRunning) return;
        ImmutableList<Player> onlinePlayers = ImmutableList.copyOf(Bukkit.getOnlinePlayers());
        Player randomPlayer = onlinePlayers.get(random.nextInt(onlinePlayers.size()));
        HSPlayer randomHsPlayer = HSPlayer.getExact(randomPlayer.getUniqueId());

        randomHsPlayer.setCurrentTeam(seekers, false);

        onlinePlayers.forEach(player -> {
            HSPlayer hsPlayer = HSPlayer.getExact(player.getUniqueId());
            if (hsPlayer.getCurrentTeam() == null && !hsPlayer.isExempt()) hsPlayer.setCurrentTeam(hiders, false);
        });

        canHiderJoin = true;
        gameRunning = true;

        taskList.add(Bukkit.getScheduler().runTaskLater(HideAndSeek.getInstance(), () -> canHiderJoin = false, (gameLength / 4) * 60 * 60 * 20));
        taskList.add(Bukkit.getScheduler().runTaskLater(HideAndSeek.getInstance(), () -> gameRunning = false, gameLength * 60 * 60 * 20));
        taskList.add(Bukkit.getScheduler().runTaskLater(HideAndSeek.getInstance(), this::calculateWinner, (gameLength * 60 * 60 * 20) - 1));
    }

    public void calculateWinner() {
        int hiderSize = hiders.getMembers().size();

        if (hiderSize == 0) {
            Bukkit.broadcastMessage(ChatColor.DARK_RED + "The seekers have won!");
            finishGame();
        }
    }

    /**
     * Clean up data from the previous game
     */
    public void finishGame() {
        hiders.getMembers().clear();
        seekers.getMembers().clear();

        HSPlayer.getHsPlayerMap().values().forEach(hsPlayer -> hsPlayer.setCurrentTeam(null, false));

        this.gameRunning = false;
        this.canHiderJoin = false;

        taskList.forEach(bukkitTask -> {
            if (!bukkitTask.isCancelled()) bukkitTask.cancel();
        });

        iChat.getPlugin().restartScoreboardTask();

        saveExemptList();
    }

    public void updateScoreboards() {
        if (gameRunning) {
            // We want to overwrite the updateScoreboard of iChat
            iChat.getPlugin().stopScoreboardTask();

            Bukkit.getScheduler().runTaskAsynchronously(HideAndSeek.getInstance(), () -> {
                ImmutableList<Player> onlinePlayers = ImmutableList.copyOf(Bukkit.getServer().getOnlinePlayers());
                onlinePlayers.forEach(player -> {
                    HSPlayer hsPlayer = HSPlayer.getExact(player.getUniqueId());
                    PlayerCache playerCache = PlayerCache.getFromUUID(player.getUniqueId());

                    ChatColor prefix = playerCache.getRank().getPrefix();

                    boolean afk = playerCache.isAFK();

                    if (afk) {
                        prefix = ChatColor.GRAY;
                    }

                    switch (hsPlayer.getCurrentTeam().getTeamType()) {
                        case SEEKER:
                            player.setPlayerListName(ChatColor.DARK_RED + player.getDisplayName());
                        case HIDER:
                            player.setPlayerListName(ChatColor.GOLD + player.getDisplayName());
                        default:
                            player.setPlayerListName(prefix + player.getDisplayName());
                    }
                });
            });
        }
    }

    private void saveExemptList() {
        List<String> uuidStringList = new ArrayList<>();
        exemptPlayers.forEach(uuid -> uuidStringList.add(uuid.toString()));

        HideAndSeek.getInstance().getConfig().set("exempt-players", uuidStringList);
        HideAndSeek.getInstance().saveConfig();
    }
}
