# Tester le backend avec Postman

La collection contient **23 requêtes** avec des assertions sur les statuts HTTP,
le JWT et les données retournées. Elle couvre les **7 routes métier** du code
actuel et le contrôle de santé `/actuator/health`.

## Prérequis

1. Démarrer Docker Desktop et configurer la base comme indiqué dans le README du backend.
2. Démarrer le backend avec Java 21, depuis sa racine : `./mvnw.cmd spring-boot:run`.
3. Vérifier qu'il écoute sur `http://localhost:8080` (ou adapter `baseUrl`).

## Dans Postman

1. Cliquer sur **Import** et sélectionner `etudiant-api.postman_collection.json`.
2. Dans les variables de la collection, adapter `baseUrl` si nécessaire, sans `/` final.
3. Choisir **Run collection**, sélectionner les 23 requêtes et conserver leur ordre.
4. Lancer une itération et consulter les assertions réussies ou échouées.

Aucun environnement ni jeton à saisir : la collection génère des identifiants,
inscrit un utilisateur, se connecte et réutilise automatiquement le JWT.
Éviter un environnement définissant `token`, `studentId` ou les variables `test*`,
car ces valeurs prendraient priorité sur celles du scénario.

## Depuis le terminal de VS Code

Depuis la racine du backend, avec Node.js et npm installés :

```powershell
powershell -ExecutionPolicy Bypass -File .\postman\test-api.ps1
```

Pour un autre port :

```powershell
powershell -ExecutionPolicy Bypass -File .\postman\test-api.ps1 -BaseUrl http://localhost:8081
```

Le script lance la même collection avec Newman 6.2.1. Le premier lancement
nécessite Internet pour télécharger Newman dans le cache npm. Les résultats
s'affichent dans le terminal et dans `target/postman/results.xml` (JUnit).
Le code de sortie vaut `0` si les tests réussissent et est non nul en cas d'échec.
Documentation : [exécuter Newman](https://learning.postman.com/docs/reference/newman-cli/installing-running-newman/).

## Couverture

| Route | Vérifications |
| --- | --- |
| `GET /actuator/health` | HTTP 200 et état `UP` |
| `POST /api/register` | Création 201, doublon 400, champs manquants 400 |
| `POST /api/login` | Connexion 200 et JWT, mauvais mot de passe 400 |
| `GET /api/students` | Tableau, présence puis absence de l'étudiant, sans JWT 401, JWT invalide 401 |
| `POST /api/students` | Création 201, champs et identifiant, sans JWT 401 |
| `GET /api/students/{id}` | Lecture 200, modification persistée, suppression 404, sans JWT 401 |
| `PUT /api/students/{id}` | Modification 200, étudiant supprimé 404, sans JWT 401 |
| `DELETE /api/students/{id}` | Suppression 204 avec corps vide, seconde suppression 404, sans JWT 401 |

`/api/login` attend actuellement un corps **x-www-form-urlencoded**, car le
contrôleur n'utilise pas `@RequestBody`. Il renvoie le JWT en texte brut.
Un mauvais mot de passe renvoie actuellement **400**, conformément au gestionnaire
d'exceptions. Ces tests reflètent ce contrat ; adapter les assertions si le contrat change.
Les autres routes techniques Actuator (diagnostic, métriques, configuration, etc.)
ne sont pas parcourues par cette collection.

## Données de test et échecs

Utiliser une base de développement/test. Chaque exécution crée un compte unique
`postman-<UUID>` qui **reste en base** : aucune route de suppression d'utilisateur
n'existe. Seul l'étudiant créé par le scénario est modifié puis supprimé.
Si l'inscription, la connexion ou la création d'étudiant échoue, le scénario
s'arrête avec une assertion en échec. Les autres échecs n'interrompent pas le
scénario afin de permettre la suppression finale de l'étudiant.
Une interruption manuelle ou une panne réseau peut laisser cet étudiant en base ;
son identifiant reste dans la variable `studentId` de la collection exécutée.
Ne pas exporter les valeurs générées du JWT ou du mot de passe pour les partager.

La réussite du scénario confirme les comportements vérifiés, pas l'absence de
tous les défauts possibles (validation des champs, charge, concurrence, etc.).
