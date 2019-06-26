//
// YodafyServidorIterativo
// (CC) jjramos, 2012
//
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Random;



//
// Nota: si esta clase extendiera la clase Thread, y el procesamiento lo hiciera el método "run()",
// ¡Podríamos realizar un procesado concurrente! 
//
public class ProcesadorYodafy {
    
        InetAddress direccion;
	// Referencia a un socket para enviar/recibir las peticiones/respuestas
	private DatagramSocket socketServicio;
	// stream de lectura (por aquí se recibe lo que envía el cliente)
	private DatagramPacket paquete;
	// stream de escritura (por aquí se envía los datos al cliente)
	private DatagramPacket paqueteEnvio;
	
	// Para que la respuesta sea siempre diferente, usamos un generador de números aleatorios.
	private Random random;
        
        byte[] bufer = new byte[256];
	
	// Constructor que tiene como parámetro una referencia al socket abierto en por otra clase
	public ProcesadorYodafy(DatagramSocket socketServicio, DatagramPacket paquete) {
                this.socketServicio=socketServicio;
                this.paquete=paquete;
		random=new Random();
	}
	
	
	// Aquí es donde se realiza el procesamiento realmente:
	DatagramPacket procesa(){
		
                String datosRecibidos;
                
		
		try {
                    direccion = InetAddress.getByName("localhost");
                    
                    // Lee la frase a Yodaficar:
                    datosRecibidos = new String(paquete.getData());
                    
                    // Yoda hace su magia:
                    // Yoda reinterpreta el mensaje:
                    String respuesta=yodaDo(datosRecibidos);
                    System.out.println(respuesta);
                    paqueteEnvio = new DatagramPacket(respuesta.getBytes(), 
                            respuesta.getBytes().length, direccion, paquete.getPort());

			
		} catch (IOException e) {
			System.err.println("Error al obtener los flujso de entrada/salida.");
		}
                
                return paqueteEnvio;
	}

	// Yoda interpreta una frase y la devuelve en su "dialecto":
	private String yodaDo(String peticion) {
		// Desordenamos las palabras:
		String[] s = peticion.split(" ");
		String resultado="";
		
		for(int i=0;i<s.length;i++){
			int j=random.nextInt(s.length);
			int k=random.nextInt(s.length);
			String tmp=s[j];
			
			s[j]=s[k];
			s[k]=tmp;
		}
		
		resultado=s[0];
		for(int i=1;i<s.length;i++){
		  resultado+=" "+s[i];
		}
		
		return resultado;
	}
}
