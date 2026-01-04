findRestaurantsByType dans user service en gros les labels je crois que c'est unique donc on pourrait sans autre faire qu'il prenne qu'un label bref

![img.png](img.png)

EN dehors du dessus il reste : 
- Verrous
- Readme
- Javadoc
- gestion des exceptions



public static void inTransaction(Consumer<EntityManager> consumer) {
EntityManager em = JpaUtils.getEntityManager();
EntityTransaction transaction = em.getTransaction();
try {
transaction.begin();
consumer.accept(em);
em.flush();
transaction.commit();
} catch (Exception ex) {
if (transaction.isActive()) {
transaction.rollback();
}
throw ex;
}
}
