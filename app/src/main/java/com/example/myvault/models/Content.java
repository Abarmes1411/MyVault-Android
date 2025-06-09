package com.example.myvault.models;

import com.example.myvault.enums.Categories;

import java.io.Serializable;
import java.util.List;

public class Content extends DomainEntity implements Serializable {

    // Atributos comunes entre contenidos
    private String categoryID;
    private String title;
    private String description;
    private String releaseDate;
    private String coverImage;
    private String rating;
    private String source;
    private String origin;
    private String originalTitle;
    private UserReview userReview;



    // Atributos para peliculas
    private String tmdbID;
    private List<String> genresTMDB;

    // Atributos para series
    private String tmdbTVID;
    private List<String> genresTVTMDB;

    // Atributos para libros
    private String bookID;
    private String publisher;
    private List<String> authors;
    private boolean isEbook;
    private String saleability;
    private String pages;
    private String language;
    private String retailPrice;
    private String currency;

    // Atributos para juegos
    private String gameID;
    private List<String> platforms;
    private String website;
    private List<String> genresGame;
    private List<String> developers;
    private String added;

    // Atributos para animes
    private String animeID;
    private String episodes;
    private List<String> genresAnime;
    private List<String> studios;

    // Atributos para mangas
    private String mangaID;
    private List<String> genresManga;
    private String popularity;

    public Content() {
    }

    // Constructor general
    public Content(String categoryID, String title, String description, String releaseDate, String rating, String coverImage, String source) {
        super();
        this.categoryID = categoryID;
        this.title = title;
        this.description = description;
        this.releaseDate = releaseDate;
        this.rating = rating;
        this.coverImage = coverImage;
        this.source = source;
    }


    //Constructor para el search
    public Content(String tmdbID, String title, String date, String image, Categories category) {
        super();
        this.tmdbID = tmdbID;
        this.title = title;
        this.releaseDate = date;
        this.coverImage = image;
    }

    // Constructor para peliculas y series
    public Content(String categoryID, String title, String description, String releaseDate, List<String> genresTMDB, String rating, String coverImage, String source, String tmdbID, String tmdbTVID) {
        super();
        this.categoryID = categoryID;
        this.title = title;
        this.description = description;
        this.releaseDate = releaseDate;
        this.genresTMDB = genresTMDB;
        this.rating = rating;
        this.coverImage = coverImage;
        this.source = source;

        // Dependiendo de si es una película o una serie, asignar el ID correspondiente
        if (tmdbID != null) {
            this.tmdbID = tmdbID;
        } else {
            this.tmdbTVID = tmdbTVID;
        }
    }


    // Constructor para animes
    public Content(String categoryID, String title, String description, String releaseDate, String rating, String coverImage, String source, String episodes, List<String> genresAnime, List<String> studios, String animeID) {
        super();
        this.categoryID = categoryID;
        this.title = title;
        this.description = description;
        this.releaseDate = releaseDate;
        this.rating = rating;
        this.coverImage = coverImage;
        this.source = source;
        this.episodes = episodes;
        this.genresAnime = genresAnime;
        this.studios = studios;
        this.animeID = animeID;
    }

    // Constructor para mangas y novelas
    public Content(String categoryID, String title, String originalTitle, String description, String releaseDate, String rating, String coverImage, String source, List<String> genresManga, String popularity, String mangaID) {
        super();
        this.categoryID = categoryID;
        this.title = title;
        this.originalTitle = originalTitle;
        this.description = description;
        this.releaseDate = releaseDate;
        this.rating = rating;
        this.coverImage = coverImage;
        this.source = source;
        this.genresManga = genresManga;
        this.popularity = popularity;
        this.mangaID = mangaID;
    }


    // Constructor para videojuegos
    public Content(String categoryID, String title, String description, String releaseDate, String rating, String coverImage, String source, List<String> platforms, String website, List<String> genresGame, List<String> developers, String added, String gameID) {
        super();
        this.categoryID = categoryID;
        this.title = title;
        this.description = description;
        this.releaseDate = releaseDate;
        this.rating = rating;
        this.coverImage = coverImage;
        this.source = source;
        this.platforms = platforms;
        this.website = website;
        this.genresGame = genresGame;
        this.developers = developers;
        this.added = added;
        this.gameID = gameID;
    }


    // Constructor para libros
    public Content(String categoryID, String title, String description, String releaseDate, String coverImage, String rating, String source, String origin, String publisher, List<String> authors, boolean isEbook, String saleability, String pages, String language, String retailPrice, String currency, String bookID) {
        this.categoryID = categoryID;
        this.title = title;
        this.description = description;
        this.releaseDate = releaseDate;
        this.coverImage = coverImage;
        this.rating = rating;
        this.source = source;
        this.origin = origin;
        this.publisher = publisher;
        this.authors = authors;
        this.isEbook = isEbook;
        this.saleability = saleability;
        this.pages = pages;
        this.language = language;
        this.retailPrice = retailPrice;
        this.currency = currency;
        this.bookID = bookID;
    }


