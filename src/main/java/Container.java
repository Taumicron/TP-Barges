import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Container {
    private Itineraire itineraire; // Itinéraire du container
    private Service from; // Service depuis lequel le container est envoyé
    private Service to; //Service final vers lequel le container est envoyé.
    private Service position; // Position actuelle, -1 = pas encore dans la simulation
    public Container (Itineraire i, Service position){
        assert position.getCapacite().size() < position.getCs();
        this.itineraire = i;
        this.from = i.arrets.get(0);
        this.to = i.arrets.get(i.arrets.size()-1);
        this.position = position;
    }

    //Retourne le prochain Service où le Container devra s'arrêter sur son itinéraire.
    public Service prochainArret(Service pos){
        return this.itineraire.prochainArret(pos);
    }
}
