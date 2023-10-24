import java.util.ArrayList;

public class Itineraire {
    ArrayList<Port> arrets; //Liste ordonnée des ports empruntés par le Container pour sa demande.

    public Itineraire(ArrayList<Port> arrets){
        this.arrets = arrets;
    }

    // Retourne le prochain arret dans l'itinéraire, qui suit pos. Si pos est le dernier arrêt, retourne null.
    public Port prochainArret(Port pos){
        try {
            return this.arrets.get(this.arrets.indexOf(pos)+1);
        } catch (ArrayIndexOutOfBoundsException e){
            return null;
        }
    }
}
