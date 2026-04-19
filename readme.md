 PDF To Markdown Converter
Debug View
Result View
09 Modul EAI Membuat API Order 101
MODULE
EAI: Membuat API Order Management 101
By - Dr. Achmad Arwan, S.Kom., M.Kom.
Study Program: Teknik Informatika
Department: Teknik Informatika
Faculty: Computer Science
University: Universitas Brawijaya
Year: 2026
Enterprise Aplikasi Integrasi – API 101 Orders ii
PREFACE
Modul ini membahas konsep dasar pembuatan API Order Management menggunakan Java Spring Boot.
Mahasiswa akan mempelajari cara membuat sistem order yang dapat menerima pesanan, mengelola
status pesanan, dan mengintegrasikan dengan entitas lain seperti produk dan pelanggan. Modul ini
dirancang untuk mahasiswa pemula yang belum memiliki pengalaman coding sebelumnya.

Enterprise Aplikasi Integrasi – API 101 Orders iii
TABLE OF CONTENTS
(Biarkan kosong — Word akan membuat daftar isi otomatis)

Enterprise Aplikasi Integrasi – API 101 Orders iv
LIST OF TABLES
(Opsional)

Enterprise Aplikasi Integrasi – API 101 Orders v
LIST OF FIGURES
(Opsional)

Enterprise Aplikasi Integrasi – API 101 Orders vi
CHAPTER 1 — Pengenalan API dan Order Management
A. Capaian Pembelajaran (LLO/CPMK)
Setelah mempelajari bab ini mahasiswa mampu:

Menjelaskan konsep dasar API dan perannya dalam sistem informasi
Memahami prinsip dasar manajemen order dalam sistem e-commerce
Mengidentifikasi komponen utama dalam sistem order management
Membuat project Spring Boot sederhana untuk order management
B. Materi Pembelajaran
1.1 Apa Itu API?
API (Application Programming Interface) adalah cara aplikasi berbicara satu sama lain melalui aturan
yang telah disepakati. Bayangkan API seperti pelayan di restoran - ketika pelanggan memesan makanan
(request), pelayan menyampaikan pesanan tersebut ke dapur dan kemudian membawakan makanan
jadi (response).

Dalam konteks sistem order management, API memungkinkan frontend (website atau aplikasi mobile)
untuk berkomunikasi dengan backend (server dan database) untuk melakukan operasi seperti
membuat order, melihat status order, dan memperbarui informasi order.

1.2 Konsep Dasar Order Management
Order management adalah proses mengelola siklus hidup pesanan mulai dari pemesanan hingga
pengiriman. Dalam sistem e-commerce, order management melibatkan beberapa entitas utama:

Produk : Barang yang dijual
Pelanggan : Orang yang memesan barang
Order : Transaksi pembelian
Item Order : Detail barang dalam order
1.3 Membuat Project Spring Boot
Untuk memulai, kita akan membuat project Spring Boot menggunakan Spring Initializr.

Langkah-langkah:

Buka browser dan kunjungi https://start.spring.io/
Pilih jenis project “Maven Project”
Pilih bahasa “Java”
Pilih Spring Boot versi terbaru
Isi Project Metadata:
o Group : com.example
Enterprise Aplikasi Integrasi – API 101 Orders vii
o Artifact : order-management
o Name : order-management
o Description : Order Management System
o Package name : com.example.ordermanagement
o Packaging : Jar
o Java : 11 atau yang lebih tinggi
Tambahkan dependencies berikut:
o Spring Web
o Spring Data JPA
o MySQL Driver
o Validation
Klik “Generate” untuk mengunduh project
Setelah mengunduh, ekstrak file zip dan buka project di IDE favorit Anda (IntelliJ IDEA, Eclipse, atau VS
Code).

1.4 Konfigurasi Database MySQL
Sebelum melakukan konfigurasi, pastikan MySQL sudah terinstall di komputer Anda. Jika belum, ikuti
langkah berikut:

Instalasi MySQL:

Download MySQL dari https://dev.mysql.com/downloads/mysql/
Install MySQL sesuai sistem operasi:
o Windows : Download MySQL Installer dan ikuti wizard instalasi
o macOS : Download DMG file dan drag ke Applications folder
o Linux : Gunakan package manager (sudo apt-get install mysql-server untuk Ubuntu)
Catat password root yang dibuat saat instalasi
Verifikasi instalasi dengan perintah:
mysql --version
Membuat Database:

Buka terminal atau command prompt
Login ke MySQL:
mysql - u root -p
Masukkan password root
Enterprise Aplikasi Integrasi – API 101 Orders viii
Buat database baru:
CREATE DATABASE order_management_db;
Verifikasi database telah dibuat:
SHOW DATABASES;
Keluar dari MySQL:
EXIT;
Konfigurasi application.properties:

Buka file src/main/resources/application.properties dan tambahkan konfigurasi berikut:

MySQL Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/order_management_db?useSSL=false&serverTime
zone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=your_password_here
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
spring.jpa.properties.hibernate.format_sql=true

Server Configuration
server.port=

Catatan Penting: - Ganti your_password_here dengan password MySQL root Anda -
spring.jpa.hibernate.ddl-auto=update akan otomatis membuat dan update tabel sesuai entity -
spring.jpa.show-sql=true menampilkan SQL query di console untuk debugging - Database
order_management_db harus sudah dibuat sebelum menjalankan aplikasi

Verifikasi Koneksi:

