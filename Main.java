import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        while (true){
            System.out.println("Menú:");
            System.out.println("1. Generar archivo de referencias");
            System.out.println("2. Simular sistema de paginación");
            System.out.println("0. Salir");
            int opcion = sc.nextInt();
            sc.nextLine();

            if (opcion == 1) {
                System.out.print("Ingrese el nombre del archivo BMP: ");
                String archivo = sc.nextLine();

                System.out.print("Ingrese el tamaño de página en bytes: ");
                int tamPagina = sc.nextInt();
                sc.nextLine();

                System.out.print("Ingrese el nombre del archivo de salida: ");
                String salida = sc.nextLine();

                GeneradorReferencias.generarReferencias(archivo, tamPagina, salida);
                
            }else if (opcion == 2){
                System.out.print("Ingrese el número de marcos asignados: ");
                int marcos = sc.nextInt();
                sc.nextLine();

                System.out.print("Ingrese el nombre del archivo de referencias: ");
                String archivo = sc.nextLine();

                System.out.print("Ingrese el tamaño de página (TP): ");
                int tp = sc.nextInt();
                sc.nextLine();

                SimuladorMemoria sim = new SimuladorMemoria(marcos, tp);
                sim.simular(archivo);
        
            }else if (opcion == 0){
                break;
            }else{
                System.out.println("Opción no válida");
            }
        }
    }
}
