//
// ServidorConcurrenteConsulta
// (CC) David Carrasco Chicharo, 2018
//
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.regex.Pattern;


public class ClienteConsultaTCP {

	public static void main(String[] args) throws Exception {
		
		String buferEnvio;
		String buferRecepcion;
                String login;
                String mensajeAutenticacion;
                int numOpcion;
                String opcionElegida;
                String fechasDisponibles;
                String fechaElegida;
                String horasDisponibles;
                String horaElegida;
		
		// Nombre del host donde se ejecuta el servidor:
		String host="localhost";
		// Puerto en el que espera el servidor:
		int port=8989;
		
		// Socket para la conexión TCP
		Socket socketServicio=null;
		
		try {
                    // Creamos un socket que se conecte a "host" y "port":
                    socketServicio = new Socket(host,port);

                    // Obtiene los flujos de escritura/lectura
                    PrintWriter outPrinter = new PrintWriter(socketServicio.getOutputStream(),true);
                    BufferedReader inReader = 
                            new BufferedReader(new InputStreamReader(socketServicio.getInputStream()));

                    System.out.print("Bienvenido/a al Servicio de Citas de su Centro de Salud."
                            + "\nIntroduzca el identificador de su tarjeta sanitaria y su fecha de nacimiento: ");

                    // El usuario introduce sus datos para autenticarse en el sistema
                    Scanner sc = new Scanner(System.in);
                    login = sc.nextLine();
                    //login = "AN0315777723:02/06/1997";
                    buferEnvio="COD001"+login;
                    outPrinter.println(buferEnvio);
                    outPrinter.flush();

                    buferRecepcion = inReader.readLine();

                    if(buferRecepcion.startsWith("COD201")){
                        mensajeAutenticacion = buferRecepcion.replaceFirst("COD(...)", "");
                        System.out.println(mensajeAutenticacion);

                        do{
                            // Elige una opción para realizar
                            System.out.print("Elija una opción (indique el número): \n\t1: Pedir cita para medicina de familia."
                                    + "\n\t2: Pedir cita para enfermería. \n\t3: Cambiar centro de salud. \n\t4: Cerrar sesión."
                                    + "\nOpción: ");
                            Scanner sc1 = new Scanner(System.in);
                            numOpcion = Integer.parseInt(sc1.nextLine());
                            // Menú de opciones
                            switch(numOpcion){
                                case 1: opcionElegida="COD101Cita médico de cabecera"; break;
                                case 2: opcionElegida="COD102Cita enfermería"; break;
                                case 3: opcionElegida="COD103Cambiar centro de salud"; break;
                                case 4: opcionElegida="COD104Cerrar sesión"; break;
                                default: opcionElegida="COD120Error"; break;
                            }
                            buferEnvio=opcionElegida;
                            outPrinter.println(buferEnvio);
                            outPrinter.flush();

                            buferRecepcion = inReader.readLine();
                            if(buferRecepcion.startsWith("COD202")){                        
                                fechasDisponibles = buferRecepcion.replaceFirst("COD(...)", "");
                                // Muestra las fechas disponibles
                                System.out.println("\nFechas disponibles:");
                                StringTokenizer fd = new StringTokenizer(fechasDisponibles,"#");
                                while(fd.hasMoreTokens()){
                                    String fecha = fd.nextToken();
                                    System.out.println(fecha);
                                }
                                System.out.flush();

                                // Elegir fecha
                                System.out.println("\nElija una fecha entre las disponibles: ");
                                Scanner sc2 = new Scanner(System.in);
                                fechaElegida = sc2.nextLine();

                                String regexp = "\\d{2}/\\d{2}/\\d{4}";
                                if(!Pattern.matches(regexp, fechaElegida))      // Comprueba que el formato de fecha es correcto
                                    buferEnvio="COD121Fecha inválida";
                                else
                                    buferEnvio="COD111"+fechaElegida;
                                outPrinter.println(buferEnvio);
                                outPrinter.flush();

                                buferRecepcion = inReader.readLine();
                                if(buferRecepcion.startsWith("COD203")){
                                    horasDisponibles = buferRecepcion.replaceFirst("COD(...)", "");
                                    // Muestra las horas diponibles
                                    System.out.println("\nHoras disponibles:");
                                    StringTokenizer hd = new StringTokenizer(horasDisponibles,"#");
                                    while(hd.hasMoreTokens()){
                                        String hora = hd.nextToken();
                                        System.out.println(hora);
                                    }
                                    System.out.flush();

                                    // Elegir hora
                                    System.out.println("\nElija una hora entre las disponibles: ");
                                    Scanner sc3 = new Scanner(System.in);
                                    horaElegida = sc3.nextLine();

                                    String regexp2 = "\\d{1,2}:\\d{2}";
                                    if(!Pattern.matches(regexp2, horaElegida))  // Comprueba que el formato de hora es correcto
                                        buferEnvio="COD122Hora inválida";
                                    else
                                        buferEnvio="COD112"+horaElegida;
                                    outPrinter.println(buferEnvio);
                                    outPrinter.flush();

                                    buferRecepcion = inReader.readLine();
                                    if(buferRecepcion.startsWith("COD210")){
                                        System.out.println(buferRecepcion.replaceFirst("COD(...)", ""));    //COD210: Cita asignada
                                        System.out.println("Día: " + fechaElegida + "\tHora: " + horaElegida + "\n\n");
                                    }else if(buferRecepcion.startsWith("COD213")){
                                        System.out.println(buferRecepcion.replaceFirst("COD(...)", ""));    //COD213: Hora inválida
                                    }
                                }
                                else{
                                    System.out.println(buferRecepcion.replaceFirst("COD(...)", ""));        //COD212: Fecha inválida
                                }
                            }else if(buferRecepcion.startsWith("COD204")){
                                String centroSaludElegido = "";
                                ArrayList<String> centrosSalud = new ArrayList<>();
                                centrosSalud.add("la Chana");
                                centrosSalud.add("Salvador Caballero");
                                centrosSalud.add("Gran Capitán");
                                centrosSalud.add("Realejo");
                                centrosSalud.add("Mirasierarra");
                                centrosSalud.add("Zaidín Sur");

                                System.out.println(buferRecepcion.replaceFirst("COD(...)", ""));

                                for(int i=0 ; i<centrosSalud.size() ; i++)
                                    System.out.println("\t" + i + ": " + centrosSalud.get(i));
                                System.out.print("Opción: ");

                                // Elegir centro de salud
                                Scanner op = new Scanner(System.in);                                
                                numOpcion = Integer.parseInt(op.nextLine());
                                if(numOpcion>=0 && numOpcion<centrosSalud.size())
                                    centroSaludElegido = centrosSalud.get(numOpcion);

                                if(centroSaludElegido.equals(""))
                                    buferEnvio = "COD123Error en la elección del centro de salud.";
                                else
                                    buferEnvio ="COD113"+centroSaludElegido;
                                outPrinter.println(buferEnvio);
                                outPrinter.flush();

                                System.out.println(inReader.readLine().replaceFirst("COD(...)", ""));   //COD[205-225-214]
                            }
                            else if(buferRecepcion.startsWith("COD206")){
                                System.out.println(buferRecepcion.replaceFirst("COD(...)", ""));        //COD206: Sesión cerrada
                            }
                            else if(buferRecepcion.startsWith("COD206")){
                                System.out.println(buferRecepcion.replaceFirst("COD(...)", ""));        //COD300: Consulta desconocida
                            }
                            else{
                                System.out.println("Error desconocido.");
                                numOpcion=4;
                            }

                        }while(numOpcion!=4);
                        
                        // Una vez terminado el servicio, cerramos el socket (automáticamente se cierran
                        // el inReader  y el outPrinter)
                        socketServicio.close();
                    }
                    else if(buferRecepcion.startsWith("COD221")){
                        mensajeAutenticacion = buferRecepcion.replaceFirst("COD(...)", "");
                        System.out.println(mensajeAutenticacion);
                    }
                    else if(buferRecepcion.startsWith("COD000")){
                        mensajeAutenticacion = buferRecepcion.replaceFirst("COD(...)", "");
                        System.out.println(mensajeAutenticacion);
                    }
                    else{
                        System.out.println("Error desconocido.");
                    }
                    // Excepciones:
		} catch (UnknownHostException e) {
			System.err.println("Error: Nombre de host no encontrado.");
		} catch (IOException e) {
			System.err.println("Error de entrada/salida al abrir el socket.");
		}
	}
}
