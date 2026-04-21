package com.eksam.weblagereksam;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class TiffApiTester extends Application {

    private final String BASE_URL =
            "https://studentiffapi-production.up.railway.app";

    private final HttpClient client = HttpClient.newHttpClient();

    private ImageView imageView = new ImageView();

    @Override
    public void start(Stage stage) {

        imageView.setFitWidth(400);
        imageView.setFitHeight(400);
        imageView.setPreserveRatio(true);

        Button btn = new Button("Load Random TIFF");
        btn.setOnAction(e -> loadRandomImage());

        VBox root = new VBox(10, btn, imageView);

        Scene scene = new Scene(root, 500, 500);

        stage.setTitle("TIFF Viewer (Memory Only)");
        stage.setScene(scene);
        stage.show();
    }

    // =========================
    // 🚀 MEMORY ONLY LOADER
    // =========================
    private void loadRandomImage() {
        try {
            byte[] zipData = getBytes("/getRandomFile");

            // 📦 ZIP directly from memory
            ZipInputStream zis =
                    new ZipInputStream(new ByteArrayInputStream(zipData));

            ZipEntry entry = zis.getNextEntry();

            if (entry == null) {
                System.out.println("ZIP is empty");
                return;
            }

            // 🧠 Read TIFF fully into memory
            ByteArrayOutputStream tiffOut = new ByteArrayOutputStream();

            byte[] buffer = new byte[4096];
            int len;

            while ((len = zis.read(buffer)) > 0) {
                tiffOut.write(buffer, 0, len);
            }

            zis.closeEntry();
            zis.close();

            byte[] tiffBytes = tiffOut.toByteArray();

            // 🖼 Decode TIFF (needs ImageIO + TwelveMonkeys)
            java.awt.image.BufferedImage img =
                    javax.imageio.ImageIO.read(
                            new ByteArrayInputStream(tiffBytes)
                    );

            if (img == null) {
                System.out.println("Cannot decode TIFF");
                return;
            }

            // 🔄 Convert to PNG in memory
            ByteArrayOutputStream pngOut = new ByteArrayOutputStream();

            javax.imageio.ImageIO.write(img, "png", pngOut);

            Image fxImage = new Image(
                    new ByteArrayInputStream(pngOut.toByteArray())
            );

            imageView.setImage(fxImage);

            System.out.println("work");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =========================
    // 🌐 API CALL
    // =========================
    private byte[] getBytes(String endpoint) throws Exception {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .GET()
                .build();

        HttpResponse<byte[]> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofByteArray()
        );

        return response.body();
    }

    public static void main(String[] args) {
        launch();
    }
}