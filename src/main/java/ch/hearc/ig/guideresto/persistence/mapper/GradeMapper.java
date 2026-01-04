package ch.hearc.ig.guideresto.persistence.mapper;

import ch.hearc.ig.guideresto.business.CompleteEvaluation;
import ch.hearc.ig.guideresto.business.EvaluationCriteria;
import ch.hearc.ig.guideresto.business.Grade;
import ch.hearc.ig.guideresto.business.Restaurant;
import ch.hearc.ig.guideresto.persistence.AbstractMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;

import java.sql.*;
import java.util.*;

import static ch.hearc.ig.guideresto.persistence.ConnectionUtils.getConnection;
import static ch.hearc.ig.guideresto.persistence.jpa.JpaUtils.getEntityManager;

public class GradeMapper extends AbstractMapper<Grade> {

    public GradeMapper() {
    }

    /**
     * Méthode de persistence en base de donnée
     * @param grade à ajouter en base
     * @return l'objet Grade créé, ou null en cas d'erreur
     */
    public Grade create(Grade grade) {
        try (EntityManager em = getEntityManager()) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                em.persist(grade);
                tx.commit();
                return grade;
            } catch (Exception e) {
                if (tx.isActive()) tx.rollback();
                return null;
            }
        }
    }

    /**
     * Méthode de mise à jour en base de donnée
     * @param grade - l'objet Grade à mettre à jour
     * @return true si la mise à jour a réussi, false en cas d'erreur
     */
    public boolean update(Grade grade) {
        try (EntityManager em = getEntityManager()) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                em.merge(grade);
                tx.commit();
                return true;
            } catch (Exception e) {
                if (tx.isActive()) tx.rollback();
                return false;
            }
        }
    }

    /**
     * Méthode de suppression en base de donnée
     * @param grade - l'objet Grade à supprimer
     * @return true si la suppression a réussi, false sinon
     */

    @Override
    public boolean delete(Grade grade) {
        return deleteById(grade.getId());
    }

    /**
     * Méthode de suppression en base de donnée
     * @param id - identifiant de l'objet Grade à supprimer
     * @return true si la suppression a réussi, false sinon
     */
    @Override
    public boolean deleteById(Integer id) {
        try (EntityManager em = getEntityManager()) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();

                Grade ref = em.getReference(Grade.class, id);
                em.remove(ref);

                tx.commit();
                return true;

            } catch (EntityNotFoundException e) {
                if (tx.isActive()) tx.rollback();
                return false;

            } catch (Exception ex) {
                if (tx.isActive()) tx.rollback();
                return false;
            }
        }
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

