package com.eksam.weblagereksam.BLL;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class TiffApiClient {

    private static final String BASE_URL = "https://studentiffapi-production.up.railway.app";
    private final HttpClient client = HttpClient.newHttpClient();

    public byte[] getRandomTiffBytes() throws Exception {
        byte[] zipData = getBytes("/getRandomFile");

        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipData));
             ByteArrayOutputStream tiffOut = new ByteArrayOutputStream()) {

            ZipEntry entry = zis.getNextEntry();

            if (entry == null) {
                throw new Exception("ZIP file is empty.");
            }

            byte[] buffer = new byte[4096];
            int len;

            while ((len = zis.read(buffer)) > 0) {
                tiffOut.write(buffer, 0, len);
            }

            zis.closeEntry();
            return tiffOut.toByteArray();
        }
    }

    public List<byte[]> getRandomTiffBatch(int amount) throws Exception {
        byte[] zipData = getBytes("/getFiles/" + amount);
        List<byte[]> files = new ArrayList<>();

        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipData))) {
            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }

                ByteArrayOutputStream tiffOut = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int len;

                while ((len = zis.read(buffer)) > 0) {
                    tiffOut.write(buffer, 0, len);
                }

                files.add(tiffOut.toByteArray());
                zis.closeEntry();
            }
        }

        return files;
    }

    public int getCount() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/getCount"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new Exception("TIFF API returned status " + response.statusCode() + " for /getCount");
        }

        return Integer.parseInt(response.body().trim());
    }

    private byte[] getBytes(String endpoint) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .GET()
                .build();

        HttpResponse<byte[]> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofByteArray()
        );

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new Exception("TIFF API returned status " + response.statusCode() + " for " + endpoint);
        }

        return response.body();
    }
}
