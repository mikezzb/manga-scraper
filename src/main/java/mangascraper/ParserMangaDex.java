package mangascraper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

class MangaMangaDex extends Manga {
    private static final String URL_TEMPLATE = "https://api.mangadex.org/manga/%s/feed?limit=96&offset=0&order[chapter]=asc&translatedLanguage[]=en";
    private static final OkHttpClient client = new OkHttpClient();
    private static final Gson gson = new GsonBuilder().create();

    public MangaMangaDex(String id, String title, String thumbnail, String description, String author) {
        super(id, title, thumbnail, description, author);
    }

    @Override
    protected List<Chapter> inflateChapters() {
        String url = String.format(URL_TEMPLATE, id);
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response);
            }
            List<Chapter> chapterList = new ArrayList<Chapter>();

            // Parse the response body using Gson
            String responseBody = response.body().string();
            JsonObject json = gson.fromJson(responseBody, JsonElement.class).getAsJsonObject();

            // Extract Chapters from raw jsonElement
            JsonArray data = json.getAsJsonArray("data");
            for (JsonElement obj : data) {
                String chapterId = obj.getAsJsonObject().get("id").getAsString();
                JsonObject attrs = obj.getAsJsonObject().getAsJsonObject("attributes");
                String chapterName = attrs.get("chapter").getAsString();
                if (chapterName == null) {
                    chapterName = attrs.get("title").getAsString();
                }
                Chapter chapter = new ChapterMangaDex(
                        chapterId,
                        chapterName);
                chapterList.add(chapter);
            }
            return chapterList;
        } catch (IOException e) {
            // Handle any exceptions that occur during the API call
            e.printStackTrace();
            return null;
        }
    }
}

class ChapterMangaDex extends Chapter {
    private static final String URL_TEMPLATE = "https://api.mangadex.org/at-home/server/%s";
    private static final String IMAGE_URL_TEMPLATE = "https://uploads.mangadex.org/data/%s/%s";
    
    private static final OkHttpClient client = new OkHttpClient();
    private static final Gson gson = new GsonBuilder().create();

    public ChapterMangaDex(String id, String name) {
        super(id, name);
    }

    private static String getPageImageUrl(String hash, String imageId) {
        if (imageId.startsWith("\"") && imageId.endsWith("\"")) {
            imageId = imageId.substring(1, imageId.length() - 1);
        } 
        return String.format(IMAGE_URL_TEMPLATE, hash, imageId);
    }

    @Override
    protected String[] inflateChapterPages() {
        String url = String.format(URL_TEMPLATE, id);
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response);
            }
            // Parse the response body using Gson
            String responseBody = response.body().string();
            JsonObject json = gson.fromJson(responseBody, JsonElement.class).getAsJsonObject();
            JsonObject chapterJson = json.get("chapter").getAsJsonObject();

            // Extract pages from raw jsonElement
            JsonArray data = chapterJson.getAsJsonArray("data");
            String hash = chapterJson.get("hash").getAsString();
            List<String> images = new ArrayList<String>();
            data.forEach(s -> {
                images.add(getPageImageUrl(hash, s.toString()));
            });
            return images.toArray(new String[0]);
        } catch (IOException e) {
            // Handle any exceptions that occur during the API call
            e.printStackTrace();
            return null;
        }
    }
}

public class ParserMangaDex extends MangaParser {
    private static final int PAGE_NUM = 32;
    private static final String BROWSE_URL_TEMPLATE = "https://api.mangadex.org/manga?limit=32&includes[]=cover_art&includes[]=artist&includes[]=author&contentRating[]=safe&contentRating[]=suggestive&contentRating[]=erotica&availableTranslatedLanguage[]=%s&order[rating]=desc&hasAvailableChapters=true";
    private static ParserMangaDex instance;
    private final OkHttpClient client;
    private final Gson gson;
    private final String lang;
    private String query;
    private int offset = 0;

    public void resetState() {
        query = null;
        offset = 0;
    }

    private ParserMangaDex(String lang) {
        client = new OkHttpClient();
        gson = new GsonBuilder().create();
        this.lang = lang;
    }

    public static ParserMangaDex getInstance(String lang) {
        if (instance == null) {
            instance = new ParserMangaDex(lang);
        }
        return instance;
    }

