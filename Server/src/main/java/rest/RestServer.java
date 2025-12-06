package rest;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import service.IAnalyzerRemote;
import javax.naming.InitialContext;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class RestServer
{

    // Cette variable va stocker la connexion vers le serveur distant (Stub).
    private static IAnalyzerRemote analyzer;

    public static void main(String[] args) throws Exception
    {
        try
        {
            System.out.println("Tentative de connexion au Backend RMI...");

            // On cherche l'objet "AIAnalyzer" dans l'annuaire (JNDI)
            // On le caste vers l'interface pour pouvoir l'utiliser
            analyzer = (IAnalyzerRemote) new InitialContext().lookup("AIAnalyzer");

            System.out.println("Connecté au serveur RMI avec succès !");

        }
        catch (Exception e)
        {
            // Si le ServerRMI n'est pas lancé, on arrête tout
            System.err.println("Erreur : Impossible de trouver le serveur RMI. Lancez ServerRMI d'abord !");
            return;
        }

        // On crée un serveur Web sur le port 8081
        HttpServer server = HttpServer.create(new InetSocketAddress(8081), 0);

        // On définit la route pour l'analyse (API)
        server.createContext("/api/analyze", new AnalysisHandler());

        // On définit la route pour afficher les fichiers HTML/CSS (Frontend)
        server.createContext("/", new StaticFileHandler());

        // On démarre le serveur
        server.setExecutor(null);
        server.start();

        System.out.println("Le site web est accessible sur : http://localhost:8081");
    }

    // Cette classe interne gère les requêtes d'analyse
    static class AnalysisHandler implements HttpHandler
    {
        @Override
        public void handle(HttpExchange exchange) throws IOException {

            // On vérifie que le navigateur envoie bien une requête POST
            if (!"POST".equals(exchange.getRequestMethod()))
            {
                exchange.sendResponseHeaders(405, -1); // Erreur 405 : Méthode non autorisée
                return;
            }

            try {
                // On lit le corps de la requête (le JSON envoyé)
                InputStream requestBody = exchange.getRequestBody();
                String body = new String(requestBody.readAllBytes(), StandardCharsets.UTF_8);

                //extraire le code et le langage du JSON
                String code = extractValue(body, "code");
                String language = extractValue(body, "language");

                System.out.println("Reçu une demande pour analyser du code : " + language);

                // C'est ici qu'on appelle la fonction sur l'autre serveur
                String analysisResult = analyzer.analyzeInput(code, language);

                // On prépare la réponse JSON pour le navigateur
                // La fonction escapeJson évite que les guillemets cassent le format
                String response = "{\"analysis\": \"" + escapeJson(analysisResult) + "\"}";

                // On ajoute les en-têtes pour dire que c'est du JSON
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().set("Content-Type", "application/json");

                // On envoie le code 200 (OK) et la taille du message
                exchange.sendResponseHeaders(200, response.getBytes().length);

                // On écrit la réponse dans le flux de sortie
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close(); // On ferme la connexion

            }
            catch (Exception e)
            {
                e.printStackTrace();
                // erreur serveur (500)
                exchange.sendResponseHeaders(500, -1);
            }
        }

        // lire le JSON manuellement
        private String extractValue(String json, String key)
        {
            // On cherche la position de la clé
            int start = json.indexOf("\"" + key + "\":\"");
            if (start == -1) return null;
            // On saute la clé pour arriver à la valeur
            start += key.length() + 4;
            // On cherche la fin de la valeur
            int end = json.indexOf("\"", start);
            // On retourne le texte entre les deux
            return end == -1 ? null : json.substring(start, end);
        }

        // nettoyer le texte avant de le mettre dans le JSON
        private String escapeJson(String text)
        {
            if (text == null)
                return "";
            // On remplace les caractères spéciaux pour ne pas casser le JSON
            return text.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
        }
    }

    // envoyer les fichiers HTML, CSS et JS
    static class StaticFileHandler implements HttpHandler
    {
        @Override
        public void handle(HttpExchange exchange) throws IOException
        {
            // On regarde quel fichier est demandé
            String path = exchange.getRequestURI().getPath();
            // Par défaut, on sert index.html
            if (path.equals("/"))
                path = "/index.html";
            // On cherche le fichier dans le dossier ressources/frontend
            InputStream fileStream = RestServer.class.getResourceAsStream("/frontend" + path);
            // Si le fichier n'existe pas
            if (fileStream == null)
            {
                exchange.sendResponseHeaders(404, -1);
                return;
            }
            // On définit le bon type de fichier
            if (path.endsWith(".css"))
                exchange.getResponseHeaders().set("Content-Type", "text/css");
            else if
                (path.endsWith(".js")) exchange.getResponseHeaders().set("Content-Type", "application/javascript");
            else
                exchange.getResponseHeaders().set("Content-Type", "text/html");

            // On envoie le fichier
            exchange.sendResponseHeaders(200, 0);
            fileStream.transferTo(exchange.getResponseBody());
            exchange.getResponseBody().close();
            fileStream.close();
        }
    }
}