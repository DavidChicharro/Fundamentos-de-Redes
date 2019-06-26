//
// ServidorConcurrenteConsultaa
// (CC) David Carrasco Chicharo, 2018
//
import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.StringTokenizer;


public class ProcesadorConsulta {
	// Referencia a un socket para enviar/recibir las peticiones/respuestas
	private Socket socketServicio;
        // stream de lectura (por aquí se recibe lo que envía el cliente)
	private InputStream inputStream;
	// stream de escritura (por aquí se envía los datos al cliente)
	private OutputStream outputStream;
        
        // Para que la respuesta sea siempre diferente, usamos un generador de números aleatorios.
	private Random random;
        
        // Constructor que tiene como parámetro una referencia al socket abierto en por otra clase
	public ProcesadorConsulta(Socket socketServicio) {
		this.socketServicio=socketServicio;
		random=new Random();
	}
		
	// Aquí es donde se realiza el procesamiento realmente:
	void procesa(){
                boolean pacienteAutenticado;
		String datosAutenticacion;
                String mensajeAutenticacion;
                String opcionElegida;                
                String tipoConsulta="";
                String fechasDisponibles;
                String fechaElegida;
                String horasDisponibles;
                String horaElegida;
		
		try {
                    // Obtiene los flujos de escritura/lectura
                    PrintWriter outPrinter = new PrintWriter(socketServicio.getOutputStream(),true);
                    BufferedReader inReader = 
                            new BufferedReader(new InputStreamReader(socketServicio.getInputStream()));


                    // Lee los datos de autenticación
                    datosAutenticacion = inReader.readLine();
                    if(datosAutenticacion.startsWith("COD001")){
                        datosAutenticacion=datosAutenticacion.replaceFirst("COD(...)", "");
                        System.out.println("DATOS: "+datosAutenticacion);
                        pacienteAutenticado = autenticarPaciente(datosAutenticacion);
                        
                        if(pacienteAutenticado){
                            mensajeAutenticacion = "Paciente autenticado correctamente.";
                            System.out.println(mensajeAutenticacion);
                            outPrinter.println("COD201"+mensajeAutenticacion);
                            
                            do{
                                opcionElegida = inReader.readLine();

                                // Procesa la opción elegida
                                if(opcionElegida.startsWith("COD101")){
                                    tipoConsulta = "medicina familia";
                                }else if(opcionElegida.startsWith("COD102")){
                                    tipoConsulta = "enfermeria";
                                }else if(opcionElegida.startsWith("COD103")){
                                    tipoConsulta = "cambiar centro salud";
                                }else if(opcionElegida.startsWith("COD104")){
                                    tipoConsulta = "cerrar sesion";
                                }else if(opcionElegida.startsWith("COD120")){
                                    System.out.println("Error en la opción elegida.");
                                }else{
                                    System.out.println("Error desconocido.");
                                }

                                // Lee el tipo de consulta
                                if(tipoConsulta.equals("medicina familia") || tipoConsulta.equals("enfermeria")){
                                    System.out.println("Elegido: " + tipoConsulta);

                                    fechasDisponibles = consultarFechasDisponibles(tipoConsulta);

                                    if (fechasDisponibles.equals(""))
                                        outPrinter.println("COD222No hay fechas disponibles");
                                    else{
                                        outPrinter.println("COD202"+fechasDisponibles);
                                        outPrinter.flush();

                                        fechaElegida = inReader.readLine();
                                        if(fechaElegida.startsWith("COD111")){
                                            fechaElegida=fechaElegida.replaceFirst("COD(...)", "");
                                            System.out.println("Elegida la fecha: " + fechaElegida);

                                            horasDisponibles = consultarHorasDisponibles(tipoConsulta, fechaElegida);
                                            if(horasDisponibles.equals(""))
                                                outPrinter.println("COD223No hay citas disponibles.");
                                            else{
                                                outPrinter.println("COD203"+horasDisponibles);
                                                outPrinter.flush();

                                                horaElegida = inReader.readLine();
                                                if(horaElegida.startsWith("COD112")){
                                                    horaElegida=horaElegida.replaceFirst("COD(...)", "");
                                                    System.out.println("Elegida la hora: " + horaElegida);
                                                    //eliminar hora -> Si era la última hora del día -> eliminar fecha
                                                    boolean existeHora = eliminarFechaHora("hora", tipoConsulta, fechaElegida+" "+horaElegida);
                                                    if(existeHora){
                                                        boolean quedanHoras = buscarFecha(tipoConsulta, fechaElegida);
                                                        if(!quedanHoras)
                                                            eliminarFechaHora("fecha", tipoConsulta, fechaElegida);

                                                        outPrinter.println("COD210Cita asignada.");
                                                        outPrinter.flush();
                                                    }
                                                    else{
                                                        outPrinter.println("COD213Hora inválida");
                                                        outPrinter.flush();
                                                    }
                                                }
                                                else if(horaElegida.startsWith("COD122")){
                                                    System.out.println(horaElegida.replaceFirst("COD(...)", ""));
                                                    outPrinter.println("COD213Hora inválida");
                                                    outPrinter.flush();
                                                }
                                            }
                                        }
                                        else if(fechaElegida.startsWith("COD121")){
                                            System.out.println(fechaElegida.replaceFirst("COD(...)", ""));
                                            outPrinter.println("COD212Fecha inválida");
                                            outPrinter.flush();
                                        }
                                    }
                                }else if(tipoConsulta.equals("cambiar centro salud")){
                                    String nuevoCentro;
                                    System.out.println("Elegido: " + tipoConsulta);

                                    outPrinter.println("COD204Elija su nuevo centro de salud: ");
                                    outPrinter.flush();

                                    nuevoCentro = inReader.readLine();
                                    if(nuevoCentro.startsWith("COD113")){
                                        nuevoCentro = nuevoCentro.replaceFirst("COD(...)", "");
                                        boolean centroCambiado = cambiarCentroSalud(datosAutenticacion, nuevoCentro);

                                        if(centroCambiado)
                                            outPrinter.println("COD205Centro de salud modificado.");
                                        else
                                            outPrinter.println("COD225Error en el cambio de centro de salud.");
                                        outPrinter.flush();
                                    }
                                    else if(nuevoCentro.startsWith("COD123")){
                                        System.out.println(nuevoCentro.replaceFirst("COD(...)", ""));
                                        outPrinter.println("COD214Centro de salud inválido.");
                                        outPrinter.flush();
                                    }
                                }else if(tipoConsulta.equals("cerrar sesion")){
                                    System.out.println("Elegido: " + tipoConsulta);
                                    outPrinter.println("COD206Sesión cerrada.");
                                    outPrinter.flush();
                                }else{
                                    String error = "Consulta desconocida.";
                                    System.out.println(error);
                                    outPrinter.println("COD300"+error);
                                    outPrinter.flush();
                                }
                            }while(!opcionElegida.startsWith("COD104"));
                        }else{
                            outPrinter.println("COD221Error en la autenticación.");
                            outPrinter.flush();
                        }                        
                    }
                    else{
                        outPrinter.println("COD000Error de autenticación.");
                        outPrinter.flush();
                    }
		} catch (IOException e) {
			System.err.println("Error al obtener los flujos de entrada/salida.");
		}
	}
        
