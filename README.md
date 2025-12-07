SafeCode Analyzer

Audit de sécurité assisté par IA
Réalisé par : Imen Dahmen & Yessmine Ellouze

Présentation du projet

SafeCode Analyzer est une application distribuée conçue pour repérer les failles de sécurité dans le code source, telles que les attaques SQL Injection et XSS, grâce au modèle Google Gemini 2.0.

Architecture du système

Le projet s’appuie sur une architecture hybride en 3 couches combinant Web, REST et RMI :

Client Web (HTML/JavaScript) : Interface interactive qui échange des données au format JSON.

Serveur REST (Gateway) : Joue le rôle d’intermédiaire en transformant les requêtes HTTP/JSON en appels RMI Java.

Serveur RMI (Backend) : Contient la logique métier et communique avec l’API Google AI.

Choix techniques
Pourquoi ce type d’architecture ?

RMI vs CORBA : RMI a été privilégié car l’environnement est entièrement basé sur Java, ce qui le rend plus efficace que CORBA dans ce contexte.

L’échange avec le client web se fait via REST/JSON, un standard moderne et largement utilisé.

Rôle du serveur REST

Le RestServer est essentiel pour assurer la communication entre le navigateur (HTTP) et le backend Java fonctionnant en RMI.

Démarrage rapide
Prérequis

Java 17

Maven

Clé API Google

Configuration

Ajoutez votre clé d’API dans le fichier suivant :
Server/src/main/resources/config.properties

Compilation du projet
mvn clean install

Lancement des serveurs

Démarrer le backend : exécuter server.ServerRMI (port 1099)

Démarrer la passerelle : exécuter rest.RestServer (port 8081)
