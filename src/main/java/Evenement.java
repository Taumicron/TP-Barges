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

    private Integer nbContainers; // type=1 : Le nombre de containers qui doivent être créés par la demande.

    private Service from; // type=1 : le Service où ajouter le container
                            //type=2 : le Service où est actuellement le container
    private Service to; // type=2 : Le service où le container va


    public Evenement( Demande d, int temps){
        this.type = 0;
        this.temps = temps;
        this.from = d.getItineraire().arrets.get(0);
        this.nbContainers = d.getNbConteneurs();
    }
}
