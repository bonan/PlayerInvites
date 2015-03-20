package se.bonan.playerinvites.commands;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import se.bonan.playerinvites.PlayerInvites;
import se.bonan.playerinvites.Str;
import se.bonan.playerinvites.object.PlayerData;

import java.util.*;
import java.util.logging.Level;

/**
 * User: Björn
 * Date: 2015-03-19
 * Time: 22:39
 */
public class TopCommand extends CommandBase implements CommandInterface {

    private final Integer topInvites;
    private final Integer topInvited;

    public TopCommand(String name, PlayerInvites plugin) {
        super(name, plugin, "invites.toplist");
        this.topInvites = plugin.getConfig().getInt("topInvites", 5);
        this.topInvited = plugin.getConfig().getInt("topInvited", 5);

    }

    @Override
    public String invoke(Player player, String[] args) {
        if (args.length > 0)
            return usage(player);

        Map<String,Integer> invited = new HashMap<>();
        Map<String,Integer> invites = new HashMap<>();

        for (PlayerData data: plugin.getData().getPlayers().values()) {
            OfflinePlayer p;
            if (data.getInvitedBy() != null) {
                int cur = 0;
                p = plugin.getServer().getOfflinePlayer(UUID.fromString(data.getInvitedBy()));
                if (p != null && p.getName() != null) {
                    if (invited.containsKey(data.getInvitedBy()))
                        cur = invited.get(data.getInvitedBy());
                    invited.put(data.getInvitedBy(), cur+1);
                }
            }
            p = plugin.getServer().getOfflinePlayer(UUID.fromString(data.getUuid()));

            if (p != null && p.getName() != null && data.getInvites() != null) {
                invites.put(data.getUuid(), data.getInvites());
            }
        }

        String invited_str = topFromMap(invited, topInvited);
        String invites_str = topFromMap(invites, topInvites);

        StringBuilder ret = new StringBuilder();
        if (invited_str.length() > 0) {
            ret.append(Str.TopInvited.format(invited_str));
        }

        if (invites_str.length() > 0) {
            if (ret.length() > 0)
                ret.append("\n");
            ret.append(Str.TopInvites.format(invites_str));
        }

        if (ret.length() == 0) {
            ret.append(Str.ErrorNoData.str());
        }

        return ret.toString();
    }

    @Override
    public String usage(Player player) {
        return usageStr(Str.HelpTop.str(), "");
    }

    private class CompareInt implements Comparator<Object> {
        @Override
        public int compare(Object o1, Object o2) {
            return ((Map.Entry<String,Integer>) o2).getValue().compareTo(
                    ((Map.Entry<String,Integer>) o1).getValue());
        }
    }

    private String topFromMap(Map<String,Integer> map, Integer max) {
        Object[] sort = map.entrySet().toArray();
        Arrays.sort(sort, new CompareInt());
        StringBuilder players = new StringBuilder();

        for (int i = 0; i < max; i++) {
            if (sort.length > i) {
                Map.Entry<String,Integer> entry = (Map.Entry<String, Integer>) sort[i];
                if (players.length() > 0)
                    players.append(", ");

                OfflinePlayer p = plugin.getServer().getOfflinePlayer(UUID.fromString(entry.getKey()));
                if (p != null && p.getName() != null) {
                    players.append(
                            Str.TopPlayer.format(
                                    p.getName(),
                                    String.valueOf(entry.getValue())
                            )
                    );
                }
            }
        }

        return players.toString();
    }

}
