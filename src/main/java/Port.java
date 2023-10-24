import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Optional;

/**
 * Classe modélisant un port, qui communique avec les autres ports, pour faire transiter des containers
 */
@Getter
@Setter
public class Port {
    private ArrayList<Service> services; // Liste des services depuis ce port. A l'échelle de l'ensemble des Port, cela représente la matrice de routage.
    private Integer cs; //Capacité max en containers
    private ArrayList<Container> capacite; // Liste des containers présents sur ce port. (la longueur représente la capacité actuelle)

    public Port(int cs){
        this.services = new ArrayList<>();
        this.cs = cs;
        this.capacite = new ArrayList<>();
    }

    public void addService(Service r) {
        if (!(r.getPort().get(0) == this || r.getPort().get(1) == this)){
            System.out.println("Erreur lors de l'ajout de " + r + " depuis le port " + this);
            return;
        }
        this.services.add(r);
        if (r.getPort().get(0) == this) {
            r.getPort().get(1).services.add(r);
        } else {
            r.getPort().get(0).services.add(r);
        }
    }

    public Evenement creerContainer(Evenement e, ArrayList<Evenement> evt) {
        if (this.capacite.size() < this.cs) {
            Container temp = new Container(e.getItineraire(), this);
            this.capacite.add(temp);
            return new Evenement(temp, temp.getPosition(), temp.getItineraire().prochainArret(this),
                    Math.max(this.tempsTrajet(temp.prochainArret(this)) + e.getTemps(), prochaineDispo(evt, e.getTemps()))); // Retourne le prochain évènement du container.
        }
        return null;
    }

    // Retourne le temps de trajet du port actuel au port entré en paramètre. On suppose qu'un tel service existe
    public Integer tempsTrajet(Port s){
        Optional<Service> optServ =  this.services.stream().filter(x -> x.getPort().contains(this) && x.getPort().contains(s)).findFirst();
        if (optServ.isEmpty()){
            System.err.println("Le trajet du port "+ this + " au port "+ s+ " n'existe pas.");
            return 9999999;
        } else {
            return optServ.get().getDuree();
        }
    }

    // Retourne le temps à partor duquel le port sera disponible du Port (pour pouvoir prendre en charge une demande. Retourne 0 si instantané
    public Integer prochaineDispo(ArrayList<Evenement> events, int temps_simu){
        if (this.capacite.size() < this.cs ){
            return temps_simu;
        }
        // La liste des évènements a déjà été préalablement ordonnée en fonction du temps.
        Optional<Evenement> optEvt = events.stream().filter(x -> x.getType() == 1 && x.getFrom() == this
                                                                || x.getType() == 2 && x.getFrom() == this)
                                                                .findFirst();
        if (optEvt.isEmpty()) return temps_simu; // S'il n'y a pas d'évènement concernant un déplacement vers ce container (redondant)
        return optEvt.get().getTemps();
    }

    @Override
    public String toString(){
        return String.valueOf(this.hashCode());
    }
}
