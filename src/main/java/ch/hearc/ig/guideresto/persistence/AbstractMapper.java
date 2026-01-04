package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.IBusinessObject;
import jakarta.persistence.Entity;
import java.util.List;
import static ch.hearc.ig.guideresto.persistence.jpa.JpaUtils.getEntityManager;

public abstract class AbstractMapper<T extends IBusinessObject> {

    protected AbstractMapper() {
    }

    public abstract T findById(Integer id);
    public abstract List<T> findAll();
    public abstract T create(T object);
    public abstract boolean update(T object);
    public abstract boolean delete(T object);
    public abstract boolean deleteById(Integer id);

    /**
     * Vérifie si un objet avec l'ID donné existe.
     * @param id the ID to check
     * @return true si l'objet existe, false sinon
     */
    public boolean exists(Integer id) {
        return getEntityManager().find(Entity.class, id) != null;
    }

    /**
     * Compte le nombre d'objets en base de données.
     * @return
     */
    public Long count() {
        Class<T> entityClass = (Class<T>) getEntityManager().getClass();
        String ql = "SELECT COUNT(e) FROM " + entityClass.getSimpleName() + " e";
        return getEntityManager().createQuery(ql, Long.class).getSingleResult();
    }
}
