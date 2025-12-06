package service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.io.InputStream;
import java.util.Properties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.List;
import java.util.HashMap;

// UnicastRemoteObject: pour être accessible via RMI
public class AnalyzerImpl extends UnicastRemoteObject implements IAnalyzerRemote
{
    // On charge la clé API une seule fois au début
    private static final String API_KEY = loadApiKey();
    // Mapper pour créer le JSON
    private static final ObjectMapper mapper = new ObjectMapper();

    //lire le fichier config.properties
    private static String loadApiKey()
    {
        try
        {
            Properties prop = new Properties();
            // On cherche le fichier dans le classpath
            InputStream input = AnalyzerImpl.class.getClassLoader().getResourceAsStream("config.properties");

            if (input == null)
            {
                System.err.println("Attention : Fichier config introuvable !");
                return null;
            }
            // On charge les propriétés et on récupère la clé
            prop.load(input);
            return prop.getProperty("google.ai.api.key");

        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    // Le constructeur
    public AnalyzerImpl() throws RemoteException
    {
        super();
    }

    // la méthode qui est appelée à distance par le RestServer
    @Override
    public String analyzeInput(String code, String language) throws RemoteException
    {
        // On délègue le travail à la fonction privée
        return callAIAPI(code, language);
    }

    // Fonction principale qui parle à l'API Google
    private String callAIAPI(String code, String language)
    {
        try
        {
            // Vérification
            if (API_KEY == null)
                return "Erreur : La clé API est manquante.";

            // URL de l'API Google
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + API_KEY;

            // On construit le prompt
            String prompt = "Analyze this " + language + " code for security vulnerabilities. " +
                    "Keep the response concise and to the point. " +
                    "For each issue, use this exact format:\n" +
                    "1. **Vulnerability Name**\n" +
                    "Brief explanation of the risk.\n" +
                    "```\n" +
                    "// Corrected Code Snippet\n" +
                    "```\n" +
                    "Do not include generic advice, only specific issues found in the code.\n\n" +
                    "Code to analyze:\n" + code;

            // On prépare la structure de données complexe demandée par Google
            Map<String, Object> requestMap = new HashMap<>();
            Map<String, Object> contents = new HashMap<>();
            Map<String, Object> parts = new HashMap<>();

            // On met le texte dans la structure
            parts.put("text", prompt);
            contents.put("parts", List.of(parts));
            requestMap.put("contents", List.of(contents));

            // On transforme la Map en chaîne de caractères JSON
            String json = mapper.writeValueAsString(requestMap);
            System.out.println("Envoi de la demande à Google AI...");

            // On crée la requête HTTP POST
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            // On envoie la requête avec un client HTTP
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200)
            {
                // On extrait la réponse du JSON
                return parseAIResponse(response.body());
            }
            else
            {

                return "Erreur Google : " + response.statusCode();
            }

        } catch (Exception e)
        {
            e.printStackTrace();
            return "Erreur lors de l'appel IA : " + e.getMessage();
        }
    }

    //  extraire le texte de la réponse JSON
    private String parseAIResponse(String jsonResponse)
    {
        try
        {
            // On lit le JSON dans une Map
            Map<String, Object> responseMap = mapper.readValue(jsonResponse, Map.class);

            // On navigue dans l'arborescence du JSON : candidates -> content -> parts -> text
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseMap.get("candidates");

            if (candidates != null && !candidates.isEmpty())
            {
                Map<String, Object> firstCandidate = candidates.get(0);
                Map<String, Object> content = (Map<String, Object>) firstCandidate.get("content");
                List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");

                if (parts != null && !parts.isEmpty())
                {
                    // On a trouvé le texte
                    return (String) parts.get(0).get("text");
                }
            }
            return "Aucune réponse trouvée";

        }
        catch (Exception e)
        {
            return "Erreur de lecture du JSON : " + e.getMessage();
        }
    }
}