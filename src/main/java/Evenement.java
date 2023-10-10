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

    private Service from; // type=0 : le Service où ajouter le container
                            //type=1 : le Service où est actuellement le container
                            //type=2 : le Service où supprimer le container.
    private Service to; // type=1 : Le service où le container va

    private Itineraire itineraire; // type=0 : l'itinéraire du container.


    // En cas de nouvelle demande (type = 0)
    public Evenement( Demande d, int temps){
        this.type = 0;
        this.temps = temps;
        this.from = d.getItineraire().arrets.get(0);
        this.nbContainers = d.getNbConteneurs();
        this.itineraire = d.getItineraire();
    }

    //En cas de décrémentation de NbContainers
    public Evenement( Evenement e, int temps, int nbContainers){
        this.type = 0;
        this.temps = temps;
        this.from = e.getItineraire().arrets.get(0);
        this.nbContainers = nbContainers - 1;
        this.itineraire = e.getItineraire();
    }

    // En cas de déplacement d'un container
    public Evenement(Container c, Service from, Service to, int temps){
        this.type = 1;
        this.temps = temps;
        this.from = from;
        this.to = to;
        this.container = c;
    }

    //En cas de suppression d'un container
    public Evenement(Container c, int temps){
        this.type = 2;
        this.temps = temps;
        this.from = c.getPosition();
        this.container = c;
    }
}