        private String consultarFechasDisponibles(String tipoConsulta) throws FileNotFoundException {
            String nombreFichero;
            Scanner entrada = null;
            String linea="";
            String fechasDisponibles="";
            
            if(tipoConsulta.equals("medicina familia"))
                nombreFichero = "fechas_disponibles_mf.txt";
            else
                nombreFichero = "fechas_disponibles_enf.txt";
            
            FileReader archFechas = new FileReader(nombreFichero);
            entrada = new Scanner(archFechas);
            
            while(entrada.hasNext()){
                linea = entrada.nextLine();
                fechasDisponibles+=linea;                
            }
            return fechasDisponibles;
        }
        
        private String consultarHorasDisponibles(String tipoConsulta, String fecha) throws FileNotFoundException {
            String nombreFichero;
            Scanner entrada = null;
            String linea="";
            String horasDisponibles="";
            
            if(tipoConsulta.equals("medicina familia"))
                nombreFichero = "horas_disponibles_mf.txt";
            else
                nombreFichero = "horas_disponibles_enf.txt";
            
            FileReader archFechas = new FileReader(nombreFichero);
            entrada = new Scanner(archFechas);
            
            while(entrada.hasNext()){
                linea = entrada.nextLine();
                if (linea.contains(fecha)) {
                    horasDisponibles+=linea;
                }                
            }
            
            return horasDisponibles;
        }

        // Elimina una fecha o una hora del documento correspondiente cuando se solicita una cita
        private boolean eliminarFechaHora(String tipo, String tipoConsulta, String fechaHora) throws IOException {
            boolean eliminado = false;
            List<String> lineas = new ArrayList<>();
            String nombreFichero;
            String linea;
            
            //tipo: fecha u hora
            if(tipo.equals("fecha"))
                nombreFichero = "fechas_disponibles";
            else
                nombreFichero = "horas_disponibles";
            
            if(tipoConsulta.equals("medicina familia"))
                nombreFichero += "_mf.txt";
            else
                nombreFichero += "_enf.txt";
            
            FileReader fr = new FileReader(nombreFichero);
            BufferedReader br = new BufferedReader(fr);
                        
            while ((linea = br.readLine()) != null) {
                if (linea.equals(fechaHora+"#")){
                    System.out.println("Borrar: " + fechaHora);
                    linea = linea.replace(fechaHora, "#");
                    eliminado=true;
                }
                lineas.add(linea);
            }
            fr.close();
            br.close();

            FileWriter fw = new FileWriter(nombreFichero);
            BufferedWriter out = new BufferedWriter(fw);
            for(String s : lineas)
                out.write(s+"\n");
            out.flush();
            out.close();
            
            return eliminado;
        }
 
