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
# 📝 Mise en place
Pour mettre en place le projet, vous avez deux options :
1. Utilisation du fichier .ZIP
    - Extraire le contenu du fichier ZIP donné
    - Lancer IntelliJ et ouvrir le projet en sélectionnant le dossier extrait


2. Clonage du projet
- Lancer IntelliJ IDEA puis New Projet from Version Control
- Utiliser le lien suivant pour cloner le dépôt GitHub :
   ```bash
   git clone https://github.com/nathalterr/GuideRestoORM.git
    ```
- Copier le contenu du fichier "hibernate.properties.template" dans un fichier "hibernate.properties" au même endroit
- Modifier les lignes suivantes avec vos informations de connexion à la base de données Oracle.
   ```properties
   hibernate.connection.url=jdbc:oracle:thin:@db.ig.he-arc.ch:1521:ens
   hibernate.connection.username=your_username
   hibernate.connection.password=your_password
   ```
- Lancer dans votre schéma les scripts SQL situés dans le projet pour créer les tables et insérer des données de test.

