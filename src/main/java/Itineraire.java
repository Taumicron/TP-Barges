import java.util.ArrayList;

public class Itineraire {
    ArrayList<Service> arrets; //Liste ordonnée des services empruntés par le Container pour sa demande.

    public Itineraire(ArrayList<Service> arrets){
        this.arrets = arrets;
    }

    // Retourne le prochain arret dans l'itinéraire, qui suit pos. Si pos est le dernier arrêt, retourne null.
    public Service prochainArret(Service pos){
        try {
            return this.arrets.get(this.arrets.indexOf(pos)+1);
        } catch (ArrayIndexOutOfBoundsException e){
            return null;
        }
    }
}
