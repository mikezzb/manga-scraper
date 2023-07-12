# manga-scraper
MangaDex scraper & downloader

### Example

```java
// Create a MangaDex scraper
MangaParser parser = ParserMangaDex.getInstance("en");
// Browse manga list
List<Manga> mangaList = parser.searchManga("oshi no ko");
// Get chapters of a manga
Manga manga = mangaList.get(0);
List<Chapter> chapters = manga.getChapters();
// Get images of a manga chapter
Chapter chapter = chapters.get(0);
String[] chapterPages = chapter.getChapterPages();
// Download a chapter
parser.downloadChapter(manga, chapter);
```