Setelah konfigurasi selesai, jalankan aplikasi dan perhatikan console. Jika berhasil terkoneksi, akan
muncul pesan:

Started OrderManagementApplication in X.XXX seconds

Jika terjadi error koneksi, periksa: 1. Apakah MySQL service sudah running? 2. Apakah username dan
password sudah benar? 3. Apakah database order_management_db sudah dibuat?

C. Ringkasan
Poin-poin penting dari bab ini:

API adalah interface yang memungkinkan aplikasi berkomunikasi
Order management melibatkan entitas produk, pelanggan, dan order
Enterprise Aplikasi Integrasi – API 101 Orders ix
Spring Boot menyederhanakan pembuatan aplikasi Java
MySQL adalah database relasional yang cocok untuk production
Struktur project Spring Boot mengikuti pola MVC (Model-View-Controller)
Database harus dibuat manual sebelum menjalankan aplikasi
D. Soal Latihan/Praktik
Jelaskan perbedaan antara API dan database dalam sistem informasi!
Apa saja entitas utama dalam sistem order management?
Buatlah project Spring Boot dengan nama “inventory-system” yang memiliki dependencies:
Spring Web, Spring Data JPA, dan H2 Database!
Jelaskan fungsi dari file application.properties!
Apa itu H2 database dan mengapa cocok untuk development?
Enterprise Aplikasi Integrasi – API 101 Orders x
CHAPTER 2 — Membuat Entitas dan Repository
A. Capaian Pembelajaran (LLO/CPMK)
Setelah mempelajari bab ini mahasiswa mampu:

Membuat entitas (Entity) dalam Spring Boot
Memahami konsep JPA (Java Persistence API)
Membuat repository untuk mengakses data
Menghubungkan entitas dengan relasi yang tepat
B. Materi Pembelajaran
2.1 Konsep JPA dan Entity
JPA (Java Persistence API) adalah spesifikasi Java yang memungkinkan kita menyimpan objek Java ke
database relasional. Entity adalah kelas Java yang merepresentasikan tabel dalam database.

Setiap entitas memiliki: - Annotation @Entity untuk menandai bahwa kelas ini adalah entity - Field yang
merepresentasikan kolom dalam tabel - Primary key yang ditandai dengan @Id - Getter dan setter untuk
mengakses nilai field

2.2 Membuat Entitas Produk
Mari kita buat entitas Produk terlebih dahulu. Buat file baru bernama Product.java dalam package
entity:

package com. example. ordermanagement. entity ;

import javax. persistence .* ;

@Entity
@Table(name = "products")
public class Product {

@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

@Column(nullable = false )
private String name;

@Column(nullable = false )
private Double price;

@Column(nullable = false )
private Integer stock;

// Constructors

Enterprise Aplikasi Integrasi – API 101 Orders xi
public Product() {}

public Product(String name, Double price, Integer stock) {
this .name = name;
this .price = price;
this .stock = stock;
}

// Getters and Setters
public Long getId() {
return id;
}

public void setId(Long id) {
this .id = id;
}

public String getName() {
return name;
}

public void setName(String name) {
this .name = name;
}

public Double getPrice() {
return price;
}

public void setPrice(Double price) {
this .price = price;
}

public Integer getStock() {
return stock;
}

public void setStock(Integer stock) {
this .stock = stock;
}
}

2.3 Membuat Entitas Pelanggan
Selanjutnya, buat entitas Pelanggan dalam file Customer.java:

package com. example. ordermanagement. entity ;

import javax. persistence .* ;

Enterprise Aplikasi Integrasi – API 101 Orders xii
@Entity
@Table(name = "customers")
public class Customer {

@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

@Column(nullable = false )
private String name;

@Column(nullable = false , unique = true )
private String email;

@Column
private String address;

// Constructors
public Customer() {}

public Customer(String name, String email, String address) {
this .name = name;
this .email = email;
this .address = address;
}

// Getters and Setters
public Long getId() {
return id;
}

public void setId(Long id) {
this .id = id;
}

public String getName() {
return name;
}

public void setName(String name) {
this .name = name;
}

public String getEmail() {
return email;
}

Enterprise Aplikasi Integrasi – API 101 Orders xiii
public void setEmail(String email) {
this .email = email;
}

public String getAddress() {
return address;
}

public void setAddress(String address) {
this .address = address;
}
}

2.4 Membuat Entitas Order
Sekarang kita buat entitas Order dalam file Order.java:

package com. example. ordermanagement. entity ;

import javax. persistence .* ;
import java. time. LocalDateTime ;
import java. util. List ;

@Entity
@Table(name = "orders")
public class Order {

@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

@Column(nullable = false , unique = true )
private String orderNumber;

@ManyToOne
@JoinColumn(name = "customer_id", nullable = false )
private Customer customer;

@Column(nullable = false )
private LocalDateTime orderDate;

@Column(nullable = false )
private String status;

@Column(nullable = false )
private Double totalAmount;

// Constructors

Enterprise Aplikasi Integrasi – API 101 Orders xiv
public Order() {}

public Order(String orderNumber, Customer customer, Double totalAmount) {
this .orderNumber = orderNumber;
this .customer = customer;
this .orderDate = LocalDateTime.now();
this .status = "PENDING";
this .totalAmount = totalAmount;
}

// Getters and Setters
public Long getId() {
return id;
}

public void setId(Long id) {
this .id = id;
}

public String getOrderNumber() {
return orderNumber;
}

public void setOrderNumber(String orderNumber) {
this .orderNumber = orderNumber;
}

public Customer getCustomer() {
return customer;
}

public void setCustomer(Customer customer) {
this .customer = customer;
}

public LocalDateTime getOrderDate() {
return orderDate;
}

public void setOrderDate(LocalDateTime orderDate) {
this .orderDate = orderDate;
}

public String getStatus() {
return status;
}

Enterprise Aplikasi Integrasi – API 101 Orders xv
public void setStatus(String status) {
this .status = status;
}

public Double getTotalAmount() {
return totalAmount;
}

public void setTotalAmount(Double totalAmount) {
this .totalAmount = totalAmount;
}
}

