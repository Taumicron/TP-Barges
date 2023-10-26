import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Classe modélisant un port, qui communique avec les autres ports, pour faire transiter des containers
 */
@Getter
@Setter
public class Port {
    private Integer id;
    private ArrayList<Service> services; // Liste des services depuis ce port. A l'échelle de l'ensemble des Port, cela représente la matrice de routage.
    private Integer cs; //Capacité max en containers
    private ArrayList<Container> capacite; // Liste des containers présents sur ce port. (la longueur représente la capacité actuelle)
    private ArrayList<Bateau> bateaux;
    public Port(int id, int cs){
        this.id = id;
        this.services = new ArrayList<>();
        this.cs = cs;
        this.capacite = new ArrayList<>();
        this.bateaux = new ArrayList<>();
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

    public Evenement creerContainer(Integer id, Evenement e, int temps) {
        if (this.capacite.size() < this.cs) {
            Container temp = new Container(id, e.getItineraire(), this);
            this.capacite.add(temp);
            return new Evenement(temps, temp, e.getFrom());
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

    public Bateau getBateauByDestination(Port destination, List<Evenement> events, int temps){
        Optional<Bateau> temp = this.bateaux.stream().filter(x -> x.getDestination() == destination).findFirst();
        if (temp.isEmpty()){ // Si un tel bateau n'est pas disponible
            return null;
        } else if (temp.get().getCapacite().size() == temp.get().getCapaciteMax()){
            return null;
        } else {
            return temp.get();
        }
    }

    @Override
    public String toString(){
        return String.valueOf(this.id);
    }
}
