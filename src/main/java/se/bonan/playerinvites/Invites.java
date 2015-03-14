package se.bonan.playerinvites;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.Map;
import java.util.UUID;

/**
 * Date: 2015-03-14
 * Time: 02:22
 */
public class Invites {

    PlayerInvites plugin;

    /**
     * Helper object for mapping of JSON to Object
     */
    DataFile data;

    public Invites(PlayerInvites plugin) {
        this.plugin = plugin;

        /**
         * Create empty data object to avoid NullPointerException if load fails
         */
        this.data = new DataFile();

        /**
         * Load data
         */
        load();
    }

    private String load() {
        try {
            /**
             * Loads file from data.json in plugin data folder
             */
            data = DataFile.load(new File(plugin.getDataFolder(), "data.json"));
        } catch (IOException e) {
            plugin.getLogger().warning(
                    "Unable to load PlayerInvites data file: " + e.getMessage()
            );
            return ChatColor.RED + "Error: " + e.getMessage();
        }
        return "";
    }

    private void save() {
        try {
            /**
             * Saves data as json to data.json in plugin data folder
             */
            data.save(new File(plugin.getDataFolder(), "data.json"));
        } catch (IOException e) {
            plugin.getLogger().warning(
                    "Unable to save PlayerInvites data file: " + e.getMessage()
            );
        }
    }

    /**
     * Adds or removes invites from a player based on player name
     * @param target Name of player
     * @param count Amount of invites to add (negative to subtract)
     * @return false if player not found
     */
    private Boolean updateInvites(String target, Integer count) {
        OfflinePlayer player = getPlayer(target);
        if (player != null) {
            updateInvites(player, count);
            return true;
        }
        return false;
    }

    /**
     * Adds or removes invites from a player based on player object
     * @param player Player object
     * @param count Amount of invites to add (negative to subtract)
     */
    private void updateInvites(OfflinePlayer player, Integer count) {
        String target = player.getUniqueId().toString();
        Map<String,Integer> invites = data.getInvites();
        Integer current = 0;
        if (invites.containsKey(target)) {
            current = invites.get(target);
        }
        Integer newCount = current + count;
        if (newCount < 0)
            newCount = 0;
        invites.put(target, newCount);
    }

    /**
     * Adds a player to white list based on player name
     * @param target Player name
     * @return Whitelisted player UUID on success, "exists" if player already is in whitelist, "failed" on error
     */
    private String addWhitelist(String target) {
        for (OfflinePlayer player: plugin.getServer().getWhitelistedPlayers()) {
            if (player.getName().equalsIgnoreCase(target)) {
                return "exists";
            }
        }
        if (!plugin.getServer().dispatchCommand(
                plugin.getServer().getConsoleSender(),
                "whitelist add "+target
        )) {
            return "failed";
        }

        for (OfflinePlayer player: plugin.getServer().getWhitelistedPlayers()) {
            if (player.getName().equalsIgnoreCase(target)) {
                return player.getUniqueId().toString();
            }
        }

        return "failed";

    }

    /**
     * Removes player from whitelist based on name
     * @param target Player name
     * @return true on success
     */
    private Boolean removeWhitelist(String target) {
        return plugin.getServer().dispatchCommand(
                plugin.getServer().getConsoleSender(),
                "whitelist remove " + target
        );
    }

    /**
     * Reloads configuration
     * @return empty string on success, error message otherwise
     */
    public String reload() {
        return load();
    }

    /**
     * Get player name from player uuid
     * @param uuid Player uuid
     * @return Player name
     */
    public String getName(String uuid) {

        OfflinePlayer player = plugin.getServer().getOfflinePlayer(UUID.fromString(uuid));
        if (player != null)
            return player.getName();
        return "";
    }

    /**
     * Get player based on player uuid
     * @param uuid UUID of player
     * @return Player
     */
    public OfflinePlayer getPlayer(UUID uuid) {
        return plugin.getServer().getOfflinePlayer(uuid);
    }

    /**
     * Gets player object from player name.
     * @param name Name of player
     * @return Player object, null if no player found
     */
    public OfflinePlayer getPlayer(String name) {
        for (OfflinePlayer player: plugin.getServer().getOfflinePlayers()) {
            if (player.getName().equalsIgnoreCase(name)) {
                return player;
            }
        }
        /**
         * If player not found, check white list for players that are whitelisted
         * but hasn't joined the server yet
         */
        for (OfflinePlayer player: plugin.getServer().getWhitelistedPlayers()) {
            if (player.getName().equalsIgnoreCase(name)) {
                return player;
            }
        }
        return null;
    }

