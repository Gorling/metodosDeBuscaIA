import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import busca.Heuristica;
import busca.BuscaLargura;
import busca.BuscaProfundidade;
import busca.AEstrela;
import busca.Estado;
import busca.MostraStatusConsole;
import busca.Nodo;
import busca.SubidaMontanha;
import javax.swing.JOptionPane;

public class LabirintoObstaculos implements Estado, Heuristica {
    
    final char matriz[][];
    int linhaEntrada, colunaEntrada; // Posição do primeiro "E"
    int linhaEntrada2, colunaEntrada2; // Posição do segundo "E"
    int linhaSaida, colunaSaida; // Posição do "S"
    final String op;
    boolean primeiraFaseConcluida; // Indica se o "E" chegou ao "S"

    char[][] clonar(char origem[][]) {
        char destino[][] = new char[origem.length][origem[0].length];
        for (int i = 0; i < origem.length; i++) {
            for (int j = 0; j < origem[0].length; j++) {
                destino[i][j] = origem[i][j];
            }
        }
        return destino;
    }

    // Construtor para estados gerados na evolução
    public LabirintoObstaculos(char m[][], int linhaE, int colunaE, int linhaE2, int colunaE2, 
                               int linhaS, int colunaS, String o, boolean faseConcluida) {
        this.matriz = m;
        this.linhaEntrada = linhaE;
        this.colunaEntrada = colunaE;
        this.linhaEntrada2 = linhaE2;
        this.colunaEntrada2 = colunaE2;
        this.linhaSaida = linhaS;
        this.colunaSaida = colunaS;
        this.op = o;
        this.primeiraFaseConcluida = faseConcluida;
    }

    // Construtor para o estado inicial
    public LabirintoObstaculos(int dimensao, String o, int porcentagemObstaculos) {
        this.matriz = new char[dimensao][dimensao];
        this.op = o;
        this.primeiraFaseConcluida = false;

        int quantidadeObstaculos = (dimensao * dimensao) * porcentagemObstaculos / 100;
        Random gerador = new Random();

        int entrada = gerador.nextInt(dimensao * dimensao);
        int entrada2;
        int saida;
        do {
            entrada2 = gerador.nextInt(dimensao * dimensao);
        } while (entrada2 == entrada);
        do {
            saida = gerador.nextInt(dimensao * dimensao);
        } while (saida == entrada || saida == entrada2);

        int contaPosicoes = 0;
        for (int i = 0; i < dimensao; i++) {
            for (int j = 0; j < dimensao; j++) {
                if (contaPosicoes == entrada) {
                    this.matriz[i][j] = 'E';
                    this.linhaEntrada = i;
                    this.colunaEntrada = j;
                } else if (contaPosicoes == entrada2) {
                    this.matriz[i][j] = 'F'; // "F" para o segundo "E" (E2)
                    this.linhaEntrada2 = i;
                    this.colunaEntrada2 = j;
                } else if (contaPosicoes == saida) {
                    this.matriz[i][j] = 'S';
                    this.linhaSaida = i;
                    this.colunaSaida = j;
                } else if (quantidadeObstaculos > 0 && gerador.nextInt(3) == 1) {
                    quantidadeObstaculos--;
                    this.matriz[i][j] = '@';
                } else {
                    this.matriz[i][j] = 'O';
                }
                contaPosicoes++;
            }
        }
    }

    @Override
    public boolean ehMeta() {
        // Meta: "E" chegou ao "S" e depois "E2" chegou ao "S"
        return primeiraFaseConcluida && linhaEntrada2 == linhaSaida && colunaEntrada2 == colunaSaida;
    }

    @Override
    public int custo() {
        return 1;
    }

    @Override
    public int h() {
        if (!primeiraFaseConcluida) {
            return Math.abs(linhaEntrada - linhaSaida) + Math.abs(colunaEntrada - colunaSaida);
        } else {
            return Math.abs(linhaEntrada2 - linhaSaida) + Math.abs(colunaEntrada2 - colunaSaida);
        }
    }

    @Override
    public List<Estado> sucessores() {
        List<Estado> visitados = new LinkedList<>();
        if (!primeiraFaseConcluida) {
            // Movimentar apenas o "E"
            paraCimaE(visitados);
            paraBaixoE(visitados);
            paraEsquerdaE(visitados);
            paraDireitaE(visitados);
        } else {
            // Movimentar apenas o "E2"
            paraCimaE2(visitados);
            paraBaixoE2(visitados);
            paraEsquerdaE2(visitados);
            paraDireitaE2(visitados);
        }
        return visitados;
    }

