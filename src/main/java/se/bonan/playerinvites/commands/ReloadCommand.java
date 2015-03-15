package se.bonan.playerinvites.commands;

import org.bukkit.entity.Player;
import se.bonan.playerinvites.PlayerInvites;
import se.bonan.playerinvites.Str;

/**
 * Reloads the plugin configuration and data
 *
 * Usage:
 *   /invite reload
 *
 * Permission:
 *   invites.reload - Allows player to issue command
 */
public class ReloadCommand extends CommandBase implements CommandInterface
{

    public ReloadCommand(String name, PlayerInvites plugin) {
        super(name, plugin, "invites.reload");
    }

    @Override
    public String invoke(Player player, String[] args) {
        /**
         * Reloads config & data
         */
        String ret = plugin.reload();

        /**
         * reload() returns empty string on success
         */
        if (ret.equals(""))
            return Str.ReloadSuccess.str();

        /**
         * Show error to command sender
         */
        return ret;
    }

    @Override
    public String usage(Player player) {
        return usageStr(Str.HelpReload.str());
    }
}
