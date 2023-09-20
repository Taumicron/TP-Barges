import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Demande {
    private Integer nbConteneurs;
    private Itineraire itineraire;

    public Demande(int nbConteneurs, Itineraire i){
        this.nbConteneurs = nbConteneurs;
        this.itineraire = i;
    }
}
