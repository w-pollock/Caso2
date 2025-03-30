public class Referencia {
    public String nombre;
    public int pagina;
    public int offset;
    public char tipoAcceso;

    public Referencia(String nombre, int pagina, int offset, char tipoAcceso){
        this.nombre = nombre;
        this.pagina = pagina;
        this.offset = offset;
        this.tipoAcceso = tipoAcceso;
    }

    @Override
    public String toString(){
        return nombre + "," + pagina + "," + offset + "," + tipoAcceso;
    }
}
