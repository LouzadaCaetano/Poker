import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Baralho {
    private final List<Carta> baralho = new ArrayList<>();

    public Baralho(){
        for (Nums nums : Nums.values()){
            for (Naipes naipes : Naipes.values()){
                baralho.add(new Carta(nums, naipes));
            }
        }
    }

    public void shuffle(){
        Collections.shuffle(baralho);
    }

    public Carta comprarCarta() {
        if (baralho.isEmpty()) {
            throw new IllegalStateException("O baralho acabou!");
        }
        return baralho.removeFirst();  // remove a primeira carta do baralho
    }

    public List<Carta> getBaralho(){
        return this.baralho;
    }
}
