package se.bonan.playerinvites.object;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import se.bonan.playerinvites.PlayerInvites;
import se.bonan.playerinvites.Str;

import java.time.Instant;
import java.util.Date;

/**
 * Object represents a configuration in config.yml for automatically
 * giving invites to players.
 *
 * See config.yml for examples
 */
public class AutoGive {

    /**
     * Name of AutoGive configuration. Used as permission node (invites.auto."name")
     * and in configuration
     */
    private final String name;

    /**
     * Interval between automatic invites (minutes)
     */
    private final Integer interval;

    /**
     * Number of invites to give every interval
     */
    private final Integer interval_count;

    /**
     * Player must have been online at least this recently (minutes) to receive
     * any invites
     */
    private final Long last_online;

    /**
     * Maximum number of invites, if player has at least this many invites,
     * no additional invites will be given
     */
    private final Integer max;

    /**
     * Number of invites to give a player once
     */
    private final Integer once;

    /**
     *
     * @param name Name of configuration
     * @param config Relevant part of configuration
     */
    public AutoGive(String name, ConfigurationSection config) {
        this.name = name;
        this.interval = config.getInt("interval", -1); // Disabled by default
        this.max = config.getInt("max", -1); // No maximum by default
        this.interval_count = config.getInt("interval_count", 1); // Give one by default
        this.last_online = config.getLong("last_online", -1); // Disabled by default
        this.once = config.getInt("once", 0);
    }

    /**
     * Returns false if this AutoGive doesn't actually do anything useful
     * Used to check if scheduling is necessary.
     * @return false if AutoGive is not needed
     */
    public Boolean isNeeded() {
        return interval != -1;
    }

    /**
     * Is invoked periodically to check if a player should get new invites
     * Permissions are checked elsewhere (AutoGiveTask)
     *
     * @param plugin Calling plugin
     * @param player Relevant player
     * @param data Data of player
     */
    public Boolean check(PlayerInvites plugin, Player player, PlayerData data) {

        /**
         * Set to true when anything changes
         */
        Boolean updated = false;

        /**
         * Number of invites to give this run
         */
        Integer giveInvites = 0;

        /**
         * Do nothing if player is already over maximum
         */
        if (max >= 0 && data.getInvites() >= max)
            return false;

        /**
         * Current date
         */
        Date now = new Date();

        /**
         * Check when player was last online
         */
        if (last_online > 0) {
            Date lastOnline = Date.from(Instant.ofEpochMilli(player.getLastPlayed()));
            Long onlineDiff = (now.getTime() - lastOnline.getTime()) / 1000L;

            if (onlineDiff/60L > last_online) {
                /**
                 * Player has been offline for too long, don't give invite
                 */
                return false;
            }
        }

        /**
         * Check for periodic invites
         */
        if (interval > 0) {
            /**
             * Last date given
             */
            Date last = data.getAutoLast(name);

            /**
             * Base last given on first played date if invite never has been given
             */
            if (last == null)
                last = Date.from(Instant.ofEpochMilli(player.getFirstPlayed()));

            /**
             * Seconds since last given invite
             */
            Long diff = (now.getTime() - last.getTime()) / 1000L;

            /**
             * More than interval minutes has passed since last automatic invite
             * Give new invite
             */
            if (diff/60L > interval) {
                giveInvites = giveInvites + interval_count;
                data.setAutoLast(name);
            }

        }

        if (once > 0) {
            if (!data.getAutoOnce(name)) {
                giveInvites = giveInvites + once;
                data.setAutoOnce(name);
            }
        }

        if (giveInvites > 0) {
            /**
             * Set updated = true
             */
            updated = true;

            /**
             * Give player invite
             */
            data.addInvites(giveInvites);

            if (player.hasPermission("invites.showgiven")) {

                /**
                 * Send message to player notifying them of their new invite
                 */
                String msg = (giveInvites == 1 ? Str.GivenInvitePeriodicOne.str() :
                    Str.GivenInvitePeriodic.format(giveInvites.toString()));

                /**
                 * Show usage
                 */
                if (data.getInvites() > 0 && player.hasPermission("invites.invite")) {
                    msg = msg + " " + Str.GivenInviteUsage.format();
                }

                player.sendMessage(msg);
            }
        }

        return updated;
    }

}
