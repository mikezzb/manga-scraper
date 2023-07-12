package mangascraper;

import java.util.List;

public class Main {
    public static void main(String[] arg) {
        MangaParser parser = ParserMangaDex.getInstance("en");
        // Browse manga list
        List<Manga> mangaList = parser.searchManga("oshi no ko");
        System.out.println("=== Results for query (oshi no ko) ===");
        for (Manga manga : mangaList) {
            System.out.println(manga);
        }
        // Get chapters of a manga (i.e. user clicked in to a manga)
        Manga manga = mangaList.get(0);
        System.out.printf("\n=== Chapters for %s ===\n", manga.title);
        List<Chapter> chapters = manga.getChapters();
        for (Chapter chapter : chapters) {
            System.out.println(chapter);
        }
        // Get images of a manga chapter (i.e. user started to read a chapter)
        Chapter chapter = chapters.get(0);
        String[] chapterPages = chapter.getChapterPages();
        System.out.printf("\n=== Pages for Chapter %s ===\n", chapter.name);
        for (String page : chapterPages) {
            System.out.println("Page: " + page);
        }
        // Download a chapter
        parser.downloadChapter(manga, chapter);
    }
}
