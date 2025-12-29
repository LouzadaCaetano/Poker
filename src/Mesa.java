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
    private int maiorAposta;
    private int dealerIndex;

    private Scanner sc = new Scanner(System.in);

    public Mesa(List<Jogador> jogadores){
        this.jogadores = new ArrayList<>(jogadores);
        this.cartasMesa = new ArrayList<>();
        this.dealerIndex = 0;
    }

    public void iniciarJogo(){
        while(jogadoresAtivos() > 1){
            controleJogo();
            dealerIndex = (dealerIndex + 1) % jogadores.size();
        }
    }

    private void iniciarRodada() {
        baralho = new Baralho();
        baralho.shuffle();
        cartasMesa.clear();
        this.winningPot = 0;

        for(Jogador jogador: this.jogadores){
            if (jogador.getMoedas() == 0){
                jogador.setAtivo(false);
                jogador.setEliminado(true);
            }else{
                jogador.limparMao();
            }
        }
    }

    private void controleJogo() {
        iniciarRodada();

        cobraBlinds();
        distribuirCartasJogadores();

        executarRodadaDeApostas(); // pré-flop
        if (jogadoresAtivos() <= 1) {
            finalizarRodada();
            return;
        }

        distribuirFlop();
        resetarApostasDaFase();
        executarRodadaDeApostas();

        distribuirTurn_River();
        resetarApostasDaFase();
        executarRodadaDeApostas();

        distribuirTurn_River();
        resetarApostasDaFase();
        executarRodadaDeApostas();

        finalizarRodada(); // showdown
    }

    private void distribuirFlop() {
        for (int i = 0; i < 3; i++) {
            cartasMesa.add(baralho.comprarCarta());
        }
        printMesa();
    }

    private void distribuirTurn_River(){
        cartasMesa.add(baralho.comprarCarta());
        printMesa();
    }

    private void printMesa(){
        System.out.println("\nCartas na mesa:");
        for (Carta card: this.cartasMesa){
            card.printCarta();
        }

    }

    private void distribuirCartasJogadores() {
        for (int i = 0; i < 2; i++) { // duas cartas para cada jogador
            for (Jogador jogador : jogadores) {
                Carta card = baralho.comprarCarta();
                jogador.receberCarta(card);
            }
        }
    }

    private void turnoJogador(Jogador jogador, int maiorAposta) {
        System.out.println("\nVez de: " + jogador.getNome());
        System.out.println("Moedas: " + jogador.getMoedas());
        System.out.println("Aposta atual: " + jogador.getApostaAtual());
        System.out.println("Maior aposta da rodada: " + maiorAposta);

        jogador.getMao();

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

    private void turnoBot(Jogador bot, int maiorAposta){
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

    private void executarRodadaDeApostas() {
        boolean rodadaEncerrada;

        do {
            rodadaEncerrada = true;

            for (Jogador jogador : jogadores) {

                //Valida se o jogar pode fazer algo na rodada
                if (!jogador.getAtivo()) continue;
                if (jogador.getMoedas() == 0) continue;

                // Se só sobrou um jogador ativo, encerra
                if (jogadoresAtivos() <= 1) {
                    return;
                }

                // Jogador ainda precisa responder à maior aposta
                if (jogador.getApostaAtual() < maiorAposta) {
                    rodadaEncerrada = false;

                    if (jogador.getBot()) {
                        turnoBot(jogador, maiorAposta);
                    } else {
                        turnoJogador(jogador, maiorAposta);
                    }
                }
            }

        } while (!rodadaEncerrada);
    }

    private void resetarApostasDaFase(){
        this.maiorAposta = 0;
        for (Jogador jogador: this.jogadores){
            jogador.resetarAposta();
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
        this.maiorAposta = jogador.getApostaAtual();

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
        this.maiorAposta = bot.getApostaAtual();

        if (valorRaise == 0) {
            System.out.println(bot.getNome() + " deu CALL (" + total + ")");
        } else {
            System.out.println(bot.getNome() + " deu RAISE de " + valorRaise +
                    " (total apostado: " + total + ")");
        }
    }

    private void allIn(Jogador jogador){
        int valor = jogador.getMoedas();

        if (valor <= 0) {
            return;
        }

        jogador.apostar(valor);
        winningPot += valor;

        if (jogador.getApostaAtual() > maiorAposta) {
            maiorAposta = jogador.getApostaAtual();
        }

        System.out.println(jogador.getNome() + " foi ALL-IN com " + valor);
    }

    private void cobraBlinds(){
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
        this.maiorAposta = bigBlind;

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

    private void finalizarRodada() {

        //Vitória automática por fold
        if (jogadoresAtivos() == 1) {
            for (Jogador jogador : jogadores) {
                if (jogador.getAtivo()) {
                    jogador.recebePremio(winningPot); // ou jogador.addMoedas(...)
                    System.out.println(jogador.getNome() + " ganhou o pote de " + winningPot + " por fold!");
                    break;
                }
            }
        }
        //Showdown (placeholder)
        else {
            System.out.println("\n--- SHOWDOWN ---");
            for (Jogador jogador : jogadores) {
                if (jogador.getAtivo()) {
                    System.out.println(jogador.getNome() + " chegou ao showdown.");
                }
            }

            // TEMPORÁRIO: até o AvaliadorDeMao existir
            Jogador vencedor = null;
            for (Jogador jogador : jogadores) {
                if (jogador.getAtivo()) {
                    vencedor = jogador;
                    break;
                }
            }

            if (vencedor != null) {
                vencedor.recebePremio(winningPot);
                System.out.println(vencedor.getNome() + " ganhou o pote de " + winningPot + " (temporário).");
            }
        }

        winningPot = 0;
        maiorAposta = 0;

        nextDealer();
    }

}
