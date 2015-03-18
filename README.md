# PlayerInvites
Bukkit plugin allowing MineCraft servers to be whitelisted, where players can add other players to the whitelist.

The idea being that players will be more responsible for who they invite, and the server gets less problems with random players joining just to grief.

## Command usage
| Command | Description | Permission |
|--------------|------------|-----|
| /invite | Shows command help | invites.use |
| /invite use &lt;player&gt; | Uses an invite (adds player to whitelist) | invites.invite |
| /invite buy [amount] | Buys invites with in-game money (Vault integration) | invites.buy |
| /invite show | Shows your invites, who invited you and who you've invited | invites.show |
| /invite show &lt;player&gt; | Shows above details for another player | invites.showother |
| /invite give &lt;player&gt; [amount] | Gives a player new invites | invites.give |

## Automatic invites
Additionally, the plugin can be configured to give invites to players periodically

See [config.yml](src/main/resources/config.yml) for examples and details

## Localization
The plugin can be fully localized. All strings are contained in the [Str class](src/main/java/se/bonan/playerinvites/Str.java).

Any string can be overridden by uncommenting and changing it in config.yml
