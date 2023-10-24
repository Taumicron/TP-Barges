import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

@Getter
@Setter
public class Simulation {
    private ArrayList<Port> ports;
    private ArrayList<Container> containers;
    private ArrayList<Evenement> events;

    public Simulation(){
        this.ports = new ArrayList<>();
        this.containers = new ArrayList<>();
        this.events = new ArrayList<>();
    }

    public static void main(String[] args) {
        // Création de la matrice de routage
        Simulation s = new Simulation();
        // Ajout des ports
        s.ports.add(new Port(3)); s.ports.add(new Port(2)); s.ports.add(new Port(1));
        s.ports.add(new Port(4)); s.ports.add(new Port(5));

        // Ajout des services
        s.ports.get(0).addService(new Service(s.ports.get(0), s.ports.get(1), 3));
        s.ports.get(1).addService(new Service(s.ports.get(1), s.ports.get(2), 2));
        s.ports.get(1).addService(new Service(s.ports.get(3), s.ports.get(1), 4));
        s.ports.get(1).addService(new Service(s.ports.get(1), s.ports.get(4), 5));
        s.ports.get(3).addService(new Service(s.ports.get(3), s.ports.get(4), 1));
        s.ports.get(0).addService(new Service(s.ports.get(3), s.ports.get(0), 3));

        // Vérification des liaisons (identification des ports par les Cs)
        s.ports.forEach(x -> x.getServices().forEach(y -> System.out.println("Port " + x.getCs() + " from " + y.getPort().get(0).getCs()+ " to "+ y.getPort().get(1).getCs())));

        //Ajout d'un Itinéraire pour ajouter une demande de nombre de containers
        Itineraire i = new Itineraire(new ArrayList<>(Arrays.asList(
                s.ports.get(0), s.ports.get(1), s.ports.get(2))));
        s.events.add(new Evenement(0, 1, i));
        i = new Itineraire(new ArrayList<>(Arrays.asList(
                s.ports.get(1), s.ports.get(4), s.ports.get(3)
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
                if (actuel.getFrom().getCapacite().size() < actuel.getFrom().getCs()) { // Si le port peut contenir ce nouveau container
                    Evenement nouv = actuel.getFrom().creerContainer(actuel, s.events);
                    System.out.println("Le container "+nouv.getContainer()+" a été créé au Port "+ nouv.getFrom());
                    if (nouv != null){
                        s.events.add(nouv);
                        s.containers.add(nouv.getContainer());
                    }
                    if (actuel.getNbContainers() > 1) {
                        s.events.add(new Evenement(actuel, actuel.getFrom().prochaineDispo(s.events, temps_simu), actuel.getNbContainers()));
                    }
                } else { // Sinon on délaie la création de container.
                    System.out.println("La création d'un container est retardé car le Port "+actuel.getFrom()+" est congestionné.");
                    actuel.setTemps(actuel.getFrom().prochaineDispo(s.events, temps_simu));
                    s.events.add(actuel);
                }
            } else if (actuel.getType() == 1) { // Si l'évènement est un déplacement de conteneur
                if (actuel.getTo().getCapacite().size() < actuel.getTo().getCs()) {
                    s.events.add(actuel.getContainer().deplacementContainer(actuel.getTo(),temps_simu));
                    System.out.println("Le container "+actuel.getContainer()+" a été déplacé de "+actuel.getFrom()+" vers "+ actuel.getTo());
                } else {
                    System.out.println("Le déplacement du container "+actuel.getContainer()+" de "+actuel.getFrom()+" vers "+ actuel.getTo()+" a été retardé");
                    actuel.setTemps(actuel.getTo().prochaineDispo(s.events, temps_simu));
                    s.events.add(actuel);
                }
            } else if (actuel.getType() == 2) {
                actuel.getContainer().retirerContainer();
                System.out.println("Le container "+ actuel.getFrom() + " a été retiré au Port "+actuel.getFrom());
            }
        } while (s.ports.stream().anyMatch(x -> !x.getCapacite().isEmpty()) || !s.events.isEmpty());
    }

}
