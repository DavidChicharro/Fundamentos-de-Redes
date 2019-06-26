//
// YodafyServidorIterativo
// (CC) jjramos, 2012
//

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;


public class YodafyClienteUDP {

	public static void main(String[] args) {
		
		byte []bufer;
                String host = "localhost";
		
		// Puerto en el que espera el servidor:
		int port=8989;
                InetAddress direccion;
                DatagramPacket paquete=null;
		// Socket para la conexión UDP
		DatagramSocket socket=null;
		
		try {
			socket = new DatagramSocket();
			
			direccion = InetAddress.getByName(host);
			
                        bufer="Al monte del volcán debes ir sin demora".getBytes();
                        
                        paquete = new DatagramPacket(bufer, bufer.length, direccion, port);
                        
                        socket.send(paquete);
                        
                        bufer = new byte[256];
                        
                        paquete = new DatagramPacket(bufer, bufer.length);
                        
                        socket.receive(paquete);

						
		// Excepciones:
		} catch (UnknownHostException e) {
			System.err.println("Error: Nombre de host no encontrado.");
		} catch (IOException e) {
			System.err.println("Error de entrada/salida al abrir el socket.");
		}
                
                System.out.println("Mensaje: "+paquete.getData());
                System.out.println("Direccion: "+paquete.getAddress());
                System.out.println("Puerto :"+paquete.getPort());
                
                // Una vez terminado el servicio, cerramos el socket (automáticamente se cierran
                // el inpuStream  y el outputStream)
                socket.close();
	}
}
