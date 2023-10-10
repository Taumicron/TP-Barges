import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Optional;

/**
 * Classe modélisant un service, qui communique avec les autres services, pour faire transiter des containers
 */
@ToString
@Getter
@Setter
public class Service {
    private ArrayList<Route> routes; // Liste des routes depuis ce service. A l'échelle de l'ensemble des Service, cela représente la matrice de routage.
    private Integer cs; //Capacité max en containers
    private ArrayList<Container> capacite; // Liste des containers présents sur ce service. (la longueur représente la capacité actuelle)

    public Service (int cs){
        this.routes = new ArrayList<>();
        this.cs = cs;
        this.capacite = new ArrayList<>();
    }

    public void addRoute(Route r) {
        if (!(r.getService().get(0) == this || r.getService().get(1) == this)){
            System.out.println("Erreur lors de l'ajout de " + r + " depuis le service " + this);
            return;
        }
        this.routes.add(r);
        if (r.getService().get(0) == this) {
            r.getService().get(1).routes.add(r);
        } else {
            r.getService().get(0).routes.add(r);
        }
    }

    public Evenement creerContainer(Evenement e, ArrayList<Evenement> evt) {
        if (this.capacite.size() < this.cs) {
            Container temp = new Container(e.getItineraire(), this);
            this.capacite.add(temp);
            return new Evenement(temp, temp.getPosition(), temp.getItineraire().prochainArret(this),
                    Math.max(this.tempsTrajet(temp.prochainArret(this)), prochaineDispo(evt))); // Retourne le prochain évènement du container.
        }
        return null;
    }

    // Retourne le temps de trajet du service actuel au service entré en paramètre. On suppose qu'une telle route existe
    public Integer tempsTrajet(Service s){
        Optional<Route> optRoute =  this.routes.stream().filter(x -> x.getService().contains(this) && x.getService().contains(s)).findFirst();
        if (optRoute.isEmpty()){
            System.err.println("Le trajet du service "+ this + " au service "+ s+ " n'existe pas.");
            return 9999999;
        } else {
            return optRoute.get().getDuree();
        }
    }

    // Retourne le temps de la prochaine disponibilité du Service (pour pouvoir prendre en charge une demande. Retourne 0 si instantané
    public Integer prochaineDispo(ArrayList<Evenement> events){
        if (this.capacite.size() -1 < this.cs ){
            return 0;
        }
        Optional<Evenement> optEvt = events.stream().filter(x -> x.getType() == 1 && x.getTo() == this).findFirst();
        if (optEvt.isEmpty()) return 0; // S'il n'y a pas d'évènement concernant un déplacement vers ce container (redondant)
        return optEvt.get().getTemps();
    }

    @Override
    public String toString(){
        return String.valueOf(this.hashCode());
    }
}
