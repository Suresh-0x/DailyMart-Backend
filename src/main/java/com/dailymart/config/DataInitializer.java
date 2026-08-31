package com.dailymart.config;

import com.dailymart.entity.*;
import com.dailymart.entity.Role.ERole;
import com.dailymart.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final CouponRepository couponRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        seedRoles();
        seedAdminUser();
        seedCategoriesAndProducts();
        seedCoupons();
    }

    private void seedRoles() {
        for (ERole roleName : ERole.values()) {
            if (roleRepository.findByName(roleName).isEmpty()) {
                Role role = new Role();
                role.setName(roleName);
                roleRepository.save(role);
                log.info("Seeded role: {}", roleName);
            }
        }
    }

    private void seedAdminUser() {
        String adminEmail = "admin@dailymart.com";
        if (!userRepository.existsByEmail(adminEmail)) {
            Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                    .orElseThrow(() -> new IllegalStateException("ROLE_ADMIN not found"));
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new IllegalStateException("ROLE_USER not found"));

            User admin = User.builder()
                    .firstName("Super")
                    .lastName("Admin")
                    .email(adminEmail)
                    .password(passwordEncoder.encode("Admin@12345"))
                    .phone("9876543210")
                    .enabled(true)
                    .accountNonLocked(true)
                    .accountNonExpired(true)
                    .credentialsNonExpired(true)
                    .roles(new HashSet<>(Set.of(adminRole, userRole)))
                    .build();

            User savedAdmin = userRepository.save(admin);
            cartRepository.save(Cart.builder().user(savedAdmin).build());
            log.info("Seeded default admin user: {} (Password: Admin@12345)", adminEmail);
        }
    }

    private void seedCategoriesAndProducts() {
        if (categoryRepository.count() > 0) {
            return;
        }

        log.info("Seeding initial categories and products...");

        Category fruitsVeg = Category.builder()
                .name("Fruits & Vegetables")
                .slug("fruits-and-vegetables")
                .description("Farm fresh fruits and organic vegetables")
                .imageUrl("https://images.unsplash.com/photo-1610832958506-aa56368176cf?w=600")
                .displayOrder(1)
                .active(true)
                .build();

        Category dairy = Category.builder()
                .name("Dairy & Breakfast")
                .slug("dairy-and-breakfast")
                .description("Fresh milk, cheese, butter, yogurt and farm eggs")
                .imageUrl("https://images.unsplash.com/photo-1528750997573-59b89d56f4f7?w=600")
                .displayOrder(2)
                .active(true)
                .build();

        Category beverages = Category.builder()
                .name("Beverages & Cold Drinks")
                .slug("beverages-and-cold-drinks")
                .description("Fresh juices, tea, coffee, and soft drinks")
                .imageUrl("https://images.unsplash.com/photo-1551024709-8f23befc6f87?w=600")
                .displayOrder(3)
                .active(true)
                .build();

        Category snacks = Category.builder()
                .name("Snacks & Munchies")
                .slug("snacks-and-munchies")
                .description("Crispy chips, biscuits, dry fruits and chocolates")
                .imageUrl("https://images.unsplash.com/photo-1621939514649-280e2ee25f60?w=600")
                .displayOrder(4)
                .active(true)
                .build();

        Category staples = Category.builder()
                .name("Atta, Rice & Staples")
                .slug("atta-rice-and-staples")
                .description("Premium grains, pulses, spices and cooking oils")
                .imageUrl("https://images.unsplash.com/photo-1586201375761-83865001e31c?w=600")
                .displayOrder(5)
                .active(true)
                .build();

        List<Category> savedCategories = categoryRepository.saveAll(
                List.of(fruitsVeg, dairy, beverages, snacks, staples)
        );

        // Seed Sample Products
        Category catFruits = savedCategories.get(0);
        Category catDairy = savedCategories.get(1);
        Category catBev = savedCategories.get(2);
        Category catSnacks = savedCategories.get(3);
        Category catStaples = savedCategories.get(4);

        List<Product> products = new ArrayList<>();

        products.add(createSampleProduct(
                "Fresh Organic Apples (Shimla)", "fresh-organic-apples-shimla",
                "Crisp, sweet, and naturally grown red apples from Shimla orchards.",
                "Fresh crisp red apples - 1kg pack", "APL-SHM-01", "DailyFresh",
                catFruits, new BigDecimal("180.00"), new BigDecimal("149.00"),
                50, true, new BigDecimal("4.8"), 45,
                List.of("https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?w=600")
        ));

        products.add(createSampleProduct(
                "Fresh Alphonso Mangoes", "fresh-alphonso-mangoes",
                "Naturally ripened Ratnagiri Alphonso mangoes, sweet and aromatic.",
                "Sweet Alphonso Mangoes - 1 Dozen", "MNG-RAT-02", "DailyFresh",
                catFruits, new BigDecimal("600.00"), new BigDecimal("499.00"),
                30, true, new BigDecimal("4.9"), 82,
                List.of("https://images.unsplash.com/photo-1553279768-865429fa0078?w=600")
        ));

        products.add(createSampleProduct(
                "Farm Fresh Full Cream Milk (1L)", "farm-fresh-full-cream-milk-1l",
                "Pasteurized homogenized cow milk rich in calcium and essential vitamins.",
                "Pasteurized Full Cream Milk - 1 Liter", "MLK-FC-01", "Amul",
                catDairy, new BigDecimal("68.00"), new BigDecimal("64.00"),
                100, true, new BigDecimal("4.7"), 120,
                List.of("https://images.unsplash.com/photo-1563636619-e9143da7973b?w=600")
        ));

        products.add(createSampleProduct(
                "Farm Fresh Brown Eggs (Pack of 12)", "farm-fresh-brown-eggs-12",
                "High protein organic farm fresh brown eggs from free-range hens.",
                "Organic Free-range Brown Eggs - 12 count", "EGG-BRN-12", "Eggoz",
                catDairy, new BigDecimal("140.00"), new BigDecimal("119.00"),
                60, false, new BigDecimal("4.6"), 38,
                List.of("https://images.unsplash.com/photo-1582722872445-44dc5f7e3c8f?w=600")
        ));

        products.add(createSampleProduct(
                "Cold-Pressed Pure Orange Juice (1L)", "cold-pressed-pure-orange-juice-1l",
                "100% natural, freshly squeezed orange juice with no added sugar or preservatives.",
                "100% Pure Orange Juice - No Sugar", "JUC-ORG-01", "Raw Pressery",
                catBev, new BigDecimal("160.00"), new BigDecimal("135.00"),
                40, true, new BigDecimal("4.5"), 29,
                List.of("https://images.unsplash.com/photo-1613478223719-2ab802602423?w=600")
        ));

        products.add(createSampleProduct(
                "Premium Roasted California Almonds (500g)", "premium-roasted-california-almonds-500g",
                "Lightly salted, oven roasted crunchy California almonds rich in vitamin E.",
                "Crunchy Roasted California Almonds 500g", "NUT-ALM-500", "Happilo",
                catSnacks, new BigDecimal("550.00"), new BigDecimal("449.00"),
                45, true, new BigDecimal("4.8"), 64,
                List.of("https://images.unsplash.com/photo-1508061252966-ef7fe9f5085f?w=600")
        ));

        products.add(createSampleProduct(
                "Organic Royal Basmati Rice (5kg)", "organic-royal-basmati-rice-5kg",
                "Extra long grain aged aromatic Basmati rice, ideal for biryani and pulao.",
                "Extra Long Grain Aged Basmati Rice 5kg", "RIC-BAS-5K", "India Gate",
                catStaples, new BigDecimal("750.00"), new BigDecimal("620.00"),
                35, true, new BigDecimal("4.9"), 95,
                List.of("https://images.unsplash.com/photo-1586201375761-83865001e31c?w=600")
        ));

        for (Product product : products) {
            productRepository.save(product);
        }

        log.info("Seeded {} sample products successfully.", products.size());
    }

    private Product createSampleProduct(String name, String slug, String description,
                                        String shortDesc, String sku, String brand,
                                        Category category, BigDecimal origPrice, BigDecimal sellPrice,
                                        int stock, boolean featured, BigDecimal avgRating, int reviewCount,
                                        List<String> imageUrls) {
        BigDecimal discount = origPrice.subtract(sellPrice)
                .divide(origPrice, 2, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        Product p = Product.builder()
                .name(name)
                .slug(slug)
                .description(description)
                .shortDescription(shortDesc)
                .sku(sku)
                .brand(brand)
                .category(category)
                .originalPrice(origPrice)
                .sellingPrice(sellPrice)
                .discountPercent(discount)
                .stockQuantity(stock)
                .featured(featured)
                .active(true)
                .averageRating(avgRating)
                .totalReviews(reviewCount)
                .build();

        List<ProductImage> images = new ArrayList<>();
        for (int i = 0; i < imageUrls.size(); i++) {
            images.add(ProductImage.builder()
                    .product(p)
                    .imageUrl(imageUrls.get(i))
                    .altText(name)
                    .primary(i == 0)
                    .displayOrder(i)
                    .build());
        }
        p.setImages(images);
        return p;
    }

    private void seedCoupons() {
        if (couponRepository.count() > 0) {
            return;
        }

        log.info("Seeding initial coupons...");

        Coupon welcome10 = Coupon.builder()
                .code("WELCOME10")
                .description("Flat 10% OFF on your first purchase above ₹299")
                .discountType(Coupon.DiscountType.PERCENTAGE)
                .discountValue(new BigDecimal("10.00"))
                .minOrderAmount(new BigDecimal("299.00"))
                .maxDiscountAmount(new BigDecimal("100.00"))
                .expiryDate(java.time.LocalDate.now().plusMonths(6))
                .usageLimit(5000)
                .usageCount(0)
                .active(true)
                .build();

        Coupon daily50 = Coupon.builder()
                .code("DAILY50")
                .description("Flat ₹50 OFF on orders above ₹499")
                .discountType(Coupon.DiscountType.FLAT)
                .discountValue(new BigDecimal("50.00"))
                .minOrderAmount(new BigDecimal("499.00"))
                .maxDiscountAmount(new BigDecimal("50.00"))
                .expiryDate(java.time.LocalDate.now().plusMonths(6))
                .usageLimit(3000)
                .usageCount(0)
                .active(true)
                .build();

        Coupon festive20 = Coupon.builder()
                .code("FESTIVE20")
                .description("20% Mega Festival Discount on orders above ₹999")
                .discountType(Coupon.DiscountType.PERCENTAGE)
                .discountValue(new BigDecimal("20.00"))
                .minOrderAmount(new BigDecimal("999.00"))
                .maxDiscountAmount(new BigDecimal("250.00"))
                .expiryDate(java.time.LocalDate.now().plusMonths(3))
                .usageLimit(2000)
                .usageCount(0)
                .active(true)
                .build();

        couponRepository.saveAll(List.of(welcome10, daily50, festive20));
        log.info("Seeded 3 starter coupons successfully.");
    }
}
