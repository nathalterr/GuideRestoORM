package ch.hearc.ig.guideresto.presentation;

import ch.hearc.ig.guideresto.business.*;
import ch.hearc.ig.guideresto.services.CityService;
import ch.hearc.ig.guideresto.services.EvaluationService;
import ch.hearc.ig.guideresto.services.RestaurantService;
import ch.hearc.ig.guideresto.services.RestaurantTypeService;

import java.sql.SQLException;
import java.util.*;

import static ch.hearc.ig.guideresto.persistence.ConnectionUtils.closePool;

/**
 * @author cedric.baudet
 * @author alain.matile
 */
public class Application {

    private static Scanner scanner;
    public static void main(String[] args) {

        scanner = new Scanner(System.in);

        System.out.println("Bienvenue dans GuideResto ! Que souhaitez-vous faire ?");
        int choice;
        do {
            printMainMenu();
            choice = readInt();
            try {
                proceedMainMenu(choice);
            } catch (SQLException e) {
                System.out.println("An internal database error occurred. Please retry or contact support." +  e);
            }
        } while (choice != 0);
    }

    /**
     * Affichage du menu principal de l'application
     */
    private static void printMainMenu() {
        System.out.println("======================================================");
        System.out.println("Que voulez-vous faire ?");
        System.out.println("1. Afficher la liste de tous les restaurants");
        System.out.println("2. Rechercher un restaurant par son nom");
        System.out.println("3. Rechercher un restaurant par ville");
        System.out.println("4. Rechercher un restaurant par son type de cuisine");
        System.out.println("5. Saisir un nouveau restaurant");
        System.out.println("0. Quitter l'application");
    }

    /**
     * On gère le choix saisi par l'utilisateur
     *
     * @param choice Un nombre entre 0 et 5.
     */
    private static void proceedMainMenu(int choice) throws  SQLException {
        switch (choice) {
            case 1:
                showRestaurantsList();
                break;
            case 2:
                searchRestaurantByName();
                break;
            case 3:
                searchRestaurantByCity();
                break;
            case 4:
                searchRestaurantByType();
                break;
            case 5:
                addNewRestaurant();
                break;
            case 0:
                System.out.println("Au revoir !");
                closePool();
                scanner.close();
                break;
            default:
                System.out.println("Erreur : saisie incorrecte. Veuillez réessayer");
                break;
        }
    }

    /**
     * On affiche à l'utilisateur une liste de restaurants numérotés, et il doit en sélectionner un !
     *
     * @param restaurants Liste à afficher
     * @return L'instance du restaurant choisi par l'utilisateur
     */
    private static Restaurant pickRestaurant(List<Restaurant> restaurants) {

        System.out.println("Plusieurs restaurants trouvés :");

        for (int i = 0; i < restaurants.size(); i++) {
            Restaurant r = restaurants.get(i);
            System.out.println(
                    (i + 1) + ") \"" + r.getName() + "\" - " +
                            r.getAddress().getStreet() + " - " +
                            r.getAddress().getCity().getZipCode() + " " +
                            r.getAddress().getCity().getCityName()
            );
        }

        System.out.println("Entrez le numéro du restaurant (ou Enter pour annuler) :");
        String input = readString();

        if (input.isBlank()) {
            return null;
        }

        try {
            int index = Integer.parseInt(input);

            if (index < 1 || index > restaurants.size()) {
                System.out.println("Numéro invalide.");
                return null;
            }

            return restaurants.get(index - 1);

        } catch (NumberFormatException e) {
            System.out.println("Veuillez entrer un numéro valide.");
            return null;
        }
    }