2.5 Membuat Repository
Repository digunakan untuk mengakses data dari database. Buat interface repository untuk setiap
entitas.

Buat file ProductRepository.java dalam package repository:

package com. example. ordermanagement. repository ;

import com. example. ordermanagement. entity. Product ;
import org. springframework. data. jpa. repository. JpaRepository ;
import org. springframework. stereotype. Repository ;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
}

Buat file CustomerRepository.java:

package com. example. ordermanagement. repository ;

import com. example. ordermanagement. entity. Customer ;
import org. springframework. data. jpa. repository. JpaRepository ;
import org. springframework. stereotype. Repository ;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
}

Buat file OrderRepository.java:

package com. example. ordermanagement. repository ;

import com. example. ordermanagement. entity. Order ;
import org. springframework. data. jpa. repository. JpaRepository ;
import org. springframework. stereotype. Repository ;

@Repository

Enterprise Aplikasi Integrasi – API 101 Orders xvi
public interface OrderRepository extends JpaRepository<Order, Long> {
}

2.6 Testing Entitas dan Repository
Untuk menguji entitas dan repository yang telah dibuat, jalankan aplikasi dengan perintah:

./mvnw spring-boot:run

atau untuk Windows:

mvnw.cmd spring-boot:run

Setelah aplikasi berjalan, perhatikan console. Jika berhasil, akan muncul pesan:

Started OrderManagementApplication in X.XXX seconds

Verifikasi Tabel Database:

Buka terminal atau command prompt
Login ke MySQL:
mysql - u root -p
Gunakan database yang telah dibuat:
USE order_management_db;
Lihat tabel-tabel yang telah dibuat:
SHOW TABLES ;
Output yang diharapkan:
+----------------------------------+
| Tables_in_order_management_db |
+----------------------------------+
| customers |
| orders |
| products |
+----------------------------------+
Lihat struktur tabel products:
DESCRIBE products;
Menambahkan Data Testing:

Untuk menambahkan data testing, buat file DataInitializer.java:

package com. example. ordermanagement ;

import com. example. ordermanagement. entity. Customer ;
import com. example. ordermanagement. entity. Product ;
import com. example. ordermanagement. repository. CustomerRepository ;
import com. example. ordermanagement. repository. ProductRepository ;

Enterprise Aplikasi Integrasi – API 101 Orders xvii
import org. springframework. beans. factory. annotation. Autowired ;
import org. springframework. boot. CommandLineRunner ;
import org. springframework. stereotype. Component ;

@Component
public class DataInitializer implements CommandLineRunner {

@Autowired
private CustomerRepository customerRepository;

@Autowired
private ProductRepository productRepository;

@Override
public void run(String ... args) throws Exception {
// Buat customer
Customer customer1 = new Customer("John Doe", "john@example.com", "123 Main St");
customerRepository.save(customer1);

Customer customer2 = new Customer("Jane Smith", "jane@example.com", "456 Oak Ave");
customerRepository.save(customer2);

// Buat products
Product product1 = new Product("Laptop", 8000000.0, 10 );
Product product2 = new Product("Mouse", 150000.0, 50 );
Product product3 = new Product("Keyboard", 500000.0, 30 );

productRepository.save(product1);
productRepository.save(product2);
productRepository.save(product3);

System.out.println("Data initialized successfully!");
System.out.println("Customers: " + customerRepository.count());
System.out.println("Products: " + productRepository.count());
}
}

Data akan otomatis ditambahkan setiap kali aplikasi dijalankan. Untuk menonaktifkan, hapus
annotation @Component atau comment out kode di method run().

C. Ringkasan
Poin-poin penting dari bab ini:

Entity adalah representasi objek dari tabel database
JPA digunakan untuk mapping objek Java ke database
Repository menyediakan interface untuk operasi database
Enterprise Aplikasi Integrasi – API 101 Orders xviii
Relasi antar entitas menggunakan annotation seperti @ManyToOne
MySQL Workbench atau command line MySQL dapat digunakan untuk melihat data
Data initializer memudahkan penambahan data testing
D. Soal Latihan/Praktik
Jelaskan perbedaan antara @Entity dan @Table!
Apa fungsi dari annotation @Id dan @GeneratedValue?
Buatlah entitas Category dengan field: id, name, description!
Jelaskan perbedaan antara @ManyToOne dan @OneToMany!
Buatlah repository untuk entitas Category yang telah dibuat!
Enterprise Aplikasi Integrasi – API 101 Orders xix
CHAPTER 3 — Membuat API Products (Master Data
Pertama)
A. Capaian Pembelajaran (LLO/CPMK)
Setelah mempelajari bab ini mahasiswa mampu:

