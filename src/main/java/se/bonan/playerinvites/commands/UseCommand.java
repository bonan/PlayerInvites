package se.bonan.playerinvites.commands;

import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import se.bonan.playerinvites.PlayerInvites;
import se.bonan.playerinvites.Str;
import se.bonan.playerinvites.object.PlayerData;

/**
 * Use an invite to add another player to whitelist.
 *
 * If the player already exists in whitelist or the player cannot
 * be added to whitelist, an error will be returned and no invites
 * will be used.
 *
 * Usage:
 *   /invite use <player>
 *
 * Permissions:
 *   invites.invite - Use invites
 *
 */
public class UseCommand extends CommandBase implements CommandInterface {

    public UseCommand(String name, PlayerInvites plugin) {
        super(name, plugin, "invites.invite");
    }

    @Override
    public String invoke(Player player, String[] args) {
        /**
         * Check for exactly one argument
         */
        if (args.length != 1)
            return usagePrefix(player);
        /**
         * Don't allow console to run command
         */
        if (player == null)
            return Str.ErrorConsole.str();
        /**
         *
         */
        if (!player.hasPermission("invites.invite"))
            return Str.ErrorPermission.str();

        PlayerData data = plugin.getPlayerData(player);

        if (data.getInvites() < 1)
            return Str.ErrorNoInvites.str();

        String target = args[0];
        Server s = plugin.getServer();

        for (OfflinePlayer p: s.getWhitelistedPlayers()) {
            if (p.getName().equalsIgnoreCase(target)) {
                return Str.ErrorInviteExists.format(target);
            }
        }

        if (!s.dispatchCommand(s.getConsoleSender(), "whitelist add "+target))
            return Str.ErrorInvite.str();

        for (OfflinePlayer p: s.getWhitelistedPlayers()) {
            if (p.getName().equalsIgnoreCase(target)) {
                plugin.addPlayer(p, player);
                data.addInvites(-1);
                plugin.save();
                return Str.InviteSuccess.format(p.getName());
            }
        }

        return Str.ErrorInvite.str();

    }


    public String usage(Player player) {
        return usageStr(Str.HelpUse.str(),
                Str.HelpArg.format(Str.Player.str())
        );
    }

}
