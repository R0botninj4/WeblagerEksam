package com.eksam.weblagereksam.BLL.Manager;

import com.eksam.weblagereksam.BE.Box;
import com.eksam.weblagereksam.BE.Document;
import com.eksam.weblagereksam.BE.DocumentMetadata;
import com.eksam.weblagereksam.BE.Page;
import com.eksam.weblagereksam.BE.User;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ExportManager {

    public enum ExportFormat {
        SINGLE_PAGE,
        MULTI_PAGE
    }

    private final DocumentManager documentManager;
    private final PageManager pageManager;
    private final DocumentMetadataManager documentMetadataManager;

    public ExportManager() throws Exception {
        documentManager = new DocumentManager();
        pageManager = new PageManager();
        documentMetadataManager = new DocumentMetadataManager();
    }

    public Path exportBox(Box box, User exportedBy, Path exportParentFolder, ExportFormat exportFormat) throws Exception {
        LocalDateTime exportedAt = LocalDateTime.now();
        Path exportFolder = exportParentFolder.resolve(exportFolderName(box, exportedAt));
        Files.createDirectories(exportFolder);

        List<Document> documents = documentManager.getDocumentsByBoxId(box.getId());

        writeExportInfo(exportFolder, box, exportedBy, exportedAt, documents.size(), exportFormat);

        for (Document document : documents) {
            exportDocument(exportFolder, document, exportFormat);
        }

        return exportFolder;
    }

    private void exportDocument(Path exportFolder, Document document, ExportFormat exportFormat) throws Exception {
        Path documentFolder = exportFolder.resolve("Document_" + number(document.getDocumentNumber()));
        Files.createDirectories(documentFolder);

        List<Page> pages = pageManager.getPagesByDocumentId(document.getId());
        List<DocumentMetadata> metadata = documentMetadataManager.getMetadataByDocumentId(document.getId());

        writeDocumentMetadata(documentFolder, document, metadata, pages, exportFormat);

        if (exportFormat == ExportFormat.MULTI_PAGE) {
            writeMultiPageTiff(documentFolder, document, pages);
        } else {
            writeSinglePageTiffs(documentFolder, pages);
        }
    }

    private void writeExportInfo(
            Path exportFolder,
            Box box,
            User exportedBy,
            LocalDateTime exportedAt,
            int documentCount,
            ExportFormat exportFormat
    ) throws Exception {
        String content = """
                Export information
                ==================
                Exported at: %s
                Exported by: %s
                Exported by user id: %s
                Export format: %s

                Box
                ===
                Box id: %s
                Box number: %s
                Client: %s
                Profile: %s
                Status: %s
                Documents: %s
                """.formatted(
                exportedAt,
                value(exportedBy != null ? exportedBy.getUsername() : null),
                value(exportedBy != null ? exportedBy.getId() : null),
                exportFormatText(exportFormat),
                box.getId(),
                value(box.getBoxNumber()),
                value(box.getClientName()),
                value(box.getProfileName()),
                value(box.getStatus()),
                documentCount
        );

        Files.writeString(exportFolder.resolve("export-info.txt"), content, StandardCharsets.UTF_8);
    }

    private void writeDocumentMetadata(
            Path documentFolder,
            Document document,
            List<DocumentMetadata> metadata,
            List<Page> pages,
            ExportFormat exportFormat
    ) throws Exception {
        StringBuilder builder = new StringBuilder();
        builder.append("Document metadata\n");
        builder.append("=================\n");
        builder.append("Document id: ").append(document.getId()).append("\n");
        builder.append("Document number: ").append(document.getDocumentNumber()).append("\n");
        builder.append("Barcode value: ").append(value(document.getBarcodeValue())).append("\n");
        builder.append("Status: ").append(value(document.getStatus())).append("\n");
        builder.append("Created at: ").append(value(document.getCreatedAt())).append("\n");
        builder.append("Pages: ").append(pages.size()).append("\n\n");
        builder.append("Export format: ").append(exportFormatText(exportFormat)).append("\n\n");

        builder.append("User added metadata\n");
        builder.append("===================\n");
        if (metadata.isEmpty()) {
            builder.append("No metadata added.\n");
        } else {
            for (DocumentMetadata field : metadata) {
                builder.append(value(field.getFieldName()))
                        .append(": ")
                        .append(value(field.getFieldValue()))
                        .append("\n");
            }
        }

        builder.append("\nPages\n");
        builder.append("=====\n");
        for (Page page : pages) {
            builder.append("Page ")
                    .append(page.getUiOrder())
                    .append(" | file=")
                    .append(exportedFileName(document, page, exportFormat))
                    .append(" | barcode=")
                    .append(page.isBarcodePage() ? "yes" : "no")
                    .append(" | rotation=")
                    .append(page.getRotation())
                    .append(" | original name=")
                    .append(value(page.getFileName()))
                    .append("\n");
        }

        Files.writeString(documentFolder.resolve("metadata.txt"), builder.toString(), StandardCharsets.UTF_8);
    }

    private void writeSinglePageTiffs(Path documentFolder, List<Page> pages) throws Exception {
        for (Page page : pages) {
            if (page.getImageData() == null || page.getImageData().length == 0) {
                continue;
            }

            Files.write(documentFolder.resolve(exportPageFileName(page)), page.getImageData());
        }
    }

    private void writeMultiPageTiff(Path documentFolder, Document document, List<Page> pages) throws Exception {
        List<BufferedImage> images = readPageImages(pages);

        if (images.isEmpty()) {
            return;
        }

        ImageWriter writer = getTiffWriter();
        Path outputFile = documentFolder.resolve(exportDocumentFileName(document));

        try (ImageOutputStream output = ImageIO.createImageOutputStream(outputFile.toFile())) {
            writer.setOutput(output);
            writer.prepareWriteSequence(null);

            for (BufferedImage image : images) {
                writer.writeToSequence(new IIOImage(image, null, null), null);
            }

            writer.endWriteSequence();
        } finally {
            writer.dispose();
        }

        int pageCountInExport = countTiffPages(outputFile);
        if (pageCountInExport != images.size()) {
            throw new Exception("Multi-page TIFF export failed. Expected " + images.size() + " pages but found " + pageCountInExport + ".");
        }
    }

    private List<BufferedImage> readPageImages(List<Page> pages) throws Exception {
        List<BufferedImage> images = new ArrayList<>();

        for (Page page : pages) {
            if (page.getImageData() == null || page.getImageData().length == 0) {
                continue;
            }

            BufferedImage image = ImageIO.read(new ByteArrayInputStream(page.getImageData()));
            if (image != null) {
                images.add(image);
            }
        }

        return images;
    }

    private ImageWriter getTiffWriter() throws Exception {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("TIFF");

        if (!writers.hasNext()) {
            throw new Exception("No TIFF writer found.");
        }

        ImageWriter writer = writers.next();

        if (!writer.canWriteSequence()) {
            writer.dispose();
            throw new Exception("The TIFF writer cannot write multi-page TIFF files.");
        }

        return writer;
    }

    private int countTiffPages(Path tiffFile) throws Exception {
        Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("TIFF");

        if (!readers.hasNext()) {
            return -1;
        }

        ImageReader reader = readers.next();

        try (ImageInputStream input = ImageIO.createImageInputStream(tiffFile.toFile())) {
            reader.setInput(input);
            return reader.getNumImages(true);
        } finally {
            reader.dispose();
        }
    }

    private String exportFolderName(Box box, LocalDateTime exportedAt) {
        String timestamp = exportedAt.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return safeFileName("Export_" + box.getBoxNumber() + "_" + timestamp);
    }

    private String exportPageFileName(Page page) {
        return "page_" + number(page.getUiOrder()) + ".tiff";
    }

    private String exportDocumentFileName(Document document) {
        return "document_" + number(document.getDocumentNumber()) + ".tiff";
    }

    private String exportedFileName(Document document, Page page, ExportFormat exportFormat) {
        return exportFormat == ExportFormat.MULTI_PAGE ? exportDocumentFileName(document) : exportPageFileName(page);
    }

    private String exportFormatText(ExportFormat exportFormat) {
        return exportFormat == ExportFormat.MULTI_PAGE ? "TIFF multi-page" : "TIFF single-page";
    }

    private String number(int number) {
        return String.format("%03d", number);
    }

    private String safeFileName(String fileName) {
        return fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    private String value(Object value) {
        return value == null || value.toString().isBlank() ? "-" : value.toString();
    }
}