Membuat API CRUD untuk Products menggunakan Spring Boot
Memisahkan kode ke layer Controller, Service, dan Repository
Menguji endpoint Products menggunakan curl atau Postman
Memahami alur request-response pada API REST
B. Materi Pembelajaran
3.1 Konsep API Master Data
Pada praktik sistem bisnis, data produk biasanya dibangun terlebih dahulu sebelum transaksi order.
Alasannya sederhana: transaksi tidak dapat dibuat jika daftar produk belum tersedia.

Analogi: - Produk seperti daftar menu restoran - Order seperti pesanan pelanggan - Tanpa menu,
pelayan tidak dapat menerima pesanan

3.2 Membuat Entity Product
Buat file src/main/java/com/example/ordermanagement/entity/Product.java:

package com. example. ordermanagement. entity ;

import javax. persistence .* ;

@Entity
@Table(name = "products")
public class Product {

@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

@Column(nullable = false )
private String name;

@Column(nullable = false )
private Double price;

@Column(nullable = false )
private Integer stock;

public Product() {

Enterprise Aplikasi Integrasi – API 101 Orders xx
}
public Product(String name, Double price, Integer stock) {
this .name = name;
this .price = price;
this .stock = stock;
}

public Long getId() {
return id;
}

public void setId(Long id) {
this .id = id;
}

public String getName() {
return name;
}

public void setName(String name) {
this .name = name;
}

public Double getPrice() {
return price;
}

public void setPrice(Double price) {
this .price = price;
}

public Integer getStock() {
return stock;
}

public void setStock(Integer stock) {
this .stock = stock;
}
}

3.3 Membuat Service Product
Buat file src/main/java/com/example/ordermanagement/service/ProductService.java:

package com. example. ordermanagement. service ;

import com. example. ordermanagement. entity. Product ;
import com. example. ordermanagement. repository. ProductRepository ;

Enterprise Aplikasi Integrasi – API 101 Orders xxi
import org. springframework. beans. factory. annotation. Autowired ;
import org. springframework. stereotype. Service ;

import java. util. List ;
import java. util. Optional ;

@Service
public class ProductService {

@Autowired
private ProductRepository productRepository;

public List getAllProducts() {
return productRepository.findAll();
}

public Optional getProductById(Long id) {
return productRepository.findById(id);
}

public Product createProduct(Product product) {
return productRepository.save(product);
}

public Product updateProduct(Long id, Product request) {
return productRepository.findById(id)
.map(product -> {
product.setName(request.getName());
product.setPrice(request.getPrice());
product.setStock(request.getStock());
return productRepository.save(product);
})
.orElse( null );
}

public boolean deleteProduct(Long id) {
if (productRepository.existsById(id)) {
productRepository.deleteById(id);
return true ;
}
return false ;
}
}

3.4 Membuat Controller Product
Buat file src/main/java/com/example/ordermanagement/controller/ProductController.java:

Enterprise Aplikasi Integrasi – API 101 Orders xxii
package com. example. ordermanagement. controller ;

import com. example. ordermanagement. entity. Product ;
import com. example. ordermanagement. service. ProductService ;
import org. springframework. beans. factory. annotation. Autowired ;
import org. springframework. http. HttpStatus ;
import org. springframework. http. ResponseEntity ;
import org. springframework. web. bind. annotation .* ;

import java. util. List ;
import java. util. Optional ;

@RestController
@RequestMapping("/api/products")
public class ProductController {

@Autowired
private ProductService productService;

@GetMapping
public ResponseEntity<List> getAllProducts() {
return ResponseEntity.ok(productService.getAllProducts());
}

@GetMapping("/{id}")
public ResponseEntity getProductById(@PathVariable Long id) {
Optional product = productService.getProductById(id);
return product.map(ResponseEntity::ok)
.orElseGet(() - > ResponseEntity.notFound().build());
}

@PostMapping
public ResponseEntity createProduct(@RequestBody Product product) {
Product created = productService.createProduct(product);
return new ResponseEntity<>(created, HttpStatus.CREATED);
}

@PutMapping("/{id}")
public ResponseEntity updateProduct(@PathVariable Long id, @RequestBody Product req
uest) {
Product updated = productService.updateProduct(id, request);
if (updated == null ) {
return ResponseEntity.notFound().build();
}
return ResponseEntity.ok(updated);
}

Enterprise Aplikasi Integrasi – API 101 Orders xxiii
@DeleteMapping("/{id}")
public ResponseEntity deleteProduct(@PathVariable Long id) {
boolean deleted = productService.deleteProduct(id);
if (!deleted) {
return ResponseEntity.notFound().build();
}
return ResponseEntity.noContent().build();
}
}

3.5 Uji Endpoint Products
Jalankan aplikasi:

./mvnw spring-boot:run

Contoh uji API:

# Create Product
curl - X POST http://localhost:8080/api/products \

H "Content-Type: application/json" \
d '{"name":"Laptop","price":8000000,"stock":10}'
# Get All Products
curl - X GET http://localhost:8080/api/products

# Update Product
curl - X PUT http://localhost:8080/api/products/1 \

