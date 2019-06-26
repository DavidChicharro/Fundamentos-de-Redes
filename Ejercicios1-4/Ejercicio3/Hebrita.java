import java.net.Socket;
/**
 *
 * @author david
 */
public class Hebrita extends Thread{
    // Creamos un objeto de la clase ProcesadorYodafy, pasándole como 
    // argumento el nuevo socket, para que realice el procesamiento
    // Este esquema permite que se puedan usar hebras más fácilmente.
    ProcesadorYodafy procesador;


    Hebrita(Socket socketConexion){
        procesador=new ProcesadorYodafy(socketConexion);
    }
    // El contenido de este método se ejecutará tras llamar al
    // método "start()". Se trata del procesamiento de la hebra.
    public void run() {
        procesador.procesa();
    }
}
