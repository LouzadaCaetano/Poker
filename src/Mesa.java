import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Mesa {
    private Baralho baralho;
    private final List<Jogador> jogadores;
    private final List<Carta> cartasMesa;

    private final int smallBlind = 10;
    private final int bigBlind = this.smallBlind * 2;
    private int winningPot;
    private int dealerIndex;

    private Scanner sc = new Scanner(System.in);

    public Mesa(List<Jogador> jogadores){
        this.jogadores = new ArrayList<>(jogadores);
        this.cartasMesa = new ArrayList<>();
        this.dealerIndex = 0;
    }

    public void iniciarJogo(){
        while(jogadoresAtivos() > 1){
            iniciarRodada();
            dealerIndex = (dealerIndex + 1) % jogadores.size();
        }
    }

    public void iniciarRodada() {
        baralho = new Baralho();
        baralho.shuffle();
        cartasMesa.clear();
        this.winningPot = 0;

        jogadores.forEach(Jogador::limparMao);

        cobraBlinds();
        distribuirCartasJogadores();
        distribuirCartasMesa();
    }

    private void distribuirCartasMesa() {
        // Flop (3 cartas)
        for (int i = 0; i < 3; i++) {
            cartasMesa.add(baralho.comprarCarta());
        }
        // Turn
        cartasMesa.add(baralho.comprarCarta());
        // River
        cartasMesa.add(baralho.comprarCarta());
    }

    private void distribuirCartasJogadores() {
        for (int i = 0; i < 2; i++) { // duas cartas para cada jogador
            for (Jogador jogador : jogadores) {
                Carta card = baralho.comprarCarta();
                jogador.receberCarta(card);
                card.printCarta();
            }
        }
    }

    public void turnoJogador(Jogador jogador, int maiorAposta) {
        System.out.println("\nVez de: " + jogador.getNome());
        System.out.println("Moedas: " + jogador.getMoedas());
        System.out.println("Aposta atual: " + jogador.getApostaAtual());
        System.out.println("Maior aposta da rodada: " + maiorAposta);

        System.out.println("Escolha uma ação:");
        System.out.println("1 - Call");
        System.out.println("2 - Raise");
        System.out.println("3 - Fold");
        System.out.println("4 - All In");

        int opcao = sc.nextInt();

        switch (opcao) {
            case 1 -> call(jogador, maiorAposta);
            case 2 -> raise(jogador, maiorAposta);
            case 3 -> jogador.fold();
            case 4 -> allIn(jogador);
            default -> {
                System.out.println("Opção inválida, tente novamente.");
                turnoJogador(jogador, maiorAposta);
            }
        }
    }

    public void turnoBot(Jogador bot, int maiorAposta){
        if (!bot.getAtivo()){
            return;
        }

        Random rnd = new Random();
        int act = rnd.nextInt(4);

        System.out.println(bot.getNome() + " está pensando...");

        switch (act){
            case 0: //FOLD
                System.out.println(bot.getNome() + " FOLD");
                bot.setAtivo(false);
                break;
            case 1: //CALL
                System.out.println(bot.getNome() + " CALL");
                call(bot, maiorAposta);
                break;
            case 2: //RAISE
                int[] percentuais = {10, 20, 30, 40, 50, 60, 70, 80, 90};
                int p = percentuais[rnd.nextInt(percentuais.length)];
                int valor = bot.getMoedas() * p / 100;
                raiseBot(bot, maiorAposta, valor);
                break;
            case 3: //ALL IN
                System.out.println(bot.getNome() + " ALL IN");
                allIn(bot);
                break;
        }
    }

    private void call(Jogador jogador, int maiorAposta) {
        int valorParaCall = maiorAposta - jogador.getApostaAtual();

        if (valorParaCall >= jogador.getMoedas()) {
            int allIn = jogador.getMoedas();
            jogador.apostar(allIn);
            winningPot += allIn;

            System.out.println(jogador.getNome() + " foi ALL-IN com " + allIn);
            return;
        }

        jogador.apostar(valorParaCall);
        winningPot += valorParaCall;
    }

    private void raise(Jogador jogador, int maiorAposta) {
        int valorRaise;

        int valorParaCall = maiorAposta - jogador.getApostaAtual();

        // Se o jogador nem consegue dar call → all-in automático
        if (valorParaCall >= jogador.getMoedas()) {
            int allIn = jogador.getMoedas();
            jogador.apostar(allIn);
            winningPot += allIn;

            System.out.println(jogador.getNome() + " foi ALL-IN com " + allIn);
            return;
        }

        do {
            System.out.print("Digite o valor do raise (0 = call): ");
            valorRaise = sc.nextInt();

            if (valorRaise < 0) {
                System.out.println("O raise não pode ser negativo.");
            }

            if (valorRaise > jogador.getMoedas() - valorParaCall) {
                System.out.println("Você não tem fichas suficientes para esse raise.");
            }

        } while (valorRaise < 0 || valorRaise > jogador.getMoedas() - valorParaCall);

        int total = valorParaCall + valorRaise;

        jogador.apostar(total);
        winningPot += total;

        if (valorRaise == 0) {
            System.out.println(jogador.getNome() + " deu CALL (" + total + ")");
        } else {
            System.out.println(jogador.getNome() + " deu RAISE de " + valorRaise +
                    " (total apostado: " + total + ")");
        }
    }

    private void raiseBot(Jogador bot, int maiorAposta, int valorRaise){

        int valorParaCall = maiorAposta - bot.getApostaAtual();

        // Se o jogador nem consegue dar call → all-in automático
        if (valorParaCall >= bot.getMoedas()) {
            int allIn = bot.getMoedas();
            bot.apostar(allIn);
            winningPot += allIn;

            System.out.println(bot.getNome() + " foi ALL-IN com " + allIn);
            return;
        }

        int total = valorParaCall + valorRaise;

        bot.apostar(total);
        winningPot += total;

        if (valorRaise == 0) {
            System.out.println(bot.getNome() + " deu CALL (" + total + ")");
        } else {
            System.out.println(bot.getNome() + " deu RAISE de " + valorRaise +
                    " (total apostado: " + total + ")");
        }
    }

    public void allIn(Jogador jogador){
        int valor = jogador.getMoedas();

        if (valor <= 0) {
            return;
        }

        jogador.apostar(valor);
        winningPot += valor;

        System.out.println(jogador.getNome() + " foi ALL-IN com " + valor);
    }

    public void cobraBlinds(){
        if (jogadores.size() < 2) {
            throw new IllegalStateException("Jogadores insuficientes para cobrar blinds.");
        }

        int sbIndex;
        int bbIndex;

        // Caso especial: heads-up (2 jogadores)
        if (jogadores.size() == 2) {
            sbIndex = dealerIndex;
            bbIndex = (dealerIndex + 1) % jogadores.size();
        } else {
            sbIndex = (dealerIndex + 1) % jogadores.size();
            bbIndex = (dealerIndex + 2) % jogadores.size();
        }

        Jogador sb = jogadores.get(sbIndex);
        Jogador bb = jogadores.get(bbIndex);

        sb.apostar(smallBlind);
        bb.apostar(bigBlind);

        this.winningPot += smallBlind + bigBlind;

        System.out.println(sb.getNome() + " pagou Small Blind: " + smallBlind);
        System.out.println(bb.getNome() + " pagou Big Blind: " + bigBlind);
    }

    private void nextDealer() {
        dealerIndex = (dealerIndex + 1) % jogadores.size();
    }

    private int jogadoresAtivos(){
        int ativos = 0;

        for (Jogador jogador: jogadores){
            if (jogador.getAtivo() && !jogador.getEliminado()){
                ativos++;
            }
        }

        return ativos;
    }

    public void finalizarRodada() {
        // distribuir o pote para o vencedor (ou dividir)
        nextDealer();
    }

}