H "Content-Type: application/json" \
d '{"name":"Laptop Pro","price":9500000,"stock":8}'
C. Ringkasan
API Products dibangun terlebih dahulu sebagai master data
Struktur utama tetap: Controller, Service, Repository
Endpoint dasar Products: GET, POST, PUT, DELETE
Hasil endpoint dapat diuji cepat menggunakan curl
D. Soal Latihan/Praktik
Jelaskan alasan API Products dibuat sebelum API Orders.
Tambahkan endpoint GET /api/products/search?name=....
Tambahkan field sku pada entity Product, lalu sesuaikan API create dan update.
Uji endpoint delete product untuk id yang tidak ada, lalu jelaskan respons yang diterima.
Buat 5 data produk berbeda dan tampilkan seluruh data menggunakan endpoint list.
Enterprise Aplikasi Integrasi – API 101 Orders xxiv
CHAPTER 4 — Membuat API Categories dan Customers
A. Capaian Pembelajaran (LLO/CPMK)
Setelah mempelajari bab ini mahasiswa mampu:

Membuat API Categories sebagai klasifikasi produk
Membuat API Customers sebagai data pelanggan
Menghubungkan Categories dengan Products
Menguji endpoint Categories dan Customers secara mandiri
B. Materi Pembelajaran
4.1 Konsep Klasifikasi dan Pelanggan
Sistem order yang baik memerlukan: - Kategori produk untuk pengelompokan barang - Data pelanggan
untuk mencatat pemilik transaksi

Analogi: - Kategori seperti rak di supermarket - Produk seperti barang di dalam rak - Customer seperti
pembeli yang mengambil barang

4.2 Membuat Entity Category dan Relasi ke Product
Buat file src/main/java/com/example/ordermanagement/entity/Category.java:

package com. example. ordermanagement. entity ;

import javax. persistence .* ;

@Entity
@Table(name = "categories")
public class Category {

@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

@Column(nullable = false , unique = true )
private String name;

@Column
private String description;

public Category() {
}

public Category(String name, String description) {
this .name = name;
this .description = description;

Enterprise Aplikasi Integrasi – API 101 Orders xxv
}
public Long getId() {
return id;
}

public void setId(Long id) {
this .id = id;
}

public String getName() {
return name;
}

public void setName(String name) {
this .name = name;
}

public String getDescription() {
return description;
}

public void setDescription(String description) {
this .description = description;
}
}

Perbarui file Product.java agar memiliki relasi category:

@ManyToOne
@JoinColumn(name = "category_id")
private Category category;

Tambahkan getter-setter untuk field category.

4.3 Membuat API Categories
Buat file src/main/java/com/example/ordermanagement/controller/CategoryController.java:

package com. example. ordermanagement. controller ;

import com. example. ordermanagement. entity. Category ;
import com. example. ordermanagement. repository. CategoryRepository ;
import org. springframework. beans. factory. annotation. Autowired ;
import org. springframework. http. HttpStatus ;
import org. springframework. http. ResponseEntity ;
import org. springframework. web. bind. annotation .* ;

import java. util. List ;
import java. util. Optional ;

Enterprise Aplikasi Integrasi – API 101 Orders xxvi
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

@Autowired
private CategoryRepository categoryRepository;

@GetMapping
public List getAll() {
return categoryRepository.findAll();
}

@GetMapping("/{id}")
public ResponseEntity getById(@PathVariable Long id) {
Optional category = categoryRepository.findById(id);
return category.map(ResponseEntity::ok)
.orElseGet(() - > ResponseEntity.notFound().build());
}

@PostMapping
public ResponseEntity create(@RequestBody Category request) {
return new ResponseEntity<>(categoryRepository.save(request), HttpStatus.CREATED);
}
}

Buat file src/main/java/com/example/ordermanagement/repository/CategoryRepository.java:

package com. example. ordermanagement. repository ;

import com. example. ordermanagement. entity. Category ;
import org. springframework. data. jpa. repository. JpaRepository ;
import org. springframework. stereotype. Repository ;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
}

4.4 Membuat API Customers
Buat file src/main/java/com/example/ordermanagement/controller/CustomerController.java:

package com. example. ordermanagement. controller ;

import com. example. ordermanagement. entity. Customer ;
import com. example. ordermanagement. repository. CustomerRepository ;
import org. springframework. beans. factory. annotation. Autowired ;
import org. springframework. http. HttpStatus ;
import org. springframework. http. ResponseEntity ;

Enterprise Aplikasi Integrasi – API 101 Orders xxvii
import org. springframework. web. bind. annotation .* ;

import java. util. List ;
import java. util. Optional ;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

@Autowired
private CustomerRepository customerRepository;

@GetMapping
public List getAllCustomers() {
return customerRepository.findAll();
}

@GetMapping("/{id}")
public ResponseEntity getCustomerById(@PathVariable Long id) {
Optional customer = customerRepository.findById(id);
return customer.map(ResponseEntity::ok)
.orElseGet(() - > ResponseEntity.notFound().build());
}

@PostMapping
public ResponseEntity createCustomer(@RequestBody Customer request) {
return new ResponseEntity<>(customerRepository.save(request), HttpStatus.CREATED);
}
}

4.5 Uji Endpoint Categories dan Customers
# Create Category
curl - X POST http://localhost:8080/api/categories \

H "Content-Type: application/json" \
d '{"name":"Elektronik","description":"Perangkat elektronik"}'
# Create Customer
curl - X POST http://localhost:8080/api/customers \

H "Content-Type: application/json" \
d '{"name":"Rani","email":"rani@mail.com","address":"Malang"}'
# List Customers
curl - X GET http://localhost:8080/api/customers

