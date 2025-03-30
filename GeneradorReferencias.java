import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class GeneradorReferencias {
    public static void generarReferencias(String archivoImagen, int tamPagina, String archivoSalida){
        Imagen imgIn = new Imagen(archivoImagen);
        int alto = imgIn.alto;
        int ancho = imgIn.ancho;


        List<Referencia> referencias = new ArrayList<>();

        int bytesImagen = alto * ancho * 3;
        int bytesFiltro = 3 * 3 * 4;
        int bytesRespuesta = alto * ancho * 3;
        int offsetImagen = 0;
        int offsetFiltroX = offsetImagen + bytesImagen;
        int offsetFiltroY = offsetFiltroX + bytesFiltro;
        int offsetRespuesta = offsetFiltroY + bytesFiltro;

        for (int i = 1; i<alto -1;i++){
            for (int j = 1;j<ancho-1;j++){
                for (int ki = -1; ki <=1; ki++){
                    for (int kj = -1; kj <=1;kj++){
                        int fila = i + ki;
                        int col = j + kj;

                        for (int c=0; c<3; c++){
                            int pos = (fila*ancho + col) * 3 + c;
                            int dir = offsetImagen + pos;
                            referencias.add(crearReferencia("Imagen[" + fila + "][" + col + "]." + canal(c), dir, tamPagina, 'R'));
                        }
                        int idx = (ki+1) * 3 + (kj +1);
                        int dirX = offsetFiltroX + idx*4;
                        int dirY = offsetFiltroY +idx*4;

                        referencias.add(crearReferencia("SOBEL_X[" + (ki+1) + "][" + (kj+1) + "]", dirX, tamPagina, 'R'));
                        referencias.add(crearReferencia("SOBEL_Y[" + (ki+1) + "][" + (kj+1) + "]", dirY, tamPagina, 'R'));

                    }
                }
                int pixelIndex = (i*ancho +j)*3;
                for (int c = 0; c < 3; c++){
                    int dir = offsetRespuesta + pixelIndex + c;
                    referencias.add(crearReferencia("Rta[" + i + "][" + j + "]." + canal(c), dir, tamPagina, 'W'));
                }
            }
        }
        int totalReferencias = referencias.size();
        int totalPaginas = (offsetRespuesta + bytesRespuesta + tamPagina -1)/tamPagina;
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(archivoSalida))){
            writer.println("TP=" + tamPagina);
            writer.println("NF=" + alto);
            writer.println("NC=" +ancho);
            writer.println("NR=" + totalReferencias);
            writer.println("NP=" + totalPaginas);
            for (Referencia r: referencias){
                writer.println(r.toString());
            }
            System.out.println("Archivo generado: " + archivoSalida);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private static Referencia crearReferencia(String nombre, int direccion, int tamPagina, char tipo){
        int pagina = direccion / tamPagina;
        int offset = direccion % tamPagina;
        return new Referencia(nombre, pagina, offset, tipo);
    }

    private static String canal(int c){
        if (c == 0) return "r";
        else if (c == 1) return "g";
        else if (c == 2) return "b";
        else return "?";
    }
}