    // Movimentos do "E"
    private void paraCimaE(List<Estado> visitados) {
        if (linhaEntrada == 0 || matriz[linhaEntrada - 1][colunaEntrada] == '@' || 
            (linhaEntrada - 1 == linhaEntrada2 && colunaEntrada == colunaEntrada2)) return;

        char mTemp[][] = clonar(matriz);
        int linhaTemp = linhaEntrada - 1;
        mTemp[linhaEntrada][colunaEntrada] = 'O';
        mTemp[linhaTemp][colunaEntrada] = 'E';

        boolean faseConcluida = (linhaTemp == linhaSaida && colunaEntrada == colunaSaida);
        LabirintoObstaculos novo = new LabirintoObstaculos(mTemp, linhaTemp, colunaEntrada, 
            linhaEntrada2, colunaEntrada2, linhaSaida, colunaSaida, "Movendo E para cima", faseConcluida);
        if (!visitados.contains(novo)) visitados.add(novo);
    }

    private void paraBaixoE(List<Estado> visitados) {
        if (linhaEntrada == matriz.length - 1 || matriz[linhaEntrada + 1][colunaEntrada] == '@' || 
            (linhaEntrada + 1 == linhaEntrada2 && colunaEntrada == colunaEntrada2)) return;

        char mTemp[][] = clonar(matriz);
        int linhaTemp = linhaEntrada + 1;
        mTemp[linhaEntrada][colunaEntrada] = 'O';
        mTemp[linhaTemp][colunaEntrada] = 'E';

        boolean faseConcluida = (linhaTemp == linhaSaida && colunaEntrada == colunaSaida);
        LabirintoObstaculos novo = new LabirintoObstaculos(mTemp, linhaTemp, colunaEntrada, 
            linhaEntrada2, colunaEntrada2, linhaSaida, colunaSaida, "Movendo E para baixo", faseConcluida);
        if (!visitados.contains(novo)) visitados.add(novo);
    }

    private void paraEsquerdaE(List<Estado> visitados) {
        if (colunaEntrada == 0 || matriz[linhaEntrada][colunaEntrada - 1] == '@' || 
            (linhaEntrada == linhaEntrada2 && colunaEntrada - 1 == colunaEntrada2)) return;

        char mTemp[][] = clonar(matriz);
        int colunaTemp = colunaEntrada - 1;
        mTemp[linhaEntrada][colunaEntrada] = 'O';
        mTemp[linhaEntrada][colunaTemp] = 'E';

        boolean faseConcluida = (linhaEntrada == linhaSaida && colunaTemp == colunaSaida);
        LabirintoObstaculos novo = new LabirintoObstaculos(mTemp, linhaEntrada, colunaTemp, 
            linhaEntrada2, colunaEntrada2, linhaSaida, colunaSaida, "Movendo E para esquerda", faseConcluida);
        if (!visitados.contains(novo)) visitados.add(novo);
    }

    private void paraDireitaE(List<Estado> visitados) {
        if (colunaEntrada == matriz[0].length - 1 || matriz[linhaEntrada][colunaEntrada + 1] == '@' || 
            (linhaEntrada == linhaEntrada2 && colunaEntrada + 1 == colunaEntrada2)) return;

        char mTemp[][] = clonar(matriz);
        int colunaTemp = colunaEntrada + 1;
        mTemp[linhaEntrada][colunaEntrada] = 'O';
        mTemp[linhaEntrada][colunaTemp] = 'E';

        boolean faseConcluida = (linhaEntrada == linhaSaida && colunaTemp == colunaSaida);
        LabirintoObstaculos novo = new LabirintoObstaculos(mTemp, linhaEntrada, colunaTemp, 
            linhaEntrada2, colunaEntrada2, linhaSaida, colunaSaida, "Movendo E para direita", faseConcluida);
        if (!visitados.contains(novo)) visitados.add(novo);
    }

    // Movimentos do "E2"
    private void paraCimaE2(List<Estado> visitados) {
        if (linhaEntrada2 == 0 || matriz[linhaEntrada2 - 1][colunaEntrada2] == '@') return;

        char mTemp[][] = clonar(matriz);
        int linhaTemp = linhaEntrada2 - 1;
        mTemp[linhaEntrada2][colunaEntrada2] = 'O';
        mTemp[linhaTemp][colunaEntrada2] = 'F';

        LabirintoObstaculos novo = new LabirintoObstaculos(mTemp, linhaEntrada, colunaEntrada, 
            linhaTemp, colunaEntrada2, linhaSaida, colunaSaida, "Movendo E2 para cima", true);
        if (!visitados.contains(novo)) visitados.add(novo);
    }

    private void paraBaixoE2(List<Estado> visitados) {
        if (linhaEntrada2 == matriz.length - 1 || matriz[linhaEntrada2 + 1][colunaEntrada2] == '@') return;

        char mTemp[][] = clonar(matriz);
        int linhaTemp = linhaEntrada2 + 1;
        mTemp[linhaEntrada2][colunaEntrada2] = 'O';
        mTemp[linhaTemp][colunaEntrada2] = 'F';

        LabirintoObstaculos novo = new LabirintoObstaculos(mTemp, linhaEntrada, colunaEntrada, 
            linhaTemp, colunaEntrada2, linhaSaida, colunaSaida, "Movendo E2 para baixo", true);
        if (!visitados.contains(novo)) visitados.add(novo);
    }

