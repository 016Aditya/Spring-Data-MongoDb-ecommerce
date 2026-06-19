package learnMongoDb.learnSpringMongoDb.config;

import learnMongoDb.learnSpringMongoDb.entity.Product;
import learnMongoDb.learnSpringMongoDb.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final ProductRepository productRepository;

    @Override
    public void run(String... args) {

        System.out.println("DATABASE SEEDER EXECUTED");

        if (productRepository.count() > 0) {
            System.out.println("Products already exist. Skipping seed.");
            return;
        }

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
                        .description("4K Ultra HD, Android 11, far-field voice control, Dolby Audio.")
                        .imageUrl("https://m.media-amazon.com/images/I/612oGaxp3DS._AC_UF1000,1000_QL80_.jpg")
                        .build(),

                Product.builder()
                        .name("Sony BRAVIA 2 II 75 inch Ultra HD (4K) LED TV")
                        .category("Electronics").subcategory("TV").brand("SONY")
                        .price(96000.0).stock(31)
                        .description("Sony BRAVIA 2 II 189.3 cm (75 inch) Ultra HD (4K) LED Smart Google TV.")
                        .imageUrl("https://rukminim2.flixcart.com/image/1536/1536/xif0q/television/p/g/5/-original-imahggetxjeguqga.jpeg?q=90")
                        .build(),

                // ── ELECTRONICS – Camera ──────────────────────────────────
                Product.builder()
                        .name("Canon EOS R50 RF-S18-45mm STM Mirrorless Camera (Black)- 24.2 MP")
                        .category("Electronics").subcategory("Camera").brand("Canon")
                        .price(62000.0).stock(31)
                        .description("Canon EOS R50 RF-S18-45mm f/4.5-6.3 is STM Mirrorless Camera (Black)- 4K Video Vlogging with 24.2 MP.")
                        .imageUrl("https://m.media-amazon.com/images/I/81LskAU5h1L._SL1500_.jpg")
                        .build(),

                Product.builder()
                        .name("SONY Alpha ILCE-6600M APS-C Mirrorless Camera with 18-135 mm Zoom Lens (Black)- 24.2 MP")
                        .category("Electronics").subcategory("Camera").brand("SONY")
                        .price(89000.0).stock(31)
                        .description("Canon EOS R50 RF-S18-45mm f/4.5-6.3 is STM Mirrorless Camera (Black)- 4K Video Vlogging with 24.2 MP. Shoot what you like with the α6600 from Sony. This camera features the Optical Stabilisation feature to provide you with shake-free images, AI-based Subject Tracking to help you track the face and eyes of the subject, High-capacity Z Battery to make sure that you can shoot for long hours without any hassle.")
                        .imageUrl("https://rukminim2.flixcart.com/image/1536/1536/k3q76a80/camera/k/7/9/sony-apsc-ilce-6600m-b-in5-mirrorless-original-imafm6nvxhybpwhs.jpeg?q=90")
                        .build(),

                // ── CLOTHING – Shirt ──────────────────────────────────────────
                Product.builder()
                        .name("Allen Solly Slim Fit Formal Shirt")
                        .category("Clothing").subcategory("Shirt").brand("Allen Solly")
                        .price(1299.0).stock(120)
                        .description("100% cotton, wrinkle-resistant, available in 6 colors.")
                        .imageUrl("https://assets.myntassets.com/w_412,q_50,,dpr_3,fl_progressive,f_webp/assets/images/32882342/2025/5/26/b28fa000-db38-4fa9-aecf-60020806a29e1748281394965-Allen-Solly-Men-Slim-Fit-Opaque-Printed-Formal-Shirt-5781748-1.jpg")
                        .build(),

                Product.builder()
                        .name("Van Heusen Checked Casual Shirt")
                        .category("Clothing").subcategory("Shirt").brand("Van Heusen")
                        .price(999.0).stock(150)
                        .description("Regular fit, pure cotton, machine washable.")
                        .imageUrl("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTRYfBel3Z2FMy_P2XZxpCwfgBMgWX1HNuWzA&s")
                        .build(),

                // ── CLOTHING – Jeans ──────────────────────────────────────────
                Product.builder()
                        .name("Levi's 511 Slim Fit Jeans")
                        .category("Clothing").subcategory("Jeans").brand("Levis")
                        .price(2999.0).stock(80)
                        .description("Slim through hip and thigh, stretch denim, classic 5-pocket style.")
                        .imageUrl("https://levi.in/cdn/shop/files/182981742_03_Front.jpg?v=1767769923")
                        .build(),

                Product.builder()
                        .name("Pepe Jeans Straight Fit Men's Jeans")
                        .category("Clothing").subcategory("Jeans").brand("Pepe Jeans")
                        .price(1799.0).stock(100)
                        .description("Straight fit, mid-rise, 98% cotton, zip fly.")
                        .imageUrl("https://d1pdzcnm6xgxlz.cloudfront.net/bottoms/8905875444640-9.jpg")
                        .build(),

                // ── CLOTHING – Shoes ──────────────────────────────────────────
                Product.builder()
                        .name("Nike Air Max 270 React")
                        .category("Clothing").subcategory("Shoes").brand("Nike")
                        .price(9995.0).stock(60)
                        .description("Max Air unit, React foam, all-day comfort and bold style.")
                        .imageUrl("https://cdn-images.farfetch-contents.com/15/63/99/84/15639984_28536210_1000.jpg")
                        .build(),

                Product.builder()
                        .name("Adidas Ultraboost 22 Running Shoes")
                        .category("Clothing").subcategory("Shoes").brand("Adidas")
                        .price(12999.0).stock(45)
                        .description("BOOST midsole, Primeknit+ upper, Continental rubber outsole.")
                        .imageUrl("https://5.imimg.com/data5/SELLER/Default/2024/9/450681034/RT/GS/WK/188692646/adidas-ultraboost-22-black-sky-rush-turbo.jpeg")
                        .build(),

                Product.builder()
                        .name("McLAREN RACING Speedcat Sneakers")
                        .category("Clothing").subcategory("Shoes").brand("Adidas")
                        .price(10999.0).stock(45)
                        .description("Motorsport isn't just a vibe \u2013 it's an energy. These lifestyle.")
                        .imageUrl("https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fafafa,w_750,h_750/global/309452/01/fnd/IND/fmt/png/McLAREN-RACING-Speedcat-Sneakers")
                        .build(),

                Product.builder()
                        .name("Air Jordan 1 Mid SE")
                        .category("Clothing").subcategory("Shoes").brand("Adidas")
                        .price(12295.0).stock(47)
                        .description("Inspired by the original AJ1, this mid-top edition maintains the iconic look you love while choice colours and crisp leather give it a distinct identity..")
                        .imageUrl("https://adn-static1.nykaa.com/nykdesignstudio-images/pub/media/catalog/product/c/e/ce80759Nike-II3789-001_1.jpg?rnd=20200526195200&tr=w-1080")
                        .build(),

                // ── CLOTHING – Dress ──────────────────────────────────────────
                Product.builder()
                        .name("W Women's Polka Dress")
                        .category("Clothing").subcategory("Dress").brand("W")
                        .price(1599.0).stock(70)
                        .description("Polka DOT print, round-neck, mid length, viscose fabric.")
                        .imageUrl("https://image.made-in-china.com/202f0j00NOUWTmBIwdov/Ladies-Summer-Sleeveless-Dress-Clothes-Women-Casual-Fashion-Polka-DOT-Dresses.webp")
                        .build(),

                Product.builder()
                        .name("W Women's Floral Wrap Dress")
                        .category("Clothing").subcategory("Dress").brand("W")
                        .price(1899.0).stock(70)
                        .description("Floral print, V-neck, midi length, viscose fabric.")
                        .imageUrl("https://assets.ajio.com/medias/sys_master/root/20240705/Fwga/6687433d1d763220faeb534f/-473Wx593H-700163809-peach-MODEL.jpg")
                        .build(),

                // ── CLOTHING – Jacket ─────────────────────────────────────────
                Product.builder()
                        .name("The North Face Thermoball Eco Jacket")
                        .category("Clothing").subcategory("Jacket").brand("The North Face")
                        .price(14999.0).stock(30)
                        .description("Water-repellent, ThermoBall insulation, packs into its own pocket.")
                        .imageUrl("https://assets.thenorthface.com/images/t_Thumbnail/v1762369809/NF0A8D1PBRI-HERO/Mens-THERMOBALL-Jacket-TNF-HERO.png")
                        .build(),

                // ── CLOTHING – Kurta ──────────────────────────────────────────
                Product.builder()
                        .name("Manyavar Men's Silk Kurta Set")
                        .category("Clothing").subcategory("Kurta").brand("Manyavar")
                        .price(3499.0).stock(55)
                        .description("Art silk, Mustard Yellow Silk Kurta Set For Men With Floral Resham Work.")
                        .imageUrl("https://ik.imagekit.io/4sjmoqtje/tr:w-1000,c-at_max/cdn/shop/files/mustard-yellow-silk-kurta-set-for-men-with-floral-resham-work-sg356826-1.jpg?v=1759832614")
                        .build(),

                // ── BOOKS – Novel ─────────────────────────────────────────────
                Product.builder()
                        .name("Atomic Habits by James Clear")
                        .category("Books").subcategory("Novel").brand("Penguin")
                        .price(399.0).stock(300)
                        .description("The proven system to build good habits and break bad ones.")
                        .imageUrl("https://i0.wp.com/freedom.to/blog/wp-content/uploads/2018/10/Atomic_Habits-2.png?fit=1526%2C1508&ssl=1")
                        .build(),

                Product.builder()
                        .name("The Alchemist by Paulo Coelho")
                        .category("Books").subcategory("Novel").brand("HarperCollins")
                        .price(299.0).stock(250)
                        .description("A magical story about following your dream and listening to your heart.")
                        .imageUrl("https://m.media-amazon.com/images/I/617lxveUjYL.jpg")
                        .build(),

                Product.builder()
                        .name("Rich Dad Poor Dad by Robert Kiyosaki")
                        .category("Books").subcategory("Novel").brand("Warner Books")
                        .price(349.0).stock(200)
                        .description("What the rich teach their kids about money that the poor and middle class do not.")
                        .imageUrl("https://m.media-amazon.com/images/I/71HJj3XmheL._AC_UF1000,1000_QL80_.jpg")
                        .build(),

                // ── BOOKS – Textbook ──────────────────────────────────────────
                Product.builder()
                        .name("Introduction to Algorithms (CLRS)")
                        .category("Books").subcategory("Textbook").brand("MIT Press")
                        .price(2499.0).stock(80)
                        .description("The bible of algorithms \u2014 comprehensive coverage of data structures and algorithms.")
                        .imageUrl("https://covers.openlibrary.org/b/isbn/9780262033848-L.jpg")
                        .build(),

                Product.builder()
                        .name("Java: The Complete Reference 12th Edition")
                        .category("Books").subcategory("Textbook").brand("McGraw Hill")
                        .price(899.0).stock(120)
                        .description("Complete coverage of Java 17+, ideal for beginners and professionals.")
                        .imageUrl("https://covers.openlibrary.org/b/isbn/9781260440232-L.jpg")
                        .build(),

                Product.builder()
                        .name("DUNE by Frank Herbert")
                        .category("Books").subcategory("Textbook").brand("ACE")
                        .price(799.0).stock(120)
                        .description("Frank Herbert's classic masterpiece\u2014a triumph of the imagination and one of the bestselling science fiction novels of all time.")
                        .imageUrl("https://m.media-amazon.com/images/S/compressed.photo.goodreads.com/books/1555447414i/44767458.jpg")
                        .build(),

                Product.builder()
                        .name("DUNE Messiah by Frank Herbert")
                        .category("Books").subcategory("Textbook").brand("ACE")
                        .price(799.0).stock(120)
                        .description("Frank Herbert's classic masterpiece\u2014a triumph of the imagination and one of the bestselling science fiction novels of all time.")
                        .imageUrl("https://m.media-amazon.com/images/I/91NXYT-nAKL._UF1000,1000_QL80_.jpg")
                        .build(),

                // ── BOOKS – Stationery ─────────────────────────────────────────
                Product.builder()
                        .name("Classmate Premium Notebook Pack of 6")
                        .category("Books").subcategory("Stationery").brand("Classmate")
                        .price(249.0).stock(500)
                        .description("A4 size, 172 pages each, ruled, smooth writing experience.")
                        .imageUrl("https://m.media-amazon.com/images/I/81DAhBaZ39L._AC_UF1000,1000_QL80_.jpg")
                        .build(),

                // ── BOOKS – Comics ───────────────────────────────────────────
                Product.builder()
                        .name("Amar Chitra Katha Box Set of 20")
                        .category("Books").subcategory("Comics").brand("ACK Media")
                        .price(1499.0).stock(90)
                        .description("Classic Indian mythology and history comics, great for kids.")
                        .imageUrl("https://m.media-amazon.com/images/I/81mqVZ4q2AL._AC_UF1000,1000_QL80_.jpg")
                        .build(),

                Product.builder()
                        .name("Iron Man and Spiderman")
                        .category("Books").subcategory("Comics").brand("Marvel Comics")
                        .price(1299.0).stock(91)
                        .description("Classic Marvel's Favourite IronMan Comics, great for kids.")
                        .imageUrl("https://i.marvelousnews.com/g/generated/Comics/Iron-Man/06/IM2026006_Cover__scaled_800.jpg")
                        .build(),

                // ── HOME – Furniture ──────────────────────────────────────────
                Product.builder()
                        .name("Nilkamal Plastic Folding Table 4 Seater")
                        .category("Home").subcategory("Furniture").brand("Nilkamal")
                        .price(3499.0).stock(40)
                        .description("Weather-resistant, 3 Year warranty, foldable for easy storage, ideal for indoor/outdoor.")
                        .imageUrl("https://www.nilkamalfurniture.com/cdn/shop/files/FESTIVEBRN_f65458d7-5872-4581-8e16-63f6108bc740.jpg?v=1773825261&width=720")
                        .build(),

                Product.builder()
                        .name("Wakefit Height Adjustable Study Desk")
                        .category("Home").subcategory("Furniture").brand("Wakefit")
                        .price(8999.0).stock(25)
                        .description("Electric height adjustment, cable management tray, anti-scratch surface.")
                        .imageUrl("https://rukmini1.flixcart.com/image/1500/1500/xif0q/office-study-table/x/v/v/120-rosewood-sheesham-metal-wstnotiqfw-wakefit-70-forsty-white-original-imahjcd6azbvsdfw.jpeg?q=70")
                        .build(),

                Product.builder()
                        .name("Homall Gaming Chair High Back PU Leather Desk Chair")
                        .category("Home").subcategory("Furniture").brand("Wakefit")
                        .price(84999.0).stock(15)
                        .description("High density shaping foam, more comfortable, elasticity resilience and service life. 1.8mm thick steel frame, more sturdy and stable. Pu Leather, skin friendly and wear resisting.")
                        .imageUrl("https://m.media-amazon.com/images/I/71WYmPG9WoL._SL1500_.jpg")
                        .build(),

                // ── HOME – Kitchen ──────────────────────────────────────────
                Product.builder()
                        .name("Prestige Iris 750W Mixer Grinder 3 Jars")
                        .category("Home").subcategory("Kitchen").brand("Prestige")
                        .price(2299.0).stock(100)
                        .description("750W motor, 3 stainless steel jars, anti-drip coupler, 5-year warranty.")
                        .imageUrl("https://shop.ttkprestige.com/media/catalog/product/0/2/0220-41350-IMG1.jpg")
                        .build(),

                Product.builder()
                        .name("Instant Pot Duo 7-in-1 Electric Pressure Cooker")
                        .category("Home").subcategory("Kitchen").brand("Instant Pot")
                        .price(7999.0).stock(35)
                        .description("Pressure cooker, slow cooker, rice cooker, steamer, saut\u00e9, warmer.")
                        .imageUrl("https://rukminim2.flixcart.com/image/480/640/xif0q/electric-cooker/9/j/0/-original-imahkq7fznzh5tmh.jpeg?q=20")
                        .build(),

                // ── HOME – Decor ───────────────────────────────────────────
                Product.builder()
                        .name("Tied Ribbons Wall Clock Modern Art")
                        .category("Home").subcategory("Decor").brand("Tied Ribbons")
                        .price(699.0).stock(150)
                        .description("Silent sweep movement, metal hands, MDF frame, 12 inch diameter.")
                        .imageUrl("https://rukminim2.flixcart.com/image/180/240/xif0q/wall-clock/o/j/o/-original-imahgvbzffec7hqj.jpeg?q=90")
                        .build(),

                Product.builder()
                        .name("Pepperfry Ceramic Pot Planter Set of 3")
                        .category("Home").subcategory("Decor").brand("Pepperfry")
                        .price(1199.0).stock(60)
                        .description("Hand-painted ceramic, drainage hole, modern minimalist design.")
                        .imageUrl("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQeFS1OaYZMDMfNC18xBbBdJCjVKYiuhxVa7A&s")
                        .build(),

                // ── SPORTS – Cricket ──────────────────────────────────────────
                Product.builder()
                        .name("SS Ton Kashmir Willow Cricket Bat")
                        .category("Sports").subcategory("Cricket").brand("SS")
                        .price(1599.0).stock(70)
                        .description("Grade 1 Kashmir willow, full size, ideal for turf and tape ball.")
                        .imageUrl("https://scssports.in/cdn/shop/files/1_ebd89908-8931-4c44-80bf-af6ed658b397.jpg?v=1779517038&width=1200")
                        .build(),

                Product.builder()
                        .name("SG Test Cricket Ball Red Pack of 3")
                        .category("Sports").subcategory("Cricket").brand("SG")
                        .price(799.0).stock(200)
                        .description("Premium quality, hand-stitched seam, 5.5 oz, tournament grade.")
                        .imageUrl("https://m.media-amazon.com/images/I/51cvQ2deD3L._AC_UF894,1000_QL80_.jpg")
                        .build(),

                // ── SPORTS – Football ─────────────────────────────────────────
                Product.builder()
                        .name("Nivia Storm Football Size 5")
                        .category("Sports").subcategory("Football").brand("Nivia")
                        .price(699.0).stock(120)
                        .description("32-panel design, machine stitched, butyl bladder, all-weather use.")
                        .imageUrl("https://m.media-amazon.com/images/I/71tB-sMtkQL._AC_UF894,1000_QL80_.jpg")
                        .build(),

                // ── SPORTS – Fitness ──────────────────────────────────────────
                Product.builder()
                        .name("Boldfit Resistance Bands Set of 5")
                        .category("Sports").subcategory("Fitness").brand("Boldfit")
                        .price(499.0).stock(300)
                        .description("5 resistance levels, latex-free, includes carrying bag and door anchor.")
                        .imageUrl("https://m.media-amazon.com/images/I/71jz4644-oL._AC_UF894,1000_QL80_.jpg")
                        .build(),

                Product.builder()
                        .name("Kore PVC Dumbbells 10KG Pair")
                        .category("Sports").subcategory("Fitness").brand("Kore")
                        .price(1299.0).stock(90)
                        .description("PVC coated, anti-slip grip, fixed weight, ideal for home workouts.")
                        .imageUrl("https://rukminim2.flixcart.com/image/480/640/xif0q/dumbbell/9/a/a/pair-of-10kg-10kg-x-2-rubber-10-vnh-original-imahf3jzxvurhqft.jpeg?q=90")
                        .build(),

                Product.builder()
                        .name("Strauss Yoga Mat Anti-Slip 6mm")
                        .category("Sports").subcategory("Fitness").brand("Strauss")
                        .price(699.0).stock(180)
                        .description("6mm thick, non-slip surface, moisture resistant, includes carry strap.")
                        .imageUrl("https://5.imimg.com/data5/QD/LD/PZ/SELLER-65506678/strauss-yoga-mat-6mm.png")
                        .build()
        );

        System.out.println("Seeding " + products.size() + " products...");
        productRepository.saveAll(products);
        System.out.println("Products seeded successfully!");
    }
}
