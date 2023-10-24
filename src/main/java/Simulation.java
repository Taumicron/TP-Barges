import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

@Getter
@Setter
public class Simulation {
    private ArrayList<Port> ports;
    private ArrayList<Container> containers;
    private ArrayList<Evenement> events;

    public Simulation(){
        // ID | Capacité
        this.ports = new ArrayList<>();
        this.containers = new ArrayList<>();
        this.events = new ArrayList<>();
    }

    public static void main(String[] args) throws FileNotFoundException {
        Simulation s = new Simulation();

        //Chemin actuel (pour se repérer pour trouver le fichier à lire)
        System.out.println(System.getProperty("user.dir"));
        s.lire_fichiers("./src/main/resources/exemple");
        // Simulation
        Integer temps_simu = -1, idContainer = 0;
        Evenement actuel = null;
        do { // Boucle de simulation
            s.events.sort(Comparator.comparing(Evenement::getTemps) // Tri du plus récent au plus vieux
                                    .thenComparing(Evenement::getType, Comparator.reverseOrder())); // Et tri des retraits, puis des déplacements, puis des créations de containers
            actuel = s.events.remove(0);
            temps_simu = actuel.getTemps();
            System.out.print("Temps : "+temps_simu+" ");
            if (actuel.getType() == 0) { // Si l'évènement est une création de container
                if (actuel.getFrom().getCapacite().size() < actuel.getFrom().getCs()) { // Si le port peut contenir ce nouveau container
                    Evenement nouv = actuel.getFrom().creerContainer(++idContainer, actuel, s.events);
                    System.out.println("Le container "+nouv.getContainer()+" a été créé au Port "+ nouv.getFrom());
                    if (nouv != null){
                        s.events.add(nouv);
                        s.containers.add(nouv.getContainer());
                    }
                    if (actuel.getNbContainers() > 1) {
                        s.events.add(new Evenement(actuel, actuel.getFrom().prochaineDispo(s.events, temps_simu), actuel.getNbContainers()));
                    }
                } else { // Sinon on délaie la création de container.
                    System.out.println("La création d'un container est retardé car le Port "+actuel.getFrom()+" est congestionné");
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
            } else if (actuel.getType() == 2) { // Si l'évènement est un retrait de container
                actuel.getContainer().retirerContainer();
                System.out.println("Le container "+ actuel.getContainer() + " a été retiré au Port "+actuel.getFrom());
            }
        } while (s.ports.stream().anyMatch(x -> !x.getCapacite().isEmpty()) || !s.events.isEmpty());
    }

    /**
     * Lit le fichier entré en paramètres et crée les ports, les services, et les évènements de demande concernés.
     * On suppose que le fichier entré est au bon format.
     */
    private void lire_fichiers(String nomFichier) throws FileNotFoundException {
        File f = new File(nomFichier);
        Scanner sc = new Scanner(f);
        //Ajout des ports
        String temp;
        String[] val;
        sc.nextLine(); sc.nextLine(); // Passage des 2 premières lignes
        while (!Objects.equals(temp = sc.nextLine(), "Services")){
            val = temp.split(" ");
            this.ports.add(new Port(Integer.parseInt(val[0]), Integer.parseInt(val[1])));
        }

        // Ajout des services (on les suppose bidirectionnels : A -> B crée également B -> A
        sc.nextLine();
        while(!Objects.equals(temp = sc.nextLine(), "Demandes")){
            val = temp.split(" ");
            Service tempService = new Service(this.portById(Integer.parseInt(val[0])), this.portById(Integer.parseInt(val[1])), Integer.parseInt(val[2]));
            this.portById(Integer.parseInt(val[0])).addService(tempService);
        }

        // Ajout des Demandes (évènements de création de containers)
        sc.nextLine();
        while(sc.hasNextLine()){
            temp = sc.nextLine();
            val = temp.split(" ");
            int[] tempItineraire = Arrays.stream(val[2].split(";")).mapToInt(Integer::parseInt).toArray();
            ArrayList<Port> itin = new ArrayList<>(); Arrays.stream(tempItineraire).forEach(x -> itin.add(this.portById(x)));
            this.events.add(new Evenement(Integer.parseInt(val[0]), Integer.parseInt(val[1]), new Itineraire(itin)));
        }
        sc.close();
    }

    /**
     * Retourne le port correspondant s'il existe
     * @param id du port
     * @return Le port ou NULL si le port n'existe pas
     */
    private Port portById(int id){
        Optional<Port> temp = this.ports.stream().filter(x -> x.getId() == id).findFirst();
        return temp.orElse(null);
    }
}
