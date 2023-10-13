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
    private ArrayList<Evenement> events;

    public Simulation(){
        this.services = new ArrayList<>();
        this.containers = new ArrayList<>();
        this.events = new ArrayList<>();
    }

    public static void main(String[] args) {
        // Création de la matrice de routage
        Simulation s = new Simulation();
        // Ajout des Services
        s.services.add(new Service(3)); s.services.add(new Service(2)); s.services.add(new Service(1));
        s.services.add(new Service(4)); s.services.add(new Service(5));

        // Ajout des routes
        s.services.get(0).addRoute(new Route(s.services.get(0), s.services.get(1), 3));
        s.services.get(1).addRoute(new Route(s.services.get(1), s.services.get(2), 2));
        s.services.get(1).addRoute(new Route(s.services.get(3), s.services.get(1), 4));
        s.services.get(1).addRoute(new Route(s.services.get(1), s.services.get(4), 5));
        s.services.get(3).addRoute(new Route(s.services.get(3), s.services.get(4), 1));
        s.services.get(0).addRoute(new Route(s.services.get(3), s.services.get(0), 3));

        // Vérification des liaisons (identification des services par les Cs)
        s.services.forEach(x -> x.getRoutes().forEach(y -> System.out.println("Service " + x.getCs() + " from " + y.getService().get(0).getCs()+ " to "+ y.getService().get(1).getCs())));

        //Ajout d'un Itinéraire pour ajouter une demande de nombre de containers
        Itineraire i = new Itineraire(new ArrayList<>(Arrays.asList(
                s.services.get(0), s.services.get(1), s.services.get(2))));
        s.events.add(new Evenement(0, 1, i));
        i = new Itineraire(new ArrayList<>(Arrays.asList(
                s.services.get(1), s.services.get(4), s.services.get(3)
        )));
        s.events.add(new Evenement(10, 10, i));

        // Simulation
        Integer temps_simu = -1;
        Evenement actuel = null;
        do { // Boucle de simulation
            s.events.sort(Comparator.comparing(Evenement::getTemps).thenComparing(Evenement::getType, Comparator.reverseOrder())); // Filtrage
            actuel = s.events.remove(0);
            temps_simu = actuel.getTemps();
            System.out.print("Temps : "+temps_simu+" ");
            if (actuel.getType() == 0) { // Si l'évènement est une création de container
                if (actuel.getFrom().getCapacite().size() < actuel.getFrom().getCs()) { // Si le service peut contenir ce nouveau container
                    Evenement nouv = actuel.getFrom().creerContainer(actuel, s.events);
                    System.out.println("Le container "+nouv.getContainer()+" a été créé au Service "+ nouv.getFrom());
                    if (nouv != null){
                        s.events.add(nouv);
                        s.containers.add(nouv.getContainer());
                    }
                    if (actuel.getNbContainers() > 1) {
                        s.events.add(new Evenement(actuel, actuel.getFrom().prochaineDispo(s.events, temps_simu), actuel.getNbContainers()));
                    }
                } else { // Sinon on délaie la création de container.
                    System.out.println("La création d'un container est retardé car le Service "+actuel.getFrom()+" est congestionné.");
                    actuel.setTemps(actuel.getFrom().prochaineDispo(s.events, temps_simu));
                    s.events.add(actuel);
                }
            } else if (actuel.getType() == 1) { // Si l'évènement est un déplacement de conteneur
                if (actuel.getTo().getCapacite().size() < actuel.getTo().getCs()) {
                    s.events.add(actuel.getContainer().deplacementContainer(actuel.getTo(),temps_simu, s.events));
                    System.out.println("Le container "+actuel.getContainer()+" a été déplacé de "+actuel.getFrom()+" vers "+ actuel.getTo());
                } else {
                    System.out.println("Le déplacement du container "+actuel.getContainer()+" de "+actuel.getFrom()+" vers "+ actuel.getTo()+" a été retardé");
                    actuel.setTemps(actuel.getTo().prochaineDispo(s.events, temps_simu));
                    s.events.add(actuel);
                }
            } else if (actuel.getType() == 2) {
                actuel.getContainer().retirerContainer();
                System.out.println("Le container "+ actuel.getFrom() + " a été retiré au Service "+actuel.getFrom());
            }
        } while (s.services.stream().anyMatch(x -> !x.getCapacite().isEmpty()) || !s.events.isEmpty());
    }

}
