import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import java.io.*;
import java.util.*;




public class Main {

	private static	Map<String, Integer> v0Init = new Hashtable<String, Integer>();
	private static	Map<String, Integer> vnInit = new Hashtable<String, Integer>();
	private static  Map<String, Double> distanceVnEtV0 = new Hashtable<String, Double>();
	private static ArrayList<String> dictionnaire;


	/**
	 * initialiser le tableau qui contient les mots du dictionnaire
	 * @throws IOException 
	 */
	public static void initialiserTabDico() throws IOException{
		dictionnaire = new ArrayList<>();
		
		//lire fichier texte qui contient les mots
		File dicoFile = new File("properties/dictionnaire.txt"); 
		FileReader fr = new FileReader(dicoFile);
		BufferedReader br = new BufferedReader(fr);
		
		String line = br.readLine();
		
		while (line != null){
			dictionnaire.add(line);
			line = br.readLine();
		}
		
		br.close();
		fr.close();
		
	}

	/**
	 * il faut utiliser un fichier model pour creer le vector 0
	 * @throws Exception 
	 */
	public static void remplirVectorZero() throws Exception{
		//email

		// lire le fichier model 
		File fTest = new File("mailModel/Offres d'emploi_stage - Chargé de clientèle _ hotliner (H_F) - Aéos Consultants - Aéos Consultants - Recrutement (no-reply@ubiposting.com) - 2015-11-17 1318.eml");
		//File fTest = new File("mailModel/Annonce de stage de fin d'études en tine Delerce-Mauris - 2016-02-05 1657.eml"); 
		//File fTest = new File("mailModel/Offre de stage - Ralitsa BONEVA (RBONEVA@alterea.fr) - 2016-01-21 1200.eml");
		
		Properties p = System.getProperties();
		p.put("mail.host", "smtp.dummydomain.com");
		p.put("mail.transport.protocol", "smtp");
		Session sessionMail = Session.getDefaultInstance(p, null);
		InputStream source = new FileInputStream(fTest);
		MimeMessage message = new MimeMessage(sessionMail, source);
		
		String contenuMail = getTextFromMessage(message).toLowerCase() +" "+ fTest.getName().toLowerCase();
		
		//System.out.println(contenuMail);
				
		//compter ocurrences fichier model
		Pattern pa;
		Matcher m;
		//System.out.println(contenu);
		for (String mot : dictionnaire) {
			
			int nbOcurrences = 0;
			pa = Pattern.compile(mot);
			m = pa.matcher(contenuMail);

			
			while (m.find()) {
				nbOcurrences++;
			}
			
			v0Init.put(mot, nbOcurrences);
		}
		
		//System.out.println(v0Init);

	}

	/*
	 * remplir le vecteur n a comparer avec le v0
	 */
	public static void remplirVn(String contenuMail, String fileName) throws FileNotFoundException, IOException{


		
		Pattern p;
		Matcher m;
		for (String mot : dictionnaire) {
			
			int nbOcurrences = 0;
			p = Pattern.compile(mot);
			m = p.matcher(contenuMail);

			while (m.find()) {
				nbOcurrences++;
			}
			
			vnInit.put(mot, nbOcurrences);
		}

		calculerDistance(v0Init.values(), vnInit.values(), fileName);
	}

	//retourne la distance de Euclidean entre le vector (array de int) n et 0
	public static void calculerDistance(Collection<Integer> v0, Collection<Integer> vn, String fileName){

		if (v0.size() != vn.size()) {
			throw new IllegalArgumentException("les vecteurs doivent avoir la meme taille (nb d'elements)");
		}

		Object[] testArrayV0 = v0.toArray();
		Object[] testArrayVn = vn.toArray();

		// vector n - vector 0
		int[] resSustraction = new int[v0.size()];

		for (int i = 0; i < vn.size(); i++) {
			resSustraction[i] = ((int) testArrayVn[i]) - ((int) testArrayV0[i]);

		}

		//calculler la norme euclidienne
		double sum = 0.0;
		double euclideanNorm = 0.0;
		for (int i = 0; i < resSustraction.length; i++) {
			sum = sum + (resSustraction[i] * resSustraction[i]);
		}
		

		euclideanNorm = Math.sqrt(sum);

		//calculer le produit scallaire
		double produit = 0.0;
		for (int i = 0; i < vn.size(); i++) {
			produit += ((int) testArrayVn[i]) * ((int) testArrayV0[i]);
		}
		
		double test = produit/euclideanNorm;
		
		distanceVnEtV0.put(fileName, test);
		
		
	}


	
	/**
	 * classe les mail spam et pas span
	 * le classement est fait d'accord a la distance qu'il y a entre le vector model (v0) et le
	 * vector d'ocurrences de chaque fichier
	 * 
	 * le hashtable "distanceVnEtV0" contient le nom du fichier (mail) et sa distance avec
	 * le vector model
	 */
	public static void classementMails(){
		int mailSpam = 0;
		int mailOk = 0;
		
		for(Map.Entry<String, Double> e : distanceVnEtV0.entrySet()){
			if (e.getValue() < 0.8) {
				mailSpam ++;
				System.out.println(e.getValue() +" "+ e.getKey());
				
				
			}else {
				mailOk ++;
				//System.out.println(e.getValue() +"*"+ e.getKey());
			}
			
		}
		
		System.out.println("Nombre mails classifies comme spam: " + mailSpam);
		System.out.println("Nombre mails classifies comme OK: " + mailOk);
	}



	public static void parcourirLireTextes() throws Exception{
		
		File dir = new File("textes");
		
		File[] textes = dir.listFiles();
	
		if (textes == null) {
			System.out.println("Le repertoire est vide ou il existe pas");
		}else{

			for (int i=0; i < textes.length; i++) {
				
				Properties p = System.getProperties();
				p.put("mail.host", "smtp.dummydomain.com");
				p.put("mail.transport.protocol", "smtp");
				Session sessionMail = Session.getDefaultInstance(p, null);
				InputStream source = new FileInputStream(textes[i]);
				MimeMessage message = new MimeMessage(sessionMail, source);
				
				String contenuMail = getTextFromMessage(message).toLowerCase() +" "+ textes[i].getName().toLowerCase();
				
						
						//on passe la chaine de caracteres qui contient le contenu du mail a verifier
						remplirVn(contenuMail, textes[i].getName());
			}
			
		}
	}


	  private static String getTextFromMessage(Message message) throws Exception {
	        String result = "";
	        if (message.isMimeType("text/plain")) {
	            result = message.getContent().toString();
	        } else if (message.isMimeType("multipart/*")) {
	            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
	            result = getTextFromMimeMultipart(mimeMultipart);
	        }
	        return result;
	    }

	  private static String getTextFromMimeMultipart(
	            MimeMultipart mimeMultipart) throws Exception{
	        String result = "";
	        int count = mimeMultipart.getCount();
	        for (int i = 0; i < count; i++) {
	            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
	            if (bodyPart.isMimeType("text/plain")) {
	                result = result + "\n" + bodyPart.getContent();
	                break; // without break same text appears twice in my tests
	            } else if (bodyPart.isMimeType("text/html")) {
	                String html = (String) bodyPart.getContent();
	                result = result + "\n" + org.jsoup.Jsoup.parse(html).text();
	            } else if (bodyPart.getContent() instanceof MimeMultipart){
	                result = result + getTextFromMimeMultipart((MimeMultipart)bodyPart.getContent());
	            }
	        }
	        return result;
	    }
	  
	public static void main(String[] args) throws Exception {
		initialiserTabDico();
		remplirVectorZero();
		parcourirLireTextes();	
		classementMails();

	}

}
