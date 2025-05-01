package com.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Storage {
    private final ObjectMapper mapper = new ObjectMapper();

    public List<Document> loadPreviousData(SiteConfig config) {
        try {
            File file = new File(config.getFilePath());
            if (file.exists()) {
                return mapper.readValue(file, mapper.getTypeFactory().constructCollectionType(List.class, Document.class));
            }
        } catch (IOException e) {
            System.err.println("Eroare la citirea datelor pentru " + config.getName() + ": " + e.getMessage());
        }
        return new ArrayList<>();
    }

    public void saveData(SiteConfig config, List<Document> documents) {
        try {
            File file = new File(config.getFilePath());
            file.getParentFile().mkdirs();
            mapper.writeValue(file, documents);
        } catch (IOException e) {
            System.err.println("Eroare la salvarea datelor pentru " + config.getName() + ": " + e.getMessage());
        }
    }

    public List<Document> findNewDocuments(List<Document> current, List<Document> previous) {
        List<Document> newDocuments = new ArrayList<>(current);
        newDocuments.removeAll(previous);
        return newDocuments;
    }
}