    // Getters y Setters




    public String getCategoryID() {
        return categoryID;
    }

    public void setCategoryID(String categoryID) {
        this.categoryID = categoryID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public List<String> getGenresTMDB() {
        return genresTMDB;
    }

    public void setGenresTMDB(List<String> genresTMDB) {
        this.genresTMDB = genresTMDB;
    }

    public String getEpisodes() {
        return episodes;
    }

    public void setEpisodes(String episodes) {
        this.episodes = episodes;
    }

    public List<String> getGenresAnime() {
        return genresAnime;
    }

    public void setGenresAnime(List<String> genresAnime) {
        this.genresAnime = genresAnime;
    }

    public List<String> getStudios() {
        return studios;
    }

    public void setStudios(List<String> studios) {
        this.studios = studios;
    }

    public List<String> getGenresManga() {
        return genresManga;
    }

    public void setGenresManga(List<String> genresManga) {
        this.genresManga = genresManga;
    }

    public String getPopularity() {
        return popularity;
    }

    public void setPopularity(String popularity) {
        this.popularity = popularity;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    public List<String> getPlatforms() {
        return platforms;
    }

    public void setPlatforms(List<String> platforms) {
        this.platforms = platforms;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public List<String> getGenresGame() {
        return genresGame;
    }

    public void setGenresGame(List<String> genresGame) {
        this.genresGame = genresGame;
    }

    public List<String> getDevelopers() {
        return developers;
    }

    public void setDevelopers(List<String> developers) {
        this.developers = developers;
    }

    public String getAdded() {
        return added;
    }

    public void setAdded(String added) {
        this.added = added;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    public boolean isEbook() {
        return isEbook;
    }

    public void setEbook(boolean ebook) {
        isEbook = ebook;
    }

    public String getSaleability() {
        return saleability;
    }

    public void setSaleability(String saleability) {
        this.saleability = saleability;
    }

    public String getPages() {
        return pages;
    }

    public void setPages(String pages) {
        this.pages = pages;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getRetailPrice() {
        return retailPrice;
    }

    public void setRetailPrice(String retailPrice) {
        this.retailPrice = retailPrice;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public List<String> getGenresTVTMDB() {
        return genresTVTMDB;
    }

    public void setGenresTVTMDB(List<String> genresTVTMDB) {
        this.genresTVTMDB = genresTVTMDB;
    }

    public UserReview getUserReview() {
        return userReview;
    }

    public void setUserReview(UserReview userReview) {
        this.userReview = userReview;
    }

    @Override
    public String toString() {
        return "Content{" +
                "categoryID='" + categoryID + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", releaseDate='" + releaseDate + '\'' +
                ", coverImage='" + coverImage + '\'' +
                ", rating='" + rating + '\'' +
                ", source='" + source + '\'' +
                ", origin='" + origin + '\'' +
                ", originalTitle='" + originalTitle + '\'' +
                ", tmdbID='" + tmdbID + '\'' +
                ", genresTMDB=" + genresTMDB +
                ", tmdbTVID='" + tmdbTVID + '\'' +
                ", genresTVTMDB=" + genresTVTMDB +
                ", bookID='" + bookID + '\'' +
                ", publisher='" + publisher + '\'' +
                ", authors=" + authors +
                ", isEbook=" + isEbook +
                ", saleability='" + saleability + '\'' +
                ", pages='" + pages + '\'' +
                ", language='" + language + '\'' +
                ", retailPrice='" + retailPrice + '\'' +
                ", currency='" + currency + '\'' +
                ", gameID='" + gameID + '\'' +
                ", platforms=" + platforms +
                ", website='" + website + '\'' +
                ", genresGame=" + genresGame +
                ", developers=" + developers +
                ", added='" + added + '\'' +
                ", animeID='" + animeID + '\'' +
                ", episodes='" + episodes + '\'' +
                ", genresAnime=" + genresAnime +
                ", studios=" + studios +
                ", mangaID='" + mangaID + '\'' +
                ", genresManga=" + genresManga +
                ", popularity='" + popularity + '\'' +
                '}';
    }


    // IDs de los contenidos para evitar duplicados principalmente

    public String getTmdbID() {
        return tmdbID;
    }

    public void setTmdbID(String tmdbID) {
        this.tmdbID = tmdbID;
    }

    public String getBookID() {
        return bookID;
    }

    public void setBookID(String bookID) {
        this.bookID = bookID;
    }

    public String getGameID() {
        return gameID;
    }

    public void setGameID(String gameID) {
        this.gameID = gameID;
    }

    public String getTmdbTVID() {
        return tmdbTVID;
    }

    public void setTmdbTVID(String tmdbTVID) {
        this.tmdbTVID = tmdbTVID;
    }

    public String getAnimeID() {
        return animeID;
    }

    public void setAnimeID(String animeID) {
        this.animeID = animeID;
    }

    public String getMangaID() {
        return mangaID;
    }

    public void setMangaID(String mangaID) {
        this.mangaID = mangaID;
    }
}