    /**
     * Get number of invites based on player name
     * @param target Player name
     * @return Number of invites, null if player not found
     */
    public Integer getInvites(String target) {
        OfflinePlayer player = getPlayer(target);
        if (player != null)
            return getInvites(player);
        return null;
    }

    /**
     * Get number of invites based on player object
     * @param target Player object
     * @return Number of invites
     */
    public Integer getInvites(OfflinePlayer target) {
        Integer count = 0;
        Map<String,Integer> invites = data.getInvites();
        String uuid = target.getUniqueId().toString();
        if (invites.containsKey(uuid)) {
            count = invites.get(uuid);
        }
        return count;
    }

    /**
     * Checks if inviter has >0 invites available, then adds target player to whitelist
     *   and subtracts number of invites from inviter by one
     * @param inviter Player issuing invite
     * @param target Player to be added to whitelist
     * @return In-game message to inviter
     */
    public String invitePlayer(Player inviter, String target) {
        FileConfiguration c = plugin.getConfig();

        if (getInvites(inviter) < 1) {
            /**
             * Not enough invites
             */
            return ChatColor.RED + c.getString("stringNoInvites",
                    "You have no invites available");
        }

        Map<String, String> invited = data.getInvited();
        String uuid = addWhitelist(target);

        if (uuid == null || uuid.equals("failed") || uuid.equals("")) {
            /**
             * Failed to add player, invalid name or player not found
             * No action taken
             */
            return ChatColor.RED + c.getString("stringInviteError",
                    "Unable to invite %s")
                    .replace("%s", ChatColor.BLUE + target + ChatColor.RED);
        }
        if (uuid.equals("exists")) {
            /**
             * Target already in whitelist, no action taken
             */
            return ChatColor.RED + c.getString("stringInviteExists",
                    "%s has already been invited")
                    .replace("%s", ChatColor.BLUE + target + ChatColor.RED);
        }

        /**
         * Add player uuid:s to invited list
         */
        invited.put(uuid, inviter.getUniqueId().toString());

        /**
         * Subtract number of invites
         */
        updateInvites(inviter, -1);

        /**
         * Save data file
         */
        save();

        return ChatColor.GREEN + c.getString("stringInviteSuccess",
                "%s has been invited.")
                .replace("%s", ChatColor.BLUE + target + ChatColor.GREEN);
    }

    /**
     * Returns information about a players number of invites
     * @param target Name of player to show info for
     * @param self True if target is player (Shows "You have" instead of "player has")
     * @return Message to player
     */
    public String showInvites(String target, Boolean self) {
        FileConfiguration c = plugin.getConfig();
        Integer invites = getInvites(target);

        if (invites == null) {
            return ChatColor.RED + c.getString(
                    "stringPlayerNotFound",
                    "Player %s was not found")
                    .replace("%s", target);
        }
        if (invites == 0) {
            if (self)
                return ChatColor.YELLOW + c.getString("stringInvitesSelfNone", "You have no invites available");
            return ChatColor.YELLOW + c.getString("stringInvitesNone", "%s has no invites available")
                    .replace("%s", ChatColor.BLUE + target + ChatColor.YELLOW);
        }
        if (invites == 1) {
            if (self)
                return ChatColor.GREEN + c.getString("stringInvitesSelfOne", "You have one invite available");
            return ChatColor.GREEN + c.getString("stringInvitesOne", "%s has one invite available")
                    .replace("%s", ChatColor.BLUE + target + ChatColor.GREEN);
        }
        if (self)
            return ChatColor.GREEN + c.getString("stringInvitesSelf", "You have %d invites available")
                    .replace("%d", ChatColor.BLUE + invites.toString() + ChatColor.GREEN);
        return ChatColor.GREEN + c.getString("stringInvites", "%s has %d invites available")
                .replace("%s", ChatColor.BLUE + target + ChatColor.GREEN)
                .replace("%d", ChatColor.BLUE + invites.toString() + ChatColor.GREEN);
    }

    /**
     * Returns info about who invited a player and who they have invited based on player name
     * @param target Player to show info about
     * @return Message to player
     */
    public String showInvited(String target) {
        OfflinePlayer player = getPlayer(target);
        if (player != null)
            return showInvited(player);

        return null;
    }

