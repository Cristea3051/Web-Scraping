package com.parser;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        // Definirea configurațiilor pentru site-uri
        List<SiteConfig> sites = Arrays.asList(
                new SiteConfig(
                        "DPRP",
                        "https://dprp.gov.ro/web/rezultate-sesiune-de-finantare-2020/",
                        ".entry-content a",
                        "data/dprp.json"
                ),
                new SiteConfig(
                        "ONIPM",
                        "https://onipm.gov.md/proiecte-programe",
                        ".content a",
                        "data/alt_site.json"
                )
                // Adaugă alte site-uri aici
        );

        // Inițializează componentele
        Scraper scraper = new Scraper();
        Storage storage = new Storage();
        Notifier notifier = new Notifier();

        // Configurează scheduler-ul
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        Runnable task = () -> {
            for (SiteConfig config : sites) {
                try {
                    System.out.println("Verific actualizări pe " + config.getName() + "...");
                    List<Document> currentDocuments = scraper.scrape(config);
                    List<Document> previousDocuments = storage.loadPreviousData(config);
                    List<Document> newDocuments = storage.findNewDocuments(currentDocuments, previousDocuments);

                    if (!newDocuments.isEmpty()) {
                        System.out.println("S-au găsit " + newDocuments.size() + " documente noi pe " + config.getName());
                        notifier.sendEmailNotification(config, newDocuments);
                        storage.saveData(config, currentDocuments);
                    } else {
                        System.out.println("Nicio actualizare pe " + config.getName());
                    }
                } catch (Exception e) {
                    System.err.println("Eroare la verificare pe " + config.getName() + ": " + e.getMessage());
                }
            }
        };

        // Rulează task-ul imediat și apoi la fiecare 6 ore
        scheduler.scheduleAtFixedRate(task, 0, 6, TimeUnit.HOURS);

        // Păstrează programul activ
        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            scheduler.shutdown();
        }
    }
}
