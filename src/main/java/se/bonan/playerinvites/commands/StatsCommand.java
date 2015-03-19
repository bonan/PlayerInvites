package se.bonan.playerinvites.commands;

import org.bukkit.entity.Player;
import se.bonan.playerinvites.PlayerInvites;
import se.bonan.playerinvites.Str;
import se.bonan.playerinvites.exception.PlayerNotFoundException;
import se.bonan.playerinvites.object.PlayerData;

/**
 * User: Björn
 * Date: 2015-03-19
 * Time: 22:29
 */
public class StatsCommand extends CommandBase implements CommandInterface {

    public StatsCommand(String name, PlayerInvites plugin) {
        super(name, plugin, "invites.stats");
    }

    @Override
    public String invoke(Player player, String[] args) {
        if (args.length > 0)
            return usage(player);

        Integer invites = 0;
        Integer invited = 0;
        Integer players = 0;

        for (PlayerData data: plugin.getData().getPlayers().values()) {
            if (data.getInvitedBy() != null) {
                invited++;
            }
            if (data.getInvites() > 0) {
                invites += data.getInvites();
                players++;
            }
        }

        return Str.StatsInvited.format(invited.toString()) + "\n" +
                Str.StatsInvites.format(players.toString(), invites.toString());

    }

    @Override
    public String usage(Player player) {
        return usageStr(Str.HelpStats.str(), "");
    }
}
