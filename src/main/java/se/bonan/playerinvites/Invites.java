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
    DataFile data;

    public Invites(PlayerInvites plugin) {
        this.plugin = plugin;
        this.data = new DataFile();
        load();
    }

    private String load() {
        try {
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
            data.save(new File(plugin.getDataFolder(), "data.json"));
        } catch (IOException e) {
            plugin.getLogger().warning(
                    "Unable to save PlayerInvites data file: " + e.getMessage()
            );
        }
    }

    private Boolean updateInvites(String target, Integer count) {
        OfflinePlayer player = getPlayer(target);
        if (player != null) {
            updateInvites(player, count);
            return true;
        }
        return false;
    }

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

    private Boolean removeWhitelist(String target) {
        return plugin.getServer().dispatchCommand(
                plugin.getServer().getConsoleSender(),
                "whitelist remove " + target
        );
    }

    public String reload() {
        return load();
    }

    public String getName(String uuid) {

        OfflinePlayer player = plugin.getServer().getOfflinePlayer(UUID.fromString(uuid));
        if (player != null)
            return player.getName();
        return "";
    }

    public OfflinePlayer getPlayer(UUID uuid) {
        return plugin.getServer().getOfflinePlayer(uuid);
    }

    public OfflinePlayer getPlayer(String name) {
        for (OfflinePlayer player: plugin.getServer().getOfflinePlayers()) {
            if (player.getName().equalsIgnoreCase(name)) {
                return player;
            }
        }
        for (OfflinePlayer player: plugin.getServer().getWhitelistedPlayers()) {
            if (player.getName().equalsIgnoreCase(name)) {
                return player;
            }
        }
        return null;
    }

    public Integer getInvites(String target) {
        OfflinePlayer player = getPlayer(target);
        if (player != null)
            return getInvites(player);
        return null;
    }

    public Integer getInvites(OfflinePlayer target) {
        Integer count = 0;
        Map<String,Integer> invites = data.getInvites();
        String uuid = target.getUniqueId().toString();
        if (invites.containsKey(uuid)) {
            count = invites.get(uuid);
        }
        return count;
    }

    public String invitePlayer(Player inviter, String target) {
        FileConfiguration c = plugin.getConfig();

        if (getInvites(inviter) < 1) {
            return ChatColor.RED + c.getString("stringNoInvites",
                    "You have no invites available");
        }

        Map<String, String> invited = data.getInvited();
        String uuid = addWhitelist(target);

        if (uuid == null || uuid.equals("failed") || uuid.equals("")) {
            return ChatColor.RED + c.getString("stringInviteError",
                    "Unable to invite %s")
                    .replace("%s", ChatColor.BLUE + target + ChatColor.RED);
        }
        if (uuid.equals("exists")) {
            return ChatColor.RED + c.getString("stringInviteExists",
                    "%s has already been invited")
                    .replace("%s", ChatColor.BLUE + target + ChatColor.RED);
        }

        invited.put(uuid, inviter.getUniqueId().toString());
        updateInvites(inviter, -1);
        save();

        return ChatColor.GREEN + c.getString("stringInviteSuccess",
                "%s has been invited.")
                .replace("%s", ChatColor.BLUE + target + ChatColor.GREEN);
    }

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

    public String showInvited(String target) {
        OfflinePlayer player = getPlayer(target);
        if (player != null)
            return showInvited(player);

        return ChatColor.RED + plugin.getConfig().getString(
                "stringPlayerNotFound",
                "Player %s was not found")
                .replace("%s", target);
    }

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
