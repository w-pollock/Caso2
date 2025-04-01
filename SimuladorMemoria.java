import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class SimuladorMemoria {
    private final Map<Integer, PaginaVirtual> tablaPaginas = new HashMap<>();
    private final Queue<Integer> marcosRAM = new LinkedList<>();
    private final int numMarcos;
    private final int tamPagina;

    private int hits = 0;
    private int misses = 0;

    public SimuladorMemoria(int numMarcos, int tamPagina){
        this.numMarcos = numMarcos;
        this.tamPagina = tamPagina;
    }

    public void simular(String archivoReferencias){
        List<Referencia> referencias = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(archivoReferencias))){
            String linea;
            while (!(linea = br.readLine()).startsWith("Imagen[")&& linea != null);

            do{
                String[] partes = linea.split(",");
                String nombre = partes[0];
                int pagina = Integer.parseInt(partes[1]);
                int offset = Integer.parseInt(partes[2]);
                char tipo = partes[3].charAt(0);
                referencias.add(new Referencia(nombre, pagina, offset, tipo));

                synchronized (tablaPaginas){
                    tablaPaginas.putIfAbsent(pagina, new PaginaVirtual(pagina));
                }
            }while ((linea = br.readLine()) != null);
        }catch (IOException e){
            e.printStackTrace();
            return;
        }

        ActualizadorBits actualizador = new ActualizadorBits(tablaPaginas);
        ProcesadorReferencias procesador = new ProcesadorReferencias(referencias, this);
        actualizador.start();
        procesador.start();

        try{
            procesador.join();
            actualizador.interrupt();
            actualizador.join();
        }catch (InterruptedException e){
            e.printStackTrace();
        }

        imprimirResultados(referencias.size());
    }

    public void accederPagina(Referencia ref){
        synchronized (tablaPaginas){
            PaginaVirtual pagina = tablaPaginas.get(ref.pagina);

            if (pagina.enRAM){
                hits++;
            }else{
                misses++;
                manejarFallo(ref.pagina);
            }

            pagina.bitR = true;
            if (ref.tipoAcceso == 'W'){
                pagina.bitM = true;
            }
            pagina.ultAcc = System.nanoTime();
        }
    }

    private void manejarFallo(int paginaFaltante){
        synchronized(marcosRAM){
            if (marcosRAM.size() < numMarcos){
                marcosRAM.add(paginaFaltante);
                synchronized(tablaPaginas){
                    tablaPaginas.get(paginaFaltante).enRAM = true;
                }
            }else{
                List<Integer>[] clases = new List[4];
                for (int i = 0; i < 4; i++) clases[i] = new ArrayList<>();
                synchronized (tablaPaginas){
                    for (int p: marcosRAM){
                        PaginaVirtual pv = tablaPaginas.get(p);
                        int clase = (pv.bitR ? 2:0) + (pv.bitM ? 1:0);
                        clases[clase].add(p);
                    }
                }
                int paginaAReemp = -1;
                for (List<Integer> clase: clases){
                    if (!clase.isEmpty()){
                        paginaAReemp = clase.get(0);
                        break;
                    }
                }

                if (paginaAReemp != -1){
                    synchronized (tablaPaginas){
                        tablaPaginas.get(paginaAReemp).enRAM = false;
                        tablaPaginas.get(paginaFaltante).enRAM = true;
                    }
                    marcosRAM.remove(paginaAReemp);
                    marcosRAM.add(paginaFaltante);
                }
            }
        }
    }

    private void imprimirResultados(int totalReferencias){
        System.out.println("Total referencias: " + totalReferencias);
        System.out.println("Hits: " + hits);
        System.out.println("Fallas de página: " + misses);
        System.out.printf("Porcentaje de hits: %.2f %%\n", (100.0 * hits) / totalReferencias);
        
    }

    static class ActualizadorBits extends Thread{
        private final Map<Integer, PaginaVirtual> tablaPaginas;

        public ActualizadorBits(Map<Integer, PaginaVirtual> tablaPaginas){
            this.tablaPaginas = tablaPaginas;
        }

        public void run(){
            while (!isInterrupted()){
                try{
                    Thread.sleep(1);
                    synchronized (tablaPaginas){
                        for (PaginaVirtual p: tablaPaginas.values()){
                            p.bitR = false;
                        }
                    }
                }catch (InterruptedException e){
                    break;
                }
            }
        }
    }
    static class ProcesadorReferencias extends Thread{
        private final List<Referencia> referencias;
        private final SimuladorMemoria simulador;

        public ProcesadorReferencias(List<Referencia> referencias, SimuladorMemoria simulador){
            this.referencias = referencias;
            this.simulador = simulador;
        }

        public void run(){
            try{
                int contador = 0;
                for (Referencia ref: referencias){
                    simulador.accederPagina(ref);
                    contador++;
                    if (contador % 10000 == 0){
                        Thread.sleep(1);
                    }
                }
            }catch (InterruptedException e){
                System.out.println("Procesador interrumpido");
            }
        }
    }
}
