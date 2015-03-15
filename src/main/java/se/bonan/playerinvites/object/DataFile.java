package se.bonan.playerinvites.object;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.Server;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DataFile {

    private Map<String, PlayerData> players;

    public DataFile(Map<String, PlayerData> players) {
        this.players = players;
    }

    public Map<String, PlayerData> getPlayers() {
        if (players == null)
            players = new HashMap<>();
        return players;
    }

    public static DataFile load(File file, Server server) throws IOException {
        if (!file.exists()) {
            if (!file.createNewFile())
                throw new IOException("Unable to create file " + file.getPath());
        }

        if (!file.canRead())
            throw new IOException("Unable to read file " + file.getPath());

        FileReader reader = new FileReader(file);
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(DataFile.class, new DataFileAdapter(server));
        Gson parser = gsonBuilder.create();
        DataFile data = parser.fromJson(reader, DataFile.class);
        reader.close();

        if (data == null)
            return new DataFile(null);
        return data;
    }

    public void save(File file) throws IOException {

        if (!file.exists()) {
            if (!file.createNewFile())
                throw new IOException("Unable to create file " + file.getPath());
        }

        if (!file.canRead())
            throw new IOException("Unable to read file " + file.getPath());

        if (!file.canWrite())
            throw new IOException(file.getPath() + " is not writeable");

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(DataFile.class, new DataFileAdapter(null));
        Gson parser = gsonBuilder.create();
        String json = parser.toJson(this);

        FileWriter writer = new FileWriter(file);
        writer.write(json);
        writer.close();
    }

}
