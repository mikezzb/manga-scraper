package mangascraper;

import java.util.List;

public abstract class MangaParser {
    public abstract List<Manga> searchManga(String query);
    public abstract List<Manga> nextPage();
    public void downloadChapter(Manga manga, Chapter chapter) {
        String chapterPath = String.format("output/%s/ch_%s", manga.title, chapter.name);
        Downloader.downloadImages(chapter.getChapterPages(), chapterPath);
    }
}