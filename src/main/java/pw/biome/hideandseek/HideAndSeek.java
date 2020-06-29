package pw.biome.hideandseek;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import pw.biome.hideandseek.commands.HSCommands;
import pw.biome.hideandseek.listener.HideAndSeekListener;
import pw.biome.hideandseek.util.GameManager;

public final class HideAndSeek extends JavaPlugin {

    @Getter
    private static HideAndSeek instance;

    @Getter
    private GameManager gameManager;

    @Override
    public void onEnable() {
        instance = this;

        getCommand("hideandseek").setExecutor(new HSCommands());
        getServer().getPluginManager().registerEvents(new HideAndSeekListener(), this);

        saveDefaultConfig();

        gameManager = new GameManager();
        gameManager.setupGame();

        getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> gameManager.updateScoreboards(), (10 * 20), (10 * 20));
    }

    @Override
    public void onDisable() {
        gameManager.getTaskList().forEach(BukkitTask::cancel);
    }
}
