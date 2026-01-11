package ch.hearc.ig.guideresto.persistence.mapper;

import ch.hearc.ig.guideresto.business.RestaurantType;
import ch.hearc.ig.guideresto.persistence.AbstractMapper;
import ch.hearc.ig.guideresto.persistence.jpa.JpaUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.NoResultException;

import java.util.List;

public class RestaurantTypeMapper extends AbstractMapper<RestaurantType> {

    public RestaurantTypeMapper() {
    }

    /**
     * Méthode de persistence en base de donnée
     * @param type à ajouter en base
     * @return l'objet RestaurantType créé, ou null en cas d'erreur
     */
    @Override
    public RestaurantType create(RestaurantType type, EntityManager em) {
        em.persist(type);  // il devient "managed"
        return type;       // retourne l'entité persistée
    }

    /**
     * Méthode de mise à jour en base de donnée
     * @param type - l'objet RestaurantType à mettre à jour
     * @return true si la mise à jour a réussi, false en cas d'erreur
     */
    @Override
    public boolean update(RestaurantType type, EntityManager em) {
        em.merge(type);  // merge l'entité avec l'EM courant
        return true;
    }
    /**
     * Méthode de suppression en base de donnée
     * @param type - l'objet RestaurantType à supprimer
     * @return true si la suppression a réussi, false sinon
     */
    @Override
    public boolean delete(RestaurantType type, EntityManager em) {
        // Récupérer l'entité gérée par l'EM
        RestaurantType managed = em.find(RestaurantType.class, type.getId(), LockModeType.PESSIMISTIC_WRITE);
        if (managed != null) {
            em.remove(managed);
        }
        return true;
    }
    /**
     * Méthode de suppression en base de donnée
     * @param id - identifiant du RestaurantType à supprimer
     * @return true si la suppression a réussi, false sinon
     */
    @Override
    public boolean deleteById(Integer id, EntityManager em) {
        RestaurantType entity = em.find(RestaurantType.class, id, LockModeType.PESSIMISTIC_WRITE);
        if (entity != null) {
            em.remove(entity);
        }
        return true;
    }

    /**
     * Méthode de recherche d'un type de restaurant en base de données par son identifiant.
     * @param id - identifiant du type de restaurant recherché
     * @return le type de restaurant trouvé, ou null s'il n'existe pas
     */
    @Override
    public RestaurantType findById(Integer id) {
        if (id == null) return null;

        try (EntityManager em = JpaUtils.getEntityManager()) {
            try {
                return em.createNamedQuery("RestaurantType.findById", RestaurantType.class)
                        .setParameter("id", id)
                        .getSingleResult();
            } catch (NoResultException e) {
                return null;
            }
        }
    }

    /**
     * Méthode de recherche de tous les types de restaurants en base de donnée
     * @return la liste des types de restaurants trouvés
     */
    @Override
    public List<RestaurantType> findAll() {
        try (EntityManager em = JpaUtils.getEntityManager()) {
            return em.createNamedQuery("RestaurantType.findAll", RestaurantType.class)
                    .getResultList();
        }
    }

    /**
     * Méthode de recherche de types de restaurants en base de données par nom
     * @param name - nom du type
     * @return la liste des types de restaurant trouvée
     */
    public List<RestaurantType> findByName(String name) {
        if (name == null || name.isEmpty()) return List.of();

        try (EntityManager em = JpaUtils.getEntityManager()) {
            return em.createQuery(
                            "SELECT rt FROM RestaurantType rt WHERE rt.label = :name",
                            RestaurantType.class
                    )
                    .setParameter("name", name)
                    .getResultList();
        }
    }

    /**
     * Méthode de recherche de types de restaurants en base de données par description
     * @param description - description du type
     * @return la liste des types de restaurant trouvés
     */
    public List<RestaurantType> findByDescription(String description) {
        try (EntityManager em = JpaUtils.getEntityManager()) {
            return em.createNamedQuery("RestaurantType.findByDescription", RestaurantType.class)
                    .setParameter("description", "%" + description + "%")
                    .getResultList();
        }
    }
}
