package se.bonan.playerinvites.commands;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import se.bonan.playerinvites.PlayerInvites;
import se.bonan.playerinvites.Str;
import se.bonan.playerinvites.object.PlayerData;

/**
 * Shows a players number of available invites, who invited them and who
 * they have invited.
 *
 * "bonaan was invited by Notch"
 * "bonaan has invited: jeb, Herobrine and Notch"
 * "bonaan has 10 invites available" *
 *
 * Usage:
 *   /invite show
 *   /invite show [player] (When player has permission invites.showother)
 *
 * Permissions:
 *   invites.show - Allows player to issue command
 *   invites.showother - Allows player to see other players invites and invited
 */
public class ShowCommand extends CommandBase implements CommandInterface
{

    public ShowCommand(String name, PlayerInvites plugin) {
        super(name, plugin, "invites.show");
    }

    @Override
    public String invoke(Player player, String[] args) {

        /**
         * Default target to invoking player
         */
        String target = null;
        if (player != null)
            target = player.getName();

        if (args.length >= 1 && (player == null || player.hasPermission("invites.showother"))) {
            /**
             * Return usage if two or more arguments is provided.
             */
            if (args.length > 1)
                return usagePrefix(player);
            /**
             * Set target
             */
            target = args[0];
        }
        /**
         * Return usage if args are provided, but user doesn't have permission
         * to show other players
         */
        else if (target == null || args.length > 0)
            return usagePrefix(player);

        Boolean self = player != null && (player.getName().equalsIgnoreCase(target));
        PlayerData data = null;
        /**
         * Target player object
         */
        OfflinePlayer tp = player;
        if (self) {
            /**
             * Fetch data from own player
             */
            data = plugin.getPlayerData(player);
        } else {
            /**
             * Find stated player and retrieve invites data
             */
            for (OfflinePlayer pl: plugin.getServer().getOfflinePlayers()) {
                if (pl.getName().equals(target)) {
                    data = plugin.getPlayerData(pl);
                    tp = pl;
                    break;
                }
            }
        }
        /**
         * Return error if player not found
         */
        if (data == null)
            return Str.ErrorNotFound.format(target);

        String reply = "";
        /**
         * Get invited by
         */
        PlayerData invitedBy = data.getInvitedBy(plugin);
        if (invitedBy != null) {
            reply = Str.ShowInvitedBy.format(tp.getName(), invitedBy.getPlayer(plugin).getName()) + "\n";
        }

        /**
         * Build string of invited players
         */
        String invited = "";
        String lastInvited = null;
        for (PlayerData n: data.getInvited(plugin)) {
            if (lastInvited != null) {
                invited = invited +
                        // Add separator between nicks
                        (invited.length() > 0 ? Str.ShowInvitedSep.str() : "") +
                        lastInvited;
            }
            // Format player name
            lastInvited = Str.PlayerName.format(n.getPlayer(plugin).getName());
        }
        if (lastInvited != null) {
            // Add "and" between last two invited
            invited = invited +
                    (invited.length() > 0 ? " " + Str.And + " " : "") +
                    lastInvited;
        }

        /**
         * Display of invited players
         */
        if (!invited.equals("")) {
            reply = reply + Str.ShowInvited.format(tp.getName(), invited) + "\n"; // %1 has invited: %2
        } else {
            reply = reply + Str.ShowInvitedNone.format(tp.getName()) + "\n"; // %1 hasn't invited anyone yet
        }

        /**
         * String to use for displaying number of invites
         */
        Str inv;

        if (data.getInvites() == 0) {
            if (self) inv = Str.ShowInvitesSelfNone; // You have no invites
            else inv = Str.ShowInvitesNone; // %1 has no invites
        } else if (data.getInvites() == 1) {
            if (self) inv = Str.ShowInvitesSelfOne; // You have one invite
            else inv = Str.ShowInvitesOne; // %1 has one invite
        } else {
            if (self) inv = Str.ShowInvitesSelf; // You have %2 invites
            else inv = Str.ShowInvites; // %1 has %2 invites
        }

        /**
         * Number of invites
         */
        reply = reply + inv.format(tp.getName(), data.getInvites().toString());

        return reply;

    }

    @Override
    public String usage(Player player) {
        if (player == null) {
            /**
             * Show player argument as required when issued from console
             */
            return usageStr(Str.HelpShow.str(),
                Str.HelpArg.format(Str.Player.str())
            );
        }
        if (player.hasPermission("invites.showother")) {
            /**
             * Only show player argument in help/usage when the player has
             * permission to use it.
             */
            return usageStr(Str.HelpShow.str(),
                Str.HelpOpt.format(Str.Player.str())
            );
        }

        return usageStr(Str.HelpShowYour.str());
    }
}
