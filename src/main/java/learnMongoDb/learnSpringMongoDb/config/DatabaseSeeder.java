package learnMongoDb.learnSpringMongoDb.config;

import learnMongoDb.learnSpringMongoDb.entity.Address;
import learnMongoDb.learnSpringMongoDb.entity.Order;
import learnMongoDb.learnSpringMongoDb.entity.OrderItem;
import learnMongoDb.learnSpringMongoDb.entity.Product;
import learnMongoDb.learnSpringMongoDb.repository.OrderRepository;
import learnMongoDb.learnSpringMongoDb.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DatabaseSeeder — runs once on startup if the products collection is empty.
 *
 * Seeded orders now contain full OrderItem snapshots so the frontend
 * can render the Orders page and Order Detail page immediately without
 * any product lookups.
 */
@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final OrderRepository   orderRepository;

    @Override
    public void run(String... args) {

        System.out.println("DATABASE SEEDER EXECUTED");

        // ── Products ─────────────────────────────────────────────────────────
        if (productRepository.count() > 0) {
            System.out.println("Products already exist. Skipping product seed.");
        } else {
            seedProducts();
        }

        // ── Orders ───────────────────────────────────────────────────────────
        // Re-seed orders every time so that they always carry proper snapshots
        // (safe because orders reference no external documents).
        if (orderRepository.count() == 0) {
            seedOrders();
        } else {
            System.out.println("Orders already exist. Skipping order seed.");
        }
    }

    // =========================================================================
    // Products
    // =========================================================================

    private void seedProducts() {
        List<Product> products = List.of(

                // ── ELECTRONICS – Mobile ──────────────────────────────────────
                Product.builder()
                        .name("Samsung Galaxy S25 Ultra")
                        .category("Electronics").subcategory("Mobile").brand("Samsung")
                        .price(99999.0).stock(50)
                        .description("Latest flagship with built-in S Pen, 200MP camera, and Snapdragon 8 Elite.")
                        .imageUrl("https://m.media-amazon.com/images/I/71tz3adVWaL.jpg")
                        .build(),

                Product.builder()
                        .name("iPhone 17 Pro Max 256GB")
                        .category("Electronics").subcategory("Mobile").brand("Apple")
                        .price(134900.0).stock(35)
                        .description("Apple Intelligence, A18 Pro chip, 5x optical zoom, titanium design.")
                        .imageUrl("https://cdn.jiostore.online/v2/jmd-asp/jdprod/wrkr/products/pictures/item/free/original/apple/494741644/0/zp5lPcLphV-w2UMH0w2JC-AppleiPhone17ProMax-MP-494741644-i-1-1200Wx1200H.jpeg")
                        .build(),

                Product.builder()
                        .name("OnePlus 15 256GB/16GB")
                        .category("Electronics").subcategory("Mobile").brand("OnePlus")
                        .price(69999.0).stock(60)
                        .description("Snapdragon 8 Elite, Hasselblad cameras, 100W SUPERVOOC charging.")
                        .imageUrl("https://m.media-amazon.com/images/I/61KXgizurpL._AC_UF1000,1000_QL80_.jpg")
                        .build(),

                Product.builder()
                        .name("Moto Edge 60 Pro 256GB/6GB")
                        .category("Electronics").subcategory("Mobile").brand("Motorola")
                        .price(29999.0).stock(80)
                        .description("Latest Moto flagship")
                        .imageUrl("https://rukminim2.flixcart.com/image/480/640/xif0q/mobile/u/b/t/-original-imahgqnzzc6cgggb.jpeg?q=90")
                        .build(),

                // ── ELECTRONICS – Laptop ──────────────────────────────────────
                Product.builder()
                        .name("Apple MacBook Air M3 15\" 8GB/256GB")
                        .category("Electronics").subcategory("Laptop").brand("Apple")
                        .price(119900.0).stock(25)
                        .description("Fanless design, 18-hour battery, M5 chip, Liquid Retina display.")
                        .imageUrl("https://store.storeimages.cdn-apple.com/1/as-images.apple.com/is/mba13-midnight-select-202503?wid=904&hei=840&fmt=jpeg&qlt=90&.v=1741885366392")
                        .build(),

                Product.builder()
                        .name("Dell XPS 15 Core i9 RTX 4060")
                        .category("Electronics").subcategory("Laptop").brand("Dell")
                        .price(169990.0).stock(15)
                        .description("4K OLED display, 32GB RAM, 1TB SSD, premium build quality.")
                        .imageUrl("https://i.dell.com/is/image/DellContent/content/dam/ss2/product-images/dell-client-products/notebooks/xps-notebooks/xps-15-9530/media-gallery/touch-black/notebook-xps-15-9530-t-black-gallery-1.psd?fmt=pjpg&pscan=auto&scl=1&wid=3778&hei=2323&qlt=100,1&resMode=sharp2&size=3778,2323&chrss=full&imwidth=5000")
                        .build(),

                Product.builder()
                        .name("HP Pavilion 15 Ryzen 5 8GB/512GB")
                        .category("Electronics").subcategory("Laptop").brand("HP")
                        .price(52990.0).stock(40)
                        .description("AMD Ryzen 5 7530U, Full HD IPS display, Windows 11 Home.")
                        .imageUrl("https://ehpworld.com/wp-content/uploads/2021/10/HP-Pavilion-Gaming-15-EC2008AX-Front.jpg")
                        .build(),

                // ── ELECTRONICS – Headphones ──────────────────────────────────
                Product.builder()
                        .name("Sony WH-1000XM5 Wireless Headphones")
                        .category("Electronics").subcategory("Headphones").brand("Sony")
                        .price(29990.0).stock(80)
                        .description("Industry-leading noise cancellation, 30-hour battery, multipoint connect.")
                        .imageUrl("https://www.sony.co.in/image/94101fcc4f07476f823d060b0a188f23?fmt=png-alpha&wid=1200")
                        .build(),

                Product.builder()
                        .name("Boat Rockerz 450 Pro Wireless")
                        .category("Electronics").subcategory("Headphones").brand("Boat")
                        .price(1299.0).stock(200)
                        .description("40-hour playback, ASAP charge, ENx mic, signature sound.")
                        .imageUrl("https://rukminim2.flixcart.com/image/1536/1536/kmccosw0/headphone/9/h/j/rockerz-450-pro-boat-original-imagf9gyd4u6w85z.jpeg?q=90")
                        .build(),

                // ── ELECTRONICS – TV ──────────────────────────────────────────
                Product.builder()
                        .name("LG 55\" 4K OLED Smart TV C3")
                        .category("Electronics").subcategory("TV").brand("LG")
                        .price(139990.0).stock(12)
                        .description("OLED evo panel, 120Hz, Dolby Vision & Atmos, webOS 23.")
                        .imageUrl("https://rukminim2.flixcart.com/image/1536/1536/xif0q/television/l/f/k/-original-imahgt5gx9bfryc7.jpeg?q=90")
                        .build(),

                Product.builder()
                        .name("Mi 43\" 4K UHD Android TV")
                        .category("Electronics").subcategory("TV").brand("Xiaomi")
                        .price(27999.0).stock(30)
                        .description("Android TV, 4K HDR, Dolby Audio, built-in Chromecast.")
                        .imageUrl("https://m.media-amazon.com/images/I/61bSsGflv5L.jpg")
                        .build(),

                // ── CLOTHING ──────────────────────────────────────────────────
                Product.builder()
                        .name("Nike Air Max 270")
                        .category("Clothing").subcategory("Shoes").brand("Nike")
                        .price(12995.0).stock(100)
                        .description("Max Air unit in the heel, breathable mesh upper.")
                        .imageUrl("https://static.nike.com/a/images/t_PDP_1728_v1/f_auto,q_auto:eco/skwgyqrbfzhu6wnf7gwg/air-max-270-shoes-2V5C4p.png")
                        .build(),

                Product.builder()
                        .name("Levi's 511 Slim Fit Jeans")
                        .category("Clothing").subcategory("Jeans").brand("Levi's")
                        .price(3499.0).stock(150)
                        .description("Slim through the hip and thigh, classic 5-pocket styling.")
                        .imageUrl("https://lsco.scene7.com/is/image/lscojo/045110193-front-pdp?fmt=jpeg&qlt=70&resMode=bisharp&fit=crop,1&op_usm=0.6,0.6,8&wid=2000&hei=2000")
                        .build(),

                // ── BOOKS ─────────────────────────────────────────────────────
                Product.builder()
                        .name("Atomic Habits — James Clear")
                        .category("Books").subcategory("Self-Help").brand("Penguin")
                        .price(399.0).stock(500)
                        .description("Tiny changes, remarkable results. #1 NYT bestseller.")
                        .imageUrl("https://m.media-amazon.com/images/I/81wgcld4wxL.jpg")
                        .build(),

                Product.builder()
                        .name("The Pragmatic Programmer")
                        .category("Books").subcategory("Technology").brand("Addison-Wesley")
                        .price(2999.0).stock(80)
                        .description("Your journey to mastery — 20th anniversary edition.")
                        .imageUrl("https://m.media-amazon.com/images/I/71f743sOPoL.jpg")
                        .build(),

                // ── HOME & KITCHEN ────────────────────────────────────────────
                Product.builder()
                        .name("Instant Pot Duo 7-in-1")
                        .category("Home & Kitchen").subcategory("Appliances").brand("Instant Pot")
                        .price(8999.0).stock(60)
                        .description("Pressure cooker, slow cooker, rice cooker, steamer & more.")
                        .imageUrl("https://m.media-amazon.com/images/I/71WtwEvYDOS.jpg")
                        .build()
        );

        productRepository.saveAll(products);
        System.out.println("Seeded " + products.size() + " products.");
    }

    // =========================================================================
    // Orders  — seeded with full product snapshots
    // =========================================================================

    private void seedOrders() {

        // ── Shared address ────────────────────────────────────────────────────
        Address kolkata = Address.builder()
                .line1("704, Tagore Nagar")
                .city("Kolkata")
                .state("West Bengal")
                .zipCode("700007")
                .country("India")
                .build();

        // ── Order 1 — two-item order (iPhone + MacBook) ───────────────────────
        OrderItem iphoneSnapshot = OrderItem.builder()
                .productId("seed-prod-iphone17")
                .productName("iPhone 17 Pro Max 256GB")
                .productImage("https://cdn.jiostore.online/v2/jmd-asp/jdprod/wrkr/products/pictures/item/free/original/apple/494741644/0/zp5lPcLphV-w2UMH0w2JC-AppleiPhone17ProMax-MP-494741644-i-1-1200Wx1200H.jpeg")
                .price(134900.0)
                .quantity(1)
                .totalPrice(134900.0)
                .build();

        Order order1 = Order.builder()
                .userId("seed-user-aditya")
                .items(List.of(iphoneSnapshot))
                .quantity(1)
                .totalPrice(134900.0)
                .status("DELIVERED")
                .address(kolkata)
                .createdAt(LocalDateTime.now().minusDays(30))
                .build();

        // ── Order 2 — Samsung Galaxy (cancelled) ──────────────────────────────
        OrderItem galaxySnapshot = OrderItem.builder()
                .productId("seed-prod-galaxy-s25")
                .productName("Samsung Galaxy S25 Ultra")
                .productImage("https://m.media-amazon.com/images/I/71tz3adVWaL.jpg")
                .price(99999.0)
                .quantity(1)
                .totalPrice(99999.0)
                .build();

        Order order2 = Order.builder()
                .userId("seed-user-aditya")
                .items(List.of(galaxySnapshot))
                .quantity(1)
                .totalPrice(99999.0)
                .status("CANCELLED")
                .address(kolkata)
                .createdAt(LocalDateTime.now().minusDays(15))
                .build();

        // ── Order 3 — OnePlus 15 (pending) ───────────────────────────────────
        OrderItem oneplusSnapshot = OrderItem.builder()
                .productId("seed-prod-oneplus15")
                .productName("OnePlus 15 256GB/16GB")
                .productImage("https://m.media-amazon.com/images/I/61KXgizurpL._AC_UF1000,1000_QL80_.jpg")
                .price(69999.0)
                .quantity(1)
                .totalPrice(69999.0)
                .build();

        Order order3 = Order.builder()
                .userId("seed-user-aditya")
                .items(List.of(oneplusSnapshot))
                .quantity(1)
                .totalPrice(69999.0)
                .status("SHIPPED")
                .address(kolkata)
                .createdAt(LocalDateTime.now().minusDays(5))
                .build();

        // ── Order 4 — multi-item (Dell XPS + Sony Headphones) ─────────────────
        OrderItem dellSnapshot = OrderItem.builder()
                .productId("seed-prod-dell-xps")
                .productName("Dell XPS 15 Core i9 RTX 4060")
                .productImage("https://i.dell.com/is/image/DellContent/content/dam/ss2/product-images/dell-client-products/notebooks/xps-notebooks/xps-15-9530/media-gallery/touch-black/notebook-xps-15-9530-t-black-gallery-1.psd?fmt=pjpg&pscan=auto&scl=1&wid=3778&hei=2323&qlt=100,1&resMode=sharp2&size=3778,2323&chrss=full&imwidth=5000")
                .price(169990.0)
                .quantity(1)
                .totalPrice(169990.0)
                .build();

        OrderItem sonySnapshot = OrderItem.builder()
                .productId("seed-prod-sony-wh1000xm5")
                .productName("Sony WH-1000XM5 Wireless Headphones")
                .productImage("https://www.sony.co.in/image/94101fcc4f07476f823d060b0a188f23?fmt=png-alpha&wid=1200")
                .price(29990.0)
                .quantity(2)
                .totalPrice(59980.0)
                .build();

        Order order4 = Order.builder()
                .userId("seed-user-aditya")
                .items(List.of(dellSnapshot, sonySnapshot))
                .quantity(3)
                .totalPrice(229970.0)
                .status("CONFIRMED")
                .address(kolkata)
                .createdAt(LocalDateTime.now().minusDays(2))
                .build();

        orderRepository.saveAll(List.of(order1, order2, order3, order4));
        System.out.println("Seeded 4 orders with full product snapshots.");
    }
}
