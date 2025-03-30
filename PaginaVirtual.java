public class PaginaVirtual {
    public int numero;
    public boolean enRAM;
    public boolean bitR;
    public boolean bitM;
    public long ultAcc;

    public PaginaVirtual(int numero){
        this.numero = numero;
        this.enRAM = false;
        this.bitR = false;
        this.bitM = false;
        this.ultAcc = System.nanoTime();
    }
}
