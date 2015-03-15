package se.bonan.playerinvites.commands;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import se.bonan.playerinvites.PlayerInvites;
import se.bonan.playerinvites.Str;

/**
 * Gives a player any amount of invites
 * Note that this is an admin command, no invites will be subtracted
 * from the user issuing the command.
 *
 * Set a negative amount to remove invites
 *
 * Usage:
 *   /invite give <player> [amount]
 *
 * Permission:
 *   invites.give - Allows a player to issue command
 *   invites.showgiven - Sends a message to a player when they receive an invite
 */
public class GiveCommand extends CommandBase implements CommandInterface
{

    public GiveCommand(String name, PlayerInvites plugin) {
        super(name, plugin, "invites.give");
    }

    @Override
    public String invoke(Player player, String[] args) {
        /**
         * Show usage if not 1-2 arguments are given
         */
        if (args.length == 0 || args.length > 2)
            return usagePrefix(player);

        /**
         * Set target
         */
        String target = args[0];

        /**
         * Set amount
         */
        Integer amount = 1;
        if (args.length == 2) {
            try {
                /**
                 * Set amount from argument
                 */
                amount = Integer.valueOf(args[1]);
            } catch (NumberFormatException e) {
                /**
                 * Invalid amount from argument, show usage
                 */
                return usagePrefix(player);
            }
        }
        /**
         * Show usage if amount = 0
         */
        if (amount == 0)
            return usagePrefix(player);

        OfflinePlayer tp = null;

        /**
         * Loop through online players to find target
         *
         * Show message to player if found and has permission invites.showgiven
         */
        for (Player pl: plugin.getServer().getOnlinePlayers()) {
            if (pl.getName().equalsIgnoreCase(target)) {
                tp = pl;
                if (pl.hasPermission("invites.showgiven")) {
                    String message;
                    if (amount == 1)
                        message = Str.GivenInviteOne.str();
                    else
                        message = Str.GivenInvite.format(amount.toString());
                    if (pl.hasPermission("invites.invite")) {
                        message = message + " " + Str.GivenInviteUsage.format();
                    }

                    pl.sendMessage(message);
                }
                break;
            }
        }

        /**
         * Lopp through offline players if not online
         */
        if (tp == null) {
            for (OfflinePlayer pl: plugin.getServer().getOfflinePlayers()) {
                if (pl.getName().equals(target)) {
                    tp = pl;
                    break;
                }
            }
        }

        /**
         * Player not found
         */
        if (tp == null)
            return Str.ErrorNotFound.format(target);

        /**
         * Give invites to player and save data file
         */
        plugin.getPlayerData(tp).addInvites(amount);
        plugin.save();

        /**
         * Return success message
         */
        if (amount == 1)
            return Str.GiveInviteOne.format(tp.getName());
        return Str.GiveInvite.format(tp.getName(), amount.toString());


    }

    @Override
    public String usage(Player player) {
        /**
         * /invite give <player> [amount]
         */
        return usageStr(Str.HelpGive.str(),
                Str.HelpArg.format(Str.Player.str()),
                Str.HelpOpt.format(Str.Amount.str())
        );
    }
}