C. Ringkasan
Categories membantu mengelompokkan produk
Enterprise Aplikasi Integrasi – API 101 Orders xxviii
Customers menyimpan identitas pembeli
Data master (Products, Categories, Customers) harus stabil sebelum transaksi
Relasi antar entitas membuat data lebih rapi dan konsisten
D. Soal Latihan/Praktik
Tambahkan endpoint GET /api/categories/{id}/products.
Tambahkan endpoint update untuk customer menggunakan method PUT.
Buat 3 kategori dan 10 produk, lalu kelompokkan setiap produk ke kategori yang sesuai.
Uji respons 404 untuk customer yang tidak ditemukan.
Jelaskan mengapa validasi email pada customer penting untuk proses bisnis.
Enterprise Aplikasi Integrasi – API 101 Orders xxix
CHAPTER 5 — Validasi, Error Handling, dan Best Practice
API Master Data
A. Capaian Pembelajaran (LLO/CPMK)
Setelah mempelajari bab ini mahasiswa mampu:

Menambahkan validasi input untuk Products, Categories, dan Customers
Menangani error dengan format respons yang konsisten
Menggunakan status code HTTP secara tepat
Menyiapkan kualitas API sebelum masuk ke transaksi Order
B. Materi Pembelajaran
5.1 Validasi DTO untuk Products dan Customers
Buat file src/main/java/com/example/ordermanagement/dto/ProductRequest.java:

package com. example. ordermanagement. dto ;

import javax. validation. constraints. Min ;
import javax. validation. constraints. NotBlank ;
import javax. validation. constraints. NotNull ;

public class ProductRequest {

@NotBlank(message = "Nama produk wajib diisi")
private String name;

@NotNull(message = "Harga wajib diisi")
@Min(value = 1 , message = "Harga minimal 1")
private Double price;

@NotNull(message = "Stok wajib diisi")
@Min(value = 0 , message = "Stok tidak boleh negatif")
private Integer stock;

public String getName() {
return name;
}

public void setName(String name) {
this .name = name;
}

public Double getPrice() {

Enterprise Aplikasi Integrasi – API 101 Orders xxx
return price;
}

public void setPrice(Double price) {
this .price = price;
}

public Integer getStock() {
return stock;
}

public void setStock(Integer stock) {
this .stock = stock;
}
}

Buat file src/main/java/com/example/ordermanagement/dto/CustomerRequest.java:

package com. example. ordermanagement. dto ;

import javax. validation. constraints. Email ;
import javax. validation. constraints. NotBlank ;

public class CustomerRequest {

@NotBlank(message = "Nama customer wajib diisi")
private String name;

@Email(message = "Format email tidak valid")
@NotBlank(message = "Email wajib diisi")
private String email;

@NotBlank(message = "Alamat wajib diisi")
private String address;

public String getName() {
return name;
}

public void setName(String name) {
this .name = name;
}

public String getEmail() {
return email;
}

public void setEmail(String email) {

Enterprise Aplikasi Integrasi – API 101 Orders xxxi
this .email = email;
}

public String getAddress() {
return address;
}

public void setAddress(String address) {
this .address = address;
}
}

5.2 Global Exception Handler
Buat file src/main/java/com/example/ordermanagement/exception/GlobalExceptionHandler.java:

package com. example. ordermanagement. exception ;

import org. springframework. http. HttpStatus ;
import org. springframework. http. ResponseEntity ;
import org. springframework. validation. FieldError ;
import org. springframework. web. bind. MethodArgumentNotValidException ;
import org. springframework. web. bind. annotation. ExceptionHandler ;
import org. springframework. web. bind. annotation. RestControllerAdvice ;

import java. util. HashMap ;
import java. util. Map ;

@RestControllerAdvice
public class GlobalExceptionHandler {

@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException e
x) {
Map<String, String> errors = new HashMap<>();
ex.getBindingResult().getAllErrors().forEach(error -> {
String fieldName = ((FieldError) error).getField();
String message = error.getDefaultMessage();
errors.put(fieldName, message);
});
return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
}

@ExceptionHandler(Exception.class)
public ResponseEntity<Map<String, String>> handleGeneral(Exception ex) {
Map<String, String> error = new HashMap<>();
error.put("message", ex.getMessage());
return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);

Enterprise Aplikasi Integrasi – API 101 Orders xxxii
}
}
5.3 Contoh Penggunaan Validasi di Controller
Perbarui method create product pada ProductController.java:

@PostMapping
public ResponseEntity createProduct(@RequestBody @Valid ProductRequest request) {
Product product = new Product();
product.setName(request.getName());
product.setPrice(request.getPrice());
product.setStock(request.getStock());
Product saved = productService.createProduct(product);
return new ResponseEntity<>(saved, HttpStatus.CREATED);
}

5.4 Uji Validasi dan Error
# Harga negatif (harus gagal validasi)
curl - X POST http://localhost:8080/api/products \

H "Content-Type: application/json" \
d '{"name":"Mouse","price":-100,"stock":10}'
# Email tidak valid (harus gagal validasi)
curl - X POST http://localhost:8080/api/customers \

H "Content-Type: application/json" \
d '{"name":"Budi","email":"salah-format","address":"Batu"}'
Respons yang diharapkan (contoh):

{
"price": "Harga minimal 1"
}

C. Ringkasan
Validasi menjaga kualitas data master
Exception handler membuat format error konsisten
API master data yang rapi memudahkan pembuatan transaksi Order
Tahap berikutnya adalah menggabungkan semua data tersebut ke API Orders
D. Soal Latihan/Praktik
Tambahkan validasi @Size(min = 3) untuk nama produk.
Buat format error response yang memuat timestamp dan status.
Tambahkan validasi agar email customer unik.
Uji minimal 5 skenario error dan dokumentasikan hasilnya.
Enterprise Aplikasi Integrasi – API 101 Orders xxxiii

