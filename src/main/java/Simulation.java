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
        s.demandes.add(new Demande(3, new Itineraire(new ArrayList<>(Arrays.asList(
                s.services.get(0),
                s.services.get(1),
                s.getServices().get(2))))));

        // Simulation
        s.events.add(new Evenement(s.demandes.remove(0), 0));
        Integer temps_simu = -1;
        Evenement actuel = null;
        do { // Boucle de simulation
            s.events.sort(Comparator.comparing(Evenement::getTemps)); // Filtrage
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
                        s.events.add(new Evenement(actuel, actuel.getFrom().prochaineDispo(s.events), actuel.getNbContainers()));
                    }
                } else { // Sinon on délaie la création de container.
                    System.err.println("La création d'un container est retardé car le Service "+actuel.getTo()+" est congestionné.");
                    actuel.setTemps(actuel.getFrom().prochaineDispo(s.events));
                    s.events.add(actuel);
                }

            } else if (actuel.getType() == 1) { // Si l'évènement est un déplacement de conteneur (donc l'arrivée)
                if (actuel.getTo().getCapacite().size() < actuel.getFrom().getCs()) {
                    s.events.add(actuel.getContainer().deplacementContainer(actuel.getTo(),temps_simu, s.events));
                    System.out.println("Le container "+actuel.getContainer()+" a été déplacé de "+actuel.getFrom()+" vers "+ actuel.getTo());
                } else {
                    actuel.setTemps(actuel.getTo().prochaineDispo(s.events));
                    System.err.println("Le déplacement du container "+actuel.getContainer()+" de "+actuel.getFrom()+" vers "+ actuel.getTo()+" a été retardé");
                    s.events.add(actuel);
                }
            } else if (actuel.getType() == 2) {
                actuel.getContainer().retirerContainer();
                System.out.println("Le container "+ actuel.getFrom() + " a été retiré au Service "+actuel.getFrom());
            }
        } while (s.services.stream().anyMatch(x -> !x.getCapacite().isEmpty()) && !s.events.isEmpty());
    }

    public void ajouterContainer(Evenement e) {
        e.getFrom().creerContainer(e, this.events);
    }
    
}
