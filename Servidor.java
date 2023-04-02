import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.HashMap;

public class Servidor {

	private static Socket socket;
	private static ServerSocket server;

	private static DataInputStream entrada;
	private static DataOutputStream saida;

	public final static Path path = Paths			
			.get("src\\fortune-br.txt");
	private int NUM_FORTUNES = 0;
	
	private int porta = 1025;
	
	public class FileReader {

		public int countFortunes() throws FileNotFoundException {

			int lineCount = 0;

			InputStream is = new BufferedInputStream(new FileInputStream(
					path.toString()));
			try (BufferedReader br = new BufferedReader(new InputStreamReader(
					is))) {

				String line = "";
				while (!(line == null)) {

					if (line.equals("%"))
						lineCount++;

					line = br.readLine();

				}// fim while

				//System.out.println(lineCount);
			} catch (IOException e) {
				System.out.println("SHOW: Excecao na leitura do arquivo.");
			}
			return lineCount;
		}

		//Lê todo o arquivo
		public void parser(HashMap<Integer, String> hm)
				throws FileNotFoundException {

			InputStream is = new BufferedInputStream(
					new FileInputStream(
							path.toString()));
			try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {

				int lineCount = 0;

				String line = "";
				while (!(line == null)) {

					if (line.equals("%"))
						lineCount++;

					line = br.readLine();
					StringBuffer fortune = new StringBuffer();
					while (!(line == null) && !line.equals("%")) {
						fortune.append(line + "\n");
						line = br.readLine();
					}

					hm.put(lineCount, fortune.toString());
					//System.out.println(fortune.toString());

					//System.out.println(lineCount);
				}

			} catch (IOException e) {
				System.out.println("SHOW: Excecao na leitura do arquivo.");
			}
		}

		public String read(HashMap<Integer, String> hm)
				throws FileNotFoundException {
			
			SecureRandom sr = new SecureRandom();
			
			int numAleatorio = sr.nextInt(NUM_FORTUNES);
			
			return hm.get(numAleatorio);
			
		}

		public void write(HashMap<Integer, String> hm, String novaFortuna)
				throws IOException {
			
			hm.put(++NUM_FORTUNES, novaFortuna);
			
			try {
		        Writer wr = new FileWriter(path.toString(), true); // criação de um escritor
		        BufferedWriter br = new BufferedWriter(wr); // adiciono a um escritor de buffer
		        
		        br.newLine();
		        br.write(novaFortuna);
		        br.newLine();
		        br.write("%");
		        br.close();
			}catch(IOException e) {
				e.printStackTrace();
			}
		}
	}

	//LÓGICA RETIRADA DA INTERNET, ALTERADA PARA CABER AO PROBLEMA
	public HashMap<String, String> parseRequest(String value) {
		value = value.substring(1, value.length()-1);           //remove curly brackets
		String[] keyValuePairs = value.split(",,");              //split the string to creat key-value pairs
		HashMap<String,String> map = new HashMap<>();               

		for(String pair : keyValuePairs)                        //iterate over the pairs
		{
			String pairFormat = pair.replace('"', ' ');
		    String[] entry = pairFormat.split("::");                 //split the pairs to get key and value 
		    map.put(entry[0].trim(), entry[1].trim());          //add them to the hashmap and trim whitespaces
		}
		return map;
	}
	
	public void iniciar() {
		FileReader fr = new FileReader();
		HashMap<Integer, String> hm = new HashMap<Integer, String>();
		try {
			this.NUM_FORTUNES = fr.countFortunes();
			fr.parser(hm);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		try {
			HashMap<String, String> request = new HashMap<String, String>();
			server = new ServerSocket(porta);
			System.out.println("Servidor iniciado na porta: " + porta);
			String mensagemEntrada = "";
			String mensagemSaida = "";
			
			socket = server.accept();
			System.out.println("Conectado a " + socket.getLocalPort());
			entrada = new DataInputStream(socket.getInputStream());
			saida = new DataOutputStream(socket.getOutputStream());
			
			while(true) {

				mensagemEntrada = entrada.readUTF();
				request = parseRequest(mensagemEntrada);
				
				System.out.println(request);
				
				String metodo = request.get("method");
				switch(metodo) {
					case "read":
						mensagemSaida = fr.read(hm);
						saida.writeUTF("{"
            					+ "\"result\"::"
            					+ "\""
            					+ mensagemSaida
            					+ "\""
            					+ "}");
						break;
					case "write":
						String arg = request.get("args");
						arg = arg.substring(1, arg.length()-1);
						fr.write(hm, arg);
						saida.writeUTF("{"
            					+ "\"result\"::"
            					+ "\""
            					+ arg
            					+ "\""
            					+ "}");
						break;
					default:
						break;
				}
				
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {

		new Servidor().iniciar();

	}

}