Jelaskan mengapa validasi sebaiknya dilakukan sebelum logika bisnis transaksi.
Enterprise Aplikasi Integrasi – API 101 Orders xxxiv
CHAPTER 6 — API Orders sebagai Integrasi Akhir
A. Capaian Pembelajaran (LLO/CPMK)
Setelah mempelajari bab ini mahasiswa mampu:

Membuat API Order sebagai tahap akhir integrasi sistem
Menggabungkan Products, Categories, dan Customers ke alur transaksi
Membuat endpoint create order, get order, dan update status order
Menguji skenario order dari awal sampai akhir
B. Materi Pembelajaran
6.1 Mengapa Orders Ditempatkan di Akhir?
Order adalah transaksi inti yang bergantung pada data master: - Product harus sudah tersedia -
Customer harus sudah terdaftar - Stock produk harus sudah tercatat

Karena itu, pembangunan API Order ditempatkan di bagian akhir agar alurnya logis dan mudah
dipahami pemula.

6.2 Membuat Entity Order dan OrderItem
Buat file src/main/java/com/example/ordermanagement/entity/Order.java:

package com. example. ordermanagement. entity ;

import javax. persistence .* ;
import java. time. LocalDateTime ;
import java. util. ArrayList ;
import java. util. List ;

@Entity
@Table(name = "orders")
public class Order {

@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

@Column(nullable = false , unique = true )
private String orderNumber;

@ManyToOne
@JoinColumn(name = "customer_id", nullable = false )
private Customer customer;

@Column(nullable = false )
private String status;

Enterprise Aplikasi Integrasi – API 101 Orders xxxv
@Column(nullable = false )
private Double totalAmount;

@Column(nullable = false )
private LocalDateTime createdAt;

@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true )
private List items = new ArrayList<>();

public Order() {
}

public void addItem(OrderItem item) {
items.add(item);
item.setOrder( this );
}

public Long getId() {
return id;
}

public void setId(Long id) {
this .id = id;
}

public String getOrderNumber() {
return orderNumber;
}

public void setOrderNumber(String orderNumber) {
this .orderNumber = orderNumber;
}

public Customer getCustomer() {
return customer;
}

public void setCustomer(Customer customer) {
this .customer = customer;
}

public String getStatus() {
return status;
}

public void setStatus(String status) {

Enterprise Aplikasi Integrasi – API 101 Orders xxxvi
this .status = status;
}

public Double getTotalAmount() {
return totalAmount;
}

public void setTotalAmount(Double totalAmount) {
this .totalAmount = totalAmount;
}

public LocalDateTime getCreatedAt() {
return createdAt;
}

public void setCreatedAt(LocalDateTime createdAt) {
this .createdAt = createdAt;
}

public List getItems() {
return items;
}

public void setItems(List items) {
this .items = items;
}
}

Buat file src/main/java/com/example/ordermanagement/entity/OrderItem.java:

package com. example. ordermanagement. entity ;

import javax. persistence .* ;

@Entity
@Table(name = "order_items")
public class OrderItem {

@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

@ManyToOne
@JoinColumn(name = "order_id", nullable = false )
private Order order;

@ManyToOne
@JoinColumn(name = "product_id", nullable = false )

Enterprise Aplikasi Integrasi – API 101 Orders xxxvii
private Product product;

@Column(nullable = false )
private Integer quantity;

@Column(nullable = false )
private Double price;

@Column(nullable = false )
private Double subtotal;

public Long getId() {
return id;
}

public void setId(Long id) {
this .id = id;
}

public Order getOrder() {
return order;
}

public void setOrder(Order order) {
this .order = order;
}

public Product getProduct() {
return product;
}

public void setProduct(Product product) {
this .product = product;
}

public Integer getQuantity() {
return quantity;
}

public void setQuantity(Integer quantity) {
this .quantity = quantity;
}

public Double getPrice() {
return price;
}

Enterprise Aplikasi Integrasi – API 101 Orders xxxviii
public void setPrice(Double price) {
this .price = price;
}

public Double getSubtotal() {
return subtotal;
}

public void setSubtotal(Double subtotal) {
this .subtotal = subtotal;
}
}

6.3 Membuat Service Order (Transaksi)
Buat file src/main/java/com/example/ordermanagement/service/OrderService.java:

package com. example. ordermanagement. service ;

import com. example. ordermanagement. entity. Customer ;
import com. example. ordermanagement. entity. Order ;
import com. example. ordermanagement. entity. OrderItem ;
import com. example. ordermanagement. entity. Product ;
import com. example. ordermanagement. repository. CustomerRepository ;
import com. example. ordermanagement. repository. OrderRepository ;
import com. example. ordermanagement. repository. ProductRepository ;
import org. springframework. beans. factory. annotation. Autowired ;
import org. springframework. stereotype. Service ;
import org. springframework. transaction. annotation. Transactional ;

import java. time. LocalDateTime ;
import java. util. List ;

