package app;

public interface AppMBean {

    /*
    Metodę zwracającą wiadomość o stanie aplikacji tj.:

        liczbie wątków,

        stanie pamięci podręcznej (liczba zajętych wpisów, liczba wolnych wpisów),

        procencie chybień (błędów) pamięci podręcznej.
     */

    String getDescription();

    void setThread(int idx);
    int getThread();

    void setMemorySize(int i);
    int getMemorySize();

}
