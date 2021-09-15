import java.lang.Math; // per Math.PI

/**
 * Questa classe implementa Runnable per definire una task che calcoli un'approssimazione
 * del valore di PI (Math.PI è usato come valore di riferimento) utilizzando la serie di
 * Gregory-Leibnitz. Il costruttore prende come parametro la precisione da raggiungere,
 * cioè la massima differenza (in valore assoluto) dal valore di Math.PI ammessa.
 * La task termina in due casi: se la precisione corrente è strettamente minore di accuracy
 * oppure se il thread che esegue la task è stato interrotto. In entrambi i casi vengono
 * stampati su standard output sia il valore approssimato (con tutte le cifre decimali, ma
 * di cui solo una parte potrebbe essere corretta, a seconda della precisione raggiunta)
 * che il numero di iterazioni eseguite e la differenza in valore assoluto dell'approssimazione
 * ottenuta da Math.PI
 */
public class LeibnitzSeries implements Runnable {
    // Approssimazione corrente
    private double approx;
    // La massima differenza (valore assoluto) che può esservi tra l'approssimazione finale e Math.PI
    // (a meno che il thread non sia stato interrotto)
    private final double accuracy;

    // Il costruttore riceve in input la precisione da raggiungere
    public LeibnitzSeries(double accuracy) {
        this.accuracy = accuracy;
    }

    public void run() {
        // Inizializzo con il primo termine della serie, che è 4, per cui posso considerarla
        // come un'iterazione già svolta
        this.approx = 4;
        long nIter = 1;
        // Se il thread che esegue la task è stato interrotto allora termino anche se
        // non ho raggiunto la precisione richiesta. Se la precisione raggiunta è minore o
        // uguale di accuracy allora termino la task
        while(Math.abs(approx - Math.PI) > this.accuracy && Thread.currentThread().isInterrupted() == false) {
            double next = 4.0 / (2 * nIter + 1);
            if(nIter % 2 == 0) {
                this.approx += next;
            }
            else {
                this.approx -= next;
            }
            nIter++;
        }
        double reached_accuracy = Math.abs(approx - Math.PI);
        // Messaggio stampato sse la precisione non è stata raggiunta
        // NOTA: se la precisione è raggiunta durante una iterazione ed il thread viene
        // interrotto subito prima di testare la guardia del while nella successiva iterazione
        // questo messaggio non viene stampato, per via della valutazione con short-circuit
        // dell'operatore logico AND. Se avessi invece utilizzato la condizione  "Thread.currentThread().isInterrupted()"
        // per l'if sottostante ciò sarebbe invece avvenuto, nonostante la approssimazione
        // fosse stata ottenuta prima dell'interruzione
        System.out.println("\n****");
        if(reached_accuracy > this.accuracy) {
            System.out.println("Tempo massimo raggiunto: il thread è stato interrotto");
        }
        System.out.println("PI è circa " + Double.toString(this.approx)
            + "\niterazioni: " + nIter
            + "\nprecisione: " + reached_accuracy + " (obiettivo: " + this.accuracy + ")\n****\n");
    }
}
