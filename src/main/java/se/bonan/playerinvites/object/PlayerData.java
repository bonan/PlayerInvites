package se.bonan.playerinvites.object;

import org.bukkit.OfflinePlayer;
import se.bonan.playerinvites.PlayerInvites;

import java.util.*;

/**
 * Date: 2015-03-15
 * Time: 00:36
 */
public class PlayerData {

    /**
     * Player UUID
     */
    private String uuid;

    /**
     * Player name
     */
    private String name;

    /**
     * UUID of player that invited
     */
    private String invitedBy;

    /**
     * Number of available invites
     */
    private Integer invites;

    /**
     * History for when periodic invite was last given
     */
    private Map<String,Date> autoLast;

    /**
     * Last static invite that was given
     */
    private Map<String,Integer> autoStatic;

    /**
     * List of keys where invite has been given once
     */
    private List<String> autoOnce;

    /**
     * Set to true when new invites has been given, notifies the user
     * when they join.
     */
    private Boolean autoNotify;

    /**
     * Creates new PlayerData
     * @param player Player object
     * @param invitedBy Player that invited this player, null if none
     */
    public PlayerData(OfflinePlayer player, OfflinePlayer invitedBy) {
        this.name = player.getName();
        this.uuid = player.getUniqueId().toString();
        if (invitedBy != null) {
            this.invitedBy = invitedBy.getUniqueId().toString();
        }
        this.invites = 0;
    }

    /**
     * Returns the number of available invites
     * @return Available invites
     */
    public Integer getInvites() {
        return invites == null ? 0 : invites;
    }

    /**
     * Sets the number of available invites
     * @param invites Number of invites to set
     */
    public void setInvites(Integer invites) {
        if (invites < 0)
            invites = 0;
        this.invites = invites;
    }

    /**
     * Adds invites to player, negative value to subtract invites
     * @param invites Number of invites to add
     */
    public void addInvites(Integer invites) {
        setInvites(this.invites + invites);
    }

    /**
     * Gets a list of PlayerData objects for everyone this player has invited
     * @param plugin
     * @return List of invited PlayerData objects
     */
    public List<PlayerData> getInvited(PlayerInvites plugin) {
        List<PlayerData> ret = new LinkedList<>();
        /**
         * Loop through other players
         */
        for (PlayerData player: plugin.getData().getPlayers().values()) {
            /**
             * Check if other player was invited by this player
             */
            if (player.invitedBy != null && player.invitedBy.equals(uuid)) {
                /**
                 * Add to list
                 */
                ret.add(player);
            }
        }
        /**
         * Return list
         */
        return ret;
    }

    /**
     * Returns the PlayerData object for the player that invited this player
     * @param plugin
     * @return PlayerData object for player that invited. Null if player wasn't invited
     */
    public PlayerData getInvitedBy(PlayerInvites plugin) {

        if (invitedBy != null && plugin.getData().getPlayers().containsKey(invitedBy)) {
            return plugin.getData().getPlayers().get(invitedBy);
        }
        return null;
    }

    /**
     * Get OfflinePlayer object for this player
     * @param plugin
     * @return OfflinePlayer object of player
     */
    public OfflinePlayer getPlayer(PlayerInvites plugin) {
        /**
         * Fetch from UUID
         */
        return plugin.getServer().getOfflinePlayer(UUID.fromString(uuid));
    }

    /**
     * Returns UUID
     * @return UUID as string
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * Returns name. Note that the name is set on registration and might not be the
     * current name. Use of getPlayer().getName() is recommended
     * @return Name of player
     */
    public String getName() {
        return name;
    }

    /**
     * Returns UUID of the player that invited this player
     * @return UUID as string
     */
    public String getInvitedBy() {
        return invitedBy;
    }

    /**
     * Returns date for last periodic invite
     * @param key Name of auto-configuration
     * @return Date of last periodic invite, null if none
     */
    public Date getAutoLast(String key) {
        if (autoLast == null)
            return null;
        if (!autoLast.containsKey(key))
            return null;
        return autoLast.get(key);
    }

    /**
     * Updates an entry with the current date
     * @param key Name of auto-configuration
     */
    public void setAutoLast(String key) {
        if (autoLast == null)
            autoLast = new HashMap<>();
        autoLast.put(key, new Date());
    }

    /**
     * Gets the interval for the last statically given invite
     * @param key Name of auto-configuration
     * @return Last interval
     */
    public Integer getAutoStatic(String key) {
        if (autoStatic == null)
            return null;
        if (!autoStatic.containsKey(key))
            return null;
        return autoStatic.get(key);
    }

    /**
     * Set the last interval a invite was given
     * @param key Name of auto-configuration
     * @param s Interval
     */
    public void setAutoStatic(String key, Integer s) {
        if (autoStatic == null)
            autoStatic = new HashMap<>();
        autoStatic.put(key,s);
    }

    /**
     * Returns true if player has been given the once automatic invite
     * @param key Name of auto-configuration
     * @return True if invite given, false otherwise
     */
    public Boolean getAutoOnce(String key) {
        return autoOnce != null && autoOnce.contains(key);
    }

    /**
     * Adds key to the list of automatic invites given once
     * @param key Name of auto-configuration
     */
    public void setAutoOnce(String key) {
        if (autoOnce == null)
            autoOnce = new ArrayList<>();
        if (!autoOnce.contains(key))
            autoOnce.add(key);
    }

    /**
     * Check whether the player has gotten new invites
     * since it was last online
     * @return true if we should notify player about new invites
     */
    public Boolean getAutoNotify() {
        return autoNotify != null && autoNotify;
    }

    /**
     * Sets whether to notify the player about their new invites
     * the next time they join.
     * @param notify true to notify player
     */
    public void setAutoNotify(Boolean notify) {
        if (notify == null || !notify)
            /**
             * Saves a few bytes of space to remove the entry instead of
             * setting it to false ;)
             */
            autoNotify = null;
        else
            autoNotify = true;
    }
}
