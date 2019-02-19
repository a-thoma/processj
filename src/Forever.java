
public class Forever {

    public static boolean istrue() { return true; }
    public static Long foo() { return 5l; }
    
    public static interface Test<T> {
        default String getName() {
            return this.getClass().getSimpleName();
        }
    }
    
    public static class Animal implements Test<Animal> {
    }
    
    public static class WingedAnimal implements Test<WingedAnimal> {
    }
    
    public static void main(String...args) {
        boolean a = true;
//        while (a)
//            ;
        System.out.println(new Animal().getName());
    }
}
