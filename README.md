# lab-reti-21-22
Esercizi relativi al corso Laboratorio di Reti - A.A. 2021-2022
La repository è strutturata in lezioni, che vertono su argomenti mirati. Ogni directory
lect\([0-9])\* contiene gli esercizi relativi a quella lezione. La directory assignments contiene
gli assignment di ogni lezione, ognuno nella propria directory della forma a([0-9])\*
## Lezione 1 - Threads
- es1: programma non multithreaded
- es2: programma equivalente, ma MT con classe che estende Thread
- es3: programma equivalente, ma MT con classe che implementa Runnable
- Assignment: calcolo approssimato di PI utilizzando la serie di Gregory-Leibnitz
## Lezione 2 - Thread pools e Callable/Future
- es1/es2: programma multithreaded che simula una biglietteria con 4 sportelli che ricevono persone (uso di threadpool)
- es3: programma multithreaded che calcola n^2 + ... + n^50 utilizzando una thread pool
- Assignment: simulazione di ufficio postale
## Lezione 3 - Lock e concorrenza
- es1: classe Counter condivisa da un insieme di thread lettori ed un insieme di thread scrittori
  - v1: Concorrenza controllata tramite ReentrantLock sia per lettori che scrittori
  - v2: Concorrenza controllata tramite ReadWriteLock, con lock diversificate tra scrittori e lettori (thread gestiti da CachedThreadPool)
  - v3: Concorrenza controllata tramite ReadWriteLock, con lock diversificate tra scrittori e lettori (thread gestiti da FixedThreadPool)
- Assignment: simulazione di un laboratorio informatico cui afferiscono Studenti, Tesisti e Professori
## Lezione 4 - Monitor e concurrent Collections
- es1: Estensione con monitor di una classe Dropbox (buffer di dimensione unitaria) su cui operano un Producer e due Consumer
- assignment: Implementazione dell'assignment del laboratorio informatico con monitor
