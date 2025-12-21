public class Carta {
    private final Nums numero;
    private final Naipes naipe;

    public Carta(Nums numero, Naipes naipe){
        this.numero = numero;
        this.naipe = naipe;
    }

    public Nums getNum(){
        return this.numero;
    }

    public Naipes getNaipe(){
        return this.naipe;
    }

    public void printCarta(){
        System.out.println(numero.getLabel() + naipe.getSymbol());
    }
}
