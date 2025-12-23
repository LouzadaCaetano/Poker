import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Menu {

    private Scanner sc = new Scanner(System.in);

    public void start(){
        System.out.println("Digite o nome do jogador: ");
        String nome = this.sc.next();

        Jogador principal = new Jogador(nome, 1000);
        principal.playerHumano();

        int quantBots;
        do {
            System.out.print("Quantidade de bots na partida (1 a 5): ");
            quantBots = this.sc.nextInt();

            if (quantBots < 1 || quantBots > 5) {
                System.out.println("Valor inválido! O número de bots deve estar entre 0 e 5.");
            }
        } while (quantBots < 1 || quantBots > 5);

        List<Jogador> jogadores = new ArrayList<>();

        for (int i = 0; i < quantBots; i++) {
            jogadores.add(new Jogador("Bot " + (i + 1), 1000));
        }
        jogadores.add(principal);

        Mesa mesa = new Mesa(jogadores);
        mesa.iniciarJogo();
    }
}
