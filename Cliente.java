
/**
 * Laboratorio 1 de Sistemas Distribuidos
 * 
 * Autor: Lucio A. Rocha
 * Ultima atualizacao: 17/12/2022
 */

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashMap;

public class Cliente {
    
    private static Socket socket;
    private static DataInputStream entrada;
    private static DataOutputStream saida;
    
    private int porta=1025;
    
    public HashMap<String, String> parseResult(String value) {
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
    
    public String printHashMap(HashMap<String, String> hm) {
    	String str = "{\nresult:" + hm.get("result") + "\n}";
    	
    	return str;
    }
    
    public void iniciar(){
    	System.out.println("Cliente iniciado na porta: "+porta);
    	
    	try {
            
            socket = new Socket("127.0.0.1", porta);
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String comando = "";
            String mensagemIda = "";
            HashMap<String,String> mensagemVolta = new HashMap<>();
            entrada = new DataInputStream(socket.getInputStream());
            saida = new DataOutputStream(socket.getOutputStream());
             
            while(true) {
	            //MENU DO USUÁRIO
            	System.out.print("\n[CLIENT]>>>");
            	comando = br.readLine();
            	switch(comando) {
            		case "\\read":
            			System.out.println("[CLIENT](read)");
            			saida.writeUTF("{"
            					+ "\"method\"::\"read\",,"
            					+ "\"args\"::[\"\"]"
            					+ "}");
            			mensagemVolta = parseResult(entrada.readUTF());
            			System.out.print("[SERVER](read):\n" + printHashMap(mensagemVolta));
            			break;
            		case "\\write":
            			System.out.print("[CLIENT](write)\n>>>");
            			mensagemIda = br.readLine();
            			saida.writeUTF("{"
            					+ "\"method\"::\"write\",,"
            					+ "\"args\"::[\""
            					+ mensagemIda
            					+ "\n\"]"
            					+ "}");
            			mensagemVolta = parseResult(entrada.readUTF());
            			if(mensagemVolta.get("result").equals("false")) {
            				System.out.print("[SERVER](error): Formato não atendido");
            			}else {
            				System.out.print("[SERVER](write):\n" + printHashMap(mensagemVolta));
            			}
            			break;
            		case "\\sair":
            			System.out.println("[CLIENT](sair)");
            			socket.close();
            			return;
            		default:
            			System.out.println("[CLIENT](unknown)");
            			break;
	            }
            }
            
            
        } catch(Exception e) {
        	e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        new Cliente().iniciar();
    }
    
}
