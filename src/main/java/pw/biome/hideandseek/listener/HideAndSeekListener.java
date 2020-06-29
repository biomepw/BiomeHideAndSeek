package pw.biome.hideandseek.listener;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import pw.biome.hideandseek.HideAndSeek;
import pw.biome.hideandseek.objects.HSPlayer;
import pw.biome.hideandseek.util.TeamType;

public class HideAndSeekListener implements Listener {

    @EventHandler
    public void loadPlayerCache(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        HSPlayer hsPlayer = HSPlayer.getOrCreate(player.getUniqueId(), player.getDisplayName());

        if (HideAndSeek.getInstance().getGameManager().isGameRunning()) {
            if (!hsPlayer.isExempt() && hsPlayer.getCurrentTeam() == null) {
                if (HideAndSeek.getInstance().getGameManager().isCanHiderJoin()) {
                    hsPlayer.setCurrentTeam(HideAndSeek.getInstance().getGameManager().getHiders(), true);
                }
            }
        }
    }

    @EventHandler
    public void onTag(EntityDamageByEntityEvent event) {
        if (!HideAndSeek.getInstance().getGameManager().isGameRunning()) return;

        Entity victimEntity = event.getEntity();
        Entity damagerEntity = event.getDamager();

        if (victimEntity instanceof Player && damagerEntity instanceof Player) {
            Player victim = (Player) victimEntity;
            Player damager = (Player) damagerEntity;

            HSPlayer victimHsPlayer = HSPlayer.getExact(victim.getUniqueId());
            HSPlayer damagerHsPlayer = HSPlayer.getExact(damager.getUniqueId());

            if (damagerHsPlayer.getCurrentTeam().getTeamType() == TeamType.SEEKER &&
                    victimHsPlayer.getCurrentTeam().getTeamType() == TeamType.HIDER) {
                victimHsPlayer.setCurrentTeam(damagerHsPlayer.getCurrentTeam(), true);
            }
        }
    }

    @EventHandler
    public void startLeaveTaskOnQuit(PlayerQuitEvent event) {
        if (!HideAndSeek.getInstance().getGameManager().isGameRunning()) return;

        HSPlayer hsPlayer = HSPlayer.getExact(event.getPlayer().getUniqueId());
        if (!hsPlayer.isExempt()) hsPlayer.startLeaveTask();
    }

    @EventHandler
    public void cancelLeaveTaskOnJoin(AsyncPlayerPreLoginEvent event) {
        if (!HideAndSeek.getInstance().getGameManager().isGameRunning()) return;

        HSPlayer hsPlayer = HSPlayer.getExact(event.getUniqueId());
        if (!hsPlayer.isExempt()) hsPlayer.cancelLeaveTask();
    }

    @EventHandler
    public void disableBlockBreaking(BlockBreakEvent event) {
        if (!HideAndSeek.getInstance().getGameManager().isGameRunning()) return;
        Player player = event.getPlayer();
        HSPlayer hsPlayer = HSPlayer.getExact(player.getUniqueId());

        if (hsPlayer.getCurrentTeam().getTeamType() == TeamType.HIDER) {
            player.sendMessage(ChatColor.RED + "You can't break blocks as a hider");
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void disableFireworkRockets(PlayerInteractEvent event) {
        if (!HideAndSeek.getInstance().getGameManager().isGameRunning()) return;
        Player player = event.getPlayer();
        HSPlayer hsPlayer = HSPlayer.getExact(player.getUniqueId());

        if (hsPlayer.isExempt()) return;

        Action action = event.getAction();

        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            if (event.getMaterial() == Material.FIREWORK_ROCKET) event.setCancelled(true);
        }
    }

    @EventHandler
    public void disableDamageToSeeker(EntityDamageEvent event) {
        if (!HideAndSeek.getInstance().getGameManager().isGameRunning()) return;
        Entity entity = event.getEntity();

        if (entity instanceof Player) {
            Player player = (Player) entity;
            HSPlayer hsPlayer = HSPlayer.getExact(player.getUniqueId());

            if (hsPlayer.getCurrentTeam().getTeamType() == TeamType.SEEKER) {
                if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void disablePotionConsume(EntityPotionEffectEvent event) {
        if (!HideAndSeek.getInstance().getGameManager().isGameRunning()) return;
        Entity entity = event.getEntity();
        EntityPotionEffectEvent.Action action = event.getAction();

        if (entity instanceof Player) {
            Player player = (Player) entity;
            HSPlayer hsPlayer = HSPlayer.getExact(player.getUniqueId());

            if (hsPlayer.isExempt()) return;

            if (action == EntityPotionEffectEvent.Action.ADDED || action == EntityPotionEffectEvent.Action.CHANGED) {
                event.setCancelled(true);
            }
        }
    }
}