@Service
public class OrderService {

@Autowired
private OrderRepository orderRepository;

@Autowired
private CustomerRepository customerRepository;

@Autowired
private ProductRepository productRepository;

@Transactional
public Order createOrder(Long customerId, List itemRequests) {
Customer customer = customerRepository.findById(customerId)
.orElseThrow(() - > new RuntimeException("Customer tidak ditemukan"));

Enterprise Aplikasi Integrasi – API 101 Orders xxxix
Order order = new Order();
order.setCustomer(customer);
order.setOrderNumber("ORD-" + System.currentTimeMillis());
order.setStatus("PENDING");
order.setCreatedAt(LocalDateTime.now());

double total = 0.0;

for (ItemRequest req : itemRequests) {
Product product = productRepository.findById(req.getProductId())
.orElseThrow(() - > new RuntimeException("Product tidak ditemukan"));

if (product.getStock() < req.getQuantity()) {
throw new RuntimeException("Stok produk tidak cukup: " + product.getName());
}

product.setStock(product.getStock() - req.getQuantity());
productRepository.save(product);

OrderItem item = new OrderItem();
item.setProduct(product);
item.setQuantity(req.getQuantity());
item.setPrice(product.getPrice());
item.setSubtotal(product.getPrice() * req.getQuantity());
order.addItem(item);

total += item.getSubtotal();
}

order.setTotalAmount(total);
return orderRepository.save(order);
}

public List getAllOrders() {
return orderRepository.findAll();
}

public Order updateStatus(Long orderId, String status) {
return orderRepository.findById(orderId)
.map(order -> {
order.setStatus(status);
return orderRepository.save(order);
})
.orElse( null );
}

Enterprise Aplikasi Integrasi – API 101 Orders xl
public static class ItemRequest {
private Long productId;
private Integer quantity;

public Long getProductId() {
return productId;
}

public void setProductId(Long productId) {
this .productId = productId;
}

public Integer getQuantity() {
return quantity;
}

public void setQuantity(Integer quantity) {
this .quantity = quantity;
}
}
}

6.4 Membuat Controller Order
Buat file src/main/java/com/example/ordermanagement/controller/OrderController.java:

package com. example. ordermanagement. controller ;

import com. example. ordermanagement. entity. Order ;
import com. example. ordermanagement. service. OrderService ;
import org. springframework. beans. factory. annotation. Autowired ;
import org. springframework. http. HttpStatus ;
import org. springframework. http. ResponseEntity ;
import org. springframework. web. bind. annotation .* ;

import java. util. List ;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

@Autowired
private OrderService orderService;

@PostMapping
public ResponseEntity createOrder(@RequestParam Long customerId,
@RequestBody List<OrderService.ItemRequest> items) {
Order created = orderService.createOrder(customerId, items);
return new ResponseEntity<>(created, HttpStatus.CREATED);

Enterprise Aplikasi Integrasi – API 101 Orders xli
}
@GetMapping
public ResponseEntity<List> getAllOrders() {
return ResponseEntity.ok(orderService.getAllOrders());
}

@PutMapping("/{id}/status")
public ResponseEntity updateStatus(@PathVariable Long id,
@RequestParam String status) {
Order updated = orderService.updateStatus(id, status);
if (updated == null ) {
return ResponseEntity.notFound().build();
}
return ResponseEntity.ok(updated);
}
}

6.5 Uji Skenario End-to-End Order
# 1) Pastikan customer dan product sudah ada
curl - X GET http://localhost:8080/api/customers
curl - X GET http://localhost:8080/api/products

# 2) Create Order
curl - X POST "http://localhost:8080/api/orders?customerId=1" \

H "Content-Type: application/json" \
d '[{"productId":1,"quantity":2},{"productId":2,"quantity":1}]'
# 3) Lihat semua order
curl - X GET http://localhost:8080/api/orders

# 4) Update status order
curl - X PUT "http://localhost:8080/api/orders/1/status?status=SHIPPED"

Contoh output ringkas saat create order berhasil:

{
"id": 1 ,
"orderNumber": "ORD-1713012345678",
"status": "PENDING",
"totalAmount": 16150000.0
}

C. Ringkasan
Orders ditempatkan di bab terakhir sebagai integrasi akhir
API Order menggunakan data master dari Products, Categories, dan Customers
Enterprise Aplikasi Integrasi – API 101 Orders xlii
Saat order dibuat, stok produk dikurangi otomatis
Endpoint utama order: create, list, update status
D. Soal Latihan/Praktik
Tambahkan endpoint GET /api/orders/{id}.
Tambahkan endpoint cancel order POST /api/orders/{id}/cancel.
Buat aturan: order tidak boleh diubah ke SHIPPED jika status masih PENDING.
Uji skenario stok habis, lalu dokumentasikan pesan error yang dihasilkan.
Buat mini laporan alur lengkap dari create category sampai create order.
Enterprise Aplikasi Integrasi – API 101 Orders xliii
REFERENCES
Gunakan format APA.

Baeldung. (2023). Spring Boot REST API Tutorial. Retrieved from https://www.baeldung.com/rest-with-
spring-series

Spring Framework. (2023). Spring Boot Documentation. Retrieved from
https://spring.io/projects/spring-boot

Richardson, L., & Ruby, S. (2008). RESTful Web Services. O’Reilly Media.

Fielding, R. T. (2000). Architectural Styles and the Design of Network-based Software Architectures.
Doctoral dissertation, University of California, Irvine.

Microsoft. (2023). API Design Guidelines. Retrieved from https://github.com/microsoft/api-guidelines

This is a offline tool, your data stays locally and is not send to any server!
Feedback & Bug Reports