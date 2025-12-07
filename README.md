SafeCode Analyzer

Audit de sécurité assisté par IA
Réalisé par : Imen Dahmen & Yessmine Ellouze

# 🛡️ SafeCode Analyzer

> **Audit de sécurité de code assisté par Intelligence Artificielle (Google Gemini 2.0).**

**Réalisé par :** Imen Dahmen & Yesmine Ellouze

---

##  Présentation du projet

**SafeCode Analyzer** est une application distribuée innovante conçue pour analyser automatiquement le code source et détecter les failles de sécurité critiques (telles que les **Injections SQL** et les failles **XSS**).

L'application agit comme un expert en sécurité virtuel en s'appuyant sur la puissance du modèle **Google Gemini 2.0** pour fournir des explications détaillées et des corrections sur le code soumis.

---

##  Architecture du système

Le projet repose sur une **architecture hybride en 3 couches**, démontrant l'interopérabilité entre les technologies Web modernes et les protocoles Java distribués.

| Couche | Technologie | Rôle |
| :--- | :--- | :--- |
| **1. Client Web** | HTML / JS / CSS | Interface utilisateur interactive. Envoie le code à analyser au format **JSON**. |
| **2. Serveur REST** | Java (HTTP Server) | Agit comme une **Passerelle (Gateway)**. Il reçoit les requêtes HTTP du web et les transforme en appels RMI vers le backend. |
| **3. Serveur RMI** | Java RMI | **Backend & Cœur du système**. Il contient la logique métier, gère la sécurité (Clé API) et communique avec Google AI. |

> **Note :** *Vous pouvez insérer ici l'image de votre diagramme d'architecture (celle que vous m'avez montrée).*

---

## Choix Techniques

### Pourquoi cette architecture hybride ?

1.  **RMI vs CORBA :**
    *   Nous avons privilégié **Java RMI** pour la communication backend car notre environnement est homogène (100% Java). C'est une solution native plus performante et moins complexe que CORBA pour du Java-to-Java.

2.  **REST/JSON pour le Web :**
    *   Les navigateurs ne supportant pas RMI, nous utilisons un **RestServer** intermédiaire.
    *   L'échange se fait en **JSON**, le standard actuel de l'industrie, assurant la compatibilité avec n'importe quelle interface moderne.

### Le rôle clé du RestServer
Ce composant est le "pont" indispensable. Il permet de **moderniser une architecture RMI** en la rendant accessible via le protocole HTTP standard. Sans lui, le navigateur (JS) et le backend (Java) ne pourraient pas communiquer.

##  Démarrage Rapide

Suivez ces étapes pour lancer le projet localement.

###  Prérequis
*   **Java JDK 17** ou supérieur
*   **Maven**
*   Une **Clé API Google Gemini** (Google AI Studio)

###  Configuration

Avant de lancer l'application, vous devez configurer votre clé API.
1. Ouvrez le fichier : `Server/src/main/resources/config.properties`
2. Ajoutez votre clé :
```properties
google.api.key=VOTRE_CLE_API_ICI
Installation et Compilation
À la racine du projet, lancez la commande suivante pour compiler les modules :
code
Bash
mvn clean install
Lancement des serveurs
Pour que l'application fonctionne, vous devez lancer les composants dans cet ordre précis (utilisez deux terminaux séparés) :
1. Démarrer le Backend (ServerRMI) :
Ce serveur écoute sur le port 1099.
code
Bash
java -cp target/classes server.ServerRMI
# Ou via votre IDE en exécutant la classe Main du ServerRMI
2. Démarrer la Passerelle (RestServer) :
Une fois le RMI lancé, démarrez le serveur REST qui écoute sur le port 8081.
code
Bash
java -cp target/classes rest.RestServer
