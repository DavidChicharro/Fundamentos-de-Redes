import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

//
// YodafyServidorIterativo
// (CC) jjramos, 2012
//
public class YodafyServidorUDP {

	public static void main(String[] args) {
		// Puerto de escucha
		int port=8989;
                InetAddress direccion;
                
                DatagramPacket paquete=null;
                DatagramPacket paqueteEnvio=null;
                
		// array de bytes auxiliar para recibir o enviar datos.
		byte []buffer=new byte[256];
                
                //Socket del servidor		
                DatagramSocket socketServidor=null;

		try {
			// Abrimos el socket en modo pasivo, escuchando el en puerto indicado por "port"
			socketServidor = new DatagramSocket(port);
			

			// Mientras ... siempre!
			do {
				paquete = new DatagramPacket(buffer, buffer.length);
				socketServidor.receive(paquete);
				
				// Creamos un objeto de la clase ProcesadorYodafy, pasándole como 
				// argumento el nuevo socket, para que realice el procesamiento
				// Este esquema permite que se puedan usar hebras más fácilmente.
				ProcesadorYodafy procesador = new ProcesadorYodafy(socketServidor, paquete);
                                paqueteEnvio=procesador.procesa();
				socketServidor.send(paqueteEnvio);
				
			} while (true);
			
                }catch (UnknownHostException e) {
                    System.err.println("Error: equipo desconocido");
		} catch (IOException e) {
                    System.err.println("Error, no se pudo antender en el puerto "+port);
		}
                socketServidor.close();
	}

}
