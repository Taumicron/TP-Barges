import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

/**
 * Service entre 2 ports.
 */

@Getter
@Setter
public class Service {
    private ArrayList<Port> port;
    private Integer duree; // en demi-journée
    public Service(Port s1, Port s2, int duree){
        this.port = new ArrayList<>(2);
        this.port.add(s1); this.port.add(s2);
        this.duree = duree;
    }
}