    private void paraEsquerdaE2(List<Estado> visitados) {
        if (colunaEntrada2 == 0 || matriz[linhaEntrada2][colunaEntrada2 - 1] == '@') return;

        char mTemp[][] = clonar(matriz);
        int colunaTemp = colunaEntrada2 - 1;
        mTemp[linhaEntrada2][colunaEntrada2] = 'O';
        mTemp[linhaEntrada2][colunaTemp] = 'F';

        LabirintoObstaculos novo = new LabirintoObstaculos(mTemp, linhaEntrada, colunaEntrada, 
            linhaEntrada2, colunaTemp, linhaSaida, colunaSaida, "Movendo E2 para esquerda", true);
        if (!visitados.contains(novo)) visitados.add(novo);
    }

    private void paraDireitaE2(List<Estado> visitados) {
        if (colunaEntrada2 == matriz[0].length - 1 || matriz[linhaEntrada2][colunaEntrada2 + 1] == '@') return;

        char mTemp[][] = clonar(matriz);
        int colunaTemp = colunaEntrada2 + 1;
        mTemp[linhaEntrada2][colunaEntrada2] = 'O';
        mTemp[linhaEntrada2][colunaTemp] = 'F';

        LabirintoObstaculos novo = new LabirintoObstaculos(mTemp, linhaEntrada, colunaEntrada, 
            linhaEntrada2, colunaTemp, linhaSaida, colunaSaida, "Movendo E2 para direita", true);
        if (!visitados.contains(novo)) visitados.add(novo);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof LabirintoObstaculos) {
            LabirintoObstaculos e = (LabirintoObstaculos) o;
            for (int i = 0; i < matriz.length; i++) {
                for (int j = 0; j < matriz[0].length; j++) {
                    if (e.matriz[i][j] != this.matriz[i][j]) return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        String estado = "";
        for (int i = 0; i < matriz.length; i++) {
            for (int j = 0; j < matriz[0].length; j++) {
                estado += matriz[i][j];
            }
        }
        return estado.hashCode();
    }

    @Override
    public String toString() {
        StringBuffer resultado = new StringBuffer();
        for (int i = 0; i < matriz.length; i++) {
            for (int j = 0; j < matriz[0].length; j++) {
                resultado.append(matriz[i][j]).append("\t");
            }
            resultado.append("\n");
        }
        resultado.append("Posição E: ").append(linhaEntrada).append(",").append(colunaEntrada).append("\n");
        resultado.append("Posição E2: ").append(linhaEntrada2).append(",").append(colunaEntrada2).append("\n");
        resultado.append("Posição Saída: ").append(linhaSaida).append(",").append(colunaSaida).append("\n");
        return "\n" + op + "\n" + resultado + "\n\n";
    }

    public static void main(String[] a) {
        LabirintoObstaculos estadoInicial = null;
        int dimensao;
        int porcentagemObstaculos;
        int qualMetodo;
        Nodo n;
        try {
            dimensao = Integer.parseInt(JOptionPane.showInputDialog("Entre com a dimensão do Puzzle!"));
            porcentagemObstaculos = Integer.parseInt(JOptionPane.showInputDialog("Porcentagem de obstáculos!"));
            qualMetodo = Integer.parseInt(JOptionPane.showInputDialog("1 - Profundidade\n2 - Largura\n3 - AEstrela"));
            estadoInicial = new LabirintoObstaculos(dimensao, "estado inicial", porcentagemObstaculos);

            switch (qualMetodo) {
                case 1:
                    System.out.println("busca em PROFUNDIDADE");
                    n = new BuscaProfundidade(new MostraStatusConsole()).busca(estadoInicial);
                    break;
                case 2:
                    System.out.println("busca em LARGURA");
                    n = new BuscaLargura(new MostraStatusConsole()).busca(estadoInicial);
                    break;
                case 3:
                    System.out.println("busca em AEstrela");
                    n = new AEstrela(new MostraStatusConsole()).busca(estadoInicial);
                    break;
                case 4:
                    System.out.println("busca em SUBIDA MONTANHA");
                    n = new SubidaMontanha(new MostraStatusConsole()).busca(estadoInicial);
                    break;
                default:
                    n = null;
                    JOptionPane.showMessageDialog(null, "Método não implementado");
            }
            if (n == null) {
                System.out.println("sem solução!");
                System.out.println(estadoInicial);
            } else {
                System.out.println("solução:\n" + n.montaCaminho() + "\n\n");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
        System.exit(0);
    }

    @Override
    public String getDescricao() {
        return "O jogo do labirinto é uma matriz NxM com duas entradas (E e E2) e uma saída (S).\n" +
               "O primeiro 'E' deve chegar ao 'S', e só depois o 'F' começa a se mover.\n" +
               "Nenhum 'E' pode passar por '@' ou pelo outro 'F'.";
    }
}
