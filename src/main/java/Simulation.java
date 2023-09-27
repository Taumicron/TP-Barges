import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

@Getter
@Setter
public class Simulation {
    private ArrayList<Service> services;
    private ArrayList<Container> containers;
    private ArrayList<Demande> demandes;
    private ArrayList<Evenement> events;

    public Simulation(){
        this.services = new ArrayList<>();
        this.containers = new ArrayList<>();
        this.demandes = new ArrayList<>();
        this.events = new ArrayList<>();
    }

    public static void main(String[] args) {
        // Création de la matrice de routage
        Simulation s = new Simulation();
        // Ajout des Services
        s.services.add(new Service(10)); s.services.add(new Service(20)); s.services.add(new Service(30));
        s.services.add(new Service(40)); s.services.add(new Service(50));

        // Ajout des routes
        s.services.get(0).addRoute(new Route(s.services.get(0), s.services.get(1), 3));
        s.services.get(1).addRoute(new Route(s.services.get(1), s.services.get(2), 2));
        s.services.get(1).addRoute(new Route(s.services.get(3), s.services.get(1), 4));
        s.services.get(1).addRoute(new Route(s.services.get(1), s.services.get(4), 5));
        s.services.get(3).addRoute(new Route(s.services.get(3), s.services.get(4), 1));
        s.services.get(0).addRoute(new Route(s.services.get(3), s.services.get(0), 3));

        // Vérification des liaisons (identification des services par les Cs)
        s.services.forEach(x -> x.getRoutes().forEach(y -> System.out.println("Service " + x.getCs() + " from " + y.getService().get(0).getCs()+ " to "+ y.getService().get(1).getCs())));

        //Ajout d'une demande
        s.demandes.add(new Demande(3, new Itineraire(new ArrayList<>(Arrays.asList(new Service[]{
                s.services.get(0),
                s.services.get(1),
                s.getServices().get(2)
        })))));

        // Simulation
        s.events.add(new Evenement(s.demandes.remove(0), 0));
        Integer temps_simu = -1;
        Evenement actuel = null;
        do { // Boucle de simulation
            s.events.sort(Comparator.comparing(Evenement::getTemps)); // Filtrage
            actuel = s.events.remove(0);
            temps_simu = actuel.getTemps();
            if (actuel.getType() == 0) { //
                Evenement nouv = actuel.getFrom().creerContainer(actuel, s.events);
                if (nouv != null){
                    actuel.setNbContainers(actuel.getNbContainers() - 1);
                    s.events.add(nouv);
                    s.containers.add(nouv.getContainer());
                }
                if (actuel.getNbContainers() > 0){
                    s.events.add(new Evenement(actuel, actuel.getFrom().prochaineDispo(s.events), actuel.getNbContainers()));
                }
                // Si nbConteneurs != 0, ajouter un évènement
            } else if (actuel.getType() == 1) {

            } else if (actuel.getType() == 2) {
                
            }
            temps_simu = actuel.getTemps();
        } while (s.services.stream().anyMatch(x -> !x.getCapacite().isEmpty()) && !s.events.isEmpty());


    }

    public void ajouterContainer(Evenement e) {
        e.getFrom().creerContainer(e, this.events);
    }
    
}
