package pw.biome.hideandseek;

import co.aikar.commands.PaperCommandManager;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import pw.biome.hideandseek.commands.HSCommands;
import pw.biome.hideandseek.listener.HideAndSeekListener;
import pw.biome.hideandseek.objects.HSPlayer;
import pw.biome.hideandseek.util.GameManager;

public final class HideAndSeek extends JavaPlugin {

    @Getter
    private static HideAndSeek instance;

    @Getter
    private GameManager gameManager;

    @Override
    public void onEnable() {
        instance = this;
        gameManager = new GameManager();

        PaperCommandManager manager = new PaperCommandManager(instance);
        manager.registerDependency(GameManager.class, gameManager);
        manager.getCommandContexts().registerContext(HSPlayer.class, HSPlayer.getContextResolver());
        manager.registerCommand(new HSCommands());
        getServer().getPluginManager().registerEvents(new HideAndSeekListener(), this);

        saveDefaultConfig();
    }

    @Override
    public void onDisable() {
        gameManager.getTaskList().forEach(BukkitTask::cancel);
    }
}
