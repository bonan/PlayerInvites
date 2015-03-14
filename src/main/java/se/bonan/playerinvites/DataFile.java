package se.bonan.playerinvites;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Date: 2015-03-14
 * Time: 04:02
 */
public class DataFile {

    private Map<String, String> invited;
    private Map<String, Integer> invites;

    public Map<String, String> getInvited() {
        if (invited == null)
            invited = new HashMap<String, String>();
        return invited;
    }

    public Map<String, Integer> getInvites() {
        if (invites == null)
            invites = new HashMap<String, Integer>();
        return invites;
    }

    public static DataFile load(File file) throws IOException {
        if (!file.exists()) {
            if (!file.createNewFile())
                throw new IOException("Unable to create file " + file.getPath());
        }

        if (!file.canRead())
            throw new IOException("Unable to read file " + file.getPath());

        FileReader reader = new FileReader(file);
        Gson parser = new Gson();
        DataFile data = parser.fromJson(reader, DataFile.class);
        reader.close();

        if (data == null)
            return new DataFile();
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

        Gson parser = new Gson();
        String json = parser.toJson(this);

        FileWriter writer = new FileWriter(file);
        writer.write(json);
        writer.close();
    }

}
