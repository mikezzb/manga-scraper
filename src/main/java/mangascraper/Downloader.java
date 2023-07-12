package mangascraper;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Downloader {
    private static final int MAX_DOWNLOADS_AT_ONCE = 3;
    private static final String ext = ".png";
    public static void downloadImages(String[] pages, String outPath) {
        downloadImages(pages, outPath, MAX_DOWNLOADS_AT_ONCE);
    }
    public static void downloadImages(String[] pages, String outPath, int maxDownloadsAtOnce) {
        // make folder if DNE
        File directory = new File(outPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        // download
        ExecutorService executor = Executors.newFixedThreadPool(maxDownloadsAtOnce);
        List<DownloadTask> tasks = new ArrayList<>();

        for (int i = 0; i < pages.length; i++) {
            DownloadTask task = new DownloadTask(pages[i], outPath, String.valueOf(i));
            tasks.add(task);
            executor.submit(task);
        }

        executor.shutdown();
    }

    private static class DownloadTask implements Runnable {
        private final String url;
        private final String outputDir;

        public DownloadTask(String url, String outputDir, String fileName) {
            this.url = url;
            this.outputDir = outputDir + File.separator + fileName + ext;
        }

        @Override
        public void run() {
            try {
                URL imageUrl = new URL(url);
                InputStream inputStream = imageUrl.openStream();
                OutputStream outputStream = new FileOutputStream(outputDir);

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                inputStream.close();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}