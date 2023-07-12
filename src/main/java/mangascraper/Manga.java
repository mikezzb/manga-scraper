package mangascraper;

import java.util.List;

/**
 * Dataclass of Manga
 */
public abstract class Manga {
    public final String id;
    public final String title;
    public final String thumbnail;
    private String description;
    private String author;
    private String latestChapter;
    private String lang;
    private List<Chapter> chapters;

    public Manga(String id, String title, String thumbnail) {
        this.id = id;
        this.title = title;
        this.thumbnail = thumbnail;
    }

    public Manga(String id, String title, String thumbnail, String description) {
        this(id, title, thumbnail);
        this.description = description;
    }

    public Manga(String id, String title, String thumbnail, String description, String author) {
        this(id, title, thumbnail, description);
        this.author = author;
    }

    public Manga(String id, String title, String thumbnail, String description, String author, String latestChapter) {
        this(id, title, thumbnail, description, author);
        this.latestChapter = latestChapter;
    }

    public Manga(String id, String title, String thumbnail, String description, String author, String latestChapter, String lang) {
        this(id, title, thumbnail, description, author, latestChapter);
        this.lang = lang;
    }

    public String getDescription() { return description; }

    public String getAuthor() { return author; }

    public String getLatestChapter() { return latestChapter; }
    
    public String getLang() { return lang; }

    protected void setChapters(List<Chapter> chapters) {
        this.chapters = chapters;
    }

    protected void appendChapters(List<Chapter> chapters) {
        this.chapters.addAll(chapters);
    }

    public List<Chapter> getChapters() {
        if (chapters == null) {
            setChapters(inflateChapters());
        } else {
            appendChapters(inflateChapters());
        }
        return chapters;
    }

    public Chapter getChapter(String chapterId) {
        for (Chapter chapter : chapters) {
            if (chapter.id == chapterId) {
                chapter.getChapterPages(); // active-init
                return chapter;
            }
        }
        return null;
    };

    protected abstract List<Chapter> inflateChapters();

    @Override
    public String toString() {
        return "Manga{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", thumbnail='" + thumbnail + '\'' +
                ", author='" + author + '\'' +
                // ", description='" + description + '\'' +
                '}';
    }
}
