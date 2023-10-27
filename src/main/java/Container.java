import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Container {
    private Integer id;
    private Itineraire itineraire; // Itinéraire du container
    private Port from; // Port depuis lequel le container est envoyé
    private Port to; //Port final vers lequel le container est envoyé.
    private Port position; // Position actuelle, -1 = pas encore dans la simulation
    public Container (int id, Itineraire i, Port position){
        assert position.getCapacite().size() < position.getCs();
        this.id = id;
        this.itineraire = i;
        this.from = i.arrets.get(0);
        this.to = i.arrets.get(i.arrets.size()-1);
        this.position = position;
    }

    //Retourne le prochain Port où le Container devra s'arrêter sur son itinéraire.
    public Port prochainArret(Port pos){
        return this.itineraire.prochainArret(pos);
    }

    /**
     * Retire le container du port si c'est son port final.
     */
    public void retirerContainer(){
        if (this.position == this.to){
            this.to.getCapacite().remove(this);
        }
    }

    @Override
    public String toString(){
        return String.valueOf(this.id);
    }
}
