import java.util.ArrayList;

public class Itineraire {
    ArrayList<Service> arrets; //Liste ordonnée des services empruntés par le Container pour sa demande.

    public Itineraire(ArrayList<Service> arrets){
        this.arrets = arrets;
    }
}
