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
    public String post(String text, boolean raw) throws IOException {
        byte[] postData = text.getBytes(StandardCharsets.UTF_8);
        HttpsURLConnection conn = getHttpsURLConnection(postData);

        String response = "e";
        DataOutputStream wr;
        try {
            wr = new DataOutputStream(conn.getOutputStream());
            wr.write(postData);
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            response = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (response.contains("key")) {
            response = response.substring(response.indexOf(":") + 2, response.length() - 2);

            String postURL = "https://paste.helpch.at/";
            response = postURL + response;
        }

        return response;
    }

    private static @NotNull HttpsURLConnection getHttpsURLConnection(byte[] postData) throws IOException {
        int postDataLength = postData.length;

        String requestURL = "https://paste.helpch.at/documents";
        URL url = new URL(requestURL);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setInstanceFollowRedirects(false);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("User-Agent", "Hastebin Java API");
        conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
        conn.setUseCaches(false);
        return conn;
    }
}
