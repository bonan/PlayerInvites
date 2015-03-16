# InvitesPlugin
Bukkit plugin allowing MineCraft servers to be whitelisted, where players can add other players to the whitelist.

The idea being that players will be more responsible for who they invite, and the server gets less problems with random players joining just to grief.

## Command usage
| Command | Description | Permission |
|--------------|------------|-----|
| /invite | Shows command help | invites.use |
| /invite use <player> | Uses an invite (adds player to whitelist) | invites.invite |
| /invite buy [amount] | Buys invites with in-game money (Vault integration) | invites.buy |
| /invite show | Shows your invites, who invited you and who you've invited | invites.show |
| /invite show <player> | Shows above details for another player | invites.showother |
| /invite give <player> [amount] | Gives a player new invites | invites.give |

## Automatic invites
Additionally, the plugin can be configured to give invites to players periodically

See [config.yml](src/main/resources/config.yml) for examples and details
