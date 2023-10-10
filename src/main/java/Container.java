import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

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

    //
    public Evenement deplacementContainer(Service vers, int temps_simu, ArrayList<Evenement> evt){
        this.position.getCapacite().remove(this);
        this.position = vers;
        vers.getCapacite().add(this);
        if (this.position == this.to){
            // Evenement de retrait du container
            return new Evenement(this, temps_simu + 1);
        } else {
            // Evenement de déplacement du container
            return new Evenement(this, this.position, this.prochainArret(this.position),
                    temps_simu + Math.max(this.position.tempsTrajet(this.prochainArret(this.position)), this.itineraire.prochainArret(this.position).tempsTrajet(this.position)));
        }
    }
    public void retirerContainer(){
        if (this.position == this.to){
            this.to.getCapacite().remove(this);
            this.position = null;
        }
    }

    @Override
    public String toString(){
        return String.valueOf(this.hashCode());
    }
}
