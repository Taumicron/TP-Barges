## TP Simulation Monôme LORTHIOIR Théo
### M1 TNSID

__Ce TP Consiste en un projet Maven Java (JDK minimum : 11) respectant le cahier des charges fourni.__
_IDE utilisé : IntelliJ, JDK 17_

GitHub : https://github.com/Taumicron/TP-Barges

### 1) Présentation succinte du code : 

__Note sur l'utilisation de Lombok__ : L'utilisation de la dépendance Lombok permet de ne pas avoir à écrire les Getter/Setter, si la classe est annotée de @Getter, @Setter, etc.
Cela permet de ne pas surcharger en code redondant les classes.

_Les différentes méthodes sont expliquées en commentaires._

Définition des différentes classes 
- Port : Représente un port, peut contenir des containers (limite de Cs Containers), et des bateaux (ici virtuellement illimité). Il
est identifié par un id, et est lié à des Services.
- Container : Représente un container identifié par un id, contient l'itinéraire qu'il parcourt, et son port de départ, actuel, et final.
- Itineraire : Représente l'itinéraire d'un container. Contient la liste ordonnée des arrêts.
- Service : Représente un Service, qui lie 2 ports. Le paramètre de durée indique le temps nécessaire pour un bateau pour traverser ce Service.
- Bateau : Représente un bateau, identifiable par id, peut contenir jusqu'à capaciteMax containers. Le bateau connait les ports où il est attaché, de départ, et d'arrivée. Il connnait également l'évènement 
  qui concerne son départ de son port d'attache, et a un délai par défaut correspondant au temps minimum avant qu'il ne quitte son port.
- Evenement : Représente un évènement de la simulation, et contient les différents paramètres nécessaires à chaque type d'évènement.
  - Type = 0 : Création d'un container dans un système
  - Type = 1 : Placement d'un container sur un bateau
  - Type = 2 : Détachement d'un bateau de son port de départ
  - Type = 3 : Rattachement d'un bateau à son port d'arrivée
  - Type = 4 : Dépôt d'un container au port où le bateau est attaché
  - Type = 5 : Retrait du container qui est arrivé à son port de destination.
- Simulation : Représente la simulation, et contient la fonction Main qui contient la boucle de simulation.

### 2) Lancement de l'application

L'application se lance depuis la fonction main() du fichier Simulation (situé à ./src/main/java).
Dans cette fonction main peut être ajusté le booléen logSurConsole, pour choisir d'afficher les logs sur console ou dans un fichier .log

Le fichier de lecture doit être spécifié dans la variable String path, et le format de données lu est supposé conforme au format suivant :

```text
[Ligne ignorée]
[Ligne ignorée, dans les fichiers exemple rappelle le format des ports]
idPort1 CapaciteMaxPort1
idPort2 CapaciteMaxPort2
...
...
idPortN CapaciteMaxPortN
Services
[Ligne ignorée, dans les fichiers exemple rappelle le format. Si pas de bateau spécifié pour un service, mettre un /]
PortA PortB Durée1 CapaciteBateau1,DelaiDefaut1,PortAttache1;...;CapaciteBateauN,DelaiDefautN,PortAttacheN
...
...
PortX PortY DuréeN CapaciteBateauK,DelaiDefautK,PortAttacheK;...;CapaciteBateauM,DelaiDefautM,PortAttacheM
Demandes
[Ligne ignorée, dans les fichiers exemple rappelle le format]
TempsDemande1 nbContainersDemande1 idPortA;idPortB;...;idPortN
TempsDemande2 nbContainersDemande2 idPortJ;idPortK;...;idPortL
...
...
TempsDemandeN nbContainersDemandeN idPortX;idPortY;...;idPortZ
[Fin fichier]
```

_Note : Les bateaux sont spécifiés en même temps que les services, plusieurs bateaux déclarés à un même service sont séparés d'un ";" et les paramètres de chaque bateau sont séparés d'un ","_

_De même pour les différents ports d'un itinéraire (qui sont spécifiés par leur id). Les lignes Services et Demandes servent de séparateur et sont donc obligatoires._

Les résultats statistiques sont enregistrés dans un fichier du dossier ressources nommé [nomFichierEntrée]_resultats
### 3) Ce qui fonctionne

- Simulation par évènements discrets
- Lecture des informations depuis un fichier (voir ci-dessus)
- Priorisation des évènements selon le type (5 > 4 > 3 > 2 > 1 > 0).
- Description de chaque évènement quand il occurre (pour le suivi du déroulé de la simulation)
- Déplacement des containers par des bateaux
- Suivi d'un itinéraire précis par la matrice de routage
- Report des évènements s'ils ne peuvent pas occurer (décalage à T+1)
- Attente des bateaux au port s'ils ne peuvent pas se décharger au port.
- Impression des mesures statistiques dans un fichier dédié.
- Retrait des containers arrivés à destination.
- Limitation du nombre de ticks maximum de la simulation (pour le cas d'une boucle infinie)

### 4) Ce qui ne fonctionne pas

- Limitation du nombre de bateaux pouvant être à un port.
- Algorithme de PathFinding pour établir un itinéraire automatiquement pour chaque container
- Gestion de pannes et d'évènements imprévus 
- Prise en compte de la priorité d'une demande.
- Mécanismes de décongestion dans le cas où un port est plein, et ne peut pas vider ses containers. (Cas exemple 3)

### 5) TODO 

- Refactoriser le code pour rendre la boucle principale de la fonction main plus lisible
- Homogénéiser le fonctionnement de certains évènements : Les attributs To et From des Evenements ne sont parfois pas utilisés.
_Des plantages peuvent intervenir sur certaines configurations de ce fait_
- Trouver le moyen de ne pas avoir à décaler les évènements à T+1, mais à la prochaine disponibilité de l'entité bloquante.
  - Pour les bateaux bloqués pour décharger les containers, déterminer quand ils pourront décharger un container
  - Pour les containers qui ne peuvent pas être chargés sur un bateau, déterminer quand le prochain bateau sera disponible
  - etc.
 - Ajouter d'autres mesures de statistiques de résultats. 
 - Alléger l'affichage des logs.
