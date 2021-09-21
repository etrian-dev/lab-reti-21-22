import java.util.Vector;

public class MainClass {
    public static void main(String[] args) {
        // un ufficio postale la cui sala d'attesa interna può ospitare fino a k persone
        int k = 10; // TODO: parametrize
        Vector<Person> all_the_people = new Vector<Person>();
        for(int i = 0; i < 100; i++) {
            all_the_people.add(new Person(i));
        }
        PostOffice office = new PostOffice(k, all_the_people);
        office.start();
    }
}
