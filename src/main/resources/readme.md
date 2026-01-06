![img.png](img.png)

Il reste : 
- Verrous - Ca je sais pas comment faire entre les transactions, la modif de la DB, etc
- Readme
- Javadoc
- gestion des exceptions
- Mettre en place le InTransaction (va avec Verrous)
- faire le trigger (ptet genre avec les bails de si machin j'incrémente, sinon non)

Questions : 
- Verrous
  - InTransaction
- Cache
- Logging SQL
- Javadoc - a quel niveau de détail on va ?


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
