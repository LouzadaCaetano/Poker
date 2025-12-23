import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Jogador {
    private String nome;
    private boolean bot;
    private int moedas;
    private boolean ativo;
    private boolean eliminado;
    private List<Carta> mao = new ArrayList<>();
    private int aposta;

    public Jogador(String nome, int moedas){
        this.nome = nome;
        this.bot = true;
        this.moedas = moedas;
        this.ativo = true;
        this.eliminado = false;
        this.aposta = 0;
    }

    public String getNome(){
        return this.nome;
    }

    public void playerHumano(){
        this.bot = false;
    }

    public boolean getBot(){
        return this.bot;
    }

    public int getMoedas(){
        return this.moedas;
    }

    public boolean getAtivo(){
        return this.ativo;
    }

    public void setAtivo(boolean ativo){
        this.ativo = ativo;
    }

    public boolean getEliminado(){
        return this.eliminado;
    }

    public void setEliminado(boolean eliminado){
        this.eliminado = eliminado;
    }

    public List<Carta> getMao() {
        return Collections.unmodifiableList(mao);
    }

    public void receberCarta(Carta carta) {
        if (mao.size() >= 2) { // Se quiser travar em 2 cartas
            throw new IllegalStateException("Jogador já tem 2 cartas na mão.");
        }
        mao.add(carta);
    }

    public void limparMao() {
        mao.clear();
    }

    public int apostar(int valor) {
        int apostaReal = Math.min(valor, moedas); // trata all-in automaticamente
        moedas -= apostaReal;
        aposta += apostaReal;
        return apostaReal; // retorna quanto realmente foi apostado
    }

    public int getApostaAtual() {
        return aposta;
    }

    public void resetarAposta() {
        aposta = 0;
    }

    public void fold(){
        this.ativo = false;
    }

    public void printJogador(){
        System.out.println("Nome: " + this.nome + "\nMoedas: " + this.moedas +
                "\nCartas: " + this.mao.get(0).toString() + " | " + this.mao.get(1).toString());
    }
}
