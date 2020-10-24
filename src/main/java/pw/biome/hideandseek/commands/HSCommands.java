package pw.biome.hideandseek.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import pw.biome.hideandseek.objects.HSPlayer;
import pw.biome.hideandseek.util.GameManager;
import pw.biome.hideandseek.util.TeamType;

@CommandAlias("hideandseek|hs")
@Description("HideAndSeek commands")
public class HSCommands extends BaseCommand {

    @Dependency
    private GameManager gameManager;

    @Default
    @Description("Shows the available commands")
    public void defaultCommand(CommandSender sender) {
        sender.sendMessage(ChatColor.DARK_AQUA + "HideAndSeek Commands:");
        sender.sendMessage(ChatColor.AQUA + "/hideandseek list - Shows a list of whos on what team");
    }

    @Subcommand("create")
    @CommandPermission("hideandseek.admin")
    @Description("Creates a HideAndSeek game")
    public void create(CommandSender sender) {
        if (!gameManager.isGameRunning()) {
            gameManager.createGame();
            sender.sendMessage(ChatColor.GREEN + "Created a HideAndSeek game!");
        } else {
            sender.sendMessage(ChatColor.RED + "A game is already running!");
        }
    }

    @Subcommand("cancel")
    @CommandPermission("hideandseek.admin")
    @Description("Cancels a HideAndSeek game")
    public void cancel(CommandSender sender) {
        if (gameManager.isGameRunning()) {
            gameManager.finishGame();
            sender.sendMessage(ChatColor.GREEN + "Cancelled the current HideAndSeek game!");
        } else {
            sender.sendMessage(ChatColor.RED + "A game isn't running!");
        }
    }

    @Subcommand("list")
    @CommandPermission("hideandseek.admin")
    @Description("Lists the status of the players")
    public void list(CommandSender sender) {
        if (gameManager.isGameRunning()) {
            sender.sendMessage(ChatColor.DARK_RED + "Seekers:");
            gameManager.getSeekers().getMembers().forEach(hsPlayer ->
                    sender.sendMessage(ChatColor.RED + "- " + hsPlayer.getName()));

            sender.sendMessage(ChatColor.GOLD + "Hiders:");
            gameManager.getHiders().getMembers().forEach(hsPlayer ->
                    sender.sendMessage(ChatColor.GOLD + "- " + hsPlayer.getName()));
        }
    }

    @Subcommand("rebuild")
    @CommandPermission("hideandseek.admin")
    @Description("Rebuilds the cache")
    public void rebuild(CommandSender sender) {
        HSPlayer.getHsPlayerMap().clear();
        Bukkit.getOnlinePlayers().forEach(player -> {
            HSPlayer hsPlayer = HSPlayer.getOrCreate(player.getUniqueId(), player.getDisplayName());
        });
        sender.sendMessage(ChatColor.GREEN + "Cache rebuilt");
    }

    @Subcommand("exempt")
    @CommandPermission("hideandseek.admin")
    @Description("Exempts a player from the current game")
    public void exempt(CommandSender sender, HSPlayer hsPlayer) {
        if (!hsPlayer.isExempt()) {
            hsPlayer.setExempt(true);
            sender.sendMessage(ChatColor.GREEN + "'" + hsPlayer.getName() + "' is now exempt!");
        }
    }

    @Subcommand("force")
    @CommandPermission("hideandseek.admin")
    @Description("Forces a player to the given team in the current game")
    public void force(CommandSender sender, HSPlayer hsPlayer, String teamName) {
        TeamType forceTeam = TeamType.valueOf(teamName);

        if (forceTeam == TeamType.HIDER) {
            hsPlayer.setCurrentTeam(gameManager.getHiders(), true);
            sender.sendMessage(ChatColor.GREEN + "Forced '" + hsPlayer.getName() + "' to " + ChatColor.GREEN + "HIDERS");
        } else if (forceTeam == TeamType.SEEKER) {
            hsPlayer.setCurrentTeam(gameManager.getSeekers(), true);
            sender.sendMessage(ChatColor.GREEN + "Forced '" + hsPlayer.getName() + "' to " + ChatColor.RED + "SEEKERS");
        } else {
            hsPlayer.setCurrentTeam(null, false);
            sender.sendMessage(ChatColor.GREEN + "Forced '" + hsPlayer.getName() + "' to " + ChatColor.RED + "null");
        }
    }
}