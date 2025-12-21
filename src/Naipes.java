public enum Naipes {
    ESPADAS("♠"),
    COPAS("♥"),
    OUROS("♦"),
    PAUS("♣");

    private final String symbol;

    Naipes(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }
}

