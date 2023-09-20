import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;

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
        return;
    }
}
