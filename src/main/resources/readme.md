# GuideResto

GuideResto est une application Java console pour gérer des restaurants, leurs types, leurs villes et leurs évaluations.

---

# ‍💻 Auteur
- Nathan Altermatt
- Stéphane Thiébaud

---

## ⚙️ Configuration

- SDK : OpenJDK 21
- Maven 

---
# Mise en place 
1. 
- Cloner le dépôt GitHub
   ```bash
   git clone nathanaltermatt/GuideResto.git
    ```
- Créer un fichier Hibernate.properties dans src/main/resources avec les informations de connexion à la base de données Oracle. Exemple :
   ```properties
   hibernate.connection.url=jdbc:oracle:thin:@db.ig.he-arc.ch:1521:ens
   hibernate.connection.username=your_username
   hibernate.connection.password=your_password
   ```
- Lancer dans votre schéma les scripts SQL situés dans le projet pour créer les tables et insérer des données de test.
   
2. Utiliser le .ZIP
   - Extraire le contenu du fichier ZIP donné
   - Lancer IntelliJ et ouvrir le projet en sélectionnant le dossier extrait

