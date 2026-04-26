package com.eksam.weblagereksam.BLL.Scanning;

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

    // ===== API setup =====

    // This is the external test API we use instead of a real scanner.
    // In a real company version this could be replaced by scanner hardware,
    // but the rest of the program can still work the same way.
    private static final String BASE_URL = "https://studentiffapi-production.up.railway.app";
    private final HttpClient client = HttpClient.newHttpClient();

    // ===== Public API methods =====

    public byte[] getRandomTiffBytes() throws Exception {
        // The API returns a ZIP file, even when we only ask for one random TIFF.
        // Because of that we first download the ZIP, then unzip it, and finally
        // return the first TIFF file from inside the ZIP.
        byte[] zipData = getBytes("/getRandomFile");
        List<byte[]> files = extractTiffFiles(zipData);

        if (files.isEmpty()) {
            throw new Exception("ZIP file is empty.");
        }

        return files.get(0);
    }

    public List<byte[]> getRandomTiffBatch(int amount) throws Exception {
        // Used by the "scan 10 files" button.
        // The API gives us a ZIP with several TIFF files, and we return them
        // as a list so ScanImportManager can import them one by one.
        byte[] zipData = getBytes("/getFiles/" + amount);
        return extractTiffFiles(zipData);
    }

    // ===== ZIP helper =====

    private List<byte[]> extractTiffFiles(byte[] zipData) throws Exception {
        List<byte[]> files = new ArrayList<>();

        // Reads each file from the ZIP into a byte array.
        //
        // A byte array is just the file content in memory. We use byte arrays here
        // because the database stores the page image in a varbinary(max) column.
        //
        // We keep the files as TIFF bytes, because the project requirement says
        // scanned pages should be saved as TIFF files.
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

    // ===== HTTP helper =====

    private byte[] getBytes(String endpoint) throws Exception {
        // Sends a normal GET request to the API.
        // We ask for bytes instead of text because the response is a ZIP file,
        // not normal JSON or plain text.
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .GET()
                .build();

        HttpResponse<byte[]> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofByteArray()
        );
        //The HTTP 200 OK success status response code indicates that the request has succeeded.
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new Exception("TIFF API returned status " + response.statusCode() + " for " + endpoint);
        }

        return response.body();
    }
}
