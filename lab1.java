import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class lab1 {
    private static final BlockingQueue<CoffeeOrder> orderQueue = new LinkedBlockingQueue<>(10);
    private static volatile boolean isOpen = true;
    public static void main(String[] args) {
        Thread barista = new Thread(new Barista(), "Barista");
        barista.start();
        for (int i = 1; i <= 10; i++) {
            new Thread(new Customer(i)).start();
            try {
                Thread.sleep((int) (Math.random() * 800)); 
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        try {
            Thread.sleep(12000); 
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        isOpen = false;
        System.out.println("Кав'ярня зачиняється! Бариста завершить обробку всіх замовлень.");
    }
    static class CoffeeOrder {
        private final int customerId;
        private final int preparationTime;

        public CoffeeOrder(int customerId, int preparationTime) {
            this.customerId = customerId;
            this.preparationTime = preparationTime;
        }

        public int getCustomerId() {
            return customerId;
        }

        public int getPreparationTime() {
            return preparationTime;
        }
    }
    static class Customer implements Runnable {
        private final int customerId;

        public Customer(int customerId) {
            this.customerId = customerId;
        }

        @Override
        public void run() {
            if (!isOpen) {
                System.out.println("Клієнт " + customerId + " прийшов, але кав'ярня вже зачинена.");
                return;
            }

            int preparationTime = 2000 + (int) (Math.random() * 2000); // Час приготування від 2 до 4 секунд
            CoffeeOrder order = new CoffeeOrder(customerId, preparationTime);

            try {
                if (orderQueue.offer(order, 2, TimeUnit.SECONDS)) {
                    System.out.println("Клієнт " + customerId + " зробив замовлення. Час приготування: " 
                                        + preparationTime / 1000 + " сек.");
                } else {
                    System.out.println("Клієнт " + customerId + " пішов, бо черга зайнята.");
                }
            } catch (InterruptedException e) {
                System.err.println("Помилка у клієнта " + customerId);
            }
        }
    }
    static class Barista implements Runnable {
        @Override
        public void run() {
            while (isOpen || !orderQueue.isEmpty()) {
                try {
                    CoffeeOrder order = orderQueue.poll(2, TimeUnit.SECONDS);
                    if (order != null) {
                        System.out.println("Бариста почав готувати каву для клієнта " + order.getCustomerId());
                        Thread.sleep(order.getPreparationTime());
                        System.out.println("Бариста завершив замовлення для клієнта " + order.getCustomerId());
                    }
                } catch (InterruptedException e) {
                    System.err.println("Бариста перерваний під час роботи.");
                }
            }
            System.out.println("Бариста завершив усі замовлення і пішов додому.");
        }
    }
}