    /**
     * Affiche la liste de tous les restaurants, sans filtre
     */
    private static void showRestaurantsList() throws SQLException{

        System.out.println("Liste des restaurants : ");
        List<Restaurant> restaurants = RestaurantService.getInstance().getAllRestaurants();
        Restaurant restaurant = pickRestaurant(restaurants);
        if (restaurant != null) {
            showRestaurant(restaurant);
        }
    }
    /**
     * Affiche une liste de restaurants dont le nom contient une chaîne de caractères saisie par l'utilisateur
     */
    private static void searchRestaurantByName() {
        System.out.println("Veuillez entrer une partie du nom recherché : ");
        String research = readString();
        List<Restaurant> restaurants ;

        try {
            restaurants = RestaurantService.getInstance().findRestaurantsByName(research);
            if (restaurants.isEmpty()) {
                System.out.println("Aucun restaurant trouvé pour : " + research);
            }
            else if (restaurants.size() == 1) {
                showRestaurant(restaurants.get(0));
            }
            else{// L'utilisateur choisit un restaurant parmi les résultats.
            Restaurant restaurant = pickRestaurant(restaurants);
            if (restaurant != null) {
                showRestaurant(restaurant);
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la recherche des restaurants : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Affiche une liste de restaurants dont le nom de la ville contient une chaîne de caractères saisie par l'utilisateur
     */
    private static void searchRestaurantByCity() {
        System.out.print("Entrez une partie du nom de la ville : ");
        String research = readString();
        try {
            // ⚡ On passe par le service au lieu du mapper
            List<Restaurant> filtered = RestaurantService.getInstance().findRestaurantsByCity(research);

            if (filtered.isEmpty()) {
                System.out.println("Aucun restaurant trouvé dans une ville contenant : " + research);
                return;
            }

            Restaurant chosen = pickRestaurant(filtered);
            if (chosen != null) {
                showRestaurant(chosen);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la recherche par ville : " + e.getMessage());
            e.printStackTrace();
        }
    }
    /**
     * L'utilisateur choisit une ville parmi celles présentes dans le système.
     *
     * @param cities La liste des villes à présnter à l'utilisateur
     * @return La ville sélectionnée, ou null si aucune ville n'a été choisie.
     */
    private static City pickCity(List<City> cities) throws SQLException {
        System.out.println("Villes disponibles :");
        for (City c : cities) {
            System.out.println(c.getZipCode() + " " + c.getCityName());
        }

        System.out.println("Entrez le NPA, ou 'NEW' pour créer une nouvelle ville :");
        String choice = readString();

        if (choice.equalsIgnoreCase("NEW")) {
            System.out.print("Nom de la nouvelle ville : ");
            String name = readString();
            System.out.print("Code postal : ");
            String zip = readString();

            //Retour de addCity provenant du service Singleton
            return CityService.getInstance().addCity(name, zip);
        } else {
            return cities.stream()
                    .filter(c -> c.getZipCode().equalsIgnoreCase(choice))
                    .findFirst()
                    .orElse(null);
        }
    }

    /**
     * L'utilisateur choisit un type de restaurant parmis ceux présents dans le système.
     *
     * @param types La liste des types de restaurant à présnter à l'utilisateur
     * @return Le type sélectionné, ou null si aucun type n'a été choisi.
     */
    private static RestaurantType pickRestaurantType(List<RestaurantType> types) {
        if (types == null || types.isEmpty()) {
            System.out.println("Aucun type disponible.");
            return null;
        }

        System.out.println("Choisissez un type de restaurant :");
        for (int i = 0; i < types.size(); i++) {
            RestaurantType t = types.get(i);
            System.out.println((i + 1) + ") " + t.getLabel() + " : " + t.getDescription());
        }

        System.out.print("Entrez le numéro ou le libellé : ");
        String input = readString().trim();

        // Si l'user tape le numéro
        if (input.matches("\\d+")) {
            int index = Integer.parseInt(input) - 1;
            if (index >= 0 && index < types.size()) {
                return types.get(index);
            }
            System.out.println("Numéro invalide.");
            return null;
        }

        // Si l'user tape le nom
        for (RestaurantType type : types) {
            if (type.getLabel().equalsIgnoreCase(input)) {
                return type;
            }
        }

        System.out.println("Type non reconnu.");
        return null;
    }


    /**
     * L'utilisateur commence par sélectionner un type de restaurant, puis sélectionne un des restaurants proposés s'il y en a.
     * Si l'utilisateur sélectionne un restaurant, ce dernier lui sera affiché.
     */
    private static void searchRestaurantByType() {
        try {
            // Récupère tous les types via le service si tu en as un,
            // sinon tu peux passer par un Set déjà connu
            List<RestaurantType> types = RestaurantTypeService.getInstance().getAllTypes(); // si tu as un service pour les types
            RestaurantType chosenType = pickRestaurantType(types);
            if (chosenType == null) return;

            // ⚡ Utilisation du service pour filtrer par type
            List<Restaurant> filtered = RestaurantService.getInstance().findRestaurantsByType(chosenType.getLabel());

            if (filtered.isEmpty()) {
                System.out.println("Aucun restaurant trouvé pour le type : " + chosenType.getLabel());
                return;
            }

            Restaurant chosen = pickRestaurant(filtered);
            if (chosen != null) showRestaurant(chosen);

        } catch (Exception e) {
            System.err.println("Erreur lors de la recherche par type : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Le programme demande les informations nécessaires à l'utilisateur puis crée un nouveau restaurant dans le système.
     */
    private static void addNewRestaurant() throws SQLException {
        String name;
        do {
            System.out.print("Nom du restaurant : ");
            name = readString().trim();
            if (name.isEmpty()) {
                System.out.println("⚠️ Le nom est obligatoire !");
            }
        } while (name.isEmpty());

        String desc;
        do {
            System.out.print("Description : ");
            desc = readString().trim();
            if (desc.isEmpty()) {
                System.out.println("⚠️ La description est obligatoire !");
            }
        } while (desc.isEmpty());

        String website;
        do {
            System.out.print("Site web : ");
            website = readString().trim();
            if (website.isEmpty()) {
                System.out.println("⚠️ Le site web est obligatoire !");
            }
        } while (website.isEmpty());

        String street;
        do {
            System.out.print("Rue : ");
            street = readString().trim();
            if (street.isEmpty()) {
                System.out.println("⚠️ La rue est obligatoire !");
            }
        } while (street.isEmpty());

        // Sélection ou création de la ville
        City city = null;
        do {
            city = pickCity(CityService.getInstance().getAllCities());
            if (city == null) {
                System.out.println("⚠️ Vous devez sélectionner une ville !");
            }
        } while (city == null);

        // Sélection du type de restaurant
        RestaurantType type = null;
        do {
            type = pickRestaurantType(RestaurantTypeService.getInstance().getAllTypes());
            if (type == null) {
                System.out.println("⚠️ Vous devez sélectionner un type de restaurant !");
            }
        } while (type == null);

        // Création via le service
        Restaurant restaurant = RestaurantService.getInstance().addRestaurant(name, desc, website, street, city, type);

        if (restaurant != null) {
            System.out.println("✅ Restaurant ajouté avec succès !");
        } else {
            System.out.println("❌ Une erreur est survenue lors de l'ajout du restaurant.");
        }
    }


    /**
     * Affiche toutes les informations du restaurant passé en paramètre, puis affiche le menu des actions disponibles sur ledit restaurant
     *
     * @param restaurant Le restaurant à afficher
     */

    private static void showRestaurant(Restaurant restaurant) {
        if (restaurant == null) {
            System.out.println("Restaurant invalide !");
            return;
        }

        try {
            // 🔹 Infos générales
            System.out.println("=== Détails du restaurant ===");
            System.out.println("Nom : " + restaurant.getName());
            System.out.println("Description : " + restaurant.getDescription());
            System.out.println("Type : " + restaurant.getType().getLabel());
            System.out.println("Site web : " + restaurant.getWebsite());
            System.out.println("Adresse : " + restaurant.getAddress().getStreet() + ", " +
                    restaurant.getAddress().getCity().getZipCode() + " " + restaurant.getAddress().getCity().getCityName());
            System.out.println();

            //LIKE et DISLIKE

            System.out.println("Chargement des likes - test");

            List<BasicEvaluation> basicEvals = EvaluationService.getInstance().getBasicEvaluations(restaurant);
            System.out.println("Likes : " + EvaluationService.getInstance().countLikes(basicEvals, true));
            System.out.println("Dislikes : " + EvaluationService.getInstance().countLikes(basicEvals, false));
            System.out.println();
            // Evaluation complete
            List<CompleteEvaluation> completeEvals = EvaluationService.getInstance().getCompleteEvaluations(restaurant);
            System.out.println("Évaluations complètes :");
            if (completeEvals.isEmpty()) {
                System.out.println("Aucune évaluation complète pour ce restaurant.");
            } else {
                for (CompleteEvaluation ce : completeEvals) {
                    System.out.println("Utilisateur : " + ce.getUsername());
                    System.out.println("Commentaire : " + ce.getComment());
                    if (ce.getGrades().isEmpty()) {
                        System.out.println("Aucune note disponible");
                    } else {
                        for (Grade g : ce.getGrades()) {
                            System.out.println(g.getCriteria().getName() + " : " + g.getGrade() + "/5");
                        }
                    }
                    System.out.println("--------------------------");
                }
            }

            // 🔹 Menu actions pour ce restaurant
            int choice;
            do {
                showRestaurantMenu();
                choice = readInt();
                proceedRestaurantMenu(choice, restaurant);
            } while (choice != 0 && choice != 6);

        } catch (Exception e) {
            System.err.println("Erreur lors de l'affichage du restaurant : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Retourne un String qui contient le détail complet d'une évaluation si elle est de type "CompleteEvaluation". Retourne un String vide s'il s'agit d'une BasicEvaluation
     *
     * @param eval L'évaluation à afficher
     * @return Un String qui contient le détail complet d'une CompleteEvaluation, ou un String vide s'il s'agit d'une BasicEvaluation
     */
    private static String getCompleteEvaluationDescription(Evaluation eval) {
        StringBuilder result = new StringBuilder();

        if (eval instanceof CompleteEvaluation) {
            CompleteEvaluation ce = (CompleteEvaluation) eval;

            result.append("Evaluation de : ").append(ce.getUsername()).append("\n");
            result.append("Commentaire : ").append(ce.getComment()).append("\n");
            for (Grade currentGrade : ce.getGrades()) {
                result.append(currentGrade.getCriteria().getName()).append(" : ").append(currentGrade.getGrade()).append("/5").append("\n");
            }
        }

        return result.toString();
    }

    /**
     * Affiche dans la console un ensemble d'actions réalisables sur le restaurant actuellement sélectionné !
     */
    private static void showRestaurantMenu() {
        System.out.println("======================================================");
        System.out.println("Que souhaitez-vous faire ?");
        System.out.println("1. J'aime ce restaurant !");
        System.out.println("2. Je n'aime pas ce restaurant !");
        System.out.println("3. Faire une évaluation complète de ce restaurant !");
        System.out.println("4. Editer ce restaurant");
        System.out.println("5. Editer l'adresse du restaurant");
        System.out.println("6. Supprimer ce restaurant");
        System.out.println("0. Revenir au menu principal");
    }

    /**
     * Traite le choix saisi par l'utilisateur
     *
     * @param choice     Un numéro d'action, entre 0 et 6. Si le numéro ne se trouve pas dans cette plage, l'application ne fait rien et va réafficher le menu complet.
     * @param restaurant L'instance du restaurant sur lequel l'action doit être réalisée
     */
    private static void proceedRestaurantMenu(int choice, Restaurant restaurant) throws  SQLException {
        switch (choice) {
            case 1:
                addBasicEvaluation(restaurant, true);
                break;
            case 2:
                addBasicEvaluation(restaurant, false);
                break;
            case 3:
                evaluateRestaurant(restaurant);
                break;
            case 4:
                editRestaurant(restaurant);

                break;
            case 5:
                boolean updated = editRestaurantAddress(restaurant);
                System.out.println(updated ? "Adresse mise à jour avec succès !" : "Erreur lors de la mise à jour.");
                break;
            case 6:
                deleteRestaurant(restaurant);
                break;
            case 0:
                break;
            default:
                break;
        }
    }

    /**
     * Ajoute au restaurant passé en paramètre un like ou un dislike, en fonction du second paramètre.
     * L'IP locale de l'utilisateur est enregistrée. S'il s'agissait d'une application web, il serait préférable de récupérer l'adresse IP publique de l'utilisateur.
     *
     * @param restaurant Le restaurant qui est évalué
     * @param like       Est-ce un like ou un dislike ?
     */
    private static void addBasicEvaluation(Restaurant restaurant, Boolean like) throws SQLException {
        EvaluationService.getInstance().addBasicEvaluation(restaurant, like);
        System.out.println("Votre vote a été pris en compte !");
    }

    /**
     * Crée une évaluation complète pour le restaurant. L'utilisateur doit saisir toutes les informations (dont un commentaire et quelques notes)
     *
     * @param restaurant Le restaurant à évaluer
     */
    private static void evaluateRestaurant(Restaurant restaurant) throws SQLException {
        String username;
        do {
            System.out.print("Nom d'utilisateur : ");
            username = readString().trim();
            if (username.isEmpty()) {
                System.out.println("⚠️ Le nom d'utilisateur est obligatoire !");
            }
        } while (username.isEmpty());

        String comment;
        do {
            System.out.print("Commentaire : ");
            comment = readString().trim();
            if (comment.isEmpty()) {
                System.out.println("⚠️ Le commentaire est obligatoire !");
            }
        } while (comment.isEmpty());

        Map<EvaluationCriteria, Integer> notes = readGrades();
        if (notes.isEmpty()) {
            System.out.println("⚠️ Vous devez fournir au moins une note !");
            notes = readGrades();
        }

        EvaluationService.getInstance().addCompleteEvaluation(restaurant, username, comment, notes);
        System.out.println("✅ Évaluation enregistrée avec succès !");
    }

    private static Map<EvaluationCriteria, Integer> readGrades() throws SQLException {
        Map<EvaluationCriteria, Integer> notes = new HashMap<>();

        List<EvaluationCriteria> criteres = EvaluationService.getInstance().getAllCriteria();
        if (criteres.isEmpty()) {
            System.out.println("Aucun critère d'évaluation disponible.");
            return notes;
        }

        for (EvaluationCriteria crit : criteres) {
            int note;
            do {
                System.out.print(crit.getName() + " (1-5) : ");
                note = readInt();
                if (note < 1 || note > 5) {
                    System.out.println("Veuillez entrer une valeur entre 1 et 5.");
                }
            } while (note < 1 || note > 5);
            notes.put(crit, note);
        }

        return notes;
    }



    /**
     * Force l'utilisateur à saisir à nouveau toutes les informations du restaurant (sauf la clé primaire) pour le mettre à jour.
     * Par soucis de simplicité, l'utilisateur doit tout resaisir.
     *
     * @param restaurant Le restaurant à modifier
     */
    private static void editRestaurant(Restaurant restaurant) throws SQLException {
        System.out.println("Edition d'un restaurant !");

        String newName;
        do {
            System.out.print("Nouveau nom : ");
            newName = readString().trim();
            if (newName.isEmpty()) {
                System.out.println("⚠️ Le nom est obligatoire !");
            } else {restaurant.setName(newName);}
        } while (newName.isEmpty());

        String newDescription;
        do {
            System.out.print("Nouvelle description : ");
            newDescription = readString().trim();
            if (newDescription.isEmpty()) {
                System.out.println("⚠️ La description est obligatoire !");
            } else {restaurant.setDescription(newDescription);}
        } while (newDescription.isEmpty());

        String newWebsite;
        do {
            System.out.print("Nouveau site web : ");
            newWebsite = readString().trim();
            if (newWebsite.isEmpty()) {
                System.out.println("⚠️ Le site web est obligatoire !");
            } else {restaurant.setWebsite(newWebsite);}
        } while (newWebsite.isEmpty());

        RestaurantType newType = null;
        do {
            newType = pickRestaurantType(RestaurantTypeService.getInstance().getAllTypes());
            if (newType == null) {
                System.out.println("⚠️ Vous devez sélectionner un type de restaurant !");
            } else {restaurant.setType(newType);}
        } while (newType == null);

        editRestaurantAddress(restaurant);

        boolean updated = RestaurantService.getInstance().updateRestaurant(restaurant);
        System.out.println(updated ? "Restaurant mis à jour avec succès !" : "Erreur lors de la mise à jour.");
    }




    /**
     * Permet à l'utilisateur de mettre à jour l'adresse du restaurant.
     * Par soucis de simplicité, l'utilisateur doit tout resaisir.
     *
     * @param restaurant Le restaurant dont l'adresse doit être mise à jour.
     */
    public static boolean editRestaurantAddress(Restaurant restaurant) throws SQLException {
        System.out.println("Edition de l'adresse d'un restaurant !");

        String newStreet;
        do {
            System.out.print("Nouvelle rue : ");
            newStreet = readString().trim();
            if (newStreet.isEmpty()) {
                System.out.println("⚠️ La rue est obligatoire !");
            }
        } while (newStreet.isEmpty());

        City city = null;
        do {
            city = pickCity(CityService.getInstance().getAllCities());
            if (city == null) {
                System.out.println("⚠️ Vous devez sélectionner une ville !");
            }
        } while (city == null);

        return RestaurantService.getInstance().updateRestaurantAddress(restaurant, newStreet, city);
    }




    /**
     * Après confirmation par l'utilisateur, supprime complètement le restaurant et toutes ses évaluations du référentiel.
     *
     * @param restaurant Le restaurant à supprimer.
     */
    private static void deleteRestaurant(Restaurant restaurant) throws SQLException {
        System.out.println("Etes-vous sûr de vouloir supprimer ce restaurant ? (O/n)");
        String choice = readString();
        if (choice.equalsIgnoreCase("o")) {
            boolean deleted = RestaurantService.getInstance().deleteRestaurant(restaurant);
            System.out.println(deleted ? "Restaurant supprimé avec succès !" : "Erreur lors de la suppression.");
        } else {
            System.out.println("Suppression annulée.");
        }
    }

    /**
     * Recherche dans le Set la ville comportant le code NPA passé en paramètre.
     * Retourne null si la ville n'est pas trouvée
     *
     * @param cities  Set de villes
     * @param zipCode NPA de la ville à rechercher
     * @return L'instance de la ville ou null si pas trouvé
     */
    private static City searchCityByZipCode(Set<City> cities, String zipCode) {
        for (City current : cities) {
            if (current.getZipCode().equalsIgnoreCase(zipCode)) {
                return current;
            }
        }
        return null;
    }

    /**
     * Recherche dans le Set le type comportant le libellé passé en paramètre.
     * Retourne null si aucun type n'est trouvé.
     *
     * @param types Set de types de restaurant
     * @param label Libellé du type recherché
     * @return L'instance RestaurantType ou null si pas trouvé
     */
    private static RestaurantType searchTypeByLabel(List<RestaurantType> types, String label) {
        for (RestaurantType current : types) {
            if (current.getLabel().equalsIgnoreCase(label)) {
                return current;
            }
        }
        return null;
    }

    /**
     * readInt ne repositionne pas le scanner au début d'une ligne donc il faut le faire manuellement sinon
     * des problèmes apparaissent quand on demande à l'utilisateur de saisir une chaîne de caractères.
     *
     * @return Un nombre entier saisi par l'utilisateur au clavier
     */
    private static int readInt() {
        int i = 0;
        boolean success = false;
        do { // Tant que l'utilisateur n'aura pas saisi un nombre entier, on va lui demander une nouvelle saisie
            try {
                i = scanner.nextInt();
                success = true;
            } catch (InputMismatchException e) {
                System.out.println("Erreur ! Veuillez entrer un nombre entier s'il vous plaît !");
            } finally {
                scanner.nextLine();
            }

        } while (!success);

        return i;
    }

    /**
     * Méthode readString pour rester consistant avec readInt !
     *
     * @return Une chaîne de caractères saisie par l'utilisateur au clavier
     */
    public static String readString() {
        return scanner.nextLine();
    }

}
