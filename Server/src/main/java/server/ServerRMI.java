package server;
import javax.naming.InitialContext;
import java.rmi.registry.LocateRegistry;
import service.AnalyzerImpl;

public class ServerRMI
{
    public static void main(String[] args)
    {
        try
        {
            System.out.println("Démarrage du Serveur Backend (RMI)...");

            // On crée le registre RMI sur le port 1099
            // C'est comme un annuaire pour nos objets
            LocateRegistry.createRegistry(1099);

            // On instancie l'objet qui fait le vrai travail
            // C'est cette classe qui contient le code pour contacter Google
            AnalyzerImpl analyzer = new AnalyzerImpl();

            // On enregistre cet objet dans l'annuaire (JNDI)
            // On lui donne le nom "AIAnalyzer" pour le retrouver plus tard.
            InitialContext ctx = new InitialContext();
            ctx.rebind("AIAnalyzer", analyzer);

            System.out.println("Le serveur est prêt ! En attente de connexion...");

        }
        catch (Exception e)
        {
            System.err.println("Erreur au démarrage du serveur :");
            e.printStackTrace();
        }
    }
}