        // Buscar la fecha en el archivo de las horas
        // Si no existe la fecha, borrar la fecha del archivo de fechas
        private boolean buscarFecha(String tipoConsulta, String fecha) throws FileNotFoundException {
            boolean existeFecha = false;
            String nombreFichero;
            Scanner entrada = null;
            String linea="";
            
            if(tipoConsulta.equals("medicina familia"))
                nombreFichero = "horas_disponibles_mf.txt";
            else
                nombreFichero = "horas_disponibles_enf.txt";
            
            FileReader archFechas = new FileReader(nombreFichero);
            entrada = new Scanner(archFechas);
            
            while(entrada.hasNext() && !existeFecha){
                linea = entrada.nextLine();  //se lee una línea
                if (linea.contains(fecha)) {
                    existeFecha = true;
                }                
            }
            
            return existeFecha;
        }
        
        //Cambia el centro de salud de un usuario
        //Si el nuevo centro de salud es el que ya tenía asignado,
        //no se efectúa el cambio
        private boolean cambiarCentroSalud(String datosAutenticacion, String nuevoCentro) throws FileNotFoundException{
            List<String> lineas = new ArrayList<>();
            boolean centroCambiado = false;
            Scanner entrada = null;
            String texto = datosAutenticacion; 
            String antiguoCentro="";
            String linea="";
            String nombreFichero = "datos_pacientes.txt";         
            
            try{
                FileReader fr = new FileReader(nombreFichero);
                BufferedReader br = new BufferedReader(fr);
                while ((linea = br.readLine()) != null) {
                    if (linea.contains(texto)){
                        System.out.println("Linea previa: "+linea);
                        StringTokenizer st = new StringTokenizer(linea,":");
                        while(st.hasMoreTokens()){
                            String a = st.nextToken();
                            if(a.contains("Centro de Salud"))
                                antiguoCentro = a;
                        }
                        if(!antiguoCentro.contains(nuevoCentro)){
                           linea = linea.replace(antiguoCentro,"Centro de Salud "+nuevoCentro);
                           centroCambiado = true;
                        }
                        System.out.println("Linea post: "+linea);
                    }
                    lineas.add(linea);
                }
                fr.close();
                br.close();

                FileWriter fw = new FileWriter(nombreFichero);
                BufferedWriter out = new BufferedWriter(fw);
                for(String s : lineas)
                    out.write(s+"\n");
                out.flush();
                out.close();
                
            } catch (NullPointerException e) {
                System.out.println(e.toString() + "No ha seleccionado ningún archivo");
            } catch (Exception e) {
                System.out.println(e.toString());
            } finally {
                if (entrada != null) {
                    entrada.close();
                }
            }
            
            return centroCambiado;
        }
        
        // Comprobar que el paciente existen en el sistema
        private boolean autenticarPaciente(String datosAutenticacion) throws FileNotFoundException {
            boolean autenticado = false;
            Scanner entrada = null; // declarar en el momento de uso
            String linea="";                     

            //Archivo que contiene los datos de los pacientes
            FileReader j = new FileReader("datos_pacientes.txt");

            //Introducimos el texto a buscar
            String texto = datosAutenticacion;

            try {
                //Scanner para leer el fichero
                entrada = new Scanner(j);
                
                //mostramos el texto a buscar
                System.out.println("Texto a buscar: " + texto);
                for(int numeroLinea=1 ; entrada.hasNext() && !autenticado ; numeroLinea++){
                    linea = entrada.nextLine();  //se lee una línea
                    if (linea.contains(texto)) {   //si la línea contiene el texto buscado se muestra por pantalla
                        System.out.println("Linea " + numeroLinea + ": " + linea);
                        autenticado = true;
                    }
                }
                if(!autenticado){ //si el archivo no contienen el texto se muestra un mensaje indicándolo
                    System.out.println(texto + " no se ha encontrado en el archivo.");
                    throw new Exception("El paciente no existe.");
                }
            } catch (NullPointerException e) {
                System.out.println(e.toString() + "No ha seleccionado ningún archivo");
            } catch (Exception e) {
                System.out.println(e.toString());
            } finally {
                if (entrada != null) {
                    entrada.close();
                }
            }
            return autenticado;
        }
}
