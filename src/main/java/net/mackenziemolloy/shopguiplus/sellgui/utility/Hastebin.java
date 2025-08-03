package net.mackenziemolloy.shopguiplus.sellgui.utility;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import javax.net.ssl.HttpsURLConnection;

public class Hastebin {
    private static final String BASE_URL = "https://paste.helpch.at/";
    private static final String API_URL = "https://paste.helpch.at/documents";

    public String post(String text, boolean raw) throws IOException {
        byte[] postData = text.getBytes(StandardCharsets.UTF_8);
        HttpsURLConnection conn = getHttpsURLConnection(postData);

        String response = "";
        DataOutputStream wr;

        try {
            wr = new DataOutputStream(conn.getOutputStream());
            wr.write(postData);
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String readLine = reader.readLine();
            response = readLine.contains("key") ? BASE_URL + readLine.substring(readLine.indexOf(":") + 2, readLine.length() - 2) : readLine;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return response;
    }

    private static @NotNull HttpsURLConnection getHttpsURLConnection(byte[] postData) throws IOException {
        URL url = new URL(API_URL);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

        conn.setDoOutput(true);
        conn.setInstanceFollowRedirects(false);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("User-Agent", "Hastebin Java API");
        conn.setRequestProperty("Content-Length", Integer.toString(postData.length));
        conn.setUseCaches(false);

        return conn;
    }
}
