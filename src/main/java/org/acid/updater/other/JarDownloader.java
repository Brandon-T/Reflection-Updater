package org.acid.updater.other;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by Brandon on 2014-12-06.
 */
public class JarDownloader {
    private final HashMap<String, String> parameters = new HashMap<>();

    public JarDownloader(String world, String gamePack) {
        try {
            HTTPSocket socket = new HTTPSocket(world + "/jav_config.ws");
            String[] lines = socket.getPage(null).replaceAll("param=|msg=", "").split("\r|\n|\r\n"); //param=|msg=(.*?)\r|\n|\r\n

            for (String line : lines) {
                if (line.length() > 0) {
                    int idx = line.indexOf("=");
                    parameters.put(line.substring(0, idx), line.substring(idx + 1));
                }
            }

            socket = new HTTPSocket(parameters.get("codebase") + parameters.get("initial_jar"));
            byte[] Buffer = socket.getRaw(null);
            try (FileOutputStream fos = new FileOutputStream(gamePack)) {
                fos.write(Buffer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
