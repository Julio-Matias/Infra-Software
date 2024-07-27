import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;


class ParadaOnibus {
    private final Semaphore assentos;
    private final Semaphore esperaOnibus; 
    private final int capacidade; 

    
    public ParadaOnibus(int capacidade) {
        // inicializa a variável e os semaphoros
        this.capacidade = capacidade;
        this.assentos = new Semaphore(capacidade, true); 
        this.esperaOnibus = new Semaphore(0);
    }


    public void entrarNoOnibus(String passageiro) {
        if (assentos.tryAcquire()) { // passageiro tenta pegar um assento
            System.out.println(passageiro + " entrou");
        } else {
            System.out.println(passageiro + " não conseguiu entrar");
        }
    }


    public void onibusChegando() throws InterruptedException {
        System.out.println("Ônibus chegou");
        esperaOnibus.release(capacidade); // liber as vagas do ônibus
        Thread.sleep(ThreadLocalRandom.current().nextInt(1000, 3000));
        esperaOnibus.acquire(capacidade); // espera capacidade máxima ser atingida
        System.out.println("Ônibus partiu da parada.");
        assentos.release(capacidade); // libera os assentos
    }

    
    public Semaphore getEsperaOnibus() {
        return esperaOnibus;
    }
}


class Passageiro implements Runnable {
    private final String nome; 
    private final ParadaOnibus parada; 

    
    public Passageiro(String nome, ParadaOnibus parada) {
        this.nome = nome;
        this.parada = parada;
    }

    
    @Override
    public void run() {
        try {
            while (true) {
                parada.getEsperaOnibus().acquire(); // espera o ônibus
                parada.entrarNoOnibus(nome); // eenta entrar no ônibus
                parada.getEsperaOnibus().release();
                Thread.sleep(ThreadLocalRandom.current().nextInt(2000, 5000));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}


public class Onibus {
    public static void main(String[] args) {
        int capacidade = 50; // define a capacidade do ônibus
        int numeroDePassagens = 2; // define quantas vezes o ônibus irá passar
        ParadaOnibus parada = new ParadaOnibus(capacidade);

        // threads para passageiros
        for (int i = 1; i <= 100; i++) {
            Thread passageiroThread = new Thread(new Passageiro("Passageiro " + i, parada));
            passageiroThread.start();
        }

        // chegada dos ônibus
        for (int i = 0; i < numeroDePassagens; i++) {
            try {
                Thread.sleep(ThreadLocalRandom.current().nextInt(1000, 3000)); // tempo de chegada
                parada.onibusChegando();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

    }
}
