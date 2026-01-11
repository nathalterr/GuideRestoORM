package ch.hearc.ig.guideresto.persistence.mapper;

import ch.hearc.ig.guideresto.business.City;
import ch.hearc.ig.guideresto.business.RestaurantType;
import ch.hearc.ig.guideresto.persistence.AbstractMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;

import java.util.List;

import static ch.hearc.ig.guideresto.persistence.jpa.JpaUtils.getEntityManager;

public class CityMapper extends AbstractMapper<City> {



    public CityMapper() {
    }

    /**
     * Méthode de persistence en base de donnée
     * @param city à ajouter en base
     * @return l'objet City créé, ou null en cas d'erreur
     */
    @Override
    public City create(City city, EntityManager em) {
        em.persist(city);  // il devient "managed"
        return city;       // retourne l'entité persistée
    }

    /**
     * Méthode de mise à jour en base de donnée
     * @param city - l'objet City à mettre à jour
     * @return true si la mise à jour a réussi, false en cas d'erreur
     */
    @Override
    public boolean update(City city, EntityManager em) {
        em.merge(city);  // merge l'entité avec l'EM courant
        return true;
    }

    /**
     * Méthode de suppression en base de donnée
     * @param city - l'objet City à supprimer
     * @return true si la suppression a réussi, false sinon
     */
    @Override
    public boolean delete(City city, EntityManager em) {
        // Récupérer l'entité gérée par l'EM
        RestaurantType managed = em.find(RestaurantType.class, city.getId(), LockModeType.PESSIMISTIC_WRITE);
        if (managed != null) {
            em.remove(managed);
        }
        return true;
    }

    /**
     * Méthode de suppression en base de donnée
     * @param id - identifiant de l'objet City à supprimer
     * @return true si la suppression a réussi, false sinon
     */
    @Override
    public boolean deleteById(Integer id, EntityManager em) {
        City entity = em.find(City.class, id, LockModeType.PESSIMISTIC_WRITE);
        if (entity != null) {
            em.remove(entity);
        }
        return true;
    }

    /**
     * Méthode de recherche d'une ville en base de données par son identifiant.
     * @param id - identifiant du City recherché
     * @return la ville trouvée, ou null s'il n'existe pas
     */
    @Override
    public City findById(Integer id) {
        if (id == null) return null;

        try (EntityManager em = getEntityManager()) {
            return em.find(City.class, id, LockModeType.PESSIMISTIC_WRITE);
        }
    }

    /**
     * Méthode de recherche de toutes les villes en base de donnée
     * @return la liste des villes trouvées
     */
    @Override
    public List<City> findAll() {
        try (EntityManager em = getEntityManager()) {
            return em.createQuery("SELECT c FROM City c", City.class)
                    .getResultList();
        }
    }
    /**
     * Méthode de recherche d'une ville en base de données par code postal
     * @param zipCode - code postal
     * @return la liste des villes trouvées
     */
    public List<City> findByZipCode(String zipCode) {
        if (zipCode == null || zipCode.isEmpty()) return List.of();

        try (EntityManager em = getEntityManager()) {
            return em.createNamedQuery("City.findByZipCode", City.class)
                    .setParameter("zipCode", "%" + zipCode + "%")
                    .getResultList();
        }
    }

    /**
     * Méthode de recherche d'une ville en base de données par nom de ville
     * @param name - nom de la ville
     * @return la liste des villes trouvées
     */
    public City findByName(String name) {
        if (name == null || name.isEmpty()) return null;

        try (EntityManager em = getEntityManager()) {
            return em.createNamedQuery("City.findByName", City.class)
                    .setParameter("name", name)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
        }
    }
    /**
     * Méthode de vérification d'existence d'une ville en base de données par nom de ville
     * @param name - nom de la ville
     * @return true ou false
     */
    public boolean existsByName(String name) {
        if (name == null || name.isEmpty()) return false;

        try (EntityManager em = getEntityManager()) {
            Long count = em.createNamedQuery("City.existsByName", Long.class)
                    .setParameter("name", name)
                    .getSingleResult();
            return count != null && count > 0;
        }
    }


}
