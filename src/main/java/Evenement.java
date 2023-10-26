import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Evenement {
    private Integer type;   // 0 : départ d'un container dans le système
                    // 1 : Déplacement d'un container dans le système
                    // 2 : Retrait d'un container dans le système (arrivé à destination)
    private Integer temps; // En demi-journées, temps auquel l'évènement occurera
    private Container container; // Le container concerné par l'évènement.

    private Integer nbContainers; // type=0 : Le nombre de containers qui doivent être créés par la demande.

    private Port from; // type=0 : le Port où ajouter le container
                            //type=1 : le Port où est actuellement le container
                            //type=2 : le Port où supprimer le container.
    private Port to; // type=1 : Le port où le container va

    private Itineraire itineraire; // type=0 : l'itinéraire du container.

    private Bateau bateau; // Bateau concerné par l'évènement


    // En cas de nouvelle demande (type = 0)
    public Evenement(int temps, int nbContainers, Itineraire i){
        this.type = 0;
        this.temps = temps;
        this.from = i.arrets.get(0);
        this.nbContainers = nbContainers;
        this.itineraire = i;
    }

    //En cas de décrémentation de NbContainers
    public Evenement( Evenement e, int temps, int nbContainers){
        this.type = 0;
        this.temps = temps;
        this.from = e.getItineraire().arrets.get(0);
        this.nbContainers = nbContainers - 1;
        this.itineraire = e.getItineraire();
    }

    //Placement du container sur un port du bateau.
    public Evenement(int temps, Container c, Port p){
        this.type = 1;
        this.temps = temps;
        this.container = c;
        this.to = p;
    }

    //Détache d'un bateau
    public Evenement(int temps, Bateau b){
        this.type = 2;
        this.temps = temps;
        this.bateau = b;
        this.from = b.getPortAttache();
        this.to = b.getDestination();
    }

    //Rattachement du bateau au port.
    public Evenement(int temps, Bateau b, Port dest){
        this.type = 3;
        this.temps = temps;
        this.bateau = b;
        this.to = b.getDestination();
    }

    // Vidage d'un container du bateau sur le port.
    public Evenement(Bateau b, int temps){
        this.type = 4;
        this.bateau = b;
        this.to = b.getPortAttache();
        this.temps = temps;
    }

    //En cas de suppression d'un container
    public Evenement(Container c, int temps){
        this.type = 5;
        this.temps = temps;
        this.from = c.getPosition();
        this.container = c;
    }
}
