import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
public class Bateau {
    private int id;
    private ArrayList<Container> capacite;
    private Integer capaciteMax;
    private Port portAttache;
    private Port source;
    private Port destination;
    private Evenement departPrevu;
    private Integer delaiDefaut;
    // Pour les mesures statistiques des résultats :
    private Integer attenteDechargement;
    private ArrayList<Integer> releveCapacite;

    public Bateau(int id, int capMax, Port attache, Port[] dests, int delaiDefaut){
        this.id = id;
        this.capaciteMax = capMax;
        this.portAttache = attache;
        this.portAttache.getBateaux().add(this);
        this.destination = this.portAttache == dests[1] ? dests[0] : dests[1];
        this.source = this.portAttache == dests[1] ? dests[1] : dests[0];
        this.delaiDefaut = delaiDefaut;
        this.capacite = new ArrayList<>();
        // Pour les mesures statistiques des résultats :
        this.attenteDechargement = 0;
        this.releveCapacite = new ArrayList<>();
    }

    public void ajouterContainer(Container c){
        this.capacite.add(c);
    }

    public void preparerNavigation(int temps){
        this.setDepartPrevu(new Evenement(temps + this.getDelaiDefaut(), this));
    }

    public void ajoutAttenteDechargement(){
        this.attenteDechargement++;
    }

    public void ajoutReleveCapacite(){
        this.releveCapacite.add(this.getCapacite().size());
    }
    @Override
    public String toString(){
        return String.valueOf(id);
    }

}
