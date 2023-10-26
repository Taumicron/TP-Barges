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
        int temps_simu = -1, idContainer = 0;
        Evenement actuel = null;
        do { // Boucle de simulation
            s.events.sort(Comparator.comparing(Evenement::getTemps) // Tri du plus récent au plus vieux
                                    .thenComparing(Evenement::getType, Comparator.reverseOrder())); // Et tri des types d'Evt. 5 > 4 > 3 > 2 > 1 > 0
            actuel = s.events.remove(0);
            temps_simu = actuel.getTemps();
            System.out.print("Temps : "+temps_simu+" ");
            if (actuel.getType() == 0) { // Création de container sur le port.
                if (actuel.getFrom().getCapacite().size() < actuel.getFrom().getCs()) { // Si le port peut contenir ce nouveau container
                    Evenement nouv = actuel.getFrom().creerContainer(++idContainer, actuel, temps_simu);
                    System.out.println("Le container "+nouv.getContainer()+" a été créé au Port "+ nouv.getContainer().getPosition());
                    if (nouv != null){
                        s.events.add(nouv);
                        s.containers.add(nouv.getContainer());
                    }
                    if (actuel.getNbContainers() > 1) {
                        s.events.add(new Evenement(actuel, temps_simu, actuel.getNbContainers()));
                    }
                } else { // Sinon on délaie la création de container.
                    System.out.println("La création d'un container est retardé car le Port "+actuel.getFrom()+" est congestionné");
                    actuel.setTemps(temps_simu + 1);
                    s.events.add(actuel);
                }
            } else if (actuel.getType() == 1) { // Placement du container sur un bateau.
                // temp <- Bateau sur lequel poser le container.
                Bateau temp = actuel.getContainer().getPosition().getBateauByDestination(actuel.getContainer().prochainArret(actuel.getContainer().getPosition()), s.events, temps_simu);
                if (temp != null){
                    actuel.getContainer().getPosition().getCapacite().remove(actuel.getContainer());
                    actuel.getContainer().setPosition(null);
                    temp.ajouterContainer(actuel.getContainer());
                    System.out.print("Le container "+ actuel.getContainer() + " a été attaché à un bateau. ");
                    if (temp.getCapaciteMax() == temp.getCapacite().size()){
                        s.events.remove(temp.getDepartPrevu()); // Retrait de l'évent de départ par défaut
                        temp.setDepartPrevu(new Evenement(temps_simu + 1, temp));
                        s.events.add(temp.getDepartPrevu());
                        System.out.print("Le bateau est au maximum de sa capacité. Départ imminent");
                    }
                } else {
                    System.out.print("Le container "+ actuel.getContainer() + " n'a pas pu être rattaché à un bateau car celui prévu est complet.");
                    actuel.setTemps(temps_simu+1);
                    s.events.add(actuel);
                }
                System.out.println();
            } else if (actuel.getType() == 2) { // Détache du bateau de son port.
                s.events.add(new Evenement(temps_simu + actuel.getFrom().tempsTrajet(actuel.getBateau().getDestination()), actuel.getBateau(), actuel.getBateau().getDestination())); //Evenement de rattache au port.
                actuel.getBateau().getPortAttache().getBateaux().remove(actuel.getBateau());
                System.out.println("Un bateau part du port "+ actuel.getFrom()+" avec les containers "+ actuel.getBateau().getCapacite());
                actuel.getBateau().setPortAttache(null);
            } else if (actuel.getType() == 3) { // Attache le bateau a son port, en vue de le vider de ses containers.
                actuel.getTo().getBateaux().add(actuel.getBateau());
                actuel.getBateau().setPortAttache(actuel.getTo());
                s.events.add(new Evenement(actuel.getBateau(), temps_simu + 1));
                System.out.println("Le port "+ actuel.getTo()+" vient de réceptionner "+ actuel.getBateau());
            } else if (actuel.getType() == 4) { // Déplacement du container sur le port en vue de sa décharge.
                if (actuel.getTo().getCapacite().size() < actuel.getTo().getCs()){
                    Container temp = actuel.getBateau().getCapacite().remove(0);
                    System.out.println("Le container "+ temp + " a été déposé au port "+actuel.getTo());
                    actuel.getTo().getCapacite().add(temp);
                    temp.setPosition(actuel.getTo());
                    if (temp.getPosition() == temp.getTo()){
                        s.events.add(new Evenement(temp, temps_simu + 1));
                    } else {
                        s.events.add(new Evenement(temps_simu + 1, temp, temp.prochainArret(temp.getPosition())));
                    }
                    temp.setPosition(actuel.getTo());
                    if (!actuel.getBateau().getCapacite().isEmpty()){
                        // Création de l'évènement pour déposer encore des containers de ce même bateau.
                        s.events.add(new Evenement(actuel.getBateau(), temps_simu));
                    } else { // Sinon le bateau est vide, on peut le supprimer
                        actuel.getBateau().getPortAttache().getBateaux().remove(actuel.getBateau());
                        actuel.getBateau().setPortAttache(null);
                    }
                } else {
                    System.out.println("Un bateau n'a pas pu décharger un container au port "+actuel.getTo()+".");
                    actuel.setTemps(temps_simu + 1);
                    s.events.add(actuel);
                }
            } else if (actuel.getType() == 5) { // Si l'évènement est un retrait de container.
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
