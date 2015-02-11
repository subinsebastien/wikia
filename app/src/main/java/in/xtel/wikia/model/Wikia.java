package in.xtel.wikia.model;

/**
 * Created by napster on 10/02/15.
 * This is base model class for this project.
 * Keeping this class completely public and simplistic.
 * This structure ensures easy access while object creation
 * is completely done though GSON from JSON objects from the API.
 */
public class Wikia {
    public int id;
    public String wordmark;
    public String desc;
    public String title;
    public String url;
    public String image;
}
