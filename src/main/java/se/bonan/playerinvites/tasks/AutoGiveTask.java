package se.bonan.playerinvites.tasks;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import se.bonan.playerinvites.PlayerInvites;
import se.bonan.playerinvites.object.AutoGive;

import java.util.HashMap;
import java.util.Map;

/**
 * AutoGiveTask is run periodically to check for users that will be
 * given new invites automatically. See config.yml for configuration examples
 *
 * A player must have the permission invites.auto to receive any automatic invites.
 */
public class AutoGiveTask extends BukkitRunnable {

    /**
     * Parent plugin
     */
    private final PlayerInvites plugin;

    /**
     * The different configurations from config.yml
     * "all" is special and applies to all players
     */
    private final Map<String, AutoGive> config;

    /**
     * Interval between checks
     */
    private final Long interval = 20L*60L;

    /**
     * Initialize Task from configuration
     * @param plugin
     */
    public AutoGiveTask(PlayerInvites plugin) {
        this.plugin = plugin;
        this.config = new HashMap<>();

        ConfigurationSection s = plugin.getConfig().getConfigurationSection("auto");
        for (String k: s.getKeys(false)) {
            config.put(k, new AutoGive(k, s.getConfigurationSection(k)));
        }
    }

    /**
     * Checks if any of the configured AutoGives are needed.
     * @return true if needed, false otherwise
     */
    public Boolean isNeeded() {
        for (AutoGive a: config.values()) {
            if (a.isNeeded())
                return true;
        }
        return false;
    }

    /**
     * Get interval (how often task should run)
     * @return Interval
     */
    public Long getInterval() {
        return interval;
    }

    @Override
    public void run() {
        Boolean doSave = false;
        for (Player player: plugin.getServer().getOnlinePlayers()) {
            if (!player.hasPermission("invites.auto"))
                continue;

            for (String perm: config.keySet()) {
                if (perm.equals("all") || player.hasPermission("invites.auto." + perm)) {
                    Boolean updated = config.get(perm).check(plugin, player, plugin.getPlayerData(player));
                    if (updated) doSave = true;
                }
            }
        }
        if (doSave)
            plugin.save();
    }
}
