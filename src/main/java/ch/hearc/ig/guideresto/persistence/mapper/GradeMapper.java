package ch.hearc.ig.guideresto.persistence.mapper;

import ch.hearc.ig.guideresto.business.*;
import ch.hearc.ig.guideresto.persistence.AbstractMapper;
import jakarta.persistence.*;
import java.sql.*;
import java.util.*;
import static ch.hearc.ig.guideresto.persistence.jpa.JpaUtils.getEntityManager;

public class GradeMapper extends AbstractMapper<Grade> {

    public GradeMapper() {
    }

    /**
     * Méthode de persistence en base de donnée
     * @param grade à ajouter en base
     * @return l'objet Grade créé, ou null en cas d'erreur
     */
    public Grade create(Grade grade, EntityManager em) {
        em.persist(grade);  // il devient "managed"
        return grade;       // retourne l'entité persistée
    }

    /**
     * Méthode de mise à jour en base de donnée
     * @param grade - l'objet Grade à mettre à jour
     * @return true si la mise à jour a réussi, false en cas d'erreur
     */
    public boolean update(Grade grade, EntityManager em) {
        em.merge(grade);  // merge l'entité avec l'EM courant
        return true;
    }

    /**
     * Méthode de suppression en base de donnée
     * @param grade - l'objet Grade à supprimer
     * @return true si la suppression a réussi, false sinon
     */

    @Override
    public boolean delete(Grade grade, EntityManager em) {
        // Récupérer l'entité gérée par l'EM
        RestaurantType managed = em.find(RestaurantType.class, grade.getId(), LockModeType.PESSIMISTIC_WRITE);
        if (managed != null) {
            em.remove(managed);
        }
        return true;
    }

    /**
     * Méthode de suppression en base de donnée
     * @param id - identifiant de l'objet Grade à supprimer
     * @return true si la suppression a réussi, false sinon
     */
    @Override
    public boolean deleteById(Integer id, EntityManager em) {
        Grade entity = em.find(Grade.class, id, LockModeType.PESSIMISTIC_WRITE);
        if (entity != null) {
            em.remove(entity);
        }
        return true;
    }

    /**
     * Méthode de recherche d'une note en base de données par son identifiant.
     * @param id - identifiant du Grade recherché
     * @return la note trouvée, ou null s'il n'existe pas
     */
    public Grade findById(Integer id) {
        try (EntityManager em = getEntityManager()) {
            try {
                return em.createNamedQuery("Grade.findById", Grade.class)
                        .setParameter("id", id)
                        .getSingleResult();
            } catch (NoResultException e) {
                return null;
            }
        }
    }

    /**
     * Méthode de recherche de toutes les notes en base de donnée
     * @return la liste des notes trouvées
     */
    public List<Grade> findAll() {
        try (EntityManager em = getEntityManager()) {
            return em.createNamedQuery("Grade.findAll", Grade.class)
                    .getResultList();
        }
    }

    /**
     * Méthode de recherche d'une note en base de données par valeur de note
     * @param gradeValue - valeur de la note
     * @return la liste des notes trouvées
     */
    public List<Grade> findByGrade(Integer gradeValue) {
        try (EntityManager em = getEntityManager()) {
            return em.createNamedQuery("Grade.findByGrade", Grade.class)
                    .setParameter("grade", gradeValue)
                    .getResultList();
        }
    }

    /**
     * Méthode de recherche d'une note en base de données par évaluation complète
     * @param completeEvaluation - évaluation complète concernée
     * @return le Set des notes trouvées
     */
    public Set<Grade> findByEvaluation(CompleteEvaluation completeEvaluation) {
        try (EntityManager em = getEntityManager()) {
            List<Grade> grades = em.createNamedQuery(
                            "Grade.findByEvaluation",
                            Grade.class
                    )
                    .setParameter("evaluation", completeEvaluation)
                    .getResultList();

            return new LinkedHashSet<>(grades);
        }
    }
}