    /**
     * Returns info about who invited a player and who they have invited based on player object
     * @param player Player to show info about
     * @return Message to player
     */
    public String showInvited(OfflinePlayer player) {
        String target = player.getUniqueId().toString();
        Map<String, String> dataInvited = data.getInvited();
        FileConfiguration c = plugin.getConfig();
        String inviterMsg = "";
        String invitedMsg = "";
        String lastInvited = null;
        for (String invited: dataInvited.keySet()) {
            String inviter = dataInvited.get(invited);
            if (inviter.equalsIgnoreCase(target)) {
                if (lastInvited != null) {
                    invitedMsg = invitedMsg +
                            (invitedMsg.length()>0 ? ", " : "") +
                            ChatColor.BLUE + getName(lastInvited) + ChatColor.RESET;
                }
                lastInvited = invited;
            }
            if (invited.equalsIgnoreCase(target)) {
                inviterMsg = c.getString("stringInvitedBy",
                        "%s was invited by %p")
                        .replace("%s", ChatColor.BLUE + player.getName() + ChatColor.RESET)
                        .replace("%p", ChatColor.BLUE + getName(inviter) + ChatColor.RESET) +
                        "\n";
            }
        }
        if (lastInvited != null) {
            if (invitedMsg.length() == 0)
                invitedMsg = ChatColor.BLUE + getName(lastInvited) + ChatColor.RESET;
            else
                invitedMsg = invitedMsg + " " + c.getString("stringAnd","and") + " " +
                        ChatColor.BLUE + getName(lastInvited) + ChatColor.RESET;
        }

        if (invitedMsg.equals("")) {
            invitedMsg = c.getString("stringInvitedNone",
                            "%s hasn't invited anyone yet")
                            .replace("%s", ChatColor.BLUE + player.getName() + ChatColor.RESET);
        } else {
            invitedMsg = c.getString("stringInvited",
                    "%s has invited:")
                    .replace("%s", ChatColor.BLUE + player.getName() + ChatColor.RESET) +
                    " " + invitedMsg;
        }

        return inviterMsg + invitedMsg;
    }

    /**
     * Adds (or subtracts if negative) invites to a player
     * @param target Name of player to give invites
     * @param count Amount of invites to give
     * @return Message to player
     */
    public String giveInvite(String target, Integer count) {

        FileConfiguration c = plugin.getConfig();

        String strPlayer = c.getString("stringPlayer", "player");
        String cmdBase = c.getString("cmdBase", "invite");
        String cmdUse = c.getString("cmdUse", "use");

        if (!updateInvites(target, count)) {
            return ChatColor.RED + c.getString("stringPlayerNotFound", "Player %s was not found")
                    .replace("%s", target);
        }
        save();

        for (Player player: plugin.getServer().getOnlinePlayers()) {
            if (player.getName().equalsIgnoreCase(target)) {
                if (player.hasPermission("invites.showgiven")) {
                    player.sendMessage(
                            ChatColor.GREEN + c.getString("stringInviteGiven",
                                    "You have been given %d additional invite(s).")
                                    .replace("%d", ChatColor.BLUE + count.toString() + ChatColor.GREEN) + " " +
                            ( player.hasPermission("invites.invite") ?
                                    c.getString("stringInviteGivenUsage",
                                            "Invite new players with /"+cmdBase+" "+cmdUse+" <"+strPlayer+">") :
                                    "" )
                    );
                }
            }
        }

        return ChatColor.GREEN + c.getString("stringGiveInvite", "%s has been given %d new invite(s).")
                .replace("%s", ChatColor.BLUE + target + ChatColor.GREEN)
                .replace("%d", ChatColor.BLUE + count.toString() + ChatColor.GREEN);
    }

    /**
     * Buy invites.
     * Subtracts money and increases number of invites of a player
     * @param player Player object that buys invites
     * @param count Amount of invites to buy
     * @return Message to player
     */
    public String buyInvite(Player player, Integer count) {
        FileConfiguration c = plugin.getConfig();
        Double price = plugin.getConfig().getDouble("buyInvitePrice");
        Double amount = price * count;

        Economy economy = plugin.getEconomy();
        Double balance = economy.getBalance(player);

        if (balance < amount) {
            return ChatColor.RED + c.getString("stringBuyFailed",
                    "You don't have enough money to buy %d invites, another %s is needed")
                    .replace("%d", ChatColor.BLUE + count.toString() + ChatColor.RED)
                    .replace("%s", ChatColor.BLUE +
                            String.valueOf(amount-balance) + " " +
                            economy.currencyNamePlural() +
                            ChatColor.RED);
        }

        EconomyResponse r = economy.withdrawPlayer(player, amount);
        if (r.transactionSuccess()) {
            updateInvites(player, count);
            save();
            return ChatColor.GREEN + c.getString("stringBuySuccess", "Bought %d invites for %s")
                    .replace("%d", ChatColor.BLUE + count.toString() + ChatColor.RESET)
                    .replace("%s", ChatColor.BLUE +
                            amount.toString() + " " +
                            economy.currencyNamePlural() +
                            ChatColor.RESET);
        }

        return ChatColor.RED + c.getString("stringBuyError", "Unable to withdraw money");
    }
}
