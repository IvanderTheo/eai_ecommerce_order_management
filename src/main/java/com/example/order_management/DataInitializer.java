package com.example.order_management;

import com.example.order_management.entity.Category;
import com.example.order_management.entity.Customer;
import com.example.order_management.entity.Product;
import com.example.order_management.repository.CategoryRepository;
import com.example.order_management.repository.CustomerRepository;
import com.example.order_management.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Override
    public void run(String... args) throws Exception {
        // Create Categories
        if (categoryRepository.count() == 0) {
            Category elektronik = new Category("Elektronik", "Perangkat elektronik dan gadget");
            Category furniture = new Category("Furniture", "Perabotan rumah dan kantor");
            Category fashion = new Category("Fashion", "Pakaian dan aksesori");
            
            categoryRepository.save(elektronik);
            categoryRepository.save(furniture);
            categoryRepository.save(fashion);
            
            System.out.println("✓ 3 Kategori berhasil ditambahkan");
        }

        // Create Products
        if (productRepository.count() == 0) {
            Category elektronik = categoryRepository.findAll().stream()
                    .filter(c -> c.getName().equals("Elektronik")).findFirst().orElse(null);
            Category furniture = categoryRepository.findAll().stream()
                    .filter(c -> c.getName().equals("Furniture")).findFirst().orElse(null);
            Category fashion = categoryRepository.findAll().stream()
                    .filter(c -> c.getName().equals("Fashion")).findFirst().orElse(null);

            Product laptop = new Product("Laptop Asus VivoBook", 8000000.0, 10);
            laptop.setSku("SKU-001");
            laptop.setCategory(elektronik);
            productRepository.save(laptop);

            Product mouse = new Product("Mouse Logitech", 150000.0, 50);
            mouse.setSku("SKU-002");
            mouse.setCategory(elektronik);
            productRepository.save(mouse);

            Product keyboard = new Product("Keyboard Mechanical", 500000.0, 30);
            keyboard.setSku("SKU-003");
            keyboard.setCategory(elektronik);
            productRepository.save(keyboard);

            Product meja = new Product("Meja Kerja Minimalis", 1500000.0, 15);
            meja.setSku("SKU-004");
            meja.setCategory(furniture);
            productRepository.save(meja);

            Product kursi = new Product("Kursi Ergonomis", 2000000.0, 20);
            kursi.setSku("SKU-005");
            kursi.setCategory(furniture);
            productRepository.save(kursi);

            Product kemeja = new Product("Kemeja Pria Premium", 250000.0, 100);
            kemeja.setSku("SKU-006");
            kemeja.setCategory(fashion);
            productRepository.save(kemeja);

            Product celana = new Product("Celana Jeans", 350000.0, 80);
            celana.setSku("SKU-007");
            celana.setCategory(fashion);
            productRepository.save(celana);

            Product sepatu = new Product("Sepatu Olahraga", 450000.0, 60);
            sepatu.setSku("SKU-008");
            sepatu.setCategory(fashion);
            productRepository.save(sepatu);

            System.out.println("✓ 8 Produk berhasil ditambahkan");
        }

        // Create Customers
        if (customerRepository.count() == 0) {
            Customer customer1 = new Customer("John Doe", "john@example.com", "Jl. Merdeka No. 123, Jakarta");
            Customer customer2 = new Customer("Jane Smith", "jane@example.com", "Jl. Sudirman No. 456, Bandung");
            Customer customer3 = new Customer("Rani Wijaya", "rani@example.com", "Jl. Ahmad Yani No. 789, Surabaya");
            Customer customer4 = new Customer("Budi Santoso", "budi@example.com", "Jl. Malioboro No. 321, Yogyakarta");
            Customer customer5 = new Customer("Siti Nurhaliza", "siti@example.com", "Jl. Raya No. 654, Medan");

            customerRepository.save(customer1);
            customerRepository.save(customer2);
            customerRepository.save(customer3);
            customerRepository.save(customer4);
            customerRepository.save(customer5);

            System.out.println("✓ 5 Customer berhasil ditambahkan");
        }

        System.out.println("═══════════════════════════════════════");
        System.out.println("✓ Data Initialization Selesai!");
        System.out.println("═══════════════════════════════════════");
    }
}
