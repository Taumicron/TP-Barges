import lombok.Getter;
import lombok.Setter;

import java.io.*;
import java.util.*;

@Getter
@Setter
public class Simulation {
    public static final int LIMITE_TICKS_SIMULATION = (int) 2E4;

    private ArrayList<Port> ports;
    private ArrayList<Container> containers;
    private ArrayList<Evenement> events;
    private ArrayList<Bateau> bateaux;

    public Simulation(){
        // ID | Capacité
        this.ports = new ArrayList<>();
        this.containers = new ArrayList<>();
        this.events = new ArrayList<>();
        this.bateaux = new ArrayList<>();
    }

    public static void main(String[] args) throws FileNotFoundException {
        Simulation s = new Simulation();

        // 0 : Logs en console, 1 : Logs en fichier (chemin modifiable, le fichier sera créé si inexistant)
        boolean logSurConsole = false;
        PrintStream output = (logSurConsole ? System.out : new PrintStream("./src/main/resources/output.log"));

        //Chemin actuel (pour se repérer pour trouver le fichier à lire)
        System.out.println(System.getProperty("user.dir"));
        String path = "./src/main/resources/exemple1";
        s.lire_fichiers(path);

        // Simulation
        int temps_simu = -1, idContainer = 0;
        Evenement actuel = null;
        do { // Boucle de simulation
            s.events.sort(Comparator.comparing(Evenement::getTemps) // Tri du plus récent au plus vieux
                    .thenComparing(Evenement::getType, Comparator.reverseOrder())); // Et tri des types d'Evt. 5 > 4 > 3 > 2 > 1 > 0
            actuel = s.events.remove(0);
            if (actuel.getTemps() != temps_simu) s.releve_stats();
            temps_simu = actuel.getTemps();
            output.print("T "+temps_simu+" | " +(temps_simu % 2 == 0 ? "Matinée " : "Soirée ") + temps_simu / 2 + ":\t");
            if (actuel.getType() == 0) { // Création de container sur le port.
                if (actuel.getFrom().getCapacite().size() < actuel.getFrom().getCs()) { // Si le port peut contenir ce nouveau container
                    Evenement nouv = actuel.getFrom().creerContainer(++idContainer, actuel, temps_simu);
                    output.println("Le container " + nouv.getContainer() + " a été créé au Port " + nouv.getContainer().getPosition());
                    if (nouv != null) {
                        s.events.add(nouv);
                        s.containers.add(nouv.getContainer());
                    }
                    if (actuel.getNbContainers() > 1) {
                        s.events.add(new Evenement(actuel, temps_simu, actuel.getNbContainers()));
                    }
                } else { // Sinon on délaie la création de container.
                    output.println("La création d'un container est retardé car le Port " + actuel.getFrom() + " est congestionné");
                    actuel.setTemps(temps_simu + 1);
                    s.events.add(actuel);
                }

            } else if (actuel.getType() == 1) { // Placement du container sur un bateau.
                // temp <- Bateau sur lequel poser le container.
                Bateau temp = actuel.getContainer().getPosition().getBateauByDestination(actuel.getContainer().prochainArret(actuel.getContainer().getPosition()));
                if (temp != null) {
                    actuel.getContainer().getPosition().getCapacite().remove(actuel.getContainer());
                    actuel.getContainer().setPosition(null);
                    temp.ajouterContainer(actuel.getContainer());
                    output.print("Le container " + actuel.getContainer() + " a été attaché au bateau " + temp + ".");
                    if (temp.getCapaciteMax() == temp.getCapacite().size()) {
                        s.events.remove(temp.getDepartPrevu()); // Décalage du départ à t+1
                        temp.setDepartPrevu(new Evenement(temps_simu + 1, temp));
                        s.events.add(temp.getDepartPrevu());
                        output.print(" Le bateau " + temp + " est au maximum de sa capacité. Départ imminent");
                    }
                } else {
                    output.print("Le container " + actuel.getContainer() + " n'a pas pu être rattaché à un bateau car il n'y en a pas de disponible.");
                    actuel.setTemps(temps_simu + 1);
                    s.events.add(actuel);
                }
                output.println();

            } else if (actuel.getType() == 2) { // Détache le bateau de son port.
                s.events.add(new Evenement(temps_simu + actuel.getBateau().getDestination().tempsTrajet(actuel.getBateau().getDestination()), actuel.getBateau(), actuel.getBateau().getDestination())); //Evenement de rattache au port.
                actuel.getBateau().getSource().getBateaux().remove(actuel.getBateau());
                output.println("Le bateau " + actuel.getBateau() + " part du port " + actuel.getFrom() + " vers " + actuel.getTo() + " avec les containers " + actuel.getBateau().getCapacite());
                actuel.getBateau().setPortAttache(null);
                //Mesure statistique
                actuel.getBateau().ajoutReleveCapacite();

            } else if (actuel.getType() == 3) { // Attache le bateau a son port
                actuel.getTo().getBateaux().add(actuel.getBateau());
                actuel.getBateau().setPortAttache(actuel.getTo());
                Port tempDest = actuel.getBateau().getSource();
                actuel.getBateau().setSource(actuel.getBateau().getDestination());
                actuel.getBateau().setDestination(tempDest);
                if (actuel.getBateau().getCapacite().isEmpty()) {
                    actuel.getBateau().preparerNavigation(temps_simu);
                    s.events.add(actuel.getBateau().getDepartPrevu());
                } else {
                    s.events.add(new Evenement(actuel.getBateau(), temps_simu));
                }
                output.println("Le port " + actuel.getTo() + " vient de réceptionner le bateau " + actuel.getBateau());

            } else if (actuel.getType() == 4) { // Déplacement du container sur le port en vue de sa décharge.
                if (actuel.getBateau().getCapacite().isEmpty()) {
                    actuel.getBateau().preparerNavigation(temps_simu);
                    s.events.add(actuel.getBateau().getDepartPrevu());
                    output.print("Le bateau " + actuel.getBateau() + " est vide et se prépare à partir du port " + actuel.getBateau().getSource() + " au port " + actuel.getBateau().getDestination() + ". ");
                } else if (actuel.getTo().getCapacite().size() < actuel.getTo().getCs()) {
                    Container temp = actuel.getBateau().getCapacite().remove(0);
                    actuel.getTo().getCapacite().add(temp);
                    temp.setPosition(actuel.getTo());
                    output.print("Le container " + temp + " a été déposé au port " + actuel.getTo() + ". ");
                    if (temp.getPosition() == temp.getTo()) {
                        s.events.add(new Evenement(temp, temps_simu + 1));
                    } else {
                        s.events.add(new Evenement(temps_simu + 1, temp, temp.prochainArret(temp.getPosition())));
                    }
                    if (!actuel.getBateau().getCapacite().isEmpty()) {
                        // Création de l'évènement pour déposer encore des containers de ce même bateau.
                        s.events.add(new Evenement(actuel.getBateau(), temps_simu));
                    } else { // Sinon le bateau est vide, on programme son retour dans l'autre sens.
                        actuel.getBateau().preparerNavigation(temps_simu);
                        s.events.add(actuel.getBateau().getDepartPrevu());
                        output.print("Le bateau " + actuel.getBateau() + " est vide et se prépare à partir du port " + actuel.getBateau().getSource() + " au port " + actuel.getBateau().getDestination());
                    }
                    output.println();
                } else {
                    output.println("Le bateau " + actuel.getBateau() + " n'a pas pu décharger un container au port " + actuel.getTo() + ".");
                    actuel.setTemps(temps_simu + 1);
                    s.events.add(actuel);
                    // Mesure statistique
                    actuel.getBateau().ajoutAttenteDechargement();
                }

            } else if (actuel.getType() == 5) { // Si l'évènement est un retrait de container.
                actuel.getContainer().retirerContainer();
                output.println("Le container " + actuel.getContainer() + " a été retiré au Port " + actuel.getFrom());
            }
        } while ((!s.containers.stream().allMatch(x -> x.getPosition() == x.getTo()) || s.events.stream().anyMatch(x -> x.getType() == 0 || x.getType() == 5)) && temps_simu < LIMITE_TICKS_SIMULATION);
        output.println("Fin de la simulation.");
        if (temps_simu >= 2E4) output.println("Simulation arrêtée car délai dépassé (25000 ticks)");
        // Ecriture des résultats dans un fichier [nom_fichier]_resultats
        output = new PrintStream(path+"_resultats");
        output.println("Résultats statistiques :\nPour les bateaux : taux d'occupation de la capacité en moyenne par tick (en %) | Attentes pour déchargement");
        ArrayList<Float> temp = new ArrayList<>();
        float moyDechargement = 0;
        for(Bateau x : s.bateaux) {
            float moy = 0;
            moyDechargement += x.getAttenteDechargement();
            for (int y : x.getReleveCapacite()) {
                moy += 100 * (float) y / (float) x.getCapaciteMax();
            }
            temp.add(moy / (float) x.getReleveCapacite().size());
            output.printf("Bateau " + x.getId() + " : %.2f" +
                    " \t|\t" + x.getAttenteDechargement() + "\n", moy / (float) x.getReleveCapacite().size());
        }
        float moy = 0;
        for(float x : temp) moy += x;
        output.println("Moyenne de tous les bateaux: "+moy/(float)temp.size() +" | "+ moyDechargement / (float) temp.size());
        output.println("Pour les Ports : taux d'occupation de la capacité en moyenne par tick (en %)");
        temp = new ArrayList<>();
        for(Port p : s.ports){
            moy = 0;
            for(int y : p.getReleveCapacite()){
                moy += 100 * (float) y / (float) p.getCs();
            }
            temp.add(moy / (float) p.getReleveCapacite().size());
            output.printf("Port " + p.getId() + " : %.2f\n",moy / (float) p.getReleveCapacite().size());
        }
        moy = 0;
        for (float x :temp) moy += x;
        output.println("Moyenne de tous les ports: "+moy/(float)temp.size());
        if (temps_simu >= LIMITE_TICKS_SIMULATION) output.println("SIMULATION PAS ARRIVEE A TERME EN RAISON D'UNE PROBABLE BOUCLE INFINIE");
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
        int idBateau = 0;
        sc.nextLine(); sc.nextLine(); // Passage des 2 premières lignes
        while (!Objects.equals(temp = sc.nextLine(), "Services")){
            val = temp.split(" ");
            this.ports.add(new Port(Integer.parseInt(val[0]), Integer.parseInt(val[1])));
        }

        // Ajout des services (on les suppose bidirectionnels : A -> B crée également B -> A)
        sc.nextLine();
        while(!Objects.equals(temp = sc.nextLine(), "Demandes")){
            val = temp.split(" ");
            Service tempService = new Service(this.portById(Integer.parseInt(val[0])), this.portById(Integer.parseInt(val[1])), Integer.parseInt(val[2]));
            this.portById(Integer.parseInt(val[0])).addService(tempService);
            if (!Objects.equals(val[3], "/")){
                String[] bateaux = val[3].split(";");
                for (String s : bateaux) { //Ajout de chaque bateau au port correspondant
                    String[] bateau = s.split(",");
                    Bateau b = new Bateau(++idBateau, Integer.parseInt(bateau[0]),
                            portById(Integer.parseInt(bateau[2])), new Port[] {portById(Integer.parseInt(val[0])), portById(Integer.parseInt(val[1]))}, Integer.parseInt(bateau[1]));
                    b.setDepartPrevu(new Evenement(b.getDelaiDefaut(), b));
                    this.events.add(b.getDepartPrevu());
                    this.bateaux.add(b);
                }
            }
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

    private void releve_stats(){
        this.ports.forEach(x -> x.getReleveCapacite().add(x.getCapacite().size()));
    }

    /**
     * Retourne le port correspondant s'il existe
     * @param id du port
     * @return Le port ou NULL si le port n'existe pas
     */
    private Port portById(int id) {
        Optional<Port> temp = this.ports.stream().filter(x -> x.getId() == id).findFirst();
        return temp.orElse(null);
    }
}
