import java.util.Date;

public class Track {
    private String name;
    private String artist;
    private Date data;

    public Track(String name,String artist){
        this.name = name;
        this.artist = artist;
        data = new Date();
    }

    public Date getData() {
        return data;
    }

    public String getArtist() {
        return artist;
    }

    public String getName() {
        return name;
    }
}
