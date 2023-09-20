import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

/**
 * Route entre 2 Services.
 */

@Getter
@Setter
public class Route {
    private ArrayList<Service> service;
    private Integer duree; // en demi-journée
    public Route(Service s1, Service s2, int duree){
        this.service = new ArrayList<>();
        this.service.add(s1); this.service.add(s2);
        this.duree = duree;
    }
}
