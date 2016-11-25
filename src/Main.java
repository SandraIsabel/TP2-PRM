import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.management.relation.RelationServiceNotRegisteredException;
import javax.security.auth.login.CredentialExpiredException;

public class Main {

	private static	Hashtable<String, Integer> v0 = new Hashtable<String, Integer>();
	private static	Hashtable<String, Integer> vn = vn = new Hashtable<String, Integer>();
	private static Hashtable<String, Double> distanceVnEtV0 = new Hashtable<String, Double>();



	public static void remplirVectorZero() throws FileNotFoundException, IOException{
		//Lire le dictionnaire pour creer le vecteur (fichier properties)

		Iterator<Object> it = chargerProperties().keySet().iterator();

		while(it.hasNext()){
			String propertyName = (String) it.next();
			String propertyValue = chargerProperties().getProperty(propertyName);
			v0.put(propertyName, Integer.parseInt(propertyValue));

		}





	}

	/*
	 * remplir le vecteur n a comparer avec le v0
	 */
	public static void remplirVn(String contenuMail, String fileName) throws FileNotFoundException, IOException{


		Iterator<Object> it = chargerProperties().keySet().iterator();
		Pattern p;
		Matcher m;
		while (it.hasNext()) {
			//le mot qu'on cherche dans le mail
			String propertyName = (String) it.next();
			// le nombre d'ocurrences dans le mail du mot cherché
			int nbOcurrences = 0;
			p = Pattern.compile("(^|(\\s+))"+propertyName);
			m = p.matcher(contenuMail);

			while (m.find()) {
				nbOcurrences++;
			}

			vn.put(propertyName, nbOcurrences);
		}

		//System.out.println(v0 );
		//System.out.println(vn);
		calculerDistance(v0.values(), vn.values(), fileName);
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

			//System.out.println(resSustraction[i]);
		}

		double sum = 0.0;
		double euclideanNorm = 0.0;
		for (int i = 0; i < resSustraction.length; i++) {
			sum = sum + (resSustraction[i] * resSustraction[i]);
		}
		//System.out.println("sum : " + sum);
		//

		euclideanNorm = Math.sqrt(sum);

		//System.out.println("norm : " + euclideanNorm);

		distanceVnEtV0.put(fileName, euclideanNorm);
		//System.out.println("*************************************");




	}



	public static Properties chargerProperties() throws FileNotFoundException, IOException{
		Properties dictionnaire = new Properties();
		dictionnaire.load(new FileInputStream("properties/dictionnaire.properties"));

		return dictionnaire;
	}

	public static void parcourirLireTextes(int position) throws IOException, FileNotFoundException, IOException{
		File texte = new File(System.getProperty("user.home") + "/Desktop/AllTextes");
		File[] textes = texte.listFiles();
		//String fileName = "";
		//		int iAux = 0;
		//		int limite = 0;

		//		if (position == 0) {
		//			limite = 500;
		//		
		//		}else if(position == 500){
		//			limite = 1000;
		//			iAux = 500;
		//		}else if (position == 1000) {
		//			limite = 1500;
		//			iAux = 1000;
		//		}else if (position == 1500) {
		//			limite = textes.length;
		//			iAux = 1500;
		//		}

		System.out.println(textes.length + "****************");

		if (textes != null) {

			// Implementation of an ExecutorService
			ExecutorService executorService = Executors.newFixedThreadPool(textes.length);
			for (File file : textes) {
				
				Runnable task = () -> {
					try {
						
						File fTest = new File(file.getPath());
						FileReader fr = new FileReader(fTest);
						BufferedReader br = new BufferedReader(fr);

						String line = br.readLine();
						String contenu = "";

						while (line != null) {
							contenu += line;
							line = br.readLine();
						}

						String fileName = fTest.getName();
						br.close();
						fr.close();
						//				InputStream stream = new FileInputStream(textes[i].getPath());
						//				InputStreamReader streamReader = new InputStreamReader(stream);
						//				BufferedReader br = new BufferedReader(streamReader);
						//				String ligne;
						//				fileName = textes[i].getName();
						//				
						//				while ((ligne = br.readLine()) != null) {
						//					contenu+=ligne;
						//				}
						//br.close();

						//on passe la chaine de caracteres qui contient le contenu du mail a verifier
						//remplirVn(contenu, fileName);

						System.out.println("fichier = " + fileName);

					} catch (Exception e) {
						// TODO: handle exception
						System.out.println(e.getMessage());
					}
				};
				
				executorService.execute(task);
			}
			
			
			System.out.println("Terminated");
		}
	}




	public static void main(String[] args) throws FileNotFoundException, IOException {
		// TODO Auto-generated method stub
		remplirVectorZero();
		parcourirLireTextes(0);
		//parcourirLireTextes(500);
		//parcourirLireTextes(1000);
		//parcourirLireTextes(1500);
		System.out.println(distanceVnEtV0);

	}

}
