package se.bonan.playerinvites.object;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.bukkit.Server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Date: 2015-03-15
 * Time: 02:07
 */
public class DataFileAdapter extends TypeAdapter<DataFile> {

    private Server server;

    public DataFileAdapter(Server server) {
        this.server = server;
    }

    @Override
    public void write(JsonWriter jsonWriter, DataFile dataFile) throws IOException {
        jsonWriter.beginObject();
        jsonWriter.name("players");
        jsonWriter.beginArray();
        for (String uuid: dataFile.getPlayers().keySet()) {
            Gson parser = new Gson();
            parser.toJson(dataFile.getPlayers().get(uuid), PlayerData.class, jsonWriter);
        }
        jsonWriter.endArray();
        jsonWriter.endObject();
    }

    @Override
    public DataFile read(JsonReader jsonReader) throws IOException {

        Gson parser = new Gson();

        /**
         * Needed to convert from old file format
         */
        Map<String, Integer> invites = new HashMap<>();
        Map<String, String> invited = new HashMap<>();
        Map<String, PlayerData> players = new HashMap<>();
        DataFile data = new DataFile(players);
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            switch(jsonReader.nextName()) {
                case "invites":
                    jsonReader.beginObject();
                    while (jsonReader.hasNext()) {
                        invites.put(jsonReader.nextName(), jsonReader.nextInt());
                    }
                    jsonReader.endObject();
                    break;

                case "invited":
                    jsonReader.beginObject();
                    while (jsonReader.hasNext()) {
                        invited.put(jsonReader.nextName(), jsonReader.nextString());
                    }
                    jsonReader.endObject();
                    break;

                case "players":
                    jsonReader.beginArray();
                    while (jsonReader.hasNext()) {
                        PlayerData d = parser.fromJson(jsonReader, PlayerData.class);
                        players.put(d.getUuid(), d);

                    }
                    jsonReader.endArray();
            }

        }
        jsonReader.endObject();

        for (String uuid: invited.keySet()) {
            String by = invited.get(uuid);
            PlayerData d = new PlayerData(
                    server.getOfflinePlayer(UUID.fromString(uuid)),
                    server.getOfflinePlayer(UUID.fromString(by))
            );
            if (!players.containsKey(uuid)) {
                players.put(uuid, d);
            }
        }

        for (String uuid: invites.keySet()) {
            Integer inv = invites.get(uuid);
            PlayerData d;
            if (!players.containsKey(uuid)) {
                d = new PlayerData(server.getOfflinePlayer(UUID.fromString(uuid)), null);
                players.put(uuid, d);
            } else {
                d = players.get(uuid);
            }
            d.setInvites(inv);
        }

        return data;
    }
}