    public static String getCoverUrl(String mangaId, String coverId) {
        return "https://mangadex.org/covers/" + mangaId + "/" + coverId + ".256.jpg";
    }

    public String getUrlFromCurrentQuery() {
        String url = String.format(BROWSE_URL_TEMPLATE, lang);
        if (query != null) {
            url += "&title=" + query;
        }
        if (offset > 0) {
            url += "&offset=" + offset;
        }
        return url;
    }

    private List<Manga> searchByUrl(String url) {
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response);
            }
            List<Manga> mangaList = new ArrayList<Manga>();

            // Parse the response body using Gson
            String responseBody = response.body().string();
            JsonObject json = gson.fromJson(responseBody, JsonElement.class).getAsJsonObject();
            // Extract Manga from raw jsonElement
            JsonArray data = json.getAsJsonArray("data");
            for (JsonElement obj : data) {
                String mangaId = obj.getAsJsonObject().get("id").getAsString();
                JsonElement attrs = obj.getAsJsonObject().getAsJsonObject("attributes");
                String title = "";
                try {
                    title = attrs.getAsJsonObject().get("title").getAsJsonObject().get("en").getAsString();
                } catch (Exception e) {
                    for (Entry<String, JsonElement> entry : attrs.getAsJsonObject().get("title").getAsJsonObject().entrySet()) {
                        title = entry.getValue().getAsString();
                        break;
                    }
                }
                String description;
                try {
                    description = attrs.getAsJsonObject().get("description").getAsJsonObject().get("en")
                        .getAsString();
                } catch (Exception e) {
                    description = "";
                }
                // Loop through relationships to get other attrs
                JsonArray relationships = obj.getAsJsonObject().getAsJsonArray("relationships");
                String author = "";
                String coverId = "";
                for (JsonElement relation : relationships) {
                    String type = relation.getAsJsonObject().get("type").getAsString();
                    if (type.equals("author")) {
                        author = relation.getAsJsonObject().get("attributes").getAsJsonObject().get("name")
                                .getAsString();
                        continue;
                    }
                    if (type.equals("cover_art")) {
                        coverId = relation.getAsJsonObject().get("attributes").getAsJsonObject().get("fileName")
                                .getAsString();
                        continue;
                    }
                }
                Manga manga = new MangaMangaDex(
                        mangaId,
                        title,
                        ParserMangaDex.getCoverUrl(mangaId, coverId),
                        description,
                        author);
                mangaList.add(manga);
            }

            // MangaSearchResult result = gson.fromJson(responseBody,
            // MangaSearchResult.class);
            offset += PAGE_NUM;
            return mangaList;
        } catch (IOException e) {
            // Handle any exceptions that occur during the API call
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Manga> nextPage() {
        String url = getUrlFromCurrentQuery();
        return searchByUrl(url);
    }

    @Override
    public List<Manga> searchManga(String query) {
        resetState();
        this.query = query;
        String url = getUrlFromCurrentQuery();
        return searchByUrl(url);
    }

    public List<Manga> searchLatestChapters(String query) {
        Request request = new Request.Builder()
                .url("https://api.mangadex.org/manga")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response);
            }
            List<Manga> mangaList = new ArrayList<Manga>();

            // Parse the response body using Gson
            String responseBody = response.body().string();
            JsonElement json = gson.fromJson(responseBody, JsonElement.class);
            // Extract Manga from raw jsonElement
            JsonArray data = json.getAsJsonObject().getAsJsonArray("data");
            for (JsonElement obj : data) {
                // Loop through attributes to find Manga
                JsonArray attrs = obj.getAsJsonObject().getAsJsonArray("relationships");
                for (int i = 0; i < attrs.size(); i++) {
                    JsonElement attr = attrs.get(i);
                    if (attr.getAsJsonObject().get("type").getAsString() == "manga") {
                        break;
                    }
                }
            }

            // MangaSearchResult result = gson.fromJson(responseBody,
            // MangaSearchResult.class);
            return mangaList;
        } catch (IOException e) {
            // Handle any exceptions that occur during the API call
            e.printStackTrace();
            return null;
        }
    }
}
