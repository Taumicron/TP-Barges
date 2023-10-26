import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Bateau {
    private ArrayList<Container> capacite;
    private Integer capaciteMax;
    private Port portAttache;
    private Port destination;
    private Evenement departPrevu;

    public Bateau(int capaciteMax, Port portDebut, Port destination, int temps){
        this.capaciteMax = capaciteMax;
        this.capacite = new ArrayList<>();
        this.portAttache = portDebut;
        this.destination = destination;
        this.departPrevu = new Evenement(temps + 3, this);
    }

    public void ajouterContainer(Container c){
        this.capacite.add(c);
    }


}
