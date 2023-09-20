public class Container {
    private Itineraire itineraire; // Itinéraire du container
    private Service from; // Service depuis lequel le container est envoyé
    private Service to; //Service final vers lequel le container est envoyé.
    private Service position; // Position actuelle, -1 = pas encore dans la simulation
    public Container (Itineraire i){
        this.itineraire = i;
        this.from = i.arrets.get(0);
        this.to = i.arrets.get(i.arrets.size());

    }
}
