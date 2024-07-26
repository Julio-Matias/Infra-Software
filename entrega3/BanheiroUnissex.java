import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Pessoa implements Runnable {
    private int id;
    private String genero;
    private Banheiro banheiro;

    public Pessoa(int id, String genero, Banheiro banheiro) {
        this.id = id;
        this.genero = genero;
        this.banheiro = banheiro;
    }

    @Override
    public void run() {
        try {
            banheiro.usarBanheiro(id, genero);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

class Banheiro {
    private static final int CAPACIDADE_MAXIMA = 3;
    private Semaphore semaforo = new Semaphore(CAPACIDADE_MAXIMA);
    private Lock trava = new ReentrantLock();
    private String generoAtual = null;
    private int contagemAtual = 0;

    public void usarBanheiro(int pessoaId, String genero) throws InterruptedException {
        boolean entrou = false;oc

        while (!entrou) {
            trava.lock();
            try {
                // Verifica se o gênero atual é nulo ou igual ao gênero desejado
                if (generoAtual == null || generoAtual.equals(genero)) {
                    if (contagemAtual == 0) {
                        generoAtual = genero; // Define o gênero atual quando a contagem é zero
                    }

                    if (semaforo.tryAcquire()) { // Tenta adquirir o semáforo
                        contagemAtual++;
                        entrou = true; // Marca que a pessoa conseguiu entrar
                        System.out.println(genero + " " + pessoaId + " entrou no banheiro.");
                    }
                }
            } finally {
                trava.unlock();
            }

            if (entrou) {
                // Simula o tempo gasto no banheiro
                Thread.sleep((long) (Math.random() * 1000));

                trava.lock();
                try {
                    semaforo.release();
                    contagemAtual--;
                    System.out.println(genero + " " + pessoaId + " saiu do banheiro.");

                    if (contagemAtual == 0) {
                        generoAtual = null; // Libera o banheiro para o próximo gênero
                    }
                } finally {
                    trava.unlock();
                }
            } else {
                // Se não conseguiu entrar, espera um pouco antes de tentar novamente
                Thread.sleep(100);
            }
        }
    }
}

public class BanheiroUnissex {
    public static void main(String[] args) {
        Banheiro banheiro = new Banheiro();
        ExecutorService executorPessoas = Executors.newFixedThreadPool(100);

        // Cria pessoas com gêneros alternados
        for (int i = 0; i < 100; i++) {
            String genero = (i % 2 == 0) ? "Masculino" : "Feminino";
            executorPessoas.execute(new Pessoa(i, genero, banheiro));
        }

        executorPessoas.shutdown();
    }
}
